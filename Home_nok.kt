package com.khci.bnm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import com.khci.bnm.Chat.Choice_user_log
import com.khci.bnm.memory.Choice_quiz_log_user
import com.khci.bnm.etc.Setting

class Home_nok : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_nok)

        val exercise_log = findViewById<LinearLayout>(R.id.exercise_log)

        exercise_log.setOnClickListener{
            /*val setting_go = Intent(this, Setting::class.java)
            startActivity(setting_go)*/
        }

        val talk_log = findViewById<LinearLayout>(R.id.talk_log)

        talk_log.setOnClickListener{
            val talk_log_go = Intent(this, Choice_user_log::class.java)
            startActivity(talk_log_go)
        }

        val quiz_layout = findViewById<LinearLayout>(R.id.quiz_layout)

        quiz_layout.setOnClickListener{
            val quiz_log_go = Intent(this, Choice_quiz_log_user::class.java)
            startActivity(quiz_log_go)
        }

        val user_setting = findViewById<LinearLayout>(R.id.user_setting)
        user_setting.setOnClickListener {
            val setting_go = Intent(this, Setting::class.java)
            startActivity(setting_go)
        }


    }
}