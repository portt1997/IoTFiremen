package com.example.firemen_admin

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firemen.RecyclerItem
import kotlinx.android.synthetic.main.recycler_item.view.*

class BrokenDeviceListAdapter  (context: Context) : RecyclerView.Adapter<BrokenDeviceListViewHolder>() {
    lateinit var recyclerItemList: MutableList<RecyclerItem>
    var context : Context = context
    private lateinit var parent: ViewGroup
    var log = "deviceAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrokenDeviceListViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val itemViewHolder = inflater.inflate(R.layout.recycler_item, parent, false)
        this.parent = parent

        return BrokenDeviceListViewHolder(itemViewHolder, this)
    }

    override fun getItemCount(): Int {
        return recyclerItemList.size
    }

    override fun onBindViewHolder(holder: BrokenDeviceListViewHolder, position: Int) {
        val currentItem = recyclerItemList[position]
        holder.itemView.address_view.text = currentItem.address
        holder.itemView.fireState.setImageResource(currentItem.fireImage)

    }
}