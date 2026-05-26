# Android架构系列博文（共4篇）

---

## 第二篇：Lego架构方法论：分治思想的极致实践，让你的代码可插拔迁移

### 前言
我经历过三次大规模的Android架构重构，每一次都像扒了一层皮：
- 第一次：从MVC迁移到MVP，熬了三个月写了一套庞大的BaseActivity/BaseFragment/BasePresenter
- 第二次：从MVP迁移到MVVM，又把所有Base类全部重写一遍，业务代码跟着改了80%
- 第三次：当MVI开始流行时，我看着项目里那套已经膨胀到几千行的Base类体系，彻底干不动了

我坐在电脑前，看着满地散落的Base类碎片，突然想起了小时候玩的Lego积木。

为什么Lego可以从一个小房子搭到一座城市？为什么同样的积木可以搭出汽车、飞机、机器人？为什么我小时候买的Lego积木，现在拿出来还能和新买的完美拼在一起？

那一刻，我突然想通了所有架构问题的答案。

---

## 一、Lego的神奇：为什么最小颗粒度是复用的终极答案

Lego是人类有史以来最伟大的发明之一。它的神奇之处不在于那些酷炫的成品模型，而在于它最基础的那一块2x4的积木。

- 一块标准的2x4 Lego积木，只有8个凸起和3个凹槽
- 它没有任何特殊的形状，没有任何预设的用途
- 它可以和任何其他Lego积木完美拼接
- 它可以用在任何模型的任何位置
- 它生产于1958年，至今仍然和最新的Lego积木100%兼容

反过来，那些为了特定模型设计的特殊形状积木，复用性几乎为零。你买了一个星球大战的千年隼套装，里面那些奇形怪状的特殊零件，除了搭千年隼之外，几乎没有任何其他用处。当你拆了千年隼想搭别的东西时，这些特殊零件就变成了垃圾。

**这就是Lego最核心的哲学：最小颗粒度 = 最高复用性 = 最强灵活性。**

代码也是完全一样的道理。

---

## 二、Lego架构的本质：分治思想的极致实践

在谈论任何架构之前，我们先回到最根本的问题：**世界上只有一种通用的解决复杂问题的方法——分治法。**

把一个大问题拆成无数个小问题，然后逐个解决。这是人类面对复杂问题时唯一有效的武器。

所有的架构模式，本质上都是分治法在不同维度的应用：
- MVC是按职责分
- MVP是按依赖分
- MVVM是按数据流向分
- Clean Architecture是按层级分
- MVI是按状态分

它们只是拆分的方式不同，但目标完全一致。

而**Lego架构，就是分治思想的极致实践**。

它不满足于把代码拆成三层、四层，它要求你把代码**无限拆分，直到不能再拆分为止**。直到每一块代码都像那块2x4的Lego积木一样，只做一件事，只解决一个问题，没有任何多余的功能。

这就是Lego架构和其他所有架构最本质的区别：
- 其他架构告诉你"代码应该放在哪里"
- Lego架构告诉你"代码应该拆成多大"

---

## 三、"轻装修，重装饰"：架构迁移的终极解决方案

在经历了三次痛苦的架构迁移之后，我总结出了一个血泪教训：**架构是会过时的，但好的积木永远不会。**

我在搬家的时候悟出了这个道理：
- 基础装修是带不走的。你在墙上打的孔、铺的地板、刷的油漆，当你搬家的时候，都只能留在原地。新家要重新装修，这是一笔巨大的开销。
- 但是你的家具是可以带走的。你昂贵的沙发、床、餐桌、电器，都可以原封不动地搬到新家继续使用。

在架构迁移的过程中，我发现了一个惊人的相似之处：
- **基础装修 = Base类体系**：那些下沉到BaseActivity、BaseFragment、BaseViewModel里的一大坨代码，是和当前架构深度绑定的。当架构变化时，它们必须全部重写。
- **家具 = 可插拔的积木**：那些封装得很好的工具类、高级工具、中间件，从来都是非常顺利地迁移到新架构上来。它们不依赖任何特定的架构，只依赖Android系统本身。

这就是Lego架构的核心哲学：**轻装修，重装饰。**

- Base类只是一个空壳，它只提供最基本的生命周期回调，不包含任何业务逻辑
- 所有的功能都通过可插拔的积木来实现
- 积木之间通过组合来实现复杂功能，而不是通过继承
- 当架构变化时，你只需要重新装修一下Base类这个空壳，所有的家具都可以原封不动地搬进去

