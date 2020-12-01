package com.example.firemen

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firemen_firefighter.FirePlaceListActivity
import com.example.firemen_firefighter.R

import kotlinx.android.synthetic.main.recycler_item.view.*

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket


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
                val intent = Intent(context, FirePlaceListActivity::class.java)

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