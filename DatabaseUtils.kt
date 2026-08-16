package com.tripmanager.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.tripmanager.data.database.DatabaseHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DatabaseUtils {

    private const val TAG = "DatabaseUtils"
    private const val DATABASE_NAME = "trip_expenses.db"

    /**
     * Возвращает путь к рабочей SQLite БД.
     */
    private fun getDatabaseFile(context: Context): File {
        return context.getDatabasePath(DATABASE_NAME)
    }

    /**
     * Экспортирует настоящую SQLite БД во временный файл.
     *
     * Этот метод НЕ делает JSON и НЕ использует Room.
     */
    fun exportDatabase(context: Context): File? {

        return try {

            val dbFile = getDatabaseFile(context)

            Log.d(
                TAG,
                "Export source: ${dbFile.absolutePath}"
            )

            if (!dbFile.exists()) {

                Log.e(
                    TAG,
                    "Database file does not exist"
                )

                return null
            }

            if (dbFile.length() == 0L) {

                Log.e(
                    TAG,
                    "Database file is empty"
                )

                return null
            }

            /*
             * Закрываем DatabaseHelper перед копированием.
             *
             * Это важно, чтобы SQLite успел записать
             * все изменения на диск.
             */
            DatabaseHelper.closeDatabase()

            val timestamp =
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.getDefault()
                ).format(Date())

            val exportDirectory =
                context.getExternalFilesDir(null)
                    ?: context.filesDir

            if (!exportDirectory.exists()) {
                exportDirectory.mkdirs()
            }

            val exportFile = File(
                exportDirectory,
                "trip_expenses_backup_$timestamp.db"
            )

            FileInputStream(dbFile).use { input ->

                FileOutputStream(exportFile).use { output ->

                    input.copyTo(output)
                }
            }

            Log.d(
                TAG,
                "Database exported successfully"
            )

            Log.d(
                TAG,
                "Export file: ${exportFile.absolutePath}"
            )

            Log.d(
                TAG,
                "Export size: ${exportFile.length()} bytes"
            )

            if (
                exportFile.exists() &&
                exportFile.length() > 0L
            ) {

                exportFile

            } else {

                Log.e(
                    TAG,
                    "Export file was not created correctly"
                )

                null
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Export error",
                e
            )

            null
        }
    }

    /**
     * Сохраняет экспортированный .db файл
     * в место, выбранное пользователем.
     */
    fun saveDatabaseToUri(
        context: Context,
        sourceFile: File,
        uri: Uri
    ): Boolean {

        return try {

            if (
                !sourceFile.exists() ||
                sourceFile.length() == 0L
            ) {

                Log.e(
                    TAG,
                    "Source export file is empty"
                )

                return false
            }

            context.contentResolver
                .openOutputStream(uri)
                ?.use { output ->

                    FileInputStream(sourceFile)
                        .use { input ->

                            input.copyTo(output)
                        }

                } ?: run {

                Log.e(
                    TAG,
                    "Cannot open destination URI"
                )

                return false
            }

            Log.d(
                TAG,
                "Database saved to URI successfully"
            )

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "saveDatabaseToUri error",
                e
            )

            false
        }
    }

    /**
     * Проверяет, является ли файл настоящей SQLite БД.
     */
    fun isSQLiteDatabase(
        file: File
    ): Boolean {

        return try {

            if (
                !file.exists() ||
                file.length() < 16L
            ) {
                return false
            }

            val header = ByteArray(16)

            file.inputStream().use { input ->

                val read = input.read(header)

                if (read < 16) {
                    return false
                }
            }

            val headerText =
                String(
                    header,
                    Charsets.US_ASCII
                )

            Log.d(
                TAG,
                "SQLite header: $headerText"
            )

            headerText.startsWith(
                "SQLite format 3"
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "SQLite validation error",
                e
            )

            false
        }
    }

    /**
     * Проверяет структуру импортируемой БД.
     *
     * Нам важно убедиться, что это именно
     * база Trip Manager, а не произвольный .db файл.
     */
    fun validateDatabase(
        file: File
    ): Boolean {

        var database: SQLiteDatabase? = null

        return try {

            if (!isSQLiteDatabase(file)) {

                Log.e(
                    TAG,
                    "File is not SQLite database"
                )

                return false
            }

            database =
                SQLiteDatabase.openDatabase(
                    file.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )

            val cursor =
                database.rawQuery(
                    """
                    SELECT name
                    FROM sqlite_master
                    WHERE type = 'table'
                    """.trimIndent(),
                    null
                )

            val tables =
                mutableSetOf<String>()

            cursor.use {

                while (it.moveToNext()) {

                    tables.add(
                        it.getString(0)
                    )
                }
            }

            Log.d(
                TAG,
                "Database tables: $tables"
            )

            /*
             * Минимально необходимая таблица.
             */
            if (!tables.contains("trips")) {

                Log.e(
                    TAG,
                    "Required table 'trips' not found"
                )

                return false
            }

            /*
             * Проверяем остальные таблицы,
             * которые используются приложением.
             */
            val expectedTables = setOf(
                "trips",
                "wallets",
                "categories",
                "expenses",
                "trip_participants",
                "expense_participants",
                "transfers",
                "trip_plan",
                "packing_list",
                "accommodations",
                "flights"
            )

            val missingTables =
                expectedTables - tables

            if (missingTables.isNotEmpty()) {

                Log.w(
                    TAG,
                    "Missing tables: $missingTables"
                )

                /*
                 * Пока НЕ запрещаем импорт.
                 *
                 * Это позволит открыть базы,
                 * созданные старой версией ПК.
                 */
            }

            true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Database validation error",
                e
            )

            false

        } finally {

            try {
                database?.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Возвращает количество поездок в SQLite БД.
     */
    fun getTripsCount(
        file: File
    ): Int {

        var database: SQLiteDatabase? = null

        return try {

            database =
                SQLiteDatabase.openDatabase(
                    file.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )

            val cursor =
                database.rawQuery(
                    "SELECT COUNT(*) FROM trips",
                    null
                )

            var count = 0

            cursor.use {

                if (it.moveToFirst()) {
                    count = it.getInt(0)
                }
            }

            Log.d(
                TAG,
                "Trips count: $count"
            )

            count

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Cannot get trips count",
                e
            )

            -1

        } finally {

            try {
                database?.close()
            } catch (_: Exception) {
            }
        }
    }
}
