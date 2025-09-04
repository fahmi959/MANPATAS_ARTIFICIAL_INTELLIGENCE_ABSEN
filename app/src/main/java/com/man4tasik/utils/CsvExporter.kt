package com.man4tasik.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.man4tasik.model.Attendance
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter

object CsvExporter {

    fun export(context: Context, fileName: String, data: Map<String, List<Attendance>>): File {
        // Folder: Download/Absen MAN 4 Tasikmalaya/csv/
        val baseDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "Absen MAN 4 Tasikmalaya/csv"
        )
        if (!baseDir.exists()) baseDir.mkdirs()

        val file = File(baseDir, "$fileName.csv")

        // ✅ Hapus file jika sudah ada
        if (file.exists()) file.delete()

        val writer = CSVWriter(FileWriter(file, false)) // false: overwrite

        data.forEach { (className, attendanceList) ->
            writer.writeNext(arrayOf("Kelas: $className"))
            writer.writeNext(arrayOf("Nama Siswa", "Tanggal", "Status"))

            attendanceList.forEach {
                writer.writeNext(arrayOf(it.name, it.date, it.status))
            }

            writer.writeNext(arrayOf("")) // baris kosong antar kelas
        }

        writer.close()

        // Setelah selesai langsung buka
        openFile(context, file, "text/csv")

        return file
    }


    private fun openFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}