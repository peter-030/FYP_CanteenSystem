package com.example.fypcanteensystem.dataModels

data class OrderFoodListData(
    var orderFoodItemId: String? = null,
    var orderFoodId : String ?= null,
    var orderFoodImage : String ?= null,
    var orderFoodName : String ?= null,
    var orderFoodPrice : String ?= null,
    var orderFoodQty : String ?= null
)
