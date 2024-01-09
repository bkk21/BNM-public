
/*class MemoryTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_test)

        val home = findViewById<ImageButton>(R.id.home)
        val text = findViewById<TextView>(R.id.printtext)
        val input = findViewById<EditText>(R.id.inputtext)
        nextmsg()

        //타자로 사용자 입력 받기
        input.setOnEditorActionListener{ textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                //여기서 사용자 값 읽어서 보내기
                nextmsg() //서버로 보내는 함수 실행
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(input.windowToken, 0)
                handled = true
            }
            handled
        } // 여기까지

        home.setOnClickListener{
            val homeIntent = Intent(this, ChoiceTalk::class.java)
            startActivity(homeIntent)
        }
    }

    fun nextmsg() {

        //사용자가 입력한 값 읽어오기
        val msg = findViewById<EditText>(R.id.inputtext).text.toString()

        // 안녕하세요 부분 - 내용이 출력될 부분
        val text = findViewById<TextView>(R.id.printtext)

        text.setText("나의 생일은 언제인가요?")


    }


}*/
package com.khci.bnm.Chat

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.khci.bnm.Retrofit2.APIS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.Manifest
import android.content.pm.ActivityInfo
import android.speech.tts.TextToSpeech
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.khci.bnm.R
import com.khci.bnm.Retrofit2.PM_chatbot_quiz
import com.khci.bnm.Retrofit2.PM_chatbot_quiz_Result
import com.khci.bnm.Retrofit2.PM_chatbot_quiz_first
import java.util.Locale

