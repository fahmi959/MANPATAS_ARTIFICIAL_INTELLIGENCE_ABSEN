package com.man4tasik.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.UnitValue
import com.man4tasik.model.Attendance
import java.io.File

object PdfExporter {

    fun export(context: Context, fileName: String, data: Map<String, List<Attendance>>): File {
        // Folder: Download/Absen MAN 4 Tasikmalaya/pdf/
        val baseDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "Absen MAN 4 Tasikmalaya/pdf"
        )
        if (!baseDir.exists()) baseDir.mkdirs()

        val file = File(baseDir, "$fileName.pdf")
        val pdfWriter = PdfWriter(file)
        val pdfDoc = PdfDocument(pdfWriter)
        val document = Document(pdfDoc)

        data.forEach { (className, attendanceList) ->
            // Judul per kelas
            document.add(Paragraph("Kelas: $className").setBold().setFontSize(14f))

            // Buat tabel 3 kolom: Nama, Tanggal, Status
            val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 2f)))
            table.setWidth(UnitValue.createPercentValue(100f))

            // Header
            table.addHeaderCell("Nama Siswa")
            table.addHeaderCell("Tanggal")
            table.addHeaderCell("Status")

            // Isi data
            attendanceList.forEach {
                table.addCell(it.name)
                table.addCell(it.date)
                table.addCell(it.status)
            }

            document.add(table)
            document.add(Paragraph("\n")) // spasi antar kelas
        }

        document.close()

        // Setelah selesai langsung buka
        openFile(context, file, "application/pdf")

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