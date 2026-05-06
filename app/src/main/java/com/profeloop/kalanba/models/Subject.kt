package com.profeloop.kalanba.models

data class Subject(
    val nombre: String = "",
    val grado: Int = 0,
    val profesorUid: String = "",
    val profesorNombre: String = "",
    val emoji: String = "📚"
) {
    companion object {
        val TODAS_LAS_ASIGNATURAS = listOf(
            "Química",
            "Biología",
            "Comprensión Lectora",
            "Matemáticas",
            "Inglés",
            "Estadística",
            "Emprendimiento",
            "Informática",
            "Física",
            "Ética",
            "Religión",
            "Geometría",
            "Competencias Ciudadanas",
            "Lenguaje",
            "Sociales",
            "Artística",
            "Educación Física"
        )

        fun emojiParaAsignatura(asignatura: String): String = when (asignatura) {
            "Química"                  -> "⚗️"
            "Biología"                 -> "🧬"
            "Comprensión Lectora"      -> "📖"
            "Matemáticas"              -> "🔢"
            "Inglés"                   -> "🇬🇧"
            "Estadística"              -> "📊"
            "Emprendimiento"           -> "💡"
            "Informática"              -> "💻"
            "Física"                   -> "⚡"
            "Ética"                    -> "⚖️"
            "Religión"                 -> "✝️"
            "Geometría"                -> "📐"
            "Competencias Ciudadanas"  -> "🏛️"
            "Lenguaje"                 -> "✏️"
            "Sociales"                 -> "🌎"
            "Artística"                -> "🎨"
            "Educación Física"         -> "⚽"
            else                       -> "📚"
        }
    }
}
