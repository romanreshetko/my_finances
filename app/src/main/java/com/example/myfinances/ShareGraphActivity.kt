package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareGraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_graph)

        val arrowBack: ImageView = findViewById(R.id.arrowBackFromGraph)
        val delete: Button = findViewById(R.id.deleteButtonGraph)
        val stats: Button = findViewById(R.id.statButton)
        val chart: CandleStickChart = findViewById(R.id.chart)
        val ticker: TextView = findViewById(R.id.graphTicker)
        ticker.text = intent.getStringExtra("ticker")

        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        stats.setOnClickListener {
            finish()
        }

        delete.setOnClickListener {
            val db = DBHelper(this, null)
            db.deleteShare(ticker.text as String)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val candlesStickEnrty = ArrayList<CandleEntry>()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()
        val module = py.getModule("getCandles")

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val res = module.callAttr("candles_array", ticker.text, "TQBR")
                    val list = res?.asList()
                    var i = 1
                    if (list != null) {
                        for (candle in list) {
                            val candleList = candle.asList()
                            candlesStickEnrty.add(
                                CandleEntry(
                                    i.toFloat(),
                                    candleList[0].toFloat(),
                                    candleList[1].toFloat(),
                                    candleList[2].toFloat(),
                                    candleList[3].toFloat()
                                )
                            )
                            i += 1
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    val candleDataSet = CandleDataSet(candlesStickEnrty, "Свечи")
                    candleDataSet.decreasingColor = Color.Red.toArgb()
                    candleDataSet.decreasingPaintStyle = android.graphics.Paint.Style.FILL
                    candleDataSet.increasingColor = Color.Green.toArgb()
                    candleDataSet.increasingPaintStyle = android.graphics.Paint.Style.FILL
                    candleDataSet.shadowWidth = 1f
                    candleDataSet.shadowColor = Color.Gray.toArgb()
                    candleDataSet.setDrawValues(false)

                    val candleData = CandleData(candleDataSet)
                    chart.data = candleData
                    chart.setBackgroundColor(Color.White.toArgb())
                    chart.description.text = "Дневные свечи за месяц"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}