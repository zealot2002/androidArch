# 第三篇：Lego架构实战：用单一RecyclerView构建超级商品详情页

> **项目地址**：[https://github.com/zealot2002/androidArch](https://github.com/zealot2002/androidArch)

---

## 前言：商品详情页——架构的"试金石"

商品详情页是电商App公认的"架构试金石"。一个标准详情页包含15个以上业务区块：轮播图、价格营销、规格选择、用户评价、店铺信息、图文详情、推荐商品……

在传统架构中，我们见过太多真实且令人头疼的灾难：

总控制逻辑不清晰，没有真正的“交通枢纽”：页面控制逻辑散落在 Activity、Fragment、Adapter 甚至工具类中。你刚在 Activity 里找到一段加载数据的代码，顺着调用链又跳到了 ViewModel，再往下可能又塞进了一个 Adapter 的内部回调。没有一个集中的地方能让你看清“这个页面到底按什么顺序做什么事”。

代码冗余严重：到处散落着冗余逻辑和细微差别的代码段。要么不知道如何抽取方法——总觉得抽出来那个方法名比代码本身还长，要么硬抽一个然后挂羊头卖狗肉，方法名叫 updatePrice，里面却顺便刷新了库存、埋了点儿数据、还弹了个 Toast。

零散的胶水代码到处都是：findViewById、临时标志位、匿名回调、多层嵌套判断……散落在各处，读起来像一本没有目录、也没有章节划分的书。

组织结构混乱，职责边界模糊：看似分了几个类或几个包，但数据转换、UI 渲染、业务判断常常交织在一起。改一个价格展示逻辑，可能要顺着调用链跳转五六个文件，而且总担心踩到其他不相关的逻辑。

找东西找不到：一个价格格式化方法，可能藏在 GoodsUtils，也可能在某个 ViewModel 的私有方法里，甚至直接硬编码在 Adapter 的 onBindViewHolder 中。新人接手后，大部分时间不是在理解业务，而是在做“寻址考古”。

这些问题的本质，不是因为没用某种“先进架构”，而是缺乏统一的分治颗粒度，以及一个清晰的控制流枢纽。这也是 Lego 架构试图解决的核心矛盾：让每个页面的总控逻辑收敛到一处（如 Assembler + ViewModel），让其他所有零件只管自己那一亩三分地。

今天，我们用实际项目代码演示：**如何运用Lego架构的"最小颗粒度 + 动态组装"思想，以商品详情页为例，把复杂的UI和业务逻辑拆解成独立、可复用、可插拔的积木。**

---

## 一、核心设计：一切皆列表项——最小颗粒度的极致实践

根据 Lego 架构“无限拆分到最小颗粒”的原则，我们需要将整个页面拆解为一个个独立的 UI 单元。一个自然而然的选择是：用一个单一的 RecyclerView 承载所有的 UI 元素。

这样做的好处是：每个 UI 单元对应一个独立的列表项类型，彼此之间没有嵌套依赖；页面整体的滚动性能由 RecyclerView 统一管理，避免了多层级嵌套带来的卡顿和内存问题；同时，新增或删除某个区块也不需要调整布局结构，只需增减对应的列表项即可。

- 把整个页面拆成一个个独立的**列表项**（积木）
- 每个列表项只负责一种UI展示，只解决一个问题
- 所有积木共享统一的接口，可以任意组合、任意排序

### 1.1 定义统一的积木接口

我们用 `sealed class` 定义所有列表项类型，这就是我们的"积木清单"：

```kotlin
sealed class GoodsDetailListItem {
    data class PriceMarketing(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data class ProductTitle(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data class ServiceSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data class AfterSales(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data class SpecSelection(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data class PurchaseQuantity(val state: GoodsDetailProductSectionState) : GoodsDetailListItem()
    data object SectionDivider : GoodsDetailListItem()
    data class Review(val state: GoodsDetailReviewState) : GoodsDetailListItem()
    data class Shop(val state: GoodsDetailShopState) : GoodsDetailListItem()
    data object DetailsTitle : GoodsDetailListItem()
    data class DetailImage(val imageUrl: String) : GoodsDetailListItem()
    data object RecommendTitle : GoodsDetailListItem()
    data class RecommendProduct(val product: BrowseProduct) : GoodsDetailListItem()
    data object ListFooter : GoodsDetailListItem()
}
```

**为什么这比传统方式好？**

- 彻底消灭"上帝布局"：没有任何嵌套的 `ScrollView` 或 `LinearLayout`
- 每个列表项可以独立开发、独立测试、独立迭代
- 新增一个UI区块，只需新增一个子类，**无需修改任何现有代码**

---

## 二、数据分层：原始数据 → UI状态——用 Mapper 封装数据转换
在商品详情页的实践中，为了进一步降低 UI 与后端数据模型的耦合，我们引入了 数据转换层（Mapper）。用来生产一个个独立的列表项积木，所需的 UI 状态
`Mapper` 是一个纯函数，输入原始数据，输出UI状态，没有副作用，极易测试。

```kotlin
object GoodsDetailProductSectionMapper {
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

- **隔离后端变化**：后端改了字段名，只需改 `Mapper`，UI层完全不受影响
- **统一数据格式**：所有UI需要的格式化逻辑都集中在Mapper中
- **彻底解耦**：UI层只依赖UI状态，不依赖任何后端数据模型

---

## 三、动态组装：Assembler——组合优于继承的完美体现

现在到了Lego架构最精髓的部分：**Assembler（组装器）**。

如果说列表项是积木，那么 `Assembler` 就是Lego的说明书。它告诉我们应该用哪些积木、按什么顺序、拼成什么样子。

```kotlin
object GoodsDetailListAssembler {
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

**这个 `Assembler` 解决了传统架构所有的痛点：**

| 痛点 | Assembler的解法 |
|------|----------------|
| 动态性 | 根据后端数据、AB实验、用户身份，动态决定显示哪些区块 |
| 可配置性 | 调整区块顺序只需移动两行代码，不需改动任何UI逻辑 |
| 解耦性 | `Activity` 只需调用 `assembler.build()`，完全不知道内部有什么区块 |
| 可测试性 | 传入不同参数即可验证不同组装结果，无需启动App |

---

## 四、ViewModel：纯粹的状态协调者

在Lego架构中，`ViewModel` 只是一个状态持有者和协调者。

它不包含任何UI逻辑，也不包含复杂的业务逻辑——所有的业务逻辑都下沉到了 `Mapper`、`Assembler` 这些可复用的积木中。

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

整个 `ViewModel` 逻辑清晰，任何新人接手都能快速理解。

---

## 五、Activity：薄薄的一层壳——架构与视图的胶水层

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
3. 观察 `ViewModel` 的状态并更新UI

**Activity并不"只是一个容器"，它是架构与Android视图系统之间的重要胶水层。** 在Lego架构中，我们仍然尊重Activity作为导航和生命周期管理者的角色，但把具体的UI组装和业务逻辑全部委托给了积木——这样Activity永远不会膨胀。

---

## 六、独立组件：真正的Lego积木

Lego 架构的精髓在于**将每个功能拆分为独立的组件**，这些组件可以像积木一样自由组合。

### 6.1 网格间距装饰器

[GridSpacingDecoration.kt](file:///Users/zzy/github/androidArch/common/src/main/kotlin/com/joy/common/widgets/recyclerview/GridSpacingDecoration.kt)

这是一个**通用组件**，放在 `common` 模块中，可以在任何项目中复用：

```kotlin
class GridSpacingDecoration(
    private val context: Context,
    private val cellSpacingDp: Int = 5,
    private val edgeInsetDp: Int = 14,
    private val targetViewType: Int? = null,
) : RecyclerView.ItemDecoration() {
    // ... 间距计算逻辑
}
```

**Lego 思想体现**：
- **独立封装**：不依赖任何业务组件
- **参数化配置**：通过构造参数定制行为
- **跨模块复用**：放在 `common` 模块，所有模块都可以使用

### 6.2 DetailAnchorTab的独立

我们甚至把简单的枚举都独立出来：

```kotlin
enum class DetailAnchorTab {
    PRODUCT,
    REVIEW,
    DETAIL,
    RECOMMEND,
}
```

**为什么这么小的东西也要独立？**

这个枚举只是：符合"无限拆分，直到最小颗粒"的原则。

### 6.3 评价面板：最小化侵入的页面组合

商品详情页中，评价列表是一个复杂的独立功能，但我们希望它**不影响主页面的简洁性**。通过侧滑面板 + Fragment 的方式，实现最小化侵入的页面组合。

**核心组件**：

| 组件 | 职责 |
|------|------|
| **ReviewListPanelController** | 面板动画控制、Fragment生命周期管理 |
| **GoodsReviewListFragment** | 评价列表的完整实现 |
| **GoodsReviewListAdapter** | 评价列表的Adapter |
| **GoodsDetailReviewMapper** | 数据到评价UI状态的转换 |

**Lego 思想体现**：

1. **完全独立的Fragment**：评价功能完全封装在 `GoodsReviewListFragment` 中，包含完整的列表逻辑

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

2. **干净的Controller**：面板控制器只负责动画和Fragment管理，不包含任何业务逻辑

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

3. **主页 Activity 极简**：只需要初始化Controller，评价逻辑完全不侵入主Activity

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

4. **布局按需加载**：评价面板的布局在点击时才inflate，不会影响首屏性能

**架构优势**：
- 评价功能可以独立开发、独立测试
- 主页Activity不包含任何评价相关的UI逻辑
- 面板动画和Fragment管理封装在Controller中
- 未来可以轻松替换为其他实现（如全屏Fragment、BottomSheet等）

---

## 七、架构优势对比

让我们用一张表格对比传统方式和Lego方式：

| 维度 | 传统方式 | Lego方式 |
|------|----------|----------|
| **代码组织** | 1个3000+行的Activity | 10+个100-300行的独立组件 |
| **可维护性** | 牵一发动全身 | 独立修改，互不影响 |
| **可测试性** | 难以单独测试 | 每个组件可独立测试 |
| **可复用性** | 几乎无法复用 | 组件可在多个页面复用 |
| **扩展性** | 新增区块要改很多地方 | 新增区块只需新增ListItem和Assembler逻辑 |
| **并行开发** | 只能串行开发 | 多个开发者可并行开发不同组件 |

---

## 八、总结：理想的商品详情页

理想的商品详情页，在架构上表现为：页面由若干职责分明、边界清晰的功能区块构成。每个区块内部，满目皆是高度内聚的工具类、UI 组件、Mapper 与 UseCase，几乎看不到零散的胶水代码。大量的基础积木、组合积木、高级积木与 UI 组件，都是经过线上环境长期检验、稳定可靠的“技术资产”。最终，那个曾经臃肿不堪的复杂页面，变得健壮、优雅、敏捷、可扩展、可测试、可维护、可读性极佳——仿佛一件精心设计的作品，而非一堆难以收拾的代码。

---

当你真正用Lego思想来构建应用时，你会发现：**复杂的不是应用本身，而是你没有把它拆成足够小的积木**。

这个商品详情页虽然复杂，但通过"单一RecyclerView + 动态列表组装 + 独立组件"，它变得清晰、可维护、可扩展。

**下一篇预告：** 我们将探讨设计模式如何作为Lego架构的粘合剂，让你的积木组合更加灵活、更加稳固。敬请期待！
