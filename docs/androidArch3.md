# Android架构系列博文（共4篇）

---

## 第三篇：Lego架构实战：拆解超级复杂的商品详情页

### 前言
商品详情页是电商App中最复杂的页面之一。一个典型的商品详情页可能包含：

- 商品图片轮播
- 商品基本信息（名称、价格、销量、评分）
- 商品规格选择（颜色、尺寸、版本）
- 购物车操作（加入购物车、修改数量）
- 立即购买
- 商品收藏
- 商品分享
- 商品描述（图文混排）
- 用户评价（列表、图片、视频）
- 推荐商品列表
- 店铺信息
- 优惠券领取
- 客服咨询
- 物流信息
- 售后服务

如果把所有这些功能都塞进一个Activity和一个ViewModel里，代码量很容易超过10000行，维护起来简直是噩梦。

今天，我们就用Lego架构来拆解这个超级复杂的页面，看看如何把它变成一堆可复用的积木。

---

### 一、商品详情页的Lego化拆解

按照Lego架构的原则，我们需要把商品详情页拆分成最小颗粒的组件。

#### 1. 按业务域拆分ViewModel

一个页面可以有多个ViewModel，每个ViewModel只负责一个业务域：

```kotlin
// 商品基础信息ViewModel
class ProductInfoViewModel : ViewModel() {
    fun loadProductInfo(productId: String) { ... }
}

// 商品规格选择ViewModel
class ProductSpecViewModel : ViewModel() {
    fun selectSpec(spec: SpecItem) { ... }
    fun getSelectedSpec(): SpecItem { ... }
}

// 购物车操作ViewModel
class CartViewModel : ViewModel() {
    fun addToCart(product: Product, count: Int) { ... }
    fun updateCartItemCount(itemId: String, count: Int) { ... }
}

// 用户评价ViewModel
class ReviewViewModel : ViewModel() {
    fun loadReviews(productId: String) { ... }
    fun submitReview(review: Review) { ... }
}

// 推荐商品ViewModel
class RecommendViewModel : ViewModel() {
    fun loadRecommendations(productId: String) { ... }
}

// 店铺信息ViewModel
class ShopViewModel : ViewModel() {
    fun loadShopInfo(shopId: String) { ... }
    fun followShop(shopId: String) { ... }
}

// 优惠券ViewModel
class CouponViewModel : ViewModel() {
    fun loadCoupons(productId: String) { ... }
    fun collectCoupon(couponId: String) { ... }
}
```

每个ViewModel都很小，职责单一，可以独立测试和复用。

#### 2. 按业务域拆分Intent

在MVI模式下，我们同样需要把Intent按业务域分组：

```kotlin
// 商品信息Intent
sealed class ProductInfoIntent {
    data class Load(val productId: String) : ProductInfoIntent()
}

// 规格选择Intent
sealed class SpecIntent {
    data class SelectColor(val color: String) : SpecIntent()
    data class SelectSize(val size: String) : SpecIntent()
    data class SelectVersion(val version: String) : SpecIntent()
}

// 购物车Intent
sealed class CartIntent {
    data class Add(val product: Product, val count: Int) : CartIntent()
    data class UpdateCount(val itemId: String, val count: Int) : CartIntent()
}

// 评价Intent
sealed class ReviewIntent {
    data class Load(val productId: String, val page: Int) : ReviewIntent()
    data class Submit(val review: Review) : ReviewIntent()
}

// 推荐Intent
sealed class RecommendIntent {
    data class Load(val productId: String) : RecommendIntent()
}
```

#### 3. 拆分UI组件

将页面拆分成多个独立的UI组件，每个组件只负责展示一个功能：

```kotlin
// 商品图片轮播组件
@Composable
fun ProductImageCarousel(images: List<String>) {
    // 只负责图片轮播
}

// 商品基础信息组件
@Composable
fun ProductBasicInfo(info: ProductInfo) {
    // 只负责展示商品名称、价格、销量等
}

// 规格选择组件
@Composable
fun SpecSelector(specs: List<SpecItem>, selectedSpec: SpecItem, onSelect: (SpecItem) -> Unit) {
    // 只负责规格选择
}

// 操作栏组件
@Composable
fun ActionBar(onAddToCart: () -> Unit, onBuyNow: () -> Unit) {
    // 只负责底部操作按钮
}

// 商品描述组件
@Composable
fun ProductDescription(description: String) {
    // 只负责展示商品描述
}

// 用户评价组件
@Composable
fun ReviewList(reviews: List<Review>) {
    // 只负责展示评价列表
}

// 推荐商品组件
@Composable
fun RecommendList(products: List<Product>) {
    // 只负责展示推荐商品
}

// 店铺信息组件
@Composable
fun ShopInfo(shop: Shop) {
    // 只负责展示店铺信息
}

// 优惠券组件
@Composable
fun CouponList(coupons: List<Coupon>, onCollect: (Coupon) -> Unit) {
    // 只负责展示和领取优惠券
}
```

#### 4. 组合所有组件

