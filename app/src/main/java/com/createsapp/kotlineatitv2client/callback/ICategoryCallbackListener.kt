package com.createsapp.kotlineatitv2client.callback

import com.createsapp.kotlineatitv2client.model.CategoryModel

interface ICategoryCallbackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message: String)
}