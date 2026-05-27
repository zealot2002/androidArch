原文构思巧妙，用 Lego 积木类比代码复用，核心观点有说服力。但在逻辑衔接、概念一致性和例证力度上还有打磨空间。我梳理了主要问题，并给出优化版本。

---

### 主要问题与优化方向

1. **主线不够聚焦**  
   从“架构迁移之痛”跳到 Lego，再跳到分治法，又跳到“轻装修，重装饰”，最后落地四个实践，段落间缺乏递进式承接。  
   **优化**：用“最小粒度—最高复用—架构无关”这条线索贯穿全文，每部分都回扣这个核心，让读者感到所有内容都在证明同一个命题。

2. **分治法与 Lego 的关联不够直接**  
   原文说“Lego 架构是分治思想的极致实践”，但缺少对“为什么 Lego 就是分治”的展开。读者可能觉得牵强。  
   **优化**：明确点出：Lego 正是把“物体”不断拆分，直到不可再分的基础颗粒，并用统一接口连接。这与笛卡尔分治法一脉相承。由此自然引出“代码也应该拆到不可再拆为止”。

3. **“轻装修，重装饰”类比有歧义**  
   装修和家具的对比中，“家具”不等于最小颗粒积木，它可能是一个整体沙发，而不是小颗粒。这与前面强调的“无限拆分”矛盾。  
   **优化**：重构类比。装修对应的是架构框架（Base 类、平台绑定），家具对应的是**独立的功能积木**。并强调这些“家具”本身也是由更小积木组合而成，从而与“三层工具封装”呼应。

4. **四个实践略显堆砌**  
   ViewModel 拆分、Intent 分组、UseCase 下沉都是好实践，但原文把它们并列，缺少与 Lego 思想的强关联。  
   **优化**：每个实践开头先点明它体现了 Lego 架构的哪条原则（如“职责单一，直到最小单元”“业务逻辑与框架解耦”），并用“带来的复用/迁移红利”收尾，让四个实践成为“分治思想”在不同层次的投影。

5. **说服力可强化**  
   可以加入一两个“如果没有这样做会怎样”的反面案例，以及“这样做之后获得了什么”的量化或质化结果（哪怕模糊的“迁移时间从两个月缩短到一周”），增强可信度。

---

### 优化版全文（保留核心观点和示例，重构逻辑链）

# Android架构系列博文（共4篇）

## 第二篇：Lego架构方法论——分治思想的极致实践

### 前言：三次重构，一个终极问题

我经历过三次大规模 Android 架构重构，每次都像脱了一层皮：

- 从 MVC 到 MVP，团队花了两个月，造出了一套厚重的 `BaseActivity` / `BaseFragment` / `BasePresenter`。
- 从 MVP 到 MVVM，Base 类全部重写，大量与 Base 深度耦合的工具类被迫修改，业务代码牵动 40%。
- 当 MVI 开始流行时，我们彻底干不动了。

到底是什么让我们如此被动？迁移到 MVI 就一劳永逸了吗？未来再冒出新的 MV-Whatever，难道还要重演噩梦？如果非迁不可，哪些代码可以原封不动地保留？

这番痛苦逼我们追问一个根本问题：**架构的终极答案究竟是什么？**

我坐在电脑前，忽然想起小时候玩的 Lego 积木。为什么几块基础砖能搭出房子、车子、飞船？为什么 10 年前的积木，今天还能和新套装完美拼合？  
那一刻，我仿佛看到了所有架构问题的答案。

---

### 一、最小颗粒度：Lego 的复用哲学

Lego 最神奇的地方，不在那些炫酷的成品，而在那枚**2×4 基础积木**：

- 只有 8 个凸点和 3 个凹槽，没有任何预设功能。
- 遵循全球统一的连接标准，可与任何其他 Lego 积木完美拼接。
- 可用于任何模型的任何位置。
- 自 1958 年诞生以来，接口规格从未变过。

反观那些为特定模型设计的异形零件——比如千年隼的弧形舱壁——除了拼那一个模型，几乎别无他用。拆下来就成了废料。

**Lego 的核心哲学是：最小颗粒度 = 最高复用性 = 最强灵活性。**  
代码复用的道理，与它如出一辙。

---

### 二、分治法：从哲学到代码

人类应对复杂问题只有一种通用策略——**分治法**。笛卡尔在《方法谈》中写道：

