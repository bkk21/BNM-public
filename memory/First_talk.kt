package com.khci.bnm.memory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.khci.bnm.Chat.talkpage
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.R
class First_talk : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_talk)

        val api = APIS.create();

        val joinmain = findViewById<TextView>(R.id.joinmain)
        val joinnext = findViewById<android.widget.Button>(R.id.joinnext)
        val joinbefore = findViewById<android.widget.Button>(R.id.joinbefore)

        // 초기 프래그먼트 설정
        replaceFragment(hometown())

        joinnext.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)

            val input = findViewById<EditText>(R.id.inputtext)


            when (currentFragment) {
                is hometown -> replaceFragment(favorite_food())
                is favorite_food -> replaceFragment(favorite_music())
                is favorite_music -> replaceFragment(favorite_season())
                is favorite_season -> replaceFragment(pet())
                is pet -> replaceFragment(past_job())
                is past_job -> replaceFragment(details())
                is details -> replaceFragment(plus_finish())
                is plus_finish -> {
                    val intent = Intent(this, talkpage::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // '이전' 버튼 클릭 이벤트
        joinbefore.setOnClickListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment)
            when (currentFragment) {
                is hometown -> {
                    val intent = Intent(this, talkpage::class.java)
                    startActivity(intent)
                    finish()
                }
                is favorite_food -> replaceFragment(hometown())
                is favorite_music -> replaceFragment(favorite_food())
                is favorite_season -> replaceFragment(favorite_music())
                is pet -> replaceFragment(favorite_season())
                is past_job -> replaceFragment(pet())
                is details -> replaceFragment(past_job())
                is plus_finish -> {
                    val intent = Intent(this, talkpage::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }


    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment, fragment)
        transaction.commit()
    }
}