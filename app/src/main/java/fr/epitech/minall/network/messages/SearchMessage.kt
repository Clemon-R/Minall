package fr.epitech.minall.network.messages

data class SearchMessage(
    val rows: List<MangaMessage>,
    val count: Int
)