package com.example.myfinances

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyException
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL


class SearchShare : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_share)

        val arrowBack: ImageView = findViewById(R.id.arrowBackFromSearch)
        val tickerText: EditText = findViewById(R.id.enteredTicker)
        val addButton: Button = findViewById(R.id.addButton)

        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val py = Python.getInstance()

        addButton.setOnClickListener {
            val ticker: String = tickerText.text.trim().toString().uppercase()
            var board: String = ""
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        try {
                            val file = resources.openRawResource(R.raw.securities)
                            val stream = file.bufferedReader()
                            var s = stream.readLine()
                            while (s != null) {
                                if (s == ticker) {
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
                        if (board == "" || board == "None") {
                            Toast.makeText(this@SearchShare, "Тикер не найден", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            val db = DBHelper(this@SearchShare, null)
                            db.addShare(Share(ticker, board))

                            val intent = Intent(this@SearchShare, MainActivity::class.java)
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
