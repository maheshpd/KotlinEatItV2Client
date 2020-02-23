package com.createsapp.kotlineatitv2client.ui.fooddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.FoodModel

class FoodDetailViewModel : ViewModel() {

    private var mutableLiveDataFood: MutableLiveData<FoodModel>? = null

    fun getMutableLiveDataFood(): MutableLiveData<FoodModel> {
        if (mutableLiveDataFood == null)
            mutableLiveDataFood = MutableLiveData()
        mutableLiveDataFood!!.value = Common.foodSelected
        return mutableLiveDataFood!!
    }

    private val _text = MutableLiveData<String>().apply {

    }
    val text: LiveData<String> = _text
}