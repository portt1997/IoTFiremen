package com.example.firemen

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class SocketInput : Thread(){
    private val ip = "172.30.1.111"
    private val port = 55555

    override fun run() {
        try{
            var socket = Socket(ip, port)

            var input = socket.getInputStream()
            var dis = DataInputStream(input)

            var output = socket.getOutputStream()
            var dos = DataOutputStream(output)

            var data1 = dis.readInt()
            var data2 = dis.readDouble()
            var data3 = dis.readUTF()

            socket.close()

        }catch(e:Exception){
            e.printStackTrace()
        }
    }
}