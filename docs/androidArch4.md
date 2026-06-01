## Android 架构系列博文（共4篇）

## 第四篇：设计模式——Lego 架构的粘合剂

> **系列导航**：[第一篇：主流 Android 架构十年演化史](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8) | [第二篇：Lego架构——分治思想的极致实践](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5) | [第三篇：用 Lego 架构重构商品详情页](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)
>
> **项目地址**：<https://github.com/zealot2002/androidArch>

---

## 前言：积木拆好了，怎么拼才稳？

前三篇，我们走了一条清晰的路径：

- **第一篇**指出：架构只是"术"，它管得了分层，管不了拆分粒度。
- **第二篇**提出 Lego 架构：无限拆分到最小颗粒，让复用从"隐形"变成"可见"。
- **第三篇**用商品详情页实战证明：Mapper、Assembler、ListItem 这些积木，确实能把 3000 行的灾难拆成 15 个独立组件。

但有一个问题，第三篇结尾已经点到了——**拆，只是第一步；拼，才决定这套体系能不能长期运转。**

两个页面都需要"登录后才能操作"，你打算复制粘贴两套 `if (isLogin)` 判断吗？三种海报（商品、社交、店铺）共享同一套"渲染 → 截屏 → 预览 → 分享"流程，但 UI 布局各不相同——你打算在 Activity 里写三个 `when` 分支，每个分支几百行吗？

这时候，**设计模式**就登场了。它不是又一套"银弹架构"，而是 Lego 积木之间的**粘合剂**——用极小的、经过验证的连接方式，把已经拆好的颗粒稳定地组装在一起。

GoF 23 种、乃至更多演进中的模式，各自解决不同的"拼接"问题：工厂负责**创建**合适的积木，策略负责**切换**可替换的行为，适配器负责**对接**不兼容的接口，观察者负责**响应**状态变化，模版方法负责**固化**不变流程……篇幅所限，本篇不会逐一展开，而是选取 demo 项目中两个有代表性的案例——**观察者**与**模版方法**——说明设计模式在 Lego 架构中大致如何发挥作用。

---

## 一、观察者模式：LoginRouter——登录态的"广播站"

### 1.1 痛点：登录拦截散落各处

电商 App 里，大量操作都依赖登录态：收藏商品、加入购物车、立即购买、发表评论……

最粗糙的做法，是在每个点击事件里重复写：

```kotlin
if (UserManager.isLogin()) {
    doSomething()
} else {
    startActivity(Intent(this, LoginActivity::class.java))
}
```

问题在于：

- **重复**：十个按钮，十份相同的判断逻辑。
- **断裂**：用户登录成功后，原来的操作往往"丢失"了——他还要再点一次按钮。
- **耦合**：甚至有些项目把登录后的跳转直接写到了 `LoginActivity`，让 login 模块耦合了其他模块。

Lego 架构要求：**把"登录后再执行"这件事，拆成一块独立的、可复用的积木。**

### 1.2 解法：观察者模式

`LoginRouter` 由两个可观测源（`LifecycleOwner`、`LoginStateLiveData`）和一个 `pendingBlock` 组成：

1. **`LoginStateLiveData`**——订阅登录态。登录成功后发布 `true`，触发 pending 回调。
2. **`LifecycleOwner`**——订阅页面生命周期。从登录页返回或页面销毁时清空 pending，避免泄漏与误触发。
3. **`pendingBlock`**——暂存"登录后再执行"的操作。未登录时挂起，登录成功后自动 `invoke()`。

核心代码如下：

```kotlin
class LoginRouter(private var context: Context) {
    private var pendingBlock: (() -> Unit)? = null

    init {
        if (context is LifecycleOwner) {
            // 观察者：登录成功 → 执行 pending 操作
            LoginStateLiveData.observe(context as LifecycleOwner) {
                if (it) {
                    pendingBlock?.invoke()
                    pendingBlock = null
                }
            }
            // 生命周期兜底：页面销毁或从登录页返回时，清空 pending
            (context as LifecycleOwner).lifecycle.addObserver(
                LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME,
                        Lifecycle.Event.ON_DESTROY -> pendingBlock = null
                        else -> {}
                    }
                }
            )
        }
    }

    fun runBlock(block: () -> Unit) {
        if (context is LifecycleOwner) {
            if (LoginStateLiveData.value == true) {
                block.invoke()
            } else {
                pendingBlock = block
                AppRouter.openLogin(context)
            }
        } else {
            block.invoke()
        }
    }
}
```

登录页在成功后，只需更新全局状态：

```kotlin
// LoginActivity
LoginStateLiveData.value = true
finish()
```

