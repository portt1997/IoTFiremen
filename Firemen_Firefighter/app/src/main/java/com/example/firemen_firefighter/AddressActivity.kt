package com.example.firemen_firefighter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.firemen.AddressListAdapter
import com.example.firemen.RecyclerItem
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.logout_dialog.view.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.concurrent.TimeUnit

class AddressActivity : AppCompatActivity() {
    var log = "address"
    lateinit var userID : String
    lateinit var adapter : AddressListAdapter
    lateinit var addrPref : SharedPreferences
    lateinit var addrPrefEdit : SharedPreferences.Editor
    var context : Context = this
    var mBackWait:Long = 0//뒤로가기 연속 클릭 대기 시간

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        userID = intent.getStringExtra("id")//get String from previous activity

        adapter = AddressListAdapter(this)

        logoutButton.setOnClickListener{

            val inflater = LayoutInflater.from(this)
            var rootView: View = inflater.inflate(R.layout.logout_dialog, null)

            var builder = android.app.AlertDialog.Builder(this).setView(rootView)
            val alertDialog = builder.show()

            rootView.yesLogout.setOnClickListener{
                var userPref = this.getSharedPreferences("FighterInfo", Context.MODE_PRIVATE)
                var userPrefEdit = userPref.edit()
                addrPref = this.getSharedPreferences("FighterAddressInfo", Context.MODE_PRIVATE)
                addrPrefEdit = addrPref.edit()
                //clear SharedPreference
                userPrefEdit.clear()
                userPrefEdit.commit()
                addrPrefEdit.clear()
                addrPrefEdit.commit()

                var intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

                alertDialog.dismiss()
            }
            rootView.noLogout.setOnClickListener {
                alertDialog.dismiss()
            }

        }

        adapter.recyclerItemList = mutableListOf()
        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        var thread = NetworkThread()
        thread.start()

        doWorkPeroiodic()
    }

    override fun onBackPressed() {//if you click button twice quickly, program will be ended
        // click back button
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(context,"한번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show()
        } else {
            finishAffinity()//종료
        }
    }

    fun doWorkPeroiodic(){
        val workRequest = PeriodicWorkRequestBuilder<SocketWorker>(1, TimeUnit.SECONDS)
            .build()
        WorkManager
            .getInstance(context)
            .enqueue(workRequest)
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

                var userData = "APP/${userID}/BUILDINGLIST"
                dos.writeUTF(userData)
                dos.flush()
                Log.i(log, "server response : $userData")

                var serverResponse : String = ""
                var fireCount : Int = 0
                try{
                    var c : Int //ues get data using input stream
                    var prefAddrList : MutableList<String> = mutableListOf()//using shared preference
                    do{
                        c = dis.read()
                        if(c.toChar()=='~'){
                            serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)//convert to utf-8
                            var strtoken = serverResponse.split("/")

                            //add item to recycler view
                            runOnUiThread(){
                                if(strtoken[1] == "True") {//fire outbreak
                                    adapter.recyclerItemList.add(RecyclerItem(R.drawable.fire, strtoken[0]))
                                    adapter.notifyItemInserted(adapter.itemCount-1)
                                    fireCount++
                                }
                                else{//normal
                                    //adapter.recyclerItemList.add(RecyclerItem(R.drawable.safe, strtoken[0]))
                                    //adapter.notifyItemInserted(adapter.itemCount-1)
                                }
                                Log.i(log, "server response : ${strtoken[0]}")
                            }

                            //get list from SharedPreference, then add last item to SharedPreference
                            var listSharedPreferences = JsonSharedPreference(context, "FighterAddressInfo")
                            prefAddrList.add(strtoken[0])
                            listSharedPreferences.setList(prefAddrList)

                            Log.i(log, "server response list : $serverResponse")
                            serverResponse=""
                            continue
                        }
                        serverResponse += c.toChar()
                    }while(dis.available()>0)

                    if(fireCount == 0){//화재 발생하지 않았을 때 recyclerView 에 하나 추가
                        runOnUiThread(){
                            var recyclerStr : String = "화재가 발생하지 않았습니다."
                            adapter.recyclerItemList.add(RecyclerItem(R.drawable.white, recyclerStr))
                            adapter.notifyItemInserted(adapter.itemCount-1)

                            Log.i(log, "server response : $recyclerStr")
                        }
                    }

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