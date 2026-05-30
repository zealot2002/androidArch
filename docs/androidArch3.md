# 第三篇：Lego架构实战：构建商品详情页

> **项目地址**：[https://github.com/zealot2002/androidArch](https://github.com/zealot2002/androidArch)

---

## 前言：商品详情页——架构的“试金石”

商品详情页是电商App公认的“架构试金石”。一个标准详情页往往包含10个以上业务区块：轮播图、价格营销、规格选择、用户评价、店铺信息、图文详情、推荐商品……

在传统架构中，我们见过太多真实且令人头疼的灾难：

**总控制逻辑不清晰，没有真正的“交通枢纽”**：页面控制逻辑散落在 Activity、Fragment、Adapter 甚至工具类中。你刚在 Activity 里找到一段加载数据的代码，顺着调用链又跳到了 ViewModel，再往下可能又塞进了一个 Adapter 的内部回调。没有一个集中的地方能让你看清“这个页面到底按什么顺序做什么事”。

**代码冗余严重**：到处散落着冗余逻辑和细微差别的代码段。要么不知道如何抽取方法——总觉得抽出来那个方法名比代码本身还长，要么硬抽一个然后挂羊头卖狗肉，方法名叫 `updatePrice`，里面却顺便刷新了库存、埋了点儿数据、还弹了个 Toast。

**零散的胶水代码到处都是**：临时标志位、匿名回调、多层嵌套判断……散落在各处，读起来像一本没有目录、也没有章节划分的书。

**组织结构混乱，职责边界模糊**：看似分了几个类或几个包，但数据转换、UI 渲染、业务判断常常交织在一起。改一个价格展示逻辑，可能要顺着调用链跳转五六个文件，而且总担心踩到其他不相关的逻辑。

**找东西找不到**：一个价格格式化方法，可能藏在 `GoodsUtils`，也可能在某个 `ViewModel` 的私有方法里，甚至直接硬编码在 Adapter 的 `onBindViewHolder` 中。新人接手后，大部分时间不是在理解业务，而是在做“寻址考古”。

这些问题的本质，不是因为没用某种“先进架构”，而是：

1. **没有拆成最小颗粒**——缺乏 Lego 架构所倡导的分治思想，代码块过大、职责过重，导致难以复用和维护；
2. **缺少治理思想**——没有建立清晰的代码组织规范和边界，任凭冗余逻辑和胶水代码四处滋生，最终失控。

Lego 架构并非单一原则，而是一个“公理 + 多个定理”的体系：
- **公理（核心思想）**：分治法、单一职责
- **定理（实践方法）**：治理思想（大局观、逻辑收敛）、工具迭代（私有→共有→远程）、复用发现（小颗粒独立便于扫描整合）

今天，我们用实际项目代码演示：**如何运用 Lego 架构的“最小颗粒度 + 动态组装”思想，以及配套的治理与工具迭代方法，以商品详情页为例，把复杂的 UI 和业务逻辑拆解成独立、可复用、可插拔的积木。**

---

## 一、核心设计：一切皆列表项——最小颗粒度的极致实践

根据 Lego 架构“无限拆分到最小颗粒”的原则，我们需要将整个页面拆解为一个个独立的 UI 单元。按照这一思路，我们选择用**一个单一的 `RecyclerView` 承载所有的 UI 元素**。这个做法初看可能有些反直觉，但它是分治思想在列表型页面中的自然投射。

这样做的好处是：
- 每个 UI 单元对应一个独立的列表项类型，彼此之间没有嵌套依赖
- 页面整体的滚动性能由 `RecyclerView` 统一管理，避免了多层级嵌套带来的卡顿和内存问题
- 新增或删除某个区块不需要调整布局结构，只需增减对应的列表项

### 1.1 定义统一的积木接口

我们用 `sealed class` 定义所有列表项类型，这就是我们的“积木清单”。**每个列表项积木的职责只有一个：描述一种 UI 区块所需的数据**。

```kotlin
sealed class GoodsDetailListItem {
    // 商品基础信息区块
    data class PriceMarketing(val state: GoodsDetailProductSectionState) : GoodsDetailListItem() // 职责：展示价格、促销标签
    data class ProductTitle(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // 职责：展示商品标题、副标题
    data class ServiceSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // 职责：展示服务承诺、销量数据
    data class AfterSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()     // 职责：展示售后保障信息

    // 规格与数量区块
    data class SpecSelection(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()   // 职责：展示规格选择器（重量/口味等）
    data class PurchaseQuantity(val state: GoodsDetailProductSectionState) : GoodsDetailListItem() // 职责：展示购买数量加减控件

    // 通用装饰区块
    data object SectionDivider : GoodsDetailListItem() // 职责：区块之间的分割线

    // 评价与店铺区块
    data class Review(val state: GoodsDetailReviewState) : GoodsDetailListItem() // 职责：展示用户评价摘要（评分、评价条数）
    data class Shop(val state: GoodsDetailShopState) : GoodsDetailListItem()     // 职责：展示店铺信息（名称、评分、客服入口）

    // 图文详情区块
    data object DetailsTitle : GoodsDetailListItem()                       // 职责：展示“图文详情”标题
    data class DetailImage(val imageUrl: String) : GoodsDetailListItem()   // 职责：展示详情页的一张图片

    // 推荐商品区块
    data object RecommendTitle : GoodsDetailListItem()                     // 职责：展示“猜你喜欢”标题
    data class RecommendProduct(val product: BrowseProduct) : GoodsDetailListItem() // 职责：展示单个推荐商品

    // 尾部区块
    data object ListFooter : GoodsDetailListItem() // 职责：列表尾部占位/加载完成标识
}
```

**为什么这比传统方式好？**
- 彻底消灭“上帝布局”：没有任何嵌套的 `ScrollView` 或 `LinearLayout`
- 每个列表项可以独立开发、独立测试、独立迭代
- 新增一个 UI 区块，只需新增一个子类，**无需修改任何现有代码**

---

## 二、数据分层：原始数据 → UI 状态——用 Mapper 封装数据转换

**【职责：Mapper】** 负责将后端原始数据转换为 UI 层可直接使用的不可变状态对象。它不包含任何业务逻辑，是一个纯函数，输入原始数据，输出 UI 状态。

有了列表项积木之后，我们需要为每个积木准备所需的数据。为了降低 UI 与后端数据模型的耦合，我们引入了**数据转换层（Mapper）**，用来生产一个个独立的列表项积木数据。

```kotlin
object GoodsDetailProductSectionMapper {
    // 职责：将 GoodsDetail 后端模型 + 用户选择（规格、数量） 转换为商品基础区块所需的 UI 状态
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
        // ... 其他字段映射
        return GoodsDetailProductSectionState(
            priceYuan = formattedPrice,
            title = detail.title,
            weightSpecs = detail.skus.map { it.name },
            // ...
        )
    }
}
```

**核心价值**：
- **隔离后端变化**：后端改了字段名，只需改 `Mapper`，UI 层完全不受影响
- **统一数据格式**：所有 UI 需要的格式化逻辑集中在 Mapper 中
- **彻底解耦**：UI 层只依赖 UI 状态，不依赖任何后端数据模型

---

## 三、动态组装：Assembler——积木的说明书

**【职责：Assembler】** 负责根据当前状态（产品信息、推荐列表、实验开关等）动态组装最终的列表项序列。它决定显示哪些区块、以什么顺序显示，是页面的“总装车间”。

如果说列表项是积木，那么 `Assembler` 就是 Lego 的说明书。它告诉我们应该用哪些积木、按什么顺序、拼成什么样子。

```kotlin
object GoodsDetailListAssembler {
    // 职责：根据传入的各区块状态，组装出完整的列表项 List
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

**这个 `Assembler` 解决了传统架构的多个痛点：**

| 痛点 | Assembler 的解法 |
|------|------------------|
| 动态性 | 根据后端数据、AB实验、用户身份，动态决定显示哪些区块 |
| 可配置性 | 调整区块顺序只需移动两行代码，不需改动任何 UI 逻辑 |
| 解耦性 | 区块是动态组装的，自然避免了逻辑被耦合到具体区块内部 |
| 可测试性 | 传入不同参数即可验证不同组装结果，无需启动 App |

---

## 四、ViewModel：纯粹的状态协调者

**【职责：ViewModel】** 作为页面级别的状态持有者和协调者。它不包含 UI 逻辑，也不包含复杂的业务逻辑——只负责：
- 持有页面所需的数据（LiveData/StateFlow）
- 接收来自 View 的用户操作（如点击规格、修改数量）
- 调用 Mapper 和 Assembler 生成新的 UI 状态
- 将状态暴露给 View 进行观察

`ViewModel` 不包含任何 UI 逻辑，也不包含复杂的业务逻辑——所有的数据转换和列表组装都下沉到了 `Mapper` 和 `Assembler` 这些**职责边界清晰**的积木中。

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
            recommendProducts = _recommendProducts.value.orEmpty(),
            showListEndFooter = _recommendHasMore.value == true,
        )
        _listItems.postValue(items)
        _detailImageUrls.postValue(detail.detailImages)
    }
}
```

整个 `ViewModel` 职责单一、逻辑清晰，新人也能快速上手。

---

## 五、Activity：薄薄的一层壳——架构与视图的胶水层

**【职责：Activity】** 作为页面与 Android 系统的交互入口，负责：
- 初始化视图绑定（`setContentView`）
- 设置 `RecyclerView`、Adapter 及其他 UI 组件
- 将用户点击事件转发给 `ViewModel`
- 观察 `ViewModel` 中的 LiveData，并更新 UI

**Activity 并不“只是一个容器”，它是架构与 Android 视图系统之间的重要胶水层。** 在 Lego 架构中，我们仍然尊重 Activity 作为导航和生命周期管理者的角色，但把具体的 UI 组装和业务逻辑全部委托给了积木——这样 Activity 永远不会膨胀。

最终的 `Activity` 代码，简洁而清晰：

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

整个 `Activity` 只负责三件事：
1. 初始化视图和 `Adapter`
2. 把用户点击事件转发给 `ViewModel`
3. 观察 `ViewModel` 的状态并更新 UI

---

## 六、独立组件：积木的落地实践

除了列表项，Lego 架构还鼓励将通用或相对独立的功能封装成独立组件。

### 6.1 网格间距装饰器

**【职责：GridSpacingDecoration】** 通用的 RecyclerView 网格间距装饰器，负责在网格布局中为每个 item 添加等距的外边距。放在 `common` 模块中，可以在任何需要网格布局的项目中直接复用。（[源码链接](file:///Users/zzy/github/androidArch/common/src/main/kotlin/com/joy/common/widgets/recyclerview/GridSpacingDecoration.kt)）

### 6.2 评价面板：最小化侵入的页面组合

商品详情页中，评价列表是一个复杂的独立功能，但我们希望它**不影响主页面的简洁性**。通过侧滑面板 + Fragment 的方式，实现最小化侵入的页面组合。

**核心组件及职责**：

| 组件 | 职责 |
|------|------|
| `ReviewListPanelController` | 控制面板的动画（显示/隐藏）、管理 Fragment 的添加/移除 |
| `GoodsReviewListFragment` | 独立承载评价列表的完整 UI 和交互逻辑 |
| `GoodsReviewListAdapter` | 评价列表的 RecyclerView Adapter，负责渲染每条评价 |
| `GoodsDetailReviewMapper` | 将详情数据转换为评价面板所需的 UI 状态 |

**Lego 思想体现**：

1. **完全独立的 Fragment**：评价功能完全封装在 `GoodsReviewListFragment` 中，包含完整的列表逻辑

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

2. **干净的 Controller**：面板控制器只负责动画和 Fragment 管理，不包含任何业务逻辑

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
        // 动画展示面板
    }

    fun hide() {
        // 动画隐藏面板
    }
}
```

3. **主 Activity 极简**：只需要初始化 Controller，评价逻辑完全不侵入主 Activity

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

4. **按需创建 Fragment**：评价面板的 Fragment 只在首次点击时创建，不会影响首屏性能。

**架构优势**：
- 评价功能可以独立开发、独立测试
- 主 Activity 不包含任何评价相关的 UI 逻辑
- 面板动画和 Fragment 管理封装在 Controller 中
- 未来可以轻松替换为其他实现（如全屏 Fragment、BottomSheet 等）

---

## 七、架构优势对比

| 维度 | 传统方式（未拆分） | Lego 方式 |
|------|------------------|-----------|
| **代码组织** | 1个 3000+ 行的 Activity | 10+ 个 100-300 行的独立组件 |
| **可维护性** | 牵一发动全身 | 独立修改，互不影响 |
| **可测试性** | 难以单独测试 | 每个组件可独立测试 |
| **可复用性** | 几乎无法复用 | 组件可在多个页面复用 |
| **扩展性** | 新增区块要改很多地方 | 新增区块只需新增 ListItem 和 Assembler 逻辑 |
| **并行开发** | 只能串行开发 | 多个开发者可并行开发不同组件 |

---

## 八、治理与工具迭代：Lego架构的持续演进

Lego 架构不只是一种静态的代码拆分方法，它还需要配套的**治理思想**和**工具迭代流程**来持续演化。本章节介绍我们在实践中沉淀出的方法论。

### 8.1 治理思想：大局观与逻辑收敛

**【职责：治理思想】** 指导整个项目的代码组织，确保大方向不失控。

- **大局观、全局鸟瞰**：定期从高处审视整个项目，识别出哪些模块已经膨胀、哪些边界已经模糊。
- **区域治理**：将项目划分为若干区域（common、feature_xxx、maven 库），每个区域有明确的准入标准。
- **逻辑收敛**：将有共性的控制逻辑（如页面总控、路由决策）集中到一处，避免散落在各处。

在商品详情页中，我们将总控逻辑收敛到 `Assembler + ViewModel`，就是治理思想的具体体现——它虽然不是 Lego 的核心公理，却是让 Lego 积木发挥最大效用的重要保障。

### 8.2 工具迭代三阶段：私有 → 共有 → 远程

**【职责：工具迭代流程】** 让工具类从临时解决方案逐步成长为久经考验的技术资产。

| 阶段 | 存放位置 | 特征 | 示例 |
|------|----------|------|------|
| **私有积木** | `feature_xxx/utils` | 业务相关，仅当前模块使用，但封装良好 | 规格选择弹窗的工具类 |
| **共有积木** | `common/utils` | 项目内多个模块共用，经过初步测试 | 价格格式化工具、网络状态工具 |
| **远程积木** | Maven 依赖 | 历经线上百万用户考验，稳定可靠 | StringUtils、ScreenUtils |

**迭代流程**：
1. 需求迭代时，顺手写一个工具类——放在 `feature_xxx/utils` 中，作为私有积木。
2. 项目定期复盘重构时，review 大量的私有工具，抽取出可在项目内共用的部分，移动到 `common/utils`，成为共有积木。
3. 共有积木历经线上百万用户使用、久经考验后，将其移动到 Maven 仓库，成为远程积木，供所有项目复用。

### 8.3 复用发现：小颗粒独立的价值

**【职责：复用发现机制】** 通过拆分到最小颗粒，让潜在的复用机会自然浮现。

我们甚至将一个小小的 `enum` 都从 Activity 中独立拆分出来，放在 `feature_xxx` 模块：

```kotlin
enum class DetailAnchorTab {
    PRODUCT,
    REVIEW,
    DETAIL,
    RECOMMEND,
}
```

**为什么这么小的东西也要独立？** 因为只有拆到最小颗粒，在定期复盘扫描时才能一眼发现：“哦，原来这个枚举在 A 页面和 B 页面都定义了类似的，可以合并到 common。” 如果它被硬编码在 Activity 内部，这种复用机会就永远被埋没了。

这就是 Lego 架构“无限拆分”的另一个重要价值：**让复用机会从“看不见”变成“看得见”，从“被动发现”变成“主动扫描”。**

---

## 九、总结：理想的商品详情页（推而广之，任何超级复杂的页面）

理想的商品详情页，在架构上表现为：页面由若干职责分明、边界清晰的功能区块构成。每个区块内部，满目皆是高度内聚的工具类、UI 组件，零散的胶水代码极少。大量的基础积木、组合积木、高级积木与 UI 组件，都是经过线上环境长期检验、稳定可靠的“技术资产”。最终，那个曾经臃肿不堪的复杂页面，变得**健壮、优雅、敏捷、轻盈、可扩展、可测试、可维护、可读性极佳**——仿佛一件精心设计的作品，而非一堆难以收拾的代码。

Lego 架构的完整体系可以概括为：
- **一个公理**：分治法 + 单一职责
- **多个定理**：治理思想（大局观、逻辑收敛）、工具迭代（私有→共有→远程）、复用发现（小颗粒独立便于扫描整合）

当你真正用 Lego 思想来构建应用时，你会发现：**复杂的不是应用本身，而是你没有把它拆成足够小的积木，并且没有持续的治理和迭代。**

---

**下一篇预告：** 我们将探讨设计模式如何作为 Lego 架构的粘合剂，让你的积木组合更加灵活、更加稳固。敬请期待！