package com.example.fypcanteensystem.model

data class OrderHistoryData (
    var orderId: String? = "",
    var orderDate: String? = "",
    var cusName: String? = "",
    var orderStatus: String? = "",
    var noOfItems: String? = "",
    var ImageUri: String? = "",
    var paymentMethod: String? = "",
    var userID: String? = "",
    var totalPrice: String? = ""
)