> 将我考察的每一个难题，都尽可能地分成细小的部分，直到可以而且适于加以圆满解决的程度为止。

所有架构模式，本质上都是分治法在不同维度的应用：

- MVC 按职责分
- MVP 按依赖分
- MVVM 按数据流分
- Clean Architecture 按层级分
- MVI 按状态分

但它们大多止步于“分层”或“分模块”，并没有追问：**分到多细才算够？**

Lego 在物理世界给出了答案：一直分到**不可再分的基础颗粒**，这些颗粒拥有完全统一的接口。于是，68 年前的积木至今仍能和新款完美拼合。

**Lego 架构，就是把分治法推向极致：不只要把代码拆成三层、四层，而要无限拆分，直到每一块都像 Lego 基础颗粒一样——只做一件事，接口统一，没有任何多余功能。**

这就是它和其他架构最本质的区别：

> 其他架构告诉你“代码应该放在哪里”。  
> Lego 架构告诉你“代码应该拆成多小”。

而这一点，正是解决架构迁移痛苦的关键。

---

### 三、分离变化与不变：迁移痛苦的解药

为什么 MVC→MVP→MVVM 让我们痛不欲生？因为我们把大部分代码都绑在了易变的“架构框架”上。那些庞大的 `BaseActivity`、`BaseFragment`、`BaseViewModel`，就像承重墙和地基，架构一变，必须砸掉重做。

但有没有东西是可以“拎包入住”的？有的，那就是**纯粹的业务逻辑和通用工具**。它们并不关心你在用 MVP 还是 MVVM，只依赖语言和平台本身。

这正如搬家的智慧：

- **基础装修**（水电、墙漆、吊顶）带不走，新家得重做。
- **好家具**（沙发、床、电器）可以原封不动搬过去，立即使用。

在软件中，**基础装修就是 Base 类体系和框架绑定代码**；**家具就是那些高内聚、低耦合、接口稳定的功能积木**。

Lego 架构的核心工程原则由此浮现：

> **把变化的东西和不变的东西彻底分开。无限拆分，让“不变的积木”占 90%，“变化的部分”只占 10%。**

- Base 类应当瘦成一个薄壳，只提供最基础的框架接缝，不承载任何业务。
- 所有功能都以可插拔的积木形式存在，通过组合而非继承来协同。
- 架构变迁时，你只需重新“装修”薄壳，所有积木原样搬入，无缝运行。

下面四个实践，正是这条原则在不同维度上的落地。

---

### 四、Lego 架构四大落地实践

所有实践围绕同一个核心：**无限拆分，直到最小颗粒，每个颗粒职责单一、接口稳定。**

#### 实践一：工具的三层封装体系——造出你的基础颗粒

工具是 Lego 架构最底层的积木。我们将其分为三层，层层内聚，层层复用。

**1. 基础工具：通用原子积木**

- **标准**：业务无关、项目无关，可直接开源。
- **粒度**：原子级，一个方法只做一件事。
- **示例**：
  ```kotlin
  fun Activity?.isSafeForUi(): Boolean {
      return this != null && !isFinishing && !isDestroyed
  }
  ```
  类似 `SPUtils.put(key, value)`、`NetworkUtils.isConnected()`，每一个都是极小的功能点，像 2×4 积木一样，可在任何项目中任意位置使用。**一生只写一次，写好它。**

**2. 高级工具：基础工具的组合**

- **标准**：仍保持业务无关，由多个基础工具组合而成，解决一类通用问题。
- **示例**：`LoginRouter` 组合了生命周期感知、登录状态观察和路由跳转，任何需要“登录拦截”的页面都能直接复用，不包含特定业务。
- **关键**：它们仍保持纯粹，不会因为某个产品的特殊需求而变形。一旦验证稳定，可沉淀为基础工具库的一部分。

**3. 业务工具：场景化组合**

- **特点**：针对特定业务流程，将基础/高级工具与业务 API 串联。
- **原则**：保持最小特殊性。每当业务工具膨胀，就要提炼共性部分，下沉到高级或基础工具中，让业务层始终纤薄。
- **示例**：
  ```kotlin
  suspend fun login(username: String, password: String): Result<User> {
      if (!NetworkUtils.isConnected()) return Result.failure(NetworkException())
      // 组合 LoadingUtils、ApiService、UserSPUtils、Toast 等积木
  }
  ```

