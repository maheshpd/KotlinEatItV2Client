package com.createsapp.kotlineatitv2client.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.callback.IBestDealLoadCallback
import com.createsapp.kotlineatitv2client.callback.IPopularLoadCallback
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.BestDealModel
import com.createsapp.kotlineatitv2client.model.PopularCategotyModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel : ViewModel(), IPopularLoadCallback, IBestDealLoadCallback {

    private var popularListMutableLiveData: MutableLiveData<List<PopularCategotyModel>>? = null
    private var bestDealListMutableLiveData: MutableLiveData<List<BestDealModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var popularLoadCallbackListener: IPopularLoadCallback
    private var bestDealCallbackListener: IBestDealLoadCallback

    val bestDealList: LiveData<List<BestDealModel>>
        get() {
            if (bestDealListMutableLiveData == null) {
                bestDealListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadBestDealList()
            }

            return bestDealListMutableLiveData!!
        }

    private fun loadBestDealList() {
        var tempList = ArrayList<BestDealModel>()
        val bestDealRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS_REF)
        bestDealRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                bestDealCallbackListener.onBestDealLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0.children) {
                    val model = itemSnapshot.getValue<BestDealModel>(BestDealModel::class.java)
                    tempList.add(model!!)
                }
                bestDealCallbackListener.onBestDealLoadSuccess(tempList)
            }

        })
    }

    val popularList: LiveData<List<PopularCategotyModel>>
        get() {
            if (popularListMutableLiveData == null) {
                popularListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadPopularList()
            }

            return popularListMutableLiveData!!
        }

    private fun loadPopularList() {
        var tempList = ArrayList<PopularCategotyModel>()
        val popularRef = FirebaseDatabase.getInstance().getReference(Common.POPULAR_REF)
        popularRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                popularLoadCallbackListener.onPopularLoadFailed(p0.message!!)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapShot in p0!!.children) {
                    val model =
                        itemSnapShot.getValue<PopularCategotyModel>(PopularCategotyModel::class.java)
                    tempList.add(model!!)
                }

                popularLoadCallbackListener.onPopularLoadSuccess(tempList)

            }

        })
    }

    init {
        popularLoadCallbackListener = this
        bestDealCallbackListener = this
    }

    override fun onPopularLoadSuccess(popularModelList: List<PopularCategotyModel>) {
        popularListMutableLiveData!!.value = popularModelList
    }

    override fun onPopularLoadFailed(message: String) {
        messageError.value = message
    }

    override fun onBestDealLoadSuccess(bestDealList: List<BestDealModel>) {
        bestDealListMutableLiveData!!.value = bestDealList
    }

    override fun onBestDealLoadFailed(message: String) {
        messageError.value = message
    }
}