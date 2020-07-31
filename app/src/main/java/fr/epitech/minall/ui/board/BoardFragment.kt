package fr.epitech.minall.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import fr.epitech.minall.MainActivity
import fr.epitech.minall.R
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.interfaces.IMangaViewerListener
import fr.epitech.minall.network.HttpClient
import fr.epitech.minall.network.messages.QueryMessage
import fr.epitech.minall.network.messages.ResultMessage
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.ui.home.HomeFragment
import fr.epitech.minall.ui.manga.MangaFragment
import fr.epitech.minall.utils.CacheManager
import fr.epitech.minall.utils.Mapper

class BoardFragment : BaseFragment(),
    IMangaViewerListener {
    companion object
    {
        private const val TAG = "BoardFragment"
        private const val REQUEST_QUERY = "{manga(x:m01,slug:\"[SLUG]\"){id,rank,title,slug,image,latestChapter,author,genres,description,createdDate,updatedDate,chapters{id,number,title,slug,date}}}"
    }

    private val mangas: MutableList<MangaData> = mutableListOf()

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val root = inflater.inflate(R.layout.fragment_board, container, false)
        recyclerView = root.findViewById(R.id.mangaViewer)
        with(recyclerView){
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = fr.epitech.minall.adapters.MangaViewerRecyclerAdapter(
                this@BoardFragment,
                this@BoardFragment.activity!!,
                mangas,
                this@BoardFragment
            )
        }
        if (mangas.isEmpty())
            for (slug in MainActivity.instance.user.mangaSlugFollowed){
                val manga = CacheManager.instance.loadClass<MangaData>(slug.key)
                HttpClient.instance.post<ResultMessage>(this, "https://api2.mangahub.io/graphql", QueryMessage(
                    REQUEST_QUERY.replace("[SLUG]", slug.key))){
                        result ->
                    if (result.data?.manga == null)
                        return@post
                    val newManga = Mapper.map(result.data.manga)
                    CacheManager.instance.saveClass(newManga.slug, newManga)
                    if (manga == null) {
                        mangas.add(newManga)
                        activity?.runOnUiThread {
                            recyclerView.adapter!!.notifyItemRangeChanged(mangas.size - 2, 2)
                        }
                    } else {
                        newManga.icon = manga.icon
                        val pos = mangas.indexOf(manga)
                        mangas.removeAt(pos)
                        mangas.add(pos, newManga)
                        if (manga.latestChapter != newManga.latestChapter)
                            activity?.runOnUiThread {
                                recyclerView.adapter!!.notifyItemChanged(pos)
                            }
                    }
                }
                if (manga != null) {
                    manga.icon = CacheManager.instance.loadBitmap(manga.slug)
                    mangas.add(manga)
                }
            }
        recyclerView.adapter!!.notifyItemRangeChanged(0, mangas.size)
        return root
    }

    override fun onMangaSelected(manga: MangaData) {
        MainActivity.instance.openFragment(MangaFragment(manga))
    }
}