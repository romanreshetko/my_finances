package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortfolioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portfolio)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()

        val mainButton: ImageView = findViewById(R.id.starP)
        val add: ImageView = findViewById(R.id.imageAdd)
        val portfolioList: RecyclerView = findViewById(R.id.portfolioList)
        val portfolioStats: ImageView = findViewById(R.id.imageStats)

        val db = DBPortfolioHelper(this, null)
        val sharesInPortfolio = db.getSharesFromPortfolio()
        var updating = true

        fun updatePrice(share: ShareInPortfolio) : String {
            var price: String = ""
            try {
                val module = py.getModule("portfolioPrice")
                val res = module.callAttr("get_price_portfolio", share.ticker, share.board)
                price = res.toString()
            } catch (e: PyException) {
                e.printStackTrace()
            }
            val pos = price.indexOf('.')
            price = price.substring(0, pos + 2)
            return price
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val module = py.getModule("getCandles")
                    for (share in sharesInPortfolio) {
                        share.price = module.callAttr("last_price", share.ticker, share.board).toString()
                    }
                }
                withContext(Dispatchers.Main) {
                    portfolioList.adapter?.notifyDataSetChanged()
                }
                while (updating) {
                    withContext(Dispatchers.IO) {
                        for (i in sharesInPortfolio.indices) {
                            sharesInPortfolio[i].price = updatePrice(sharesInPortfolio[i])
                            delay(1000)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        portfolioList.adapter?.notifyDataSetChanged()
                    }
                }
            } catch (e: PyException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        portfolioList.layoutManager = LinearLayoutManager(this)
        portfolioList.adapter = PortfolioAdapter(sharesInPortfolio, this)

        mainButton.setOnClickListener {
            updating = false
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        add.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
        }

        portfolioStats.setOnClickListener {
            val intent = Intent(this, PortfolioStatistics::class.java)
            startActivity(intent)
        }
    }
}