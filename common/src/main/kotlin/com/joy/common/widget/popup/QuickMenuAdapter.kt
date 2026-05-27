package com.joy.common.widget.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.joy.common.R
import com.joy.common.extend.onClick200
import com.joy.common.widgets.BadgeView
import com.joy.common.widgets.IconFontView

class QuickMenuAdapter(
    private val context: Context,
    private val data: List<QuickMenuBean>,
) : BaseAdapter() {

    override fun getCount(): Int = data.size

    override fun getItem(position: Int): QuickMenuBean = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_quick_menu, parent, false)
            holder = ViewHolder(
                ivIcon = view.findViewById(R.id.ivIcon),
                tvTitle = view.findViewById(R.id.tvTitle),
                tvBadge = view.findViewById(R.id.tvBadge),
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val bean = data[position]
        holder.ivIcon.text = bean.iconText
        holder.tvTitle.text = bean.title
        holder.tvBadge.setCount(if (bean.badgeCount > 0) bean.badgeCount else 0)
        view.onClick200 { bean.onClick() }
        return view
    }

    private class ViewHolder(
        val ivIcon: IconFontView,
        val tvTitle: TextView,
        val tvBadge: BadgeView,
    )
}
