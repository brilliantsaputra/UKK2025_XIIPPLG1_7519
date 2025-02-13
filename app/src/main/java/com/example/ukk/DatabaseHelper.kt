package com.example.ukk

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: android.content.Context) :
    SQLiteOpenHelper(context, "Ukk_DB", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {


    }

    }