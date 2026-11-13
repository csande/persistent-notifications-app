package com.github.csande.persistentnotificationsapp.tile

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.github.csande.persistentnotificationsapp.MainActivity
import com.github.csande.persistentnotificationsapp.R

/**
 * Quick Settings tile that opens the editor with a blank, new note.
 *
 * The tile is an action, not a toggle, so it always reports the inactive state.
 * The system binds this service only while the Quick Settings panel is visible.
 *
 * Large tiles show the icon with two lines of text: the label (the app name) and the
 * subtitle. Small tiles show only the icon; the system handles that automatically.
 */
class NewNoteTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.app_name)
            subtitle = getString(R.string.tile_subtitle)
            icon = Icon.createWithResource(this@NewNoteTileService, R.drawable.ic_tile)
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()

        // CLEAR_TASK restarts the editor so a blank note is shown even when the
        // activity is already open (for example, editing another note in the background).
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Since Android 14, the Intent overload is deprecated in favor of a PendingIntent.
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
