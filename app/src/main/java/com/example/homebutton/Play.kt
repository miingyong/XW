package com.example.homebutton

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.example.homebutton.adapter.DramaRecyclerViewAdapter
import com.example.homebutton.entity.Drama
import com.example.homebutton.utils.MyCallBack
import com.example.homebutton.utils.Net
import com.example.homebutton.utils.StringUtil
import com.tencent.smtt.sdk.WebSettings
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.common_title.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Comment
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.regex.Pattern


class Play : BaseActivity() {
    val lineList = mutableListOf<String>("线路一","线路二","线路三")
    val lineData = mutableListOf<String>("http://jsap.attakids.com/?url=","https://jiexi.380k.com/?url=","http://17kyun.com/api.php?url=")
    val yuanList = mutableListOf<String>()
    val yuandataList = mutableListOf<String>()

    val centerList = mutableListOf<String>()
    val centerListData = mutableListOf<String>()

    val dramaBanner = mutableListOf<Drama>()
    val dramaData = mutableListOf<String>()

    var dramaAdapter : DramaRecyclerViewAdapter? = null

    var url:String? = null


    companion object{
        @JvmStatic
        var nowLine = "http://jsap.attakids.com/?url="
        @JvmStatic
        var nowDrama = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        supportActionBar?.hide()
        url = intent.getStringExtra("url").replace("//m.","//www.")

        loading()

        val sharedPreferences = getSharedPreferences("data", 0)
        val string = sharedPreferences.getString("line",null)
        if (string != null){
            centerList.clear()
            centerListData.clear()
            for (it in 0..2){
                if (lineData[it].equals(string)){
                    centerList.add(lineList[it])
                    centerListData.add(lineData[it])
                }
            }
            for (it in 0..2){
                if (!lineData[it].equals(string)){
                    centerList.add(lineList[it])
                    centerListData.add(lineData[it])
                }
            }
            lineList.clear()
            lineData.clear()
            for (it in 0..2){
                lineData.add(centerListData[it])
                lineList.add(centerList[it])
            }
        }

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lineList)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.adapter = arrayAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                nowLine = lineData[position]
                web.loadUrl(nowLine+nowDrama)
                val sharedPreferences = getSharedPreferences("data", 0).edit()
                sharedPreferences.putString("line", nowLine)
                sharedPreferences.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }



        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        dramaAdapter = DramaRecyclerViewAdapter(context!!,dramaBanner,false)
        drama.layoutManager = linearLayoutManager
        drama.adapter = dramaAdapter

        getDetails(url!!)
    }

    fun getDetails(url :String){
        Net.get(url,object : MyCallBack{
            @SuppressLint("WrongConstant")
            override fun callBack(doc: Document?) {
               if (doc == null){
                   return
               }

                val split = url.split("/")
                var flag = true
                for (it in split){
                    if (it.equals("va")){
                        flag = false
                    }
                }
                var jsonText = ""
                if (flag){
                    jsonText = StringUtil.getSubString(doc.toString(),"var serverdata =",";")
                }else{
                    jsonText = StringUtil.getSubString(doc.toString(),"var serverdata=",";")
                }

                Log.e("匹配",jsonText)

                val parseObject = JSON.parseObject(jsonText)
                val type = parseObject.getString("playtype")
                val ids = parseObject.getString("id")
                val cat = parseObject.getString("cat")
                val playArray = parseObject.getJSONArray("playsite")
                Log.e("匹配",type)
                Log.e("匹配",ids)
                Log.e("匹配",cat)

                val biaoTi = doc.select(".title-left h1").text()
                var select : Elements? = null
                if (flag){
                    if(type.equals("comics")){
                        select = doc.select(".item-wrap")
                        val item = select[0].select(".item")
                        val leiXings = item[0].getElementsByTag("a")
                        var leiXing = ""
                        for (it in leiXings){
                            leiXing+=it.text() + " "
                        }

                        val nianDai = item[1].text().replace("<span>年代 ：</span>","")
                        val diQu = item[2].text().replace("<span>地区 ：</span>","")


                        val counts = doc.select(".item-desc")
                        val count = counts[counts.size - 1].text()
                        Log.e("匹配",biaoTi)
                        Log.e("匹配",leiXing)
                        Log.e("匹配",nianDai)
                        Log.e("匹配",diQu)
                        Log.e("匹配",count)
                        activity?.runOnUiThread {
                            comTitle.setText(biaoTi)
                            miaoShu.setText("类型："+leiXing + "\n"+nianDai+"\n"+diQu+"\n"+"介绍："+count)
                        }
                    }else{
                        select = doc.select(".item-wrap")
                        val item = select[0].select(".item")
                        val leiXings = item[0].getElementsByTag("a")
                        var leiXing = ""
                        for (it in leiXings){
                            leiXing+=it.text() + " "
                        }

                        val nianDai = item[1].text().replace("<span>年代 ：</span>","")
                        val diQu = item[2].text().replace("<span>地区 ：</span>","")

                        val daoYans = item[3].getElementsByTag("a")
                        var daoYan = ""
                        for (it in daoYans){
                            daoYan+=it.text() + " "
                        }

                        val yanYuans = item[4].getElementsByTag("a")
                        var yanYuan = ""
                        for (it in yanYuans){
                            yanYuan+=it.text() + " "
                        }


                        val counts = doc.select(".item-desc")
                        val count = counts[counts.size - 1].text()
                        Log.e("匹配",biaoTi)
                        Log.e("匹配",leiXing)
                        Log.e("匹配",nianDai)
                        Log.e("匹配",diQu)
                        Log.e("匹配",daoYan)
                        Log.e("匹配",yanYuan)
                        Log.e("匹配",count)
                        activity?.runOnUiThread {
                            comTitle.setText(biaoTi)
                            miaoShu.setText("类型："+leiXing + "\n"+nianDai+"\n"+diQu+"\n"+"导演："+daoYan+"\n"+"演员："+yanYuan+"\n"+"介绍："+count)
                        }
                    }

                }else{
                    select = doc.select(".base-item-wrap")
                    val item = select[0].select(".item")
                    val leiXings = item[0].getElementsByTag("a")
                    var leiXing = ""
                    for (it in leiXings){
                        leiXing+=it.text() + " "
                    }

                    val nianDai = item[1].text().replace("<span>年代 ：</span>","")
                    val diQu = item[2].text().replace("<span>地区 ：</span>","")

                    val daoYans = item[3].getElementsByTag("a")
                    var daoYan = ""
                    for (it in daoYans){
                        daoYan+=it.text() + " "
                    }


                    val counts = doc.select(".item-desc")
                    val count = counts[counts.size - 1].text()
                    Log.e("匹配",biaoTi)
                    Log.e("匹配",leiXing)
                    Log.e("匹配",nianDai)
                    Log.e("匹配",diQu)
                    Log.e("匹配",daoYan)
                    Log.e("匹配",count)
                    activity?.runOnUiThread {
                        comTitle.setText(biaoTi)
                        miaoShu.setText("类型："+leiXing + "\n"+nianDai+"\n"+diQu+"\n"+"明星："+daoYan+"\n"+"介绍："+count)
                    }
                }




                when(type){
                    "tv" -> {
                        for (it in playArray){
                            val jsonObject = it as JSONObject
                            val cnsite = jsonObject.getString("cnsite")
                            val ensite = jsonObject.getString("ensite")
                            yuanList.add(cnsite)
                            yuandataList.add(ensite)
                        }


                        activity?.runOnUiThread {
                            val arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, yuanList)
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner1.adapter = arrayAdapter

                            spinner1.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    initTv(yuandataList[position],ids,cat)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }
                        }




                    }
                    "movie" -> {
                        yuanList.clear()
                        yuandataList.clear()
                        val select1 = doc.select(".js-site")
                        for (it in select1){
                            val cnsite = it.text()
                            val ensite = it.attr("href")
                            yuanList.add(cnsite)
                            yuandataList.add(ensite)
                        }

                        activity?.runOnUiThread {
                            val arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, yuanList)
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner1.adapter = arrayAdapter

                            spinner1.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    nowDrama = yuandataList[position]
                                    web.loadUrl(nowLine+nowDrama)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }
                        }

                    }
                    "arts" -> {
                        dramaData.clear()
                        dramaBanner.clear()

                        val titles = doc.select(".w-newfigure-hint")
                        val urls = doc.select(".js-link")
                        var i=0
                        for (it in titles){
                            val title = it.text()
                            val url  = urls[i].attr("href")
                            Log.e("匹配",title)
                            Log.e("匹配",url)
                            dramaBanner.add(Drama(title,url))
                            dramaData.add(url)
                            i+=1
                        }

                        yuanList.clear()
                        yuandataList.clear()
                        val select1 = doc.select(".ea-site")
                        for (it in select1){
                            val cnsite = it.text()
                            yuanList.add(cnsite)
                            yuandataList.add("")
                        }

                        activity?.runOnUiThread {
                            dramaAdapter = DramaRecyclerViewAdapter(context!!,dramaBanner,true)
                            drama.adapter = dramaAdapter

                            val arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, yuanList)
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner1.adapter = arrayAdapter

                            spinner1.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }
                        }

                        activity?.runOnUiThread {
                            dramaAdapter?.notifyDataSetChanged()
                            nowDrama = dramaData[0]
                            web.loadUrl(nowLine+nowDrama)
                        }


                    }
                    "comics" -> {


                        for (it in playArray){
                            val jsonObject = it as JSONObject
                            val cnsite = jsonObject.getString("cnsite")
                            val ensite = jsonObject.getString("ensite")
                            yuanList.add(cnsite)
                            yuandataList.add(ensite)
                        }


                        activity?.runOnUiThread {
                            val arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, yuanList)
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner1.adapter = arrayAdapter

                            spinner1.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    initCommic(yuandataList[position],ids,cat)
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {

                                }
                            }
                        }


                    }
                }

                activity?.runOnUiThread {
                    closeLoading()
                }

            }

        })

    }


    fun initTv(ensite :String,id : String , cat : String ){
        Net.get("https://www.360kan.com/cover/switchsitev2?site="+ensite+"&id="+id+"&category="+cat,object : MyCallBack{
            override fun callBack(doc: Document?) {
                if (doc == null){
                    return
                }

                dramaData.clear()
                dramaBanner.clear()
                val text : String = doc.body().html().replace("\\&quot;","")
                    .replace("&gt;","")
                    .replace("&lt;","")
                    .replace("\\\"","\"")
                    .replace("\\r","")
                Log.e("剧集输出",text)
                val subString = StringUtil.getSubString(text,"data\":\"", "\",\"error")
                Log.e("剧集输出",subString)

                val parse = Jsoup.parse(subString)
                val elementdt = parse.select(".num-tab-main")
                val element = elementdt[elementdt.size-1]
                val titles = element.getElementsByTag("a")
                for (it in titles){
                    val title = it.attr("data-num").replace("\\","").replace("\"","")
                    val url = it.attr("href").replace("\\","")
                    if (!url.equals("#")){
                        dramaBanner.add(Drama(title,url))
                        dramaData.add(url)
                        Log.e("剧集输出",title)
                        Log.e("剧集输出",url)
                    }
                }

                activity?.runOnUiThread {
                    dramaAdapter?.notifyDataSetChanged()
                    nowDrama = dramaData[0]
                    web.loadUrl(nowLine+nowDrama)
                }

                activity?.runOnUiThread {
                    closeLoading()
                }

            }
        })

    }

    fun initCommic(ensite :String,id : String , cat : String ){
        Net.get("https://www.360kan.com/cover/switchsitev2?site="+ensite+"&id="+id+"&category="+cat,object : MyCallBack{
            override fun callBack(doc: Document?) {
                if (doc == null){
                    return
                }

                dramaData.clear()
                dramaBanner.clear()
                val text : String = doc.body().html().replace("\\&quot;","")
                    .replace("&gt;","")
                    .replace("&lt;","")
                    .replace("\\\"","\"")
                    .replace("\\r","")
                    .replace("js-series-part\"","js-series-part")
                Log.e("剧集输出",text)
                val subString = StringUtil.getSubString(text,"data\":\"", "\",\"error")
                Log.e("剧集输出",subString)

                val parse = Jsoup.parse(subString)
                val elementdt = parse.select(".m-series-number-container")
                val element = elementdt[elementdt.size-1]
                val titles = element.getElementsByTag("a")
                for (it in titles){
                    val title = it.attr("data-num")
                        .replace("\\","")
                        .replace("\"","")
                        .replace("data-daochu=to=imgo","")
                        .replace("data-daochu=to=qq","")
                        .replace("data-daochu=to=qiyi","")
                        .replace("data-daochu=to=cntv","")
                        .replace("data-daochu=to=leshi","")
                        .replace("data-daochu=to=sohu","")
                        .replace("data-daochu=to=pptv","")
                        .replace("data-daochu=to=tudou","")
                        .replace("data-daochu=to=youku","")
                    val url = it.attr("href").replace("\\","")
                    if (!url.equals("###") && !url.equals("")){
                        dramaBanner.add(Drama(title,url))
                        dramaData.add(url)
                        Log.e("剧集输出",title)
                        Log.e("剧集输出",url)
                    }
                }

                activity?.runOnUiThread {
                    dramaAdapter?.notifyDataSetChanged()
                    nowDrama = dramaData[0]
                    web.loadUrl(nowLine+nowDrama)
                }

                activity?.runOnUiThread {
                    closeLoading()
                }

            }
        })

    }

}

