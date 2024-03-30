package com.example.myfinances

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBPortfolioHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "portfolio", factory, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query =
            "CREATE TABLE portfolio (id INT PRIMARY KEY, ticker TEXT, board TEXT, number INT, transactionPrice TEXT, cashFlow TEXT, transactions INT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS portfolio")
        onCreate(db)
    }

    fun addShareToPortfolio(shareInPortfolio: ShareInPortfolio) {
        val checkDB = this.readableDatabase
        val result = checkDB.rawQuery(
            "SELECT * FROM portfolio WHERE ticker = ?",
            arrayOf(shareInPortfolio.ticker)
        )
        val db = this.writableDatabase
        val values = ContentValues()
        if (result.moveToNext()) {
            var transactionPrice = ""
            val colTransactionPriceIndex = result.getColumnIndex("transactionPrice")
            val colNumberIndex = result.getColumnIndex("number")
            val number = result.getInt(colNumberIndex) + shareInPortfolio.number
            if (number == 0) {
                db.delete("portfolio", "ticker = ?", arrayOf(shareInPortfolio.ticker))
                result.close()
                checkDB.close()
                db.close()
                return
            }
            if (result.getInt(colNumberIndex) * shareInPortfolio.number > 0) {
                transactionPrice = ((result.getString(colTransactionPriceIndex)
                    .toDouble() * result.getInt(colNumberIndex) + shareInPortfolio.transactionPrice.toDouble() * shareInPortfolio.number) / (result.getInt(
                    colNumberIndex
                ) + shareInPortfolio.number)).toString()
            } else if (number * result.getInt(colNumberIndex) < 0) {
                transactionPrice = shareInPortfolio.transactionPrice
            } else if (number > 0) {
                transactionPrice = result.getString(colTransactionPriceIndex)
            } else {
                transactionPrice = shareInPortfolio.transactionPrice
            }
            val colCashFlowIndex = result.getColumnIndex("cashFlow")
            val colTransactionsIndex = result.getColumnIndex("transactions")
            val cashFlow = (result.getString(colCashFlowIndex)
                .toDouble() + shareInPortfolio.cashFlow.toDouble()).toString()
            val transactions = result.getInt(colTransactionsIndex) + shareInPortfolio.transactions

            values.put("ticker", shareInPortfolio.ticker)
            values.put("board", shareInPortfolio.board)
            values.put("number", number)
            values.put("transactionPrice", transactionPrice)
            values.put("cashFlow", cashFlow)
            values.put("transactions", transactions)

            db.update("portfolio", values, "ticker = ?", arrayOf(shareInPortfolio.ticker))
        } else {
            values.put("ticker", shareInPortfolio.ticker)
            values.put("board", shareInPortfolio.board)
            values.put("number", shareInPortfolio.number)
            values.put("transactionPrice", shareInPortfolio.transactionPrice)
            values.put("cashFlow", shareInPortfolio.cashFlow)
            values.put("transactions", shareInPortfolio.transactions)

            db.insert("portfolio", null, values)
        }
        result.close()
        checkDB.close()
        db.close()
    }

    fun deleteShareFromPortfolio(ticker: String) {
        val db = this.writableDatabase
        db.delete("portfolio", "ticker = ?", arrayOf(ticker))
        db.close()
    }

    fun getSharesFromPortfolio(): ArrayList<ShareInPortfolio> {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM portfolio", null)
        val colTickerIndex = result.getColumnIndex("ticker")
        val colBoardIndex = result.getColumnIndex("board")
        val colNumberIndex = result.getColumnIndex("number")
        val colTransactionPriceIndex = result.getColumnIndex("transactionPrice")
        val colCashFlowIndex = result.getColumnIndex("cashFlow")
        val colTransactionsIndex = result.getColumnIndex("transactions")
        val sharesList = arrayListOf<ShareInPortfolio>()

        while (result.moveToNext()) {
            val shareInPortfolio = ShareInPortfolio(
                result.getString(colTickerIndex),
                result.getString(colBoardIndex),
                result.getInt(colNumberIndex),
                result.getString(colTransactionPriceIndex)
            )
            shareInPortfolio.cashFlow = result.getString(colCashFlowIndex)
            shareInPortfolio.transactions = result.getInt(colTransactionsIndex)
            sharesList.add(shareInPortfolio)
        }
        result.close()
        db.close()
        return sharesList
    }
}