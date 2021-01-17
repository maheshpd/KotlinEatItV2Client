package com.createsapp.kotlineatitv2client.model

import com.createsapp.kotlineatitv2client.database.CartItem

class Order {
    var userId: String? = null
    var userName: String? = null
    var userPhone: String? = null
    var shippingAddress: String? = null
    var comment: String? = null
    var transactionId: String? = null
    var lat: Double = 0.toDouble()
    var lng: Double = 0.toDouble()
    var totalPayment: Double = 0.toDouble()
    var finalPayment: Double = 0.toDouble()
    var isCod: Boolean = false
    var discount: Int = 0
    var cartItemList: List<CartItem>? = null
    var createDate: Long = 0
}