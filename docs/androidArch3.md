# Android架构系列博文（共4篇）

---

## 第三篇：Lego架构实战：用单一RecyclerView构建超级复杂的商品详情页

### 前言：商品详情页的困境

商品详情页是电商App中最复杂的页面之一。一个典型的商品详情页可能包含：

- 商品图片轮播
- 商品价格营销（优惠券、促销标签）
- 商品标题和基本信息
- 销量、发货地、服务承诺
- 售后服务保障
- 商品规格选择（尺寸、口味等）
- 购买数量加减
- 用户评价预览
- 店铺信息
- 商品详情图文
- 推荐商品网格列表
- 底部操作栏（收藏、客服、购物车、立即购买）

在传统实现中，我们可能会用多个ScrollView嵌套，或者用多个Fragment，或者写一个几千行的Activity，各种业务逻辑耦合在一起，牵一发而动全身。

今天，我们用实际项目代码展示：**如何用单一RecyclerView + 动态列表组装，把超级复杂的商品详情页拆成一个个独立的Lego积木**。

---

### 一、核心设计：一切皆列表项

这个项目的最大亮点是：**用一个RecyclerView承载所有UI元素**。

为什么这么设计？

1. **性能最优**：RecyclerView天然支持视图复用，即使有成百上千个元素，内存占用也很低
2. **易于扩展**：新增一个UI区块，只需要新增一个列表项类型
3. **解耦彻底**：每个列表项独立开发、独立测试
4. **动态灵活**：可以根据数据动态调整区块的显示和顺序

#### 1.1 定义列表项类型

首先，我们用sealed class定义所有可能的列表项：

[GoodsDetailListItem.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/GoodsDetailListItem.kt)

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

每个列表项都是一个独立的"积木"，只负责一种UI展示。

---

### 二、数据模型与状态映射

#### 2.1 原始数据模型

我们从服务端拿到原始数据模型 [GoodsDetail.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/model/GoodsDetail.kt)：

```kotlin
data class GoodsDetail(
    val title: String,
    val subtitle: String,
    val priceYuan: String,
    val originalPriceYuan: String?,
    val skus: List<GoodsSku>,
    val selectedWeightIndex: Int = 0,
    val selectedFlavorIndex: Int = 0,
    val buyCount: Int = 1,
    val detailImages: List<String>,
    val shipFromCity: String,
    // ... 更多字段
)
```

#### 2.2 Mapper模式：数据到UI状态的转换

我们不直接把原始数据丢给UI，而是通过Mapper转换成UI专用的State：

[GoodsDetailProductSectionMapper.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/GoodsDetailProductSectionMapper.kt)

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
        val priceYuan = selectedSku?.priceYuan ?: detail.priceYuan
        val formattedPrice = formatDisplayPrice(priceYuan)
        
        return GoodsDetailProductSectionState(
            price = formattedPrice,
            originalPrice = detail.originalPriceYuan?.let { formatDisplayPrice(it) },
            title = detail.title,
            subtitle = detail.subtitle,
            shipFromCity = shipFromCity ?: detail.shipFromCity,
            selectedWeightIndex = selectedWeightIndex,
            selectedFlavorIndex = selectedFlavorIndex,
            quantity = quantity,
            // ... 更多字段
        )
    }
}
```

**为什么要这样做？**

- 原始数据可能不适合直接展示（例如价格是分，需要转成元）
- UI需要的数据结构和后端返回的可能完全不同
- 数据转换逻辑集中管理，易于测试和维护
- 业务逻辑下沉，ViewModel更简洁

---

### 三、动态列表组装：Lego的精髓

现在，最精彩的部分来了：如何把这些独立的积木组装成完整的页面？

这就是[GoodsDetailListAssembler.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/GoodsDetailListAssembler.kt)的职责：

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

        // 1. 商品价格营销区块
        items += GoodsDetailListItem.PriceMarketing(productSection)
        items += GoodsDetailListItem.ProductTitle(productSection)
        items += GoodsDetailListItem.ServiceSales(productSection)
        
        // 2. 分割线
        items += GoodsDetailListItem.SectionDivider
        
        // 3. 售后服务
        items += GoodsDetailListItem.AfterSales(productSection)
        items += GoodsDetailListItem.SectionDivider
        
        // 4. 规格选择 + 购买数量
        items += GoodsDetailListItem.SpecSelection(productSection)
        items += GoodsDetailListItem.PurchaseQuantity(productSection)
        
        // 5. 分割线 + 评价
        items += GoodsDetailListItem.SectionDivider
        items += GoodsDetailListItem.Review(GoodsDetailReviewMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider
        
        // 6. 店铺信息
        items += GoodsDetailListItem.Shop(GoodsDetailShopMapper.from(detail))
        items += GoodsDetailListItem.SectionDivider
        
        // 7. 商品详情图文
        items += GoodsDetailListItem.DetailsTitle
        detailImageUrls.forEach { url ->
            items += GoodsDetailListItem.DetailImage(url)
        }
        
        // 8. 推荐商品
        items += GoodsDetailListItem.RecommendTitle
        recommendProducts.forEach { product ->
            items += GoodsDetailListItem.RecommendProduct(product)
        }
        
        // 9. 底部
        if (showListEndFooter) {
            items += GoodsDetailListItem.ListFooter
        }

        return items
    }
}
```

