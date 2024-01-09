package com.khci.bnm.memory

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.khci.bnm.Chat.Chat_log
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PM_get_main_nok_info
import com.khci.bnm.Retrofit2.PM_get_main_nok_info_Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Choice_quiz_log_user : AppCompatActivity() {
    //api 생성
    val api = APIS.create()

    lateinit var log: LinearLayout
    var num = 0
    var userList: List<String> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_quiz_log_user)
        get_user_id()

        log = findViewById(R.id.log)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "퀴즈 기록"


    }

    // 툴바 아이템 클릭 이벤트 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 뒤로 가기 버튼 클릭 시
        if (item.itemId == android.R.id.home) {
            finish() // 현재 액티비티 종료
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setButtonEvents(){

        val btn_num : Int = num

        for (i in 0 until btn_num) {
            val btn: android.widget.Button? = findViewById(i)
            val btn_i = i

            btn?.setOnClickListener{
                for (i in 0 until btn_num) {
                    if(i == btn_i)
                        continue
                    val btn2: android.widget.Button? = findViewById(i)
                    val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn)
                    btn2?.background = backgroundDrawable
                }

                val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn2)
                btn?.background = backgroundDrawable

                val btn_text = btn.text.toString()


                val Chat_log_go = Intent(this, quiz_log::class.java)
                val sharedPreference = getSharedPreferences("quiz_log", Context.MODE_PRIVATE)
                val editor = sharedPreference?.edit()
                editor?.putString("log_user_id", btn_text)
                editor?.apply() // 데이터 저장
                startActivity(Chat_log_go)
                finish()

            }
        }
    }


    private fun createButton(j:Int) {

        // 버튼 생성
        val newButton: android.widget.Button = android.widget.Button(applicationContext)

        // 버튼 텍스트 설정
        newButton.text = userList[j]

        // 버튼 텍스트 크기
        newButton.textSize = 30f

        // 버튼 텍스트 타입 설정
        newButton.setTypeface(null, Typeface.BOLD)

        // 배경 설정
        val backgroundDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.join_gender_btn)
        newButton.background = backgroundDrawable

        // id 설정
        newButton.id = num
        num += 1

        // 레이아웃 설정
        val param: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // 상단 마진 설정 (20dp)
        val marginTop = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics).toInt()
        param.topMargin = marginTop

        // 오른쪽 정렬 설정
        param.gravity = Gravity.CENTER

        // margin 설정
        param.marginEnd = 50
        param.marginStart = 50

        // padding 설정
        newButton.setPadding(50, 50, 50, 50)

        // 글자색 설정
        val color = ContextCompat.getColor(applicationContext, R.color.black)
        newButton.setTextColor(color)

        // 적용
        newButton.layoutParams = param

        //클릭 효과 삭제
        newButton.stateListAnimator = null

        log.addView(newButton)
    } //createButton

    fun get_user_id(){
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        val nok_id = sharedPreference.getString("user_id", "데이터 존재 x").toString()

        val data = PM_get_main_nok_info(nok_id)

        val msg : TextView = findViewById(R.id.msg)

        api.get_main_nok_info(data).enqueue(object : Callback<PM_get_main_nok_info_Result> {

            override fun onResponse(call: Call<PM_get_main_nok_info_Result>, response: Response<PM_get_main_nok_info_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 문장 실행
                if(!response.body().toString().isEmpty()){

                    // Response에서 user_list 추출하여 userList에 할당
                    userList = response.body()?.user_list ?: emptyList()

                    val list_size: Int? = response.body()?.user_list?.size

                    if(list_size != null) {
                        for (j in 0 until list_size) {
                            createButton(j)
                            setButtonEvents()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PM_get_main_nok_info_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        }) //통신 끝
    } // 함수 끝

}