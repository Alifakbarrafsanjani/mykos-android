package com.alif.mykos.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.alif.mykos.data.local.AppDatabase
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.data.repository.KamarRepository
import com.alif.mykos.utils.ImageStorageHelper
import kotlinx.coroutines.flow.Flow

class DetailKamarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KamarRepository

    init {
        val dao = AppDatabase.getInstance(application).kamarDao()
        repository = KamarRepository(dao)
    }

    fun observeKamar(kamarId: Int): Flow<KamarEntity?> {
        return repository.observeKamarById(kamarId)
    }

    suspend fun deleteKamar(kamar: KamarEntity) {
        ImageStorageHelper.deleteInternalPhoto(kamar.fotoUri)
        repository.deleteKamar(kamar)
    }
}