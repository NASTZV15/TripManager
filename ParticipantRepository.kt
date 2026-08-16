package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.Participant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ParticipantRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    suspend fun getParticipantsByTrip(
        tripId: Long
    ): List<Participant> = withContext(Dispatchers.IO) {

        val participants = mutableListOf<Participant>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "trip_participants",
            arrayOf(
                "id",
                "trip_id",
                "name",
                "created_at"
            ),
            "trip_id = ?",
            arrayOf(tripId.toString()),
            null,
            null,
            "id ASC"
        )

        cursor.use {
            while (it.moveToNext()) {

                participants.add(
                    Participant(
                        id = it.getLong(
                            it.getColumnIndexOrThrow("id")
                        ),

                        tripId = it.getLong(
                            it.getColumnIndexOrThrow("trip_id")
                        ),

                        name = it.getString(
                            it.getColumnIndexOrThrow("name")
                        ),

                        createdAt = it.getStringOrNull(
                            "created_at"
                        )
                    )
                )
            }
        }

        participants
    }

    suspend fun getParticipantById(
        participantId: Long
    ): Participant? = withContext(Dispatchers.IO) {

        val db = dbHelper.readableDatabase

        val cursor = db.query(
            "trip_participants",
            arrayOf(
                "id",
                "trip_id",
                "name",
                "created_at"
            ),
            "id = ?",
            arrayOf(participantId.toString()),
            null,
            null,
            null,
            "1"
        )

        cursor.use {

            if (!it.moveToFirst()) {
                return@withContext null
            }

            Participant(
                id = it.getLong(
                    it.getColumnIndexOrThrow("id")
                ),

                tripId = it.getLong(
                    it.getColumnIndexOrThrow("trip_id")
                ),

                name = it.getString(
                    it.getColumnIndexOrThrow("name")
                ),

                createdAt = it.getStringOrNull(
                    "created_at"
                )
            )
        }
    }

    suspend fun insertParticipant(
        participant: Participant
    ): Long = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {

            if (participant.id > 0) {
                put("id", participant.id)
            }

            put("trip_id", participant.tripId)
            put("name", participant.name)

            /*
             * Если createdAt == null, НЕ передаём
             * колонку вообще.
             *
             * Тогда SQLite использует:
             *
             * DEFAULT CURRENT_TIMESTAMP
             */
            if (participant.createdAt != null) {
                put(
                    "created_at",
                    participant.createdAt
                )
            }
        }

        db.insertOrThrow(
            "trip_participants",
            null,
            values
        )
    }

    suspend fun updateParticipant(
        participant: Participant
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {

            put("trip_id", participant.tripId)
            put("name", participant.name)

            if (participant.createdAt != null) {
                put(
                    "created_at",
                    participant.createdAt
                )
            } else {
                putNull("created_at")
            }
        }

        db.update(
            "trip_participants",
            values,
            "id = ?",
            arrayOf(participant.id.toString())
        )
    }

    suspend fun deleteParticipant(
        participant: Participant
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        db.delete(
            "trip_participants",
            "id = ?",
            arrayOf(participant.id.toString())
        )
    }

    suspend fun deleteByTrip(
        tripId: Long
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        db.delete(
            "trip_participants",
            "trip_id = ?",
            arrayOf(tripId.toString())
        )
    }

    suspend fun deleteAll(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "trip_participants",
                null,
                null
            )
        }

    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM trip_participants",
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

    private fun android.database.Cursor.getStringOrNull(
        columnName: String
    ): String? {

        val index = getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return null
        }

        return getString(index)
    }
}