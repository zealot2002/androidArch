## Modern Android Architecture (Part 4): Design Patterns — The Glue of Lego Architecture

> **Series Navigation**: [Article 1: A Decade of Android Architecture Evolution](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8) | [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5) | [Article 3: Refactoring a Product Detail Page with Lego Architecture](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)
>
> **Project Repository**: <https://github.com/zealot2002/androidArch>

***

## Foreword: Bricks Are Ready, How to Assemble Them Stably?

In the first three articles, we followed a clear path:

- **Article 1** pointed out: Architecture is just "technique" — it can manage layering, but not splitting granularity.
- **Article 2** proposed Lego Architecture: Split infinitely to minimum particles, making reuse visible.
- **Article 3** proved with practical product detail page: Bricks like Mapper, Assembler, ListItem can indeed split a 3000-line disaster into 15 independent components.

But there's a question hinted at the end of Article 3 — **Splitting is only the first step; assembly determines whether this system can run long-term.**

Two pages both require "login before operation" — do you plan to copy-paste two sets of `if (isLogin)` checks? Three types of posters (product, social, store) share the same "render → screenshot → preview → share" workflow, but with completely different UI layouts — do you plan to write three `when` branches in Activity, each hundreds of lines long?

This is where **design patterns** come in. They are not another "silver bullet architecture," but the **glue** between Lego bricks — using minimal, proven connection methods to stably assemble already-split particles together.

The 23 GoF patterns and more evolving patterns each solve different "assembly" problems: Factory is responsible for **creating** the right bricks, Strategy for **switching** replaceable behaviors, Adapter for **connecting** incompatible interfaces, Observer for **responding** to state changes, Template Method for **solidifying** invariant processes... Due to space constraints, this article won't cover all of them, but will select two representative cases from the demo project — **Observer** and **Template Method** — to illustrate how design patterns generally work in Lego Architecture.

***

## I. Observer Pattern: LoginRouter — The "Broadcast Station" for Login State

### 1.1 Pain Point: Login Interception Scattered Everywhere

In e-commerce apps, many operations depend on login state: favorite products, add to cart, buy now, post comments...

The crudest approach is to repeat this check in every click event:

```kotlin
if (UserManager.isLogin()) {
    doSomething()
} else {
    startActivity(Intent(this, LoginActivity::class.java))
}
```

The problems are:

- **Repetition**: Ten buttons, ten copies of the same check logic.
- **Broken flow**: After successful login, the original operation is often "lost" — the user has to click the button again.
- **Coupling**: Some projects even write post-login navigation directly into `LoginActivity`, coupling the login module with other modules.

Lego Architecture requires: **Extract "execute after login" as an independent, reusable brick.**

### 1.2 Solution: Observer Pattern

`LoginRouter` consists of two observable sources (`LifecycleOwner`, `LoginStateLiveData`) and a `pendingBlock`:

1. **`LoginStateLiveData`** — Subscribe to login state. Publish `true` after successful login to trigger pending callbacks.
2. **`LifecycleOwner`** — Subscribe to page lifecycle. Clear pending when page is destroyed to avoid leaks; also clear on `ON_RESUME` — when user presses back from login page without logging in, pending operations should be abandoned.
3. **`pendingBlock`** — Store the "execute after login" operation. Suspended when not logged in, automatically `invoke()` after successful login.

Core code:

```kotlin
class LoginRouter(private var context: Context) {
    private var pendingBlock: (() -> Unit)? = null

    init {
        if (context is LifecycleOwner) {
            // Observer: Login success → execute pending operation
            LoginStateLiveData.observe(context as LifecycleOwner) {
                if (it) {
                    pendingBlock?.invoke()
                    pendingBlock = null
                }
            }
            // ON_RESUME: User returns from login page but not logged in (e.g., pressed back), abandon pending
            // ON_DESTROY: Page destroyed, clean up pending
            (context as LifecycleOwner).lifecycle.addObserver(
                LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME,
                        Lifecycle.Event.ON_DESTROY -> pendingBlock = null
                        else -> {}
                    }
                }
            )
        }
    }

    fun runBlock(block: () -> Unit) {
        if (context is LifecycleOwner) {
            if (LoginStateLiveData.value == true) {
                block.invoke()
            } else {
                pendingBlock = block
                AppRouter.openLogin(context)
            }
        } else {
            block.invoke()
        }
    }
}
```

After successful login, the login page only needs to update the global state:

```kotlin
// LoginActivity
LoginStateLiveData.value = true
finish()
```

**This is the complete Observer Pattern loop:**

