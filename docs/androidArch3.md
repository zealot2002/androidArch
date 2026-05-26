
## 第三篇：Lego架构核心：如何打造原子级可插拔工具集

### 前言
在上一篇文章中，我们介绍了Lego架构的核心思想：打造一套与架构无关的、可插拔的、原子级的工具集。

但很多人会说："工具类谁不会写？我项目里也有很多Utils类啊。"

是的，几乎每个项目都有一个叫`Utils`的包，但里面的工具质量却千差万别。有些工具是真正的Lego积木，可以随意组合、随处使用；而有些工具则是挂羊头卖狗肉，不仅不能提高开发效率，反而会成为项目的技术债务。

今天，我们就来详细讲解：**什么样的工具才算是优秀的Lego积木，以及如何封装这样的工具**。

### 一、先看看什么是"零分工具"
在讲优秀工具之前，我们先看看什么是"零分工具"。如果你项目里的工具符合以下任何一条，那么它就是一个零分工具：

1. **挂羊头卖狗肉**：做了超出方法名以外的事情。比如一个叫`getUserInfo()`的方法，不仅获取了用户信息，还顺便弹了一个Toast，甚至跳转到了登录页面。
2. **业务耦合**：工具里包含了特定的业务逻辑，无法给其他项目使用。比如一个叫`ToastUtils.showError()`的方法，里面硬编码了"网络错误，请稍后重试"这样的业务文案。
3. **有状态**：工具类持有状态，不同的调用顺序会产生不同的结果。比如一个单例工具类里有一个`isShowed`的成员变量。
4. **线程不安全**：在多线程环境下调用会产生竞态条件。
5. **没有生命周期感知**：工具持有Activity的引用，导致内存泄漏。
6. **API设计复杂**：一个工具类有几十个public方法，使用者需要花很长时间才能学会怎么用。
7. **没有自毁能力**：工具注册了广播或监听器，但没有提供取消注册的方法。

**判定工具好坏只有1个标准**：你的工具能否开源？如果不能，那么它不是工具，而是垃圾。

#### 真实项目中的"零分工具"反例

让我们从真实项目中看看这些反例：

**反例1：有状态的工具类**

```java
// KongFzInitUtils.java - 持有大量静态状态
public class KongFzInitUtils {
    // 静态成员变量 - 违反"无状态"原则
    public static int mVersionCode;
    public static String mVersionName;
    public static int screenWidth;
    public static int screenHeight;
    private Application application;  // 持有Application引用
    
    public void init(Context context, Application application) {
        this.application = application;
        mVersionCode = Util.getVersionCode(context);
        mVersionName = Util.getVersionName(context);
        // ...
    }
}
```

**问题分析**：这个工具类违反了"无状态"原则，它持有多个静态成员变量来存储全局状态。这导致：
- 难以测试：无法在测试中隔离状态
- 线程不安全：多线程环境下可能产生竞态条件
- 耦合度高：所有使用这些状态的代码都依赖这个工具类

---

**反例2：业务耦合的"假工具"**

```java
// PaySuccessUtils.java - 根本不是工具类，而是测试数据容器
public interface PaySuccessUtils {
    // 硬编码的业务测试数据 - 严重违反"业务无关"原则
    String json = "{\n" +
            "    \"count\": 2,\n" +
            "    \"orderInfos\": [\n" +
            "        {\n" +
            "            \"buyerReviewed\": \"0\",\n" +
            "            \"goodsAmount\": \"0.03\",\n" +
            "            \"nickname\": \"autotest02\",\n" +
            "            \"orderId\": \"34919272\",\n" +
            "            ...\n" +
            "        }\n" +
            "    ]\n" +
            "}";
}
```

**问题分析**：这个类名为"Utils"，但实际上只是一个存放测试数据的接口。它：
- 命名误导：让开发者以为这是一个工具类
- 业务耦合：硬编码了业务数据，无法复用
- 设计错误：接口不应该用于存储数据

---

**反例3：过于臃肿的工具类**

