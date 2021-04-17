package com.example.stravabasedapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DataBaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val TABLE_NAME = "Acts"
    private val COL_ID = "id"
    private val COL_JSON = "json"

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY, " +
                "$COL_JSON TEXT)"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun insert(id: Long, json: String) {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COL_ID, id)
        cv.put(COL_JSON, json)
        val result = db.insert(TABLE_NAME, null, cv)
        if (result == -1L)
            Log.wtf("myTag", "failed db insert")
        else
            Log.wtf("myTag", "success db insert")
    }

    fun get(id: Long): String {
        val db = this.readableDatabase
        val query = "select $COL_JSON from $TABLE_NAME where $COL_ID = $id"
        val result = db.rawQuery(query, null)
        if(result.moveToFirst()) {
            return result.getString(0)
        }
        return ""
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "SBA.db"
    }
}