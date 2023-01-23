package com.teh.windowmodeswitch

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.teh.windowmodeswitch.databinding.ActivityMainBinding
import com.topjohnwu.superuser.Shell
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var PICK_FILE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Shell.getShell()
        binding.btnSwitch.isEnabled = Shell.isAppGrantedRoot() != false

        binding.BtnCheckRoot.setOnClickListener {
            val filePath = "/sdcard/windows/boot.img"
            val command = "ls $filePath"
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
                val exitValue = process.waitFor()
                if (exitValue == 0) {
                    val drawable = ContextCompat.getDrawable(this, R.drawable.ic_file_success)
                    binding.icCheck.setImageDrawable(drawable)
                    binding.textRoot.setText(getString(R.string.success_file_boot))
                    binding.textRoot.setTextColor(Color.GREEN)
                } else {
                    val drawable = ContextCompat.getDrawable(this, R.drawable.ic_file_error)
                    binding.icCheck.setImageDrawable(drawable)
                    binding.textRoot.setText(getString(R.string.fail_file_boot))
                    binding.textRoot.setTextColor(Color.RED)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        binding.btnSwitch.setOnClickListener {
            val filePath = "/sdcard/windows/boot.img"
            val command = "ls $filePath"
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("$command\n")
                os.writeBytes("exit\n")
                os.flush()
                val exitValue = process.waitFor()
                if (exitValue == 0) {
                    val llPadding = 30
                    val ll = LinearLayout(this)
                    ll.orientation = LinearLayout.HORIZONTAL
                    ll.setPadding(llPadding, llPadding, llPadding, llPadding)
                    ll.gravity = Gravity.CENTER
                    var llParam = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    llParam.gravity = Gravity.CENTER
                    ll.layoutParams = llParam
                    // create progressBar
                    val progressBar = ProgressBar(this)
                    progressBar.isIndeterminate = true
                    progressBar.setPadding(0, 0, llPadding, 0)
                    progressBar.layoutParams = llParam
                    llParam = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    llParam.gravity = Gravity.CENTER

                    // Creating a TextView inside the layout
                    val tvText = TextView(this)
                    tvText.text = getString(R.string.loading)
                    tvText.setTextColor(Color.parseColor("#000000"))
                    tvText.textSize = 20f
                    tvText.layoutParams = llParam
                    ll.addView(progressBar)
                    ll.addView(tvText)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setView(ll)
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                    val window: Window? = dialog.window
                    if (window != null) {
                        val layoutParams = WindowManager.LayoutParams()
                        layoutParams.copyFrom(dialog.window?.attributes)
                        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                        dialog.window?.attributes = layoutParams

                        // Disabling screen touch to avoid exiting the Dialog
                        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }
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
                } else {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Warning")
                        builder.setMessage(getString(R.string.file_not_exist))
                    builder.setPositiveButton(
                        "OK"
                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    val alert = builder.create();
                    alert.show();
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }


        }
        val database = Firebase.database
        val LinkRef = database.getReference()
        LinkRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue<String>()
                binding.download.setOnClickListener {
//            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            val downloadUri = Uri.parse(LinkRef)
//            val request = DownloadManager.Request(downloadUri)
//            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                .setAllowedOverRoaming(false)
//                .setTitle("Download")
//                .setDescription("Downloading file...")
//                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "file.zip")
//            val downloadId = downloadManager.enqueue(request)
                    if (value != null) {
                        Log.d("AAA", value)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })


    }
}