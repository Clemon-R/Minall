package fr.epitech.minall.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.utils.Utils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.LinkedHashMap


class HttpClient
{
    companion object{
        val JSON = "application/json; charset=utf-8".toMediaType()
        val TAG = "HttpClient"

        val instance: HttpClient by lazy {
            HttpClient()
        }

        inline fun <reified T> responseToClass(response: Response) : T?
        {
            var result: T? = null
            try {
                val data = response.body?.string() ?: throw IOException()
                Log.d(TAG, "JSON: $data")
                result = Utils.gson.fromJson(data, Utils.genericType<T>())
            } catch (e: Exception){
                Log.e(TAG, e.toString())
            }
            return result
        }
    }

    private val okClient = OkHttpClient()

    private val lock = ReentrantLock()
    private val requests: LinkedHashMap<BaseFragment, Queue<RequestData>> = LinkedHashMap()


    constructor(){
        Thread {
            Log.d(TAG, "Launching the request executor")
            while (true){
                if (requests.isEmpty()){
                    Thread.sleep(10)
                    continue
                }
                lock.lock()
                val key= requests.keys.reversed().firstOrNull()
                val value = requests[key] ?: continue
                var request = value.remove()
                lock.unlock()
                try {
                    val response = okClient.newCall(request.request)
                        .execute()
                    if (request.action != null) {
                        Thread {
                            Log.d(TAG, "Launching the action of the request : ${request.request.url.toUrl()}")
                            request.action?.invoke(response)
                        }.start()
                    }
                }catch (e: IOException)
                {
                    e.printStackTrace()
                    value.add(request)
                }catch (e: Exception){
                    e.printStackTrace()
                }
                lock.lock()
                if (value.isEmpty())
                    requests.remove(key)
                lock.unlock()
            }
        }.start()
    }

    fun downloadImage(root: BaseFragment, url: String, action: (bitmap: Bitmap) -> Unit)
    {
        val request: Request = Request.Builder()
            .url(url)
            .build()
        execute(root, request){
            response ->
            val inputStream = response.body?.byteStream() ?: return@execute
            val bitmap =  BitmapFactory.decodeStream(inputStream)
            if (bitmap != null)
                action.invoke(bitmap)
        }
    }

    inline fun <reified T> post(root: BaseFragment, url: String, obj: IMessage, noinline action: ((response: T) -> Unit)?) {
        var json = Utils.gson.toJson(obj)
        Log.d(TAG, "Request post with body $json")
        val body = json.toRequestBody(JSON)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        execute(root, request){
            response ->
            val tmp: T? = responseToClass<T>(response)
            if (tmp != null)
                action?.invoke(tmp)
        }
    }

    fun get(root: BaseFragment, url: String, action: ((response: Response) -> Unit)?) {
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        execute(root, request, action)
    }

    fun execute(root: BaseFragment, request: Request, action: ((response: Response) -> Unit)?)
    {
        Log.d(TAG, "New request to $request")
        try {
            lock.lock()
            val list = requests[root] ?: LinkedList<RequestData>()
            list.add(RequestData(request, action))
            requests[root] = list
            lock.unlock()
        } catch (e: Exception){
            Log.e(TAG, e.toString())
        }
    }

    fun removeAllRequests(root: BaseFragment)
    {
        Log.d(TAG, "Removing all requests of a fragment")
        try {
            lock.lock()
            requests.remove(root)
            lock.unlock()
        } catch (e: Exception){
            Log.e(TAG, e.toString())
        }
    }

    inner class RequestData(val request: Request, val action: ((response: Response) -> Unit)?)
}