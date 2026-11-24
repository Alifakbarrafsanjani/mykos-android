package com.alif.mykos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KamarDao {

    @Query("SELECT * FROM kamar ORDER BY id DESC")
    fun getAllKamar(): Flow<List<KamarEntity>>

    @Query("SELECT * FROM kamar WHERE status = 'Terisi' ORDER BY nomorKamar ASC")
    suspend fun getOccupiedKamarSync(): List<KamarEntity>

    @Query("SELECT * FROM kamar WHERE id = :kamarId LIMIT 1")
    fun observeKamarById(kamarId: Int): Flow<KamarEntity?>

    @Query("SELECT * FROM kamar WHERE id = :kamarId LIMIT 1")
    suspend fun getKamarById(kamarId: Int): KamarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKamar(kamar: KamarEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKamar(kamarList: List<KamarEntity>)

    @Update
    suspend fun updateKamar(kamar: KamarEntity)

    @Delete
    suspend fun deleteKamar(kamar: KamarEntity)

    @Query("DELETE FROM kamar WHERE id = :kamarId")
    suspend fun deleteKamarById(kamarId: Int)
}