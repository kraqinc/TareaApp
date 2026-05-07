package com.profeloop.kalanba.models

data class Submission(
    val id: String = "",
    val taskId: String = "",
    val estudianteId: String = "",
    val estudianteNombre: String = "",
    val archivoUrl: String = "",
    val archivoNombre: String = "",
    val fechaEnvio: Long = 0L,
    val estado: String = "enviado",
    val nota: String = "",
    val comentarioProfe: String = ""
)
