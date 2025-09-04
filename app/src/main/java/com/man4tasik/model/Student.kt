package com.man4tasik.model

data class Student(
    val id: String = "",
    val name: String = "",
    val kelas: String = "",
    val faceEmbedding: List<Float> = emptyList()
)

