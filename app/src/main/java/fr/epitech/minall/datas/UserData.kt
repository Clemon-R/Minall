package fr.epitech.minall.datas

data class UserData(
    val mangaSlugFollowed: MutableMap<String, MutableList<Int>> = mutableMapOf()
)