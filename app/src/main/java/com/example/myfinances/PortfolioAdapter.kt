package com.example.myfinances

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.Jsoup
import java.util.concurrent.Executors

class PortfolioAdapter(var shares: List<ShareInPortfolio>, var context: Context) :
    RecyclerView.Adapter<PortfolioAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ticker: TextView = view.findViewById(R.id.shareTicker)
        val price: TextView = view.findViewById(R.id.sharePrice)
        val change: TextView = view.findViewById(R.id.sharePriceChange)
        val button: androidx.constraintlayout.widget.ConstraintLayout =
            view.findViewById(R.id.share_in_list)
        val imageLogo: ImageView = view.findViewById(R.id.shareImage)
        var imageSet = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.share_in_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return shares.count()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (!holder.imageSet) {
            val executor = Executors.newSingleThreadExecutor()
            val handler = android.os.Handler(Looper.getMainLooper())
            var image: Bitmap? = null
            executor.execute {
                try {
                    val url = "https://porti.ru/company/mfso/${shares[position].ticker}"
                    val doc = Jsoup.connect(url).get()
                    val body = doc.body()
                    val imageBlock = body.select("div.logo img[itemprop=image]")
                    val imageUrl = imageBlock.attr("src")
                    val stream = java.net.URL(imageUrl).openStream()
                    image = BitmapFactory.decodeStream(stream)
                    handler.post {
                        holder.imageLogo.setImageBitmap(image)
                        holder.imageSet = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            holder.ticker.text = shares[position].ticker
            holder.price.text = String.format("%.2f", shares[position].price.toDouble() * shares[position].number)
            var differ: Double =
                (shares[position].price.toDouble() - shares[position].transactionPrice.toDouble())
            val ratio: Double = differ / shares[position].transactionPrice.toDouble()
            differ *= shares[position].number
            val changeText = (if (differ >= 0) "+" else "") + String.format("%.2f", differ) + " (${
                String.format(
                    "%.5f",
                    ratio
                )
            }%)"
            holder.change.text = changeText
            if (differ >= 0) {
                holder.change.setTextColor(Color.GREEN)
            } else {
                holder.change.setTextColor(Color.RED)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.button.setOnClickListener {
            val intent = Intent(context, PortfolioShareStatistics::class.java)
            intent.putExtra("ticker", shares[position].ticker)
            intent.putExtra("transactionPrice", shares[position].transactionPrice)
            intent.putExtra("currentPrice", shares[position].price)
            intent.putExtra("cashFlow", shares[position].cashFlow)
            intent.putExtra("transactions", shares[position].transactions)
            intent.putExtra("number", shares[position].number)
            context.startActivity(intent)
        }
    }
}