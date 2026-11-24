package com.alif.mykos.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val rupiahFormat: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    fun formatRupiah(amount: Long): String {
        val formatted = rupiahFormat.format(amount)
        return formatted.replace("Rp", "Rp ").replace("Rp  ", "Rp ")
    }

    fun formatHargaPerBulan(amount: Long): String {
        return "${formatRupiah(amount)}/bulan"
    }

    fun formatWhatsAppNumber(rawPhone: String): String {
        val digitsOnly = rawPhone.filter { it.isDigit() }
        return when {
            digitsOnly.startsWith("0") -> "62${digitsOnly.substring(1)}"
            digitsOnly.startsWith("62") -> digitsOnly
            digitsOnly.startsWith("8") -> "62$digitsOnly"
            else -> digitsOnly
        }
    }
}