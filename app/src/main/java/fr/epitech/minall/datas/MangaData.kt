package fr.epitech.minall.datas

import android.graphics.Bitmap
import com.google.gson.annotations.Expose

data class MangaData(
    val id: Int,
    val title: String,
    val description: String,
    val slug: String,
    val image: String,
    val latestChapter: Float,
    val rank: Int,
    val genres: String,
    val chapters: List<ChapterData>,
    @Expose(serialize = false, deserialize = false)
    var icon: Bitmap? = null
)