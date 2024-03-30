package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class ShareStatisticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_statistics)

        val arrowBack: ImageView = findViewById(R.id.arrowBackFromStats)
        val delete: Button = findViewById(R.id.deleteButton)
        val graph: Button = findViewById(R.id.graphButton)
        val ticker: TextView = findViewById(R.id.statsTicker)
        ticker.text = intent.getStringExtra("ticker")

        val price: TextView = findViewById(R.id.price)
        val priceText = price.text.toString() + intent.getStringExtra("price")
        price.text = priceText

        val pe: TextView = findViewById(R.id.pe)
        val eps: TextView = findViewById((R.id.eps))
        val evEbitda: TextView = findViewById(R.id.evebitda)
        val lastDiv: TextView = findViewById(R.id.lastdiv)
        val nextDiv: TextView = findViewById(R.id.nextdiv)
        val divYield: TextView = findViewById(R.id.divyield)
        val divDate: TextView = findViewById(R.id.divdate)
        val roe: TextView = findViewById(R.id.roe)
        val beta: TextView = findViewById(R.id.beta)
        val debtEbitda: TextView = findViewById(R.id.debtebitda)
        val forecast: TextView = findViewById(R.id.forecast)


        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        graph.setOnClickListener {
            val intent = Intent(this, ShareGraphActivity::class.java)
            intent.putExtra("ticker", ticker.text)
            startActivity(intent)
        }

        delete.setOnClickListener {
            val db = DBHelper(this, null)
            db.deleteShare(ticker.text as String)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = "https://porti.ru/company/mfso/${ticker.text}"
                    val divUrl = "https://www.dohod.ru/ik/analytics/dividend/${
                        ticker.text.toString().lowercase()
                    }"
                    val urlForecast = "https://www.tinkoff.ru/invest/stocks/${ticker.text}/"
                    val doc = Jsoup.connect(url).get()
                    val body = doc.body()

                    try {
                        val epsData = body.select("td[field=eps][data-type=LTM]")
                        val epsString = eps.text.toString() + epsData.text()
                        eps.text = epsString
                    } catch (e: Exception) {
                        val epsString = eps.text.toString() + "-"
                        eps.text = epsString
                    }

                    try {
                        val roeData = body.select("td[field=roe][data-type=LTM]")
                        val roeString = roe.text.toString() + roeData.text()
                        roe.text = roeString
                    } catch (e: Exception) {
                        val roeString = roe.text.toString() + "-"
                        roe.text = roeString
                    }

                    try {
                        val peData = body.select("td[field=p_e][data-type=LTM]")
                        val peString = pe.text.toString() + peData.text()
                        pe.text = peString
                    } catch (e: Exception) {
                        val peString = pe.text.toString() + "-"
                        pe.text = peString
                    }

                    try {
                        val evEbitdaData = body.select("td[field=ev_ebitda][data-type=LTM]")
                        val evEbitdaString = evEbitda.text.toString() + evEbitdaData.text()
                        evEbitda.text = evEbitdaString
                    } catch (e: Exception) {
                        val evEbitdaString = evEbitda.text.toString() + "-"
                        evEbitda.text = evEbitdaString
                    }

                    try {
                        val betaData = body.select("td[field=beta][data-type=LTM]")
                        val betaString = beta.text.toString() + betaData.text()
                        beta.text = betaString
                    } catch (e: Exception) {
                        val betaString = beta.text.toString() + "-"
                        beta.text = betaString
                    }

                    try {
                        val netDebtEbitdaData =
                            body.select("td[field=netdebt_ebitda][data-type=LTM]")
                        val netDebtEbitdaString =
                            debtEbitda.text.toString() + netDebtEbitdaData.text()
                        debtEbitda.text = netDebtEbitdaString
                    } catch (e: Exception) {
                        val netDebtEbitdaString = debtEbitda.text.toString() + "-"
                        debtEbitda.text = netDebtEbitdaString
                    }

                    try {
                        val lastDivData = body.select("td[field=dividend][data-type=LTM]")
                        var lastDivString = lastDiv.text.toString() + lastDivData.text()
                        if (lastDivData.text() == "") {
                            lastDivString += "0"
                        }
                        lastDiv.text = lastDivString
                    } catch (e: Exception) {
                        val lastDivString = lastDiv.text.toString() + "-"
                        lastDiv.text = lastDivString
                    }

                    try {
                        val divDoc = Jsoup.connect(divUrl).get()
                        val divBody = divDoc.body()
                        val divs = divBody.select("div#leftside-col > p > strong > span.black")
                        if (divs.text() == "0") {
                            val nextDivString = nextDiv.text.toString() + "-"
                            nextDiv.text = nextDivString

                            val divYieldString = divYield.text.toString() + "-"
                            divYield.text = divYieldString

                            val divDateString = divDate.text.toString() + "-"
                            divDate.text = divDateString
                        } else {
                            val divsExtra = divBody.select("div#leftside-col > p")
                            val strongs = divsExtra.select("strong")
                            val nextDivString =
                                nextDiv.text.toString() + strongs[2].select("span.black").text()
                            nextDiv.text = nextDivString

                            val divYieldString =
                                divYield.text.toString() + strongs[3].select("span.black").text()
                            divYield.text = divYieldString

                            val divDateString = divDate.text.toString() + strongs[4].text()
                            divDate.text = divDateString
                        }
                    } catch (e: HttpStatusException) {
                        val nextDivString = nextDiv.text.toString() + "-"
                        nextDiv.text = nextDivString

                        val divYieldString = divYield.text.toString() + "-"
                        divYield.text = divYieldString

                        val divDateString = divDate.text.toString() + "-"
                        divDate.text = divDateString
                    }

                    try {
                        val forDoc = Jsoup.connect(urlForecast).get()
                        val forBody = forDoc.body()
                        var forecastValue = forBody.select("span.Money-module__money_UZBbh")[1].text()
                        forecastValue = forecastValue.replace(',', '.').replace(' ', Char.MIN_VALUE).dropLast(1)
                        val forecastString = forecast.text.toString() + forecastValue
                        forecast.text = forecastString
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}