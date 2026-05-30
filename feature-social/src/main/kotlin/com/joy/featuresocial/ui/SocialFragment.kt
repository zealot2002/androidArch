package com.joy.featuresocial.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joy.common.base.BaseFragment
import com.joy.common.extend.onClick200
import com.joy.common.router.AppRouter
import com.joy.featuresocial.R

class SocialFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_social, container, false)

    override fun initView(view: View) {
        view.findViewById<android.widget.TextView>(R.id.tvPlaceholder)
            .setText(R.string.placeholder_social)
        view.findViewById<android.widget.Button>(R.id.btnGoSocialDetail).onClick200 {
            AppRouter.openSocialDetail(requireContext())
        }
    }

    override fun initData() = Unit

    override fun initObserver() = Unit
}
