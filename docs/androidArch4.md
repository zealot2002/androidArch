
## 第四篇：单仓库多Module的Lego架构落地实践

### 前言
在前面的三篇文章中，我们介绍了Lego架构的核心思想：
1. 架构不是最重要的，编程思想才是
2. 轻装修，重装饰，打造可插拔的工具集
3. 封装原子级的优秀工具，通过组合实现复杂功能

今天，我们来看看如何在实际项目中落地Lego架构。我会分享我目前在项目中使用的单仓库多Module架构，以及如何在这个架构中应用Lego思想。

### 一、为什么选择单仓库多Module？
现在有两种主流的项目组织方式：单仓库（Monorepo）和多仓库（Multirepo）。

我个人强烈推荐**单仓库多Module**的方式，原因如下：
- **统一的版本管理**：所有Module使用相同的版本号，避免版本冲突
- **方便的代码共享**：工具类和基础组件可以很方便地在各个业务Module之间共享
- **统一的构建配置**：所有Module使用相同的编译配置、签名配置、混淆配置
- **更好的代码导航**：在IDE中可以轻松地在各个Module之间跳转
- **更简单的CI/CD**：只需要一个CI/CD流水线就可以构建整个项目

当然，单仓库也有它的缺点，比如随着项目规模的增长，构建时间会变长。但对于绝大多数Android项目来说，单仓库多Module都是最佳选择。

### 二、项目结构划分
一个典型的Lego架构项目应该包含以下几个Module：

```
project-root/
├── app/                    # 应用外壳
├── business/               # 业务Module集合
│   ├── home/               # 首页业务
│   ├── mine/               # 我的业务
│   ├── login/              # 登录业务
│   └── ...
├── base-project/           # 项目相关的基础层
├── base-android/           # Android相关的基础工具
├── base-java/              # Java/Kotlin通用工具
└── resources/              # 公共资源
```

下面我们来详细讲解每个Module的职责。

#### 1. app：应用外壳
`app` Module是整个应用的入口，它只包含：
- `AndroidManifest.xml`
- 应用的`Application`类
- 启动页`SplashActivity`
- 主页面`MainActivity`
- 应用级的主题和样式

`app` Module不应该包含任何业务逻辑。它的唯一职责就是把各个业务Module组装起来，形成一个完整的应用。

#### 2. business：业务Module集合
`business`目录下包含了所有的业务Module。每个独立的业务模块都应该是一个单独的Module。

比如：
- `business-home`：首页相关的所有页面和逻辑
- `business-mine`：我的页面相关的所有页面和逻辑
- `business-login`：登录注册相关的所有页面和逻辑

每个业务Module都是完全独立的，它们之间不应该有直接的依赖关系。如果业务Module之间需要通信，应该通过`base-project` Module提供的接口来实现。

在每个业务Module内部，你可以自由选择使用MVVM、MVI或任何你喜欢的架构。Lego架构不限制你在业务层使用的架构模式。

#### 3. base-project：项目相关的基础层
`base-project` Module包含了与当前项目相关的所有基础代码：
- 项目的BaseActivity、BaseFragment、BaseViewModel
- 项目的全局常量和配置
- 项目的网络请求封装
- 项目的数据库封装
- 项目的业务工具类

注意，`base-project` Module只包含与当前项目相关的代码。任何可以给其他项目使用的通用代码，都不应该放在这里。

`base-project` Module应该非常薄，它只提供最基本的抽象，所有的具体功能都应该通过组合`base-android`和`base-java`中的工具来实现。

#### 4. base-android：Android相关的基础工具
`base-android` Module包含了所有与Android系统相关的通用工具：
- UI相关工具：ToastUtils、LoadingUtils、KeyboardUtils
- 系统相关工具：PermissionUtils、NetworkUtils、DeviceUtils
- 组件相关工具：ActivityUtils、FragmentUtils、ServiceUtils
- 存储相关工具：SPUtils、FileUtils、ImageUtils
- 生命周期相关工具：LifecycleUtils

这个Module中的所有工具都应该是与业务无关的，可以给任何Android项目使用。它不应该依赖任何其他业务Module，也不应该依赖`base-project` Module。

#### 5. base-java：Java/Kotlin通用工具
`base-java` Module包含了所有纯Java/Kotlin的通用工具，不依赖任何Android API：
- 字符串工具：StringUtils
- 日期工具：DateUtils
- 加密工具：EncryptUtils
- 数学工具：MathUtils
- 集合工具：CollectionUtils
- 校验工具：ValidateUtils

这个Module是最底层的Module，它不依赖任何其他Module。它可以在任何Java/Kotlin项目中使用，包括Android项目、后端项目、桌面项目等。

#### 6. resources：公共资源
`resources` Module包含了所有公共的资源文件：
- 公共的drawable、mipmap
- 公共的颜色、尺寸、字符串
- 公共的动画、布局
- 公共的字体、音频、视频

所有业务Module都依赖这个Module，这样可以避免资源重复，保证应用风格的统一。

### 三、依赖关系规则
在Lego架构中，我们有严格的依赖关系规则：
- `app` Module依赖所有的业务Module和`base-project` Module
- 业务Module依赖`base-project` Module、`base-android` Module、`base-java` Module和`resources` Module
- `base-project` Module依赖`base-android` Module、`base-java` Module和`resources` Module
- `base-android` Module依赖`base-java` Module
- `base-java` Module不依赖任何其他Module
- **业务Module之间不允许直接依赖**

这些规则保证了我们的依赖关系是单向的、清晰的、没有循环依赖的。

### 四、Lego架构在项目中的应用
在这个架构中，Lego思想体现在以下几个方面：

1. **薄Base层**：`base-project`中的Base类非常薄，它们只提供最基本的生命周期回调，不包含任何业务逻辑。所有的功能都通过组合`base-android`中的工具来实现。

2. **可插拔的工具集**：`base-android`和`base-java`中的所有工具都是可插拔的。你可以随时替换其中任何一个工具，而不会影响其他代码。

3. **原子粒度的功能**：每个工具都只做一件原子级的事情。复杂的功能通过组合简单的工具来实现。

4. **无痛架构迁移**：如果未来你想从MVVM迁移到MVI，你只需要修改`base-project`中的BaseViewModel，所有的业务代码和工具代码都不需要修改。

### 五、架构迁移的经验和避坑指南
最后，我想分享一些我在架构迁移过程中总结的经验和避坑指南：

1. **不要试图一次性重构整个项目**：架构迁移是一个渐进的过程。你可以先把工具类抽出来，然后再慢慢重构业务代码。

2. **不要过度设计Base类**：Base类越薄越好。不要把所有东西都塞到Base类里，那样会导致Base类变得臃肿不堪，难以维护。

3. **不要在工具类中引入业务逻辑**：一旦工具类中包含了业务逻辑，它就失去了通用性，变成了零分工具。

4. **重视工具的文档和测试**：工具类是整个项目的基础，它们的质量直接决定了整个项目的质量。一定要为工具类编写详细的文档和完善的单元测试。

5. **保持工具集的持续更新**：随着Android系统的更新，很多工具类也需要更新。要定期检查和更新你的工具集，确保它们兼容最新的Android版本。

### 结语