package com.joy.common.service

import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter

/**
 * 跨模块 **IProvider 服务** 统一入口：通过 ARouter Provider 表按接口类型获取实现。
 *
 * - **页面跳转** 使用 [com.joy.common.router.AppRouter]。
 * - **契约接口** 定义在本包；**实现** 在各 feature 模块（`@Route`，path 见 [ServiceConstants]），app 须 implementation 对应模块。
 *
 * 勿对 IProvider 使用 `build(path).navigation()`，应使用 `navigation(YourService::class.java)`。
 */
object AppModuleService {

    fun socialHomeFragment(): ISocialService? = provider()

    fun requireSocialHomeFragment(): ISocialService =
        socialHomeFragment()
            ?: error("ISocialService 未注册，请确认 app 已依赖 :feature-social 并 Rebuild 安装")

    private inline fun <reified T : IProvider> provider(): T? {
        return ARouter.getInstance().navigation(T::class.java) as? T
    }
}
