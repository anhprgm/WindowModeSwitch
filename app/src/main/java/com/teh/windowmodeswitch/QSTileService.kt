package com.teh.windowmodeswitch

import android.content.Intent
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.topjohnwu.superuser.Shell

class QSTileService: TileService() {
    override fun onClick() {
        super.onClick()
        Shell.getShell()
        if (Shell.isAppGrantedRoot() == true) {
            if (Shell.cmd("getprop ro.boot.slot_suffix").exec().out.contains("_a")) {
                Log.e("WindowsSwitch", "slot a")
                Shell.cmd(
                    "dd if=/sdcard/windows/boot.img of=/dev/block/sde14 bs=16M",
                    "sleep 1",
                    "svc power reboot"
                ).exec()
            } else if (Shell.cmd("getprop ro.boot.slot_suffix").exec().out.contains("_b")) {
                Log.e("WindowsSwitch", "slot b")
                Shell.cmd(
                    "dd if=/sdcard/windows/boot.img of=/dev/block/sde37 bs=16M",
                    "sleep 1",
                    "svc power reboot"
                ).exec()
            }
        }

    }

    override fun onTileAdded() {
        super.onTileAdded()
        Toast.makeText(this, "added qs tile", Toast.LENGTH_SHORT).show()
    }
}