package com.profeloop.kalanba.models

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "estudiante",
    val nivel: String = "bachillerato",
    val grado: Int = 8,
    val asignaturas: List<String> = emptyList(),
    val fcmToken: String = ""
)
