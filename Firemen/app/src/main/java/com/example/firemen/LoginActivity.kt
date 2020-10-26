package com.example.firemen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    var auth = Firebase.auth
    var log = "login"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        login_button.setOnClickListener{
            signIn()
        }

        go_signup.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    fun signIn(){

        auth.signInWithEmailAndPassword(userEmail.text.toString(), userPW.text.toString())
            .addOnCompleteListener {
                task ->
                if (task.isSuccessful) {
                    //Login
                    Log.d(log, "loginWithEmail:success")
                    moveAddrPage(task.result?.user)

                }
                else {
                    //Show the error message
                    Log.w(log, "loginWithEmail:failure", task.exception)
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveAddrPage(user:FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, AddressActivity::class.java))
        }

    }


}
