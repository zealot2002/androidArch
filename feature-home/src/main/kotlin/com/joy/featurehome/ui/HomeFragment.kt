package com.joy.featurehome.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.joy.common.base.BaseFragment
import com.joy.common.extend.onClick300
import com.joy.common.router.AppRouter
import com.joy.common.router.RouterConstants
import com.joy.featurehome.R
import com.joy.featurehome.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initView(view: View) {
        binding.tvPlaceholder.setText(R.string.placeholder_home)
        binding.btnGoGoodsDetail.onClick300 {
            AppRouter.openGoodsDetail(requireContext(),MOCK_SPU_ID)
        }
    }

    override fun initData() = Unit

    override fun initObserver() = Unit

    companion object {
        private const val MOCK_SPU_ID = "mock-salmon"
    }
}
