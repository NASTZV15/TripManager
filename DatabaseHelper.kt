package com.tripmanager.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper private constructor(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {

        // ВАЖНО:
        // Используем IF NOT EXISTS.
        // Это позволяет открыть уже существующую
        // импортированную SQLite-базу.

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trips (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                destination TEXT,
                start_date INTEGER,
                end_date INTEGER,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS wallets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                name TEXT NOT NULL,
                currency TEXT DEFAULT 'RUB',
                balance REAL DEFAULT 0,
                created_at INTEGER NOT NULL,
                owner TEXT DEFAULT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                icon TEXT DEFAULT '📌',
                color TEXT DEFAULT '#3498db'
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                wallet_id INTEGER,
                category_id INTEGER,
                description TEXT NOT NULL,
                amount REAL NOT NULL,
                paid_by TEXT NOT NULL,
                date INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (wallet_id)
                    REFERENCES wallets(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (category_id)
                    REFERENCES categories(id)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trip_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS expense_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                expense_id INTEGER,
                participant_name TEXT,
                share REAL,
                FOREIGN KEY (expense_id)
                    REFERENCES expenses(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transfers (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                from_wallet_id INTEGER,
                to_wallet_id INTEGER,
                amount REAL NOT NULL,
                converted_amount REAL,
                description TEXT,
                date INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (from_wallet_id)
                    REFERENCES wallets(id)
                    ON DELETE CASCADE,
                FOREIGN KEY (to_wallet_id)
                    REFERENCES wallets(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS trip_plan (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                date TEXT NOT NULL,
                time TEXT,
                place TEXT NOT NULL,
                description TEXT,
                address TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS packing_list (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                category TEXT NOT NULL,
                item TEXT NOT NULL,
                is_checked INTEGER DEFAULT 0,
                quantity INTEGER DEFAULT 1,
                note TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS accommodations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                name TEXT NOT NULL,
                address TEXT,
                check_in INTEGER,
                check_out INTEGER,
                price REAL,
                currency TEXT DEFAULT 'RUB',
                contact_phone TEXT,
                contact_email TEXT,
                booking_reference TEXT,
                notes TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS flights (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trip_id INTEGER,
                airline TEXT,
                flight_number TEXT,
                departure_airport TEXT,
                arrival_airport TEXT,
                departure_date INTEGER,
                departure_time TEXT,
                arrival_date INTEGER,
                arrival_time TEXT,
                price REAL,
                currency TEXT DEFAULT 'RUB',
                booking_reference TEXT,
                seat_number TEXT,
                baggage_info TEXT,
                notes TEXT,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (trip_id)
                    REFERENCES trips(id)
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Пока не удаляем данные автоматически.
        //
        // Импорт/экспорт .db должен работать с существующей
        // структурой базы.
    }

    companion object {

        private const val DATABASE_NAME = "trip_expenses.db"

        private const val DATABASE_VERSION = 1

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: DatabaseHelper(
                    context.applicationContext
                ).also {
                    INSTANCE = it
                }
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}