package com.example.petcare.ui.settings.petsInfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petcare.data.PetData
import com.example.petcare.databinding.PetItemViewBinding
import com.example.petcare.ui.hospital.HospitalAdapter


class PetAdapter(private val itemClickListener: IItemClickListener): RecyclerView.Adapter<PetAdapter.ViewHolder>()  {

    private val petList = ArrayList<PetData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemViewBinding = PetItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemViewBinding)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemViewBinding.petItem.text = petList[position].name

        holder.itemViewBinding.petCardView.setOnClickListener {
            itemClickListener.onItemClickListener(petList[position])
        }
    }

    fun updateList(petList: List<PetData>) {
        this.petList.clear()
        this.petList.addAll(petList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val itemViewBinding: PetItemViewBinding) : RecyclerView.ViewHolder(itemViewBinding.root)

    interface IItemClickListener {
        fun onItemClickListener(data: PetData)
    }
}