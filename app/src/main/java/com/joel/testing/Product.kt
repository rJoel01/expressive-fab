package com.joel.testing

data class Product(
    val title: String,
    var price: Double,
    var amount: Int
) {

    fun applyDiscount(discountPercent: Int) {
        if (amount > 0 && amount <= 10) {
            price -= (price * discountPercent / 100)
        }
    }

}
