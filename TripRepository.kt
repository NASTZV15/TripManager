package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TripRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    /**
     * Получить все поездки.
     */
    suspend fun getAllTrips(): List<Trip> =
        withContext(Dispatchers.IO) {

            val trips = mutableListOf<Trip>()

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "trips",
                arrayOf(
                    "id",
                    "name",
                    "destination",
                    "start_date",
                    "end_date",
                    "created_at"
                ),
                null,
                null,
                null,
                null,
                "created_at DESC"
            )

            cursor.use {

                while (it.moveToNext()) {

                    trips.add(
                        Trip(
                            id = it.getLong(
                                it.getColumnIndexOrThrow("id")
                            ),

                            name = it.getString(
                                it.getColumnIndexOrThrow("name")
                            ),

                            destination =
                                it.getStringOrNull(
                                    "destination"
                                ),

                            startDate =
                                it.getLongOrNull(
                                    "start_date"
                                ),

                            endDate =
                                it.getLongOrNull(
                                    "end_date"
                                ),

                            createdAt =
                                it.getLongOrNull(
                                    "created_at"
                                )
                                    ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            trips
        }

    /**
     * Получить одну поездку по ID.
     */
    suspend fun getTripById(
        tripId: Long
    ): Trip? =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "trips",
                arrayOf(
                    "id",
                    "name",
                    "destination",
                    "start_date",
                    "end_date",
                    "created_at"
                ),
                "id = ?",
                arrayOf(tripId.toString()),
                null,
                null,
                null,
                "1"
            )

            cursor.use {

                if (!it.moveToFirst()) {
                    return@withContext null
                }

                Trip(
                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),

                    name = it.getString(
                        it.getColumnIndexOrThrow("name")
                    ),

                    destination =
                        it.getStringOrNull(
                            "destination"
                        ),

                    startDate =
                        it.getLongOrNull(
                            "start_date"
                        ),

                    endDate =
                        it.getLongOrNull(
                            "end_date"
                        ),

                    createdAt =
                        it.getLongOrNull(
                            "created_at"
                        )
                            ?: System.currentTimeMillis()
                )
            }
        }

    /**
     * Добавить поездку.
     *
     * Если id == 0, SQLite создаст его автоматически.
     * Если id > 0, сохраняем существующий ID.
     *
     * Это особенно важно для импорта .db из ПК-версии.
     */
    suspend fun insertTrip(
        trip: Trip
    ): Long =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                if (trip.id > 0) {
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

    /**
     * Обновить поездку.
     */
    suspend fun updateTrip(
        trip: Trip
    ) =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

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
                arrayOf(
                    trip.id.toString()
                )
            )
        }

    /**
     * Удалить одну поездку.
     *
     * Если в DatabaseHelper включён foreign key,
     * связанные wallets/expenses и т.д.
     * также будут удалены согласно ON DELETE CASCADE.
     */
    suspend fun deleteTrip(
        trip: Trip
    ) =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "trips",
                "id = ?",
                arrayOf(
                    trip.id.toString()
                )
            )
        }

    /**
     * Удалить все поездки.
     */
    suspend fun deleteAll() =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "trips",
                null,
                null
            )
        }

    /**
     * Количество поездок.
     */
    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

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
     * Безопасно получить Long из SQLite.
     *
     * SQLite INTEGER -> Long.
     */
    private fun android.database.Cursor.getLongOrNull(
        columnName: String
    ): Long? {

        val index =
            getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getLong(index)
    }

    /**
     * Безопасно получить String из SQLite.
     */
    private fun android.database.Cursor.getStringOrNull(
        columnName: String
    ): String? {

        val index =
            getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getString(index)
    }
}