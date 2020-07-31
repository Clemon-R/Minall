package fr.epitech.minall.ui.reader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.google.gson.Gson
import fr.epitech.minall.MainActivity
import fr.epitech.minall.R
import fr.epitech.minall.adapters.ChapterSpinnerAdapter
import fr.epitech.minall.datas.ChapterData
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.network.HttpClient
import fr.epitech.minall.network.messages.QueryMessage
import fr.epitech.minall.network.messages.ResultMessage
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.utils.CacheManager
import fr.epitech.minall.utils.Mapper
import fr.epitech.minall.utils.Utils


class ReaderFragment(private val manga: MangaData, private var currentChapter: ChapterData) : BaseFragment() {

    companion object{
        private const val TAG = "ReaderFragment"
        private const val REQUEST_QUERY = "{chapter(x:m01,slug:\"[SLUG]\",number:[NUMBER]){id,number,title,date,pages}}"
    }

    private lateinit var listScans: LinearLayout
    private lateinit var loading: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_reader, container, false)
        val btnPrevious = root.findViewById<Button>(R.id.btnPrevious)
        val btnNext = root.findViewById<Button>(R.id.btnNext)
        val spinChapters = root.findViewById<Spinner>(R.id.spinChapters)
        listScans = root.findViewById(R.id.listScans)
        loading = root.findViewById(R.id.loading)

        spinChapters.adapter = ChapterSpinnerAdapter(context!!, manga.chapters.reversed())
        spinChapters.setSelection(manga.chapters.size - manga.chapters.indexOf(currentChapter) - 1)
        spinChapters.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?, view: View,
                position: Int, id: Long
            ) {
                HttpClient.instance.removeAllRequests(this@ReaderFragment)
                val chapter = spinChapters.adapter.getItem(position) as ChapterData
                createPagesView(chapter)
                activity?.runOnUiThread {
                    btnPrevious.isEnabled = position + 1 < manga.chapters.size
                    btnNext.isEnabled = position - 1 >= 0
                }
            }

            override fun onNothingSelected(adapter: AdapterView<*>?) {}
        }
        btnPrevious.setOnClickListener {
            if (spinChapters.selectedItemPosition + 1 >= manga.chapters.size)
                return@setOnClickListener
            activity?.runOnUiThread {
                spinChapters.setSelection(spinChapters.selectedItemPosition + 1)
            }
        }
        btnNext.setOnClickListener {
            if (spinChapters.selectedItemPosition - 1 < 0)
                return@setOnClickListener
            activity?.runOnUiThread {
                spinChapters.setSelection(spinChapters.selectedItemPosition - 1)
            }
        }
        return root
    }

    private fun createPagesView(chapter: ChapterData)
    {
        activity?.runOnUiThread {
            loading.visibility = View.VISIBLE
        }
        listScans.removeAllViewsInLayout()
        var pages = CacheManager.instance.loadClass<Map<String, String>>("${manga.slug}-${chapter.number}")
        if (pages != null){
            pagesToImg(pages, chapter)
            MainActivity.instance.chapterReaded(manga, chapter)
        }else {
            HttpClient.instance.post<ResultMessage>(this,
                "https://api2.mangahub.io/graphql",
                QueryMessage(createRequestQuery(chapter))
            ) { result ->
                if (result.data?.chapter == null)
                    return@post
                currentChapter = Mapper.map(result.data.chapter)
                val pages: Map<String, String> = Utils.gson.fromJson(
                    currentChapter.pages,
                    Utils.genericType<LinkedHashMap<String, String>>()
                )
                CacheManager.instance.saveClass("${manga.slug}-${chapter.number}", pages)
                pagesToImg(pages, chapter)
            }
        }
    }

    private fun pagesToImg(pages: Map<String, String>, chapter: ChapterData)
    {
        activity?.runOnUiThread {
            loading.visibility = View.GONE
        }
        var nbr = 0
        for (page in pages) {
            activity?.runOnUiThread {
                val img = ImageView(listScans.context)
                val icon = CacheManager.instance.loadBitmap(page.value)
                if (icon != null)
                    activity?.runOnUiThread {
                        nbr++
                        img.setImageBitmap(icon)
                    }
                else
                    HttpClient.instance.downloadImage(this, "https://cdn.mangahub.io/file/imghub/${page.value}") {
                        nbr++
                        activity?.runOnUiThread {
                            img.setImageBitmap(it)
                        }
                        if (MainActivity.instance.user.mangaSlugFollowed.containsKey(manga.slug))
                            CacheManager.instance.saveBitmap(page.value, it)
                    }
                if (nbr == pages.size)
                    MainActivity.instance.chapterReaded(manga, chapter)
                img.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                listScans.addView(img)
            }
        }
    }

    private fun createRequestQuery(chapter: ChapterData) : String
    {
        return REQUEST_QUERY
            .replace("[SLUG]", manga.slug)
            .replace("[NUMBER]", chapter.number.toString())
    }
}