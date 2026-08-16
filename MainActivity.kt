package com.tripmanager

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tripmanager.data.database.DatabaseHelper
import com.tripmanager.databinding.ActivityMainBinding
import com.tripmanager.utils.Constants
import com.tripmanager.utils.DatabaseImporter
import com.tripmanager.utils.DatabaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Экспорт:
     *
     * Сначала создаём настоящий .db файл,
     * затем Android показывает пользователю
     * системное окно выбора места сохранения.
     */
    private val exportLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            if (uri != null) {
                saveExportedDatabase(uri)
            }
        }

    /**
     * Импорт:
     *
     * Показываем только SQLite DB файлы.
     */
    private val importLauncher =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                importDatabaseFromUri(uri)
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        try {

            Log.d(
                Constants.TAG,
                "MainActivity: onCreate started"
            )

            binding =
                ActivityMainBinding.inflate(
                    layoutInflater
                )

            setContentView(binding.root)

            Log.d(
                Constants.TAG,
                "MainActivity: binding inflated"
            )

            /*
             * SQLite DatabaseHelper.
             *
             * Room здесь больше не используется.
             */
            try {

                val database =
                    DatabaseHelper.getInstance(this)

                Log.d(
                    Constants.TAG,
                    "SQLite database initialized successfully"
                )

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "SQLite database initialization error",
                    e
                )
            }

            val navView: BottomNavigationView =
                binding.navView

            val navController =
                findNavController(
                    R.id.nav_host_fragment_activity_main
                )

            setSupportActionBar(
                binding.toolbar
            )

            supportActionBar
                ?.setDisplayShowTitleEnabled(true)

            supportActionBar?.title =
                "Trip Manager"

            val appBarConfiguration =
                AppBarConfiguration(
                    setOf(
                        R.id.navigation_home,
                        R.id.navigation_trips,
                        R.id.navigation_expenses,
                        R.id.navigation_debts,
                        R.id.navigation_calendar
                    )
                )

            setupActionBarWithNavController(
                navController,
                appBarConfiguration
            )

            navView.setupWithNavController(
                navController
            )

            Log.d(
                Constants.TAG,
                "MainActivity: navigation setup complete"
            )

        } catch (e: Exception) {

            Log.e(
                Constants.TAG,
                "MainActivity: onCreate error",
                e
            )

            throw e
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu?
    ): Boolean {

        menuInflater.inflate(
            R.menu.main_menu,
            menu
        )

        return true
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {

        return when (item.itemId) {

            R.id.menu_export -> {

                exportDatabase()

                true
            }

            R.id.menu_import -> {

                importDatabase()

                true
            }

            else -> {

                super.onOptionsItemSelected(
                    item
                )
            }
        }
    }

    /**
     * Экспорт настоящего SQLite файла.
     */
    private fun exportDatabase() {

        Log.d(
            Constants.TAG,
            "exportDatabase: started"
        )

        lifecycleScope.launch {

            try {

                val exportFile =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.exportDatabase(
                            this@MainActivity
                        )
                    }

                if (
                    exportFile != null &&
                    exportFile.exists() &&
                    exportFile.length() > 0L
                ) {

                    Log.d(
                        Constants.TAG,
                        "Export file created: ${exportFile.absolutePath}"
                    )

                    Log.d(
                        Constants.TAG,
                        "Export size: ${exportFile.length()} bytes"
                    )

                    /*
                     * Просим пользователя выбрать,
                     * куда сохранить .db файл.
                     */
                    exportLauncher.launch(
                        exportFile.name
                    )

                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "❌ Не удалось создать базу для экспорта",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "exportDatabase: error",
                    e
                )

                Toast.makeText(
                    this@MainActivity,
                    "❌ Ошибка экспорта: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Сохраняем созданный .db файл
     * в выбранное пользователем место.
     */
    private fun saveExportedDatabase(
        uri: Uri
    ) {

        lifecycleScope.launch {

            try {

                /*
                 * Создаём новый экспортный файл.
                 *
                 * DatabaseUtils.exportDatabase()
                 * закрывает SQLite перед копированием.
                 */
                val exportFile =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.exportDatabase(
                            this@MainActivity
                        )
                    }

                if (exportFile == null) {

                    Toast.makeText(
                        this@MainActivity,
                        "❌ Не удалось подготовить БД",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                val success =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.saveDatabaseToUri(
                            this@MainActivity,
                            exportFile,
                            uri
                        )
                    }

                if (success) {

                    Toast.makeText(
                        this@MainActivity,
                        "✅ SQLite БД экспортирована",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.d(
                        Constants.TAG,
                        "Database saved successfully"
                    )

                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "❌ Ошибка сохранения БД",
                        Toast.LENGTH_LONG
                    ).show()
                }

                /*
                 * Временный экспортный файл
                 * больше не нужен.
                 */
                withContext(Dispatchers.IO) {

                    try {
                        exportFile.delete()
                    } catch (_: Exception) {
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "saveExportedDatabase: error",
                    e
                )

                Toast.makeText(
                    this@MainActivity,
                    "❌ Ошибка: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Открывает системный выбор файла.
     *
     * Теперь принимаем только SQLite .db.
     */
    private fun importDatabase() {

        Log.d(
            Constants.TAG,
            "importDatabase: started"
        )

        importLauncher.launch(
            arrayOf(
                "application/octet-stream",
                "application/x-sqlite3",
                "application/vnd.sqlite3",
                "*/*"
            )
        )
    }

    /**
     * Получаем выбранный пользователем .db файл.
     */
    private fun importDatabaseFromUri(
        uri: Uri
    ) {

        Log.d(
            Constants.TAG,
            "importDatabaseFromUri: $uri"
        )

        lifecycleScope.launch {

            try {

                /*
                 * Сначала копируем выбранный файл
                 * во временную директорию.
                 */
                val temporaryFile =
                    withContext(Dispatchers.IO) {

                        val file = File(
                            cacheDir,
                            "trip_expenses_import.db"
                        )

                        if (file.exists()) {
                            file.delete()
                        }

                        contentResolver
                            .openInputStream(uri)
                            ?.use { input ->

                                file.outputStream()
                                    .use { output ->

                                        input.copyTo(
                                            output
                                        )
                                    }

                            } ?: throw Exception(
                            "Не удалось открыть выбранный файл"
                        )

                        file
                    }

                Log.d(
                    Constants.TAG,
                    "Temporary import file: ${temporaryFile.absolutePath}"
                )

                Log.d(
                    Constants.TAG,
                    "Temporary import size: ${temporaryFile.length()}"
                )

                /*
                 * Проверяем SQLite header.
                 */
                val isSQLite =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.isSQLiteDatabase(
                            temporaryFile
                        )
                    }

                if (!isSQLite) {

                    temporaryFile.delete()

                    Toast.makeText(
                        this@MainActivity,
                        "❌ Выбранный файл не является SQLite БД",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                /*
                 * Проверяем структуру.
                 */
                val isValid =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.validateDatabase(
                            temporaryFile
                        )
                    }

                if (!isValid) {

                    temporaryFile.delete()

                    Toast.makeText(
                        this@MainActivity,
                        "❌ База данных повреждена или не содержит таблицу trips",
                        Toast.LENGTH_LONG
                    ).show()

                    return@launch
                }

                val tripsCount =
                    withContext(Dispatchers.IO) {

                        DatabaseUtils.getTripsCount(
                            temporaryFile
                        )
                    }

                Log.d(
                    Constants.TAG,
                    "Imported DB trips count: $tripsCount"
                )

                /*
                 * Теперь заменяем рабочую БД.
                 */
                val importer =
                    DatabaseImporter(
                        this@MainActivity
                    )

                val success =
                    withContext(Dispatchers.IO) {

                        importer.importDatabase(
                            uri
                        )
                    }

                temporaryFile.delete()

                if (success) {

                    Toast.makeText(
                        this@MainActivity,
                        if (tripsCount >= 0) {
                            "✅ БД импортирована. Поездок: $tripsCount"
                        } else {
                            "✅ БД импортирована"
                        },
                        Toast.LENGTH_LONG
                    ).show()

                    /*
                     * Перезапускаем приложение,
                     * чтобы все экраны заново открыли
                     * новую SQLite БД.
                     */
                    restartApplication()

                } else {

                    Toast.makeText(
                        this@MainActivity,
                        "❌ Не удалось импортировать БД",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {

                Log.e(
                    Constants.TAG,
                    "importDatabaseFromUri: error",
                    e
                )

                Toast.makeText(
                    this@MainActivity,
                    "❌ Ошибка импорта: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Полностью перезапускает приложение.
     */
    private fun restartApplication() {

        try {

            Log.d(
                Constants.TAG,
                "Restarting application..."
            )

            DatabaseHelper.closeDatabase()

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()

        } catch (e: Exception) {

            Log.e(
                Constants.TAG,
                "restartApplication error",
                e
            )
        }
    }

    override fun onSupportNavigateUp():
            Boolean {

        val navController =
            findNavController(
                R.id.nav_host_fragment_activity_main
            )

        return navController.navigateUp() ||
                super.onSupportNavigateUp()
    }
}