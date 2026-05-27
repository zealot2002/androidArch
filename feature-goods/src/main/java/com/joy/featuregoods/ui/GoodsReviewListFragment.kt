package com.joy.featuregoods.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.joy.featuregoods.R
import com.joy.featuregoods.data.GoodsReviewListMockData
import com.joy.featuregoods.databinding.FragmentGoodsReviewListBinding
import com.joy.featuregoods.model.GoodsDetailReviewState

class GoodsReviewListFragment : Fragment() {

    private var _binding: FragmentGoodsReviewListBinding? = null
    private val binding get() = _binding!!
    private val listAdapter = GoodsReviewListAdapter()
    private var reviewState: GoodsDetailReviewState? = null

    var onCloseClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGoodsReviewListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvReviewList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviewList.adapter = listAdapter
        binding.iconReviewListClose.setOnClickListener { onCloseClick?.invoke() }
        reviewState?.let { render(it) }
    }

    fun render(state: GoodsDetailReviewState) {
        reviewState = state
        if (_binding == null) return
        binding.tvReviewListTitle.text = getString(
            R.string.goods_review_section_title_format,
            state.totalCount,
        )
        binding.tvReviewListPositiveRate.text = getString(
            R.string.goods_review_positive_rate_format,
            state.positiveRate,
        )
        GoodsReviewListAdapter.bindReviewTags(binding.flReviewListTags, state.tags)
        listAdapter.submitList(GoodsReviewListMockData.reviews())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): GoodsReviewListFragment = GoodsReviewListFragment()
    }
}
