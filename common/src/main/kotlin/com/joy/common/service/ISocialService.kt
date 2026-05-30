package com.joy.common.service

import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.template.IProvider

/**
 * 底部「社交」Tab 的 Fragment 契约；由 feature_social 实现并通过 ARouter 注册。
 * feature_home 通过 [AppModuleService.requireSocialHomeFragment] 获取，不直接依赖 social 模块。
 */
interface ISocialService : IProvider {

    fun createSocialFragment(): Fragment
}