**这就是观察者模式的完整闭环：**

```
LoginActivity 登录成功
       │
       ▼
LoginStateLiveData.value = true   ← 被观察者（Subject）发布状态
       │
       ▼
LoginRouter 收到通知              ← 观察者（Observer）
       │
       ▼
pendingBlock?.invoke()            ← 自动执行之前挂起的操作
```

### 1.3 在商品详情页中的用法

第三篇重构后的 `GoodsDetailActivity`，收藏、加购、立即购买全部通过 `LoginRouter` 一行搞定：

```kotlin
private val loginRouter: LoginRouter by lazy { LoginRouter(this) }

// 收藏
loginRouter.runBlock {
    // 注意并非页面跳转，如果未登陆则什么也不做
    favViewModel.setFavorite(currentSpuId, targetFavorite)
}

// 加入购物车
loginRouter.runBlock {
    ToastUtils.show(this, getString(R.string.goods_action_add_cart_hint))
}

// 立即购买 → 登录成功后跳转确认订单页
loginRouter.runBlock {
    AppRouter.openConfirmOrder(this)
}
```

`LoginRouter` 与路由拦截器的差别在于：拦截器通常只做统一的页面跳转，而 `runBlock` 可以 pending 任意操作——收藏、请求接口、弹 Toast，不限于开新页。后续动作仍由页面就地声明，`LoginActivity` 也不必再当中转分发站，各模块边界清晰、互不耦合。

### 1.4 缘起与边界

`LoginRouter` 并非事先设计，而是项目推进中被逼出来的。

详情页接上收藏、加购、立即购买后，同一套逻辑在多个按钮上反复出现：判登录、跳登录页、登录成功后续接原操作。拦截器只能统一拦跳转，做不了「登录后继续收藏」；把目的地写进 `LoginActivity`，login 模块又和业务缠在一起。几轮 copy-paste 之后，痛点就很清楚了——**缺一块能 pending 任意后续动作的公共积木。**

抽离、观察、抽象：`LoginRouter` 把登录门禁和 pending 续接收成一块，用 `LoginStateLiveData` 与 `LifecycleOwner` 串起时序，优雅地解决了这个痛点问题。

Lego 架构不只要求会拆，还要求会在重复里看见模式、在痛点里发现积木——`LoginRouter` 就是一次这样的提炼。

---

## 二、模版方法模式：BillActivity——固定流程，可变内容

### 2.1 痛点：三种海报，一套流程

我们在 demo 项目里新增了**海报分享**功能——用户可以把商品、社交动态、店铺首页生成一张精美海报，保存到相册或分享到微信。

三种海报的 UI 布局完全不同，但**页面级工作流完全一致**：

```
加载数据 → 选择 Render → 绑定视图 → 等待渲染就绪 → 截屏 → 预览 → 保存/分享
```

如果在 `BillActivity` 里用 `when (billCase)` 写三套渲染逻辑，Activity 会迅速膨胀；如果为每种海报各写一个 Activity，截屏、预览、保存的代码又会被复制三遍。

模版方法模式的解法：**把"不变的流程"固化在模版里，把"变化的部分"留给子类实现。**

### 2.2 架构分层：接口 → 抽象模版 → 具体实现

#### 第一步：定义积木接口

```kotlin
interface BillRender {
    fun onBindView(data: Any, listener: Listener)
    fun getBillView(): View

    interface Listener {
        fun screenReady(bgBitmap: Bitmap? = null)
    }
}
```

`BillRender` 是标准接口——所有海报 Render 积木都必须遵循这两个方法。

#### 第二步：抽象模版类固化公共骨架

```kotlin
abstract class BaseBillRender<T, B : ViewBinding>(private val context: Context) : BillRender {

    private lateinit var binding: B

    // 子类只实现这个方法——填充具体 UI
    abstract fun onRenderView(data: T, binding: B, listener: BillRender.Listener)

    init {
        // 模版方法：反射自动 inflate 对应的 ViewBinding
        val superclass = javaClass.genericSuperclass as ParameterizedType
        val bindingClass = superclass.actualTypeArguments[1] as Class<B>
        val inflate = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = inflate.invoke(null, (context as Activity).layoutInflater) as B
    }

    override fun onBindView(data: Any, listener: BillRender.Listener) {
        onRenderView(data as T, binding, listener)  // 调用子类实现
    }

    override fun getBillView(): View = binding.root
}
```

**这就是模版方法模式的核心：**

