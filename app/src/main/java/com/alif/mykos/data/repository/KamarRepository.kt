package com.alif.mykos.data.repository

import com.alif.mykos.data.local.KamarDao
import com.alif.mykos.data.local.KamarEntity
import kotlinx.coroutines.flow.Flow

class KamarRepository(private val kamarDao: KamarDao) {

    val allKamar: Flow<List<KamarEntity>> = kamarDao.getAllKamar()

    fun observeKamarById(kamarId: Int): Flow<KamarEntity?> =
        kamarDao.observeKamarById(kamarId)

    suspend fun getKamarById(kamarId: Int): KamarEntity? =
        kamarDao.getKamarById(kamarId)

    suspend fun insertKamar(kamar: KamarEntity): Long =
        kamarDao.insertKamar(kamar)

    suspend fun updateKamar(kamar: KamarEntity) =
        kamarDao.updateKamar(kamar)

    suspend fun deleteKamar(kamar: KamarEntity) =
        kamarDao.deleteKamar(kamar)

    suspend fun deleteKamarById(kamarId: Int) =
        kamarDao.deleteKamarById(kamarId)
}