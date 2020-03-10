package com.createsapp.kotlineatitv2client.model

class FoodModel {
    var key: String?= null
    var name: String? = null
    var image: String? = null
    var id: String? = null
    var description: String? = null
    var price: String? = null
    var addon: List<AddonModel> = ArrayList<AddonModel>()
    var size: List<SizeModel> = ArrayList<SizeModel>()

    var ratingValue:Double = 0.toDouble()
    var ratingCount:Long = 0.toLong()

    var userSelectedAddon: List<AddonModel>? = null
    var userSelectedSize: SizeModel? = null
}