- `BaseBillRender` 定义了**算法骨架**（inflate Binding → 调用 `onRenderView` → 暴露 `getBillView`）。
- 子类只需实现 **`onRenderView` 这一个钩子**，填充各自不同的 UI 逻辑。
- 公共的 ViewBinding 初始化通过泛型 + 反射完成，子类零样板代码。

#### 第三步：具体子类只关心自己的 UI，只需要实现一个方法即可

以商品海报为例：

```kotlin
class GoodsBillRender(context: Context) :
    BaseBillRender<GoodsBillData, LayoutBillGoodsBinding>(context) {

    override fun onRenderView(
        data: GoodsBillData,
        binding: LayoutBillGoodsBinding,
        listener: BillRender.Listener,
    ) {
        binding.tvTitle.text = data.title
        binding.tvPrice.text = data.price
        // ... 填充商品专属字段

        // 等待布局绘制 + 网络图片加载全部就绪
        binding.ivCover.loadNetworkImage(data.imageUrl, onSuccess = { markReady() })
        binding.ivQrCode.loadNetworkImageCircle(data.miniProgramCodeUrl, onSuccess = { markReady() })
        // 全部就绪后回调 screenReady
    }
}
```

`SocialBillRender`、`ShopBillRender` 结构相同，各自绑定不同的布局和 Data 类型。**新增一种海报，只需新增一个 Render 子类 + 一个 Layout XML，零修改现有代码。**

#### 第四步：工厂选择具体积木

```kotlin
object BillRenderFactory {
    fun make(context: Context, case: Int): BillRender {
        return when (case) {
            RouterConstants.BILL_CASE_SOCIAL -> SocialBillRender(context)
            RouterConstants.BILL_CASE_SHOP -> ShopBillRender(context)
            else -> GoodsBillRender(context)
        }
    }
}
```

### 2.3 BillActivity：编排固定工作流

`BillActivity` 本身不包含任何具体海报的 UI 逻辑，它只负责**编排模版流程**：

```kotlin
class BillActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ... 初始化视图、分享按钮

        // 1. 工厂选择 Render 积木
        billRender = BillRenderFactory.make(this, billCase)
        binding.flBillContainer.addView(billRender.getBillView(), ...)

        // 2. 加载数据 + 绑定视图
        val billData = BillDataLoader.load(billCase, billId)
        billRender.onBindView(billData, object : BillRender.Listener {
            override fun screenReady(bgBitmap: Bitmap?) {
                captureBillSnapshot()  // 3. 渲染就绪 → 截屏
            }
        })
    }

    private fun captureBillSnapshot() {
        // 4. View → Bitmap → 预览展示
        val bitmap = BillBitmapUtils.viewToBitmap(billRender.getBillView())
        binding.ivBill.setImageBitmap(bitmap)
        // 5. 显示底部保存/分享栏
    }
}
```

整个页面的职责边界清晰：

| 组件 | 职责 | 对应模式 |
| --- | --- | --- |
| `BillDataLoader` | 按 case 加载 Mock 数据 | 简单工厂 |
| `BillRenderFactory` | 选择具体 Render 实现 | 简单工厂 |
| `BaseBillRender` | 固化 inflate + bind 骨架 | **模版方法** |
| `GoodsBillRender` 等 | 填充具体 UI + 异步就绪检测 | 模版方法的钩子 |
| `BillActivity` | 编排 加载→渲染→截屏→预览→分享 | 流程控制器 |
| `BillBitmapUtils` / `BillImageSaver` | 截屏与保存 | 独立工具积木 |

### 2.4 与第二篇的呼应：什么时候该用模版方法？

第二篇我们严厉批评了这种 Base：

```kotlin
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initCallerData()   // 强制顺序
        initView()
        initPageData()
        initObservers()
    }
    abstract fun initView()
    abstract fun initPageData()
    // ...
}
```

为什么 `BaseBillRender` 的模版方法可以接受，而 `BaseActivity` 的不行？

| | 坏的 BaseActivity 模版 | 好的 BaseBillRender 模版 |
| --- | --- | --- |
| **流程是否真的一致？** | 不同页面的初始化顺序差异很大 | 所有海报都必须 inflate → bind → render → ready |
| **约束的是顺序还是结构？** | 强制规定 `initView` 在 `initData` 之前 | 只规定"绑定数据时调用 `onRenderView`" |
| **子类自由度** | 被四个 abstract 方法绑架 | 只需实现一个 `onRenderView` |
| **是否可插拔** | 必须继承 Base 才能用 | 实现 `BillRender` 接口即可，不必继承 |

**判断标准很简单：只有当多个子类的算法骨架真正一致时，才用模版方法；如果只是在 Base 里"顺便"塞了一堆可能用也可能不用的初始化步骤，那就是枷锁，不是模版。**

