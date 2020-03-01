package com.createsapp.kotlineatitv2client.common

import com.createsapp.kotlineatitv2client.model.CategoryModel
import com.createsapp.kotlineatitv2client.model.FoodModel
import com.createsapp.kotlineatitv2client.model.UserModel

object Common {

    val COMMENT_REF: String = "Comments"
    var foodSelected: FoodModel? = null
    var categorySelected: CategoryModel? = null
    val CATEGORY_REF: String = "Category"
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUM_COUNT: Int = 0
    val BEST_DEALS_REF: String = "BestDeals"
    val POPULAR_REF: String = "MostPopular"
    val USER_REFERENCE = "Users"
    var currentUser: UserModel? = null
}