package fr.epitech.minall.interfaces

import fr.epitech.minall.datas.MangaData

interface IMangaViewerListener {
    fun onMangaSelected(manga: MangaData)
}