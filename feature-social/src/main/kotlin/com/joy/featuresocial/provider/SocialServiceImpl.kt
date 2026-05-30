package com.joy.featuresocial.provider

import android.content.Context
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.joy.common.service.ServiceConstants
import com.joy.common.service.ISocialService
import com.joy.featuresocial.ui.SocialFragment

@Route(path = ServiceConstants.SERVICE_SOCIAL_HOME_FRAGMENT)
class SocialServiceImpl : ISocialService {

    override fun init(context: Context?) = Unit

    override fun createSocialFragment(): Fragment = SocialFragment()
}
