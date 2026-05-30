package com.joy.featuregoods.data

import kotlinx.coroutines.delay

class FavRepository {

    private val favorites = mutableSetOf<String>()

    suspend fun isFavorite(spuId: String): Boolean {
        delay(100)
        return favorites.contains(spuId)
    }

    suspend fun addFavorite(spuId: String): Boolean {
        delay(200)
        favorites.add(spuId)
        return true
    }

    suspend fun removeFavorite(spuId: String): Boolean {
        delay(200)
        favorites.remove(spuId)
        return true
    }
}
