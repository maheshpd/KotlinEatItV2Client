package com.createsapp.kotlineatitv2client.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.FoodModel


class FoodListViewModel : ViewModel() {

    private var mutableFoodModelListData: MutableLiveData<List<FoodModel>>? = null

    fun getMutableFoodModelListData(): MutableLiveData<List<FoodModel>> {
        if (mutableFoodModelListData == null)
            mutableFoodModelListData = MutableLiveData()
        mutableFoodModelListData!!.value = Common.categorySelected!!.foods
        return mutableFoodModelListData!!
    }
}