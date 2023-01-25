package com.teh.windowmodeswitch

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import java.io.File

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex != -1) {
                val status = cursor.getInt(statusIndex)
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    if(localUriIndex != -1){
                        val localUri = cursor.getString(localUriIndex)
                        val file = File(localUri)
                        Log.d("AAA", file.toString())
                    }else {
                        Log.d("AAA", "COLUMN_LOCAL_URI does not exist")
                    }
                } else {

                }
            }else {
                Log.d("AAA", "COLUMN_STATUS does not exist")
            }
        }
        cursor.close()
    }
}
