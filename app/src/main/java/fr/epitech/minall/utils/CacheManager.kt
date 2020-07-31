package fr.epitech.minall.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import fr.epitech.minall.network.HttpClient
import java.io.*

class CacheManager(val editor: SharedPreferences.Editor, val cache: SharedPreferences, private val wrapper: ContextWrapper){

    companion object{
        const val TAG = "CacheManager"
        const val APPLICATION_PACKAGE = "fr.epitech.minall"

        private lateinit var varInstance: CacheManager
        var instance: CacheManager
            get() = varInstance
            private set(value){
                varInstance = value
            }

        fun setup(context: Context)
        {
            Log.d(TAG, "Setup...")
            val sharedPreferences = context.getSharedPreferences(APPLICATION_PACKAGE, Context.MODE_PRIVATE)
            instance = CacheManager(sharedPreferences.edit(), sharedPreferences, ContextWrapper(context))
        }
    }

    inline fun <reified T> saveClass(name: String, tmp: T)
    {
        Log.d(TAG, "Saving class, with the name: $name")
        val json = Utils.gson.toJson(tmp)
        editor.putString(name, json)
        editor.commit()
        editor.apply()
    }

    inline fun <reified T> loadClass(name: String): T?
    {
        Log.d(TAG, "Loading class, with the name: $name")
        val json = cache.getString(name, null) ?: return null
        return Utils.gson.fromJson(json, Utils.genericType<T>())
    }

    fun saveBitmap(name: String, img: Bitmap)
    {
        Log.d(TAG, "Saving new bitmap, with the name: $name")
        val folder = wrapper.getDir("Thumbnails", Context.MODE_PRIVATE)
        val file = File(folder,"${name.replace("/","-").replace(".jpg", "")}.jpg")

        try{
            if (file.exists())
                file.delete()
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            img.compress(Bitmap.CompressFormat.JPEG,100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun loadBitmap(name: String) : Bitmap?
    {
        Log.d(TAG, "Loading bitmap, with the name: $name")
        val folder = wrapper.getDir("Thumbnails", Context.MODE_PRIVATE)
        val file = File(folder,"${name.replace("/","-").replace(".jpg", "")}.jpg")
        if (!file.exists())
            return null
        return BitmapFactory.decodeFile(file.absolutePath)
    }
}