var chat_msg_Q = ""
class MemoryTest : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    //api 생성
    val api = APIS.create()

    var history_test = mutableListOf<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_test)
        //가로 화면 막기
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        gochat()

        //tts
        tts = TextToSpeech(this, this)

        val text = findViewById<TextView>(R.id.printtext)
        val input = findViewById<EditText>(R.id.inputtext)

        //타자로 사용자 입력 받아 서버로 보내기
        input.setOnEditorActionListener{ textView, action, event ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                //여기서 사용자 값 읽어서 보내기
                gomsg() //서버로 보내는 함수 실행
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(input.windowToken, 0)

                input.setCursorVisible(false)
                handled = true
            }
            handled
        } // 여기까지

        // 권한 설정
        requestPermission()

        // RecognizerIntent 생성
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)    // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")         // 언어 설정

        //메인 화면 돌아가기 버튼
        val home = findViewById<ImageButton>(R.id.home)

        //음성으로 말하기 버튼
        val mic = findViewById<ImageButton>(R.id.mic)

        //sos 긴급 신고 버튼
        val sos = findViewById<TextView>(R.id.SOS)

        //홈으로 이동
        home.setOnClickListener{
            val homeIntent = Intent(this, ChoiceTalk::class.java)
            startActivity(homeIntent)
            finish()
        }


        //음성으로 말하기
        mic.setOnClickListener{
            // 새 SpeechRecognizer 를 만드는 팩토리 메서드
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MemoryTest)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작
        }

        sos.setOnClickListener{
            var intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:119")
            if(intent.resolveActivity(packageManager) != null){
                startActivity(intent)
                finish()
            }
        }
    }

    //대화하기 실행 시
    fun gochat(){

        val input = findViewById<EditText>(R.id.inputtext)
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        //화면 전환 시 값 받아오기로 구현 해야 함
        val user_id = sharedPreference.getString("user_id", "test1").toString()
        //var user_id = intent.getStringExtra("user_id").toString()

        val loading = findViewById<LottieAnimationView>(R.id.loding)

        //테스트용 // 안녕하세요 부분
        val text = findViewById<TextView>(R.id.printtext)


        val data = PM_chatbot_quiz_first(user_id)

        //로딩 시작
        runOnUiThread {
            loading.playAnimation()
        }

        input.isEnabled = false

        //통신 관련
        api.chatbot_quiz_first(data).enqueue(object : Callback<PM_chatbot_quiz_Result> {

            override fun onResponse(call: Call<PM_chatbot_quiz_Result>, response: Response<PM_chatbot_quiz_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 맨 처음 문장 실행
                if(!response.body().toString().isEmpty()){

                    //로딩 끝
                    runOnUiThread {
                        loading.pauseAnimation()
                        loading.visibility = View.GONE
                    }
                    chat_msg_Q = response.body()?.msg.toString()
                    text.setText(chat_msg_Q)
                    chat_msg_tts(chat_msg_Q)
                    input.setText("")
                    input.isEnabled = true
                    history_test = response.body()?.history!!

                }
            }

            override fun onFailure(call: Call<PM_chatbot_quiz_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        }) //여기까지가 통신 한 묶음
    }

    //대화 읽고 보내기
    fun gomsg(){
        val msg = findViewById<EditText>(R.id.inputtext).text.toString()

        val input = findViewById<EditText>(R.id.inputtext)
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        //화면 전환 시 값 받아오기로 구현 해야 함
        val user_id = sharedPreference.getString("user_id", "test1").toString()
        //var user_id = intent.getStringExtra("user_id").toString()
        val loading = findViewById<LottieAnimationView>(R.id.loding)


        //테스트용 // 안녕하세요 부분
        val text = findViewById<TextView>(R.id.printtext)


        input.isEnabled = false
        //로딩 시작
        runOnUiThread {
            loading.visibility = View.VISIBLE
            loading.playAnimation()
        }

        text.setText("로딩 중..")
        val data = PM_chatbot_quiz(user_id, msg, history_test)



        //통신 관련
        api.chatbot_quiz(data).enqueue(object : Callback<PM_chatbot_quiz_Result> {

            override fun onResponse(call: Call<PM_chatbot_quiz_Result>, response: Response<PM_chatbot_quiz_Result>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 맨 처음 문장 실행
                if(!response.body().toString().isEmpty()){


                    chat_msg_Q = response.body()?.msg.toString()
                    //로딩 끝
                    runOnUiThread {
                        loading.pauseAnimation()
                        loading.visibility = View.GONE
                    }

                    text.setText(chat_msg_Q)
                    chat_msg_tts(chat_msg_Q)
                    input.setText("")
                    input.isEnabled = true
                    history_test = response.body()?.history!!

                    /*if(response.body()?.result.toString() == "end")
                    {
                        val homeIntent = Intent(this@MemoryTest, ChoiceTalk::class.java)
                        startActivity(homeIntent)
                        finish()
                    }*/

                }
            }

            override fun onFailure(call: Call<PM_chatbot_quiz_Result>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }


        }) //여기까지가 통신 한 묶음
    }

    private fun chat_msg_tts(chat_msg: String) {
        if (::tts.isInitialized) {
            tts.speak(chat_msg, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {

        }
    }


    // 권한 설정 메소드
    private fun requestPermission() {
        // 버전 체크, 권한 허용했는지 체크
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this@MemoryTest, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MemoryTest,
                arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }
    }

    // STT 설정
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        // 말하기 시작할 준비가되면 호출
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(applicationContext, "이제 말씀하세요!", Toast.LENGTH_SHORT).show()
        }
        // 말하기 시작했을 때 호출
        override fun onBeginningOfSpeech() {
            Toast.makeText(applicationContext, "잘 듣고 있어요.", Toast.LENGTH_SHORT).show()
        }
        // 입력받는 소리의 크기를 알려줌
        override fun onRmsChanged(rmsdB: Float) {}
        // 말을 시작하고 인식이 된 단어를 buffer에 담음
        override fun onBufferReceived(buffer: ByteArray) {}
        // 말하기를 중지하면 호출
        override fun onEndOfSpeech() {
            Toast.makeText(applicationContext, "말하기가 중단 되었어요.", Toast.LENGTH_SHORT).show()
        }
        // 오류 발생했을 때 호출
        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            Toast.makeText(applicationContext, "에러 발생: $message", Toast.LENGTH_SHORT).show()
        }
        // 인식 결과가 준비되면 호출
        override fun onResults(results: Bundle) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

            var result = findViewById<TextView>(R.id.inputtext)
            for (i in matches!!.indices) result.setText(matches[i])

            gomsg()
        }
        // 부분 인식 결과를 사용할 수 있을 때 호출
        override fun onPartialResults(partialResults: Bundle) {}
        // 향후 이벤트를 추가하기 위해 예약
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    override fun onInit(status: Int) {


        if (status == TextToSpeech.SUCCESS) {
            // TTS 초기화 성공
            val result = tts.setLanguage(Locale.US) // 언어 설정

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 언어 데이터가 없거나 지원되지 않는 경우
                // 오류 처리
            } else {
                // 텍스트를 음성으로 변환

            }
        } else {
            // TTS 초기화 실패
            // 오류 처리
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        // TTS 자원 해제
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}


