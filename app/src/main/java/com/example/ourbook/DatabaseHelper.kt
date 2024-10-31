package com.example.ourbook

import Profile
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "OurBook.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_NAME = "book_data"

        // Table columns
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_NICKNAME = "nickname"
        private const val COL_EMAIL = "email"
        private const val COL_ADDRESS = "address"
        private const val COL_BIRTHDATE = "birthdate"
        private const val COL_PHONE = "phone"
        private const val COL_PHOTO_URI = "photo_uri" // Ubah nama kolom
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_NAME TEXT NOT NULL, " +
                "$COL_NICKNAME TEXT NOT NULL, " +
                "$COL_EMAIL TEXT NOT NULL, " +
                "$COL_ADDRESS TEXT, " +
                "$COL_BIRTHDATE TEXT NOT NULL, " +
                "$COL_PHONE TEXT NOT NULL, " +
                "$COL_PHOTO_URI TEXT)") // Ubah tipe menjadi TEXT
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(name: String, nickname: String, email: String, address: String?, birthdate: String, phone: String, photoUri: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_NICKNAME, nickname)
            put(COL_EMAIL, email)
            put(COL_ADDRESS, address)
            put(COL_BIRTHDATE, birthdate)
            put(COL_PHONE, phone)
            put(COL_PHOTO_URI, photoUri) // Simpan URI di sini
        }

        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L // Return true if insert was successful
    }

    fun getAllBooks(): List<Profile> {
        val books = mutableListOf<Profile>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
                val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COL_NICKNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS))
                val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDATE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_URI))

                books.add(Profile(id, name, nickname, email, address, birthdate, phone, photoUri))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return books
    }

    fun updateData(id: Int, name: String, nickname: String, email: String, address: String?, birthdate: String, phone: String, photoUri: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_NICKNAME, nickname)
            put(COL_EMAIL, email)
            put(COL_ADDRESS, address)
            put(COL_BIRTHDATE, birthdate)
            put(COL_PHONE, phone)
            put(COL_PHOTO_URI, photoUri) // Simpan URI di sini
        }

        val result = db.update(TABLE_NAME, contentValues, "$COL_ID = ?", arrayOf(id.toString()))
        return result > 0 // Return true if update was successful
    }

    fun deleteData(id: Int): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
        return result > 0 // Return true if delete was successful
    }

    fun getBookById(id: Int): Profile? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COL_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        var book: Profile? = null
        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
            val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COL_NICKNAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL))
            val address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS))
            val birthdate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BIRTHDATE))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
            val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_URI))

            book = Profile(id, name, nickname, email, address, birthdate, phone, photoUri)
        }
        cursor.close()
        return book
    }

}