```java
// SPUtils.java - 超过970行的巨型工具类
public class SPUtils {
    // 多个SP文件名常量
    public static String app = "app";
    public static String name = "Kongfz";
    private static final String user_data_name = "user_data_name";
    private static final String ship_area = "Kongfz_SHIP_AREA";
    // ... 更多常量
    
    // 几十种get/set方法
    public static void put(Context context, String key, Object value) { ... }
    public static String getString(Context context, String key) { ... }
    public static void saveUserInfo(Context context, UserInfo member) { ... }
    public static void setToken(Context context, String token) { ... }
    public static void saveUserPublishData(Context context, String content) { ... }
    public static void setMessageSetting(MessageSetting ms) { ... }
    // ... 还有几十种其他方法
}
```

**问题分析**：这个工具类违反了"单一职责"和"API简洁"原则：
- 职责过多：既处理通用SP操作，又处理用户信息、消息设置、发布草稿等业务逻辑
- API复杂：使用者需要学习几十个方法
- 难以维护：970行代码，修改任何部分都可能影响其他功能

---

**反例4：功能混杂的工具类**

```java
// ToastUtils.java - 功能过于繁杂
public class ToastUtils {
    public static Toast mToast;
    public static Toast singleTost;
    public static Toast mCustomToastLayoutToast;
    
    // 普通Toast
    public static void show(Context context, String msg) { ... }
    
    // 阻塞式Toast（阻断点击事件）
    public static void showBlockToast(Activity activity, String msg) { ... }
    
    // 自定义Toast
    public static void showCustomToast(String msg, int picId, OnCustomToastDismiss listener) { ... }
    
    // 加入购物车成功的Toast
    public static Toast toast4(String text, int duration) { ... }
    
    // 成功提示Toast（绿色）
    public static void toastSuccessGreen(Context context, String text) { ... }
    
    // 调试用Toast
    public static void debug(String msg) { ... }
    public static void debugException(Exception e) { ... }
}
```

**问题分析**：这个工具类违反了"单一职责"原则：
- 功能混杂：普通Toast、阻塞Toast、自定义Toast、调试Toast混在一起
- 状态管理复杂：多个静态Toast实例需要手动管理生命周期
- 难以复用：调试功能和生产功能耦合在一起

---

**反例5：全局状态计数器**

```java
// ConstantsUtils.java - 全局状态计数器
public class ConstantsUtils {
    public static List<String> refreshTextList;
    public static String DES_FOR_PULL_TO_REFRESH = "DES_FOR_PULL_TO_REFRESH";
    private static int count = 0;  // 刷新次数 - 全局状态
    
    public static void add(Context context) {
        count++;
        MmkvUtils.getIns().putInt(DES_FOR_PULL_TO_REFRESH, count);
    }
    
    public static int getCount(Context context) {
        if (count > 0) {
            return count;
        } else {
            count = MmkvUtils.getIns().getInt(DES_FOR_PULL_TO_REFRESH, 0);
            return count;
        }
    }
}
```

**问题分析**：使用静态变量缓存状态，导致：
- 多线程环境下存在竞态条件
- 状态与MMKV不同步时会产生数据不一致
- 无法进行单元测试

---

**反例6：巨型路由工具**

```java
// RouteUtils.java - 超过1100行的路由工具
public class RouteUtils {
    public static boolean route(String url, Context context) {
        // 50+个if-else分支处理不同页面跳转
        if (toHome(uri, page, context)) { flag = true; }
        else if (toShopDetail(uri, page, context)) { flag = true; }
        else if (toAuctionDetail(uri, page, context)) { flag = true; }
        // ... 还有几十种其他跳转
    }
    
    // 50+个私有方法，每个处理一种页面跳转
    private static boolean toSocialTopicList(Uri uri, String page, Context context) { ... }
    private static boolean toSocialExpertList(Uri uri, String page, Context context) { ... }
    private static boolean toAuctionList1(Uri uri, String page, Context context) { ... }
    // ...
}
```

**问题分析**：违反"单一职责"和"开闭原则"：
- 职责过重：所有路由逻辑都在一个类中
- 修改困难：添加新页面需要修改这个巨型类
- 难以测试：无法单独测试某个页面的路由逻辑

---

**反例7：无意义的工具类**

```java
// SearchUtils.java - 只有一个方法的工具类
public class SearchUtils {
    public static void L(String str) {
        DebugLog.e("Search", "搜索热词log " + str);
    }
}
```

**问题分析**：这个工具类完全没有存在的必要：
- 功能单一：只是给日志加了个前缀
- 命名误导：叫"Utils"但功能太单一
- 增加认知负担：开发者需要学习这个类的用法

---

