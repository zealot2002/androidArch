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

### 四、ViewModel：子线程组装列表

在这个架构中，ViewModel不仅负责数据加载，还负责列表组装。关键的是：**列表组装在子线程执行，避免阻塞主线程**：

[GoodsDetailViewModel.kt](file:///Users/zzy/github/androidArch/feature-goods/src/main/java/com/joy/featuregoods/viewmodel/GoodsDetailViewModel.kt)

```kotlin
class GoodsDetailViewModel : ViewModel() {
    private val repository = GoodsRepository()

    private val _detail = MutableLiveData<GoodsDetail>()
    val detail: LiveData<GoodsDetail> = _detail

    private val _recommendProducts = MutableLiveData<List<BrowseProduct>>()
    val recommendProducts: LiveData<List<BrowseProduct>> = _recommendProducts

    private val _listItems = MutableLiveData<List<GoodsDetailListItem>>()
    val listItems: LiveData<List<GoodsDetailListItem>> = _listItems

    private val _detailImageUrls = MutableLiveData<List<String>>()
    val detailImageUrls: LiveData<List<String>> = _detailImageUrls

    private var selectedWeightIndex = 0
    private var selectedFlavorIndex = 0
    private var purchaseQuantity = 1

    fun load(spuId: String) {
        viewModelScope.launch {
            try {
                val detailResponse = repository.fetchGoodsDetail(spuId)
                _detail.postValue(detailResponse)
                val recommends = repository.loadRecommended()
                _recommendProducts.postValue(recommends)
                rebuildListItems()  // 触发列表组装
            } catch (e: Exception) {
                _errorOb.postValue(e.message ?: "加载失败")
            }
        }
    }

    fun selectWeightSpec(index: Int) {
        selectedWeightIndex = index
        rebuildListItems()
    }

    fun incrementQuantity() { purchaseQuantity++; rebuildListItems() }
    fun decrementQuantity() { if (purchaseQuantity > 1) { purchaseQuantity--; rebuildListItems() } }

    private fun rebuildListItems() {
        val detail = _detail.value ?: return
        viewModelScope.launch {
            // 关键：在子线程执行列表组装
            val items = withContext(Dispatchers.Default) {
                val productSection = GoodsDetailProductSectionMapper.from(
                    detail = detail,
                    shipFromCity = detail.shipFromCity,
                    selectedWeightIndex = selectedWeightIndex,
                    selectedFlavorIndex = selectedFlavorIndex,
                    quantity = purchaseQuantity,
                )
                GoodsDetailListAssembler.build(
                    productSection = productSection,
                    detail = detail,
                    detailImageUrls = detail.detailImages,
                    recommendProducts = _recommendProducts.value.orEmpty(),
                    showListEndFooter = _recommendHasMore.value == true,
                )
            }
            _listItems.postValue(items)
            _detailImageUrls.postValue(detail.detailImages)
        }
    }
}
```

**为什么这样做？**

1. **子线程执行**：使用 `withContext(Dispatchers.Default)` 在后台线程组装列表，避免阻塞UI
2. **单一数据源**：所有状态集中在ViewModel，Activity无需管理复杂状态
3. **状态封装**：规格选择、数量修改等操作都封装在ViewModel中
4. **视图同步**：当数据变化时，自动触发 `rebuildListItems()` 重新组装列表

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
        
        applyEdgeToEdgeInsets(binding.root)
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

Activity只负责：
1. 初始化视图
2. 观察ViewModel状态
3. 处理用户交互（点击事件等）

**注意**：由于 `listItems` 和 `detailImageUrls` 在同一协程中发布，我们分别观察两个LiveData，确保数据都准备好后才提交给Adapter。

同时，Adapter的 `submit()` 方法也简化了：

```kotlin
fun submit(
    listItems: List<GoodsDetailListItem>,
    imageUrls: List<String>,
) {
    this.detailImageUrls = imageUrls
    items.clear()
    items.addAll(listItems)
    notifyDataSetChanged()
 }
 ```

#### 5.1 用户交互：委托给ViewModel

Activity中的用户交互（如规格选择、数量修改）都委托给ViewModel处理：

```kotlin
detailAdapter = GoodsDetailAdapter(this,
    object : GoodsDetailAdapter.Callbacks {
        override fun onWeightSpecSelected(index: Int) {
            viewModel.selectWeightSpec(index)  // ViewModel处理
        }

        override fun onFlavorSpecSelected(index: Int) {
            viewModel.selectFlavorSpec(index)  // ViewModel处理
        }

        override fun onQuantityMinus() {
            viewModel.decrementQuantity()  // ViewModel处理
        }

        override fun onQuantityPlus() {
            viewModel.incrementQuantity()  // ViewModel处理
        }
        // ... 其他回调
    }
)
```

这样设计的好处：
- **Activity只负责UI绑定**，不包含任何业务逻辑
- **ViewModel统一管理状态**，规格选择、数量修改都会触发列表重新组装
- **数据流清晰**：用户操作 → ViewModel处理 → 子线程组装 → 更新UI

---

### 六、独立组件：真正的Lego积木

让我们看几个独立组件的例子：

#### 6.1 网格间距装饰器

[GridSpacingDecoration.kt](file:///Users/zzy/github/androidArch/common/src/main/kotlin/com/joy/common/widgets/recyclerview/GridSpacingDecoration.kt)

这是一个**通用组件**，放在 `common` 模块中，可以在任何项目中复用：

```kotlin
class GridSpacingDecoration(
    private val context: Context,
    private val cellSpacingDp: Int = 5,
    private val edgeInsetDp: Int = 14,
    private val targetViewType: Int? = null,
) : RecyclerView.ItemDecoration() {

    private val cellSpacingPx by lazy { DimensUtil.dimen(context, cellSpacingDp) }
    private val edgeInsetPx by lazy { DimensUtil.dimen(context, edgeInsetDp) }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        
        if (targetViewType != null) {
            val adapter = parent.adapter ?: return
            if (adapter.getItemViewType(position) != targetViewType) {
                return
            }
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

**使用方式**：

```kotlin
// 在商品详情页中使用
binding.rvDetailContent.addItemDecoration(
    GridSpacingDecoration(
        context = this,
        cellSpacingDp = 5,
        edgeInsetDp = 14,
        targetViewType = GoodsDetailAdapter.VIEW_TYPE_RECOMMEND_PRODUCT,
    )
)
```

**设计要点**：
- **参数化配置**：支持自定义 cellSpacing 和 edgeInset
- **ViewType过滤**：可选参数 `targetViewType` 实现只对特定类型的列表项应用间距
- **延迟初始化**：使用 `by lazy` 延迟计算像素值，避免不必要的计算
- **完全通用**：不依赖任何业务组件，可以在任何网格布局中使用

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

Lego架构的威力不仅体现在业务代码的拆分，更体现在**工具层的精心设计**。所有工具都遵循"三层封装"原则：通用工具 → 高级工具 → 业务工具，层层内聚，层层复用。

#### 7.1 基础工具层（com.joy.common.utils）

这些工具是整个架构的"原子积木"，业务无关、项目无关，甚至可以开源：

| 工具类 | 职责 | 典型方法 |
|--------|------|----------|
| **ToastUtils** | 吐司提示 | `show()`, `showSuccess()`, `showError()` |
| **LoadingUtils** | 加载框 | `show()`, `dismiss()` |
| **SizeUtils** | 尺寸转换 | `getDimen()`, `dp2px()`, `px2dp()` |
| **DimensUtil** | 设计稿尺寸 | `dimen()` |
| **NetworkUtils** | 网络状态 | `isConnected()` |
| **StatusBarUtils** | 状态栏操作 | `setStatusBarColor()`, `getStatusBarHeight()` |
| **PaletteColorUtils** | 颜色提取 | `computeMutedBackgroundGradient()` |

**示例：PaletteColorUtils的协程改造**

为了防止内存泄漏，我们将原来的线程池+Handler模式改为Kotlin协程：

```kotlin
object PaletteColorUtils {
    fun computeMutedBackgroundGradient(
        lifecycleScope: CoroutineScope,  // 传入lifecycleScope防止内存泄漏
        bitmap: Bitmap,
        endColor: Int,
        fallbackStartColor: Int,
        onResult: (Drawable) -> Unit,
    ) {
        lifecycleScope.launch {
            val gradient = withContext(Dispatchers.Default) {
                extractDominantColors(bitmap, endColor, fallbackStartColor)
            }
            onResult(gradient)
        }
    }
}
```

调用时传入 `lifecycleScope`，当Activity销毁时协程自动取消，**彻底避免内存泄漏**。

#### 7.2 扩展函数层（com.joy.common.extend）

为Android类添加Kotlin扩展方法，让代码更简洁：

| 文件 | 扩展内容 |
|------|----------|
| **ContextExtend.kt** | `Context.isValid()` 判空检查 |
| **ActivityExtend.kt** | Activity专用扩展 |
| **ViewExtend.kt** | `onClick()` 系列，点击防抖处理 |

**示例：防抖点击扩展**

```kotlin
fun View.onClick200(debounce: Int = 200, action: () -> Unit) {
    setOnClickListener(object : DebouncedOnClickListener(debounce) {
        override fun doClick(view: View) { action() }
    })
}
```

使用方式：`binding.btnSubmit.onClick200 { viewModel.submit() }`，再也不用担心用户疯狂点击。

#### 7.3 路由层（com.joy.common.router）

统一的路由管理，支持Activity和Fragment导航：

```kotlin
object AppRouter {
    fun navigateToGoodsDetail(context: Context, spuId: String) {
        ARouter.getInstance()
            .build(RouterConstants.GOODS_DETAIL)
            .withString("spuId", spuId)
            .navigation()
    }
}

// 登录拦截路由
class LoginRouter(private val context: Context) {
    fun navigateWithLoginCheck(targetPath: String, onLoggedIn: () -> Unit) {
        if (isLoggedIn()) {
            onLoggedIn()
        } else {
            ARouter.getInstance()
                .build(RouterConstants.LOGIN)
                .navigation()
        }
    }
}
```

#### 7.4 通用组件层（com.joy.common.widgets）

可复用的UI组件，封装了完整的功能逻辑：

| 组件 | 用途 |
|------|------|
| **IconFontView** | 字体图标，支持IconFont |
| **BadgeView** | 红点角标 |
| **ScrollToTopFloatView** | 滚动到顶部悬浮按钮 |
| **QuickMenuPopup** | 快速操作菜单弹窗 |

**示例：IconFontView**

项目使用IconFont图标库，通过 `IconFontView` 组件统一管理：

```xml
<com.joy.common.widgets.IconFontView
    android:id="@+id/iv_star"
    app:iconText="@string/iconfont_star_fill"
    app:iconColor="@color/func_orange_text_1" />
```

字符串资源定义在 `strings_iconfont.xml` 中，**图标样式与业务代码彻底解耦**。

#### 7.5 Tools工具模块（com.joy.tools）

业务无关的通用工具，可跨项目复用：

| 工具类 | 职责 |
|--------|------|
| **DateUtils** | 日期格式化、计算 |
| **StringUtils** | 字符串处理 |
| **ValidateUtils** | 格式校验（手机号、邮箱等） |

---

### 八、资源系统的支撑：Design Token体系

一个成熟的架构不仅需要代码层面的拆分，还需要**资源层面的系统性设计**。

在 [app_res模块](file:///Users/zzy/github/androidArch/app_res) 中，我们建立了完整的 **Design Token 体系**，实现设计与代码的解耦。

#### 8.1 颜色Token体系

参考系列博文《[现代化UI架构：三层颜色体系与系统化设计方案](https://dev.to/zealot2002/xian-dai-hua-ui-jia-gou-san-ceng-yan-se-ti-xi-yu-xi-tong-hua-she-ji-fang-an-5ebo)》，我们将颜色分为三层：

**第一层：Primitive（原始值）**
```xml
<color name="primitive_white_1">#FFFFFF</color>
<color name="primitive_black_1">#000000</color>
<color name="primitive_orange_500">#FF6B00</color>
```

**第二层：Semantic（语义色）**
```xml
<color name="func_orange_text_1">@color/primitive_orange_500</color>
<color name="func_gray_bg_1">#F5F5F5</color>
```

**第三层：Theme（主题色）**
```xml
<color name="t_white_1">@color/primitive_white_1</color>
<color name="t_primary">@color/primitive_orange_500</color>
```

这样做的好处：
- **设计改版只需改一处**：修改Primitive层，所有引用自动生效
- **语义化命名**：代码中用 `func_orange_text_1` 比 `#FF6B00` 更易理解
- **主题切换**：通过替换Theme层实现暗黑模式

#### 8.2 尺寸Token体系

遵循同样的三层结构：

```xml
<!-- Primitive -->
<dimen name="dp_1">1dp</dimen>
<dimen name="dp_4">4dp</dimen>
<dimen name="dp_8">8dp</dimen>
...

<!-- Semantic -->
<dimen name="d_goods_card_padding">12dp</dimen>
<dimen name="d_button_height">48dp</dimen>

<!-- Component -->
<dimen name="c_icon_size_small">24dp</dimen>
<dimen name="c_icon_size_medium">32dp</dimen>
```

#### 8.3 Drawable资源池

项目积累了50+个通用Drawable，覆盖各种交互状态：

**按状态分类：**
- `bg_xxx_fill_interact_x` - 实心填充按钮（可交互）
- `bg_xxx_stroke_interact_x` - 描边按钮（可交互）
- `bg_xxx_flat_interact_x` - 扁平按钮（可交互）
- `bg_xxx_surface_xxx` - 卡片/面板背景
- `sel_xxx` - Selector选择器

**示例：规格选择器的背景切换**
```kotlin
// 未选中
bg_gray_fill_interact_1  // 灰色实心
// 选中
bg_orange_stroke_interact_1  // 橙色描边
```

通过代码动态切换背景，实现视觉反馈，**无需写一行Java/Kotlin代码**。

---

### 九、商品详情页的完整UI组件清单

商品详情页的UI被拆分为以下独立组件：

#### 9.1 Adapter列表项（13种）

| 组件 | ViewType | 对应布局文件 |
|------|----------|--------------|
| **PriceMarketing** | 1 | `item_goods_detail_price_marketing.xml` |
| **ProductTitle** | 2 | `item_goods_detail_product_title.xml` |
| **ServiceSales** | 3 | `item_goods_detail_service_sales.xml` |
| **AfterSales** | 4 | `item_goods_detail_after_sales.xml` |
| **SpecSelection** | 5 | `item_goods_detail_spec_selection.xml` |
| **PurchaseQuantity** | 6 | `item_goods_detail_purchase_quantity.xml` |
| **Review** | 7 | `item_goods_detail_review.xml` |
| **Shop** | 8 | `item_goods_detail_shop.xml` |
| **DetailsTitle** | 9 | `item_goods_detail_details_title.xml` |
| **DetailImage** | 10 | `item_goods_detail_detail_image.xml` |
| **RecommendTitle** | 11 | `item_goods_detail_recommend_title.xml` |
| **RecommendProduct** | 12 | `item_browse_history_product.xml` |
| **SectionDivider** | 14 | `item_goods_detail_section_divider.xml` |

#### 9.2 独立组件类

| 组件类 | 职责 |
|--------|------|
| **GoodsImagePagerAdapter** | 轮播图适配器 |
| **GoodsReviewListAdapter** | 评价列表适配器 |
| **GoodsReviewListFragment** | 评价列表Fragment |
| **ReviewListPanelController** | 评价面板滑动控制器 |
| **RecommendGridSpacingDecoration** | 推荐商品网格间距装饰 |
| **QuickMenuPopup** | 右侧快捷菜单弹窗 |

#### 9.3 Mapper转换器

| Mapper类 | 职责 |
|----------|------|
| **GoodsDetailProductSectionMapper** | 商品基础信息 → UI状态 |
| **GoodsDetailReviewMapper** | 商品详情 → 评价预览状态 |
| **GoodsDetailShopMapper** | 商品详情 → 店铺信息状态 |

#### 9.4 列表组装器

| Assembler类 | 职责 |
|-------------|------|
| **GoodsDetailListAssembler** | 将所有数据组装为列表项 |
| **GoodsDetailListItem** | 列表项sealed class定义 |

---

### 十、架构优势对比

---

### 十、架构优势对比

让我们用一张表格对比传统方式和Lego方式：

| 维度 | 传统方式 | Lego方式 |
|------|----------|----------|
| **代码组织** | 1个3000+行的Activity | 10+个100-300行的独立组件 |
| **可维护性** | 牵一发动全身 | 独立修改，互不影响 |
| **可测试性** | 难以单独测试 | 每个组件可独立测试 |
| **可复用性** | 几乎无法复用 | 组件可在多个页面复用 |
| **扩展性** | 新增区块要改很多地方 | 新增区块只需新增ListItem和Assembler逻辑 |
| **并行开发** | 只能串行开发 | 多个开发者可并行开发不同组件 |
| **性能** | 主线程组装列表，复杂页面易卡顿 | 子线程组装，主线程只负责展示 |
| **资源管理** | 颜色硬编码，样式散落各处 | Design Token体系，修改一处全局生效 |

**在这个项目中，我们看到的具体收益：**

1. **Activity代码量减少了60%以上**：原来可能3000行，现在只有1000行左右
2. **新增区块极简单**：例如要加一个"限时抢购"区块，只需要：
   - 新增一个 `GoodsDetailListItem.FlashSale`
   - 新增对应的布局文件
   - 在Assembler里加一行
   - 完了！
3. **每个组件都可以独立预览和测试**
4. **即使未来改用Compose，Mapper、Assembler等核心逻辑也能直接复用**
5. **列表组装在子线程执行**：使用 `withContext(Dispatchers.Default)` 避免阻塞UI
6. **状态管理集中在ViewModel**：规格选择、数量修改等操作统一由ViewModel处理
7. **单一数据源**：Activity无需管理复杂状态，只需观察ViewModel的数据

---

### 十一、总结：Lego架构的实践启示

这个商品详情页的实现，完美诠释了Lego架构的核心思想：

**1. 无限拆分，直到最小颗粒**
   - 列表项类型：每个只负责一种UI
   - Mapper：只负责数据转换
   - Assembler：只负责列表组装
   - ViewModel：负责数据协调 + 子线程列表组装
   - Activity：只负责生命周期和交互绑定

**2. 组合优于继承**
   - 没有庞大的BaseActivity承载业务
   - 所有功能通过组合独立组件实现
   - BaseActivity只是一个薄壳

**3. 业务逻辑下沉**
   - UI逻辑下沉到Mapper、Assembler
   - 通用逻辑下沉到BaseActivity、工具类
   - 列表组装下沉到ViewModel的子线程中
   - ViewModel和Activity保持轻薄

**4. 一切皆可复用**
   - RecommendGridSpacingDecoration可以在任何网格布局用
   - DetailAnchorTab可以在任何需要锚点的页面用
   - Mapper逻辑甚至可以跨平台复用
   - ViewModel中的列表组装逻辑也可以独立测试
   - 工具类（ToastUtils、SizeUtils等）可在任何项目中使用

**5. 资源系统与代码同等重要**
   - Design Token体系让颜色、尺寸管理更加系统化
   - 三层颜色体系（Primitive → Semantic → Theme）实现设计与代码解耦
   - Drawable资源池覆盖所有交互状态，减少重复开发

---

当你真正用Lego思想来构建应用时，你会发现：**复杂的不是应用本身，而是你没有把它拆成足够小的积木**。

这个商品详情页虽然复杂，但通过"单一RecyclerView + 子线程列表组装 + 独立组件"，它变得清晰、可维护、可扩展。

**性能优化关键点**：将列表组装放在 `Dispatchers.Default` 子线程中执行，即使列表项众多，也不会造成UI卡顿。ViewModel作为协调者，负责在合适的时机触发列表重建，Activity只负责最终的UI展示。

**下一篇预告：** 我们将探讨设计模式如何作为Lego架构的补充，让你的积木更加稳固、更加灵活。
