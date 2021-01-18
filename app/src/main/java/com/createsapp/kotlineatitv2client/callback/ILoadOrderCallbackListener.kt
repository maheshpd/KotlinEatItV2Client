package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.Order

interface ILoadOrderCallbackListener {
    fun onLoadOrderSuccess(orderList: List<Order>)
    fun onLoadOrderFailed(message: String)
}