**反例8：线程不安全的防重复点击**

```java
// FastClickUtils.java - 全局共享时间戳
public class FastClickUtils {
    public static long lastTime = 0;  // 全局状态
    
    public static boolean isFast() {
        if ((System.currentTimeMillis() - lastTime) > 500) {
            lastTime = System.currentTimeMillis();
            return false;
        } else {
            return true;
        }
    }
}
```

**问题分析**：使用静态变量存储点击时间：
- 线程不安全：多个线程同时调用会产生竞态条件
- 全局共享：不同按钮共享同一个时间戳，可能导致误判

---

**反例9：职责混乱的工具类**

```java
// KfzTransparentActivityUtils.java - 功能不相关的方法混在一起
public class KfzTransparentActivityUtils {
    // Base64解码
    public static String decodeBase64(String base64str) { ... }
    
    // 构建推送Intent
    public static Intent buildPushIntent(Context activity, Map<String, String> getAttachData) { ... }
    
    // JSON转Map
    public static Map<String, String> getMap(String jsonString) { ... }
}
```

**问题分析**：违反"单一职责"原则：
- Base64解码、Intent构建、JSON解析混在一起
- 难以维护：修改一个功能可能影响其他不相关功能
- 难以复用：无法单独引用其中一个功能

---

**反例10：名字混乱的常量工具**

```java
// ConstUtils.java - 各种常量混在一起
public class ConstUtils {
    // 存储常量
    public static final int KB = 1024;
    public static final int MB = 1048576;
    
    // 时间常量
    public static final int SEC = 1000;
    public static final int MIN = 60000;
    
    // 正则表达式
    public static final String REGEX_MOBILE_SIMPLE = "^[1]\\d{10}$";
    public static final String REGEX_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    
    // 枚举
    public enum MemoryUnit { BYTE, KB, MB, GB }
    public enum TimeUnit { MSEC, SEC, MIN, HOUR, DAY }
}
```

**问题分析**：违反"单一职责"原则：
- 存储、时间、正则等常量混在一起
- 查找困难：需要在大量常量中找到需要的
- 难以维护：常量太多，容易冲突

---

**反例11：UI操作与工具耦合**

```java
// NoticeSpanHelper.java - 直接操作UI组件
public class NoticeSpanHelper {
    public static void setTextSpan(String content, List<FormatBean> format, 
                                   SpannableStringBuilder builder, TextView textView) {
        for (FormatBean formatBean : format) {
            // ... 设置Span
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    RouteUtils.route(formatBean.getAppUrl(), textView.getContext());
                }
                // ...
            };
            builder.setSpan(clickableSpan, indexOf, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
```

**问题分析**：工具类直接操作UI组件：
- 耦合度高：工具类依赖具体的UI组件
- 难以测试：需要创建真实的TextView才能测试
- 职责不清：格式化文本和路由跳转混在一起

---

**反例12：生命周期处理不当**

```java
// LifecycleHelper.java - 生命周期管理有缺陷
public class LifecycleHelper {
    public static void execute(LifecycleOwner lifecycleOwner, final Runnable runnable) {
        Task<Object> task = new Task<Object>() {
            @Override
            public Object doInBackground() {
                runnable.run();
                return null;
            }
        };
        
        TaskScheduler.execute(task);
        // 在UI线程添加生命周期观察者
        TaskScheduler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
                    @Override
                    public void onStateChanged(@NonNull LifecycleOwner source, 
                                               @NonNull Lifecycle.Event event) {
                        if (Lifecycle.Event.ON_DESTROY == event) {
                            TaskScheduler.cancelTask(task);
                            lifecycleOwner.getLifecycle().removeObserver(this);
                        }
                    }
                });
            }
        });
    }
}
```

**问题分析**：生命周期管理存在竞态条件：
- 任务执行和生命周期观察不同步
- 如果Activity在任务执行前销毁，观察者可能不会被正确注册
- 取消任务的时机不确定

---

**反例13：单例状态管理**

```java
// NotifyHelper.java - 单例持有全局状态
public class NotifyHelper {
    private static ViewQueue<ToastView> viewQueue;  // 空闲View队列
    private static List<ToastView> showingViews;    // 正在显示的View
    
    private static class SingletonInner {
        private static final NotifyHelper instance = new NotifyHelper();
    }
    
    public static NotifyHelper getInstance() {
        return SingletonInner.instance;
    }
    
    public void show(Activity activity, NotifyBean bean) {
        // ... 使用viewQueue和showingViews
    }
}
```

