完全理解您的意见。“架构只是容器”确实有些极端，容易让人误以为架构本身无足轻重。实际上，架构提供了结构约束、依赖规则和数据流向，是不可或缺的骨架。Lego架构并非否定架构的价值，而是**在已有架构之上，补充一种以“最小颗粒度”为核心的分治思想**——让任何架构（MVC/MVP/MVVM/MVI）都能发挥出最大威力。

下面是调整后的版本，修改了相关措辞，突出“架构是骨架，Lego思想是填充与组织方式”的定位。

---

# 第三篇：Lego架构实战：用单一RecyclerView构建超级商品详情页

> **项目地址**：[https://github.com/zealot2002/androidArch](https://github.com/zealot2002/androidArch)
>
> 本文是《Android架构十年演化史》系列第三篇，延续前两篇的核心结论：**架构提供了结构约束与依赖规则，但真正的可维护性来源于分治思想。Lego架构不是替代架构模式，而是让任何架构都能发挥最大威力的编程思想。**

---

## 前言：商品详情页——架构的“试金石”

商品详情页是电商App公认的“架构试金石”。一个标准详情页包含15个以上完全独立的UI区块：轮播图、价格营销、规格选择、用户评价、店铺信息、图文详情、推荐商品……

在传统架构中，我们见过太多灾难：

- 一个 `Activity` 写下5000行代码，改一个价格展示却不小心搞崩了规格选择
- `ScrollView` 嵌套三四个 `RecyclerView`，滑动卡顿、内存泄漏频发
- 新增一个营销区块，要改动十几个文件的代码
- 架构从MVP迁移到MVVM再迁移到MVI，代码反而越来越乱

这正是前两篇反复强调的问题：**架构提供了优秀的设计约束，但约束本身无法阻止我们写出“上帝类”。** 无论采用MVC、MVP、MVVM还是MVI，如果不懂得拆分，最终都会得到一个臃肿难维护的页面。

今天，我们用生产环境验证过的代码演示：**如何运用Lego架构的“三层工具封装 + 最小颗粒度 + 动态组装”思想，以商品详情页为例，把复杂的UI和业务逻辑拆解成独立、可复用、可插拔的积木。** 在这个过程中，我们依然会使用MVVM作为整体架构骨架，但真正让代码保持干净的，是Lego的分治思想。

---

## 一、核心设计：一切皆列表项——最小颗粒度的极致实践

整个页面最反直觉但也最精妙的设计是：**用一个单一的 `RecyclerView` 承载所有UI元素。**

这不是炫技，而是Lego架构“无限拆分到最小颗粒”原则的必然结果：

- 把整个页面拆成一个个独立的**列表项**（积木）
- 每个列表项只负责一种UI展示，只解决一个问题
- 所有积木共享统一的接口，可以任意组合、任意排序

### 1.1 定义统一的积木接口

我们用 `sealed class` 定义所有列表项类型，这就是我们的“积木清单”。（完整代码见仓库）

```kotlin
/** 商品详情页所有列表项的统一接口 */
sealed class GoodsDetailListItem {
    // 商品基础信息积木
    data class PriceMarketing(val state: GoodsDetailProductState) : GoodsDetailListItem()
    data class SpecSelection(val state: GoodsDetailProductState) : GoodsDetailListItem()
    
    // 独立功能积木
    data class Review(val state: GoodsDetailReviewState) : GoodsDetailListItem()
    data class Shop(val state: GoodsDetailShopState) : GoodsDetailListItem()
    
    // 通用积木
    data object SectionDivider : GoodsDetailListItem()
    data class DetailImage(val url: String) : GoodsDetailListItem()
    // ... 其他10+个列表项
}
```

**为什么这比传统方式好？**

- 彻底消灭“上帝布局”：没有任何嵌套的 `ScrollView` 或 `LinearLayout`
- 每个列表项可以独立开发、独立测试、独立迭代
- 新增一个UI区块，只需新增一个子类，**无需修改任何现有代码**

---

## 二、工具层：Lego架构的基石——三层封装原则

很多人只看到了“单一 `RecyclerView`”的表象，却忽略了支撑整个架构的**底层基石**：经过精心设计的三层工具体系。

这是Lego架构最核心的实践之一：**所有工具严格遵循“通用工具 → 高级工具 → 业务工具”的三层封装原则，层层内聚，层层复用。** 没有这一层的支撑，前面的所有设计都是空中楼阁。

### 2.1 第一层：基础工具（原子积木）

位于 `com.joy.common.utils`，是整个架构最底层的原子积木。它们**业务无关、项目无关、可直接开源**，你的一生只需要写一次。

| 工具类 | 核心职责 | 典型方法 |
|--------|----------|----------|
| `ToastUtils` | 全局统一吐司 | `show()`、`showSuccess()`、`showError()` |
| `LoadingUtils` | 全局统一加载框 | `show()`、`dismiss()` |
| `SizeUtils` | 系统尺寸转换 | `dp2px()`、`px2dp()` |
| `NetworkUtils` | 网络状态检测 | `isConnected()`、`isWifi()` |
| `StatusBarUtils` | 沉浸式状态栏 | `setStatusBarColor()`、`getStatusBarHeight()` |

**设计原则**：一个方法只做一件事，没有任何业务逻辑，没有任何副作用。

### 2.2 第二层：高级工具（组合积木）

位于各业务模块的 `utils` 包下，由若干基础工具组合而成。**业务相关、项目相关，可在项目内复用。**

例如商品模块的 `GoodsPriceUtils`：

```kotlin
object GoodsPriceUtils {
    fun formatDisplayPrice(priceYuan: String): String = "¥$priceYuan"
    fun formatOriginalPrice(originalPriceYuan: String): String = "¥$originalPriceYuan"
}
```

**设计原则**：解决特定业务场景的通用问题，不包含任何页面相关的逻辑。

### 2.3 第三层：业务工具（场景积木）

位于具体页面或组件内部，由多个高级工具和基础工具组合而成。**针对特定业务场景，完成一个完整的业务流程。**

例如商品详情页的 `GoodsShareUtils`：

```kotlin
object GoodsShareUtils {
    fun shareGoods(context: Context, goods: GoodsDetail) {
        if (!NetworkUtils.isConnected()) {
            ToastUtils.showError("网络异常，请稍后再试")
            return
        }
        val shareContent = buildShareContent(goods)
        ShareUtils.showSharePanel(context, shareContent)
    }
    // ...
}
```

**设计原则**：封装完整的业务流程，让页面代码中没有零散的逻辑碎片。

---

## 三、数据分层：原始数据 → UI状态——职责单一原则

Lego架构要求**数据和UI严格分离**。我们绝不把后端返回的原始数据直接丢给UI，而是通过 `Mapper` 转换成UI专用的不可变状态。

这正是第二层“高级工具积木”的典型应用：`Mapper` 是一个纯函数，输入原始数据，输出UI状态，没有副作用，极易测试。

```kotlin
/** 商品基础信息Mapper：把后端数据转换成UI可直接使用的状态 */
object GoodsDetailProductMapper {
    fun from(detail: GoodsDetail, selectedSpecIndex: Int, quantity: Int): GoodsDetailProductState {
        val selectedSku = detail.skus[selectedSpecIndex]
        return GoodsDetailProductState(
            price = GoodsPriceUtils.formatDisplayPrice(selectedSku.priceYuan),
            originalPrice = detail.originalPriceYuan?.let(GoodsPriceUtils::formatOriginalPrice),
            selectedSpecIndex = selectedSpecIndex,
            quantity = quantity,
            hasMultipleSpecs = detail.skus.size > 1
        )
    }
}
```

**核心价值**：

- **隔离后端变化**：后端改了字段名，只需改 `Mapper`，UI层完全不受影响
- **统一数据格式**：所有UI需要的格式化逻辑都集中在高级工具中
- **彻底解耦**：UI层只依赖UI状态，不依赖任何后端数据模型

---

## 四、动态组装：Assembler——组合优于继承的完美体现

现在到了Lego架构最精髓的部分：**Assembler（组装器）**。

如果说列表项是积木，工具是零件，那么 `Assembler` 就是Lego的说明书。它告诉我们应该用哪些积木、按什么顺序、拼成什么样子。

```kotlin
/** 商品详情页列表组装器：唯一职责是根据状态组装最终的列表 */
object GoodsDetailListAssembler {
    fun build(
        productState: GoodsDetailProductState,
        detail: GoodsDetail,
        recommendProducts: List<BrowseProduct>,
        showReview: Boolean,
        showShop: Boolean
    ): List<GoodsDetailListItem> {
        val items = mutableListOf<GoodsDetailListItem>()

        items += PriceMarketing(productState)
        items += SectionDivider
        items += SpecSelection(productState)
        items += SectionDivider

        if (showReview && detail.totalReviewCount > 0) {
            items += Review(GoodsDetailReviewMapper.from(detail))
            items += SectionDivider
        }

        // ... 其他区块

        if (recommendProducts.isNotEmpty()) {
            items += RecommendTitle
            recommendProducts.forEach { items += RecommendProduct(it) }
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

这正是Lego架构和其他代码组织方式最本质的区别：**传统架构告诉你“代码应该放在哪个层级”，而Lego架构在此基础上进一步追问“代码应该拆成多小，然后怎么组合起来”。**

---

## 五、ViewModel：纯粹的状态协调者——架构骨架与积木填充

在Lego架构中，我们依然使用MVVM的 `ViewModel` 作为整体架构骨架，但其内部职责被极度简化：**它只是一个状态持有者和协调者**。

它不包含任何UI逻辑，也不包含复杂的业务逻辑——所有的业务逻辑都下沉到了 `Mapper`、`Assembler` 和 `UseCase` 这些可复用的积木中。这正是第二篇讲的 **“轻装修，重装饰”** 原则：`Base` 类和 `ViewModel` 提供稳定的结构约束，但具体的功能全部由可插拔的积木实现。

```kotlin
class GoodsDetailViewModel : ViewModel() {
    private val repository = GoodsRepository()
    private val _listItems = MutableLiveData<List<GoodsDetailListItem>>()
    val listItems: LiveData<List<GoodsDetailListItem>> = _listItems

    private var selectedSpecIndex = 0
    private var purchaseQuantity = 1

    fun load(spuId: String) {
        viewModelScope.launch {
            val detail = repository.fetchGoodsDetail(spuId)
            rebuildListItems(detail)
        }
    }

    fun selectSpec(index: Int) {
        selectedSpecIndex = index
        rebuildListItems(_detail.value!!)
    }

    private fun rebuildListItems(detail: GoodsDetail) {
        val productState = GoodsDetailProductMapper.from(detail, selectedSpecIndex, purchaseQuantity)
        val items = GoodsDetailListAssembler.build(productState, detail, emptyList(), true, true)
        _listItems.value = items
    }
}
```

**整个 `ViewModel` 不到80行代码，逻辑清晰到一眼就能看懂。** 任何新人接手，5分钟就能明白它在做什么。

对比传统架构中几千行的“上帝 `ViewModel`”——这就是Lego架构的威力：**借助架构提供的结构约束（如ViewModel、Lifecycle），我们把复杂的逻辑拆成一个个简单的积木，让架构骨架保持干净，让业务积木高度复用。**

---

## 六、Activity：薄薄的一层壳——架构与视图的胶水层

最终的 `Activity` 代码，简洁而清晰：

```kotlin
@Route(path = RouterConstants.GOODS_DETAIL)
class GoodsDetailActivity : BaseActivity() {
    private lateinit var binding: ActivityGoodsDetailBinding
    private val viewModel: GoodsDetailViewModel by viewModels()
    private val detailAdapter = GoodsDetailAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoodsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupAdapterCallbacks()
        observeViewModel()
        
        viewModel.load(intent.getStringExtra("spuId")!!)
    }

    private fun observeViewModel() {
        viewModel.listItems.observe(this) { items ->
            detailAdapter.submitList(items)
        }
    }

    private fun setupAdapterCallbacks() {
        detailAdapter.callbacks = object : GoodsDetailAdapter.Callbacks {
            override fun onSpecSelected(index: Int) = viewModel.selectSpec(index)
            override fun onQuantityPlus() = viewModel.incrementQuantity()
            override fun onReviewClicked() = showReviewListPanel()
        }
    }
}
```

整个 `Activity` 只有不到70行代码，它只负责三件事：

1. 初始化视图和 `Adapter`
2. 把用户点击事件转发给 `ViewModel`
3. 观察 `ViewModel` 的状态并更新UI

**Activity并不“只是一个容器”，它是架构与Android视图系统之间的重要胶水层。** 在Lego架构中，我们仍然尊重Activity作为导航和生命周期管理者的角色，但把具体的UI组装和业务逻辑全部委托给了积木——这样Activity永远不会膨胀。

当未来架构从MVI迁移到下一个MVXXX时，你不需要重写任何业务逻辑，只需要按照新架构的规则调整 `ViewModel` 和 `Activity` 的协作方式，而所有的工具、`Mapper`、`Assembler`、组件都可以原封不动地搬过去。这正是第二篇讲的 **“架构迁移的终极解决方案”**。

---

## 七、实战收益：数据说话

这个商品详情页已经在生产环境稳定运行两年，我们对比了重构前后的数据：

| 指标 | 传统MVP实现 | Lego架构实现 | 提升幅度 |
|------|--------------|--------------|----------|
| 核心代码行数 | 3200行（单Activity） | 1200行（15个独立组件） | **减少62%** |
| 平均开发一个新区块 | 3天 | 0.5天 | **提升600%** |
| 线上bug率 | 每月12个 | 每月2个 | **降低83%** |
| 新人上手时间 | 2周 | 1天 | **提升1400%** |
| 并行开发支持 | 最多1人同时开发 | 最多5人同时开发 | **提升500%** |

更重要的是，当我们去年把整个项目从MVVM迁移到MVI时，**商品详情页的所有业务逻辑代码一行都没有改**。我们只重写了 `ViewModel` 和 `Activity` 中与新架构适配的部分，所有的工具、`Mapper`、`Assembler`、组件都原封不动地复用了。

这就是Lego架构最核心的价值：**架构为我们提供了可靠的设计约束和依赖规则，而Lego思想则让这些约束之下的代码变得极度内聚、可拆分、可复用。两者结合，才能真正应对架构演化的挑战。**

---

## 八、总结：理想的商品详情页

很多人觉得商品详情页天生就应该是复杂的，天生就应该有几千行代码。但Lego架构告诉我们：

> **复杂的不是页面本身，而是你没有把它拆成足够小的积木。**

理想的商品详情页是什么样子？

- 页面整体遵循清晰的架构分层（如MVVM），但每一层内部都是由大量小积木组合而成
- 每一块积木职责分明、边界清晰，内部满眼可见的是**工具类和UI组件**，零散代码极少
- 大量的基础积木、组合积木、高级积木、UI组件，都是**饱经风霜、久经考验的可靠伙伴**
- 一个原本超级复杂的页面，变得**健壮、优雅、轻灵、可扩展、可测试、可维护、可读性极佳**

这正是整个系列想要传递的核心观点：**架构提供了必要的骨架和约束，但真正的长期可维护性来源于分治思想。** MVC、MVP、MVVM、MVI……这些架构都是优秀的工具，而Lego架构是让这些工具发挥最大效能的编程方法论。

无论你使用哪种架构，只要掌握了**分治思想**，懂得把代码拆成最小颗粒的积木，并且用**三层封装原则**构建你的工具体系，你就能写出干净、可维护、可扩展的代码。

---

**下一篇预告：** 我们将探讨 **“设计模式如何作为Lego架构的粘合剂”** ，让你的积木组合更加灵活、更加稳固。敬请期待！