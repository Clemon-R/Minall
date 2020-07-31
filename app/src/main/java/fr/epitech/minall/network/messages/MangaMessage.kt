package fr.epitech.minall.network.messages

data class MangaMessage(
    val id: Int,
    val title: String,
    val description: String,
    val slug: String,
    val image: String,
    val latestChapter: Float,
    val rank: Int,
    val genres: String,
    val chapters: List<ChapterMessage>
)