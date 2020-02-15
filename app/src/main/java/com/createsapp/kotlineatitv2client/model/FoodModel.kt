package com.createsapp.kotlineatitv2client.model

class FoodModel {
    var name: String? = null
    var image: String? = null
    var id: String? = null
    var description: String? = null
    var price: String? = null
    var addon: List<AddonModel> = ArrayList<AddonModel>()
    var size: List<SizeModel> = ArrayList<SizeModel>()
}