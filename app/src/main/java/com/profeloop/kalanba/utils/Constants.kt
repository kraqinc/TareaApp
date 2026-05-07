package com.profeloop.kalanba.utils

object Constants {
    const val COLLECTION_USERS = "users"
    const val COLLECTION_TASKS = "tasks"
    const val COLLECTION_SUBMISSIONS = "submissions"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    const val ROL_ESTUDIANTE = "estudiante"
    const val ROL_PROFESOR = "profesor"
    const val ESTADO_ENVIADO = "enviado"
    const val ESTADO_REVISANDO = "revisando"
    const val ESTADO_CALIFICADO = "calificado"
    const val CHANNEL_ID = "profeloop_channel"

    val SUBJECTS = listOf(
        "Química", "Biología", "Comprensión Lectora", "Matemáticas",
        "Inglés", "Estadística", "Emprendimiento", "Informática",
        "Física", "Ética", "Religión", "Geometría",
        "Competencias Ciudadanas", "Lenguaje", "Sociales",
        "Artística", "Educación Física"
    )

    val SUBJECT_EMOJIS = mapOf(
        "Química" to "⚗️",
        "Biología" to "🧬",
        "Comprensión Lectora" to "📖",
        "Matemáticas" to "📐",
        "Inglés" to "🇬🇧",
        "Estadística" to "📊",
        "Emprendimiento" to "💡",
        "Informática" to "💻",
        "Física" to "⚡",
        "Ética" to "🤝",
        "Religión" to "🙏",
        "Geometría" to "📏",
        "Competencias Ciudadanas" to "🏛️",
        "Lenguaje" to "✍️",
        "Sociales" to "🌎",
        "Artística" to "🎨",
        "Educación Física" to "⚽"
    )

    val GRADE_COLORS = listOf(
        "#EF5350", "#EC407A", "#AB47BC", "#7E57C2", "#5C6BC0",
        "#42A5F5", "#26A69A", "#66BB6A", "#FF7043"
    )
}
