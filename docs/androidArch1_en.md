## Modern Android Architecture (Part 1): A Decade of Android Architecture Evolution: What Problem Are We Really Solving?

> **Series Navigation**: [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5) | [Article 3: Refactoring a Product Detail Page with Lego Architecture](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

### Foreword: After Learning All Architectures, Did Our Code Actually Get Better?

Since Android's birth in 2010, mainstream architectures have undergone at least five industry-level iterations. We've memorized MVC's layering, mastered MVP's interface decoupling, embraced MVVM's data binding, praised Clean Architecture's dependency inversion, and now we're all turning to MVI's unidirectional data flow.

But here's the gut-wrenching question: **After learning and refactoring with all these architectures, has our code actually gotten better?**

If your answer is "No" — or even "We just moved the garbage code to a different place" — then this article is for you.

***

### I. The Essence of Architectural Evolution: A Revolution to "Reduce Connections"

Understanding all architectural evolution requires only one extremely simple model: **The number of strong reference connections determines system complexity.**

The more connections there are, the deeper the dependencies between modules, and the more likely changes will trigger cascading reactions. Throughout history, all architectures have been doing the same thing — **reducing connections as much as possible to make dependency relationships unidirectional and clear.**

#### 1. MVC: The Confused Triangle (6 connections)

As the originator of modern architecture, MVC first proposed the idea of layering:

- Model: Data
- View: UI
- Controller: Logic

However, in classic implementations, all three layers **interact with each other**, forming a closed triangle with a total of 6 strong reference connections:

```
View ←→ Controller ←→ Model
↑                       ↑
└───────────────────────┘
```

In Android, Activity/Fragment acts as both View and Controller, leading to the common "Massive View Controller" with thousands of lines. A single data change can affect all layers, making it nearly impossible to understand the flow.

#### 2. MVP: Cutting Direct Connections (4 connections)

MVP made a revolutionary simplification: **Completely prohibit direct interaction between View and Model.**

All cross-layer communication must go through Presenter. The triangle is broken, becoming a linear dependency:

```
View ←→ Presenter ←→ Model
```

Connections reduced from 6 to 4, a 33% complexity reduction. View only displays, Model only handles data, and business logic lives entirely in Presenter. Between 2015-2018, this almost saved the entire Android development community.

#### 3. MVVM: Dependency Inversion (3 connections)

Building on MVP, MVVM introduced data binding and observable states to achieve dependency inversion:

```
View → ViewModel ←→ Model
```

- View still holds a ViewModel reference, but ViewModel no longer holds any View references.
- After completing a task, ViewModel only updates an observable data state, completely unaware and unconcerned about who is observing.

Connections reduced to 3. The relationship between View and ViewModel becomes unidirectional, eliminating manual UI update code and tedious interface definitions.

The deeper value of this change: ViewModel becomes a reusable **mediator** (or service provider), observable and reusable by any view. Multiple ViewModels can naturally serve a single view. Compared to Presenter, ViewModel has smaller granularity and higher reusability.

These improvements made MVVM quickly replace MVP as the mainstream.

#### 4. MVI: Unidirectional Closed Loop (3 connections + strict unidirectional flow)

MVI is the latest evolution. Building on MVVM, it **completely straightens the data flow**:

- All user operations are aggregated into a single **Intent**
- All UI states are aggregated into an immutable **State**
- Data flows in a strict unidirectional cycle: `User Action → Intent → ViewModel → Model → New State → View Rendering`

From a reference perspective, both MVI and MVVM have 3 connections. But MVI enforces: **Any state change must go through ViewModel; View cannot directly modify data.**

This brings significant engineering benefits:

- Centralized state management for full visibility of page behavior
- Immutable state prevents accidental modifications
- Predictable behavior for easy debugging and logging
- Advanced features like time-travel debugging and unified logging

**However, MVI has fatal implementation barriers.** It requires extremely high abstraction skills from developers. When programmers lacking splitting awareness use MVI, they fall into three typical traps:

1. **Intent Explosion**: A complex page defines 200+ Intents with long, smelly names and overlapping responsibilities, making maintenance harder.
2. **God State**: A single State object stuffed with 30 unrelated fields, where changes trigger cascading updates.
3. **Mesh Dependencies**: Multiple Intents call each other, forming chaotic call chains that make the unidirectional flow meaningless.

What should be a rigorous finite state machine becomes an "infinite state machine" — this is why MVI is seen as a disaster by some.

***

### II. Architecture is Just "Technique": It Can't Stop You from Writing Bad Code

I've seen many absurd projects:

- Using advanced MVVM but writing 5000-line ViewModels (architecture can't control internal bloat)
- Strictly following Clean Architecture but filling the Domain layer with business coupling (architecture can't control UseCase boundaries)
- Insisting on MVI's unidirectional flow but mixing carousel data, recommendation lists, and user info in one State (architecture can't control State granularity)

Bad programming habits don't disappear just by switching architectures. We merely moved garbage from Activity to Presenter, then to ViewModel, then to UseCase. **We just moved garbage from one room to another without actually cleaning it up.**

Take the same login logic:

- In MVC, it's scattered across the Activity
- After refactoring to MVP, it's copied entirely into Presenter, still 300 lines of procedural code
- After migrating to MVVM, it's stuffed into a `LoginViewModel`, with the `login()` method still a mess
- Converting to MVI, it becomes a jumble of Intents and States, appearing unidirectional on the surface but with tangled logic underneath

**Architecture is "technique". It only solves the "rough partitioning" of code, specifying where View, ViewModel, and Model should go. But it can't tell you how fine-grained to split code within the same layer, how to organize it, or how to reuse it.** What truly improves code quality includes at least:

- **Programming Philosophy**: Fundamental principles like divide-and-conquer, abstraction, and single responsibility determine how we view and decompose problems.
- **Code Hygiene**: Zero tolerance for redundant code, poor naming, and out-of-bounds logic keeps the codebase healthy long-term.
- **Programming Literacy**: Habits and awareness of writing readable, testable, maintainable code make collaboration painless.
- **Scientific Implementation**: Practical tool encapsulation strategies, governance processes, and iteration mechanisms ensure ideas are truly executed.

These are the foundational skills and qualities of an excellent engineer. Engineers with these abilities can write clean, robust, evolvable code without relying on any "silver bullet architecture."

***

**Related Reading**:

- [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5)
- [Article 3: Refactoring a Product Detail Page with Lego Architecture: From 3000 Lines to 15 Standalone Components](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