---

## 三、从两个案例看粘合方式

上面两个案例，分别展示了设计模式在 Lego 架构中两种常见的"粘合"方向——**响应变化**与**固化流程**。其他模式解决的则是创建、替换、适配等不同维度的拼接问题，思路相通，此处不再展开。

| 维度 | 观察者（LoginRouter） | 模版方法（BillRender） |
| --- | --- | --- |
| **解决什么问题** | A 的状态变了，B 需要自动响应 | 多个子类共享同一算法骨架，只有部分步骤不同 |
| **连接方式** | 订阅 / 回调 | 继承抽象类 + 实现钩子方法 |
| **耦合方向** | 被观察者不知道谁在观察（单向） | 子类依赖父类骨架（单向） |
| **Lego 定位** | 跨模块的事件广播积木 | 同类 Render 的公共骨架积木 |
| **扩展方式** | 新增观察者，不改 Subject | 新增子类，不改模版骨架 |

无论哪种模式，有一个共同前提：**它们都是在 Lego 拆分之后，用来"粘合"积木的——而不是用来替代拆分本身。**

如果你还没有把登录逻辑拆成 `LoginRouter`，观察者模式救不了你——你只是在三个 Activity 里各写一套 LiveData 观察。如果你还没有把三种海报拆成独立的 Render，模版方法也救不了你——`BaseBillRender` 里会塞满 `if (case == SOCIAL)` 的分支。工厂、策略等其他模式同理——**先拆，再粘，顺序不能反。**

---

## 四、Lego 架构完整体系：四篇串联

走到这里，四篇博文构成了一条完整的脉络：

```
第一篇：问题意识
  └─ 架构只是"术"，连线数量决定复杂度

第二篇：拆分方法论
  └─ Lego 架构 = 无限拆分到最小颗粒 + 治理迭代

第三篇：拆分实战
  └─ 商品详情页：ListItem + Mapper + Assembler + ViewModel

第四篇：粘合剂
  └─ 设计模式：在拆分之上，用合适的模式把积木拼稳（本篇以观察者、模版方法为例）
```

Lego 架构的完整体系可以概括为：

- **一个公理**：分治法 + 单一职责
- **多个定理**：治理思想、工具迭代（私有→共有→远程）、复用发现
- **一套实战**：商品详情页的 15 个独立组件
- **一层粘合**：设计模式——本篇以观察者（LoginRouter）、模版方法（BillRender）为例，项目中同样可见工厂（`BillRenderFactory`）、策略、适配等模式的身影

当你真正用 Lego 思想来构建应用时，你会发现：

- **复杂的不是应用本身**，而是你没有把它拆成足够小的积木。
- **设计模式不是炫技**，而是在拆分完成后，按场景选用合适的模式，用最少的连线把积木稳稳地拼在一起。
- **好的代码不是学了一种架构写出来的**，而是具备了分治意识、治理纪律、模式直觉，以及发现积木的那双眼睛。

---

## 系列总结

四篇博文，从"架构演化史"出发，到"Lego 拆分方法论"，到"商品详情页实战"，再到"设计模式粘合剂"——我们试图回答一个贯穿始终的问题：

**怎样让 Android 代码真正变好？**

答案不在 MVVM 或 MVI 的标签里，而在每一天的编程纪律中：拆到不能再拆，粘到刚好够用，治理到可持续迭代。

愿你的代码库，像一盒 Lego——每块积木职责清晰、接口稳定、随意组合，历经多年依然能拼出新的模型。

---

**相关阅读**：
- [第一篇：主流 Android 架构十年演化史——我们到底在解决什么问题？](https://dev.to/zealot2002/zhu-liu-android-jia-gou-shi-nian-yan-hua-shi-wo-men-dao-di-zai-jie-jue-shi-yao-wen-ti-a-decade-of-android-architecture-evolution-what-problem-are-we-4pc8)
- [第二篇：Lego架构——分治思想的极致实践](https://dev.to/zealot2002/lego-jia-gou-fen-zhi-si-xiang-de-ji-zhi-shi-jian-the-lego-architecture-divide-and-conquer-taken-to-the-extreme-1cg5)
- [第三篇：用 Lego 架构重构商品详情页：从 3000 行到 15 个独立组件](https://dev.to/zealot2002/yong-lego-jia-gou-zhong-gou-shang-pin-xiang-qing-ye-cong-3000-xing-dao-15-ge-du-li-zu-jian-refactoring-a-product-detail-page-with-lego-architecture-from-2843)
