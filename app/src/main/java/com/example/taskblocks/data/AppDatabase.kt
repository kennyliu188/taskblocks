package com.example.taskblocks.data

import android.content.Context
import com.example.taskblocks.R
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.runBlocking

@Database(entities = [Task::class, Category::class, Record::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao
}

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "taskblocks_db"
            )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                .also { db ->
                    INSTANCE = db
                    runBlocking {
                        val dao = db.categoryDao()
                        if (dao.getAllSync().isEmpty()) {
                            val res = context.applicationContext.resources
                            listOf(
                                Category(name = res.getString(R.string.category_work), colorInt = 0xFF4CAF50.toInt(), sortOrder = 0),
                                Category(name = res.getString(R.string.category_life), colorInt = 0xFF2196F3.toInt(), sortOrder = 1),
                                Category(name = res.getString(R.string.category_study), colorInt = 0xFFFF9800.toInt(), sortOrder = 2),
                                Category(name = res.getString(R.string.category_other), colorInt = 0xFF9C27B0.toInt(), sortOrder = 3)
                            ).forEach { dao.insert(it) }
                        }
                    }
                }
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN completionStars INTEGER DEFAULT NULL")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE tasks ADD COLUMN deadlineAt INTEGER DEFAULT NULL")
        }
    }
}
