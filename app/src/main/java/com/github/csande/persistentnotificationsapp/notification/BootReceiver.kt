package com.github.csande.persistentnotificationsapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restores all saved notes as notifications after a reboot or an app update. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> NotificationHelper.showAll(context)
        }
    }
}
