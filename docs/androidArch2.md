# Android架构系列博文（共4篇）

---

## 第二篇：Lego架构方法论：分治思想，让你的代码像积木一样可复用

### 前言
在上一篇中，我们回顾了Android架构十年的演化史，从MVC到MVI，每一次架构革命都在试图解决同一个问题：如何让代码更易维护、更易扩展。

但我们也发现了一个残酷的现实：**无论采用什么架构，如果缺乏正确的编程思想，代码依然会变成垃圾**。

那么，什么样的编程思想才是正确的？答案其实很简单，却常常被我们忽视——**分治法**。

---

### 一、Lego架构的本质：分治方法论

Lego架构不是一种新的架构模式，它是一种**编程思想**，它的本质就是**分治方法论**。

分治法是解决复杂问题的唯一通用方法：把一个大问题拆成无数个小问题，然后逐个解决。

Lego积木之所以神奇，正是因为它完美诠释了分治思想：
- **最小颗粒**：每一块积木都很小，只做一件事
- **高度复用**：同一块积木可以用在任何场景
- **自由组合**：通过组合不同的积木，可以搭建出任何复杂的东西
- **易于迁移**：当需要拆毁重建时，积木可以完整保留，用在下一个项目中

这就是Lego的神奇之处：**它用最简单的规则，实现了最大的灵活性**。

---

### 二、架构之外：代码组织的科学方法

在谈论架构之前，我们先问一个更本质的问题：**代码应该如何组织、如何划分才是科学的？**

答案只有一个：**无限拆分，直到最小颗粒**。

就像Lego积木一样，最小颗粒的复用性是最高的。当你把代码拆分到最小颗粒时：
- 每一块代码都只做一件事，职责单一
- 每一块代码都可以独立测试、独立复用
- 通过组合不同的代码块，可以快速构建复杂功能
- 当需求变化时，只需要替换或重组代码块，而不需要重写

这就是Lego架构的核心思想：**把代码拆分成最小颗粒的积木，然后通过组合这些积木来构建系统**。

---

### 三、根基：最小颗粒的复用性

为什么最小颗粒的复用性最高？让我们用一个简单的例子来说明。

假设你需要实现一个"登录功能"：

**错误的做法**：写一个巨大的Login类，包含所有逻辑
- 网络请求
- 数据验证
- 密码加密
- Token存储
- 错误处理
- UI更新

这个Login类可能有2000行代码，它只能在登录场景使用，无法复用。

**正确的做法**：拆分成最小颗粒的积木
- `HttpClient`：负责网络请求
- `Validator`：负责数据验证
- `Encryptor`：负责密码加密
- `TokenStorage`：负责Token存储
- `ErrorHandler`：负责错误处理
- `StateObserver`：负责UI更新

每个积木只有几十行代码，它们都可以在任何场景复用。当你需要实现"注册功能"时，只需要组合这些积木，而不需要重写任何代码。

这就是Lego架构的根基：**最小颗粒的复用性最高**。

---

### 四、实践一：工具的封装

在Lego架构中，工具是最核心的积木。我们将工具分为三个层次：

#### 1. 基础工具：标准——业务无关、项目无关、可以开源

基础工具是Lego架构的基石，它们应该满足三个标准：
- **业务无关**：不包含任何业务逻辑，只提供通用的技术能力
- **项目无关**：不依赖项目的特定配置或约定
- **可以开源**：代码质量足够高，可以独立发布到开源社区

基础工具的例子：
- `HttpClient`：封装网络请求
- `Validator`：封装数据验证
- `Encryptor`：封装加密算法
- `Storage`：封装数据存储
- `Logger`：封装日志记录
- `Timer`：封装定时任务

这些工具应该被封装成独立的库，可以在任何项目中使用。

#### 2. 高级工具：业务相关，放在对应的业务模块中，最小化namespace

高级工具是基于基础工具的组合，它们与特定业务相关。

高级工具的特点：
- **业务相关**：包含特定的业务逻辑
- **模块化**：放在对应的业务模块中，不污染全局命名空间
- **最小化**：只做一件事，职责单一

高级工具的例子：
- `LoginHelper`：组合`HttpClient`、`Validator`、`Encryptor`，实现登录逻辑
- `PaymentHelper`：组合`HttpClient`、`Encryptor`、`Storage`，实现支付逻辑
- `UserCenterHelper`：组合多个基础工具，实现用户中心逻辑

这些工具应该放在对应的业务模块中，避免全局污染。

#### 3. 工具组合：高级工具由若干基础工具组合而成，尽可能避免做出特殊的lego零件，难以复用

