## Modern Android Architecture (Part 2): The Lego Architecture: Divide and Conquer, Taken to the Extreme

> **Series Navigation**: [Article 1: A Decade of Android Architecture Evolution](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8) | [Article 3: Refactoring a Product Detail Page with Lego Architecture](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

### Foreword: The Pain of Bad Code Doesn't Come from Architecture

In the previous article, we discussed that architecture is just "technique" — it only solves the rough partitioning of code, which is far from sufficient. In this article, we face the real pain points in project implementation and propose a practical divide-and-conquer methodology — Lego Architecture.

### 0. Inspiration: Reflections from the Pain of Architecture Migration

I've gone through three large-scale Android architecture refactorings, each time feeling like I've shed a layer of skin:

- From MVC to MVP: The team spent two months creating a heavy set of `BaseActivity` / `BaseFragment` / `BasePresenter`.
- From MVP to MVVM: All Base classes had to be rewritten, a large number of utility classes deeply coupled with Base were forced to change, affecting 40% of business code.
- When MVI started to become popular, we were completely exhausted.

What made us so passive? Is migrating to MVI a one-time solution? Will future architectures like MV-Whatever repeat this nightmare? If migration is inevitable, which code can remain unchanged?

This pain forced us to ask a fundamental question: **What is the ultimate answer to architecture?**

Sitting in front of my computer, I suddenly remembered the Lego bricks I played with as a child.

Why can a few basic bricks build houses, cars, spaceships? Why can bricks from 10 years ago still perfectly fit with new sets?
At that moment, I seemed to see the answer to all architectural problems.

The most amazing thing about Lego is not the cool finished products, but those basic 1×1 and 2×4 bricks:

- Only a few studs and tubes, no predefined functions.
- Follow a globally unified connection standard, perfectly compatible with any other Lego brick.
- Can be used anywhere in any model.
- Interface specifications have never changed since 1958.

In contrast, those specialized parts designed for specific models — like the curved hull of the Millennium Falcon — are almost useless outside of that one model. Once removed, they become waste.

**Lego's core philosophy is: Minimum granularity = Maximum reusability = Maximum flexibility.**
**Specialized parts become useless outside their model. The same principle applies to code reuse.**

From this, we've distilled a software engineering approach called **Lego Architecture** — it doesn't aim to replace MVVM or MVI, but provides a set of **programming philosophies and engineering disciplines** about "how to split, how to accumulate, how to govern."

***

### I. Base Classes: Avoid If Possible, Keep Minimal If Necessary

In the three architecture refactorings, what pained me the most wasn't business logic, but those seemingly "convenient" Base classes.

What's the problem? **We tied too many unnecessary things to Base.**

#### 1.1 The Correct Use of Base: Avoid If Possible

Base classes have only one valid reason to exist: **Provide a seam for lifecycle-related capabilities needed by 90% of pages.**

For example:

- Automatic page entry/exit tracking for analytics
- Callback distribution for permission requests

Beyond that, no business logic, page initialization order, or utility class references should go into Base.

#### 1.2 Strongly Disliked: Constraining Page Initialization Workflow in Base

I've seen Base classes like this:

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCallerData()
        initView()
        initPageData()
        initObservers()
    }
    abstract fun initView()
    abstract fun initPageData()
    abstract fun initCallerData()
    abstract fun initObservers()
}
```

This is **violent constraint** on subclasses. When you don't know the initialization order desired by upper-level pages (some want to initialize observers first, others want to initialize views first), this template method becomes a shackle and source of redundant confusion.

**Good Base classes shouldn't dictate order, only provide optional capabilities.** Even `initView` shouldn't exist — let each page write its own initialization in `onCreate`, making it clear and explicit.

#### 1.3 Pluggable Bricks Instead of Base

Observation reveals that Base classes often contain many methods or code blocks that could be in xxxUtils — permission requests, soft keyboard management, network detection... They're stuffed into Base only because "multiple pages need them," not because they're truly lifecycle-related.

Better approach: Base is just an empty shell; all functionality is provided through pluggable bricks.

For example, if you need permission request capability, instead of writing it in Base, use an independent PermissionHelper directly where needed:

```kotlin
class PermissionHelper(private val activity: AppCompatActivity) {
    fun request(permission: String, onGranted: () -> Unit) { ... }
}
```

Any page, Fragment, or Dialog can use this brick directly without depending on inheritance. Base can even be an open class with nothing inside.

### II. From Coarse-Grained to Extreme Divide-and-Conquer: Lego Principles

All mainstream architectures in the industry (MVC, MVP, MVVM, MVI, Clean Architecture...) essentially embody **divide-and-conquer thinking** — they split code by responsibility, layer, or data flow.

But their granularity is usually **coarse**:

- A layer (View, ViewModel, Model)
- A module (feature, domain, data)
- A role (Presenter, UseCase)

They don't answer a crucial question: **How fine-grained should we split?**

Lego bricks provide a physical-world answer: keep splitting until reaching **indivisible basic particles**. A 2×4 brick has a unified interface, can be used anywhere in any Lego model, and hasn't changed in 68 years.

**Lego Architecture is the ultimate practice of divide-and-conquer thinking: not just splitting at the MVX level, but splitting infinitely across all levels and dimensions until each unit does only one thing, with stable interfaces and no redundant functionality.**

This principle can be applied to:

- UI layer: Each `ViewHolder` of a `RecyclerView` is an independent brick
- Logic layer: Each `UseCase` encapsulates only one business scenario
- Utility layer: Each utility method does only one thing
- State layer: Each `State` describes only one independent domain

The stopping condition for splitting is simple: stop when you can't give the unit a smaller, more accurate name. If a class or method name contains words like "and", "with", or "as well as", it means it can be split further.

This is the fundamental difference between Lego Architecture and other architectures:

> Other architectures tell you "which layer code should go to".
> Lego Architecture tells you "how small code should be split and how to keep splitting".

***

### III. Splitting Examples Under Lego Thinking

Let's look at some concrete examples of how code is split into minimal particles following Lego principles.

#### 3.1 Utility Classes: From Kitchen Sink to Atomic Bricks

**Counterexample**: A `DateUtils` with a dozen methods for time formatting, timestamp conversion, relative time calculation, timezone handling... hundreds of lines of code.

**Lego Split**:

- `DateFormatUtils`: Only formats dates into strings
- `TimestampConverter`: Only converts between timestamps and date objects
- `RelativeTimeCalculator`: Only calculates "just now", "a few minutes ago", etc.
- `TimeZoneHelper`: Only handles timezone-related operations

Each class has a single responsibility, clear method names, and is easy to test and reuse.

#### 3.2 ViewModel: From God Object to Service Composition

**Counterexample**: A `HomeViewModel` containing carousel data, recommendation lists, user info, shopping cart count, notification unread count... 5000 lines of code, changing one place may affect unrelated fields.

**Lego Split**:

- `HomeBannerViewModel`: Only responsible for carousel data
- `HomeRecommendViewModel`: Only responsible for recommendation lists
- `ShoppingCartViewModel`: Universal service, reusable across multiple pages
- `NotificationViewModel`: Universal service, reusable across multiple pages

Combined usage in Activity:

```kotlin
private val bannerVM: HomeBannerViewModel by viewModels()
private val cartVM: ShoppingCartViewModel by viewModels()
private val notificationVM: NotificationViewModel by viewModels()
```

Each ViewModel can be independently tested and reused. Even `ShoppingCartViewModel` can be observed simultaneously in product detail pages, cart pages, and home pages.

#### 3.3 Intent/MVI State: From Explosion to Grouping

**Counterexample**: A `HomeIntent` sealed class stuffed with 200+ intents, including carousel clicks, recommendation list load more, user avatar clicks...

**Lego Split**: Align Intents with ViewModels, each ViewModel corresponds to an Intent group.

```kotlin
sealed class BannerIntent {
    object Load : BannerIntent()
    data class Click(val position: Int) : BannerIntent()
}
sealed class RecommendIntent {
    object LoadMore : RecommendIntent()
    data class Click(val item: RecommendItem) : RecommendIntent()
}
```

Same for State: Instead of stuffing all fields into one `HomeState`, split into `BannerState`, `RecommendState`, etc., with each ViewModel managing its own state.

#### 3.4 UseCase: One Class Per Scenario

**Counterexample**: `UserUseCase` with five unrelated operations: login, register, change password, get user info, upload avatar...

**Lego Split**: Each UseCase encapsulates only one complete business scenario.

- `LoginUseCase`
- `RegisterUseCase`
- `UpdatePasswordUseCase`
- `FetchUserInfoUseCase`

This way, any page only needs to depend on the UseCase it needs, not the entire kitchen sink.

***

### IV. Discovery and Iteration of Utility Classes: Good Bricks Grow Naturally

Bricks in Lego Architecture aren't designed in one go; they emerge naturally during continuous development and refactoring — essentially a process of evolving from specialized parts to basic parts.

#### 4.1 Private Bricks: Write as You Use

During feature development, you find a piece of logic that can be encapsulated (like a price formatting method), but you're unsure if it's universal. Write it in the current module's `utils` package as a **private brick**.

```kotlin
// feature_goods/utils/PriceFormatUtils.kt
internal fun formatPrice(priceYuan: String): String = "¥$priceYuan"
```

It's only used within this module and not exposed to other modules.

#### 4.2 Shared Bricks: Regular Review, Extract for Reuse

The project conducts a review refactoring every quarter. The team scans private utilities under each `feature_xxx/utils`, finds classes that are **used by at least two modules** or **clearly have universal value**, and moves them to the `common/utils` module to become **shared bricks**.

Meanwhile, the original private bricks disappear (or become thin, leaving only a delegation to the shared brick). This step requires unit testing and Code Review to ensure no functionality is broken during the move.

#### 4.3 Remote Bricks: Battle-Tested, Become Assets

After a shared brick has been used online for half a year or a year and proven stable by millions of users, it can be extracted from the `common` module, published to a Maven repository, and become a **remote brick** for all company projects to depend on directly.

At this point, it becomes a **"write-once, use-forever tool"** — like `StringUtils`, `NetworkUtils`, `ScreenUtils`. You'll never need to rewrite them; just upgrade the version number.

#### 4.4 Why Must We Split to Minimum Granularity?

Because only when split small enough can you easily spot during regular scans: "Oh, this formatting and that formatting can be merged" or "This validation logic has appeared three times." If they're scattered in hundreds-of-lines classes or duplicated across different Activities, you'll never find reuse opportunities.

**The infinite splitting in Lego Architecture isn't for splitting's sake; it's to make reuse opportunities visible, turning 'passive discovery' into 'active scanning'.**

***

### V. Conclusion: Lego Architecture, Embracing Change with Stability

Lego Architecture isn't meant to replace your existing MVVM or MVI; it's a set of **programming disciplines and governance philosophies** that require you to do the following on top of any architecture:

1. **Minimal Base Classes**: Avoid if possible; if unavoidable, only include the most basic lifecycle seams, never dictate initialization order.
2. **Infinite Splitting**: Split code across all levels (UI, logic, state, utilities) into the smallest stable particles until each unit does only one thing.
3. **Continuous Utility Iteration**: Private → Shared → Remote, letting good bricks grow naturally and eventually become "write-once" technical assets.

When you truly achieve these, you'll find:

- Architecture migration is no longer a disaster: Base classes are thin shells, business logic is in pluggable bricks, changing architecture only changes the container.
- Code reuse is no longer a slogan: Minimum granularity naturally drives reuse; someone may have already provided a remote brick before you even write it.
- Maintenance costs drop exponentially: Each brick has a single responsibility, changes don't ripple to unrelated areas.
- Team collaboration is easy: Newcomers only need to learn brick usage, not understand thousands of lines of god classes.

This is Lego Architecture — the ultimate practice of divide-and-conquer, a methodology that embraces change with stability.

**Next Article Preview**: We'll use a real e-commerce App product detail page to demonstrate the complete process of Lego Architecture from splitting to assembly.

***

**Related Reading**:

- [Article 1: A Decade of Android Architecture Evolution: What Problem Are We Really Solving?](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8)
- [Article 3: Refactoring a Product Detail Page with Lego Architecture: From 3000 Lines to 15 Standalone Components](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

