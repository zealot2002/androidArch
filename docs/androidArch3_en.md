## Modern Android Architecture (Part 3): Refactoring a Product Detail Page with Lego Architecture: From 3000 Lines to 15 Standalone Components

> **Series Navigation**: [Article 1: A Decade of Android Architecture Evolution](https://dev.to/zealot2002/modern-android-architecture-part-1-a-decade-of-android-architecture-evolution-what-problem-are-403f) | [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/modern-android-architecture-part-2-the-lego-architecture-divide-and-conquer-taken-to-the-58j7) | [Article 4: Design Patterns — The Glue of Lego Architecture](https://dev.to/zealot2002/modern-android-architecture-part-4-design-patterns-the-glue-of-lego-architecture-1e9f)
>
> **Project Repository**: <https://github.com/zealot2002/androidArch>

***

## Foreword: Product Detail Page — The "Touchstone" of Architecture

The product detail page is widely recognized as the "touchstone" of e-commerce app architecture. A standard detail page typically contains over 10 business sections: image carousel, price marketing, specification selection, user reviews, store information, product details, recommended products...

In traditional architecture, we've seen too many real and painful disasters:

**Unclear overall control logic, no real "traffic hub"**: Page control logic is scattered across Activity, Fragment, Adapter, and even utility classes. You find code loading data in Activity, follow the call chain to ViewModel, then it might end up in an Adapter's internal callback. There's no centralized place to see "what this page does in what order."

**Severe code redundancy**: Redundant logic and slightly different code snippets are scattered everywhere. Either you don't know how to extract methods — always feeling the method name would be longer than the code itself — or you force an extraction that does multiple things (a method called `updatePrice` that also refreshes inventory, logs analytics, and shows a Toast).

**Scattered glue code everywhere**: Temporary flags, anonymous callbacks, nested conditionals... scattered throughout, reading like a book without table of contents or chapters.

**Chaotic organization, vague responsibility boundaries**: Although seemingly divided into classes or packages, data conversion, UI rendering, and business logic are often intertwined. Changing a price display logic might require jumping through five or six files along the call chain, always worrying about breaking unrelated logic.

**Hard to find things**: A price formatting method might be hidden in `GoodsUtils`, in a ViewModel's private method, or even hardcoded directly in an Adapter's `onBindViewHolder`. Newcomers spend most of their time "archaeological searching" rather than understanding business.

The essence of these problems isn't the lack of some "advanced architecture," but:

1. **Not split into minimal particles** — lacking the divide-and-conquer thinking advocated by Lego Architecture, code blocks are too large with too many responsibilities, making reuse and maintenance difficult.
2. **Lack of governance thinking** — no clear code organization standards or boundaries established, allowing redundant logic and glue code to proliferate until it spirals out of control.

As discussed in Article 2, the core of Lego Architecture is divide-and-conquer and single responsibility, along with practical methods like governance thinking, tool iteration, and reuse discovery. Today, we'll demonstrate with actual project code: **how to apply Lego Architecture's "minimum granularity + dynamic assembly" philosophy to break down complex UI and business logic into independent, reusable, pluggable bricks using a product detail page as an example.**

***

## I. Core Design: Everything is a List Item — The Ultimate Practice of Minimum Granularity

Following Lego Architecture's principle of "infinite splitting to minimum particles," we need to break down the entire page into individual UI units. Following this idea, we choose to use **a single** **`RecyclerView`** **to host all UI elements**. While counterintuitive at first glance, this is a natural projection of divide-and-conquer thinking for list-based pages.

The benefits are:

- Each UI unit corresponds to an independent list item type, with no nested dependencies between them
- Scrolling performance is uniformly managed by `RecyclerView`, avoiding lag and memory issues from multi-layer nesting
- Adding or removing sections doesn't require layout structure changes, just add/remove corresponding list items

### 1.1 Define a Unified Brick Interface

We use `sealed class` to define all list item types — our "brick inventory." **Each list item brick has only one responsibility: describe the data needed for a UI section.**

```kotlin
sealed class GoodsDetailListItem {
    // Product basic information sections
    data class PriceMarketing(val state: GoodsDetailProductSectionState) : GoodsDetailListItem() // Responsibility: Display price, promotion tags
    data class ProductTitle(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // Responsibility: Display product title, subtitle
    data class ServiceSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // Responsibility: Display service commitments, sales data
    data class AfterSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()     // Responsibility: Display after-sales guarantee information

    // Specification and quantity sections
    data class SpecSelection(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // Responsibility: Display specification selector (weight/flavor, etc.)
    data class PurchaseQuantity(val state: GoodsDetailProductSectionState) : GoodsDetailListItem() // Responsibility: Display quantity increment/decrement controls

    // Universal decoration sections
    data object SectionDivider : GoodsDetailListItem() // Responsibility: Divider between sections

    // Review and store sections
    data class Review(val state: GoodsDetailReviewState) : GoodsDetailListItem() // Responsibility: Display user review summary (rating, review count)
    data class Shop(val state: GoodsDetailShopState) : GoodsDetailListItem()     // Responsibility: Display store info (name, rating, customer service)

    // Product detail sections
    data object DetailsTitle : GoodsDetailListItem()                       // Responsibility: Display "Product Details" title
    data class DetailImage(val imageUrl: String) : GoodsDetailListItem()   // Responsibility: Display one detail image

    // Recommendation sections
    data object RecommendTitle : GoodsDetailListItem()                     // Responsibility: Display "Recommended for You" title
    data class RecommendProduct(val product: BrowseProduct) : GoodsDetailListItem() // Responsibility: Display single recommended product

    // Footer section
    data object ListFooter : GoodsDetailListItem() // Responsibility: List footer placeholder/loading complete indicator
}
```

**Why is this better than traditional approaches?**

- Eliminates "God layouts" completely: No nested `ScrollView` or `LinearLayout`
- Each list item can be developed, tested, and iterated independently
- Adding a new UI section only requires adding a new subclass, **no existing code needs modification**

***

## II. Data Layering: Raw Data → UI State — Encapsulating Data Conversion with Mapper

**\[Mapper Responsibility:]** Responsible for converting backend raw data into immutable state objects directly usable by the UI layer. It contains no business logic and is a pure function that takes raw data and outputs UI state.

With list item bricks defined, we need to prepare data for each brick. To reduce coupling between UI and backend data models, we introduce a **data conversion layer (Mapper)** to produce data for individual list item bricks.

```kotlin
object GoodsDetailProductSectionMapper {
    // Responsibility: Convert GoodsDetail backend model + user selections (specifications, quantity) into UI state for product basic sections
    fun from(
        detail: GoodsDetail,
        shipFromCity: String?,
        selectedWeightIndex: Int,
        selectedFlavorIndex: Int,
        quantity: Int,
    ): GoodsDetailProductSectionState {
        val selectedSku = detail.skus.getOrNull(selectedWeightIndex) ?: detail.skus.firstOrNull()
        val priceYuan = selectedSku?.priceYuan ?: "98.00"
        val formattedPrice = formatDisplayPrice(priceYuan)
        // ... other field mappings
        return GoodsDetailProductSectionState(
            priceYuan = formattedPrice,
            title = detail.title,
            weightSpecs = detail.skus.map { it.name },
            // ...
        )
    }
}
```

**Core Value**:

- **Isolate backend changes**: If backend changes field names, only `Mapper` needs modification, UI layer is completely unaffected
- **Unified data format**: All UI formatting logic is centralized in Mapper
- **Complete decoupling**: UI layer only depends on UI state, not any backend data models

***

## III. Dynamic Assembly: Assembler — The Instruction Manual for Bricks

**\[Assembler Responsibility:]** Responsible for dynamically assembling the final list item sequence based on current state (product info, recommendation list, experiment flags, etc.). It determines which sections to display and in what order — the page's "assembly workshop."

If list items are bricks, then `Assembler` is the Lego instruction manual. It tells us which bricks to use, in what order, and how to assemble them.

```kotlin
object GoodsDetailListAssembler {
    // Responsibility: Assemble complete list items based on input section states
    fun build(
        productSection: GoodsDetailProductSectionState,
        detail: GoodsDetail,
        detailImageUrls: List<String>,
        recommendProducts: List<BrowseProduct>,
        showListEndFooter: Boolean,
    ): List<GoodsDetailListItem> {
        val items = mutableListOf<GoodsDetailListItem>()

        items += GoodsDetailListItem.PriceMarketing(productSection)
        items += GoodsDetailListItem.ProductTitle(productSection)
        items += GoodsDetailListItem.ServiceSales(productSection)
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.AfterSales(productSection)
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.SpecSelection(productSection)
        items += GoodsDetailListItem.PurchaseQuantity(productSection)

        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.Review(GoodsDetailReviewMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider

        items += GoodsDetailListItem.Shop(GoodsDetailShopMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.DetailsTitle
        detailImageUrls.forEach { url ->
            items += GoodsDetailListItem.DetailImage(url)
        }

        items += GoodsDetailListItem.RecommendTitle
        recommendProducts.forEach { product ->
            items += GoodsDetailListItem.RecommendProduct(product)
        }

        if (showListEndFooter) {
            items += GoodsDetailListItem.ListFooter
        }

        return items
    }
}
```

**This** **`Assembler`** **solves multiple pain points of traditional architecture:**

| Pain Point      | Assembler Solution                                                                                  |
| --------------- | --------------------------------------------------------------------------------------------------- |
| Dynamicity      | Dynamically determine which sections to display based on backend data, AB tests, user identity      |
| Configurability | Adjusting section order only requires moving two lines of code, no UI logic changes needed          |
| Decoupling      | Sections are dynamically assembled, naturally avoiding logic being coupled inside specific sections |
| Testability     | Pass different parameters to verify different assembly results without launching the App            |

***

## IV. ViewModel: Pure State Coordinator

**\[ViewModel Responsibility:]** Acts as a page-level state holder and coordinator. It contains no UI logic and no complex business logic — only responsible for:

- Holding page data (LiveData/StateFlow)
- Receiving user operations from View (e.g., clicking specifications, modifying quantity)
- Calling Mapper and Assembler to generate new UI state
- Exposing state to View for observation

```kotlin
class GoodsDetailViewModel : ViewModel() {
    private val repository = GoodsRepository()

    private val _detail = MutableLiveData<GoodsDetail>()
    val detail: LiveData<GoodsDetail> = _detail

    private val _listItems = MutableLiveData<List<GoodsDetailListItem>>()
    val listItems: LiveData<List<GoodsDetailListItem>> = _listItems

    private val _detailImageUrls = MutableLiveData<List<String>>()
    val detailImageUrls: LiveData<List<String>> = _detailImageUrls

    private var selectedWeightIndex = 0
    private var selectedFlavorIndex = 0
    private var purchaseQuantity = 1

    fun load(spuId: String) {
        viewModelScope.launch {
            val detailResponse = repository.fetchGoodsDetail(spuId)
            _detail.postValue(detailResponse)
            rebuildListItems()
        }
    }

    fun selectWeightSpec(index: Int) {
        selectedWeightIndex = index
        rebuildListItems()
    }

    fun selectFlavorSpec(index: Int) {
        selectedFlavorIndex = index
        rebuildListItems()
    }

    fun incrementQuantity() {
        purchaseQuantity++
        rebuildListItems()
    }

    fun decrementQuantity() {
        if (purchaseQuantity > 1) {
            purchaseQuantity--
            rebuildListItems()
        }
    }

    private fun rebuildListItems() {
        val detail = _detail.value ?: return
        val productSection = GoodsDetailProductSectionMapper.from(
            detail = detail,
            shipFromCity = detail.shipFromCity,
            selectedWeightIndex = selectedWeightIndex,
            selectedFlavorIndex = selectedFlavorIndex,
            quantity = purchaseQuantity,
        )
        val items = GoodsDetailListAssembler.build(
            productSection = productSection,
            detail = detail,
            detailImageUrls = detail.detailImages,
            recommendProducts = emptyList(),
            showListEndFooter = true,
        )
        _listItems.postValue(items)
        _detailImageUrls.postValue(detail.detailImages)
    }
}
```

The entire `ViewModel` has a single responsibility and clear logic, making it easy for newcomers to understand.

***

## V. Activity: A Thin Shell — The Glue Layer Between Architecture and View

**\[Activity Responsibility:]** Serves as the interaction entry point between the page and Android system, responsible for:

- Initializing view binding (`setContentView`)
- Setting up `RecyclerView`, Adapter, and other UI components
- Forwarding user click events to `ViewModel`
- Observing LiveData in `ViewModel` and updating UI

**Activity is not "just a container"; it's an important glue layer between architecture and the Android view system.** In Lego Architecture, we still respect Activity's role as navigation and lifecycle manager, but delegate all specific UI assembly and business logic to bricks — ensuring Activity never bloats.

The final `Activity` code is clean and clear:

```kotlin
@Route(path = RouterConstants.GOODS_DETAIL)
class GoodsDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityGoodsDetailBinding
    private val viewModel: GoodsDetailViewModel by lazy {
        ViewModelProvider(this)[GoodsDetailViewModel::class.java]
    }
    private val imageAdapter = GoodsImagePagerAdapter()
    private lateinit var detailAdapter: GoodsDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoodsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupReviewListPanel()
        setupDetailRecyclerView()
        setupScrollToTop()
        setupTopBarScroll()
        setupActions()
        setupPager()
        observeViewModel()
        loadData()
    }

    private fun observeViewModel() {
        viewModel.detail.observe(this) { detail ->
            currentDetail = detail
            currentReviewState = GoodsDetailReviewMapper.from(detail)
            imageAdapter.submit(detail.bannerImages)
            updateImageCount(position = 0, total = detail.bannerImages.size)
        }
        viewModel.listItems.observe(this) { items ->
            val imageUrls = viewModel.detailImageUrls.value.orEmpty()
            if (imageUrls.isNotEmpty()) {
                detailAdapter.submit(items, imageUrls)
            }
        }
        viewModel.detailImageUrls.observe(this) { imageUrls ->
            val items = viewModel.listItems.value
            if (!items.isNullOrEmpty() && imageUrls.isNotEmpty()) {
                detailAdapter.submit(items, imageUrls)
            }
        }
    }
}
```

The entire `Activity` only does three things:

1. Initialize views and `Adapter`
2. Forward user click events to `ViewModel`
3. Observe `ViewModel` state and update UI

***

## VI. Independent Components

### 6.1 Grid Spacing Decoration

**\[Responsibility: GridSpacingDecoration]** A universal RecyclerView grid spacing decorator responsible for adding equal outer margins to each item in grid layouts. Placed in the `common` module, it can be directly reused in any project requiring grid layouts.

### 6.2 Review Panel: Minimally Intrusive Page Composition

In the product detail page, the review list is a complex independent feature, but we want it **not to affect the main page's simplicity**. Through a side panel + Fragment approach, we achieve minimally intrusive page composition.

**Core Components and Responsibilities**:

| Component                   | Responsibility                                                              |
| --------------------------- | --------------------------------------------------------------------------- |
| `ReviewListPanelController` | Controls panel animations (show/hide), manages Fragment addition/removal    |
| `GoodsReviewListFragment`   | Independently hosts complete review list UI and interaction logic           |
| `GoodsReviewListAdapter`    | RecyclerView Adapter for review list, responsible for rendering each review |
| `GoodsDetailReviewMapper`   | Converts detail data to UI state needed by review panel                     |

**Lego Thinking in Action**:

1. **Completely Independent Fragment**: Review functionality is fully encapsulated in `GoodsReviewListFragment`, containing complete list logic

```kotlin
class GoodsReviewListFragment : Fragment() {
    private val listAdapter = GoodsReviewListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.rvReviewList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviewList.adapter = listAdapter
    }

    fun render(state: GoodsDetailReviewState) {
        binding.tvReviewListTitle.text = getString(R.string.goods_review_section_title_format, state.totalCount)
        listAdapter.submitList(GoodsReviewListMockData.reviews())
    }
}
```

2. **Clean Controller**: Panel controller only handles animations and Fragment management, no business logic

```kotlin
class ReviewListPanelController(
    private val activity: FragmentActivity,
    private val panelRoot: View,
    private val scrim: View,
    private val panel: View,
    private val fragmentContainerId: Int,
) {
    fun show(reviewState: GoodsDetailReviewState) {
        ensureFragment(reviewState)
        // Animation to show panel
    }

    fun hide() {
        // Animation to hide panel
    }
}
```

3. **Minimal Main Activity**: Only needs to initialize Controller, review logic doesn't intrude into main Activity

```kotlin
private fun setupReviewListPanel() {
    reviewListPanelController = ReviewListPanelController(
        activity = this,
        panelRoot = binding.reviewListPanelOverlay.root,
        scrim = binding.reviewListPanelOverlay.reviewListScrim,
        panel = binding.reviewListPanelOverlay.reviewListPanel,
        fragmentContainerId = binding.reviewListPanelOverlay.reviewListFragmentContainer.id,
    )
}

private fun showReviewListPanel() {
    val reviewState = GoodsDetailReviewMapper.from(detail)
    reviewListPanelController.show(reviewState)
}
```

4. **On-Demand Fragment Creation**: Review panel Fragment is only created on first click, no impact on initial screen performance.

**Architectural Benefits**:

- Review functionality can be developed and tested independently
- Main Activity contains no review-related UI logic
- Panel animations and Fragment management are encapsulated in Controller
- Can be easily replaced with other implementations in the future (e.g., full-screen Fragment, BottomSheet)

***

## VII. Project Structure Overview

### 7.1 Module Directory Tree

```
androidArch/
│
├── app/                          # Application entry
│   ├── App.kt                    # Application
│   └── MainActivity.kt           # Home page
│
├── common/                       # Common base (can be depended on by all modules)
│   ├── base/
│   │   ├── BaseActivity.kt       # Activity base class (encapsulates analytics, lifecycle)
│   │   └── BaseViewModel.kt      # ViewModel base class
│   │
│   ├── utils/
│   │   ├── ToastUtils.kt         # Global Toast
│   │   ├── StatusBarUtils.kt     # Status bar operations
│   │   └── PaletteColorUtils.kt  # Color extraction
│   │
│   ├── router/
│   │   └── AppRouter.kt         # Route navigation
│   │
│   └── widgets/
│       ├── GridSpacingDecoration.kt   # Universal grid spacing decorator
│       └── IconFontView.kt           # Icon font component
│
├── app_res/                      # Resource center (colors, dimensions, Drawables)
│   ├── res/
│   │   ├── drawable/             # 50+ universal Drawables
│   │   ├── values/colors_*.xml   # Three-layer color system
│   │   └── values/dimens.xml      # Dimension tokens
│   └── assets/iconfont.ttf        # Icon font
│
├── feature-goods/                # Goods module
│   ├── ui/
│   │   ├── GoodsDetailActivity.kt    # Product detail page (main controller)
│   │   ├── GoodsDetailAdapter.kt     # List adapter (14 ViewTypes)
│   │   ├── GoodsDetailListItem.kt    # List item sealed class
│   │   ├── GoodsDetailListAssembler.kt   # List assembler
│   │   ├── GoodsDetailProductSectionMapper.kt  # Data→UI state
│   │   ├── GoodsDetailReviewMapper.kt
│   │   ├── GoodsDetailShopMapper.kt
│   │   ├── ReviewListPanelController.kt  # Review panel controller
│   │   ├── GoodsReviewListFragment.kt   # Review list (independent Fragment)
│   │   └── DetailAnchorTab.kt           # Tab enum
│   │
│   ├── viewmodel/
│   │   └── GoodsDetailViewModel.kt  # State coordinator
│   │
│   ├── model/
│   │   ├── GoodsDetail.kt              # Raw data model
│   │   ├── GoodsDetailProductSectionState.kt  # UI state model
│   │   ├── GoodsDetailReviewState.kt
│   │   └── BrowseProduct.kt
│   │
│   └── data/
│       ├── GoodsRepository.kt          # Data repository
│       └── GoodsDetailMockCatalog.kt   # Mock data
│
├── feature-login/                # Login module
│   ├── ui/LoginActivity.kt
│   ├── domain/UserRepository.kt
│   └── viewmodel/LoginViewModel.kt
│
└── tools/                       # Pure utilities (cross-project reuse)
    └── utils/
        ├── DateUtils.kt
        ├── StringUtils.kt
        └── ValidateUtils.kt
```

### 7.2 Core Class Responsibility Description

| Module     | Class Name                      | Responsibility                                       | Lines of Code |
| ---------- | ------------------------------- | ---------------------------------------------------- | ------------- |
| **ui**     | GoodsDetailActivity             | View binding, lifecycle, user interaction forwarding | approx. 500   |
| **ui**     | GoodsDetailAdapter              | List rendering for 14 ViewTypes                      | approx. 400   |
| **ui**     | GoodsDetailListAssembler        | Dynamic list item assembly                           | approx. 80    |
| **ui**     | GoodsDetailListItem             | List item type definition                            | approx. 50    |
| **ui**     | ReviewListPanelController       | Review panel animation and Fragment management       | approx. 150   |
| **ui**     | DetailAnchorTab                 | Tab enum (independent small class)                   | approx. 10    |
| **mapper** | GoodsDetailProductSectionMapper | Data→Product section UI state                        | approx. 60    |
| **mapper** | GoodsDetailReviewMapper         | Data→Review section UI state                         | approx. 30    |
| **mapper** | GoodsDetailShopMapper           | Data→Shop section UI state                           | approx. 20    |
| **vm**     | GoodsDetailViewModel            | Data loading, state holding, trigger rebuild         | approx. 150   |
| **common** | BaseActivity                    | Encapsulates analytics, Edge-to-Edge                 | approx. 50    |
| **common** | GridSpacingDecoration           | Universal grid spacing decorator                     | approx. 40    |
| **common** | ToastUtils                      | Global Toast                                         | approx. 30    |

### 7.3 Data Flow

```
Backend Data          Mapper Conversion         Assembler Assembly         ViewModel Holding        Activity Observation
   │                        │                           │                        │                        │
   ▼                        ▼                           ▼                        ▼                        ▼
GoodsDetail ──► GoodsDetailProduct ──► List<GoodsDetail ──► _listItems ─────► notifyDataSetChanged
                SectionState             ListItem>
```

***

This practical case fully implements the core principles of Lego Architecture proposed in Article 2:

- **Minimal Base Classes**: `GoodsDetailActivity` only inherits an empty `BaseActivity` (handling only essential seams like analytics and lifecycle), with all functionality provided through pluggable bricks.
- **Infinite Splitting**: The page is split into 15+ independent list item bricks, each with a single responsibility; the review panel is split into independent `Fragment` + `Controller`.
- **Tool Iteration**: The price formatting method was initially written in `feature_goods/utils`, then moved to `common/utils` after review found it was needed by multiple modules — exactly the "Private → Shared" evolution path described in Article 2.

Detailed methodologies on governance thinking, the three stages of tool iteration (Private→Shared→Remote), and reuse discovery were fully explained in Article 2 and won't be repeated here.

***

## VIII. Architecture Advantage Comparison

| Dimension                | Traditional Approach (Unsplit)                | Lego Approach                                                    |
| ------------------------ | --------------------------------------------- | ---------------------------------------------------------------- |
| **Code Organization**    | 1 Activity with 3000+ lines                   | 10+ independent components with 100-300 lines each               |
| **Maintainability**      | Changing one thing affects everything         | Independent modifications, no cross-component impact             |
| **Testability**          | Difficult to test individually                | Each component can be tested independently                       |
| **Reusability**          | Almost impossible to reuse                    | Components can be reused across multiple pages                   |
| **Extensibility**        | Adding sections requires changing many places | Adding sections only needs new ListItem and Assembler logic      |
| **Parallel Development** | Only sequential development possible          | Multiple developers can work on different components in parallel |

***

## IX. Conclusion: The Ideal Complex Page

An ideal complex page, architecturally speaking, consists of several functional blocks with clear responsibilities and boundaries. Inside each block, you see highly cohesive utility classes and UI components, with minimal scattered glue code. Numerous basic bricks, composite bricks, advanced bricks, and UI components are battle-tested, stable, reliable "technical assets." Ultimately, what was once a bloated complex page becomes **robust, elegant, agile, lightweight, extensible, testable, maintainable, and highly readable** — like a carefully designed work of art rather than a pile of unmanageable code.

The complete Lego Architecture system can be summarized as:

- **One Axiom**: Divide-and-conquer + Single Responsibility
- **Multiple Theorems**: Governance thinking (big-picture view, logic convergence), tool iteration (Private→Shared→Remote), reuse discovery (small independent particles facilitate scanning and integration)

When you truly build applications with Lego thinking, you realize: **Complexity doesn't come from the application itself, but from not splitting it into small enough bricks and lacking continuous governance and iteration.**

***

Of course, mastery of design patterns, code aesthetics, and the ability to spot reusable bricks are also essential qualities of an excellent engineer.

**Next Article Preview**: We'll continue enriching our demo project and explore how design patterns can serve as the glue for Lego Architecture, making your brick combinations more flexible and robust. See [Article 4](https://dev.to/zealot2002/modern-android-architecture-part-4-design-patterns-the-glue-of-lego-architecture-1e9f).

**Related Reading**:

- [Article 1: A Decade of Android Architecture Evolution: What Problem Are We Really Solving?](https://dev.to/zealot2002/modern-android-architecture-part-1-a-decade-of-android-architecture-evolution-what-problem-are-403f)
- [Article 2: The Lego Architecture: Divide and Conquer, Taken to the Extreme](https://dev.to/zealot2002/modern-android-architecture-part-2-the-lego-architecture-divide-and-conquer-taken-to-the-58j7)
- [Article 4: Design Patterns — The Glue of Lego Architecture](https://dev.to/zealot2002/modern-android-architecture-part-4-design-patterns-the-glue-of-lego-architecture-1e9f)

