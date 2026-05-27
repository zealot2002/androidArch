package com.joy.featuregoods.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joy.featuregoods.data.GoodsRepository
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import com.joy.featuregoods.model.GoodsDetailProductSectionState
import com.joy.featuregoods.ui.GoodsDetailListItem
import com.joy.featuregoods.ui.GoodsDetailListAssembler
import com.joy.featuregoods.ui.GoodsDetailProductSectionMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoodsDetailViewModel : ViewModel() {

    private val repository = GoodsRepository()

    private val _detail = MutableLiveData<GoodsDetail>()
    val detail: LiveData<GoodsDetail> = _detail

    private val _recommendProducts = MutableLiveData<List<BrowseProduct>>()
    val recommendProducts: LiveData<List<BrowseProduct>> = _recommendProducts

    private val _recommendHasMore = MutableLiveData<Boolean>()
    val recommendHasMore: LiveData<Boolean> = _recommendHasMore

    private val _listItems = MutableLiveData<List<GoodsDetailListItem>>()
    val listItems: LiveData<List<GoodsDetailListItem>> = _listItems

    private val _detailImageUrls = MutableLiveData<List<String>>()
    val detailImageUrls: LiveData<List<String>> = _detailImageUrls

    private val _errorOb = MutableLiveData<String>()
    val errorOb: LiveData<String> = _errorOb

    private var loadingMoreRecommended = false
    private var selectedWeightIndex = 0
    private var selectedFlavorIndex = 0
    private var purchaseQuantity = 1

    fun load(spuId: String) {
        viewModelScope.launch {
            try {
                val detailResponse = repository.fetchGoodsDetail(spuId)
                _detail.value = detailResponse
                val recommends = repository.loadRecommended()
                _recommendProducts.value = recommends
                _recommendHasMore.value = false
                rebuildListItems()
            } catch (e: Exception) {
                _errorOb.value = e.message ?: "加载失败"
            }
        }
    }

    fun loadMoreRecommended() {
        if (loadingMoreRecommended || _recommendHasMore.value != true) return
        loadingMoreRecommended = true
        viewModelScope.launch {
            try {
                val more = repository.loadMoreRecommended()
                if (more.isEmpty()) {
                    _recommendHasMore.postValue(false)
                } else {
                    _recommendProducts.postValue(_recommendProducts.value.orEmpty() + more)
                    rebuildListItems()
                }
            } catch (e: Exception) {
                _recommendHasMore.postValue(false)
            } finally {
                loadingMoreRecommended = false
            }
        }
    }

    fun selectWeightSpec(index: Int) {
        selectedWeightIndex = index
        rebuildListItems()
    }

    fun selectFlavorSpec(index: Int) {
        selectedFlavorIndex = index
        rebuildListItems()
    }

    fun incrementQuantity() {
        if (purchaseQuantity >= 99) return
        purchaseQuantity++
        rebuildListItems()
    }

    fun decrementQuantity() {
        if (purchaseQuantity > 1) {
            purchaseQuantity--
            rebuildListItems()
        }
    }

    private fun rebuildListItems() {
        val detail = _detail.value ?: return
        viewModelScope.launch {
            val imageUrls = detail.detailImages.ifEmpty { detail.bannerImages }
            val items = withContext(Dispatchers.Default) {
                val productSection = GoodsDetailProductSectionMapper.from(
                    detail = detail,
                    shipFromCity = detail.shipFromCity,
                    selectedWeightIndex = selectedWeightIndex,
                    selectedFlavorIndex = selectedFlavorIndex,
                    quantity = purchaseQuantity,
                )
                GoodsDetailListAssembler.build(
                    productSection = productSection,
                    detail = detail,
                    detailImageUrls = imageUrls,
                    recommendProducts = _recommendProducts.value.orEmpty(),
                    showListEndFooter = _recommendHasMore.value == true,
                )
            }
            _listItems.value = items
            _detailImageUrls.value = imageUrls
        }
    }
}