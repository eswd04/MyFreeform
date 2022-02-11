package com.demo.myfreeform

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.demo.myfreeform.databinding.ItemAppBinding

class AppAdapter(val list: List<AppInfo>): RecyclerView.Adapter<AppAdapter.Holder>() {

    var clickListener:OnClickListener?=null


    interface OnClickListener{
        fun onItemClick(appInfo: AppInfo,position: Int)
    }


    inner class Holder(val itemBinder:ItemAppBinding):RecyclerView.ViewHolder(itemBinder.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        return Holder(ItemAppBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val appinfo = list[position]
        holder.itemBinder.textAppname.setText(appinfo.textName)
        holder.itemBinder.imgAppIcon.setImageDrawable(appinfo.icon)
        holder.itemView.setOnClickListener {
            clickListener?.onItemClick(appinfo,position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}