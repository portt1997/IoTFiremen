package com.example.firemen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_item.view.*

class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
    lateinit var itemList: List<Item>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemViewHolder = inflater.inflate(R.layout.recycler_item, parent, false)
        return MyViewHolder(itemViewHolder, this).apply{
            itemView.deleteButton.setOnClickListener{

            }

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList.get(position)
        holder.itemView.address_view.text = currentItem.address
    }
}