package com.example.firemen_firefighter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService


class SocketWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    val log = "worker"

    var userPref = context.getSharedPreferences("FighterInfo", Context.MODE_PRIVATE)
    var userID = userPref.getString("id", "")

    var activityContext = context
    var listSharedPreferences = JsonSharedPreference(activityContext, "FighterAddressInfo")
    var prefAddrList : MutableList<String> = listSharedPreferences.getList()//using shared preference



    override fun doWork(): Result {
        try{
            var thread = NetworkThread()
            thread.start()

            return Result.success()
        }catch(e : Exception){
            e.printStackTrace()
            return Result.failure()
        }

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

                var userData = "APP/${userID}/START"
                dos.writeUTF(userData)
                Log.i(log, "server response worker : $userData")
                dos.flush()

                var serverResponse : String = ""
                try{
                    var c : Int
                    do{
                        c = dis.read()
                        serverResponse += c.toChar()
                    }while(dis.available()>0)

                    serverResponse = String(serverResponse.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)//convert to utf-8
                    var strtoken = serverResponse.split("/")

                    if (prefAddrList.contains(strtoken[2])){//화재 발생 주소가 sharedPreference 에 들어있을 경우
                        var builder = getNotificationBuilder("channel3", "세 번째 채널")
                        builder.setTicker("Ticker")
                        builder.setSmallIcon(R.drawable.fire)
                        var bitmap = BitmapFactory.decodeResource(activityContext.resources, R.drawable.fire)
                        builder.setLargeIcon(bitmap)
                        builder.setNumber(1)
                        builder.setAutoCancel(true)
                        builder.setContentTitle("화재 발생")
                        builder.setContentText("${strtoken[2]} ${strtoken[3]} 화재 발생")

                        var notication = builder.build()

                        var mng = activityContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        mng.notify(10, notication)
                    }
                    Log.i(log, "server response worker : $serverResponse")

                }catch(e : Exception){

                }

            }catch(e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun getNotificationBuilder(id:String, name:String) : NotificationCompat.Builder{
        var builder: NotificationCompat.Builder? = null

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            var manager = activityContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            manager.createNotificationChannel(channel)

            builder = NotificationCompat.Builder(activityContext, id)

        }
        else{
            builder = NotificationCompat.Builder(activityContext)
        }

        return builder
    }
}