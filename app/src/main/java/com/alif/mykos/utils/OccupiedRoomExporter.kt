package com.alif.mykos.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.alif.mykos.data.local.KamarEntity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OccupiedRoomExporter {

    fun getInitialSampleRooms(): List<KamarEntity> = listOf(
        // ==================== LANTAI 1 ====================
        KamarEntity(
            nomorKamar = "Kamar 101",
            hargaSewa = 850_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Budi Santoso",
            noHpPenghuni = "081234567801",
            tanggalMasuk = "01 Oktober 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 102",
            hargaSewa = 750_000L,
            fasilitas = "Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Siti Rahmawati",
            noHpPenghuni = "081234567802",
            tanggalMasuk = "15 Oktober 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 103",
            hargaSewa = 650_000L,
            fasilitas = "Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 104",
            hargaSewa = 1_000_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Rizky Pratama",
            noHpPenghuni = "081234567804",
            tanggalMasuk = "01 November 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 105",
            hargaSewa = 700_000L,
            fasilitas = "Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 106",
            hargaSewa = 900_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Nadia Putri",
            noHpPenghuni = "081234567806",
            tanggalMasuk = "05 November 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 107",
            hargaSewa = 600_000L,
            fasilitas = "Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 108",
            hargaSewa = 1_100_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Dimas Saputra",
            noHpPenghuni = "081234567808",
            tanggalMasuk = "10 November 2026"
        ),

        // ==================== LANTAI 2 ====================
        KamarEntity(
            nomorKamar = "Kamar 201",
            hargaSewa = 950_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Aulia Rahman",
            noHpPenghuni = "081234567809",
            tanggalMasuk = "12 Oktober 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 202",
            hargaSewa = 800_000L,
            fasilitas = "Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Fajar Nugroho",
            noHpPenghuni = "081234567810",
            tanggalMasuk = "20 Oktober 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 203",
            hargaSewa = 650_000L,
            fasilitas = "Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 204",
            hargaSewa = 1_150_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Putri Maharani",
            noHpPenghuni = "081234567812",
            tanggalMasuk = "02 November 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 205",
            hargaSewa = 750_000L,
            fasilitas = "Kamar Mandi Dalam, Wi-Fi, Kasur, Meja Belajar",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 206",
            hargaSewa = 900_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Hendra Wijaya",
            noHpPenghuni = "081234567814",
            tanggalMasuk = "08 November 2026"
        ),
        KamarEntity(
            nomorKamar = "Kamar 207",
            hargaSewa = 600_000L,
            fasilitas = "Wi-Fi, Kasur, Meja Belajar",
            status = KamarEntity.STATUS_KOSONG
        ),
        KamarEntity(
            nomorKamar = "Kamar 208",
            hargaSewa = 1_200_000L,
            fasilitas = "AC, Kamar Mandi Dalam, Wi-Fi, Kasur, Lemari, Meja Belajar",
            status = KamarEntity.STATUS_TERISI,
            namaPenghuni = "Clarissa Amanda",
            noHpPenghuni = "081234567816",
            tanggalMasuk = "14 November 2026"
        )
    )

    fun createDefaultFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        return "Laporan_Kamar_Terisi_MyKos_$stamp.pdf"
    }

    fun createSingleRoomFileName(nomorKamar: String): String {
        val safeName = nomorKamar.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        return "Data_${safeName}_MyKos_$stamp.pdf"
    }

    fun generateShareableSingleRoomPdfUri(
        context: Context,
        kamar: KamarEntity
    ): Uri? {
        return try {
            val sharedDir = File(context.cacheDir, "shared_pdfs").apply {
                if (!exists()) mkdirs()
            }
            sharedDir.listFiles()?.forEach { it.delete() }

            val pdfFile = File(sharedDir, createSingleRoomFileName(kamar.nomorKamar))
            val isWritten = FileOutputStream(pdfFile).use { outputStream ->
                writeSingleRoomPdfToStream(context, outputStream, kamar)
            }

            if (isWritten) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportSingleRoomToPdf(
        context: Context,
        destinationUri: Uri,
        kamar: KamarEntity
    ): Boolean {
        return try {
            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                writeSingleRoomPdfToStream(context, outputStream, kamar)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeSingleRoomPdfToStream(
        context: Context,
        outputStream: OutputStream,
        kamar: KamarEntity
    ): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 44f

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 11f
            }
            val sectionPaint = Paint().apply {
                color = Color.parseColor("#2563EB")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val labelPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 10.5f
            }
            val valuePaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 12.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val boxFillPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            var y = 54f

            val tanggalCetak = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                .format(Date())

            canvas.drawText("DETAIL DATA KAMAR & PENGHUNI", margin, y, titlePaint)
            y += 18f
            canvas.drawText("Aplikasi Manajemen Kos MyKos  •  Dicetak: $tanggalCetak WIB", margin, y, subtitlePaint)
            y += 22f

            val bitmap = loadRoomBitmap(context, kamar.fotoUri)
            if (bitmap != null) {
                val targetWidth = pageWidth - (margin * 2)
                val targetHeight = 200f
                val destRect = RectF(margin, y, margin + targetWidth, y + targetHeight)
                canvas.drawBitmap(bitmap, null, destRect, null)
                canvas.drawRect(destRect, borderPaint)
                bitmap.recycle()
                y += targetHeight + 22f
            }

            val box1Top = y
            val box1Height = 148f
            canvas.drawRoundRect(
                RectF(margin, box1Top, pageWidth - margin, box1Top + box1Height),
                10f, 10f, boxFillPaint
            )
            canvas.drawRoundRect(
                RectF(margin, box1Top, pageWidth - margin, box1Top + box1Height),
                10f, 10f, borderPaint
            )

            var innerY = box1Top + 24f
            canvas.drawText("INFORMASI KAMAR", margin + 16f, innerY, sectionPaint)
            innerY += 22f

            canvas.drawText("Nomor / Nama Kamar", margin + 16f, innerY, labelPaint)
            canvas.drawText("Status Kamar", margin + 280f, innerY, labelPaint)
            innerY += 16f
            canvas.drawText(kamar.nomorKamar, margin + 16f, innerY, valuePaint)
            canvas.drawText(kamar.status, margin + 280f, innerY, valuePaint)
            innerY += 24f

            canvas.drawText("Harga Sewa per Bulan", margin + 16f, innerY, labelPaint)
            innerY += 16f
            canvas.drawText(CurrencyFormatter.formatHargaPerBulan(kamar.hargaSewa), margin + 16f, innerY, valuePaint)
            innerY += 24f

            val fasilitas = kamar.fasilitas.takeIf { it.isNotBlank() } ?: "Standar"
            canvas.drawText("Fasilitas: $fasilitas", margin + 16f, innerY, subtitlePaint)

            y = box1Top + box1Height + 20f

            val box2Top = y
            val box2Height = 148f
            canvas.drawRoundRect(
                RectF(margin, box2Top, pageWidth - margin, box2Top + box2Height),
                10f, 10f, boxFillPaint
            )
            canvas.drawRoundRect(
                RectF(margin, box2Top, pageWidth - margin, box2Top + box2Height),
                10f, 10f, borderPaint
            )

            var pY = box2Top + 24f
            canvas.drawText("INFORMASI PENGHUNI", margin + 16f, pY, sectionPaint)
            pY += 22f

            val namaPenghuni = kamar.namaPenghuni?.takeIf { it.isNotBlank() } ?: "-"
            val noHp = kamar.noHpPenghuni?.takeIf { it.isNotBlank() } ?: "-"
            val tglMasuk = kamar.tanggalMasuk?.takeIf { it.isNotBlank() } ?: "-"

            canvas.drawText("Nama Lengkap Penghuni", margin + 16f, pY, labelPaint)
            pY += 16f
            canvas.drawText(namaPenghuni, margin + 16f, pY, valuePaint)
            pY += 24f

            canvas.drawText("Nomor HP / WhatsApp", margin + 16f, pY, labelPaint)
            canvas.drawText("Tanggal Masuk", margin + 280f, pY, labelPaint)
            pY += 16f
            canvas.drawText(noHp, margin + 16f, pY, valuePaint)
            canvas.drawText(tglMasuk, margin + 280f, pY, valuePaint)

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadRoomBitmap(context: Context, fotoUriString: String?): Bitmap? {
        if (fotoUriString.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(fotoUriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun exportOccupiedRoomsToPdf(
        context: Context,
        destinationUri: Uri,
        occupiedRooms: List<KamarEntity>
    ): Boolean {
        return try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40f

            val titlePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#475569")
                textSize = 11f
            }
            val sectionPaint = Paint().apply {
                color = Color.parseColor("#2563EB")
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyBoldPaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val bodyPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 10.5f
            }
            val linePaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 1f
            }
            val boxFillPaint = Paint().apply {
                color = Color.parseColor("#F1F5F9")
                style = Paint.Style.FILL
            }

            val totalPendapatan = occupiedRooms.sumOf { it.hargaSewa }
            val rupiahFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))
            val tanggalCetak = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                .format(Date())

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas: Canvas = page.canvas
            var y = 48f

            canvas.drawText("LAPORAN DATA KAMAR TERISI — MYKOS", margin, y, titlePaint)
            y += 18f
            canvas.drawText("Dicetak pada: $tanggalCetak WIB", margin, y, subtitlePaint)
            y += 18f

            canvas.drawRect(margin, y, pageWidth - margin, y + 48f, boxFillPaint)
            canvas.drawText(
                "Total Kamar Terisi: ${occupiedRooms.size} Kamar",
                margin + 12f,
                y + 19f,
                bodyBoldPaint
            )
            canvas.drawText(
                "Estimasi Total Pendapatan Sewa: Rp ${rupiahFormat.format(totalPendapatan)} / bulan",
                margin + 12f,
                y + 37f,
                sectionPaint
            )
            y += 68f

            occupiedRooms.forEachIndexed { index, kamar ->
                if (y > pageHeight - 130f) {
                    canvas.drawText(
                        "Halaman $pageNumber",
                        pageWidth - margin - 55f,
                        pageHeight - 24f,
                        subtitlePaint
                    )
                    pdfDocument.finishPage(page)

                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 48f
                }

                val hargaText = CurrencyFormatter.formatHargaPerBulan(kamar.hargaSewa)
                canvas.drawText(
                    "${index + 1}. ${kamar.nomorKamar}  •  $hargaText",
                    margin,
                    y,
                    bodyBoldPaint
                )
                y += 16f

                val penghuni = kamar.namaPenghuni?.takeIf { it.isNotBlank() } ?: "-"
                val noHp = kamar.noHpPenghuni?.takeIf { it.isNotBlank() } ?: "-"
                val tglMasuk = kamar.tanggalMasuk?.takeIf { it.isNotBlank() } ?: "-"
                val fasilitas = kamar.fasilitas.takeIf { it.isNotBlank() } ?: "Standar"

                canvas.drawText(
                    "    Penghuni: $penghuni   |   No. HP/WA: $noHp",
                    margin,
                    y,
                    bodyPaint
                )
                y += 15f
                canvas.drawText(
                    "    Tanggal Masuk: $tglMasuk",
                    margin,
                    y,
                    bodyPaint
                )
                y += 15f
                canvas.drawText(
                    "    Fasilitas: $fasilitas",
                    margin,
                    y,
                    bodyPaint
                )
                y += 12f
                canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
                y += 18f
            }

            canvas.drawText(
                "Halaman $pageNumber",
                pageWidth - margin - 55f,
                pageHeight - 24f,
                subtitlePaint
            )
            pdfDocument.finishPage(page)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            } ?: run {
                pdfDocument.close()
                return false
            }

            pdfDocument.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}