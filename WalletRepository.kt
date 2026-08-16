package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WalletRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    /**
     * Получить все кошельки конкретной поездки.
     */
    suspend fun getWalletsByTrip(
        tripId: Long
    ): List<Wallet> =
        withContext(Dispatchers.IO) {

            val wallets = mutableListOf<Wallet>()

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "wallets",
                arrayOf(
                    "id",
                    "trip_id",
                    "name",
                    "currency",
                    "balance",
                    "created_at",
                    "owner"
                ),
                "trip_id = ?",
                arrayOf(tripId.toString()),
                null,
                null,
                "id ASC"
            )

            cursor.use {

                while (it.moveToNext()) {

                    wallets.add(
                        Wallet(
                            id = it.getLong(
                                it.getColumnIndexOrThrow(
                                    "id"
                                )
                            ),

                            tripId =
                                it.getNullableLong(
                                    "trip_id"
                                ) ?: 0L,

                            name = it.getString(
                                it.getColumnIndexOrThrow(
                                    "name"
                                )
                            ),

                            currency =
                                it.getStringOrDefault(
                                    "currency",
                                    "RUB"
                                ),

                            balance =
                                it.getDoubleOrDefault(
                                    "balance",
                                    0.0
                                ),

                            owner =
                                it.getStringOrNull(
                                    "owner"
                                ),

                            createdAt =
                                it.getNullableLong(
                                    "created_at"
                                )
                                    ?: System.currentTimeMillis()
                        )
                    )
                }
            }

            wallets
        }

    /**
     * Получить кошелёк по ID.
     */
    suspend fun getWalletById(
        walletId: Long
    ): Wallet? =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "wallets",
                arrayOf(
                    "id",
                    "trip_id",
                    "name",
                    "currency",
                    "balance",
                    "created_at",
                    "owner"
                ),
                "id = ?",
                arrayOf(walletId.toString()),
                null,
                null,
                null,
                "1"
            )

            cursor.use {

                if (!it.moveToFirst()) {
                    return@withContext null
                }

                Wallet(
                    id = it.getLong(
                        it.getColumnIndexOrThrow(
                            "id"
                        )
                    ),

                    tripId =
                        it.getNullableLong(
                            "trip_id"
                        ) ?: 0L,

                    name = it.getString(
                        it.getColumnIndexOrThrow(
                            "name"
                        )
                    ),

                    currency =
                        it.getStringOrDefault(
                            "currency",
                            "RUB"
                        ),

                    balance =
                        it.getDoubleOrDefault(
                            "balance",
                            0.0
                        ),

                    owner =
                        it.getStringOrNull(
                            "owner"
                        ),

                    createdAt =
                        it.getNullableLong(
                            "created_at"
                        )
                            ?: System.currentTimeMillis()
                )
            }
        }

    /**
     * Добавить кошелёк.
     *
     * Если id == 0, SQLite создаёт ID самостоятельно.
     *
     * Если id > 0, сохраняем существующий ID.
     *
     * Второй вариант нужен для переноса данных между
     * ПК и Android без потери связей между таблицами.
     */
    suspend fun insertWallet(
        wallet: Wallet
    ): Long =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                if (wallet.id > 0) {
                    put(
                        "id",
                        wallet.id
                    )
                }

                put(
                    "trip_id",
                    wallet.tripId
                )

                put(
                    "name",
                    wallet.name
                )

                put(
                    "currency",
                    wallet.currency
                )

                put(
                    "balance",
                    wallet.balance
                )

                if (wallet.owner != null) {

                    put(
                        "owner",
                        wallet.owner
                    )

                } else {

                    putNull("owner")
                }

                put(
                    "created_at",
                    wallet.createdAt
                )
            }

            db.insertOrThrow(
                "wallets",
                null,
                values
            )
        }

    /**
     * Обновить кошелёк.
     */
    suspend fun updateWallet(
        wallet: Wallet
    ): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                put(
                    "trip_id",
                    wallet.tripId
                )

                put(
                    "name",
                    wallet.name
                )

                put(
                    "currency",
                    wallet.currency
                )

                put(
                    "balance",
                    wallet.balance
                )

                if (wallet.owner != null) {

                    put(
                        "owner",
                        wallet.owner
                    )

                } else {

                    putNull("owner")
                }

                put(
                    "created_at",
                    wallet.createdAt
                )
            }

            db.update(
                "wallets",
                values,
                "id = ?",
                arrayOf(
                    wallet.id.toString()
                )
            )
        }

    /**
     * Удалить кошелёк.
     */
    suspend fun deleteWallet(
        wallet: Wallet
    ): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "wallets",
                "id = ?",
                arrayOf(
                    wallet.id.toString()
                )
            )
        }

    /**
     * Удалить все кошельки.
     */
    suspend fun deleteAll(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "wallets",
                null,
                null
            )
        }

    /**
     * Удалить кошельки конкретной поездки.
     */
    suspend fun deleteByTrip(
        tripId: Long
    ): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "wallets",
                "trip_id = ?",
                arrayOf(
                    tripId.toString()
                )
            )
        }

    /**
     * Получить Long или null.
     */
    private fun android.database.Cursor.getNullableLong(
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
     * Получить String или null.
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

    /**
     * Получить String или значение по умолчанию.
     */
    private fun android.database.Cursor.getStringOrDefault(
        columnName: String,
        defaultValue: String
    ): String {

        return getStringOrNull(
            columnName
        ) ?: defaultValue
    }

    /**
     * Получить Double или значение по умолчанию.
     */
    private fun android.database.Cursor.getDoubleOrDefault(
        columnName: String,
        defaultValue: Double
    ): Double {

        val index =
            getColumnIndex(columnName)

        if (index < 0 || isNull(index)) {
            return defaultValue
        }

        return getDouble(index)
    }
}