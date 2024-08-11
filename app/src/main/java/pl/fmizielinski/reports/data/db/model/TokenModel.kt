package pl.fmizielinski.reports.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Token")
data class TokenModel(
    @PrimaryKey val id: Int? = null,
    @ColumnInfo(name = "token") val token: String,
)
