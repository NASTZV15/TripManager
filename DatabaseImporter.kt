package com.tripmanager.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.tripmanager.data.database.DatabaseHelper
import java.io.File

class DatabaseImporter(
    private val context: Context
) {

    companion object {

        private const val TAG =
            "DatabaseImporter"

        private const val DATABASE_NAME =
            "trip_expenses.db"
    }

    fun importDatabase(
        uri: Uri
    ): Boolean {

        var testDatabase: SQLiteDatabase? = null
        var success = false

        try {

            Log.d(
                TAG,
                "Starting database import"
            )

            /*
             * Закрываем DatabaseHelper,
             * чтобы файл больше не был открыт.
             */
            DatabaseHelper.closeDatabase()

            val databaseFile =
                context.getDatabasePath(
                    DATABASE_NAME
                )

            databaseFile.parentFile?.mkdirs()

            /*
             * Сначала сохраняем новый файл
             * во временный файл.
             *
             * Это безопаснее, чем сразу
             * перезаписывать рабочую БД.
             */
            val temporaryFile = File(
                context.cacheDir,
                "trip_expenses_import.db"
            )

            if (temporaryFile.exists()) {
                temporaryFile.delete()
            }

            context.contentResolver
                .openInputStream(uri)
                ?.use { input ->

                    temporaryFile.outputStream()
                        .use { output ->

                            input.copyTo(output)
                        }

                } ?: run {

                Log.e(
                    TAG,
                    "Cannot open selected file"
                )

                return false
            }

            Log.d(
                TAG,
                "Temporary file size: ${temporaryFile.length()}"
            )

            if (
                !temporaryFile.exists() ||
                temporaryFile.length() == 0L
            ) {

                Log.e(
                    TAG,
                    "Imported file is empty"
                )

                temporaryFile.delete()

                return false
            }

            /*
             * Проверяем SQLite header.
             *
             * SQLite-файл должен начинаться
             * с:
             *
             * SQLite format 3
             */
            val header = ByteArray(16)

            temporaryFile.inputStream()
                .use { input ->

                    val read =
                        input.read(header)

                    if (read < 16) {

                        Log.e(
                            TAG,
                            "File is too small"
                        )

                        temporaryFile.delete()

                        return false
                    }
                }

            val sqliteHeader =
                String(
                    header,
                    Charsets.US_ASCII
                )

            Log.d(
                TAG,
                "SQLite header: $sqliteHeader"
            )

            if (
                !sqliteHeader.startsWith(
                    "SQLite format 3"
                )
            ) {

                Log.e(
                    TAG,
                    "Selected file is not SQLite database"
                )

                temporaryFile.delete()

                return false
            }

            /*
             * Проверяем, что SQLite действительно
             * может открыть файл.
             */
            testDatabase =
                SQLiteDatabase.openDatabase(
                    temporaryFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )

            val tables =
                testDatabase.rawQuery(
                    """
                SELECT name
                FROM sqlite_master
                WHERE type = 'table'
                ORDER BY name
                """.trimIndent(),
                    null
                )

            val tableNames =
                mutableListOf<String>()

            tables.use {

                while (it.moveToNext()) {

                    tableNames.add(
                        it.getString(0)
                    )
                }
            }

            Log.d(
                TAG,
                "Imported database tables: $tableNames"
            )

            if (
                !tableNames.contains("trips")
            ) {

                Log.e(
                    TAG,
                    "Database does not contain trips table"
                )

                testDatabase.close()
                testDatabase = null

                temporaryFile.delete()

                return false
            }

            testDatabase.close()
            testDatabase = null

            /*
             * Удаляем старую рабочую БД.
             */
            if (databaseFile.exists()) {

                if (!databaseFile.delete()) {

                    Log.e(
                        TAG,
                        "Cannot delete old database"
                    )

                    temporaryFile.delete()

                    return false
                }
            }

            /*
             * Перемещаем импортированный файл
             * на место рабочей БД.
             */
            temporaryFile.copyTo(
                databaseFile,
                overwrite = true
            )

            temporaryFile.delete()

            Log.d(
                TAG,
                "Database imported successfully"
            )

            Log.d(
                TAG,
                "New DB size: ${databaseFile.length()}"
            )

            /*
             * Проверяем уже рабочий файл.
             */
            testDatabase =
                SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )

            val cursor =
                testDatabase.rawQuery(
                    "SELECT COUNT(*) FROM trips",
                    null
                )

            var tripsCount = 0

            cursor.use {

                if (it.moveToFirst()) {
                    tripsCount = it.getInt(0)
                }
            }

            Log.d(
                TAG,
                "Imported trips count: $tripsCount"
            )

            testDatabase.close()
            testDatabase = null

            /*
             * DatabaseHelper будет открыт заново
             * только тогда, когда приложение
             * действительно обратится к БД.
             */
            success = true

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Database import error",
                e
            )

            success = false

        } finally {

            try {
                testDatabase?.close()
            } catch (_: Exception) {
            }
        }

        return success
    }
}