**问题分析**：单例模式管理UI状态：
- 内存泄漏风险：持有Activity引用
- 状态难以追踪：全局共享状态
- 难以测试：无法Mock单例

---

**反例14：反射滥用**

```java
// PresenterHelper.java - 使用反射管理Presenter
public class PresenterHelper {
    public static <T extends IView, P extends Presenter> P create(Class<P> klass, T view) {
        try {
            P presenter = klass.newInstance();  // 已废弃的API
            presenter.onCreate(view);
            return presenter;
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public static <T> void onDestroyed(T object, Class<?> superClass) {
        for (Class<?> klass = object.getClass();
             !superClass.equals(klass);
             klass = klass.getSuperclass()) {
            Field[] fields = klass.getDeclaredFields();
            for (Field f : fields) {
                if (!Presenter.class.isAssignableFrom(f.getType())) {
                    continue;
                }
                f.setAccessible(true);
                try {
                    Presenter p = (Presenter) f.get(object);
                    p.onDestroy();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

**问题分析**：使用反射存在诸多问题：
- 使用废弃API：`newInstance()`在Java 9+已废弃
- 性能开销：反射操作较慢
- 类型不安全：编译期无法检查类型转换
- 难以维护：反射代码可读性差

---

**反例15：过度封装的桥梁类**

```java
// UtilsBridge.java - 过度封装的桥梁类（超过600行）
class UtilsBridge {
    // 转发各种工具类的方法
    static Activity getTopActivity() {
        return UtilsActivityLifecycleImpl.INSTANCE.getTopActivity();
    }
    
    static boolean isAppRunning(@NonNull final String pkgName) {
        return AppUtils.isAppRunning(pkgName);
    }
    
    static String bytes2HexString(final byte[] bytes) {
        return ConvertUtils.bytes2HexString(bytes);
    }
    
    // ... 还有几十个转发方法
}
```

**问题分析**：过度封装没有意义：
- 只是简单转发，没有添加任何价值
- 增加调用层级，降低性能
- 增加认知负担：开发者需要学习这个额外的层级

---

**反例16：功能混杂的Web工具**

```java
// NewWebUtils.java - 超过600行的Web跳转工具
public class NewWebUtils {
    public static boolean matchNewWeb(Context context, String url) {
        // 20+个if-else分支
        if (RouteUtils.route(url, context)) { flag = true; }
        else if (matchPromotion(context, url)) { flag = true; }
        else if (matchShouShu(context, url)) { flag = true; }
        else if (matchBookItem(context, url)) { flag = true; }
        // ... 更多分支
    }
    
    // 20+个私有方法处理不同的URL模式
    private static boolean matchPromotion(Context context, String url) { ... }
    private static boolean matchShouShu(Context context, String url) { ... }
    private static boolean matchBookItem(Context context, String url) { ... }
    // ...
}
```

**问题分析**：违反"单一职责"和"开闭原则"：
- 职责过重：所有URL匹配逻辑都在一个类中
- 修改困难：添加新URL模式需要修改这个巨型类
- 难以测试：无法单独测试某个URL模式

---

**反例17：命名不当的工具类**

```java
// BadgeHelper.java - 名为Helper实为View
public class BadgeHelper extends View {
    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    private String text = "0";
    private int number;
    
    public void bindToTargetView(View target) {
        // 将自己绑定到目标View
    }
    
    public void setBadgeNumber(int number) {
        this.number = number;
        this.text = String.valueOf(number);
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制小红点
    }
}
```

**问题分析**：命名不当造成误解：
- 名为"Helper"但实际上是一个View
- 违反直觉：开发者以为是工具类，实际是UI组件
- 职责不清：既是View又是工具

---

**反例18：业务耦合的分享工具**

```java
// AuctionShareHelper.java - 完全业务耦合的分享工具
public class AuctionShareHelper {
    private static final String wxAppId = "gh_b377a4154631";  // 硬编码的业务配置
    