工具组合是Lego架构的核心思想：**高级工具由基础工具组合而成，而不是重新实现**。

**错误的做法**：为每个业务场景都写一个特殊的工具
```kotlin
class SpecialLoginHelper {
    // 重新实现了网络请求
    private fun sendRequest() { ... }
    // 重新实现了数据验证
    private fun validate() { ... }
    // 重新实现了加密
    private fun encrypt() { ... }
}
```

**正确的做法**：组合基础工具
```kotlin
class LoginHelper(
    private val httpClient: HttpClient,
    private val validator: Validator,
    private val encryptor: Encryptor
) {
    fun login(username: String, password: String) {
        validator.validate(username, password)
        val encrypted = encryptor.encrypt(password)
        httpClient.post("/login", mapOf("username" to username, "password" to encrypted))
    }
}
```

这样做的好处：
- 基础工具可以在多个高级工具中复用
- 当基础工具升级时，所有高级工具自动受益
- 避免了重复造轮子

---

### 五、实践二：ViewModel拆分

当一个页面的ViewModel代码行数超过2000行时，肯定是需要拆分了。

#### 拆分原则
- **按职能拆分**：每个ViewModel聚焦解决一类问题
- **按业务拆分**：每个ViewModel负责一个独立的业务模块
- **按状态拆分**：每个ViewModel管理一组相关的状态

#### 拆分示例

**错误的做法**：一个巨大的ViewModel
```kotlin
class UserProfileViewModel : ViewModel() {
    // 用户信息
    val userInfo = MutableLiveData<User>()
    
    // 订单列表
    val orderList = MutableLiveData<List<Order>>()
    
    // 地址列表
    val addressList = MutableLiveData<List<Address>>()
    
    // 优惠券列表
    val couponList = MutableLiveData<List<Coupon>>()
    
    // ... 2000+ 行代码
}
```

**正确的做法**：拆分成多个ViewModel
```kotlin
// 用户信息ViewModel
class UserInfoViewModel : ViewModel() {
    val userInfo = MutableLiveData<User>()
    fun loadUserInfo() { ... }
}

// 订单ViewModel
class OrderViewModel : ViewModel() {
    val orderList = MutableLiveData<List<Order>>()
    fun loadOrders() { ... }
}

// 地址ViewModel
class AddressViewModel : ViewModel() {
    val addressList = MutableLiveData<List<Address>>()
    fun loadAddresses() { ... }
}

// 优惠券ViewModel
class CouponViewModel : ViewModel() {
    val couponList = MutableLiveData<List<Coupon>>()
    fun loadCoupons() { ... }
}
```

#### 组合使用
在Activity或Fragment中组合使用多个ViewModel：
```kotlin
class UserProfileActivity : AppCompatActivity() {
    private val userInfoViewModel: UserInfoViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val addressViewModel: AddressViewModel by viewModels()
    private val couponViewModel: CouponViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfoViewModel.loadUserInfo()
        orderViewModel.loadOrders()
        addressViewModel.loadAddresses()
        couponViewModel.loadCoupons()
    }
}
```

这样做的好处：
- 每个ViewModel职责单一，易于维护
- 可以独立测试每个ViewModel
- 可以在多个页面复用同一个ViewModel
- 降低单个ViewModel的复杂度

---

### 六、实践三：Intent拆分

在MVI架构中，当一个页面的Intent数量爆炸后，维护成本会急剧提高。这时可以考虑将Intent根据职能或业务，拆分为N组Intent，每一组Intent由一个ViewModel持有。

#### Intent爆炸的问题
```kotlin
sealed class UserIntent {
    // 用户信息相关
    object LoadUserInfo : UserIntent()
    data class UpdateUserInfo(val user: User) : UserIntent()
    
    // 订单相关
    object LoadOrders : UserIntent()
    data class CreateOrder(val order: Order) : UserIntent()
    data class CancelOrder(val orderId: String) : UserIntent()
    
    // 地址相关
    object LoadAddresses : UserIntent()
    data class AddAddress(val address: Address) : UserIntent()
    data class DeleteAddress(val addressId: String) : UserIntent()
    
    // 优惠券相关
    object LoadCoupons : UserIntent()
    data class UseCoupon(val couponId: String) : UserIntent()
    
    // ... 200+ 个Intent
}
```

#### Intent拆分方案

