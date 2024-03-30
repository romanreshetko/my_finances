package com.example.myfinances

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddTransaction : AppCompatActivity() {
    var board = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        var type: Boolean? = null
        val arrowBack: ImageView = findViewById(R.id.arrowBackFromAdd)
        val submit: Button = findViewById(R.id.addTransactionButton)
        val radioBuy: RadioButton = findViewById(R.id.radioButtonBuy)
        val radioSell: RadioButton = findViewById(R.id.radioButtonSell)
        val ticker: EditText = findViewById(R.id.enteredTickerTransaction)
        val price: EditText = findViewById(R.id.enteredPriceTransaction)
        val volume: EditText = findViewById(R.id.enteredVolumeTransaction)

        radioBuy.setOnClickListener {
            type = true
        }

        radioSell.setOnClickListener {
            type = false
        }

        arrowBack.setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            startActivity(intent)
        }

        submit.setOnClickListener {
            val enteredTicker = ticker.text.trim().toString().uppercase()
            val enteredPrice = price.text.trim().toString()
            val priceValue = enteredPrice.toDoubleOrNull()
            val enteredVolume = volume.text.trim().toString()
            val volumeValue = enteredVolume.toIntOrNull()

            if (enteredTicker == "" || enteredPrice == "" || enteredVolume == "" || type == null) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
            } else {
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            try {
                                val file = resources.openRawResource(R.raw.securities)
                                val stream = file.bufferedReader()
                                var s = stream.readLine()
                                while (s != null) {
                                    if (s == enteredTicker) {
                                        board = "TQBR"
                                        break
                                    }
                                    s = stream.readLine()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    withContext(Dispatchers.Main) {
                        try {
                            if (board == "") {
                                Toast.makeText(
                                    this@AddTransaction,
                                    "Тикер не найден",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (priceValue == null || priceValue <= 0) {
                                Toast.makeText(
                                    this@AddTransaction,
                                    "Неверная цена",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (volumeValue == null || volumeValue == 0) {
                                Toast.makeText(
                                    this@AddTransaction,
                                    "Неверный объём",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val shareInPortfolio = ShareInPortfolio(
                                    enteredTicker,
                                    board,
                                    if (type == true) volumeValue else -volumeValue,
                                    priceValue.toString()
                                )
                                val db = DBPortfolioHelper(this@AddTransaction, null)
                                db.addShareToPortfolio(shareInPortfolio)

                                val intent =
                                    Intent(this@AddTransaction, PortfolioActivity::class.java)
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}