package com.alif.mykos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alif.mykos.utils.RentReminderHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RentReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                RentReminderHelper.checkAndNotifyDueRooms(context, sendDemoIfNone = false)
            } finally {
                pendingResult.finish()
            }
        }
    }
}