package com.example.myfinances

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(val context: Context, val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "shares", factory, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE shares (id INT PRIMARY KEY, ticker TEXT, board TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS shares")
        onCreate(db)
    }

    fun addShare(share: Share) {
        val values = ContentValues()
        values.put("ticker", share.ticker)
        values.put("board", share.board)

        val db = this.writableDatabase
        db.insert("shares", null, values)
        db.close()
    }

    fun deleteShare(ticker: String) {
        val db = this.writableDatabase
        db.delete("shares", "ticker = ?", arrayOf(ticker))
        db.close()
    }

    fun getShares() : ArrayList<Share> {
        val db = this.readableDatabase
        val result = db.rawQuery("SELECT * FROM shares", null)
        val colTickerIndex = result.getColumnIndex("ticker")
        val colBoardIndex = result.getColumnIndex("board")
        val sharesList = arrayListOf<Share>()

        while (result.moveToNext()) {
            sharesList.add(Share(result.getString(colTickerIndex), result.getString(colBoardIndex)))
        }
        result.close()
        db.close()
        return sharesList
    }
}