package com.example.firemen_admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firemen_admin.R
import com.example.firemen_admin.AddressActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class LoginActivity : AppCompatActivity() {
    var log = "login"
    var context : Context = this
    lateinit var pref : SharedPreferences// = this.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
    lateinit var edit : SharedPreferences.Editor// = pref.edit()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        pref = this.getSharedPreferences("AdminInfo", Context.MODE_PRIVATE)
        edit = pref.edit()

        val prefIsLogin : Boolean = pref.getBoolean("isLogin", false)

        if(prefIsLogin){
            moveAddrPage()
        }

        login_button.setOnClickListener{
            signIn()
        }


    }

    fun signIn(){
        var thread = NetworkThread()
        thread.start()

    }
    fun moveAddrPage(){
        var Intent = Intent(this, AddressActivity::class.java)

        pref = this.getSharedPreferences("AdminInfo", Context.MODE_PRIVATE)
        edit = pref.edit()

        val prefUserID = pref.getString("id", "")

        Intent.putExtra("id", prefUserID)
        startActivity(Intent)
    }

    inner class NetworkThread : Thread(){
        private val ip = "54.221.152.48"
        private val port = 4000


        override fun run() {
            try{
                var socket = Socket(ip, port)

                var input = socket.getInputStream()
                var dis = DataInputStream(input)

                var output = socket.getOutputStream()
                var dos = DataOutputStream(output)

                var userData = "APP/RLOGIN/${userLoginEmail.text.toString()}/${userLoginPW.text.toString()}"

                dos.writeUTF(userData)

                dos.flush()

                var serverResponse : String = ""
                try{
                    var c : Int
                    do{
                        c = dis.read()
                        serverResponse += c.toChar()
                    }while(dis.available()>0)
                    Log.i(log, "server response : $serverResponse")

                }catch(e : Exception){
                    e.printStackTrace()
                }
                if(serverResponse == "SERVER/LOGIN AVAILABLE"){
                    //로그인 성공 시
                    var id = userLoginEmail.text.toString()
                    var pw = userLoginPW.text.toString()
                    edit.putString("id", id)
                    edit.putString("pw", pw)
                    edit.putBoolean("isLogin", true)
                    edit.commit()

                    socket.close()//?
                    moveAddrPage()
                }
                else if(serverResponse == "SERVER/LOGIN NOT AVAILABLE"){
                    Toast.makeText(context,"로그인에 실패했습니다.", Toast.LENGTH_LONG).show()
                }
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }


}