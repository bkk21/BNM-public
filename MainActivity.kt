package com.khci.bnm


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.APIS
import com.khci.bnm.Retrofit2.PMlogin
import com.khci.bnm.Retrofit2.loginResult
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//

class MainActivity : AppCompatActivity() {

    val api = APIS.create();

    //내부 db 사용 x
    //var DB:DBHelper?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auto_login()


        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)

        //DB 사용 x
        //DB = DBHelper(this)


        val loginbutton = findViewById<android.widget.Button>(R.id.login_go)
        val join = findViewById<TextView>(R.id.joingo)
        val err_text = findViewById<TextView>(R.id.err_text)

        val inputPw = findViewById<EditText>(R.id.inputpw)

        inputPw.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_view, 0)
        // 터치 이벤트 리스너 설정
        inputPw.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (inputPw.right - inputPw.compoundPaddingEnd)) {
                    // 비밀번호 보기/숨기기 토글
                    inputPw.transformationMethod = if (inputPw.transformationMethod is PasswordTransformationMethod) {
                        HideReturnsTransformationMethod.getInstance()
                    } else {
                        PasswordTransformationMethod.getInstance()
                    }
                    // 커서를 텍스트 끝으로 이동
                    inputPw.setSelection(inputPw.text.length)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }


        loginbutton.setOnClickListener{

            //입력한 값 받아오기
            val user_id = findViewById<EditText>(R.id.inputid).text.toString()
            val user_pw = findViewById<EditText>(R.id.inputpw).text.toString()

            if(user_id == "" && user_pw == "")
            {
                Log.d("error", "아이디 비밀번호 없음")
                err_text.setText("아이디와 비밀번호를 입력해주세요")
            }
            else if(user_id == "")
            {
                Log.d("error", "아이디 없음")
                err_text.setText("아이디를 입력해주세요")
            }
            else if(user_pw == "")
            {
                Log.d("error", "비밀번호 없음")
                err_text.setText("비밀번호를 입력해주세요")
            }

            else {
                //json 형태 만들기
                val data = PMlogin(user_id, user_pw)

                //서버로 전송
                api.post_chat_one(data).enqueue(object : Callback<loginResult> {

                    override fun onResponse(
                        call: Call<loginResult>,
                        response: Response<loginResult>
                    ) {
                        //Log.d("log",response.toString())

                        // 맨 처음 문장 실행
                        if (!response.body().toString().isEmpty()) {

                            val login_result = response.body()?.result.toString()
                            val msg = response.body()?.msg.toString()
                            val err_code = response.body()?.err_code.toString()
                            val user_type = response.body()?.user_type.toString()


                            //맞으면 화면 전환 "로그인 되었습니다."
                            if (login_result == "success") {
                                Toast.makeText(this@MainActivity, "로그인 되었습니다.", Toast.LENGTH_SHORT)
                                    .show()

                                val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
                                val editor: SharedPreferences.Editor = sharedPreference.edit()
                                editor.putString("user_id", user_id)
                                editor.putString("user_pw", user_pw)
                                editor.putString("user_type", user_type)
                                editor?.apply()


                                if (user_type == "main_nok") {
                                    val goIntent = Intent(this@MainActivity, Home_nok::class.java)
                                    intent.putExtra("user_id", user_id)
                                    startActivity(goIntent)
                                    finish()
                                } else {
                                    val goIntent = Intent(this@MainActivity, Home::class.java)
                                    intent.putExtra("user_id", user_id)
                                    startActivity(goIntent)
                                    finish()
                                }
                            }


                            //틀리면 오류 띄우기
                            val errorBody = response.errorBody()?.string()
                            if (errorBody != null) {
                                try {
                                    val jsonObject = JSONObject(errorBody)
                                    val errorCode = jsonObject.getString("err_code")
                                    Log.d("ErrorCode", errorCode)
                                    if (errorCode == "20") {
                                        err_text.setText("아이디가 존재하지 않습니다.")
                                    }
                                    if (errorCode == "22") {
                                        err_text.setText("비밀번호가 틀립니다. 확인해주세요.")
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }

                        }
                    }

                    override fun onFailure(call: Call<loginResult>, t: Throwable) {
                        // 실패
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                    }
                }) //여기까지가 통신 한 묶음
            }
        }

        //회원가입 누르면 화면 전환
        join.setOnClickListener {
            val joinIntent = Intent(this@MainActivity, Join_Choice::class.java)
            startActivity(joinIntent)
        }
    }

    /* 배경 슬라이딩 + input 화면 등장*/
    fun slideback(){
        //var back = findViewById<RelativeLayout>(R.id.mainback) // 상단 사진 부분
        var input = findViewById<LinearLayout>(R.id.input) //입력 부분

        //val slide: ValueAnimator = ObjectAnimator.ofFloat(back, "translationY", 700f)
        val appear: ValueAnimator = ObjectAnimator.ofFloat(input, View.ALPHA, 0f, 1.0f)

        //slide.duration = 1000
        appear.duration = 1800

        //slide.start()
        appear.start()

    }

    fun auto_login(){
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        val user_type = sharedPreference.getString("user_type", "데이터 존재 x").toString()
        val user_id = sharedPreference.getString("user_id", "데이터 존재 x").toString()
        val user_pw = sharedPreference.getString("user_pw", "데이터 존재 x").toString()

        // SharedPreferences 안에 값이 저장되어 있지 않을 때 -> Login
        if(user_id == "데이터 존재 x"
            || user_pw == "데이터 존재 x") {


        }
        else { // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동
            if(user_type == "user") {
                Toast.makeText(
                    this,
                    "${user_id}님 자동 로그인 되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, Home::class.java)
                startActivity(intent)
                finish()
            }

            else if(user_type == "main_nok") {
                Toast.makeText(
                    this,
                    "${user_id}님 자동 로그인 되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this, Home_nok::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}