---

## 四、Lego架构的四大落地实践

Lego架构不是空中楼阁，它有非常具体、可落地的实践方法。所有的实践都围绕一个核心原则：**无限拆分，直到最小颗粒。**

### 实践一：工具的三层封装体系

工具是Lego架构最基础的积木。我们把工具分为三层，每一层都有明确的职责和标准：

#### 1. 基础工具：通用原子积木
**标准**：业务无关、项目无关、可以直接开源  
**存放位置**：独立的base-android/base-java模块  
**粒度**：原子级，一个方法只做一件事

例子：
- `ToastUtils.show(message)`：只负责弹Toast，不包含任何业务文案
- `SPUtils.put(key, value)`：只负责读写SP，不包含任何业务key
- `NetworkUtils.isConnected()`：只负责判断网络状态，不处理任何网络错误

基础工具是最底层的积木，它们应该像标准的2x4 Lego积木一样，可以用在任何项目的任何地方。

#### 2. 高级工具：业务组合积木
**标准**：业务相关、项目相关、可以在项目内复用  
**存放位置**：对应的业务模块中，最小化namespace  
**粒度**：由若干基础工具组合而成

例子：
- `LoginToast.showError(message)`：封装了登录模块统一的Toast样式
- `UserSPUtils.saveUser(user)`：封装了用户信息的存储逻辑
- `ImageLoader.loadAvatar(url, imageView)`：封装了头像的统一加载逻辑

高级工具是为了解决特定业务场景的通用问题而存在的。它们由基础工具组合而成，不应该包含任何特殊的、无法复用的逻辑。

#### 3. 工具组合：场景化解决方案
**标准**：针对特定场景，由多个高级工具和基础工具组合而成  
**存放位置**：使用它的页面或组件中  
**粒度**：完成一个完整的业务流程

例子：
```kotlin
suspend fun login(username: String, password: String): Result<User> {
    if (!NetworkUtils.isConnected()) {
        return Result.failure(NetworkException())
    }
    
    LoadingUtils.show(activity)
    return try {
        val user = ApiService.login(username, password)
        UserSPUtils.saveUser(user)
        LoginToast.showSuccess("登录成功")
        Result.success(user)
    } catch (e: Exception) {
        LoginToast.showError(e.message)
        Result.failure(e)
    } finally {
        LoadingUtils.dismiss()
    }
}
```

**最重要的原则**：尽可能避免做出特殊的Lego零件。如果一个工具只能用在一个地方，那它就不是一个好的工具，应该被拆分成更小的通用工具。

### 实践二：ViewModel按职责拆分，拒绝"上帝ViewModel"

当一个页面的ViewModel代码行数超过2000行时，它就变成了一个"上帝ViewModel"，维护成本会指数级上升。

Lego架构的解决方案是：**不要一个页面只能有一个ViewModel，而是一个页面可以有多个ViewModel，每个ViewModel聚焦解决一类问题。**

拆分原则：
- 按业务职责拆分，而不是按UI拆分
- 每个ViewModel的代码行数控制在500行以内
- 每个ViewModel只负责一个独立的业务域

例子：
```kotlin
// 不要这样写：一个5000行的GodViewModel
class HomeViewModel : ViewModel() {
    // 轮播图逻辑
    // 推荐列表逻辑
    // 分类列表逻辑
    // 搜索逻辑
    // 购物车逻辑
}

// 应该这样写：多个职责单一的ViewModel
class HomeBannerViewModel : ViewModel() {
    // 只负责轮播图逻辑
}

class HomeRecommendViewModel : ViewModel() {
    // 只负责推荐列表逻辑
}

class HomeCategoryViewModel : ViewModel() {
    // 只负责分类列表逻辑
}
```

在View中，你可以同时实例化多个ViewModel，分别观察它们的状态：
```kotlin
class HomeActivity : AppCompatActivity() {
    private val bannerViewModel: HomeBannerViewModel by viewModels()
    private val recommendViewModel: HomeRecommendViewModel by viewModels()
    private val categoryViewModel: HomeCategoryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bannerViewModel.state.observe(this) { updateBanner(it) }
        recommendViewModel.state.observe(this) { updateRecommendList(it) }
        categoryViewModel.state.observe(this) { updateCategoryList(it) }
    }
}
```

