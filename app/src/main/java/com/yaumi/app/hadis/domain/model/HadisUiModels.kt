package com.yaumi.app.hadis.domain.model

data class HadisCollection(
    val name: String,
    val slug: String,
    val total: Int
)

data class HadisItem(
    val number: Int,
    val arabicText: String,
    val translationId: String
)
