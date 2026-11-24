package com.alif.mykos.ui.form

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.alif.mykos.data.local.AppDatabase
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.data.repository.KamarRepository
import com.alif.mykos.utils.ImageStorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FormKamarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KamarRepository

    init {
        val dao = AppDatabase.getInstance(application).kamarDao()
        repository = KamarRepository(dao)
    }

    suspend fun getKamarById(kamarId: Int): KamarEntity? {
        if (kamarId <= 0) return null
        return repository.getKamarById(kamarId)
    }

    suspend fun saveKamar(
        existingKamar: KamarEntity?,
        nomorKamar: String,
        hargaSewa: Long,
        fasilitas: String,
        status: String,
        selectedPhotoUri: Uri?,
        namaPenghuni: String?,
        noHpPenghuni: String?,
        tanggalMasuk: String?
    ) = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val oldPhotoUri = existingKamar?.fotoUri

        val finalPhotoUriString: String? = when {
            selectedPhotoUri == null -> null
            selectedPhotoUri.toString() == oldPhotoUri -> oldPhotoUri
            else -> {
                val savedUri = ImageStorageHelper.copyUriToInternalStorage(context, selectedPhotoUri)
                if (savedUri != null && !oldPhotoUri.isNullOrBlank() && oldPhotoUri != savedUri) {
                    ImageStorageHelper.deleteInternalPhoto(oldPhotoUri)
                }
                savedUri ?: oldPhotoUri
            }
        }

        val isTerisi = status.equals(KamarEntity.STATUS_TERISI, ignoreCase = true)

        val kamarEntity = KamarEntity(
            id = existingKamar?.id ?: 0,
            nomorKamar = nomorKamar.trim(),
            hargaSewa = hargaSewa,
            fasilitas = fasilitas,
            status = if (isTerisi) KamarEntity.STATUS_TERISI else KamarEntity.STATUS_KOSONG,
            fotoUri = finalPhotoUriString,
            namaPenghuni = if (isTerisi) namaPenghuni?.trim() else null,
            noHpPenghuni = if (isTerisi) noHpPenghuni?.trim() else null,
            tanggalMasuk = if (isTerisi) tanggalMasuk?.trim() else null
        )

        if (existingKamar == null) {
            repository.insertKamar(kamarEntity)
        } else {
            repository.updateKamar(kamarEntity)
        }
    }
}