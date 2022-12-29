package com.example.fypcanteensystem.model

data class OrderReceiveDetailsData (
    var itemName: String? = "",
    var itemPrice: String? = "",
    var itemQty: String? = "",
    var itemSubPrice: String? = "",
    var itemTotalPrice: String? = "",
    var itemDiscountPrice: String? = ""
)