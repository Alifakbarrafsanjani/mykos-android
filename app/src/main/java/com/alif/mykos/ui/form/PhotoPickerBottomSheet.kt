package com.alif.mykos.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.alif.mykos.databinding.BottomSheetPhotoPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PhotoPickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPhotoPickerBinding? = null
    private val binding get() = _binding!!

    private var onCameraClick: (() -> Unit)? = null
    private var onGalleryClick: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPhotoPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.optionKamera.setOnClickListener {
            dismiss()
            onCameraClick?.invoke()
        }

        binding.optionGaleri.setOnClickListener {
            dismiss()
            onGalleryClick?.invoke()
        }

        binding.btnPickerCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "PhotoPickerBottomSheet"

        fun show(
            fragmentManager: FragmentManager,
            onCameraSelected: () -> Unit,
            onGallerySelected: () -> Unit
        ) {
            if (fragmentManager.findFragmentByTag(TAG) != null) return

            PhotoPickerBottomSheet().apply {
                this.onCameraClick = onCameraSelected
                this.onGalleryClick = onGallerySelected
            }.show(fragmentManager, TAG)
        }
    }
}