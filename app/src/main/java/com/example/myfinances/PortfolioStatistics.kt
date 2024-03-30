package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class PortfolioStatistics : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio_statistics)

        val arrowBack: ImageView = findViewById(R.id.arrowBackFromPortfolioStats)
        val portfolioReturn: TextView = findViewById(R.id.portfolioReturn)
        val indexReturn: TextView = findViewById(R.id.indexReturn)
        val structureData: TextView = findViewById(R.id.structureData)
        val radioCountry: RadioButton = findViewById(R.id.radioButtonCountry)
        val radioSector: RadioButton = findViewById(R.id.radioButtonSector)
        val radioShares: RadioButton = findViewById(R.id.radioButtonShares)
        var radioPressed = ""
        var db: DBPortfolioHelper
        var shares: ArrayList<ShareInPortfolio> = ArrayList()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("getCandles")

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val urlIndex = "https://investfunds.ru/indexes/216/"
                    val docIndex = Jsoup.connect(urlIndex).get()
                    val bodyIndex = docIndex.body()
                    val indexReturnData = bodyIndex.select("tr.field_scroll_0")[1].select("td")[3]
                    indexReturn.text = indexReturnData.text()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    var earned = 0.0
                    var spent = 0.0
                    db = DBPortfolioHelper(this@PortfolioStatistics, null)
                    shares = db.getSharesFromPortfolio()
                    db.close()
                    for (share in shares) {
                        val price =
                            module.callAttr("last_price", share.ticker, share.board).toString()
                        spent += share.transactionPrice.toDouble() * share.number.absoluteValue
                        earned += (price.toDouble() - share.transactionPrice.toDouble()) * share.number
                        share.price  = price
                    }
                    if (spent != 0.0) {
                        val returnText = "${String.format("%.2f", (earned / spent) * 100)}%"
                        portfolioReturn.text = returnText
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        arrowBack.setOnClickListener {
            finish()
        }

        radioSector.setOnClickListener {
            if (radioPressed == "Sector") {
                return@setOnClickListener
            }
            radioPressed = "Sector"
            lifecycleScope.launch {
                try {
                    var structureText = ""
                    withContext(Dispatchers.IO) {
                        val sectorMap: MutableMap<String, Double> = mutableMapOf()
                        var sumPrice = 0.0
                        for (share in shares) {
                            val url = "https://porti.ru/company/mfso/${share.ticker}"
                            val doc = Jsoup.connect(url).get()
                            val body = doc.body()
                            val sector = body.select("a[rel=nofollow]")[0].text()
                            if (sectorMap[sector] == null) {
                                sectorMap[sector] = share.price.toDouble()
                            } else {
                                sectorMap[sector] = sectorMap[sector]!! + share.price.toDouble()
                            }
                            sumPrice += share.price.toDouble()
                        }
                        for (sector in sectorMap) {
                            structureText += "${sector.key} - ${((sector.value / sumPrice) * 100).roundToInt()}%\n"
                        }
                    }
                    withContext(Dispatchers.Main) {
                        structureData.text = structureText
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        radioShares.setOnClickListener {
            if (radioPressed == "Shares") {
                return@setOnClickListener
            }
            radioPressed = "Shares"
            lifecycleScope.launch {
                try {
                    var structureText = ""
                    withContext(Dispatchers.IO) {
                        val sharesMap: MutableMap<String, Double> = mutableMapOf()
                        var sumPrice = 0.0
                        for (share in shares) {
                            if (sharesMap[share.ticker] == null) {
                                sharesMap[share.ticker] = share.price.toDouble()
                            } else {
                                sharesMap[share.ticker] = sharesMap[share.ticker]!! + share.price.toDouble()
                            }
                            sumPrice += share.price.toDouble()
                        }
                        for (share in sharesMap) {
                            structureText += "${share.key} - ${((share.value / sumPrice) * 100).roundToInt()}%\n"
                        }
                    }
                    withContext(Dispatchers.Main) {
                        structureData.text = structureText
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        radioCountry.setOnClickListener {
            if (shares.isNotEmpty()) {
                val text = "Россия - 100%"
                structureData.text = text
            }
            radioPressed = "Country"
        }
    }
}