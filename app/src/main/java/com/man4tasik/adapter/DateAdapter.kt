package com.man4tasik

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.man4tasik.databinding.ItemDateBinding

class DateAdapter(
    private val dates: List<String>,
    private val onDownloadPdf: (String) -> Unit,
    private val onDownloadCsv: (String) -> Unit
) : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    inner class ViewHolder(val b: ItemDateBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = dates[position]
        holder.b.txtDate.text = date
        holder.b.btnPdf.setOnClickListener { onDownloadPdf(date) }
        holder.b.btnCsv.setOnClickListener { onDownloadCsv(date) }
    }
}