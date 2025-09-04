package com.man4tasik.model




data class Attendance(
    val name: String = "",
    val kelas: String = "",
    val date: String = "",
    val status: String = "hadir" // bisa: hadir, izin, sakit, alfa
)

