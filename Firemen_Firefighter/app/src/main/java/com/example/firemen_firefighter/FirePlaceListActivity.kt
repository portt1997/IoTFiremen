package com.example.firemen_firefighter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firemen.RecyclerItem
import kotlinx.android.synthetic.main.activity_fire_place_list.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class FirePlaceListActivity : AppCompatActivity() {
    var log = "firePlaceLIst"

    lateinit var address : String
    lateinit var adapterPlace : FirePlaceListAdapter
    lateinit var pref : SharedPreferences
    lateinit var prefUserID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fire_place_list)

        address = intent.getStringExtra("address")//get String from previous activity
        adapterPlace = FirePlaceListAdapter(this)
        pref = this.getSharedPreferences("FighterInfo", Context.MODE_PRIVATE)
        prefUserID = pref.getString("id", "").toString()

        fireEndButton.setOnClickListener{
            //삭제 소켓 전달
            var thread = NetworkThread("clearFire")
            thread.start()

            //intent 받아서 화재 건물 리스트로


            var Intent = Intent(this, AddressActivity::class.java)
            Intent.putExtra("id", prefUserID)
            startActivity(Intent)
        }
        adapterPlace.recyclerItemList = mutableListOf()
        fire_recycler_view.adapter = adapterPlace
        fire_recycler_view.layoutManager = LinearLayoutManager(this)

        var thread = NetworkThread("listAddr")
        thread.start()
    }

    override fun onBackPressed() {
        var Intent = Intent(this, AddressActivity::class.java)
        Intent.putExtra("id", prefUserID)
        startActivity(Intent)
    }

    inner class NetworkThread(var com : String) : Thread(){
        private val ip = "54.221.152.48"
        private val port = 4000

        override fun run() {
            try{
                var socket = Socket(ip, port)

                var input = socket.getInputStream()
                var dis = DataInputStream(input)

                var output = socket.getOutputStream()
                var dos = DataOutputStream(output)


                if(com == "listAddr"){
                    var userData = "APP/FHWLIST/$address"
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
                                var strtoken = serverResponse.split("/")
                                var data : String = ""
                                //add item to recycler view
                                runOnUiThread(){
                                    data = strtoken[0] + " " + strtoken[1] + "명 "
                                    if(strtoken[2] == "True") {//화재 근원지
                                        data += "화재 근원지 "
                                    }
                                    if(strtoken[3] == "True") {//기기 고장
                                        data += "기기 고장"
                                    }

                                    adapterPlace.recyclerItemList.add(RecyclerItem(R.drawable.fire, data))
                                    adapterPlace.notifyItemInserted(adapterPlace.itemCount-1)

                                    Log.i(log, "server response : $data")

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
                }
                else{//click 화재 진압 완료 버튼
                    var userData = "APP/FIRECANCELLED/$address"

                    dos.writeUTF(userData)
                    dos.flush()

                    Log.i(log, "server response : $userData")
                }
                socket.close()
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }
}