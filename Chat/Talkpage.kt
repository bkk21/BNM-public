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
import com.khci.bnm.Retrofit2.ChatResult
import com.khci.bnm.Retrofit2.PMChat
import android.Manifest
import android.content.pm.ActivityInfo
import android.speech.tts.TextToSpeech
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.khci.bnm.R
import java.util.Locale

var chat_msg = ""
class talkpage : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var tts: TextToSpeech

    //api 생성
    val api = APIS.create()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talkpage)


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
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this@talkpage)
            speechRecognizer.setRecognitionListener(recognitionListener)    // 리스너 설정
            speechRecognizer.startListening(intent)                         // 듣기 시작
        }

        //sos 자동 연결 - 연결 안 됨 이슈...
        sos.setOnClickListener{
            var intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:119")
            if(intent.resolveActivity(packageManager) != null){
                startActivity(intent)
            }
        }

    }

    //대화하기 실행 시
    fun gochat(){

        val input = findViewById<EditText>(R.id.inputtext)
        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        //화면 전환 시 값 받아오기로 구현 해야 함
        val user_id = sharedPreference.getString("user_id", "test1").toString()
        val msg = "안녕" //처음 넘기는 화면

        val loading = findViewById<LottieAnimationView>(R.id.loding)


        //테스트용 // 안녕하세요 부분
        val text = findViewById<TextView>(R.id.printtext)

        val data = PMChat(user_id, msg)

        //로딩 시작
        runOnUiThread {
            loading.playAnimation()
        }

        input.isEnabled = false

        //통신 관련
        api.post_chat_one(data).enqueue(object : Callback<ChatResult> {

            override fun onResponse(call: Call<ChatResult>, response: Response<ChatResult>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 맨 처음 문장 실행
                if(!response.body().toString().isEmpty()){
                    //val text2 = response.body().toString()

                    //로딩 끝
                    runOnUiThread {
                        loading.pauseAnimation()
                        loading.visibility = View.GONE
                    }

                    chat_msg = response.body()?.msg.toString()
                    text.setText(chat_msg)
                    chat_msg_tts(chat_msg)
                    input.setText("")
                    input.isEnabled = true

                }
            }

            override fun onFailure(call: Call<ChatResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        }) //여기까지가 통신 한 묶음
    }

    //대화 읽고 보내기
    fun gomsg(){
        val input = findViewById<EditText>(R.id.inputtext)

        val sharedPreference = getSharedPreferences("login", MODE_PRIVATE)
        //화면 전환 시 값 받아오기로 구현 해야 함
        val user_id = sharedPreference.getString("user_id", "test1").toString()

        //사용자가 입력한 값 읽어오기
        val msg = findViewById<EditText>(R.id.inputtext).text.toString()

        // 안녕하세요 부분 - 내용이 출력될 부분
        val text = findViewById<TextView>(R.id.printtext)

        val loading = findViewById<LottieAnimationView>(R.id.loding)

        input.isEnabled = false
        //로딩 시작
        runOnUiThread {
            loading.visibility = View.VISIBLE
            loading.playAnimation()
        }

        text.setText("로딩 중..")

        //보내줄 data
        val data = PMChat(user_id, msg)
        api.post_chat_one(data).enqueue(object : Callback<ChatResult> {

            override fun onResponse(call: Call<ChatResult>, response: Response<ChatResult>) {
                //Log.d("log",response.toString())
                Log.d("log", response.body().toString())

                // 문장 실행
                if(!response.body().toString().isEmpty()){
                    //val text2 = response.body().toString()

                    chat_msg = response.body()?.msg.toString()

                    //로딩 끝
                    runOnUiThread {
                        loading.pauseAnimation()
                        loading.visibility = View.GONE
                    }

                    text.setText(chat_msg)
                    chat_msg_tts(chat_msg)
                    input.setText("")
                    input.isEnabled = true
                }
            }

            override fun onFailure(call: Call<ChatResult>, t: Throwable) {
                // 실패
                Log.d("log",t.message.toString())
                Log.d("log","fail")
            }
        })//통신 끝
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
            ContextCompat.checkSelfPermission(this@talkpage, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@talkpage,
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
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간 초과"
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

/*
fun progressON(){
        progressDialog = AppCompatDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setContentView(R.layout.progress_loading)
        progressDialog.show()
    }
    fun progressOFF(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss()
        }
    }
* */

