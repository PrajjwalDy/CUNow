package com.hindu.joltt.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hindu.cunow.R
import com.hindu.joltt.Model.GjobModel

class gJobAdapter (private val mContext: Context,
    private val mgJob:List<GjobModel>):RecyclerView.Adapter<gJobAdapter.ViewHolder>(){
        inner class ViewHolder(@NonNull itemView:View):RecyclerView.ViewHolder(itemView){
            private val gjobImage: ImageView = itemView.findViewById(R.id.gjob_image) as ImageView
            private val gjobName: TextView = itemView.findViewById(R.id.gjobName) as TextView


            fun bind(list: GjobModel){

                Glide.with(mContext).load(list.gJobImage).into(gjobImage)
                gjobName.text = list.gJobName

                itemView.setOnClickListener {
                    openLink(list.gJobLink!!)
                }
                }
                }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
   val view = LayoutInflater.from(mContext).inflate(R.layout.gjob_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mgJob.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mgJob[position])
    }
private fun openLink(link:String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(mContext, Uri.parse(link))
}
}