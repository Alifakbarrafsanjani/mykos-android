package com.alif.mykos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kamar")
data class KamarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nomorKamar: String,
    val hargaSewa: Long,
    val fasilitas: String,
    val status: String,
    val fotoUri: String? = null,
    val namaPenghuni: String? = null,
    val noHpPenghuni: String? = null,
    val tanggalMasuk: String? = null
) {
    companion object {
        const val STATUS_TERISI = "Terisi"
        const val STATUS_KOSONG = "Kosong"
    }

    fun isTerisi(): Boolean = status.equals(STATUS_TERISI, ignoreCase = true)

    fun getFasilitasList(): List<String> {
        if (fasilitas.isBlank()) return emptyList()
        return fasilitas.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}