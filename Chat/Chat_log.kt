package com.khci.bnm.Chat

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PM_get_user_chat_log
import com.khci.bnm.Retrofit2.PM_get_user_chat_log_Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Chat_log : AppCompatActivity() {


    //api 생성
    val api = APIS.create()

    var chat_log = mutableListOf<Map<String, String>>()


    lateinit var log: LinearLayout
    var num = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        get_user_chat_log()


        log = findViewById(R.id.log)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "대화 기록"


        val date : TextView = findViewById(R.id.date)


    }//OnCreate

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val home_go = Intent(this, com.khci.bnm.Chat.Choice_user_log::class.java)
            startActivity(home_go)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createView_user(value:String) {

        // 텍스트뷰 생성
        val newtextview: TextView = TextView(applicationContext)

        // 텍스트 뷰 글자 설정
        newtextview.text = value

        // 텍스트뷰 글자 크기
        newtextview.textSize = 20f

        // 텍스트뷰 글자 타입 설정
        newtextview.setTypeface(null, Typeface.BOLD)

        // 배경 설정
        val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn2)
        newtextview.background = backgroundDrawable

        // id 설정
        newtextview.id = num
        num += 1

        // 레이아웃 설정
        val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // 상하단 마진 설정 (각각 10dp)
        val marginVertical = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        param.topMargin = marginVertical
        param.bottomMargin = marginVertical


        // 오른쪽 정렬 설정
        param.gravity = Gravity.END

        // margin 설정
        param.marginStart = 150
        param.marginEnd = 30

        // padding 설정
        newtextview.setPadding(50, 50, 50, 50)

        // 글자색 설정
        val color = ContextCompat.getColor(applicationContext, R.color.black)
        newtextview.setTextColor(color)

        // 적용
        newtextview.layoutParams = param

        log.addView(newtextview)
    } //createView_user

    private fun createView_gpt(value:String) {

        // 텍스트뷰 생성
        val newtextview: TextView = TextView(applicationContext)

        // 텍스트 뷰 글자 설정
        newtextview.text = value

        // 텍스트뷰 글자 크기
        newtextview.textSize = 20f

        // 텍스트뷰 글자 타입 설정
        newtextview.setTypeface(null, Typeface.BOLD)

        // 배경 설정
        val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn)
        newtextview.background = backgroundDrawable

        // id 설정
        newtextview.id = num
        num += 1

        // 레이아웃 설정
        val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // 상하단 마진 설정 (각각 10dp)
        val marginVertical = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        param.topMargin = marginVertical
        param.bottomMargin = marginVertical

        // 오른쪽 정렬 설정
        param.gravity = Gravity.START

        // margin 설정
        param.marginStart = 30
        param.marginEnd = 150

        // padding 설정
        newtextview.setPadding(50, 50, 50, 50)

        // 글자색 설정
        val color = ContextCompat.getColor(applicationContext, R.color.black)
        newtextview.setTextColor(color)

        // 적용
        newtextview.layoutParams = param

        log.addView(newtextview)
    } //createView_gpt

    private fun createView_date(date : String) {
        // 텍스트뷰 생성
        val newtextview: TextView = TextView(applicationContext)

        // 텍스트 뷰 글자 설정
        newtextview.text = "$date 대화 기록"

        // 텍스트뷰 글자 크기
        newtextview.textSize = 20f

        // 텍스트뷰 글자 타입 설정
        newtextview.setTypeface(null, Typeface.BOLD)

        /*// 배경 설정
        val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn)
        newtextview.background = backgroundDrawable*/

        // id 설정
        newtextview.id = num
        num += 1

        // 레이아웃 설정
        val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // 상단 마진 설정 (20dp)
        val marginTop = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
        param.topMargin = marginTop

        // 중앙 정렬
        param.gravity = Gravity.CENTER


        // padding 설정
        newtextview.setPadding(20, 20, 20, 20)

        // 글자색 설정
        val color = ContextCompat.getColor(applicationContext, R.color.black)
        newtextview.setTextColor(color)

        // 적용
        newtextview.layoutParams = param

        log.addView(newtextview)
    }//날짜 넣기

    private fun createView_line() {
        // View 객체 생성
        val newLine: View = View(applicationContext)

        // 선의 높이 설정 (예: 2dp)
        val lineHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt()

        // 배경색 설정 (회색)
        newLine.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.gray))

        // 레이아웃 설정
        val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // 선의 너비를 부모 뷰에 맞춤
            lineHeight // 선의 높이
        )

        // 상단 마진 설정 (예: 20dp)
        val marginTop = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
        param.topMargin = marginTop

        // margin 설정
        param.marginStart = 30
        param.marginEnd = 30

        // 적용
        newLine.layoutParams = param

        // 뷰 추가
        log.addView(newLine)
    }//선 만들기

    //로그 받아오기
    fun get_user_chat_log(){

        val sharedPreferences = getSharedPreferences("chat_log", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()

        val log_user_id = sharedPreferences?.getString("log_user_id", "").toString()

        //보내줄 data
        val data = PM_get_user_chat_log(log_user_id)
        api.get_user_chat_log(data).enqueue(object : Callback<PM_get_user_chat_log_Result> {

            override fun onResponse(call: Call<PM_get_user_chat_log_Result>, response: Response<PM_get_user_chat_log_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 문장 실행
                if(!response.body().toString().isEmpty()){
                    chat_log = response.body()?.log!!
                    var date = response.body()?.date!!
                    //Log.d("log", chat_log.toString())

                    for (map in chat_log) {
                        for ((key, value) in map) {
                            //Log.d("ChatLog", "Key: $key, Value: $value")
                            if(key == "user" && value == "안녕"){
                                createView_line()
                                createView_date(date)
                            }

                            else{
                                if(key == "assistant")
                                    createView_gpt(value)
                                else
                                    createView_user(value)
                            }
                        }
                    }

                }
            }

            override fun onFailure(call: Call<PM_get_user_chat_log_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })//통신 끝
    }

}