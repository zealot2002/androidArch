## Android 架构系列博文（共4篇）

## 第二篇：Lego架构——分治思想的极致实践

> **系列导航**：[第一篇：主流 Android 架构十年演化史](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8) | [第三篇：用 Lego 架构重构商品详情页](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)

### 前言：垃圾代码的痛，根源不在架构

上一篇我们谈到，架构只是"术"，它只解决了代码的粗略分区，而粗略分区是远远不够的。这一篇，我们直面项目实施过程中的真实痛点，并提出一套可操作的分治方法论——Lego 架构。

### 零、灵感：架构迁移之痛引发的思考

我经历过三次大规模 Android 架构重构，每次都像脱了一层皮：

- 从 MVC 到 MVP，团队花了两个月，造出了一套厚重的 `BaseActivity` / `BaseFragment` / `BasePresenter`。
- 从 MVP 到 MVVM，Base 类全部重写，大量与 Base 深度耦合的工具类被迫修改，业务代码牵动 40%。
- 当 MVI 开始流行时，我们彻底干不动了。

到底是什么让我们如此被动？迁移到 MVI 就一劳永逸了吗？未来再冒出新的 MV-Whatever，难道还要重演噩梦？如果非迁不可，哪些代码可以原封不动地保留？

这番痛苦逼我们追问一个根本问题：**架构的终极答案究竟是什么？**

我坐在电脑前，忽然想起小时候玩的 Lego 积木。

为什么几块基础砖能搭出房子、车子、飞船？为什么 10 年前的积木，今天还能和新套装完美拼合？\
那一刻，我仿佛看到了所有架构问题的答案。

Lego 最神奇的地方，不在那些炫酷的成品，而在那些 1×1、2×4 基础积木：

只有几个凸点和凹槽，没有任何预设功能。

遵循全球统一的连接标准，可与任何其他 Lego 积木完美拼接。

可用于任何模型的任何位置。

自 1958 年诞生以来，接口规格从未变过。

反观那些为特定模型设计的异形零件——比如千年隼的弧形舱壁——除了拼那一个模型，几乎别无他用。拆下来就成了废料。

**Lego 的核心哲学是：最小颗粒度 = 最高复用性 = 最强灵活性。
而异形零件，离开模型即废。代码复用与否的道理，与它如出一辙。**

由此，我们提炼出一套面向软件工程的 **Lego 架构**——它不是要取代 MVVM 或 MVI，而是一套关于 **"如何拆分、如何沉淀、如何治理"** 的编程思想与工程纪律。

***

### 一、Base类：能没有就没有，不能没有就只剩薄壳

在三次架构重构中，最让我痛苦的不是业务逻辑，而是那些看似"省事"的Base类。

问题出在哪？**我们把太多不该绑定的东西绑在了Base上。**

#### 1.1 Base的正确用法：能没有就没有

Base类只有一个合理的存在理由：**为那些90%页面都需要的、且与生命周期强相关的能力提供一个接缝。**

比如：

- 埋点自动上报页面进入/退出
- 权限请求的回调分发

除此之外，一切业务逻辑、页面初始化顺序、工具类引用，都不应该进入Base。

#### 1.2 极度反感：Base里约束页面初始化工作流

