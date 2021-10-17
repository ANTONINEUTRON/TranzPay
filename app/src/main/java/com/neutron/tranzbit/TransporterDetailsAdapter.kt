package com.neutron.tranzbit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class TransporterDetailsAdapter(val details: Map<String, String>) :
    RecyclerView.Adapter<TransporterDetailsAdapter.ViewHolder>() {
    lateinit var context: Context
    val list2Map = details.toList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransporterDetailsAdapter.ViewHolder {
        context = parent.context
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.details_layout, parent, false))
    }

    override fun onBindViewHolder(holder: TransporterDetailsAdapter.ViewHolder, position: Int) {
        holder.title.text = list2Map.get(position).first
        holder.details.text = list2Map.get(position).second
    }

    override fun getItemCount(): Int = details.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val details: TextView = itemView.findViewById(R.id.details)
    }
}
