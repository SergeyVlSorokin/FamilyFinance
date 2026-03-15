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

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        ProjectEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FamilyFinanceDatabase : RoomDatabase() {
    abstract val dao: FinanceDao
}
