package com.man4tasik

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.man4tasik.databinding.ActivityReportBinding
import com.man4tasik.model.Attendance
import com.man4tasik.utils.CsvExporter
import com.man4tasik.utils.PdfExporter

class ReportActivity : AppCompatActivity() {

    private lateinit var b: ActivityReportBinding
    private val db = FirebaseDatabase.getInstance().getReference("attendance")
    private val dates = mutableListOf<String>()
    private lateinit var adapter: DateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityReportBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = DateAdapter(
            dates,
            onDownloadPdf = { date -> exportReport(date, "pdf") },
            onDownloadCsv = { date -> exportReport(date, "csv") }
        )

        b.recyclerDates.layoutManager = LinearLayoutManager(this)
        b.recyclerDates.adapter = adapter

        loadDates()
    }

    private fun loadDates() {
        db.get().addOnSuccessListener { snapshot ->
            dates.clear()
            for (child in snapshot.children) {
                child.key?.let { dates.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun exportReport(date: String, type: String) {
        db.child(date).get().addOnSuccessListener { snapshot ->
            val map = mutableMapOf<String, List<Attendance>>()

            for (classNode in snapshot.children) {
                val className = classNode.key ?: continue
                val attendanceList = mutableListOf<Attendance>()

                for (studentNode in classNode.children) {
                    val valueMap = studentNode.value as? Map<*, *>

                    val name = valueMap?.get("name")?.toString() ?: "Tidak diketahui"
                    val status = valueMap?.get("status")?.toString() ?: "Tidak diketahui"

                    val attendance = Attendance(
                        name = name,
                        kelas = className,
                        date = date,
                        status = status
                    )
                    attendanceList.add(attendance)
                }

                map[className] = attendanceList
            }

            when (type) {
                "pdf" -> PdfExporter.export(this, "laporan_$date", map)
                "csv" -> CsvExporter.export(this, "laporan_$date", map)
            }

            Toast.makeText(this, "Laporan $date ($type) berhasil disimpan", Toast.LENGTH_LONG).show()
        }
    }
}
