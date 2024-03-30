package com.example.myfinances

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.PyException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

class MainActivity : AppCompatActivity() {
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()

        fun updatePrice(share: Share) : String {
            var price: String = ""
            try {
                val module = py.getModule("script")
                val res = module.callAttr("get_price", share.ticker, share.board)
                price = res.toString()
            } catch (e: PyException) {
                e.printStackTrace()
            }
            val pos = price.indexOf('.')
            price = price.substring(0, pos + 2)
            return price
        }


        val search: ImageView = findViewById(R.id.imageSearch)
        val sharesList: RecyclerView = findViewById(R.id.sharesList)
        val portfolioButton: ImageView = findViewById(R.id.portfolioM)

        val db = DBHelper(this, null)
        val shares = db.getShares()
        var updating = true

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val module = py.getModule("getCandles")
                    for (share in shares) {
                        share.price = module.callAttr("last_price", share.ticker, share.board).toString()
                    }
                    withContext(Dispatchers.Main) {
                        sharesList.adapter?.notifyDataSetChanged()
                    }
                }
                withContext(Dispatchers.IO) {
                    val module = py.getModule("getCandles")
                    try {
                        for (share in shares) {
                            share.openPrice =
                                module.callAttr("open_price", share.ticker, share.board).toDouble()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    withContext(Dispatchers.Main) {
                        sharesList.adapter?.notifyDataSetChanged()
                    }
                }
                while (updating) {
                    withContext(Dispatchers.IO) {
                        for (i in shares.indices) {
                            shares[i].price = updatePrice(shares[i])
                            delay(1000)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        sharesList.adapter?.notifyDataSetChanged()
                    }
                }
            } catch (e: PyException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        search.setOnClickListener {
            updating = false
            val intent = Intent(this, SearchShare::class.java)
            startActivity(intent)
        }

        portfolioButton.setOnClickListener {
            updating = false
            val intent = Intent(this, PortfolioActivity::class.java)
            startActivity(intent)
        }

        sharesList.setOnClickListener {
            updating = false
        }

        sharesList.layoutManager = LinearLayoutManager(this)
        sharesList.adapter = SharesAdapter(shares, this)


    }
}