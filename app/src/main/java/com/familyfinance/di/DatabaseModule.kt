package com.familyfinance.di

import android.content.Context
import androidx.room.Room
import com.familyfinance.data.local.FamilyFinanceDatabase
import com.familyfinance.data.local.dao.FinanceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FamilyFinanceDatabase {
        return Room.databaseBuilder(
            context,
            FamilyFinanceDatabase::class.java,
            "family_finance.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideFinanceDao(db: FamilyFinanceDatabase): FinanceDao {
        return db.dao
    }
}
