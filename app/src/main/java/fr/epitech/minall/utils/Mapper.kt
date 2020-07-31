package fr.epitech.minall.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.network.messages.MangaMessage

class Mapper
{
    companion object{
        fun map(message: MangaMessage): MangaData
        {
            val json = Utils.gson.toJson(message)
            val result = Utils.gson.fromJson(json, MangaData::class.java)

            result.icon = CacheManager.instance.loadBitmap(message.slug)
            return result
        }

        inline fun <reified T, reified F> map(message: T): F
        {
            val json = Utils.gson.toJson(message)
            return Utils.gson.fromJson(json, Utils.genericType<F>()) as F
        }
    }
}