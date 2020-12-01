package com.example.firemen

import java.io.DataOutputStream
import java.net.Socket

class SocketClass{
    private val ip : String = "54.221.152.48"
    private val port : Int = 4000

    inner class SendSocketThread(ip: String, port:Int): Thread(){
        var socket = Socket(ip, port)
        var output = socket.getOutputStream()
        var dos = DataOutputStream(output)

        fun sendData(data : String){
            dos.writeUTF(data)
            dos.flush()
        }
    }

    inner class ReceiveSocketThread(): Thread(){

        fun receiveData(): String{
            val d = ""
            return d
        }
    }

}