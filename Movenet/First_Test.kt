package com.khci.bnm.Movenet

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.khci.bnm.R

class First_Test : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_test)

        /*val sharedPreferences_user = getSharedPreferences("user_register", Context.MODE_PRIVATE)
        val editor = sharedPreferences_user?.edit()

        //운동 기록 1로 설정
        editor?.putInt("movenet_first", 1)
        editor?.apply() // 데이터 저장*/
    }
}