在主页面中组合所有组件：

```kotlin
@Composable
fun ProductDetailPage(productId: String) {
    // 注入多个ViewModel
    val infoViewModel: ProductInfoViewModel by viewModels()
    val specViewModel: ProductSpecViewModel by viewModels()
    val cartViewModel: CartViewModel by viewModels()
    val reviewViewModel: ReviewViewModel by viewModels()
    val recommendViewModel: RecommendViewModel by viewModels()
    val shopViewModel: ShopViewModel by viewModels()
    val couponViewModel: CouponViewModel by viewModels()
    
    // 观察各个ViewModel的状态
    val productInfo by infoViewModel.state.collectAsState()
    val selectedSpec by specViewModel.selectedSpec.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val recommendations by recommendViewModel.recommendations.collectAsState()
    val coupons by couponViewModel.coupons.collectAsState()
    
    // 组合所有组件
    Column {
        ProductImageCarousel(productInfo.images)
        ProductBasicInfo(productInfo)
        SpecSelector(productInfo.specs, selectedSpec) { specViewModel.selectSpec(it) }
        CouponList(coupons) { couponViewModel.collectCoupon(it.id) }
        ProductDescription(productInfo.description)
        ReviewList(reviews)
        RecommendList(recommendations)
        ShopInfo(productInfo.shop)
        ActionBar(
            onAddToCart = { cartViewModel.addToCart(productInfo, 1) },
            onBuyNow = { /* 处理立即购买 */ }
        )
    }
}
```

---

### 二、业务逻辑下沉到UseCase

把所有业务逻辑都封装到UseCase中，ViewModel只负责协调：

```kotlin
// 商品信息UseCase
class GetProductInfoUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(productId: String): Result<ProductInfo> {
        return repository.getProductInfo(productId)
    }
}

// 购物车UseCase
class AddToCartUseCase(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(product: Product, count: Int): Result<CartItem> {
        val userId = userRepository.getCurrentUserId()
        return cartRepository.addItem(userId, product, count)
    }
}

// 优惠券UseCase
class CollectCouponUseCase(
    private val couponRepository: CouponRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(couponId: String): Result<Boolean> {
        val userId = userRepository.getCurrentUserId()
        return couponRepository.collectCoupon(userId, couponId)
    }
}
```

ViewModel变得非常简洁：

```kotlin
class CartViewModel(
    private val addToCartUseCase: AddToCartUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<CartState>(CartState.Idle)
    val state: StateFlow<CartState> = _state
    
    fun addToCart(product: Product, count: Int) {
        viewModelScope.launch {
            _state.value = CartState.Loading
            val result = addToCartUseCase(product, count)
            _state.value = when (result) {
                is Result.Success -> CartState.Success(result.data)
                is Result.Failure -> CartState.Error(result.exception.message)
            }
        }
    }
}
```

---

### 三、工具层的支撑

所有的工具都来自`base-android`和`base-java`模块：

```kotlin
// 使用基础工具
lifecycleScope.launch {
    // 网络请求工具
    val result = HttpUtils.get<Product>("https://api.example.com/product/$productId")
    
    // Toast工具
    ToastUtils.showSuccess("加入购物车成功")
    
    // 加载框工具
    LoadingUtils.show(activity)
    
    // 图片加载工具
    ImageLoader.load(product.imageUrl, imageView)
    
    // 路由工具
    Router.navigateTo(ShopDetailActivity::class.java, bundleOf("shopId" to shopId))
    
    // 分享工具
    ShareUtils.share(ShareType.WECHAT, product.title, product.shareUrl)
    
    // 存储工具
    SPUtils.put("lastViewedProduct", productId)
}
```

---

### 四、Lego化的优势

通过Lego架构拆解商品详情页，我们获得了以下优势：

| 维度 | 传统方式 | Lego方式 |
|------|----------|----------|
| **代码量** | 1个10000+行的Activity | 10+个100-500行的组件 |
| **可维护性** | 牵一发动全身 | 独立修改，互不影响 |
| **可测试性** | 难以单独测试 | 每个组件可独立测试 |
| **可复用性** | 几乎无法复用 | 组件可在多个页面复用 |
| **并行开发** | 只能串行开发 | 多个开发者可并行开发不同组件 |
| **架构迁移** | 代价巨大 | 只需修改组件与框架的接口 |

---

### 五、总结

Lego架构的威力在于：**无限拆分，直到最小颗粒**。

商品详情页虽然复杂，但只要我们遵循Lego原则，就能把它拆分成一个个独立的、可复用的积木。这些积木不仅可以在商品详情页使用，还可以在其他页面复用。

当未来需要迁移到新架构时，我们只需要修改组件与框架的接口部分，而所有的业务逻辑和工具代码都可以原封不动地保留。

这就是Lego架构的终极目标：**让代码成为永恒的积木，而不是一次性的垃圾**。

---

**下一篇预告：** 我们将探讨设计模式如何作为Lego架构的补充，让你的积木更加稳固、更加灵活。