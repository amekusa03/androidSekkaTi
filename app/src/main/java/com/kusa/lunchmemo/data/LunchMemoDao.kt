package com.kusa.sekkati.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SekkaTiDao {
    @Query("SELECT * FROM lunch_memos")
    fun getAllMemos(): Flow<List<SekkaTiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: SekkaTiEntity)

    @Query("DELETE FROM lunch_memos WHERE date < :date")
    suspend fun deleteMemosOlderThan(date: String)

    @Query("DELETE FROM lunch_memos WHERE date BETWEEN :startDate AND :endDate")
    suspend fun deleteMemosInRange(startDate: String, endDate: String)

    @Query("DELETE FROM lunch_memos WHERE date = :date")
    suspend fun deleteMemoByDate(date: String)
}
