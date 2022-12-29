package com.example.fypcanteensystem.dataModels

data class OrderListData(
    var orderId: String? = null,
    var vendorId: String? = null,
    var vendorName: String? = null,
    var orderTotalQty: String? = null,
    var orderNote: String? = null,
    var orderStatus: String? = null,
    var orderDateTime: String? = null,
    var paymentMethod: String? = null,
    var orderTotalPrice: String? = null,
    var orderRating: String? = null
)
