package com.teh.windowmodeswitch

import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Message
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

        val filePathx = "/sdcard/windows/boot.img"
        val commandx = "ls $filePathx"
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$commandx\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            if (exitValue == 0) {
                val drawable = ContextCompat.getDrawable(this, R.drawable.ic_file_success)
                binding.icCheck.setImageDrawable(drawable)
                binding.textRoot.text = getString(R.string.success_file_boot)
                binding.textRoot.setTextColor(Color.GREEN)
            } else {
                val drawable = ContextCompat.getDrawable(this, R.drawable.ic_file_error)
                binding.icCheck.setImageDrawable(drawable)
                binding.textRoot.text = getString(R.string.fail_file_boot)
                binding.textRoot.setTextColor(Color.RED)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


        val filePathUEFI = "/sdcard/windows/boot.img"
        val commandf = "ls $filePathUEFI"
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$commandf\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            if (exitValue == 0) {
                binding.icDownload.setImageResource(R.drawable.ic_file_success)
                binding.textDownload.text = getString(R.string.file_exist)
                binding.textDownload.setTextColor(Color.GREEN)
            } else {

            }
        }
        catch (e : IOException) {
            Log.d("AAA", e.toString())
        }

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
                    binding.textRoot.text = getString(R.string.success_file_boot)
                    binding.textRoot.setTextColor(Color.GREEN)
                } else {
                    val drawable = ContextCompat.getDrawable(this, R.drawable.ic_file_error)
                    binding.icCheck.setImageDrawable(drawable)
                    binding.textRoot.text = getString(R.string.fail_file_boot)
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
        val progressBar = ProgressBar(this)
        val builder = AlertDialog.Builder(this)
        builder.setView(progressBar)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)


        val database = Firebase.database
        val linkRef = database.getReference("link")
        linkRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value.toString()
                binding.download.setOnClickListener {
                    alertDialog.show()

                    val filePath = "/storage/emulated/0/Download/nabu_win_boot.img"
                    val command = "ls $filePath"
                    try {
                        val process = Runtime.getRuntime().exec("su")
                        val os = DataOutputStream(process.outputStream)
                        os.writeBytes("$command\n")
                        os.writeBytes("exit\n")
                        os.flush()
                        val exitValue = process.waitFor()
                        if (exitValue == 0) {
                            binding.icDownload.setImageResource(R.drawable.ic_file_success)
                            binding.textDownload.text = getString(R.string.file_exist)
                            binding.textDownload.setTextColor(Color.GREEN)
                        } else {
                            binding.icDownload.setImageResource(R.drawable.ic_file_download)
                            binding.textDownload.text = getString(R.string.download_file_boot)
                            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val downloadUri = Uri.parse(value)
                            val request = DownloadManager.Request(downloadUri)
                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                                .setAllowedOverRoaming(false)
                                .setTitle("Download file boot")
                                .setDescription("Downloading file...")
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "nabu_win_boot.img")
                            val downloadId = downloadManager.enqueue(request)
                            val br = object :BroadcastReceiver() {
                                override fun onReceive(p0: Context?, p1: Intent?) {
                                    val id:Long? = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                                    if (id==downloadId) {
                                        Shell.cmd("dd if=/storage/emulated/0/Download/nabu_win_boot.img of=/sdcard/windows/boot.img").exec()
                                        Toast.makeText(applicationContext, "success", Toast.LENGTH_SHORT).show()
                                        alertDialog.dismiss()
                                        binding.icCheck.setImageResource(R.drawable.ic_file_success)
                                        binding.textRoot.text = getString(R.string.success_file_boot)
                                        binding.textRoot.setTextColor(Color.GREEN)
                                        binding.icDownload.setImageResource(R.drawable.ic_file_success)
                                        binding.textDownload.text = getString(R.string.success_file_boot)
                                        binding.textDownload.setTextColor(Color.GREEN)
                                    }
                                }

                            }
                            registerReceiver(br, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                        }
                    }
                    catch (e : IOException) {
                        Log.d("AAA", e.toString())
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        binding.guideline.setOnClickListener {
            val url = "https://github.com/erdilS/Port-Windows-11-Xiaomi-Pad-5/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        val s = getText(R.string.crr_slot)
        val slot = Shell.cmd("getprop ro.boot.slot_suffix").exec().out.toString()
        if (Shell.cmd("getprop ro.boot.slot_suffix").exec().out.contains("_a")) {
            binding.currentSlot.text = "$s: a"
        } else {
            binding.currentSlot.text = "$s: b"
        }
        binding.dumpBootFile.setOnClickListener { dumpBootFile(slot) }
        val filePath = "/sdcard/windows/boot_android.img"
        val command = "ls $filePath"
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            if (exitValue == 0) {
                binding.dumpBootFileIc.setImageResource(R.drawable.ic_file_success)
                binding.dumpBootFileTxt.text = getString(R.string.dumped_file_boot)
                binding.dumpBootFileTxt.setTextColor(Color.GREEN)
            }
        } catch (e: IOException) {
            Log.e("err", e.toString())
        }
    }


    private fun dumpBootFile(string: String) {
        Shell.getShell()
        val filePath = "/sdcard/windows/boot_android.img"
        val command = "ls $filePath"
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            if (exitValue == 0) {
                binding.dumpBootFileIc.setImageResource(R.drawable.ic_file_success)
                binding.dumpBootFileTxt.text = getString(R.string.dumped_file_boot)
                binding.dumpBootFileTxt.setTextColor(Color.GREEN)
            } else {
                Toast.makeText(this, "dumping ...", Toast.LENGTH_SHORT).show()
                if (string.contains("_a")) {
                    Log.d("AA", "a")
                    Shell.cmd("dd if=/dev/block/bootdevice/by-name/boot_a of=/sdcard/windows/boot_android.img").exec()
                } else Shell.cmd("dd if=/dev/block/bootdevice/by-name/boot_b of=/sdcard/windows/boot_android.img").exec()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }


}