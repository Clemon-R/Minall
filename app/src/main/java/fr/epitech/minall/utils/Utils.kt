package fr.epitech.minall.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Utils {
    companion object{
        val gson = Gson()
        inline fun <reified T> genericType() = object: TypeToken<T>() {}.type
    }
}