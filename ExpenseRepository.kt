package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    suspend fun getExpensesByTrip(tripId: Long): List<Expense> =
        withContext(Dispatchers.IO) {

            val expenses = mutableListOf<Expense>()
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "expenses",
                arrayOf(
                    "id",
                    "trip_id",
                    "wallet_id",
                    "category_id",
                    "description",
                    "amount",
                    "paid_by",
                    "date",
                    "created_at"
                ),
                "trip_id = ?",
                arrayOf(tripId.toString()),
                null,
                null,
                "date DESC"
            )

            cursor.use {
                while (it.moveToNext()) {

                    expenses.add(
                        Expense(
                            id = it.getLong(
                                it.getColumnIndexOrThrow("id")
                            ),

                            tripId = it.getLong(
                                it.getColumnIndexOrThrow("trip_id")
                            ),

                            walletId = it.getLong(
                                it.getColumnIndexOrThrow("wallet_id")
                            ),

                            categoryId = getNullableLong(
                                it,
                                "category_id"
                            ),

                            description = it.getString(
                                it.getColumnIndexOrThrow("description")
                            ),

                            amount = it.getDouble(
                                it.getColumnIndexOrThrow("amount")
                            ),

                            paidBy = it.getString(
                                it.getColumnIndexOrThrow("paid_by")
                            ),

                            date = it.getStringOrNull("date"),

                            createdAt = it.getStringOrNull("created_at")
                        )
                    )
                }
            }

            expenses
        }

    suspend fun getExpenseById(expenseId: Long): Expense? =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "expenses",
                arrayOf(
                    "id",
                    "trip_id",
                    "wallet_id",
                    "category_id",
                    "description",
                    "amount",
                    "paid_by",
                    "date",
                    "created_at"
                ),
                "id = ?",
                arrayOf(expenseId.toString()),
                null,
                null,
                null,
                "1"
            )

            cursor.use {

                if (!it.moveToFirst()) {
                    return@withContext null
                }

                Expense(
                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),

                    tripId = it.getLong(
                        it.getColumnIndexOrThrow("trip_id")
                    ),

                    walletId = it.getLong(
                        it.getColumnIndexOrThrow("wallet_id")
                    ),

                    categoryId = getNullableLong(
                        it,
                        "category_id"
                    ),

                    description = it.getString(
                        it.getColumnIndexOrThrow("description")
                    ),

                    amount = it.getDouble(
                        it.getColumnIndexOrThrow("amount")
                    ),

                    paidBy = it.getString(
                        it.getColumnIndexOrThrow("paid_by")
                    ),

                    date = it.getStringOrNull("date"),

                    createdAt = it.getStringOrNull("created_at")
                )
            }
        }

    suspend fun getExpensesByDateRange(
        tripId: Long,
        startDate: String,
        endDate: String
    ): List<Expense> =
        withContext(Dispatchers.IO) {

            val expenses = mutableListOf<Expense>()
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "expenses",
                arrayOf(
                    "id",
                    "trip_id",
                    "wallet_id",
                    "category_id",
                    "description",
                    "amount",
                    "paid_by",
                    "date",
                    "created_at"
                ),
                "trip_id = ? AND date BETWEEN ? AND ?",
                arrayOf(
                    tripId.toString(),
                    startDate,
                    endDate
                ),
                null,
                null,
                "date DESC"
            )

            cursor.use {
                while (it.moveToNext()) {

                    expenses.add(
                        Expense(
                            id = it.getLong(
                                it.getColumnIndexOrThrow("id")
                            ),

                            tripId = it.getLong(
                                it.getColumnIndexOrThrow("trip_id")
                            ),

                            walletId = it.getLong(
                                it.getColumnIndexOrThrow("wallet_id")
                            ),

                            categoryId = getNullableLong(
                                it,
                                "category_id"
                            ),

                            description = it.getString(
                                it.getColumnIndexOrThrow("description")
                            ),

                            amount = it.getDouble(
                                it.getColumnIndexOrThrow("amount")
                            ),

                            paidBy = it.getString(
                                it.getColumnIndexOrThrow("paid_by")
                            ),

                            date = it.getStringOrNull("date"),

                            createdAt = it.getStringOrNull("created_at")
                        )
                    )
                }
            }

            expenses
        }

    suspend fun insertExpense(expense: Expense): Long =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                if (expense.id > 0) {
                    put("id", expense.id)
                }

                put("trip_id", expense.tripId)
                put("wallet_id", expense.walletId)

                if (expense.categoryId != null) {
                    put("category_id", expense.categoryId)
                } else {
                    putNull("category_id")
                }

                put("description", expense.description)
                put("amount", expense.amount)
                put("paid_by", expense.paidBy)

                if (expense.date != null) {
                    put("date", expense.date)
                } else {
                    putNull("date")
                }

                if (expense.createdAt != null) {
                    put("created_at", expense.createdAt)
                } else {
                    putNull("created_at")
                }
            }

            db.insertOrThrow(
                "expenses",
                null,
                values
            )
        }

    suspend fun updateExpense(expense: Expense): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                put("trip_id", expense.tripId)
                put("wallet_id", expense.walletId)

                if (expense.categoryId != null) {
                    put("category_id", expense.categoryId)
                } else {
                    putNull("category_id")
                }

                put("description", expense.description)
                put("amount", expense.amount)
                put("paid_by", expense.paidBy)

                if (expense.date != null) {
                    put("date", expense.date)
                } else {
                    putNull("date")
                }

                if (expense.createdAt != null) {
                    put("created_at", expense.createdAt)
                } else {
                    putNull("created_at")
                }
            }

            db.update(
                "expenses",
                values,
                "id = ?",
                arrayOf(expense.id.toString())
            )
        }

    suspend fun deleteExpense(expense: Expense): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "expenses",
                "id = ?",
                arrayOf(expense.id.toString())
            )
        }

    suspend fun deleteByTrip(tripId: Long): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "expenses",
                "trip_id = ?",
                arrayOf(tripId.toString())
            )
        }

    suspend fun deleteAll(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "expenses",
                null,
                null
            )
        }

    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM expenses",
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

    suspend fun getTotalForTrip(tripId: Long): Double =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                """
                SELECT COALESCE(SUM(amount), 0)
                FROM expenses
                WHERE trip_id = ?
                """.trimIndent(),
                arrayOf(tripId.toString())
            )

            cursor.use {
                if (it.moveToFirst()) {
                    it.getDouble(0)
                } else {
                    0.0
                }
            }
        }

    private fun getNullableLong(
        cursor: android.database.Cursor,
        columnName: String
    ): Long? {

        val index = cursor.getColumnIndex(columnName)

        if (index < 0 || cursor.isNull(index)) {
            return null
        }

        return cursor.getLong(index)
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