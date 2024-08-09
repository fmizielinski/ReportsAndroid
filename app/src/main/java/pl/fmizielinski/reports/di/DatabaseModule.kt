package pl.fmizielinski.reports.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import pl.fmizielinski.reports.BuildConfig
import pl.fmizielinski.reports.data.db.ReportsDatabase
import pl.fmizielinski.reports.data.db.dao.UserDao
import timber.log.Timber

@Module
class DatabaseModule {

    @Single
    fun reportsDatabase(application: Application): ReportsDatabase = Room.databaseBuilder(
        application,
        ReportsDatabase::class.java,
        ReportsDatabase.NAME,
    )
        .apply {
            if (BuildConfig.DEBUG) {
                setQueryCallback(
                    queryCallback = { sqlQuery, bindArgs ->
                        Timber.d("SQL Query: $sqlQuery")
                        Timber.d("Bind Args: $bindArgs")
                    },
                    executor = { it.run() },
                )
            }
        }
        .build()

    @Single
    fun userDao(database: ReportsDatabase): UserDao = database.userDao()
}
