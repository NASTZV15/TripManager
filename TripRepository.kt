package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.tripmanager.data.models.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TripRepository(
    context: Context
) {

    private val databaseHelper =
        DatabaseHelper.getInstance(context.applicationContext)

    suspend fun getAllTrips(): List<Trip> =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.readableDatabase

            val trips = mutableListOf<Trip>()

            val cursor = db.rawQuery(
                """
                SELECT
                    id,
                    name,
                    destination,
                    start_date,
                    end_date,
                    created_at
                FROM trips
                ORDER BY created_at DESC
                """.trimIndent(),
                null
            )

            cursor.use {

                while (it.moveToNext()) {

                    val trip = Trip(
                        id = it.getLongOrNull("id") ?: 0L,

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

                    trips.add(trip)
                }
            }

            trips
        }

    suspend fun getTripById(
        tripId: Long
    ): Trip? =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.readableDatabase

            val cursor = db.rawQuery(
                """
                SELECT
                    id,
                    name,
                    destination,
                    start_date,
                    end_date,
                    created_at
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
                    id =
                        it.getLongOrNull("id")
                            ?: tripId,

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

    suspend fun insertTrip(
        trip: Trip
    ): Long =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            val values = ContentValues().apply {

                if (trip.id > 0L) {
                    put("id", trip.id)
                }

                put(
                    "name",
                    trip.name
                )

                if (trip.destination != null) {
                    put(
                        "destination",
                        trip.destination
                    )
                } else {
                    putNull("destination")
                }

                if (trip.startDate != null) {
                    put(
                        "start_date",
                        trip.startDate
                    )
                } else {
                    putNull("start_date")
                }

                if (trip.endDate != null) {
                    put(
                        "end_date",
                        trip.endDate
                    )
                } else {
                    putNull("end_date")
                }

                put(
                    "created_at",
                    trip.createdAt
                )
            }

            db.insertOrThrow(
                "trips",
                null,
                values
            )
        }

    suspend fun updateTrip(
        trip: Trip
    ) =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            val values = ContentValues().apply {

                put(
                    "name",
                    trip.name
                )

                if (trip.destination != null) {
                    put(
                        "destination",
                        trip.destination
                    )
                } else {
                    putNull("destination")
                }

                if (trip.startDate != null) {
                    put(
                        "start_date",
                        trip.startDate
                    )
                } else {
                    putNull("start_date")
                }

                if (trip.endDate != null) {
                    put(
                        "end_date",
                        trip.endDate
                    )
                } else {
                    putNull("end_date")
                }

                put(
                    "created_at",
                    trip.createdAt
                )
            }

            db.update(
                "trips",
                values,
                "id = ?",
                arrayOf(trip.id.toString())
            )
        }

    suspend fun deleteTrip(
        trip: Trip
    ) =
        withContext(Dispatchers.IO) {

            val db = databaseHelper.writableDatabase

            db.delete(
                "trips",
                "id = ?",
                arrayOf(trip.id.toString())
            )
        }

    suspend fun deleteTripById(
        tripId: Long
    ) =
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

    /**
     * Читает дату из SQLite и преобразует её
     * в Unix timestamp в миллисекундах.
     *
     * Поддерживаются:
     *
     * INTEGER milliseconds
     * INTEGER seconds
     * yyyy-MM-dd
     * yyyy-MM-dd HH:mm:ss
     * yyyy-MM-dd HH:mm:ss.SSS
     * dd.MM.yyyy
     * dd.MM.yyyy HH:mm:ss
     */
    private fun getDateAsTimestamp(
        cursor: Cursor,
        columnName: String
    ): Long? {

        val index =
            cursor.getColumnIndex(columnName)

        if (index < 0 || cursor.isNull(index)) {
            return null
        }

        val value =
            cursor.getString(index)
                ?.trim()

        if (value.isNullOrEmpty()) {
            return null
        }

        /*
         * Сначала проверяем число.
         */
        value.toLongOrNull()?.let { number ->

            /*
             * Если число похоже на Unix timestamp
             * в секундах — переводим в миллисекунды.
             */
            return if (
                number in 1L..10_000_000_000L
            ) {
                number * 1000L
            } else {
                number
            }
        }

        /*
         * Если в БД дата хранится как текст.
         */
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
                // Пробуем следующий формат.
            }
        }

        return null
    }

    private fun Cursor.getStringOrNull(
        columnName: String
    ): String? {

        val index =
            getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getString(index)
    }

    private fun Cursor.getLongOrNull(
        columnName: String
    ): Long? {

        val index =
            getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getLong(index)
    }
}