```
LoginActivity login successful
       │
       ▼
LoginStateLiveData.value = true   ← Subject publishes state
       │
       ▼
LoginRouter receives notification ← Observer
       │
       ▼
pendingBlock?.invoke()            ← Automatically execute suspended operation
```

### 1.3 Usage in Product Detail Page

In the refactored `GoodsDetailActivity` from Article 3, favorite, add to cart, and buy now are all handled with one line through `LoginRouter`:

```kotlin
private val loginRouter: LoginRouter by lazy { LoginRouter(this) }

// Favorite
loginRouter.runBlock {
    // Note: not a page navigation, does nothing if not logged in
    favViewModel.setFavorite(currentSpuId, targetFavorite)
}

// Add to cart
loginRouter.runBlock {
    ToastUtils.show(this, getString(R.string.goods_action_add_cart_hint))
}

// Buy now → Navigate to confirm order page after login
loginRouter.runBlock {
    AppRouter.openConfirmOrder(this)
}
```

The difference between `LoginRouter` and a route interceptor: interceptors usually only do unified page navigation, while `runBlock` can pending any operation — favorite, API request, show Toast, not limited to opening new pages. Subsequent actions are still declared locally in the page, and `LoginActivity` no longer needs to act as a transit hub. Module boundaries are clear and decoupled.

### 1.4 Origin and Boundaries

`LoginRouter` wasn't designed upfront; it emerged from project pain points.

After connecting favorite, add to cart, and buy now to the detail page, the same logic repeatedly appeared across multiple buttons: check login, navigate to login page, continue original operation after success. Interceptors could only intercept navigation uniformly, not handle "continue favorite after login"; writing destinations into `LoginActivity` would couple the login module with business logic. After several rounds of copy-paste, the pain point became clear — **missing a common brick that can pending any follow-up action.**

Extract, observe, abstract: `LoginRouter` combines login guard and pending continuation into one brick, using `LoginStateLiveData` and `LifecycleOwner` to coordinate timing, elegantly solving this pain point.

Lego Architecture not only requires the ability to split, but also to see patterns in repetition and discover bricks in pain points — `LoginRouter` is one such refinement.

***

## II. Template Method Pattern: BillActivity — Fixed Process, Variable Content

### 2.1 Pain Point: Three Poster Types, One Workflow

We added a **poster sharing** feature to the demo project — users can generate beautiful posters for products, social posts, or store pages, save to album, or share to WeChat.

The three poster types have completely different UI layouts, but **the page-level workflow is identical**:

```
Load data → Select Render → Bind view → Wait for render ready → Screenshot → Preview → Save/Share
```

If we write three sets of rendering logic in `BillActivity` using `when (billCase)`, the Activity will quickly bloat; if we write a separate Activity for each poster type, the screenshot, preview, and save code will be duplicated three times.

Template Method Pattern solution: **Solidify the "invariant process" in a template, leave "variable parts" for subclasses to implement.**

### 2.2 Architecture Layers: Interface → Abstract Template → Concrete Implementation

#### Step 1: Define Brick Interface

```kotlin
interface BillRender {
    fun onBindView(data: Any, listener: Listener)
    fun getBillView(): View

    interface Listener {
        fun screenReady(bgBitmap: Bitmap? = null)
    }
}
```

`BillRender` is a standard interface — all poster Render bricks must follow these two methods.

#### Step 2: Abstract Template Class Solidifies Common Skeleton

```kotlin
abstract class BaseBillRender<T, B : ViewBinding>(private val context: Context) : BillRender {

    private lateinit var binding: B

    // Subclasses only implement this method — fill specific UI
    abstract fun onRenderView(data: T, binding: B, listener: BillRender.Listener)

    init {
        // Template method: Reflection automatically inflates corresponding ViewBinding
        val superclass = javaClass.genericSuperclass as ParameterizedType
        val bindingClass = superclass.actualTypeArguments[1] as Class<B>
        val inflate = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = inflate.invoke(null, (context as Activity).layoutInflater) as B
    }

    override fun onBindView(data: Any, listener: BillRender.Listener) {
        onRenderView(data as T, binding, listener)  // Call subclass implementation
    }

    override fun getBillView(): View = binding.root
}
```

**This is the core of Template Method Pattern:**

- `BaseBillRender` defines the **algorithm skeleton** (inflate Binding → call `onRenderView` → expose `getBillView`).
- Subclasses only need to implement **one hook:** **`onRenderView`**, filling in their own UI logic.
- Common ViewBinding initialization is done through generics + reflection, with zero boilerplate code for subclasses.

#### Step 3: Concrete Subclasses Only Care About Their UI, Need Only Implement One Method

