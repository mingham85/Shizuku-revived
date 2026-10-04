package moe.shizuku.manager.adb

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import moe.shizuku.manager.R
import moe.shizuku.manager.starter.StarterActivity
import moe.shizuku.manager.utils.EnvironmentUtils
import rikka.shizuku.Shizuku

@RequiresApi(Build.VERSION_CODES.N)
class AdbTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isRunning = Shizuku.pingBinder()
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.home_wireless_adb_title)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_wadb_24)
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        if (Shizuku.pingBinder()) {
            updateTileState()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(this, StarterActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            var connectMdns: AdbMdns? = null
            connectMdns = AdbMdns(this, AdbMdns.TLS_CONNECT) { port ->
                if (port > 0) {
                    connectMdns?.stop()
                    val startIntent = Intent(this, StarterActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                        putExtra(StarterActivity.EXTRA_HOST, "127.0.0.1")
                        putExtra(StarterActivity.EXTRA_PORT, port)
                    }
                    startActivityAndCollapse(startIntent)
                }
            }
            connectMdns.start()
        } else {
            val port = EnvironmentUtils.getAdbTcpPort()
            if (port > 0) {
                val startIntent = Intent(this, StarterActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(StarterActivity.EXTRA_IS_ROOT, false)
                    putExtra(StarterActivity.EXTRA_HOST, "127.0.0.1")
                    putExtra(StarterActivity.EXTRA_PORT, port)
                }
                startActivityAndCollapse(startIntent)
            }
        }
    }
}