    public static void shareAuctionDetail(Context context, AuctionBean auctionBean, String bigImg) {
        String title = "";
        String text = "";
        
        // 根据业务逻辑构建分享内容
        if (saType == 1) {
            title = "同步拍 | LOT " + saLotNumber + " " + itemName;
            if ("unFinished".equals(offlineBidStatus)) {
                text = "拍品正在直播竞价中，立即参与~";
            } else if (BiddingStatus.NO_START.equals(biddingStatus)) {
                text = "拍品正在预展，快来看看~";
            }
            // ... 更多业务逻辑
        }
        
        // 硬编码的分享配置
        ShareCtx.Builder builder = new ShareCtx.Builder(...)
                .allThirdChannels()
                .copy()
                .wxCircleShortLinkV2(...);
        new ShareProxy(context).share(builder.build());
    }
}
```

**问题分析**：严重违反"业务无关"原则：
- 硬编码业务文案和配置
- 业务逻辑与分享逻辑耦合
- 无法复用到其他项目

---

**反例19：过度复杂的配置类**

```java
// BridgeUtil.java - 常量与方法混杂
public class BridgeUtil {
    // 大量常量
    final static String YY_OVERRIDE_SCHEMA = "yy://";
    final static String YY_RETURN_DATA = YY_OVERRIDE_SCHEMA + "return/";
    final static String YY_FETCH_QUEUE = YY_RETURN_DATA + "_fetchQueue/";
    final static String EMPTY_STR = "";
    final static String UNDERLINE_STR = "_";
    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    // ... 更多常量
    
    // 方法
    public static String parseFunctionName(String jsUrl) { ... }
    public static String getDataFromReturnUrl(String url) { ... }
    public static void webViewLoadJs(WebView view, String url) { ... }
}
```

**问题分析**：常量与方法混杂：
- 职责不清：既是常量容器又是工具类
- 查找困难：常量和方法混在一起
- 难以维护：常量太多

---

**反例20：方法命名不清晰**

```java
// LookSimilarAyUtils.java - 命名不清晰
public class LookSimilarAyUtils {
    public static CommondityInfo buildCommondityInfo(ShopAndGoodsItemBean b) {
        CommondityInfo commondityInfo = new CommondityInfo();
        commondityInfo.setImgUrl(b.listBean.getImgUrl());
        commondityInfo.setShopId(b.listBean.getShopId());
        // ...
        return commondityInfo;
    }
    
