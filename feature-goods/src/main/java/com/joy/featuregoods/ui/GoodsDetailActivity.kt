package com.joy.featuregoods.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.joy.featuregoods.R
import com.joy.featuregoods.databinding.ActivityGoodsDetailBinding
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.viewmodel.GoodsDetailViewModel
import com.joy.appres.R as AppResR

class GoodsDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoodsDetailBinding
    private val viewModel: GoodsDetailViewModel by lazy {
        ViewModelProvider(this)[GoodsDetailViewModel::class.java]
    }
    private val imageAdapter = GoodsImagePagerAdapter()
    private lateinit var detailAdapter: GoodsDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoodsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets()
        setupViewPager()
        setupRecyclerView()
        setupActions()
        observeViewModel()
        loadData()
    }

    private fun loadData() {
        val spuId = intent.getStringExtra("spuId") ?: "mock-salmon"
        viewModel.load(spuId)
    }

    private fun observeViewModel() {
        viewModel.detail.observe(this) { detail ->
            updateUI(detail)
        }
        viewModel.recommendProducts.observe(this) { recommends ->
            detailAdapter.submit(viewModel.detail.value ?: return@observe, recommends)
        }
        viewModel.errorOb.observe(this) { error ->
            android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(detail: GoodsDetail) {
        imageAdapter.submit(detail.bannerImages)
        updateImageCount(0, detail.bannerImages.size)
    }

    private fun setupViewPager() {
        binding.vpGallery.adapter = imageAdapter
        binding.vpGallery.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateImageCount(position, imageAdapter.itemCount)
                }
            }
        )
    }

    private fun setupRecyclerView() {
        detailAdapter = GoodsDetailAdapter(
            object : GoodsDetailAdapter.Callbacks {
                override fun onRecommendProductClick(spuId: String) {
                    android.widget.Toast.makeText(this@GoodsDetailActivity, "推荐商品: $spuId", android.widget.Toast.LENGTH_SHORT).show()
                }

                override fun onEnterShopClick() {
                    android.widget.Toast.makeText(this@GoodsDetailActivity, "进入店铺", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.rvDetailContent.layoutManager = LinearLayoutManager(this)
        binding.rvDetailContent.adapter = detailAdapter
    }

    private fun setupActions() {
        binding.iconTitle1Back.setOnClickListener { finish() }
        binding.iconTitle2Back.setOnClickListener { finish() }
        binding.iconTitle1Share.setOnClickListener { 
            android.widget.Toast.makeText(this, "分享", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle2Share.setOnClickListener { 
            android.widget.Toast.makeText(this, "分享", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle1Search.setOnClickListener { 
            android.widget.Toast.makeText(this, "搜索", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.layoutTitle2Search.setOnClickListener { 
            android.widget.Toast.makeText(this, "搜索", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle1Favorite.setOnClickListener { 
            android.widget.Toast.makeText(this, "收藏", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle2Favorite.setOnClickListener { 
            android.widget.Toast.makeText(this, "收藏", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle1More.setOnClickListener { 
            android.widget.Toast.makeText(this, "更多", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.iconTitle2More.setOnClickListener { 
            android.widget.Toast.makeText(this, "更多", android.widget.Toast.LENGTH_SHORT).show() 
        }

        binding.tvTabProduct.setOnClickListener { scrollToTop() }
        binding.tvTabReview.setOnClickListener { 
            android.widget.Toast.makeText(this, "评价", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.tvTabDetail.setOnClickListener { 
            android.widget.Toast.makeText(this, "详情", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.tvTabRecommend.setOnClickListener { 
            android.widget.Toast.makeText(this, "推荐", android.widget.Toast.LENGTH_SHORT).show() 
        }

        binding.layoutShop.setOnClickListener { 
            android.widget.Toast.makeText(this, "店铺", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.layoutService.setOnClickListener { 
            android.widget.Toast.makeText(this, "客服", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.layoutCart.setOnClickListener { 
            android.widget.Toast.makeText(this, "购物车", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.btnAddCart.setOnClickListener { 
            android.widget.Toast.makeText(this, "已加入购物车", android.widget.Toast.LENGTH_SHORT).show() 
        }
        binding.btnBuyNow.setOnClickListener { goToLogin() }
        binding.scrollToTopFloatView.setOnClickListener { scrollToTop() }
    }

    private fun goToLogin() {
        try {
            val clazz = Class.forName("com.joy.featurelogin.ui.LoginActivity")
            val intent = android.content.Intent(this, clazz)
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            android.widget.Toast.makeText(this, "登录页面未找到", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToTop() {
        binding.rvDetailContent.scrollToPosition(0)
    }

    private fun updateImageCount(position: Int, total: Int) {
        binding.tvImageCount.text = "图片${position + 1}/$total"
    }

    private fun applyEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.detailTitleOverlay.setPadding(0, bars.top, 0, 0)
            binding.llBottomBar.setPadding(0, 0, 0, bars.bottom)
            insets
        }
    }
}