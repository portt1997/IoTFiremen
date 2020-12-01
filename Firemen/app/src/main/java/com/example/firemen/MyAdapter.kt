package com.example.firemen

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.activity_login.view.userLoginEmail
import kotlinx.android.synthetic.main.activity_login.view.userLoginPW
import kotlinx.android.synthetic.main.activity_register.view.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.recycler_item.view.*
import kotlinx.android.synthetic.main.remove_dialog.*
import kotlinx.android.synthetic.main.remove_dialog.view.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket


class MyAdapter(context: Context, userID : String) : RecyclerView.Adapter<MyViewHolder>() {
    lateinit var recyclerItemList: MutableList<RecyclerItem>
    var context : Context = context
    var userID = userID
    private lateinit var parent: ViewGroup
    var log = "adapter"
    //shared preference variable
    //var addrPref : SharedPreferences = context.getSharedPreferences("AddressInfo", Context.MODE_PRIVATE)
    //var addrPrefEdit : SharedPreferences.Editor = addrPref.edit()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val itemViewHolder = inflater.inflate(R.layout.recycler_item, parent, false)
        this.parent = parent


        return MyViewHolder(itemViewHolder, this).apply{
            itemView.deleteButton.setOnClickListener{
                //recyclerItemList.removeAt(adapterPosition)
                //notifyItemRemoved(adapterPosition)

                val inflater = LayoutInflater.from(context)
                var rootView:View = inflater.inflate(R.layout.remove_dialog, parent, false)

                //val builder = androidx.appcompat.app.AlertDialog.Builder(context)
                    //.setView(inflater.inflate(R.layout.remove_dialog, parent, false))

                var builder = AlertDialog.Builder(context).setView(rootView)
                //var builder = AlertDialog.Builder(context).setView(inflater.inflate(R.layout.remove_dialog, parent, false))<<하면 망함
                rootView.removeCheck.text = "${recyclerItemList[adapterPosition].address} 을(를) 삭제하시겠습니까?"
                val alertDialog = builder.show()

                //builder.setMessage(" 삭제하시겠습니까?")
                //builder.setPositiveButton("확인", null)
                //builder.setNegativeButton("취소",null)

                rootView.yesRemove.setOnClickListener{

                    //get list from SharedPreference, then remove selected item from SharedPreference
                    var prefAddrList : MutableList<String>
                    var listSharedPreferences = JsonSharedPreference(context, "AddressInfo")

                    prefAddrList = listSharedPreferences.getList()

                    Log.i(log, "adpater log : $prefAddrList[adapterPosition]")
                    Log.i(log, "adpater log : $adapterPosition")
                    var removeAddr = prefAddrList[adapterPosition]
                    prefAddrList.removeAt(adapterPosition)
                    listSharedPreferences.setList(prefAddrList)

                    recyclerItemList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)

                    var thread = NetworkThread(removeAddr)
                    thread.start()

                    alertDialog.dismiss()
                }
                rootView.noRemove.setOnClickListener {
                    alertDialog.dismiss()
                }
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

//        holder.apply{
//            itemView.deleteButton.setOnClickListener{
//
    }

    inner class NetworkThread(removeData : String) : Thread(){
        private val ip = "54.221.152.48"
        private val port = 4000
        var removeAddr = removeData

        override fun run() {
            try{
                var socket = Socket(ip, port)

                var output = socket.getOutputStream()
                var dos = DataOutputStream(output)

                //var userData = "APP/LOGIN/${userLoginEmail.text.toString()}/${userLoginPW.text.toString()}"
                var userData = "APP/$userID/DELETEBUILDINGCODE/$removeAddr"
                dos.writeUTF(userData)
                dos.flush()

            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }
}