拆分后的好处：
- 每个ViewModel都很小，容易理解和维护
- 不同的业务逻辑完全解耦
- 可以单独测试每个ViewModel
- 相同的ViewModel可以在多个页面复用

### 实践三：Intent按业务域分组，解决Intent爆炸问题

MVI最大的痛点就是Intent爆炸。一个复杂页面很容易就有200+个Intent，命名又臭又长，维护起来简直是灾难。

Lego架构的解决方案是：**不要把所有Intent都放在一个文件里，而是根据业务域拆分为N组Intent，每一组Intent由对应的ViewModel持有。**

拆分原则：
- 和ViewModel的拆分保持一致
- 一组Intent对应一个ViewModel
- 每个Intent只做一件事

例子：
```kotlin
// 不要这样写：一个包含200个Intent的密封类
sealed class HomeIntent {
    object LoadBanner : HomeIntent()
    object RefreshBanner : HomeIntent()
    object ClickBanner : HomeIntent()
    object LoadRecommendList : HomeIntent()
    // ... 还有190个
}

// 应该这样写：按业务域分组的Intent
sealed class BannerIntent {
    object Load : BannerIntent()
    object Refresh : BannerIntent()
    data class Click(val position: Int) : BannerIntent()
}

sealed class RecommendIntent {
    object Load : RecommendIntent()
    object Refresh : RecommendIntent()
    object LoadMore : RecommendIntent()
    data class Click(val item: RecommendItem) : RecommendIntent()
}
```

每个ViewModel只处理自己对应的Intent：
```kotlin
class HomeBannerViewModel : ViewModel() {
    fun handleIntent(intent: BannerIntent) {
        when (intent) {
            is BannerIntent.Load -> loadBanner()
            is BannerIntent.Refresh -> refreshBanner()
            is BannerIntent.Click -> handleBannerClick(intent.position)
        }
    }
}
```

拆分后的好处：
- Intent数量不再爆炸，每个密封类只有几个Intent
- 代码结构清晰，很容易找到某个Intent对应的处理逻辑
- 新增业务时，只需要新增对应的Intent组，不需要修改现有代码
- 相同的Intent组可以在多个ViewModel中复用

### 实践四：业务逻辑下沉到UseCase，实现跨页面复用

很多人会犯一个错误：把业务逻辑写在ViewModel里。这会导致相同的业务逻辑在多个ViewModel中重复出现，维护成本极高。

Lego架构的解决方案是：**把所有的业务逻辑都下沉到UseCase中，ViewModel只负责协调UseCase和View之间的交互。**

UseCase是Lego架构中最高级的积木，它封装了一个完整的、独立的业务逻辑。

例子：
```kotlin
class LoginUseCase(
    private val userRepository: UserRepository,
    private val userSPUtils: UserSPUtils
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isEmpty()) {
            return Result.failure(IllegalArgumentException("用户名不能为空"))
        }
        if (password.isEmpty()) {
            return Result.failure(IllegalArgumentException("密码不能为空"))
        }
        
        val user = userRepository.login(username, password)
        userSPUtils.saveUser(user)
        return Result.success(user)
    }
}
```

ViewModel只负责调用UseCase：
```kotlin
class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()
    
    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Submit -> login(intent.username, intent.password)
        }
    }
    
    private fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = loginUseCase(username, password)
            _state.value = when (result) {
                is Result.Success -> LoginState.Success(result.data)
                is Result.Failure -> LoginState.Error(result.exception.message)
            }
        }
    }
}
```

拆分后的好处：
- 业务逻辑完全独立，和任何架构都无关
- 相同的业务逻辑可以在多个ViewModel中复用
- 可以单独测试业务逻辑，不需要依赖ViewModel
- 当架构变化时，所有的UseCase都可以原封不动地继续使用

---

## 五、总结

Lego架构不是一种新的架构模式，它是一种编程思想和工程实践。它不要求你放弃现有的MVVM或MVI，而是在它们之上提供一个额外的抽象层。

它的核心只有一句话：**把你的代码拆成最小的颗粒，像Lego积木一样。**

当你做到这一点时，你会发现：
- 架构迁移不再是一场灾难，而只是换一个容器
- 代码复用不再是一句口号，而是自然而然的事情
- 新人入职不再需要花几个月学习复杂的架构体系，只需要掌握几个核心积木的用法
- 项目的维护成本会指数级下降

---