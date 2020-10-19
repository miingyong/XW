package com.example.homebutton.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.homebutton.Play
import com.example.homebutton.R
import com.example.homebutton.entity.FaXian
import com.example.homebutton.entity.MoverCount


class FaXianGridViewAdapter(val context: Context,data1: List<FaXian>) : BaseAdapter() {

    val data : List<FaXian> = data1

    override fun getCount(): Int {
       return data.size
    }

    override fun getItem(position: Int): Any {
        return data.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view:View ?= null
        var myHolder:MyHolder ?= null

        if(convertView == null){
            myHolder = MyHolder()
            view = LayoutInflater.from(context).inflate(R.layout.faxian_gridview_adapater ,null)
            myHolder.img = view.findViewById(R.id.img)
            myHolder.text = view.findViewById(R.id.text)
            view.tag = myHolder
        }else{
            view = convertView
            myHolder = view.tag as MyHolder
        }

        if (data.size>0){
            myHolder.text.setText(data.get(position).text)
            myHolder.img.setImageResource(data.get(position).img)
        }
        return view!!
    }

    inner class MyHolder(){
        lateinit var img : ImageView
        lateinit var text : TextView
    }
}