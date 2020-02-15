package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.PopularCategotyModel

interface IPopularLoadCallback {
    fun onPopularLoadSuccess(popularModelList: List<PopularCategotyModel>)
    fun onPopularLoadFailed(message:String)
}