三层递进，形成了持续沉淀的“积木工厂”。每一次业务开发，都在为未来积累更多标准颗粒。

#### 实践二：ViewModel 按职责拆分——拒绝“上帝对象”

当一个页面的 ViewModel 超过 2000 行，它就成了难以维护的“上帝 ViewModel”。  
**Lego 方案：一个页面可有多个 ViewModel，每个聚焦一个独立业务域，就像微服务拆解单体。**

- **拆分原则**：按业务职责，而非 UI 区块；每个 ViewModel 控制在 500 行以内。
- **示例**：
  ```kotlin
  class HomeBannerViewModel : ViewModel()       // 只负责轮播图
  class HomeRecommendViewModel : ViewModel()    // 只负责推荐列表
  class HomeCategoryViewModel : ViewModel()     // 只负责分类
  ```
  在 Activity 中组合使用：
  ```kotlin
  private val bannerVM: HomeBannerViewModel by viewModels()
  private val recommendVM: HomeRecommendViewModel by viewModels()
  ```
**收益**：每个 ViewModel 短小精悍，可独立测试、独立复用。即便切换架构（如从 MVVM 到 MVI），只需调整各 ViewModel 与视图的通信方式，其内部逻辑积木无需改动。

#### 实践三：Intent 按业务域分组——告别 Intent 爆炸

MVI 中，单个页面轻易产生 200+ 个 Intent，所有意图塞在一个密封类里，臃肿不堪。  
**Lego 方案：将 Intent 与 ViewModel 对齐，按业务域拆分为多个小组，每组由对应 ViewModel 独享。**

- **反例**：一个 `HomeIntent` 密封类承载全部意图。
- **正例**：
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
  每个 ViewModel 只处理自己的 Intent 组，逻辑内聚，互不干扰。

**收益**：意图清晰可控，新增业务域只需增加对应的 Intent 组和 ViewModel，不触碰现有代码。架构迁移时，Intent 定义和 ViewModel 可以整体搬移。

#### 实践四：业务逻辑下沉到 UseCase——与框架彻底解耦

把业务逻辑直接写在 ViewModel 里，相当于把珍贵家具钉在墙上。一旦需要搬到另一个“房间”（架构），就得连墙皮一起揭下来。  
**Lego 方案：所有业务逻辑封装为 UseCase，ViewModel 只负责协调 UseCase 和 UI 状态。**

- **示例**：
  ```kotlin
  class LoginUseCase(private val userRepo: UserRepository) {
      suspend operator fun invoke(username: String, password: String): Result<User> {
          if (username.isEmpty()) return Result.failure(…)
          val user = userRepo.login(username, password)
          return Result.success(user)
      }
  }
  ```
  ViewModel 变得极其轻薄：
  ```kotlin
  fun login(username: String, password: String) {
      viewModelScope.launch {
          _state.value = Loading
          _state.value = when (val r = loginUseCase(username, password)) {
              is Success -> LoginState.Success(r.data)
              is Failure -> LoginState.Error(r.exception.message)
          }
      }
  }
  ```

**收益**：UseCase 是纯粹的 Kotlin/Java 代码，不依赖任何 Android 框架类，也不依赖 MVVM/MVI。无论架构怎么变，它们都可以丝毫无损地复用。单元测试也极其简单。

---

### 五、总结：Lego 架构，一套以不变应万变的思想

Lego 架构不是要取代 MVVM 或 MVI 的“新架构”，而是一种**编程思想与工程纪律**——它要求你在任何架构之上，坚持把代码拆解到最小稳定单元。

当你真正做到这一点，你会发现：

- 架构迁移不再是一场灾难，而只是换一个容器，90% 的积木原封不动。
- 代码复用不再需要刻意设计，最小颗粒天然驱动复用。
- 新人只需掌握少量积木及其组合方式，就能快速上手。
- 项目维护成本指数级下降，而可复用积木持续沉淀，反哺整个团队。

那些按照最小颗粒度切分的代码，会像夜晚的星光一样自然浮现，告诉你：“我已经是一个标准的 Lego 积木了，请把我放进基础库里吧。”

---

**下一篇预告：** 我们将用完整的电商 App 案例，展示如何从零开始，逐步将 Lego 架构落地到真实项目中。敬请期待。