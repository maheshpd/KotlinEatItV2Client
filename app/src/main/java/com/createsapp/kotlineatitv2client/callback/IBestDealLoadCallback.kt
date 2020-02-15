package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.BestDealModel

interface IBestDealLoadCallback {
    fun onBestDealLoadSuccess(bestDealList:List<BestDealModel>)
    fun onBestDealLoadFailed(message:String)
}