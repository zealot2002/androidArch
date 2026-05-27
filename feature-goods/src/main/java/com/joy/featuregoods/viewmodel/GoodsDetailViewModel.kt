package com.joy.featuregoods.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joy.featuregoods.data.GoodsRepository
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail
import kotlinx.coroutines.launch

class GoodsDetailViewModel : ViewModel() {

    private val repository = GoodsRepository()

    private val _detail = MutableLiveData<GoodsDetail>()
    val detail: LiveData<GoodsDetail> = _detail

    private val _recommendProducts = MutableLiveData<List<BrowseProduct>>()
    val recommendProducts: LiveData<List<BrowseProduct>> = _recommendProducts

    private val _recommendHasMore = MutableLiveData<Boolean>()
    val recommendHasMore: LiveData<Boolean> = _recommendHasMore

    private val _errorOb = MutableLiveData<String>()
    val errorOb: LiveData<String> = _errorOb

    private var loadingMoreRecommended = false

    fun load(spuId: String) {
        viewModelScope.launch {
            try {
                val detailResponse = repository.fetchGoodsDetail(spuId)
                _detail.postValue(detailResponse)
                val recommends = repository.loadRecommended()
                _recommendProducts.postValue(recommends)
                _recommendHasMore.postValue(false)
            } catch (e: Exception) {
                _errorOb.postValue(e.message ?: "加载失败")
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
                }
            } catch (e: Exception) {
                _recommendHasMore.postValue(false)
            } finally {
                loadingMoreRecommended = false
            }
        }
    }
}
