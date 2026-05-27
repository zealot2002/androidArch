package com.joy.common.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel : ViewModel() {
    protected fun launchOnIO(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            block()
        }
    }

    protected fun launchOnMain(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            block()
        }
    }

    protected suspend fun <T> withIOContext(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            block()
        }
    }

    protected suspend fun <T> withMainContext(block: suspend () -> T): T {
        return withContext(Dispatchers.Main) {
            block()
        }
    }
}