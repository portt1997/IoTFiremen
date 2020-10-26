package com.example.firemen

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    var log = "register"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        signup_button.setOnClickListener{
            signUp()
        }
        go_login.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun signUp() {
        var flag:Int = 0
        val userEmail = editTextEmail.text.toString()
        val userPW = editTextPassword.text.toString()
        val userPWCheck = editTextPasswordCheck.text.toString()
        val userName = editTextName.text.toString()
        val userPNumber = editTextPNumber.text.toString()
        lateinit var database: DatabaseReference

        if(userPW != userPWCheck){
            //Show the error message
            Log.w(log, "password check:failure")
            Log.w(log, userPW)
            Log.w(log, userPWCheck)
            Toast.makeText(this,"비밀번호 다시 확인해주세요.",Toast.LENGTH_LONG).show()
            flag =1
        }
        if(editTextEmail.text.toString().trim().isEmpty()){
            Log.w(log, "empty email:failure")
            Toast.makeText(this,"이메일을 입력해주세요.",Toast.LENGTH_LONG).show()
            flag =1
        }
        if(editTextName.text.toString().trim().isEmpty()){
            Log.w(log, "empty name:failure")
            Toast.makeText(this,"이름을 입력해주세요.",Toast.LENGTH_LONG).show()
            flag =1
        }
        if(editTextPNumber.text.toString().trim().isEmpty()){
            Log.w(log, "empty phone number:failure")
            Toast.makeText(this,"휴대폰 번호를 입력해주세요.",Toast.LENGTH_LONG).show()
            flag =1
        }
        if(flag == 1)
            return
        else {
            auth.createUserWithEmailAndPassword(
                editTextEmail.text.toString(),
                editTextPassword.text.toString()
            )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Creating a user account
                        Log.d(log, "createUserWithEmail:success")

                        val userID = auth.currentUser!!.uid
                        //val user = UserData(userEmail, userName, userPNumber)

                        var addressCode:MutableList<String> = mutableListOf()
                        var status:MutableList<Int> = mutableListOf()
                        var address:MutableList<String> = mutableListOf()

                        val a = data(userEmail, userName, userPNumber, addressCode, status, address)
                        database = FirebaseDatabase.getInstance().reference
                        database.child("users").child(userID).setValue(a)


                        moveLoginPage()
                    } else {
                        //Show the error message
                        Log.w(log, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

    }
    fun moveLoginPage(){
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
