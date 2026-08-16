package com.tripmanager.data.database

import android.content.ContentValues
import android.content.Context
import com.tripmanager.data.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    suspend fun getAllCategories(): List<Category> =
        withContext(Dispatchers.IO) {

            val categories = mutableListOf<Category>()
            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "categories",
                arrayOf(
                    "id",
                    "name",
                    "icon",
                    "color"
                ),
                null,
                null,
                null,
                null,
                "name ASC"
            )

            cursor.use {
                while (it.moveToNext()) {

                    categories.add(
                        Category(
                            id = it.getLong(
                                it.getColumnIndexOrThrow("id")
                            ),
                            name = it.getString(
                                it.getColumnIndexOrThrow("name")
                            ),
                            icon = it.getStringOrDefault(
                                "icon",
                                "📌"
                            ),
                            color = it.getStringOrDefault(
                                "color",
                                "#3498db"
                            )
                        )
                    )
                }
            }

            categories
        }

    suspend fun getCategoryById(categoryId: Long): Category? =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.query(
                "categories",
                arrayOf(
                    "id",
                    "name",
                    "icon",
                    "color"
                ),
                "id = ?",
                arrayOf(categoryId.toString()),
                null,
                null,
                null,
                "1"
            )

            cursor.use {

                if (!it.moveToFirst()) {
                    return@withContext null
                }

                Category(
                    id = it.getLong(
                        it.getColumnIndexOrThrow("id")
                    ),
                    name = it.getString(
                        it.getColumnIndexOrThrow("name")
                    ),
                    icon = it.getStringOrDefault(
                        "icon",
                        "📌"
                    ),
                    color = it.getStringOrDefault(
                        "color",
                        "#3498db"
                    )
                )
            }
        }

    suspend fun insertCategory(category: Category): Long =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {

                if (category.id > 0) {
                    put("id", category.id)
                }

                put("name", category.name)
                put("icon", category.icon)
                put("color", category.color)
            }

            db.insertOrThrow(
                "categories",
                null,
                values
            )
        }

    suspend fun updateCategory(category: Category): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            val values = ContentValues().apply {
                put("name", category.name)
                put("icon", category.icon)
                put("color", category.color)
            }

            db.update(
                "categories",
                values,
                "id = ?",
                arrayOf(category.id.toString())
            )
        }

    suspend fun deleteCategory(category: Category): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "categories",
                "id = ?",
                arrayOf(category.id.toString())
            )
        }

    suspend fun deleteAll(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.writableDatabase

            db.delete(
                "categories",
                null,
                null
            )
        }

    suspend fun getCount(): Int =
        withContext(Dispatchers.IO) {

            val db = dbHelper.readableDatabase

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM categories",
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

    private fun android.database.Cursor.getStringOrDefault(
        columnName: String,
        defaultValue: String
    ): String {

        return getStringOrNull(columnName) ?: defaultValue
    }
}