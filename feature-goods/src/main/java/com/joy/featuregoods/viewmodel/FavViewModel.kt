package com.joy.featuregoods.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joy.common.base.BaseViewModel
import com.joy.common.data.Result
import com.joy.featuregoods.data.FavRepository

class FavViewModel(
    private val repository: FavRepository = FavRepository(),
) : BaseViewModel() {

    private val _isFavorited = MutableLiveData(false)
    val isFavorited: LiveData<Boolean> = _isFavorited

    private val _favResult = MutableLiveData<Result<Boolean>>()
    val favResult: LiveData<Result<Boolean>> = _favResult

    fun loadFavoriteState(spuId: String) {
        if (spuId.isBlank()) return
        launchOnIO {
            val favorited = repository.isFavorite(spuId)
            launchOnMain {
                _isFavorited.value = favorited
            }
        }
    }

    fun setFavorite(spuId: String, favorite: Boolean) {
        if (spuId.isBlank()) return
        _favResult.value = Result.Loading
        launchOnIO {
            val success = if (favorite) {
                repository.addFavorite(spuId)
            } else {
                repository.removeFavorite(spuId)
            }
            launchOnMain {
                if (success) {
                    _isFavorited.value = favorite
                    _favResult.value = Result.Success(favorite)
                } else {
                    _favResult.value = Result.Failure(Exception("收藏操作失败"))
                }
            }
        }
    }
}
