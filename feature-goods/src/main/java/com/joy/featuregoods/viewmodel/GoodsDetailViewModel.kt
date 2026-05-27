package com.joy.featuregoods.viewmodel

import androidx.lifecycle.MutableLiveData
import com.joy.common.base.BaseViewModel
import com.joy.featuregoods.data.GoodsRepository
import com.joy.featuregoods.model.BrowseProduct
import com.joy.featuregoods.model.GoodsDetail

class GoodsDetailViewModel : BaseViewModel() {

    val detail = MutableLiveData<GoodsDetail>()
    val recommendProducts = MutableLiveData<List<BrowseProduct>>()
    val loadFinished = MutableLiveData<Unit>()
    val errorOb = MutableLiveData<String>()

    private val repository = GoodsRepository()

    fun load(spuId: String) {
        launchOnIO {
            try {
                val goodsDetail = repository.fetchGoodsDetail(spuId)
                detail.postValue(goodsDetail)
                val recommends = repository.loadRecommended()
                recommendProducts.postValue(recommends)
                loadFinished.postValue(Unit)
            } catch (e: Exception) {
                errorOb.postValue(e.message ?: "加载失败")
            }
        }
    }
}