package com.man4tasik.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.man4tasik.model.Student

object FirebaseUtils {
    val firestore = FirebaseFirestore.getInstance()
    val realtimeDb = FirebaseDatabase.getInstance().reference

    fun addStudent(student: Student, onResult: (Boolean) -> Unit) {
        firestore.collection("students")
            .document(student.id)
            .set(student)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun markAttendance(studentId: String, className: String, date: String) {
        val attendanceRef = realtimeDb.child("attendance").child(date).child(className)
        attendanceRef.child(studentId).setValue("hadir")
    }

    fun markAttendanceIfNotExists(
        studentId: String,
        name: String,
        kelas: String,
        date: String,
        onComplete: (wasNew: Boolean) -> Unit // ✅ tambahkan param boolean
    ) {
        val db = FirebaseDatabase.getInstance().reference
        val attendanceRef = db.child("attendance").child(date).child(kelas).child(studentId)

        attendanceRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                val attendanceData = mapOf(
                    "name" to name,
                    "status" to "Hadir"
                )
                attendanceRef.setValue(attendanceData).addOnCompleteListener {
                    onComplete(true) // ✅ Data baru ditambahkan
                }
            } else {
                onComplete(false) // ✅ Sudah pernah absen
            }
        }
    }



}