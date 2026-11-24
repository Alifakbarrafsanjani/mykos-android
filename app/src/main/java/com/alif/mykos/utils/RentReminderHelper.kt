package com.alif.mykos.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alif.mykos.MainActivity
import com.alif.mykos.R
import com.alif.mykos.data.local.AppDatabase
import com.alif.mykos.data.local.KamarEntity
import com.alif.mykos.receiver.RentReminderReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object RentReminderHelper {

    const val CHANNEL_ID = "mykos_rent_reminder_channel"
    private const val ALARM_REQUEST_CODE = 2026

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RentReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    suspend fun checkAndNotifyDueRooms(context: Context, sendDemoIfNone: Boolean = false): Int {
        if (!hasNotificationPermission(context)) return 0
        createNotificationChannel(context)

        val occupiedRooms = AppDatabase.getInstance(context)
            .kamarDao()
            .getOccupiedKamarSync()

        var notifiedCount = 0

        for (kamar in occupiedRooms) {
            val daysLeft = calculateDaysUntilNextDue(kamar.tanggalMasuk) ?: continue
            if (daysLeft in 0..3) {
                val nama = kamar.namaPenghuni?.takeIf { it.isNotBlank() } ?: "Penghuni"
                val harga = CurrencyFormatter.formatRupiah(kamar.hargaSewa)
                val statusWaktu = if (daysLeft == 0) "jatuh tempo HARI INI" else "jatuh tempo H-$daysLeft"

                showNotification(
                    context = context,
                    notificationId = 1000 + kamar.id,
                    title = "Pengingat Sewa ${kamar.nomorKamar}",
                    message = "Sewa a.n. $nama ($harga) $statusWaktu."
                )
                notifiedCount++
            }
        }

        if (notifiedCount == 0 && sendDemoIfNone) {
            val sampleRoom = occupiedRooms.firstOrNull()
            if (sampleRoom != null) {
                val nama = sampleRoom.namaPenghuni?.takeIf { it.isNotBlank() } ?: "Penghuni"
                val harga = CurrencyFormatter.formatRupiah(sampleRoom.hargaSewa)
                showNotification(
                    context = context,
                    notificationId = 9001,
                    title = "Pengingat Sewa ${sampleRoom.nomorKamar}",
                    message = "Pengingat aktif: Tagihan sewa $nama sebesar $harga dipantau otomatis."
                )
            } else {
                showNotification(
                    context = context,
                    notificationId = 9001,
                    title = "Notifikasi MyKos Aktif",
                    message = "Sistem pengingat jatuh tempo sewa bulanan siap digunakan."
                )
            }
        }

        return notifiedCount
    }

    fun notifyRoomSaved(
        context: Context,
        nomorKamar: String,
        status: String,
        namaPenghuni: String?
    ) {
        if (!hasNotificationPermission(context)) return
        createNotificationChannel(context)

        val detail = if (status.equals(KamarEntity.STATUS_TERISI, ignoreCase = true) && !namaPenghuni.isNullOrBlank()) {
            "$nomorKamar berhasil ditambahkan (Terisi • $namaPenghuni)."
        } else {
            "$nomorKamar berhasil ditambahkan dengan status $status."
        }

        showNotification(
            context = context,
            notificationId = (System.currentTimeMillis() % 10000).toInt(),
            title = "Kamar Baru Ditambahkan",
            message = detail
        )
    }

    private fun calculateDaysUntilNextDue(tanggalMasuk: String?): Int? {
        if (tanggalMasuk.isNullOrBlank()) return null
        return try {
            val formatter = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            val parsedDate = formatter.parse(tanggalMasuk) ?: return null

            val masukCal = Calendar.getInstance().apply { time = parsedDate }
            val dueDay = masukCal.get(Calendar.DAY_OF_MONTH)

            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val nextDueCal = (todayCal.clone() as Calendar).apply {
                val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, minOf(dueDay, maxDay))
            }

            if (nextDueCal.before(todayCal)) {
                nextDueCal.add(Calendar.MONTH, 1)
                val maxDayNext = nextDueCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                nextDueCal.set(Calendar.DAY_OF_MONTH, minOf(dueDay, maxDayNext))
            }

            val diffMillis = nextDueCal.timeInMillis - todayCal.timeInMillis
            (diffMillis / (1000L * 60 * 60 * 24)).toInt()
        } catch (_: Exception) {
            null
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission(context)) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_home_kos)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}