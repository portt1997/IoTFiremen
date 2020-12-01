package com.example.firemen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.logout_dialog.view.*
import kotlinx.android.synthetic.main.recycler_item.view.*
import kotlinx.android.synthetic.main.remove_dialog.view.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


class AddressActivity : AppCompatActivity() {

    //lateinit var addrCode : String
    var log = "address"
    lateinit var userID : String
    lateinit var adapter : MyAdapter
    var context : Context = this
    var addressCode: String = "before"
    lateinit var addrPref : SharedPreferences
    lateinit var addrPrefEdit : SharedPreferences.Editor
    //var listSize : Int = 0
    var mBackWait:Long = 0//뒤로가기 연속 클릭 대기 시간

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        userID = intent.getStringExtra("id")//get String from previous activity
        adapter = MyAdapter(this, userID)
        //addrPref = this.getSharedPreferences("AddressInfo", Context.MODE_PRIVATE)
        //addrPrefEdit = addrPref.edit()

        add_address_button.setOnClickListener{
            //showDialog()

            val fm = supportFragmentManager
            val myFragment=MyDialogFragment(this)
            myFragment.show(fm, "Fragment")

            myFragment.setOnOKClickedListener { addrContext->
                //textView4.text =  context
                addressCode = addrContext
                var thread = NetworkThread("addAddr")
                thread.start()
            }

            Log.i(log, addressCode)
        }

        logoutButton.setOnClickListener{

            val inflater = LayoutInflater.from(this)
            var rootView: View = inflater.inflate(R.layout.logout_dialog, null)

            var builder = android.app.AlertDialog.Builder(this).setView(rootView)
            val alertDialog = builder.show()

            rootView.yesLogout.setOnClickListener{
                var userPref = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
                var userPrefEdit = userPref.edit()
                addrPref = this.getSharedPreferences("AddressInfo", Context.MODE_PRIVATE)
                addrPrefEdit = addrPref.edit()
                //clear SharedPreference
                userPrefEdit.clear()
                userPrefEdit.commit()
                addrPrefEdit.clear()
                addrPrefEdit.commit()

                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                alertDialog.dismiss()
            }
            rootView.noLogout.setOnClickListener {
                alertDialog.dismiss()
            }

        }

        //val adapter = MyAdapter(this)
        adapter.recyclerItemList = mutableListOf()
        //adapter.itemList = listOf(Item("add1"), Item("add2"), Item("add3"))
        //adapter.recyclerItemList = mutableListOf(RecyclerItem(R.drawable.fire,"충청북도 청주시 245-7 개신동 기원빌"), RecyclerItem(R.drawable.safe,"충청북도 청주시 235-8 개신동 나눔빌"))
        //adapter.recyclerItemList = mutableListOf()

        //adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        //adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe,"충청북도 청주시 245-7 개신동 기원빌"))
        //adapter.notifyItemInserted(adapter.itemCount-1)

