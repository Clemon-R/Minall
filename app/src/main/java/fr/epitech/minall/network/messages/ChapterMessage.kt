package fr.epitech.minall.network.messages

import java.util.*

data class ChapterMessage(
    val id: Int,
    val number: Float,
    val title: String,
    val date: Date,
    val pages: String
)