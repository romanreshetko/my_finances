package com.example.myfinances

import kotlin.math.absoluteValue

class ShareInPortfolio(val ticker: String, val board: String, var number: Int, var transactionPrice: String) {
    var price: String = ""
    var cashFlow = ((transactionPrice.toDouble() * number).absoluteValue).toString()
    var transactions = 1
}