package com.tripmanager.data.database

import com.tripmanager.data.models.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context

class TripRepository(
    private val databaseHelper =
        DatabaseHelper.getInstance(context)
) {

    suspend fun getAllTrips(): List<Trip> = withContext(Dispatchers.IO) {
        val db = databaseHelper.readableDatabase
        val trips = mutableListOf<Trip>()

        val cursor = db.rawQuery(
            """
            SELECT id, name, destination, start_date, end_date, created_at
            FROM trips
            ORDER BY created_at DESC
            """.trimIndent(),
            null
        )

        cursor.use {
            while (it.moveToNext()) {

                val id = it.getLongOrNull("id") ?: 0L

                val name = it.getStringOrNull("name") ?: ""

                val destination =
                    it.getStringOrNull("destination")

                val startDate =
                    getDateAsTimestamp(it, "start_date")

                val endDate =
                    getDateAsTimestamp(it, "end_date")

                val createdAt =
                    getDateAsTimestamp(it, "created_at")
                        ?: System.currentTimeMillis()

                trips.add(
                    Trip(
                        id = id,
                        name = name,
                        destination = destination,
                        startDate = startDate,
                        endDate = endDate,
                        createdAt = createdAt
                    )
                )
            }
        }

        trips
    }

    suspend fun getTripById(tripId: Long): Trip? =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.readableDatabase

            val cursor = db.rawQuery(
                """
                SELECT id, name, destination, start_date, end_date, created_at
                FROM trips
                WHERE id = ?
                LIMIT 1
                """.trimIndent(),
                arrayOf(tripId.toString())
            )

            cursor.use {
                if (!it.moveToFirst()) {
                    return@withContext null
                }

                Trip(
                    id = it.getLongOrNull("id") ?: tripId,

                    name =
                        it.getStringOrNull("name")
                            ?: "",

                    destination =
                        it.getStringOrNull("destination"),

                    startDate =
                        getDateAsTimestamp(
                            it,
                            "start_date"
                        ),

                    endDate =
                        getDateAsTimestamp(
                            it,
                            "end_date"
                        ),

                    createdAt =
                        getDateAsTimestamp(
                            it,
                            "created_at"
                        )
                            ?: System.currentTimeMillis()
                )
            }
        }

    suspend fun insertTrip(trip: Trip): Long =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            val values = android.content.ContentValues().apply {

                if (trip.id > 0) {
                    put("id", trip.id)
                }

                put("name", trip.name)

                if (trip.destination != null) {
                    put("destination", trip.destination)
                } else {
                    putNull("destination")
                }

                if (trip.startDate != null) {
                    put("start_date", trip.startDate)
                } else {
                    putNull("start_date")
                }

                if (trip.endDate != null) {
                    put("end_date", trip.endDate)
                } else {
                    putNull("end_date")
                }

                put("created_at", trip.createdAt)
            }

            db.insertOrThrow(
                "trips",
                null,
                values
            )
        }

    suspend fun updateTrip(trip: Trip) =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            val values = android.content.ContentValues().apply {

                put("name", trip.name)

                if (trip.destination != null) {
                    put("destination", trip.destination)
                } else {
                    putNull("destination")
                }

                if (trip.startDate != null) {
                    put("start_date", trip.startDate)
                } else {
                    putNull("start_date")
                }

                if (trip.endDate != null) {
                    put("end_date", trip.endDate)
                } else {
                    putNull("end_date")
                }

                put("created_at", trip.createdAt)
            }

            db.update(
                "trips",
                values,
                "id = ?",
                arrayOf(trip.id.toString())
            )
        }

    suspend fun deleteTrip(trip: Trip) =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            db.delete(
                "trips",
                "id = ?",
                arrayOf(trip.id.toString())
            )
        }

    suspend fun deleteTripById(tripId: Long) =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            db.delete(
                "trips",
                "id = ?",
                arrayOf(tripId.toString())
            )
        }

    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM trips",
                null
            )

            cursor.use {
                if (it.moveToFirst()) {
                    it.getInt(0)
                } else {
                    0
                }
            }
        }

    private fun getDateAsTimestamp(
        cursor: android.database.Cursor,
        columnName: String
    ): Long? {

        val index = cursor.getColumnIndex(columnName)

        if (index < 0 || cursor.isNull(index)) {
            return null
        }

        /*
         * В Android SQLite наша БД может содержать:
         *
         * 1. INTEGER:
         *    1776816000000
         *
         * 2. TEXT:
         *    2026-08-20
         *
         * 3. TEXT:
         *    2026-08-20 00:00:00
         *
         * 4. TEXT:
         *    20.08.2026
         *
         * Поэтому сначала читаем значение
         * как String и затем определяем формат.
         */

        val value = cursor.getString(index)?.trim()

        if (value.isNullOrEmpty()) {
            return null
        }

        // Если это Unix timestamp
        value.toLongOrNull()?.let { number ->

            /*
             * Если число очень маленькое, это может быть
             * Unix timestamp в секундах, а не миллисекундах.
             *
             * Например:
             * 1787000000
             *
             * превращаем в:
             * 1787000000000
             */

            return if (number in 1..10_000_000_000L) {
                number * 1000L
            } else {
                number
            }
        }

        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd",
            "dd.MM.yyyy",
            "dd.MM.yyyy HH:mm:ss"
        )

        for (format in formats) {

            try {

                val formatter =
                    SimpleDateFormat(
                        format,
                        Locale.US
                    )

                formatter.isLenient = false

                val date =
                    formatter.parse(value)

                if (date != null) {
                    return date.time
                }

            } catch (_: Exception) {
                // Пробуем следующий формат
            }
        }

        return null
    }

    private fun android.database.Cursor.getStringOrNull(
        columnName: String
    ): String? {

        val index = getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getString(index)
    }

    private fun android.database.Cursor.getLongOrNull(
        columnName: String
    ): Long? {

        val index = getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getLong(index)
    }
}