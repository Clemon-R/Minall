package fr.epitech.minall.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import fr.epitech.minall.MainActivity
import fr.epitech.minall.R
import fr.epitech.minall.datas.MangaData
import fr.epitech.minall.interfaces.IMangaViewerListener
import fr.epitech.minall.network.HttpClient
import fr.epitech.minall.ui.BaseFragment
import fr.epitech.minall.utils.CacheManager
import kotlinx.android.synthetic.main.manga_card.view.*
import java.util.*


class MangaViewerRecyclerAdapter(
    private val root: BaseFragment,
    private val activity: FragmentActivity,
    private val items: List<MangaData>,
    private val listener: IMangaViewerListener
) : RecyclerView.Adapter<MangaViewerRecyclerAdapter.ViewHolder>() {
    private val views: MutableMap<View, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.manga_card, parent, false)
        view.isEnabled = false
        views += (view to -1)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int  = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.lblName.text = item.title
        holder.lblRank.text = item.rank.toString()
        holder.lblGenres.text = item.genres
        holder.lblLastChapter.text = item.latestChapter.toString()

        val slug = MainActivity.instance.user.mangaSlugFollowed[item.slug]
        if (slug != null){
            holder.btnUnfollow.visibility = View.VISIBLE
            holder.btnUnfollow.setOnClickListener {
                MainActivity.instance.unfollowManga(item)
                holder.lblChapterToRead.visibility = View.GONE
                holder.btnUnfollow.visibility = View.GONE
            }
            if (item.chapters != null) {
                holder.lblChapterToRead.visibility = View.VISIBLE
                holder.lblChapterToRead.text = "${item.chapters.size - slug.size} Chapters to read"
            } else {
                holder.lblChapterToRead.visibility = View.GONE
            }
        } else {
            holder.lblChapterToRead.visibility = View.GONE
            holder.btnUnfollow.visibility = View.GONE
        }

        holder.mView.isEnabled = false
        views[holder.mView] = position
        if (item.icon == null){
            holder.imageMain.setImageResource(R.drawable.ic_clemon)
            Timer().schedule(
                object : TimerTask() {
                    override fun run() { // your code here
                        if (views[holder.mView] == position)
                            HttpClient.instance.downloadImage(root, "https://thumb.mangahub.io/${item.image}"){
                                    img ->
                                item.icon = img
                                if (views[holder.mView] == position) {
                                    activity.runOnUiThread {
                                        holder.mView.isEnabled = true
                                        holder.imageMain.setImageBitmap(item.icon)
                                        this@MangaViewerRecyclerAdapter.notifyItemChanged(position)
                                    }
                                }
                                CacheManager.instance.saveBitmap(item.slug, item.icon!!)
                            }
                        this.cancel()
                    }
                },
                500 //Delay so we don't download directly if we don't stay on this manga
            )
        } else {
            holder.mView.isEnabled = true
            holder.imageMain.setImageBitmap(item.icon)
        }

        with(holder.mView){
            setOnClickListener {
                listener.onMangaSelected(item)
            }
        }
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val lblName = mView.lblName
        val imageMain = mView.iconView
        val lblRank = mView.lblRank
        val lblGenres = mView.lblGenres
        val lblLastChapter = mView.lblLastChapter

        val lblChapterToRead = mView.lblChapterToRead
        val btnUnfollow = mView.btnUnfollow
    }
}