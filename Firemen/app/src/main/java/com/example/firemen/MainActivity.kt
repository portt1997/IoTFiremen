package com.example.firemen

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth

        //네트워크 연결 확인
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetwork

        if (networkInfo != null){
        } else{
            var yesListener = object:DialogInterface.OnClickListener{
                override fun onClick(p0: DialogInterface?, p1: Int){
                    finishAffinity()
                }
            }

            var builder = AlertDialog.Builder(this)
            builder.setMessage("네트워크를 연결해주세요.")
            builder.setPositiveButton("확인", yesListener)
            builder.setCancelable(false)
            builder.show()

        }

        //로그인 버튼 클릭 시
        go_login_button.setOnClickListener{
            Toast.makeText(this,"Go Sign In",Toast.LENGTH_LONG).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        // 회원가입 버튼 클릭 시
        go_singup_button.setOnClickListener(){
            Toast.makeText(this,"Go Sign Up",Toast.LENGTH_LONG).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
    public override fun onStart(){
        super.onStart()
        //Check if user is signed in (non-null) and update UI accordingly
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            if (user.isEmailVerified) {
                startActivity(Intent(this, AddressActivity::class.java))
            }
        }
    }


}
