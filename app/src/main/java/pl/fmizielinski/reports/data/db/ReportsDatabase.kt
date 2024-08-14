package pl.fmizielinski.reports.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.fmizielinski.reports.data.db.dao.TokenDao
import pl.fmizielinski.reports.data.db.model.TokenModel

@Database(
    entities = [TokenModel::class],
    version = 1,
    exportSchema = false,
)
abstract class ReportsDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao

    companion object {
        const val NAME = "reports_db"
    }
}
