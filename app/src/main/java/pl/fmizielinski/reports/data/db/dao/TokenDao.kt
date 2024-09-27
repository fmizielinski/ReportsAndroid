package pl.fmizielinski.reports.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import pl.fmizielinski.reports.data.db.model.TokenModel

@Dao
interface TokenDao {

    @Query("SELECT COUNT(*) > 0 FROM Token")
    suspend fun hasToken(): Boolean

    @Query("SELECT * FROM Token LIMIT 1")
    fun getTokenBlocking(): TokenModel

    /**
     * Adds a new user to the database. Removes old user if exists.
     */
    @Transaction
    suspend fun addToken(user: TokenModel): Boolean {
        deleteToken()
        return insertToken(user) > 0
    }

    @Insert
    suspend fun insertToken(user: TokenModel): Long

    @Query("DELETE FROM Token")
    suspend fun deleteToken(): Int
}
