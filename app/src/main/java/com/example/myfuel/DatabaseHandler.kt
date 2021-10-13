package com.example.myfuel

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DatabaseHandler(context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "FuelDatabase"
        private val TABLE_FUEL = "FuelTable"
        private val KEY_ID = "id"
        private val KEY_ODOMETER = "odometer"
        private val KEY_FUEL_AMOUNT = "fuel_amount"
        private val KEY_FUEL_PRICE = "fuel_price"
        private val KEY_DATE = "date"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_FUEL_TABLE = ("CREATE TABLE " + TABLE_FUEL +"("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_ODOMETER + " INTEGER, "
                + KEY_FUEL_AMOUNT + " REAL, " + KEY_FUEL_PRICE + " REAL, "
                + KEY_DATE + " TEXT" + ")")
        db?.execSQL(CREATE_FUEL_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS " + TABLE_FUEL)
        onCreate(db)
    }

    fun addFuelRecord(fuelRecord: DTOFuel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        val id: Int = getLastId() + 1

        contentValues.put(KEY_ID, id)
        contentValues.put(KEY_ODOMETER, fuelRecord.odometer)
        contentValues.put(KEY_FUEL_AMOUNT, fuelRecord.fuelAmount)
        contentValues.put(KEY_FUEL_PRICE, fuelRecord.fuelPrice)
        contentValues.put(KEY_DATE, fuelRecord.date.toString())

        val success = db.insert(TABLE_FUEL, null, contentValues)

        db.close()

        return  success
    }

    fun getStatistics(): DTOStats? {
        val db = this.writableDatabase
        val queryResultList: ArrayList<DTOFuel> = ArrayList<DTOFuel>()
        val query = "SELECT * FROM $TABLE_FUEL ORDER BY $KEY_ID ASC"

        var cursor: Cursor? = null
        try{
            cursor = db.rawQuery(query, null)
        }catch (e: SQLiteException) {
            db.execSQL(query)
            return null
        }

        var id: Int
        var odometer: Int
        var fuelAmount: Double
        var fuelPrice: Double
        var date: Date

        if (cursor.moveToFirst()) {
            cursor.count
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                odometer = cursor.getInt(cursor.getColumnIndex(KEY_ODOMETER))
                fuelAmount = cursor.getDouble(cursor.getColumnIndex(KEY_FUEL_AMOUNT))
                fuelPrice = cursor.getDouble(cursor.getColumnIndex(KEY_FUEL_PRICE))
                val stringDate = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                date = Date(stringDate)
                queryResultList.add(DTOFuel(id = id, odometer = odometer, fuelAmount = fuelAmount, fuelPrice = fuelPrice, date = date))
            } while (cursor.moveToNext())
        }

        if (queryResultList.isEmpty()) {
            return DTOStats(avgFuel = 0.0, avgMonthlyFuelCost = 0.0, avgMonthlyDistance = 0)
        }
        val startKM:Int = queryResultList.first().odometer!!
        val endKM:Int = queryResultList.last().odometer!!

        val totalDistance = endKM - startKM


        val totalFuel = queryResultList.sumOf { it.fuelAmount } - queryResultList.last().fuelAmount

        val avgConsumptionPer100KM: Double = if (totalDistance == 0) 0.0 else (totalFuel / totalDistance) * 100


        val MonthMap = HashMap<String, ArrayList<DTOFuel>>()

        for (fuelObject in queryResultList) {
            val produceKey = "${fuelObject.date!!.year}${fuelObject.date!!.month}"
            if(MonthMap.containsKey(produceKey)) {
                val arrList: ArrayList<DTOFuel> = MonthMap[produceKey]!!
                arrList.add(fuelObject)

                MonthMap[produceKey] = arrList
            } else {
                val arrList = ArrayList<DTOFuel>()
                arrList.add(fuelObject)

                MonthMap[produceKey] = arrList
            }
        }
        var sumDistance = 0
        var sumFuelCost = 0.0
        for ((key, value ) in MonthMap) {
            var fuelCost = 0.0
            for(fuelElement in value) {
                sumFuelCost += fuelElement.fuelAmount!! * fuelElement.fuelPrice!!
            }
            val min = value.minByOrNull { it.odometer!! }
            val max = value.maxByOrNull { it.odometer!! }
            sumDistance += (max!!.odometer!! - min!!.odometer!!)

        }
        return DTOStats(avgFuel = avgConsumptionPer100KM, avgMonthlyFuelCost = (sumFuelCost / MonthMap.size), avgMonthlyDistance = (sumDistance / MonthMap.size))
    }

    fun getLastId(): Int {
        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_ID FROM $TABLE_FUEL ORDER BY $KEY_ID DESC LIMIT 1";
        val cursor: Cursor
        var id: Int
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return -1
        }

        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(KEY_ID))
        } else {
            return 0
        }
    }
}