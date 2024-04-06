package com.example.petcare.ui.hospital

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petcare.data.Feature
import com.example.petcare.databinding.HospitalItemViewBinding

class HospitalAdapter(private val itemClickListener: IItemClickListener): RecyclerView.Adapter<HospitalAdapter.ViewHolder>() {

    var hospitalList: List<Feature> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemViewBinding = HospitalItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemViewBinding)
    }

    override fun getItemCount(): Int {
        return hospitalList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemViewBinding.hospitalItem.text = hospitalList[position].Name

        holder.itemViewBinding.hospitalCardView.setOnClickListener {
            itemClickListener.onItemClickListener(hospitalList[position])
        }
    }

    inner class ViewHolder(val itemViewBinding: HospitalItemViewBinding) : RecyclerView.ViewHolder(itemViewBinding.root)

    interface IItemClickListener {
        fun onItemClickListener(data: Feature)
    }
}