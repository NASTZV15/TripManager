package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.ExpenseParticipant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseParticipantRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    suspend fun getParticipantsByExpense(
        expenseId: Long
    ): List<ExpenseParticipant> =
        withContext(Dispatchers.IO) {

            val participants = mutableListOf<ExpenseParticipant>()
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "expense_participants",
                arrayOf(
                    "id",
                    "expense_id",
                    "participant_name",
                    "share"
                ),
                "expense_id = ?",
                arrayOf(expenseId.toString()),
                null,
                null,
                "id ASC"
            )

            cursor.use {
                while (it.moveToNext()) {

                    participants.add(
                        ExpenseParticipant(
                            id = it.getLong(
                                it.getColumnIndexOrThrow("id")
                            ),

                            expenseId = it.getLong(
                                it.getColumnIndexOrThrow("expense_id")
                            ),

                            participantName =
                                it.getStringOrNull(
                                    "participant_name"
                                ) ?: "",

                            share = if (
                                it.isNull(
                                    it.getColumnIndexOrThrow("share")
                                )
                            ) {
                                0.0
                            } else {
                                it.getDouble(
                                    it.getColumnIndexOrThrow("share")
                                )
                            }
                        )
                    )
                }
            }

            participants
        }

    suspend fun getParticipantById(
        participantId: Long
    ): ExpenseParticipant? =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "expense_participants",
                arrayOf(
                    "id",
                    "expense_id",
                    "participant_name",
                    "share"
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

                ExpenseParticipant(
                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),

                    expenseId = it.getLong(
                        it.getColumnIndexOrThrow("expense_id")
                    ),

                    participantName =
                        it.getStringOrNull(
                            "participant_name"
                        ) ?: "",

                    share = if (
                        it.isNull(
                            it.getColumnIndexOrThrow("share")
                        )
                    ) {
                        0.0
                    } else {
                        it.getDouble(
                            it.getColumnIndexOrThrow("share")
                        )
                    }
                )
            }
        }

    suspend fun insertExpenseParticipant(
        participant: ExpenseParticipant
    ): Long = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {

            if (participant.id > 0) {
                put("id", participant.id)
            }

            put(
                "expense_id",
                participant.expenseId
            )

            put(
                "participant_name",
                participant.participantName
            )

            put(
                "share",
                participant.share
            )
        }

        db.insertOrThrow(
            "expense_participants",
            null,
            values
        )
    }

    suspend fun updateExpenseParticipant(
        participant: ExpenseParticipant
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {

            put(
                "expense_id",
                participant.expenseId
            )

            put(
                "participant_name",
                participant.participantName
            )

            put(
                "share",
                participant.share
            )
        }

        db.update(
            "expense_participants",
            values,
            "id = ?",
            arrayOf(
                participant.id.toString()
            )
        )
    }

    suspend fun deleteExpenseParticipant(
        participant: ExpenseParticipant
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        db.delete(
            "expense_participants",
            "id = ?",
            arrayOf(
                participant.id.toString()
            )
        )
    }

    suspend fun deleteByExpense(
        expenseId: Long
    ): Int = withContext(Dispatchers.IO) {

        val db = dbHelper.writableDatabase

        db.delete(
            "expense_participants",
            "expense_id = ?",
            arrayOf(expenseId.toString())
        )
    }

    suspend fun deleteAll(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "expense_participants",
                null,
                null
            )
        }

    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM expense_participants",
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