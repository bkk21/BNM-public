package com.khci.bnm.Simple_profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Home
import com.khci.bnm.Retrofit2.PMinfo
import com.khci.bnm.Retrofit2.user_info_Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.khci.bnm.R

class simpleprofilepage : AppCompatActivity() {

    val api = APIS.create();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simpleprofilpage)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "간이 신분증"

        get_info()

        val go_home = findViewById<android.widget.Button>(R.id.home_go)

        go_home.setOnClickListener {
            val joinIntent = Intent(this, Home::class.java)
            startActivity(joinIntent)
        }
    }


    fun get_info(){
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        val user_id = sharedPreference.getString("user_id", "데이터 존재 x").toString()

        //각 정보
        val name = findViewById<TextView>(R.id.name)
        val birthday = findViewById<TextView>(R.id.birthday)
        val gender = findViewById<TextView>(R.id.gender)
        val address = findViewById<TextView>(R.id.address)
        val blood = findViewById<TextView>(R.id.blood)
        val nok_name = findViewById<TextView>(R.id.nok_name)
        val nok_phone = findViewById<TextView>(R.id.nok_phone)

        //json 형태 만들기
        val data = PMinfo(user_id)

        //서버로 전송
        api.get_user_info(data).enqueue(object : Callback<user_info_Result> {

            override fun onResponse(call: Call<user_info_Result>, response: Response<user_info_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 맨 처음 문장 실행
                if(!response.body().toString().isEmpty()){

                    val info_result = response.body()?.result.toString()


                    //맞으면 화면 전환 "로그인 되었습니다."
                    if (info_result == "success"){

                        val info_name = response.body()?.name.toString()
                        val info_birthday = response.body()?.birthday.toString()
                        var info_gender = response.body()?.gender.toString()
                        val info_address = response.body()?.address.toString()
                        val info_blood = response.body()?.blood_type.toString()
                        val info_nok_name = response.body()?.main_nok_name.toString()
                        val info_nok_phone = response.body()?.main_nok_tell.toString()

                        //성별 정리
                        if (info_gender == "F")
                            info_gender = "여성"
                        else
                            info_gender = "남성"

                        name.setText(info_name)
                        birthday.setText(info_birthday)
                        gender.setText(info_gender)
                        address.setText(info_address)
                        blood.setText(info_blood)
                        nok_name.setText(info_nok_name)
                        nok_phone.setText(info_nok_phone)

                    }

                    else{
                        Toast.makeText(this@simpleprofilepage, "실패하였습니다", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<user_info_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        }) //여기까지가 통신 한 묶음
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