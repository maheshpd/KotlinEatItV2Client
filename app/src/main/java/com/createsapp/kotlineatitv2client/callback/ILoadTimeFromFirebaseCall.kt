package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.Order

interface ILoadTimeFromFirebaseCallback {
    fun onLoadTimeSuccess(order: Order, estimatedTimeMs: Long)
    fun onLoadTimeFailed(message: String)
}