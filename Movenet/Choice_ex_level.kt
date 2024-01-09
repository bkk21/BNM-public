package com.khci.bnm.Movenet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.khci.bnm.Home
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PM_check_user_exercise_first
import com.khci.bnm.Retrofit2.PM_check_user_exercise_first_Result
import retrofit2.Response

class Choice_ex_level : AppCompatActivity() {
    val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice_ex_level)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "운동 단계 선택"

        val lv1 = findViewById<android.widget.Button>(R.id.ch_lv1)
        val lv2 = findViewById<android.widget.Button>(R.id.ch_lv2)
        val lv3 = findViewById<android.widget.Button>(R.id.ch_lv3)

        val sharedPreference = getSharedPreferences("ex_level", MODE_PRIVATE)

        showPopup_warning(this)
        //first_check()

        lv1.setOnClickListener{
            val lv1_go = Intent(this, MoveNet::class.java)
            val editor  : SharedPreferences.Editor = sharedPreference.edit()
            editor.putInt("ch_level", 1)
            editor?.apply()
            startActivity(lv1_go)
        }

        lv2.setOnClickListener{
            val lv1_go = Intent(this, MoveNet::class.java)
            val editor  : SharedPreferences.Editor = sharedPreference.edit()
            editor.putInt("ch_level", 2)
            editor?.apply()
            startActivity(lv1_go)
        }

        lv3.setOnClickListener{
            val lv1_go = Intent(this, MoveNet::class.java)
            val editor  : SharedPreferences.Editor = sharedPreference.edit()
            editor.putInt("ch_level", 3)
            editor?.apply()
            startActivity(lv1_go)
        }
    }

    fun showPopup(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("레벨테스트 요청")
        builder.setMessage("운동하기 전 적정 단계를 확인하기 위해 레벨 테스트를 진행해야 합니다.\n\n확인을 누르면 레벨테스트로 진입합니다!")

        // AlertDialog를 생성하지만 리스너는 null로 설정
        val alertDialog = builder.setPositiveButton("확인", null)
            .setNegativeButton("취소", null)
            .setCancelable(false) // 팝업이 취소될 수 없도록 설정
            .create()

        // AlertDialog 표시
        alertDialog.show()

        // '확인' 및 '취소' 버튼의 텍스트 색상을 검정색으로 설정
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

        // '확인' 버튼의 클릭 이벤트를 별도로 처리
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val homeGo = Intent(context, First_Test::class.java)
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

    fun showPopup_warning(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("주의 사항")
        builder.setMessage("해당 운동을 시작하기 앞서 전문가의 판단이 필요합니다.\n\n 꼭 병원을 방문하여 사용자의 신체 상태에 대해 운동 가능 여부를 진단 받으시길 바랍니다.")

        // AlertDialog를 생성하지만 리스너는 null로 설정
        val alertDialog = builder.setPositiveButton("확인", null)
            .setCancelable(false) // 팝업이 취소될 수 없도록 설정
            .create()

        // AlertDialog 표시
        alertDialog.show()

        // 메시지 텍스트의 글자 크기와 색상 변경
        val textView = alertDialog.findViewById<TextView>(android.R.id.message)
        textView?.apply {
            textSize = 20f // 글자 크기를 20sp로 설정
            setTextColor(Color.RED) // 글자 색상을 빨간색으로 설정
        }

        // '확인' 버튼의 텍스트 색상을 검정색으로 설정
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)

    }



    fun first_check(){

        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        val user_id = sharedPreference.getString("user_id", "test1").toString()
        val data = PM_check_user_exercise_first(user_id, "get")

        //통신 관련
        api.check_user_exercise_first(data).enqueue(object : retrofit2.Callback<PM_check_user_exercise_first_Result> {

            override fun onResponse(call: retrofit2.Call<PM_check_user_exercise_first_Result>, response: Response<PM_check_user_exercise_first_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 문장 실행
                if(!response.body().toString().isEmpty()){

                    val is_exercise_first = response.body()?.is_exercise_first

                    if (is_exercise_first == 1){
                        showPopup(this@Choice_ex_level)
                    }

                }
            }

            override fun onFailure(call: retrofit2.Call<PM_check_user_exercise_first_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })
    }
    //툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
