package com.example.kdyaimap.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kdyaimap.core.model.UserHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface UserHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: UserHistory)

    @Query("SELECT * FROM user_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryByUserId(userId: Long): Flow<List<UserHistory>>
}