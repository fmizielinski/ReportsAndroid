package pl.fmizielinski.reports.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "User")
data class UserModel(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "token") val token: String,
)