我曾见过这样的Base：

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCallerData()
        initView()
        initPageData()
        initObservers()
    }
    abstract fun initView()
    abstract fun initPageData()
    abstract fun initCallerData()
    abstract fun initObservers()
}
```

这是对子类的**暴力约束**。当你不知道上层页面想以什么顺序初始化时（比如有的想先初始化observer，有的想先初始化view），这种模板方法就成了枷锁和冗余混乱的根源。

**好的Base不应该规定顺序，只提供可选的能力。** 甚至连`initView`都不应该出现——让每个页面自己去`onCreate`里写，清晰明了。

#### 1.3 可插拔积木代理Base

通过观察不难发现，Base 类里经常有大量本可以放入 xxxUtils 里的方法或代码块——权限请求、软键盘管理、网络检测……它们被塞进 Base，仅仅因为"多个页面都需要"，而不是真的与 Base 生命周期强相关。

更好的做法：Base 只是一个空壳，所有功能都通过可插拔的积木来提供。

例如，你需要权限请求能力，不一定要写在 Base 里，而是在需要的地方直接使用一个独立的 PermissionHelper：

```kotlin
class PermissionHelper(private val activity: AppCompatActivity) {
    fun request(permission: String, onGranted: () -> Unit) { ... }
}
```

任何页面、任何 Fragment、任何 Dialog 都可以直接使用这个积木，不依赖继承。Base 甚至可以是 open class 但里面空空如也。

### 二、从粗粒度分治到极致分治：Lego的准则

业内所有主流架构（MVC、MVP、MVVM、MVI、Clean Architecture……）本质上都是**分治思想**的一种体现——它们把代码按职责、按层级、按数据流向切分。

但它们的粒度通常是**粗的**：

- 一层（View、ViewModel、Model）
- 一个模块（feature、domain、data）
- 一个角色（Presenter、UseCase）

它们没有回答一个关键问题：**分到多细才算够？**

Lego积木给出了物理世界的答案：一直分到**不可再分的基础颗粒**。一块2×4的积木，接口统一，可在Lego 架构，就是分治思想的极致实践任何模型、任何位置使用，并且历经68年不改变。

**Lego架构，就是分治思想的极致实践：不仅仅是在MVX层面分，而是在所有层面、所有维度上，无限拆分，直到每个单元只做一件事，接口稳定，没有冗余功能。**

这个准则可以应用于：

- UI层：一个`RecyclerView`的每个`ViewHolder`都是一个独立积木
- 逻辑层：每个`UseCase`只封装一个业务场景
- 工具层：每个工具方法只做一件事
- 状态层：每个`State`只描述一个独立域

拆分的停止条件很简单：当你无法再给这个单元起一个更小、更准确的名字时，就可以停了。如果一个类或方法的名字里出现了 "和"、"与"、"以及"，那就说明它还可以再拆。

这就是Lego架构与其他架构最本质的区别：

> 其他架构告诉你"代码应该放在哪一层"。\
> Lego架构告诉你"代码应该拆成多小，以及如何持续拆下去"。

***

### 三、Lego思想下的拆分实例

让我们用几个具体例子，看看在Lego准则下，代码是如何被拆成最小颗粒的。

#### 3.1 工具类：从大杂烩到原子积木

**反面例子**：一个`DateUtils`里塞了时间格式化、时间戳转换、相对时间计算、时区处理……十几个方法，几百行代码。

**Lego拆分**：

- `DateFormatUtils`：只做日期格式化成字符串
- `TimestampConverter`：只做时间戳与日期对象互转
- `RelativeTimeCalculator`：只做"刚刚、几分钟前"这类计算
- `TimeZoneHelper`：只做时区相关操作

每个类只有一个职责，方法名清晰，易于测试和复用。

#### 3.2 ViewModel：从上帝对象到服务组合

**反面例子**：一个`HomeViewModel`包含了轮播图、推荐列表、用户信息、购物车数量、通知未读数……5000行代码，改一个地方可能影响其他不相关字段。

**Lego拆分**：

- `HomeBannerViewModel`：只负责轮播图数据
- `HomeRecommendViewModel`：只负责推荐列表
- `ShoppingCartViewModel`：通用服务，可被多个页面复用
- `NotificationViewModel`：通用服务，可被多个页面复用

在Activity中组合使用：

```kotlin
private val bannerVM: HomeBannerViewModel by viewModels()
private val cartVM: ShoppingCartViewModel by viewModels()
private val notificationVM: NotificationViewModel by viewModels()
```

每个ViewModel都可以独立测试、独立复用。甚至`ShoppingCartViewModel`可以在商品详情页、购物车页、首页同时被观察。

#### 3.3 Intent/MVI State：从爆炸到分组

**反面例子**：一个`HomeIntent`密封类里塞了200多个意图，包括轮播图的点击、推荐列表的加载更多、用户头像的点击……

**Lego拆分**：将Intent与ViewModel对齐，每个ViewModel对应一个Intent组。

```kotlin
sealed class BannerIntent {
    object Load : BannerIntent()
    data class Click(val position: Int) : BannerIntent()
}
sealed class RecommendIntent {
    object LoadMore : RecommendIntent()
    data class Click(val item: RecommendItem) : RecommendIntent()
}
```

State同理：不要把所有字段塞进一个`HomeState`，而是拆分为`BannerState`、`RecommendState`等，每个ViewModel管理自己的状态。

#### 3.4 UseCase：一个场景一个类

**反面例子**：`UserUseCase`里有登录、注册、修改密码、获取用户信息、上传头像……五个不相关的操作。

**Lego拆分**：每个UseCase只封装一个完整的业务场景。

- `LoginUseCase`
- `RegisterUseCase`
- `UpdatePasswordUseCase`
- `FetchUserInfoUseCase`

这样，任何页面只需要依赖它需要的那个UseCase，而不是整个大杂烩。

***

### 四、工具类的发现与迭代：好的积木是长出来的

Lego架构中的积木不是一次性设计出来的，而是在持续开发和重构中自然涌现的——本质上，这是一个从异形零件逐步演化为基础零件的过程。

#### 4.1 私有积木：随用随写

在需求开发中，你发现有一段逻辑可以封装（比如一个价格格式化方法），但不确定是否通用。那就先写在当前模块的`utils`包下，作为**私有积木**。

```kotlin
// feature_goods/utils/PriceFormatUtils.kt
internal fun formatPrice(priceYuan: String): String = "¥$priceYuan"
```

它只在本模块使用，不暴露给其他模块。

#### 4.2 共有积木：定期复盘，抽离复用

项目每季度进行一次复盘重构。团队会扫描各个`feature_xxx/utils`下的私有工具，找出那些**至少被两个模块使用**或**明显具有通用价值**的类，将它们移动到`common/utils`模块，成为**共有积木**。

同时，原来的私有积木消失（或变薄，只留下一个对共有积木的委托）。这个步骤需要配合单元测试和Code Review，确保移动过程不破坏功能。

#### 4.3 远程积木：久经考验，成为资产

一个共有积木经过线上半年或一年的使用，被百万用户验证稳定后，就可以从`common`模块中独立出来，发布到Maven仓库，成为**远程积木**，供公司所有项目直接依赖。

这时，它就成了**"一生只写一次的工具"**——比如`StringUtils`、`NetworkUtils`、`ScreenUtils`。你永远不需要再重写它们，只需升级版本号。

#### 4.4 为什么一定要拆到最小颗粒？

因为只有拆得足够小，你才能在定期扫描时一眼看出："哦，这个格式化和那个格式化可以合并""这个判断逻辑已经出现三次了"。如果它们散落在几百行的大类里，或者干脆被重复写在不同的Activity中，你就永远发现不了复用机会。

**Lego架构的无限拆分，不是为了拆分而拆分，而是为了让复用机会从"隐形"变成"可见"，从"被动发现"变成"主动扫描"。**

***

### 五、总结：Lego架构，以不变应万变

Lego架构不是要取代你现有的MVVM或MVI，它是一套**编程纪律和治理思想**，要求你在任何架构之上做到：

1. **Base类极简化**：能没有就没有，不得不放也只放最基础的生命周期接缝，绝不规定初始化顺序。
2. **无限拆分**：在所有层面（UI、逻辑、状态、工具）把代码拆到最小稳定颗粒，直到每个单元只做一件事。
3. **持续迭代工具**：私有 → 共有 → 远程，让好的积木自然生长，最终成为"一生只写一次"的技术资产。

当你真正做到这些，你会发现：

- 架构迁移不再是灾难：Base类是薄壳，业务逻辑全在可插拔积木里，换架构只换容器。
- 代码复用不再是口号：最小颗粒自然驱动复用，甚至你还没来得及写，别人已经提供了远程积木。
- 维护成本指数下降：每个积木职责单一，改一处不会波及无关地方。
- 团队协作轻松：新人只需学习积木用法，而不是理解几千行的上帝类。

这就是Lego架构——分治思想的极致实践，一套以不变应万变的方法论。
**下一篇预告：** 我们将用一个真实的电商App商品详情页，完整演示Lego架构从拆分到组装的全过程。

***

**相关阅读**：
- [第一篇：主流 Android 架构十年演化史——我们到底在解决什么问题？](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8)
- [第三篇：用 Lego 架构重构商品详情页：从 3000 行到 15 个独立组件](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)
