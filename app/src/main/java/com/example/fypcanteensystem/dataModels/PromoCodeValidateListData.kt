package com.example.fypcanteensystem.dataModels

data class PromoCodeValidateListData (
    var promoCodeId: String? = null,
    var promoCodeName: String? = null,
    var promoCodeStatus: String? = null,
    var promoCodeStartDate: String? = null,
    var promoCodeEndDate: String? = null,
    var promoCodeQuantity: String? = null,
    var promoCodeMinSpend: String? = null,
    var promoCodeDiscountPrice: String? = null
)