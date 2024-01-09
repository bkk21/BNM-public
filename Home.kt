package com.khci.bnm

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.khci.bnm.Chat.ChoiceTalk
import com.khci.bnm.Movenet.Choice_ex_level
import com.khci.bnm.Movenet.First_Test
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PMinfo
import com.khci.bnm.Retrofit2.user_info_Result
import com.khci.bnm.Simple_profile.simpleprofilepage
import com.khci.bnm.etc.Setting_user
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home : AppCompatActivity() {

    val api = APIS.create()
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //메인 기능 버튼
        var exercise = findViewById<LinearLayout>(R.id.exercise_layout)
        var talk = findViewById<LinearLayout>(R.id.talk_layout)
        var simpleprofile = findViewById<LinearLayout>(R.id.simpleprofile_layout)
        var user_setting = findViewById<LinearLayout>(R.id.user_setting)

        user_setting.setOnClickListener {

            val logout_go = Intent(this, Setting_user::class.java)
            finish()
            startActivity(logout_go)
        }

        //운동 하기로 이동
        exercise.setOnClickListener {

            val sharedPreferences_user = getSharedPreferences("user_register", Context.MODE_PRIVATE)

            val movenet_first = sharedPreferences_user.getInt("movenet_first", 0)

            val exercise_go = Intent(this, Choice_ex_level::class.java)
            startActivity(exercise_go)

        }

        //대화 보기로 이동
        talk.setOnClickListener {
            val talk_go = Intent(this, ChoiceTalk::class.java)
            startActivity(talk_go)
            finish()
        }

        //간이신분증 으로 이동
        simpleprofile.setOnClickListener {
            val simpleprofile_go = Intent(this, simpleprofilepage::class.java)
            startActivity(simpleprofile_go)
            finish()
        }

    }
}