**这个Assembler的威力在于：**

- 可以动态决定显示哪些区块（例如没有评价时跳过评价区块）
- 可以动态调整区块顺序
- 可以根据不同商品类型展示不同的区块组合
- 完全解耦：Activity只需要调用 `assembler.build(...)`，不需要知道里面有什么

---

### 四、ViewModel：轻量级协调者

在这个架构中，ViewModel变得非常简洁：

[GoodsDetailViewModel.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/viewmodel/GoodsDetailViewModel.kt)

```kotlin
class GoodsDetailViewModel : ViewModel() {
    private val repository = GoodsRepository()

    private val _detail = MutableLiveData<GoodsDetail>()
    val detail: LiveData<GoodsDetail> = _detail

    private val _recommendProducts = MutableLiveData<List<BrowseProduct>>()
    val recommendProducts: LiveData<List<BrowseProduct>> = _recommendProducts

    fun load(spuId: String) {
        viewModelScope.launch {
            try {
                val detailResponse = repository.fetchGoodsDetail(spuId)
                _detail.postValue(detailResponse)
                val recommends = repository.loadRecommended()
                _recommendProducts.postValue(recommends)
            } catch (e: Exception) {
                _errorOb.postValue(e.message ?: "加载失败")
            }
        }
    }
}
```

ViewModel只负责：
1. 数据加载
2. 数据持有
3. 状态暴露

**没有任何UI逻辑、没有任何数据转换逻辑、没有任何列表组装逻辑。**

这些逻辑都下沉到了Mapper和Assembler中。

---

### 五、Activity：薄薄的一层壳

现在看[GoodsDetailActivity.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/GoodsDetailActivity.kt)，你会发现它非常简洁：

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
        
        applyEdgeToEdgeInsets(binding.root)  // 使用BaseActivity的方法
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
            imageAdapter.submit(detail.bannerImages)
            rebuildDetailList()  // 重新组装列表
        }
        viewModel.recommendProducts.observe(this) { 
            rebuildDetailList() 
        }
    }

    private fun rebuildDetailList() {
        val detail = currentDetail ?: return
        val productSection = GoodsDetailProductSectionMapper.from(...)
        val items = GoodsDetailListAssembler.build(
            productSection = productSection,
            detail = detail,
            detailImageUrls = detail.detailImages,
            recommendProducts = viewModel.recommendProducts.value.orEmpty(),
            showListEndFooter = viewModel.recommendHasMore.value == true
        )
        detailAdapter.submitList(items)
    }
}
```

Activity只负责：
1. 初始化视图
2. 观察ViewModel状态
3. 调用Assembler组装列表
4. 处理用户交互（点击事件等）

---

### 六、独立组件：真正的Lego积木

让我们看几个独立组件的例子：

#### 6.1 推荐商品网格间距装饰

[RecommendGridSpacingDecoration.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/RecommendGridSpacingDecoration.kt)

```kotlin
class RecommendGridSpacingDecoration(
    private val context: Context,
    private val detailAdapter: GoodsDetailAdapter,
) : RecyclerView.ItemDecoration() {

    private val cellSpacingPx = DimensUtil.dimen(context, 5)
    private val edgeInsetPx = DimensUtil.dimen(context, 14)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        if (detailAdapter.getItemViewType(position) != GoodsDetailAdapter.VIEW_TYPE_RECOMMEND_PRODUCT) {
            return
        }
        val lp = view.layoutParams as? GridLayoutManager.LayoutParams ?: return
        outRect.top = cellSpacingPx
        outRect.bottom = cellSpacingPx
        when (lp.spanIndex) {
            0 -> {
                outRect.left = edgeInsetPx
                outRect.right = cellSpacingPx
            }
            else -> {
                outRect.left = cellSpacingPx
                outRect.right = edgeInsetPx
            }
        }
    }
}
```

这个组件：
- 完全独立，不依赖Activity
- 可以在任何使用网格布局的地方复用
- 职责单一：只负责推荐商品的间距计算

#### 6.2 DetailAnchorTab的独立

我们甚至把简单的枚举都独立出来：

[DetailAnchorTab.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/ui/DetailAnchorTab.kt)

```kotlin
enum class DetailAnchorTab {
    PRODUCT,
    REVIEW,
    DETAIL,
    RECOMMEND,
}
```

**为什么这么小的东西也要独立？**

- 未来其他页面也可能需要这个枚举
- Activity更清爽
- 符合"无限拆分，直到最小颗粒"的原则

---

### 七、工具层的支撑

在[BaseActivity.kt](file:///Users/zzy/github/androidArch/common/src/main/kotlin/com/joy/common/base/BaseActivity.kt)中，我们封装了通用的EdgeToEdge逻辑：

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    protected open fun applyEdgeToEdgeInsets(rootView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            onApplyWindowInsets(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }
    }

    protected open fun onApplyWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
    }
}
```

