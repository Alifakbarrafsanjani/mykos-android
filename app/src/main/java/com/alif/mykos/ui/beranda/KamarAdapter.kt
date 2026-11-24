package com.alif.mykos.ui.beranda

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alif.mykos.R
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.databinding.ItemKamarBinding
import com.alif.mykos.utils.CurrencyFormatter

class KamarAdapter(
    private val onItemClick: (KamarEntity) -> Unit
) : ListAdapter<KamarEntity, KamarAdapter.KamarViewHolder>(KamarDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KamarViewHolder {
        val binding = ItemKamarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KamarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KamarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KamarViewHolder(
        private val binding: ItemKamarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kamar: KamarEntity) {
            val context = binding.root.context

            binding.tvNomorKamar.text = kamar.nomorKamar
            binding.tvHargaKamar.text = CurrencyFormatter.formatHargaPerBulan(kamar.hargaSewa)

            if (kamar.isTerisi()) {
                binding.tvStatusBadge.text = context.getString(R.string.status_terisi)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_terisi)
                binding.tvStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.on_success_container)
                )
            } else {
                binding.tvStatusBadge.text = context.getString(R.string.status_kosong)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_kosong)
                binding.tvStatusBadge.setTextColor(
                    ContextCompat.getColor(context, R.color.on_neutral_container)
                )
            }

            if (!kamar.fotoUri.isNullOrBlank()) {
                binding.ivKamarFoto.load(Uri.parse(kamar.fotoUri)) {
                    crossfade(true)
                    placeholder(R.drawable.ic_image_placeholder)
                    error(R.drawable.ic_image_placeholder)
                }
            } else {
                binding.ivKamarFoto.setImageResource(R.drawable.ic_image_placeholder)
            }

            binding.root.setOnClickListener {
                onItemClick(kamar)
            }
        }
    }

    companion object {
        private val KamarDiffCallback = object : DiffUtil.ItemCallback<KamarEntity>() {
            override fun areItemsTheSame(oldItem: KamarEntity, newItem: KamarEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: KamarEntity, newItem: KamarEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}