package com.example.firemen_admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firemen.RecyclerItem
import kotlinx.android.synthetic.main.activity_broken_divice_list.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class BrokenDeviceListActivity : AppCompatActivity() {
    var log = "deviceLIst"

    lateinit var address : String
    lateinit var adapterDevice : BrokenDeviceListAdapter
    lateinit var pref : SharedPreferences
    lateinit var prefUserID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broken_divice_list)

        address = intent.getStringExtra("address")//get String from previous activity
        adapterDevice = BrokenDeviceListAdapter(this)
        pref = this.getSharedPreferences("AdminInfo", Context.MODE_PRIVATE)
        prefUserID = pref.getString("id", "").toString()

        adapterDevice.recyclerItemList = mutableListOf()
        device_recycler_view.adapter = adapterDevice
        device_recycler_view.layoutManager = LinearLayoutManager(this)

        var thread = NetworkThread()
        thread.start()
    }

    override fun onBackPressed() {
        var Intent = Intent(this, AddressActivity::class.java)
        Intent.putExtra("id", prefUserID)
        startActivity(Intent)
    }

    inner class NetworkThread() : Thread(){
        private val ip = "54.221.152.48"
        private val port = 4000

        override fun run() {
            try{
                var socket = Socket(ip, port)

                var input = socket.getInputStream()
                var dis = DataInputStream(input)

                var output = socket.getOutputStream()
                var dos = DataOutputStream(output)



                var userData = "APP/RHWLIST/$address"
                dos.writeUTF(userData)
                dos.flush()
                Log.i(log, "server response : $userData")

                var serverResponse : String = ""

                try{
                    var c : Int //ues get data using input stream

                    do{
                        c = dis.read()

                        if(c.toChar()=='~'){
                            serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)//convert to utf-8

                            //add item to recycler view
                            runOnUiThread(){
                                adapterDevice.recyclerItemList.add(RecyclerItem(R.drawable.caution, serverResponse))
                                adapterDevice.notifyItemInserted(adapterDevice.itemCount-1)

                                Log.i(log, "server response : $serverResponse")

                            }

                            Log.i(log, "server response list : $serverResponse")
                            serverResponse=""
                            continue
                        }
                        serverResponse += c.toChar()
                    }while(dis.available()>0)

                }catch(e : Exception){
                    e.printStackTrace()
                }
                socket.close()
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }
}