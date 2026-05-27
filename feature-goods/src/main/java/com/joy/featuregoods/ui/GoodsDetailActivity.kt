package com.joy.featuregoods.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.google.android.material.appbar.AppBarLayout
import com.joy.common.base.BaseActivity
import com.joy.appres.R as AppResR
import com.joy.common.extend.onClick200
import com.joy.common.extend.onClick300
import com.joy.common.router.AppRouter
import com.joy.common.router.LoginRouter
import com.joy.common.router.RouterConstants
import com.joy.common.utils.SizeUtils
import com.joy.common.utils.ToastUtils
import com.joy.common.widget.popup.QuickMenuPopup
import com.joy.featuregoods.R
import com.joy.featuregoods.databinding.ActivityGoodsDetailBinding
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailProductSectionState
import com.joy.featuregoods.ui.DetailAnchorTab
import com.joy.featuregoods.ui.GoodsImagePagerAdapter
import com.joy.featuregoods.ui.RecommendGridSpacingDecoration
import com.joy.featuregoods.viewmodel.GoodsDetailViewModel
import kotlin.math.abs

@Route(path = RouterConstants.GOODS_DETAIL)
class GoodsDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityGoodsDetailBinding
    private val viewModel: GoodsDetailViewModel by lazy {
        ViewModelProvider(this)[GoodsDetailViewModel::class.java]
    }
    private val imageAdapter = GoodsImagePagerAdapter()
    private lateinit var detailAdapter: GoodsDetailAdapter
    private var currentDetail: GoodsDetail? = null
    private var selectedWeightIndex = 0
    private var selectedFlavorIndex = 0
    private var purchaseQuantity = 1
    private var productSectionState: GoodsDetailProductSectionState? = null
    private var statusBarShowingTitle2Fill: Boolean = false
    private var pendingDetailAnchorTab: DetailAnchorTab? = null
    private var scrollToTopScreenHeightPx: Int = 0
    private var scrollToTopDistancePx: Int = 0
    private var quickMenuShowing = false
    private var lastAppBarVerticalOffset = 0

    private val statusBarTitle2FillColor: Int by lazy {
        ContextCompat.getColor(this, AppResR.color.func_gray_bg_2)
    }

    private val loginRouter: LoginRouter by lazy {
        LoginRouter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityGoodsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyEdgeToEdgeInsets(binding.root)
        setupDetailRecyclerView()
        setupScrollToTop()
        setupTopBarScroll()
        setupActions()
        setupPager()
        observeViewModel()
        loadData()
    }

    private fun loadData() {
        val spuId = intent.getStringExtra(RouterConstants.EXTRA_GOODS_SPU_ID)
            ?: intent.getStringExtra("spuId")
            .orEmpty()
            .ifBlank { "mock-salmon" }
        viewModel.load(spuId)
    }

    private fun observeViewModel() {
        viewModel.detail.observe(this) { detail ->
            currentDetail = detail
            imageAdapter.submit(detail.bannerImages)
            updateImageCount(position = 0, total = detail.bannerImages.size)
            rebuildDetailList()
        }
        viewModel.recommendProducts.observe(this) { rebuildDetailList() }
        viewModel.recommendHasMore.observe(this) { rebuildDetailList() }
        viewModel.errorOb.observe(this) { error ->
            ToastUtils.show(this, error)
        }
    }

    private fun setupDetailRecyclerView() {
        detailAdapter = GoodsDetailAdapter(this,
            object : GoodsDetailAdapter.Callbacks {
                override fun onCouponClick() {
                    ToastUtils.show(this@GoodsDetailActivity, getString(R.string.goods_action_coupon_hint))
                }

                override fun onAfterSalesClick() {
                    ToastUtils.show(this@GoodsDetailActivity, getString(R.string.goods_action_service_detail_hint))
                }

                override fun onWeightSpecSelected(index: Int) {
                    selectedWeightIndex = index
                    rebuildDetailList()
                }

                override fun onFlavorSpecSelected(index: Int) {
                    selectedFlavorIndex = index
                    rebuildDetailList()
                }

                override fun onQuantityMinus() {
                    if (purchaseQuantity <= 1) return
                    purchaseQuantity--
                    rebuildDetailList()
                }

                override fun onQuantityPlus() {
                    if (purchaseQuantity >= 99) return
                    purchaseQuantity++
                    rebuildDetailList()
                }

                override fun onRowReviewClick() {
                    ToastUtils.show(this@GoodsDetailActivity, getString(R.string.goods_action_review_hint))
                }

                override fun onEnterShopClick() {
                    ToastUtils.show(this@GoodsDetailActivity, getString(R.string.goods_action_shop))
                }

                override fun onRecommendProductClick(spuId: String) {
                    AppRouter.openGoodsDetail(this@GoodsDetailActivity, spuId)
                }
            },
        )
        val span = 2
        val gridLayoutManager = GridLayoutManager(this, span)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (detailAdapter.getItemViewType(position)) {
                    GoodsDetailAdapter.VIEW_TYPE_RECOMMEND_PRODUCT -> 1
                    else -> span
                }
            }
        }
        binding.rvDetailContent.layoutManager = gridLayoutManager
        binding.rvDetailContent.adapter = detailAdapter
        binding.rvDetailContent.addItemDecoration(RecommendGridSpacingDecoration(this, detailAdapter))
        binding.rvDetailContent.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    updateScrollToTopVisibility(dy)
                    if (dy <= 0) return
                    val lm = recyclerView.layoutManager as? GridLayoutManager ?: return
                    val lastVisible = lm.findLastVisibleItemPosition()
                    if (lastVisible >= lm.itemCount - 4) {
                        viewModel.loadMoreRecommended()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        flushPendingDetailAnchorScrollIfIdle()
                    }
                }
            },
        )
    }

    private fun setupScrollToTop() {
        scrollToTopScreenHeightPx = resources.displayMetrics.heightPixels
        binding.scrollToTopFloatView.setOnTopClickListener {
            scrollToTop()
        }
    }

    private fun updateScrollToTopVisibility(dy: Int) {
        scrollToTopDistancePx += dy
        if (scrollToTopDistancePx > scrollToTopScreenHeightPx) {
            binding.scrollToTopFloatView.show()
        } else if (scrollToTopDistancePx <= 0) {
            binding.scrollToTopFloatView.hide()
            scrollToTopDistancePx = 0
        }
    }

    private fun scrollToTop() {
        applyDetailTabSelection(DetailAnchorTab.PRODUCT)
        pendingDetailAnchorTab = null
        binding.rvDetailContent.stopScroll()
        performDetailAnchorScroll(DetailAnchorTab.PRODUCT)
        scrollToTopDistancePx = 0
        binding.scrollToTopFloatView.hide()
    }

    private fun rebuildDetailList() {
        val detail = currentDetail ?: return
        productSectionState = GoodsDetailProductSectionMapper.from(
            detail = detail,
            shipFromCity = detail.shipFromCity,
            selectedWeightIndex = selectedWeightIndex,
            selectedFlavorIndex = selectedFlavorIndex,
            quantity = purchaseQuantity,
        )
        val section = productSectionState ?: return
        val detailImageUrls = detail.detailImages.ifEmpty { detail.bannerImages }
        detailAdapter.submit(
            productSection = section,
            detail = detail,
            detailImageUrls = detailImageUrls,
            recommendProducts = viewModel.recommendProducts.value.orEmpty(),
            showListEndFooter = viewModel.recommendHasMore.value == false,
        )
    }

    private fun setupActions() {
        applyDemoBadges()

        val finishFn = { finish() }
        binding.iconTitle1Back.onClick200 { finishFn() }
        binding.iconTitle2Back.onClick200 { finishFn() }

        val shareFn = {
            ToastUtils.show(this, getString(R.string.goods_action_share_hint))
        }
        binding.iconTitle1Share.onClick200 { shareFn() }
        binding.iconTitle2Share.onClick200 { shareFn() }

        val searchFn = {
            ToastUtils.show(this, getString(R.string.goods_detail_search_hint))
        }
        binding.iconTitle1Search.onClick200 { searchFn() }
        binding.layoutTitle2Search.onClick200 { searchFn() }

        val favHint = {
            ToastUtils.show(this, getString(R.string.goods_action_favorite_off))
        }
        binding.iconTitle1Favorite.onClick200 { favHint() }
        binding.iconTitle2Favorite.onClick200 { favHint() }

        binding.flTitle1More.onClick200 { showQuickMenu() }
        binding.flTitle2More.onClick200 { showQuickMenu() }

        binding.tvTabProduct.onClick200 {
            applyDetailTabSelection(DetailAnchorTab.PRODUCT)
            scrollDetailContentTo(DetailAnchorTab.PRODUCT)
        }
        binding.tvTabReview.onClick200 {
            applyDetailTabSelection(DetailAnchorTab.REVIEW)
            scrollDetailContentTo(DetailAnchorTab.REVIEW)
        }
        binding.tvTabDetail.onClick200 {
            applyDetailTabSelection(DetailAnchorTab.DETAIL)
            scrollDetailContentTo(DetailAnchorTab.DETAIL)
        }
        binding.tvTabRecommend.onClick200 {
            applyDetailTabSelection(DetailAnchorTab.RECOMMEND)
            scrollDetailContentTo(DetailAnchorTab.RECOMMEND)
        }

        binding.layoutShop.onClick200 {
            ToastUtils.show(this, getString(R.string.goods_action_shop))
        }
        binding.layoutService.onClick200 {
            ToastUtils.show(this, getString(R.string.goods_action_service_hint))
        }
        binding.layoutCart.onClick200 {
            ToastUtils.show(this, getString(R.string.goods_action_cart_hint))
        }
        binding.btnAddCart.onClick300 {
            ToastUtils.show(this, getString(R.string.goods_action_add_cart_hint))
        }
        binding.btnBuyNow.onClick300 {
            loginRouter.runBlock {
                //do something
                //to orderConfirm
                ToastUtils.show(this,"登陆成功后跳转，比登陆拦截器更加灵活")
                AppRouter.openGoodsDetail(this,"")
            }
        }
    }

    private fun applyDemoBadges() {
        binding.tvMoreBadge.setCount(23)
        binding.tvCartBadge.setCount(8)
    }

    private fun setupPager() {
        binding.vpGallery.adapter = imageAdapter
        binding.vpGallery.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateImageCount(position, imageAdapter.itemCount)
                }
            },
        )
    }

    private fun setupTopBarScroll() {
        binding.appBarGoodsDetail.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                lastAppBarVerticalOffset = verticalOffset
                val range = binding.appBarGoodsDetail.totalScrollRange
                if (range <= 0) return@OnOffsetChangedListener
                val hideHeight = SizeUtils.getDimen(this, AppResR.dimen.dp_160)
                val blendDistance = SizeUtils.getDimen(this, AppResR.dimen.dp_72)
                val absOffset = abs(verticalOffset).coerceIn(0, range)
                val progress = when {
                    absOffset <= hideHeight -> 0f
                    absOffset >= hideHeight + blendDistance -> 1f
                    else -> (absOffset - hideHeight).toFloat() / blendDistance
                }
                binding.flTitle1.alpha = 1f - progress
                binding.llTitle2.alpha = progress
                binding.flTitle1.isClickable = binding.flTitle1.alpha > 0.04f
                binding.llTitle2.isClickable = binding.llTitle2.alpha > 0.04f
                if (!quickMenuShowing) {
                    applyStatusBarForTitleProgress(progress > 0.45f)
                }
            },
        )
    }

    private fun applyStatusBarForTitleProgress(title2Dominant: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            title2Dominant
        applyStatusBarFillForTitleMode(title2Dominant)
    }

    private fun refreshStatusBarFromAppBar() {
        val range = binding.appBarGoodsDetail.totalScrollRange
        if (range <= 0) {
            applyStatusBarForTitleProgress(title2Dominant = false)
            return
        }
        val hideHeight = SizeUtils.getDimen(this, AppResR.dimen.dp_160)
        val blendDistance = SizeUtils.getDimen(this, AppResR.dimen.dp_72)
        val absOffset = abs(lastAppBarVerticalOffset).coerceIn(0, range)
        val progress = when {
            absOffset <= hideHeight -> 0f
            absOffset >= hideHeight + blendDistance -> 1f
            else -> (absOffset - hideHeight).toFloat() / blendDistance
        }
        applyStatusBarForTitleProgress(progress > 0.45f)
    }

    private fun showQuickMenu() {
        QuickMenuPopup(
            activity = this,
            onShowingChanged = { showing ->
                quickMenuShowing = showing
                if (!showing) {
                    binding.appBarGoodsDetail.post { refreshStatusBarFromAppBar() }
                }
            },
        ).show()
    }

    private fun applyStatusBarFillForTitleMode(title2Dominant: Boolean) {
        if (title2Dominant == statusBarShowingTitle2Fill) return
        statusBarShowingTitle2Fill = title2Dominant
        setWindowStatusBarColor(if (title2Dominant) statusBarTitle2FillColor else Color.TRANSPARENT)
    }

    @Suppress("DEPRECATION")
    private fun setWindowStatusBarColor(color: Int) {
        window.statusBarColor = color
    }

    private fun applyDetailTabSelection(tab: DetailAnchorTab) {
        val selectedColor = ContextCompat.getColor(this, AppResR.color.red_4)
        val normalColor = ContextCompat.getColor(this, AppResR.color.func_black_text_1)
        binding.tvTabProduct.setTextColor(
            if (tab == DetailAnchorTab.PRODUCT) selectedColor else normalColor,
        )
        binding.tvTabProduct.setTypeface(
            null,
            if (tab == DetailAnchorTab.PRODUCT) Typeface.BOLD else Typeface.NORMAL,
        )
        binding.tvTabReview.setTextColor(
            if (tab == DetailAnchorTab.REVIEW) selectedColor else normalColor,
        )
        binding.tvTabReview.setTypeface(
            null,
            if (tab == DetailAnchorTab.REVIEW) Typeface.BOLD else Typeface.NORMAL,
        )
        binding.tvTabDetail.setTextColor(
            if (tab == DetailAnchorTab.DETAIL) selectedColor else normalColor,
        )
        binding.tvTabDetail.setTypeface(
            null,
            if (tab == DetailAnchorTab.DETAIL) Typeface.BOLD else Typeface.NORMAL,
        )
        binding.tvTabRecommend.setTextColor(
            if (tab == DetailAnchorTab.RECOMMEND) selectedColor else normalColor,
        )
        binding.tvTabRecommend.setTypeface(
            null,
            if (tab == DetailAnchorTab.RECOMMEND) Typeface.BOLD else Typeface.NORMAL,
        )
    }

    private fun scrollDetailContentTo(tab: DetailAnchorTab) {
        binding.rvDetailContent.stopScroll()
        pendingDetailAnchorTab = tab
        binding.rvDetailContent.post { flushPendingDetailAnchorScrollIfIdle() }
    }

    private fun flushPendingDetailAnchorScrollIfIdle() {
        val tab = pendingDetailAnchorTab ?: return
        if (binding.rvDetailContent.scrollState != RecyclerView.SCROLL_STATE_IDLE) return
        pendingDetailAnchorTab = null
        performDetailAnchorScroll(tab)
    }

    private fun performDetailAnchorScroll(tab: DetailAnchorTab) {
        val layoutManager = binding.rvDetailContent.layoutManager as? GridLayoutManager ?: return
        if (tab == DetailAnchorTab.PRODUCT) {
            binding.appBarGoodsDetail.setExpanded(true, true)
            binding.rvDetailContent.post {
                layoutManager.scrollToPositionWithOffset(0, 0)
            }
            return
        }
        binding.appBarGoodsDetail.setExpanded(false, false)
        val anchorTab = when (tab) {
            DetailAnchorTab.REVIEW -> GoodsDetailAdapter.AnchorTab.REVIEW
            DetailAnchorTab.DETAIL -> GoodsDetailAdapter.AnchorTab.DETAIL
            DetailAnchorTab.RECOMMEND -> GoodsDetailAdapter.AnchorTab.RECOMMEND
            DetailAnchorTab.PRODUCT -> GoodsDetailAdapter.AnchorTab.PRODUCT
        }
        val position = detailAdapter.anchorPosition(anchorTab)
        val headerOffsetPx = detailAnchorHeaderOffsetPx()
        binding.rvDetailContent.post {
            layoutManager.scrollToPositionWithOffset(position, headerOffsetPx)
        }
    }

    private fun detailAnchorHeaderOffsetPx(): Int {
        return binding.detailTitleOverlay.paddingTop +
            SizeUtils.getDimen(this, AppResR.dimen.dp_48) +
            SizeUtils.getDimen(this, AppResR.dimen.dp_44) +
            SizeUtils.getDimen(this, AppResR.dimen.dp_1)
    }

    private fun updateImageCount(position: Int, total: Int) {
        val safeTotal = if (total <= 0) 1 else total
        val safePosition = (position + 1).coerceAtMost(safeTotal)
        binding.tvImageCount.text = getString(R.string.goods_image_count, safePosition, safeTotal)
    }

    private fun applyMoreBadgeLayout(statusBarInsetTopPx: Int) {
        val params = binding.tvMoreBadge.layoutParams as CoordinatorLayout.LayoutParams
        params.topMargin = statusBarInsetTopPx +
            SizeUtils.getDimen(this, R.dimen.goodsDetailTitleMoreBadgeMarginTop)
        params.marginEnd = SizeUtils.getDimen(this, R.dimen.goodsDetailTitleMoreBadgeMarginEnd)
        binding.tvMoreBadge.layoutParams = params
    }

    override fun onApplyWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        binding.detailTitleOverlay.updatePadding(top = top)
        applyMoreBadgeLayout(top)
        binding.llBottomBar.updatePadding(bottom = bottom)
        setWindowStatusBarColor(Color.TRANSPARENT)
        statusBarShowingTitle2Fill = false
    }
}
