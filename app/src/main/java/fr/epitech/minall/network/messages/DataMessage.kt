package fr.epitech.minall.network.messages

import fr.epitech.minall.datas.MangaData

data class DataMessage(
    val search: SearchMessage?,
    val manga: MangaMessage?,
    val chapter: ChapterMessage?
)