Taking product poster as example:

```kotlin
class GoodsBillRender(context: Context) :
    BaseBillRender<GoodsBillData, LayoutBillGoodsBinding>(context) {

    override fun onRenderView(
        data: GoodsBillData,
        binding: LayoutBillGoodsBinding,
        listener: BillRender.Listener,
    ) {
        binding.tvTitle.text = data.title
        binding.tvPrice.text = data.price
        // ... Fill product-specific fields

        // Wait for layout drawing + network image loading to complete
        binding.ivCover.loadNetworkImage(data.imageUrl, onSuccess = { markReady() })
        binding.ivQrCode.loadNetworkImageCircle(data.miniProgramCodeUrl, onSuccess = { markReady() })
        // Call screenReady when all ready
    }
}
```

`markReady()` internally maintains a counter (product poster needs to wait for layout drawing + cover image + QR code = 3 items), and only triggers `screenReady` when the count is complete, ensuring complete content during screenshot.

`SocialBillRender` and `ShopBillRender` have the same structure, each binding different layouts and Data types. **Adding a new poster type only requires adding a new Render subclass + one Layout XML, zero modifications to existing code.**

#### Step 4: Factory Selects Concrete Brick

```kotlin
object BillRenderFactory {
    fun make(context: Context, case: Int): BillRender {
        return when (case) {
            RouterConstants.BILL_CASE_SOCIAL -> SocialBillRender(context)
            RouterConstants.BILL_CASE_SHOP -> ShopBillRender(context)
            else -> GoodsBillRender(context)
        }
    }
}
```

### 2.3 BillActivity: Orchestrating the Fixed Workflow

`BillActivity` itself contains no specific poster UI logic; it only **orchestrates the template process**:

```kotlin
class BillActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ... Initialize views, share button

        // 1. Factory selects Render brick
        billRender = BillRenderFactory.make(this, billCase)
        binding.flBillContainer.addView(billRender.getBillView(), ...)

        // 2. Load data + bind view
        val billData = BillDataLoader.load(billCase, billId)
        billRender.onBindView(billData, object : BillRender.Listener {
            override fun screenReady(bgBitmap: Bitmap?) {
                captureBillSnapshot()  // 3. Render ready → screenshot
            }
        })
    }

    private fun captureBillSnapshot() {
        // 4. View → Bitmap → Preview display
        val bitmap = BillBitmapUtils.viewToBitmap(billRender.getBillView())
        binding.ivBill.setImageBitmap(bitmap)
        // 5. Show bottom save/share bar
    }
}
```

The responsibility boundaries are clear throughout the page:

| Component                            | Responsibility                                           | Pattern                    |
| ------------------------------------ | -------------------------------------------------------- | -------------------------- |
| `BillDataLoader`                     | Load Mock data by case                                   | Simple Factory             |
| `BillRenderFactory`                  | Select specific Render implementation                    | Simple Factory             |
| `BaseBillRender`                     | Solidify inflate + bind skeleton                         | **Template Method**        |
| `GoodsBillRender`, etc.              | Fill specific UI + async ready detection                 | Template Method hooks      |
| `BillActivity`                       | Orchestrate load → render → screenshot → preview → share | Process controller         |
| `BillBitmapUtils` / `BillImageSaver` | Screenshot and save                                      | Independent utility bricks |

### 2.4 Echo with Article 2: When to Use Template Method?

