package fr.epitech.minall.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import fr.epitech.minall.R
import fr.epitech.minall.datas.ChapterData
import org.w3c.dom.Text

class ChapterSpinnerAdapter(context: Context, private val datas: List<ChapterData>) : ArrayAdapter<ChapterData>(context, R.layout.chapter_item)
{
    override fun getItem(position: Int): ChapterData? {
        return datas[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = super.getView(position, convertView, parent)

        bindView(datas[position], view)

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)

        bindView(datas[position], view)

        return view
    }

    private fun bindView(chapter: ChapterData, view: View)
    {
        val lblName = view as TextView

        lblName.text = "CHAPTER ${chapter.number} : ${chapter.title.replace("Ep[isode]{0,}[\\.\\s]{0,2}[0-9\\.]+|Ch[apter]{0,}[\\.\\s]{0,2}[0-9\\.]+".toRegex(), "")}"
    }
}