package com.bigbrother.app.data.local

import android.content.Context
import androidx.room.Room
import com.bigbrother.app.data.local.dao.BalanceDao
import com.bigbrother.app.data.local.dao.FocusSessionDao
import com.bigbrother.app.data.local.dao.TrackedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bigbrother.db")
            .addMigrations(*DatabaseMigrations.ALL)
            .build()

    @Provides
    fun provideBalanceDao(database: AppDatabase): BalanceDao = database.balanceDao()

    @Provides
    fun provideTrackedAppDao(database: AppDatabase): TrackedAppDao = database.trackedAppDao()

    @Provides
    fun provideFocusSessionDao(database: AppDatabase): FocusSessionDao = database.focusSessionDao()
}