在GoodsDetailActivity中，只需要重写：

```kotlin
override fun onApplyWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
    binding.detailTitleOverlay.updatePadding(top = top)
    applyMoreBadgeLayout(top)
    binding.llBottomBar.updatePadding(bottom = bottom)
    setWindowStatusBarColor(Color.TRANSPARENT)
    statusBarShowingTitle2Fill = false
}
```

这就是Lego的精髓：**把通用逻辑封装成可复用的基础积木**。

---

### 八、架构优势对比

让我们用一张表格对比传统方式和Lego方式：

| 维度 | 传统方式 | Lego方式 |
|------|----------|----------|
| **代码组织** | 1个3000+行的Activity | 10+个100-300行的独立组件 |
| **可维护性** | 牵一发动全身 | 独立修改，互不影响 |
| **可测试性** | 难以单独测试 | 每个组件可独立测试 |
| **可复用性** | 几乎无法复用 | 组件可在多个页面复用 |
| **扩展性** | 新增区块要改很多地方 | 新增区块只需新增ListItem和Assembler逻辑 |
| **并行开发** | 只能串行开发 | 多个开发者可并行开发不同组件 |

**在这个项目中，我们看到的具体收益：**

1. **Activity代码量减少了60%以上**：原来可能3000行，现在只有1000行左右
2. **新增区块极简单**：例如要加一个"限时抢购"区块，只需要：
   - 新增一个 `GoodsDetailListItem.FlashSale`
   - 新增对应的布局文件
   - 在Assembler里加一行
   - 完了！
3. **每个组件都可以独立预览和测试**
4. **即使未来改用Compose，Mapper、Assembler等核心逻辑也能直接复用**

---

### 九、总结：Lego架构的实践启示

这个商品详情页的实现，完美诠释了Lego架构的核心思想：

**1. 无限拆分，直到最小颗粒**
   - 列表项类型：每个只负责一种UI
   - Mapper：只负责数据转换
   - Assembler：只负责列表组装
   - ViewModel：只负责数据协调
   - Activity：只负责生命周期和交互

**2. 组合优于继承**
   - 没有庞大的BaseActivity承载业务
   - 所有功能通过组合独立组件实现
   - BaseActivity只是一个薄壳

**3. 业务逻辑下沉**
   - UI逻辑下沉到Mapper、Assembler
   - 通用逻辑下沉到BaseActivity、工具类
   - ViewModel和Activity保持轻薄

**4. 一切皆可复用**
   - RecommendGridSpacingDecoration可以在任何网格布局用
   - DetailAnchorTab可以在任何需要锚点的页面用
   - Mapper逻辑甚至可以跨平台复用

---

当你真正用Lego思想来构建应用时，你会发现：**复杂的不是应用本身，而是你没有把它拆成足够小的积木**。

这个商品详情页虽然复杂，但通过"单一RecyclerView + 动态列表组装 + 独立组件"，它变得清晰、可维护、可扩展。

**下一篇预告：** 我们将探讨设计模式如何作为Lego架构的补充，让你的积木更加稳固、更加灵活。
