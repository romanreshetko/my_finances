package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class PortfolioShareStatistics : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio_share_statistics)

        val arrowBack: ImageView = findViewById(R.id.arrowBackFromPortfolioShareStats)
        val ticker: TextView = findViewById(R.id.statsTickerPortfolio)
        ticker.text = intent.getStringExtra("ticker")

        val transactionPrice: TextView = findViewById(R.id.transPrice)
        val transactionPriceText =
            transactionPrice.text.toString() + intent.getStringExtra("transactionPrice")
        transactionPrice.text = transactionPriceText

        val currentPrice: TextView = findViewById(R.id.currentPrice)
        val currentPriceText = currentPrice.text.toString() + intent.getStringExtra("currentPrice")
        currentPrice.text = currentPriceText

        val earnedDivs: TextView = findViewById(R.id.earnedDivs)


        val totalReturn: TextView = findViewById(R.id.totalReturn)
        var ret = "-"
        try {
            ret = String.format("%.2f", (intent.getStringExtra("currentPrice")
                ?.toDouble()!! - intent.getStringExtra("transactionPrice")
                ?.toDouble()!!) * intent.getIntExtra("number", 1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val totalReturnText = totalReturn.text.toString() + ret
        totalReturn.text = totalReturnText
        val earnedDivsText = earnedDivs.text.toString() + "0"
        earnedDivs.text = earnedDivsText

        val cashFlow: TextView = findViewById(R.id.cashFlow)
        val cashFlowText = cashFlow.text.toString() + intent.getStringExtra("cashFlow")
        cashFlow.text = cashFlowText

        val transactionsNumber: TextView = findViewById(R.id.transNumber)
        val transactionNumberText =
            transactionsNumber.text.toString() + intent.getIntExtra("transactions", 1)
        transactionsNumber.text = transactionNumberText


        arrowBack.setOnClickListener {
            finish()
        }
    }
}