**方案一：按业务拆分**
```kotlin
// 用户信息Intent
sealed class UserInfoIntent {
    object LoadUserInfo : UserInfoIntent()
    data class UpdateUserInfo(val user: User) : UserInfoIntent()
}

// 订单Intent
sealed class OrderIntent {
    object LoadOrders : OrderIntent()
    data class CreateOrder(val order: Order) : OrderIntent()
    data class CancelOrder(val orderId: String) : OrderIntent()
}

// 地址Intent
sealed class AddressIntent {
    object LoadAddresses : AddressIntent()
    data class AddAddress(val address: Address) : AddressIntent()
    data class DeleteAddress(val addressId: String) : AddressIntent()
}

// 优惠券Intent
sealed class CouponIntent {
    object LoadCoupons : CouponIntent()
    data class UseCoupon(val couponId: String) : CouponIntent()
}
```

**方案二：按职能拆分**
```kotlin
// 加载Intent
sealed class LoadIntent {
    object LoadUserInfo : LoadIntent()
    object LoadOrders : LoadIntent()
    object LoadAddresses : LoadIntent()
    object LoadCoupons : LoadIntent()
}

// 更新Intent
sealed class UpdateIntent {
    data class UpdateUserInfo(val user: User) : UpdateIntent()
    data class UpdateOrder(val order: Order) : UpdateIntent()
}

// 删除Intent
sealed class DeleteIntent {
    data class DeleteOrder(val orderId: String) : DeleteIntent()
    data class DeleteAddress(val addressId: String) : DeleteIntent()
}
```

#### 对应的ViewModel拆分
```kotlin
// 用户信息ViewModel
class UserInfoViewModel : ViewModel() {
    private val _state = MutableStateFlow<UserInfoState>(UserInfoState.Loading)
    val state: StateFlow<UserInfoState> = _state
    
    fun onIntent(intent: UserInfoIntent) {
        when (intent) {
            is UserInfoIntent.LoadUserInfo -> loadUserInfo()
            is UserInfoIntent.UpdateUserInfo -> updateUserInfo(intent.user)
        }
    }
}

// 订单ViewModel
class OrderViewModel : ViewModel() {
    private val _state = MutableStateFlow<OrderState>(OrderState.Loading)
    val state: StateFlow<OrderState> = _state
    
    fun onIntent(intent: OrderIntent) {
        when (intent) {
            is OrderIntent.LoadOrders -> loadOrders()
            is OrderIntent.CreateOrder -> createOrder(intent.order)
            is OrderIntent.CancelOrder -> cancelOrder(intent.orderId)
        }
    }
}
```

这样做的好处：
- 每个ViewModel只处理一组相关的Intent
- Intent的定义更加清晰，易于理解
- 降低单个ViewModel的复杂度
- 可以独立测试每组Intent的处理逻辑

---

### 七、实践四：State拆分

在MVI架构中，State也应该按照最小颗粒原则进行拆分。

#### State爆炸的问题
```kotlin
data class UserState(
    val userInfo: User? = null,
    val orderList: List<Order> = emptyList(),
    val addressList: List<Address> = emptyList(),
    val couponList: List<Coupon> = emptyList(),
    val isLoadingUserInfo: Boolean = false,
    val isLoadingOrders: Boolean = false,
    val isLoadingAddresses: Boolean = false,
    val isLoadingCoupons: Boolean = false,
    val userInfoError: String? = null,
    val orderError: String? = null,
    val addressError: String? = null,
    val couponError: String? = null,
    // ... 30+ 个字段
)
```

#### State拆分方案

**方案一：按业务拆分**
```kotlin
data class UserInfoState(
    val userInfo: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class OrderState(
    val orderList: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AddressState(
    val addressList: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CouponState(
    val couponList: List<Coupon> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**方案二：按状态类型拆分**
```kotlin
sealed class UserInfoState {
    object Loading : UserInfoState()
    data class Success(val user: User) : UserInfoState()
    data class Error(val message: String) : UserInfoState()
}

sealed class OrderState {
    object Loading : OrderState()
    data class Success(val orders: List<Order>) : OrderState()
    data class Error(val message: String) : OrderState()
}
```

这样做的好处：
- 每个State只包含相关的数据
- State的定义更加清晰
- 降低State的复杂度
- 可以独立观察每个State的变化

---

### 八、总结

Lego架构的核心思想可以总结为一句话：**无限拆分，直到最小颗粒**。

通过最小颗粒的拆分，我们获得了：
- **最高的复用性**：最小颗粒的代码可以在任何场景复用
- **最低的复杂度**：每个代码块只做一件事，易于理解和维护
- **最大的灵活性**：通过组合不同的代码块，可以快速构建复杂功能
- **最易的迁移**：当架构变化时，最小颗粒的代码可以原封不动地迁移

在下一篇文章中，我会通过一个完整的实战案例，展示如何在实际项目中应用Lego架构，以及如何评估代码的颗粒度是否合适。

---