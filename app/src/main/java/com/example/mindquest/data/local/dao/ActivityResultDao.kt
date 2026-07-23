package com.example.mindquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mindquest.data.local.entity.ActivityResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityResultDao {
    @Insert
    suspend fun insert(result: ActivityResultEntity)

    @Query("SELECT * FROM activity_results ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ActivityResultEntity>>

    @Query("SELECT * FROM activity_results ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ActivityResultEntity>>

    @Query("DELETE FROM activity_results")
    suspend fun clearAll()
}
