package com.example.firemen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.dialog_layout.view.*


class AddressActivity : AppCompatActivity() {

    //lateinit var addrCode : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        add_address_button.setOnClickListener{
            //showDialog()
            var addressCode: String = "before"
            val fm = supportFragmentManager
            val myFragment=MyDialogFragment(this)
            myFragment.show(fm, "Fragment")

            myFragment.setOnOKClickedListener { context->
                //textView4.text =  context
                addressCode = context


            }
            var log = "addr"
            Log.i(log, addressCode)
            //Log.i(log, editTextAddr.text.toString())
            //textView4.text = addressCode
        }

        val adapter = MyAdapter()
        //adapter.itemList = listOf(Item("add1"), Item("add2"), Item("add3"))
        adapter.recyclerItemList = mutableListOf(RecyclerItem(R.drawable.fire,"충청북도 청주시 245-7 개신동 기원빌"), RecyclerItem(R.drawable.safe,"충청북도 청주시 235-8 개신동 나눔빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)


    }



    private fun showDialog(){
        val inflater = LayoutInflater.from(this)
        val builder = AlertDialog.Builder(this)
            .setView(inflater.inflate(R.layout.dialog_layout, null))

        //
        // builder.setView(inflater.inflate(R.layout.dialog_layout, null))

        builder.setCancelable(false)

        inflater.inflate(R.layout.dialog_layout, null).yes_button.setOnClickListener {
            var addressCode = editTextAddr.text.toString()
            //tv_result.setText(addressCode)
            //dialog.dismiss()
            var log = "addr"
            Log.w(log, addressCode)
        }
        inflater.inflate(R.layout.dialog_layout, null).no_button.setOnClickListener {
            //dialog.dismiss()
        }
        //dialog.show()
        builder.create()
    }
}