In Article 2, we harshly criticized this kind of Base:

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCallerData()   // Forced order
        initView()
        initPageData()
        initObservers()
    }
    abstract fun initView()
    abstract fun initPageData()
    // ...
}
```

Why is the template method in `BaseBillRender` acceptable, but not in `BaseActivity`?

| <br />                                | Bad BaseActivity Template                                 | Good BaseBillRender Template                                   |
| ------------------------------------- | --------------------------------------------------------- | -------------------------------------------------------------- |
| **Is the process really consistent?** | Different pages have very different initialization orders | All posters must inflate → bind → render → ready               |
| **Constrains order or structure?**    | Forces `initView` before `initData`                       | Only specifies "call `onRenderView` when binding data"         |
| **Subclass freedom**                  | Kidnapped by four abstract methods                        | Only need to implement one `onRenderView`                      |
| **Is it pluggable?**                  | Must inherit Base to use                                  | Just implement `BillRender` interface, no inheritance required |

**The judgment is simple: Only use template method when the algorithm skeleton is truly consistent across multiple subclasses; if you're just "incidentally" stuffing a bunch of optional initialization steps into Base, it's a shackle, not a template.**

***

## III. Gluing Approaches from Two Cases

The two cases above demonstrate two common "gluing" directions of design patterns in Lego Architecture — **responding to changes** and **solidifying processes**. Other patterns solve different dimensions of assembly problems like creation, replacement, adaptation, etc., with similar thinking.

| Dimension              | Observer (LoginRouter)                                   | Template Method (BillRender)                                                  |
| ---------------------- | -------------------------------------------------------- | ----------------------------------------------------------------------------- |
| **Problem Solved**     | When A's state changes, B needs to respond automatically | Multiple subclasses share the same algorithm skeleton, only some steps differ |
| **Connection Method**  | Subscription / Callback                                  | Inherit abstract class + implement hook methods                               |
| **Coupling Direction** | Subject doesn't know who's observing (unidirectional)    | Subclass depends on parent skeleton (unidirectional)                          |
| **Lego Position**      | Cross-module event broadcasting brick                    | Common skeleton brick for similar Renders                                     |
| **Extension Method**   | Add new observers without modifying Subject              | Add new subclasses without modifying template skeleton                        |

Regardless of pattern, there's a common prerequisite: **They are used to "glue" bricks after Lego splitting — not to replace splitting itself.**

If you haven't split login logic into `LoginRouter`, Observer Pattern won't save you — you're just writing LiveData observations in three Activities. If you haven't split the three posters into independent Renders, Template Method won't save you either — `BaseBillRender` will be filled with `if (case == SOCIAL)` branches. Same for Factory, Strategy, and other patterns — **Split first, then glue, order matters.**

Beyond the two cases detailed in this article, the demo project has patterns working silently throughout — they're not the stars, but they're connectors between bricks:

| Pattern             | Location in Project                   | What It Glues                                                                  |
| ------------------- | ------------------------------------- | ------------------------------------------------------------------------------ |
| **Observer**        | `LoginRouter`                         | Login state changes → cross-page pending follow-up operations                  |
| **Template Method** | `BaseBillRender`                      | Multiple posters share inflate → bind → render skeleton                        |
| **Simple Factory**  | `BillRenderFactory`, `BillDataLoader` | Create corresponding Render / data by `billCase`                               |
| **Strategy**        | `BillRender` and its implementations  | Switch different poster rendering strategies under the same screenshot process |
| **Adapter**         | `GoodsDetailAdapter`                  | 14 types of `ListItem` → RecyclerView multi-type rendering                     |

***

## IV. Complete Lego Architecture System: Four Articles Connected

At this point, the four articles form a complete narrative:

```
Article 1: Problem Awareness
  └─ Architecture is just "technique", connection count determines complexity

Article 2: Splitting Methodology
  └─ Lego Architecture = Infinite splitting to minimum particles + Governance iteration

Article 3: Splitting Practice
  └─ Product Detail Page: ListItem + Mapper + Assembler + ViewModel

Article 4: The Glue
  └─ Design Patterns: On top of splitting, use appropriate patterns to stably assemble bricks (this article focuses on Observer and Template Method)
```

The complete Lego Architecture system can be summarized as:

- **One Axiom**: Divide-and-conquer + Single Responsibility
- **Multiple Theorems**: Governance thinking, tool iteration (Private→Shared→Remote), reuse discovery
- **One Practice**: 15 independent components in product detail page
- **One Layer of Glue**: Design patterns — this article focuses on Observer and Template Method; the project also uses Factory, Strategy, Adapter patterns for connection (see table above)

When you truly build applications with Lego thinking, you'll find:

- **Complexity doesn't come from the application itself**, but from not splitting it into small enough bricks.
- **Design patterns aren't for showing off**, but for selecting appropriate patterns by scenario after splitting, using minimal connections to stably assemble bricks.
- **Good code isn't written by learning one architecture**, but by having divide-and-conquer awareness, governance discipline, pattern intuition, and the ability to spot reusable bricks.

***

## Series Conclusion

Four articles, from "Architecture Evolution History" to "Lego Splitting Methodology", to "Product Detail Page Practice", and finally to "Design Pattern Glue" — we've tried to answer a consistent question:

**How to truly improve Android code quality?**

The answer isn't in the labels of MVVM or MVI, but in daily programming discipline: split until you can't split anymore, glue only when needed, govern for sustainable iteration.

May your codebase be like a box of Lego — each brick has clear responsibilities, stable interfaces, can be combined freely, and can still build new models after many years.

***

**Related Reading**:

- [Article 1: A Decade of Android Architecture Evolution: What Problem Are We Really Solving?](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8)
- [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5)
- [Article 3: Refactoring a Product Detail Page with Lego Architecture: From 3000 Lines to 15 Standalone Components](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

