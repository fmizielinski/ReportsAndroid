package pl.fmizielinski.reports.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.fmizielinski.reports.data.db.dao.UserDao
import pl.fmizielinski.reports.data.db.model.UserModel

@Database(
    entities = [UserModel::class],
    version = 1,
    exportSchema = false,
)
abstract class ReportsDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        const val NAME = "reports_db"
    }
}