        var thread = NetworkThread("listAddr")
        thread.start()
        doWorkPeroiodic()
    }


    /*
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
    */

    fun doWorkPeroiodic(){
        //var workRequest = PeriodicWorkRequestBuilder<SocketWorker>(1, TimeUnit.HOURS).build()
        val workRequest = PeriodicWorkRequestBuilder<SocketWorker>(1, TimeUnit.SECONDS)
            // Additional configuration
            .build()
//        val workManager = WorkManager.getInstance()
//        workManager?.enqueue(workRequest)
        WorkManager
            .getInstance(context)
            .enqueue(workRequest)
    }

    override fun onBackPressed() {//if you click button twice quickly, program will be ended
        // click back button
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(context,"한번 더 누르면 종료됩니다.",Toast.LENGTH_LONG).show()
        } else {
            finishAffinity()//종료
        }
    }



    inner class NetworkThread(var com : String) : Thread(){
        private val ip = "54.221.152.48"
        private val port = 4000

        override fun run() {
            try{

                Log.i(log, "server response : 1")
                var socket = Socket(ip, port)

                var input = socket.getInputStream()
                var dis = DataInputStream(input)
                Log.i(log, "server response : 2")
                var output = socket.getOutputStream()
                var dos = DataOutputStream(output)
                Log.i(log, "server response : 3")

                if(com == "listAddr"){
                    var userData = "APP/${userID}/BUILDINGLIST"
                    dos.writeUTF(userData)
                    dos.flush()
                    Log.i(log, "server response : $userData")

                    var serverResponse : String = ""

                    try{
                        //var c : Byte
                        var c : Int //ues get data using input stream
                        var prefAddrList : MutableList<String> = mutableListOf()//using shared preference
                        do{
                            //c = dis.readByte()// c : Byte 일 때
                            //c = input.read() //아래랑 같음
                            c = dis.read()

                            if(c.toChar()=='~'){
                                serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)//convert to utf-8
                                var strtoken = serverResponse.split("/")

                                //add item to recycler view
                                runOnUiThread(){
                                    if(strtoken[1] == "True") {//fire outbreak
                                        adapter.recyclerItemList.add(RecyclerItem(R.drawable.fire, strtoken[0]))
                                        adapter.notifyItemInserted(adapter.itemCount-1)
                                    }
                                    else{//normal
                                        adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe, strtoken[0]))
                                        adapter.notifyItemInserted(adapter.itemCount-1)

                                        Log.i(log, "server response : ${strtoken[0]}")
                                    }
                                }

                                //get list from SharedPreference, then add last item to SharedPreference
                                var listSharedPreferences = JsonSharedPreference(context, "AddressInfo")
                                prefAddrList.add(strtoken[0])
                                listSharedPreferences.setList(prefAddrList)

                                Log.i(log, "server response list : $serverResponse")
                                serverResponse=""
                                continue
                            }
                            serverResponse += c.toChar()
                        }while(dis.available()>0)


//                        var c : Int
//                        //var c : String
//                        //var c : Char
//
//                        do{
//                            //c = dis.readByte()// c : Byte 일 때
//                            //c = input.read() //아래랑 같음
//                            c = dis.read()
//                            Log.i(log, "server response d: $c")
//                            serverResponse += c.toChar()
//
//                        }while(dis.available()>0)
//
//                        serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
//                        var strtoken = serverResponse.split("~")
//                        for(str in strtoken){
//                            var add = str.split("/")
//                            if(add[1] == "True") {
//                                adapter.recyclerItemList.add(RecyclerItem(R.drawable.fire, strtoken[0]))
//                                //adapter.notifyItemInserted(adapter.itemCount-1)
//                            }
//                            else{
//                                Log.i(log, "server response : 6")
//                                adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe, strtoken[0]))
//                                //adapter.notifyItemInserted(adapter.itemCount-1)
//
//                            }
//                        }
//
//                        runOnUiThread() {
//                            adapter.notifyItemInserted(adapter.itemCount - 1)
//                        }

//                        Log.i(log, "server response : $serverResponse")
                    }catch(e : Exception){
                        e.printStackTrace()
                    }
                }
                else{//click add button to add address
                    var userData = "APP/${userID}/GETBUILDINGCODE/${addressCode}"

                    dos.writeUTF(userData)
                    dos.flush()

                    Log.i(log, "server response : $userData")
                    try{
                        var serverResponse : String = ""
                        //var c : Byte
                        var c : Int
                        do{
                            //c = dis.readByte()// c : Byte 일 때
                            //c = input.read() //아래랑 같음
                            c = dis.read()
                            serverResponse += c.toChar()
                        }while(dis.available()>0)
                        serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)//convert to utf-8
                        Log.i(log, "server response : $serverResponse")
                        var strtoken = serverResponse.split("/")

                        if(strtoken[1] == "NOT AVAILABLE ADDRESS"){
                            Log.i(log, "server response : $strtoken[1]")
                        } else if(strtoken[1] == "DUPLICATED ADDRESS"){
                            Log.i(log, "server response : $strtoken[1]")
                        }else{//available, strtoken is building address
                            Log.i(log, "server response success : $strtoken[1]")
                            adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe, strtoken[1]))
                            adapter.notifyItemInserted(adapter.itemCount -1 );

                            //get list from SharedPreference, then add last item to SharedPreference
                            var prefAddrList : MutableList<String>
                            var listSharedPreferences = JsonSharedPreference(context, "AddressInfo")

                            if(listSharedPreferences.checkEmpty()){//if you don't have sharedPreference
                                prefAddrList = mutableListOf()
                            }else{
                                prefAddrList = listSharedPreferences.getList()
                            }
                            prefAddrList.add(strtoken[1])
                            listSharedPreferences.setList(prefAddrList)
                        }

                    }catch(e : Exception){
                        e.printStackTrace()
                    }
                }
                if(socket.isClosed)
                    Log.i(log, "server response : isClosed true 1")
                else
                    Log.i(log, "server response : isClosed false 1")
                socket.close()
                if(socket.isClosed)
                    Log.i(log, "server response : isClosed true 2")
                else
                    Log.i(log, "server response : isClosed false 2")
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }
}

