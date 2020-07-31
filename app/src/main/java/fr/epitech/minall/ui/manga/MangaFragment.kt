package fr.epitech.minall.ui.manga

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.solver.Cache
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.epitech.minall.MainActivity
import fr.epitech.minall.R
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.network.HttpClient
import fr.epitech.minall.network.messages.QueryMessage
import fr.epitech.minall.network.messages.ResultMessage
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.ui.reader.ReaderFragment
import fr.epitech.minall.utils.CacheManager
import fr.epitech.minall.utils.Mapper
import org.w3c.dom.Text

class MangaFragment(private val baseManga: MangaData) : BaseFragment() {

    companion object
    {
        private const val TAG = "MangaFragment"
        private const val REQUEST_QUERY = "{manga(x:m01,slug:\"[SLUG]\"){id,rank,title,slug,image,latestChapter,author,genres,description,createdDate,updatedDate,chapters{id,number,title,slug,date}}}"
    }

    private lateinit var manga: MangaData

    private lateinit var loading: View
    private lateinit var content: TableLayout
    private lateinit var lblDescription: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_manga, container, false)
        val iconView = root.findViewById<ImageView>(R.id.iconView)
        val lblName = root.findViewById<TextView>(R.id.lblName)
        val lblRank = root.findViewById<TextView>(R.id.lblRank)
        val lblGenres = root.findViewById<TextView>(R.id.lblGenres)
        val lblLastChapter = root.findViewById<TextView>(R.id.lblLastChapter)
        lblDescription = root.findViewById(R.id.lblDescription)

        loading = root.findViewById(R.id.loading)
        content = root.findViewById(R.id.content)

        iconView.setImageBitmap(baseManga.icon)
        lblName.text = baseManga.title
        lblGenres.text = baseManga.genres
        lblLastChapter.text = baseManga.latestChapter.toString()
        lblRank.text = baseManga.rank.toString()

        val tmp = CacheManager.instance.loadClass<MangaData>(baseManga.slug)
        if (tmp != null) {
            Log.d(TAG, "Cache found !")
            manga = tmp
            manga.icon = baseManga.icon
            parseMangaData(manga)
        }
        HttpClient.instance.post<ResultMessage>(this,
            "https://api2.mangahub.io/graphql",
            QueryMessage(REQUEST_QUERY.replace("[SLUG]", baseManga.slug))
        ) { result ->
            if (result.data?.manga == null)
                return@post
            val newManga = Mapper.map(result.data.manga)
            newManga.icon = baseManga.icon
            if (tmp != null){
                Log.d(TAG, "Refesh cache !")
                activity?.runOnUiThread {
                    lblLastChapter.text = newManga.latestChapter.toString()
                    lblRank.text = newManga.rank.toString()
                }
            }
            if (tmp == null || newManga.latestChapter != manga.latestChapter) {
                Log.d(TAG, "Parse and save !")
                parseMangaData(newManga)
                CacheManager.instance.saveClass(newManga.slug, newManga)
            }
            manga = newManga
        }
        val fab: FloatingActionButton = root.findViewById(R.id.fab)
        if (MainActivity.instance.user.mangaSlugFollowed.containsKey(baseManga.slug)){
            fab.setImageResource(R.drawable.ic_heart)
        }
        fab.setOnClickListener { view ->
            if (MainActivity.instance.user.mangaSlugFollowed.containsKey(baseManga.slug)){
                Snackbar.make(view, "The manga has been unfollowed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                activity?.runOnUiThread {
                    fab.setImageResource(R.drawable.ic_empty_heart)
                }
                MainActivity.instance.unfollowManga(baseManga)
            }else {
                Snackbar.make(view, "The manga has been followed", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                activity?.runOnUiThread {
                    fab.setImageResource(R.drawable.ic_heart)
                }
                MainActivity.instance.followManga(baseManga)
            }
        }
        return root
    }

    private fun parseMangaData(manga: MangaData)
    {
        activity?.runOnUiThread {
            loading.visibility = View.VISIBLE
            content.visibility = View.GONE
            content.removeAllViewsInLayout()

            var lastRow = TableRow(content.context)
            var lastRowTotalWidth = 0

            lblDescription.text = manga.description
            content.addView(lastRow)

            for (chapter in manga.chapters.reversed()){
                val btn = Button(content.context)
                if (MainActivity.instance.chapterHasBeenReaded(manga, chapter))
                    btn.backgroundTintList = context?.resources?.getColorStateList(R.color.colorGreen)
                else
                    btn.backgroundTintList = context?.resources?.getColorStateList(R.color.colorWhite)
                btn.text = "CHAPTER ${chapter.number}"
                btn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT)
                btn.setOnClickListener {
                    MainActivity.instance.openFragment(ReaderFragment(manga, chapter))
                    Snackbar.make(view!!, chapter.title, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }

                if (lastRowTotalWidth < 3) {
                    lastRowTotalWidth++
                    lastRow.addView(btn)
                }else {
                    lastRow = TableRow(content.context)
                    content.addView(lastRow)
                    lastRowTotalWidth = 1
                    lastRow.addView(btn)
                }
            }
            loading.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }
}