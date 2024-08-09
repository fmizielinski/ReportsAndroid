package pl.fmizielinski.reports.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pl.fmizielinski.reports.data.db.model.UserModel

@Dao
interface UserDao {

    @Query("SELECT * FROM User LIMIT 1")
    fun getUser(): Flow<UserModel>

    /**
     * Adds a new user to the database. Removes old user if exists.
     */
    @Transaction
    suspend fun addUser(user: UserModel): Boolean {
        deleteUser()
        return insertUser(user) > 0
    }

    @Insert
    suspend fun insertUser(user: UserModel): Long

    @Query("DELETE FROM User")
    suspend fun deleteUser(): Int
}
