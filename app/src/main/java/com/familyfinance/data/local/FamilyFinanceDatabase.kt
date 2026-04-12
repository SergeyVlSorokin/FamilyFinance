package com.familyfinance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.familyfinance.data.local.converter.Converters
import com.familyfinance.data.local.dao.FinanceDao
import com.familyfinance.data.local.entity.AccountEntity
import com.familyfinance.data.local.entity.CategoryEntity
import com.familyfinance.data.local.entity.ProjectEntity
import com.familyfinance.data.local.entity.TransactionEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        ProjectEntity::class,
        TransactionEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
// @trace TASK-114, TASK-122, TASK-202
abstract class FamilyFinanceDatabase : RoomDatabase() {
    abstract val dao: FinanceDao

    companion object {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isReturnExpected INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN refundLinkedId TEXT")
            }
        }
    }
}
