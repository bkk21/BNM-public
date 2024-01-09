package com.khci.bnm.Chat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.khci.bnm.Home
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.khci.bnm.Home_nok
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PM_check_user_first
import com.khci.bnm.Retrofit2.PM_check_user_first_Result
import com.khci.bnm.memory.First_talk
import retrofit2.Response

class ChoiceTalk : AppCompatActivity() {

    val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_talk)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "대화 선택하기"

        first_check()

        val ch_memory = findViewById<android.widget.Button>(R.id.ch_memory)
        val ch_talk  = findViewById<android.widget.Button>(R.id.ch_talk)

        ch_memory.setOnClickListener{
            //val memory_go = Intent(this, MT_test::class.java)
            val memory_go = Intent(this, MemoryTest::class.java)
            startActivity(memory_go)
        }

        ch_talk.setOnClickListener{
            val talk_go = Intent(this, talkpage::class.java)
            startActivity(talk_go)
        }
    }

    fun showPopup(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("개인 정보 입력 요청")
        builder.setMessage("기억력 연습과 일상대화를 사용하기 위해서는 개인 정보 입력이 필요합니다.\n\n사용될 개인 정보를 작성해주세요!")

        // AlertDialog를 생성하고 리스너를 설정하지 않음 (null)
        val alertDialog = builder.setPositiveButton("확인", null)
            .setNegativeButton("취소", null)
            .create()

        // AlertDialog 표시
        alertDialog.show()

        // '확인' 및 '취소' 버튼의 텍스트 색상을 검정색으로 설정
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

        // '확인' 버튼의 클릭 이벤트를 별도로 처리
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val homeGo = Intent(context, First_talk::class.java)
            context.startActivity(homeGo)
            (context as Activity).finish()
        }

        // '취소' 버튼의 클릭 이벤트를 별도로 처리
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            val homeGo = Intent(context, Home::class.java)
            context.startActivity(homeGo)
            (context as Activity).finish()
        }
    }


    fun first_check(){

        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        val user_id = sharedPreference.getString("user_id", "test1").toString()
        val data = PM_check_user_first(user_id, "get")

        //통신 관련
        api.check_user_first(data).enqueue(object : retrofit2.Callback<PM_check_user_first_Result> {

            override fun onResponse(call: retrofit2.Call<PM_check_user_first_Result>, response: Response<PM_check_user_first_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 문장 실행
                if(!response.body().toString().isEmpty()){

                    val is_first = response.body()?.is_first

                    if (is_first == 1){
                        showPopup(this@ChoiceTalk)
                    }

                }
            }

            override fun onFailure(call: retrofit2.Call<PM_check_user_first_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }

    //툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val home_go = Intent(this, Home::class.java)
            startActivity(home_go)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}