    public static CommondityInfo buildCommondityInfo(TraceGoodsItemBean b) {
        CommondityInfo commondityInfo = new CommondityInfo();
        commondityInfo.setImgUrl(b.getImgurl());
        commondityInfo.setShopId(b.getShopid());
        // ...
        return commondityInfo;
    }
}
```

**问题分析**：命名存在问题：
- 类名中的"Ay"含义不明确
- 方法重载但参数类型差异大，容易混淆
- 拼写错误："Commondity"应为"Commodity"

---

这些工具不仅不能帮助你，反而会给你带来无尽的麻烦。它们就像那些形状怪异的特殊Lego积木，只能用在特定的模型上，无法通用。

### 二、优秀工具的9个黄金标准
一个优秀的Lego工具，必须同时满足以下9个标准：

#### 1. 单一职责：工具方法只做方法名描述的事情
这是最基本也是最重要的一条。`getUserInfo()`就只能获取用户信息，不能弹Toast，不能跳页面，不能做任何其他事情。

一个方法，只做一件事，而且是不可以再切分了的原子事件。这就是Lego的精髓。

#### 2. 完全通用：可以给任何项目使用
优秀的工具应该是与业务完全无关的。它不应该知道任何关于你的项目的信息，不应该依赖任何你的业务类。

如果你把一个工具类复制到另一个完全不同的项目中，它不需要任何修改就能正常工作，那么它就是一个通用的工具。

#### 3. 无状态：工具类不持有任何状态
优秀的工具应该是无状态的。相同的输入，永远产生相同的输出。

工具类不应该有任何非final的成员变量。所有的状态都应该由调用者传入，而不是由工具自己持有。

#### 4. 线程安全：可以在任何线程安全调用
优秀的工具应该是线程安全的。如果工具内部有共享资源，必须使用适当的同步机制来保护。

如果工具只能在特定线程调用，必须在文档中明确说明。

#### 5. 指定工作线程：明确标注方法应该在哪个线程调用
这是很多人容易忽略的一点。一个优秀的工具应该明确标注每个方法应该在哪个线程调用：
- 如果方法会执行耗时操作，必须加上`@WorkerThread`注解，防止主线程调用
- 如果方法需要操作UI，必须加上`@MainThread`注解
- 如果方法会调用Binder接口，必须加上`@BinderLocked`注解，提醒使用者注意Binder等待耗时

#### 6. 生命周期感知：自动管理生命周期，避免内存泄漏
优秀的工具应该是生命周期感知的。它应该能够自动在适当的时机取消注册、释放资源，避免内存泄漏。

在Android中，我们可以使用`LifecycleObserver`来实现这一点。工具可以观察Activity或Fragment的生命周期，在`onDestroy()`时自动清理自己。

#### 7. 订阅发布模式：零耦合通知结果
优秀的工具应该使用订阅发布模式来通知结果，而不是使用回调接口。

回调接口会导致耦合，而订阅发布模式是完全解耦的。你可以使用RxJava、LiveData或者EventBus来实现这一点。

#### 8. 自毁能力：API极少，降低使用成本
优秀的工具应该有极少的API。最好的情况是，一个工具类只有一两个public方法。

使用者不需要学习复杂的API，只需要调用一个方法就能完成所有操作。工具应该自己管理内部的复杂逻辑，而不是把复杂度暴露给使用者。

#### 9. 自动清理：不需要使用者手动释放资源
优秀的工具应该能够自动清理自己。使用者不需要记得调用`cancel()`、`release()`这样的方法。

当生命周期结束时，工具应该自动释放所有资源，取消所有正在进行的操作。

### 三、原子粒度：越小的积木，复用性越好
Lego积木之所以强大，是因为它们的粒度非常小。一块2x4的基础积木，可以用在任何地方，搭建出任何东西。而那些特殊形状的积木，只能用在特定的模型上。

工具也是一样。**粒度越小，复用性就越好**。

一个优秀的工具应该只做一件原子级的事情。比如：
- 不要写一个`showLoadingAndRequestData()`的方法
- 而是写两个独立的工具：`LoadingUtils.show()`和`HttpUtils.request()`
- 然后在业务层把它们组合起来

这样，`LoadingUtils`和`HttpUtils`都可以在其他地方独立使用。而如果把它们写在一起，那么这个方法就只能用在这一个特定的场景。

### 四、工具组合：用简单的积木搭建复杂的功能
Lego架构的威力在于组合。你可以通过组合简单的原子工具，搭建出任何复杂的功能。

比如，要实现"显示加载框 -> 请求网络 -> 隐藏加载框 -> 显示结果"这样一个常见的流程，你不需要写一个新的工具类，只需要组合现有的工具：

```kotlin
lifecycleScope.launch {
    LoadingUtils.show(this@MainActivity)
    try {
        val result = HttpUtils.get<User>("https://api.example.com/user")
        ToastUtils.showSuccess("获取成功")
        tvResult.text = result.toString()
    } catch (e: Exception) {
        ToastUtils.showError(e.message ?: "未知错误")
    } finally {
        LoadingUtils.dismiss()
    }
}
```

每个工具都只做自己的事情，它们之间完全解耦。你可以随时替换其中任何一个工具，而不会影响其他工具的使用。

### 五、复杂场景处理：当薄Base+工具集不够用时

很多人会问：对于简单页面，薄Base+工具集确实很好用，每个class都很短小。但对于复杂页面、复杂业务呢？我时常看到MVI模式实现成了100+个intent，viewmodel代码行数爆炸。单靠薄Base+工具集好像不够用啊！

#### 问题本质分析

MVI模式下的intent爆炸，本质上是**业务逻辑没有被正确分解和组织**导致的。当所有业务逻辑都堆积在一个ViewModel中时，必然会导致：
- Intent数量爆炸（100+个intent）
- ViewModel代码行数爆炸
- intent命名模棱两可
- 难以维护和测试

#### 解决方案：分层+模块化拆分

**1. 状态切片（State Slicing）**

将单一的大State拆分为多个小的State Slice：

```kotlin
// 传统方式：单一大State
data class ComplexPageState(
    val userInfo: UserInfo,
    val products: List<Product>,
    val cartItems: List<CartItem>,
    val loading: Boolean,
    val error: String?,
    // ... 20+个字段
)

