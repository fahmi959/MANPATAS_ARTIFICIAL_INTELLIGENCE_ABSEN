package com.man4tasik



import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.man4tasik.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterStudentActivity::class.java))
        }
        b.btnAttendance.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }
        b.btnReport.setOnClickListener {
            startActivity(Intent(this, ReportActivity::class.java))
        }


        // PENGECEKAN GANTI VERSI
       checkForUpdate()
    }

    private fun checkForUpdate() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0 // 0 detik tidak perlu fetch menunggu 1 jam
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val minVersion = remoteConfig.getLong("minimum_required_version")
                val updateUrl = remoteConfig.getString("update_url")

                val currentVersion = BuildConfig.VERSION_CODE.toLong()

                if (currentVersion < minVersion) {
                    showUpdateDialog(updateUrl)
                }
            }
        }
    }


    private fun showUpdateDialog(downloadUrl: String) {
        // Jalankan dialog di UI thread
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(
                this
            )
            builder.setTitle("Pembaruan Diperlukan")
                .setMessage("Versi aplikasi ini sudah usang. Silakan perbarui untuk melanjutkan.")
                .setCancelable(false)
                .setPositiveButton("Perbarui Sekarang") { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                .show()
        }
    }
}