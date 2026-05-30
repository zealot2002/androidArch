package com.joy.featurehome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joy.common.base.BaseFragment
import com.joy.featurehome.R

class SocialFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_placeholder, container, false)

    override fun initView(view: View) {
        view.findViewById<android.widget.TextView>(R.id.tvPlaceholder)
            .setText(R.string.placeholder_social)
    }

    override fun initData() = Unit

    override fun initObserver() = Unit
}