// 切片方式：多个小State
data class UserState(val userInfo: UserInfo)
data class ProductState(val products: List<Product>, val loading: Boolean)
data class CartState(val items: List<CartItem>)
```

**2. 模块化Intent处理**

将Intent按业务域分组：

```kotlin
sealed class ComplexPageIntent {
    sealed class UserIntent : ComplexPageIntent() {
        object LoadUser : UserIntent()
        data class UpdateProfile(val profile: Profile) : UserIntent()
    }
    
    sealed class ProductIntent : ComplexPageIntent() {
        object LoadProducts : ProductIntent()
        data class SelectProduct(val productId: String) : ProductIntent()
    }
    
    sealed class CartIntent : ComplexPageIntent() {
        object LoadCart : CartIntent()
        data class AddToCart(val product: Product) : CartIntent()
    }
}
```

**3. 引入领域层（Domain Layer）**

将复杂业务逻辑下沉到领域层，ViewModel只负责协调：

```kotlin
class ProductDomain {
    suspend fun getRecommendedProducts(userId: String): List<Product> {
        // 复杂的推荐算法、数据聚合等
    }
    
    suspend fun calculateDiscount(product: Product, user: User): BigDecimal {
        // 复杂的折扣计算逻辑
    }
}

class ComplexViewModel(
    private val productDomain: ProductDomain,
    private val cartDomain: CartDomain
) : ViewModel() {
    fun handleIntent(intent: ComplexPageIntent) {
        when (intent) {
            is ProductIntent.LoadProducts -> {
                viewModelScope.launch {
                    val products = productDomain.getRecommendedProducts(currentUser.id)
                    updateProductState(products)
                }
            }
        }
    }
}
```

**4. 事件驱动架构（EDA）**

使用事件总线解耦组件间的通信：

```kotlin
interface EventBus {
    fun publish(event: Event)
    fun <T : Event> subscribe(type: Class<T>, handler: (T) -> Unit)
}

data class ProductAddedToCartEvent(val product: Product) : Event
data class UserLoggedInEvent(val user: User) : Event
```

**5. UI组件化拆分**

将复杂页面拆分为多个独立的UI组件：

```kotlin
@Composable
fun ComplexPage() {
    Column {
        UserProfileSection()      // 用户信息组件
        ProductListSection()     // 商品列表组件  
        CartSummarySection()     // 购物车摘要组件
        QuickActionsSection()    // 快捷操作组件
    }
}
```

**6. 使用Effect模式处理副作用**

将副作用从ViewModel中分离出来：

```kotlin
sealed class Effect {
    data class NavigateToDetail(val productId: String) : Effect()
    data class ShowToast(val message: String) : Effect()
    object RefreshData : Effect()
}

class ComplexViewModel : ViewModel() {
    private val _effect = Channel<Effect>()
    val effect = _effect.receiveAsFlow()
    
    fun handleIntent(intent: Intent) {
        when (intent) {
            is SelectProduct -> {
                _effect.send(NavigateToDetail(intent.productId))
            }
        }
    }
}
```

#### 实践建议总结

| 策略 | 适用场景 | 收益 |
|------|----------|------|
| **状态切片** | State字段超过10个 | 降低复杂度，提高可测试性 |
| **Intent分组** | Intent超过20个 | 提高代码组织性，便于查找 |
| **领域层** | 业务逻辑复杂 | 解耦UI与业务，提高复用性 |
| **事件驱动** | 组件间通信复杂 | 降低耦合，提高灵活性 |
| **UI组件化** | 页面超过500行 | 提高复用性，便于并行开发 |
| **Effect模式** | 副作用较多 | 分离关注点，提高可测试性 |

**关键原则**：不要让ViewModel成为"上帝类"，而是将复杂逻辑合理地分布到各个层次和组件中。

在下一篇文章中，我们会把这些思想应用到实际的项目架构中，讲解如何搭建一个单仓库多Module的Lego架构项目。

---
Lego架构不是一种新的架构模式，它是一种编程思想和工程实践。它的核心是**把不变的东西和变化的东西分开**。

架构是会变化的，业务是会变化的，需求是会变化的。但那些基础的工具、那些通用的逻辑、那些优秀的编程思想，是永远不会变化的。

把你的精力投入到那些不变的东西上，打造一套属于你自己的原子级工具集。这样，无论未来架构如何变化，你都可以从容应对。

希望这个系列的文章能对你有所帮助。如果你有任何问题或想法，欢迎在评论区留言交流。