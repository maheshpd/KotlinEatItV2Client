package com.createsapp.kotlineatitv2client.ui.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.createsapp.kotlineatitv2client.callback.ICategoryCallbackListener
import com.createsapp.kotlineatitv2client.common.Common
import com.createsapp.kotlineatitv2client.model.BestDealModel
import com.createsapp.kotlineatitv2client.model.CategoryModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuViewModel : ViewModel(), ICategoryCallbackListener {

    private var categoriesListMutable: MutableLiveData<List<CategoryModel>>? = null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private val categoryCallBackListener: ICategoryCallbackListener

    init {
        categoryCallBackListener = this
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        categoriesListMutable!!.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }

    fun getCategoryList(): MutableLiveData<List<CategoryModel>> {
        if (categoriesListMutable == null) {
            categoriesListMutable = MutableLiveData()
            loadCategory()
        }

        return categoriesListMutable!!

    }

    fun getMessageError():MutableLiveData<String> {
        return messageError
    }

    private fun loadCategory() {
        val tempList = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                categoryCallBackListener
                    .onCategoryLoadFailed(p0.message!!)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (itemSnapshot in p0!!.children)
                {
                    val model = itemSnapshot.getValue<CategoryModel>(CategoryModel::class.java)
                    model!!.menu_id = itemSnapshot.key
                    tempList.add(model!!)
                }

                categoryCallBackListener.onCategoryLoadSuccess(tempList)
            }

        })
    }


}