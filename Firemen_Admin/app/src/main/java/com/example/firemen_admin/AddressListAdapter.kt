package com.example.firemen_admin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firemen.MyViewHolder
import com.example.firemen.RecyclerItem


import kotlinx.android.synthetic.main.recycler_item.view.*

class AddressListAdapter(context: Context) : RecyclerView.Adapter<MyViewHolder>() {
    lateinit var recyclerItemList: MutableList<RecyclerItem>
    var context : Context = context
    private lateinit var parent: ViewGroup
    var log = "placeAdapter"


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val itemViewHolder = inflater.inflate(R.layout.recycler_item, parent, false)
        this.parent = parent

        return MyViewHolder(itemViewHolder, this).apply{
            itemView.setOnClickListener{
                val intent = Intent(context, BrokenDeviceListActivity::class.java)

                intent.putExtra("address", recyclerItemList[adapterPosition].address)
                context.startActivity(intent)
            }
        }
        return MyViewHolder(itemViewHolder, this)
    }

    override fun getItemCount(): Int {
        return recyclerItemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = recyclerItemList[position]
        holder.itemView.address_view.text = currentItem.address
        holder.itemView.fireState.setImageResource(currentItem.fireImage)

    }
}