package com.example.firemen

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_address.*
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.dialog_layout.view.*
import kotlinx.android.synthetic.main.dialog_layout.view.editTextAddr




class MyDialogFragment(context : Context) : DialogFragment(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var listener : MyDialogOKClickedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView:View = inflater.inflate(R.layout.dialog_layout, container, false)
        var yesButton = rootView.yes_button
        var noButton = rootView.no_button
        isCancelable = false
        yesButton.setOnClickListener(object :View.OnClickListener
        {
            override fun onClick(v: View?) {
                var log = "frag"
                Log.i(log, rootView.editTextAddr.text.toString())
                //Log.i(log, editTextAddr.text.toString())
                listener.onOKClicked(rootView.editTextAddr.text.toString())

                dismiss()
            }
        })
        noButton.setOnClickListener(object :View.OnClickListener
        {
            override fun onClick(v: View?) {

                dismiss()
            }
        })
        return rootView
    }

    fun setOnOKClickedListener(listener: (String) -> Unit) {
        this.listener = object: MyDialogOKClickedListener {
            override fun onOKClicked(content: String) {
                listener(content)
            }
        }
    }

    interface MyDialogOKClickedListener{
        fun onOKClicked(content : String)
    }
}