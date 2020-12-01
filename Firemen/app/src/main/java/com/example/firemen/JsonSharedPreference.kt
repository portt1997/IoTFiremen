package com.example.firemen

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonSharedPreference(context : Context, prefName : String) {//in order to save list at SharedPreference
    var pref : String = prefName
    var prefList : SharedPreferences = context.getSharedPreferences(pref, Context.MODE_PRIVATE)
    var prefListEdit : SharedPreferences.Editor = prefList.edit()

    //saving list in Shared Preference
    fun setList(list:MutableList<String>){
        val gson = Gson()
        val json = gson.toJson(list)//converting list to Json
        prefListEdit.putString("LIST",json)
        prefListEdit.commit()
    }
    //getting the list from shared preference
    fun getList():MutableList<String>{
        val gson = Gson()
        val json = prefList.getString("LIST",null)
        val type = object : TypeToken<MutableList<String>>(){}.type//converting the json to list

        return gson.fromJson(json,type)//returning the list
    }
    fun checkEmpty() : Boolean{
        var check = prefList.getString("LIST","empty")
        return check == "empty"
    }
}