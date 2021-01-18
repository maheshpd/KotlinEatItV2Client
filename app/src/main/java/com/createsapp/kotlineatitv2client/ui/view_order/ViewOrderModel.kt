package com.createsapp.kotlineatitv2client.ui.view_order

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.model.Order

class ViewOrderModel : ViewModel() {

    val mutableLiveDataOrderList: MutableLiveData<List<Order>>

    init {
        mutableLiveDataOrderList = MutableLiveData()
    }

    fun setMutableLiveDataOrderList(orderList: List<Order>) {
        mutableLiveDataOrderList.value = orderList
    }

}