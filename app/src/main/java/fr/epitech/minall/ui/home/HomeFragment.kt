package fr.epitech.minall.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.epitech.minall.MainActivity
import fr.epitech.minall.R
import fr.epitech.minall.adapters.MangaViewerRecyclerAdapter
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.interfaces.IMangaViewerListener
import fr.epitech.minall.network.HttpClient
import fr.epitech.minall.network.messages.QueryMessage
import fr.epitech.minall.network.messages.ResultMessage
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.ui.manga.MangaFragment
import fr.epitech.minall.utils.Mapper


class HomeFragment : BaseFragment(),
    IMangaViewerListener {
    companion object{
        private const val TAG = "HomeFragment"

        private val REQUEST_QUERY = "{search(x:m01,genre:\"all\",mod:LATEST,count:true,offset:[OFFSET],limit:[LIMIT]){rows{id,rank,title,slug,genres,image,latestChapter,createdDate},count}}"
        private val QUERY_LIMIT = 100

    }

    private var currentOffset = 0
    private val mangas: MutableList<MangaData> = mutableListOf()
    private var nextPage: ResultMessage? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var loading: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        MainActivity.instance.addToStack(this)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.findViewById(R.id.mangaViewer)
        loading = root.findViewById(R.id.loading)
        with(recyclerView){
            layoutManager = LinearLayoutManager(context)
            adapter = MangaViewerRecyclerAdapter(
                this@HomeFragment,
                this@HomeFragment.activity!!,
                mangas,
                this@HomeFragment
            )
            autoLoadNewManga(this)
        }
        if (nextPage == null)
            loadNextSetOfManga()
        return root
    }

    private fun autoLoadNewManga(viewer: RecyclerView)
    {
        viewer.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadNextSetOfManga()
                }
            }
        })
    }

    private fun loadNextSetOfManga()
    {
        if (nextPage != null)
            parseMangaReponsesToView(nextPage!!)
        else {
            loading.visibility = View.VISIBLE
            HttpClient.instance.post<ResultMessage>(this, "https://api2.mangahub.io/graphql", QueryMessage(getQueryStr())){
                parseMangaReponsesToView(it)
                activity?.runOnUiThread {
                    loading.visibility = View.GONE
                }
            }
            currentOffset++
        }
        HttpClient.instance.post<ResultMessage>(this, "https://api2.mangahub.io/graphql", QueryMessage(getQueryStr()))
        {
                response ->
            nextPage = response
            currentOffset++
        }
    }

    private fun getQueryStr() : String{
        return REQUEST_QUERY
            .replace("[OFFSET]", (QUERY_LIMIT*currentOffset).toString())
            .replace("[LIMIT]", QUERY_LIMIT.toString())
    }

    private fun parseMangaReponsesToView(result: ResultMessage)
    {
        if (result.data?.search?.rows == null)
            return
        val offset = mangas.size
        for (message in result.data.search.rows){
            mangas += Mapper.map(message)
        }
        activity?.runOnUiThread {
            recyclerView.adapter!!.notifyItemInserted(offset)
        }
    }

    override fun onMangaSelected(manga: MangaData) {
        MainActivity.instance.openFragment(MangaFragment(manga))
    }
}