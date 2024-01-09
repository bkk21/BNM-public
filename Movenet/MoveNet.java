package com.khci.bnm.Movenet;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import java.util.Locale;
import android.view.OrientationEventListener;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.TextView;
import android.widget.VideoView;

import com.khci.bnm.Home;
import com.khci.bnm.R;

public class MoveNet extends AppCompatActivity implements SurfaceHolder.Callback, TextToSpeech.OnInitListener  {

    public static final int REQUEST_CAMERA = 100;

    private NcnnBodypose ncnnbodypose = new NcnnBodypose();
    private int facing = 0;

    private Spinner spinnerModel;
    private Spinner spinnerCPUGPU;
    private int current_model = 0;
    private int current_cpugpu = 0;
    private int num = 0;
    private int tts = 0;
    private int posestep = 0;
    private int motion_step = 1;
    private int before_motion_step = 1;


    private SurfaceView cameraView;
    private ImageButton ex_home;
    private ImageView ex_mic;

    private TextView ex_SOS;

    private TextView ex_text;
    private ImageView ex;
    private int level = 1;
    private RelativeLayout RL;

    private OrientationEventListener orientationEventListener;
    private TextToSpeech textToSpeech;
    private boolean isTTSInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_net);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences sharedPreference = getSharedPreferences("ex_level", MODE_PRIVATE);
        level = sharedPreference.getInt("ch_level", 1);

        // TextToSpeech 초기화
        textToSpeech = new TextToSpeech(this, this);

        // TTS 초기화 완료 리스너
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // TTS 말하기 시작 시 처리 (여기서 필요한 작업 추가)
            }

            @Override
            public void onDone(String utteranceId) {
                // TTS 말하기 완료 시 처리 (여기서 필요한 작업 추가)
            }

            @Override
            public void onError(String utteranceId) {
                // TTS 오류 발생 시 처리 (여기서 필요한 작업 추가)
            }
        });

        cameraView = (SurfaceView) findViewById(R.id.cameraview);
        ex_home = (ImageButton) findViewById(R.id.ex_home);
        ex_SOS = (TextView) findViewById(R.id.ex_SOS);
        ex = (ImageView) findViewById(R.id.ex);

        if(level == 1)
            ex.setImageResource(R.drawable.lv1_up_1_1);
        else if(level == 2)
            ex.setImageResource(R.drawable.lv2_up_1_1);
        else if(level == 3)
            ex.setImageResource(R.drawable.lv3_up_1_1);

        //테스트 텍스트 나중에 지울 것 위에 상단 버튼음
        ex_text = (TextView) findViewById(R.id.ex_text);

        Handler handler = new Handler();

        //예시 영상 배경 띄우기 함수
        //ex_back();

        //예시 영상 띄우기 함수
        ex_video(level, 1);


        ncnnbodypose.pose(level);
        int num = ncnnbodypose.posenum();
        int tts = ncnnbodypose.tts();

        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                rotateExImage(orientation);
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }


        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        MyBackgroundTask backgroundTask = new MyBackgroundTask();
        backgroundTask.start(); // 백그라운드 작업 시작

        /*Button buttonSwitchCamera = (Button) findViewById(R.id.ex_text);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                int new_facing = 1 - facing;

                ncnnbodypose.closeCamera();

                ncnnbodypose.openCamera(new_facing);

                facing = new_facing;
            }
        });*/

        ex_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MoveNet.this, Home.class);

                startActivity(intent);

            }
        });


        ex_SOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:119"));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });



        reload();

    }

    private void rotateExImage(int orientation) {
        int degrees = 0;

        // 디바이스 방향에 따라 회전 각도를 결정합니다.
        if (orientation >= 315 || orientation < 45) {
            degrees = 0; // 디바이스가 세로 모드 (기본 모드)
        } else if (orientation >= 45 && orientation < 135) {
            degrees = 270; // 디바이스가 가로 모드 (오른쪽으로 회전)
        } else if (orientation >= 135 && orientation < 225) {
            degrees = 180; // 디바이스가 세로 모드 (뒤집힌 모드)
        } else if (orientation >= 225 && orientation < 315) {
            degrees = 90; // 디바이스가 가로 모드 (왼쪽으로 회전)
        }

        // 이미지 회전
        ex.setRotation(degrees);
    }

    public class MyBackgroundTask {
        private Handler handler;
        private boolean isRunning;

        public MyBackgroundTask() {
            handler = new Handler(Looper.getMainLooper());
        }

        public void start() {
            isRunning = true;
            // 백그라운드 스레드 시작
            Thread backgroundThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // 무한 루프 시작
                    while (isRunning) {
                        // 백그라운드 스레드에서 핸들러로 ui 수정
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                num = ncnnbodypose.posenum();
                                tts = ncnnbodypose.tts();

                                //현재 동작의 단계 상태
                                posestep = ncnnbodypose.posestep();

                                //현재 동작 상태(상체1, 상체2, 하체1 같은)
                                motion_step = ncnnbodypose.motion_step();

                                if(before_motion_step != motion_step)
                                {
                                    num = 0;
                                    tts_go("화면에 나오는 영상을 보고 따라해보세요!", "hello");
                                    //예시 영상 배경 띄우기 함수
                                    //ex_back();

                                    //예시 영상 띄우기 함수
                                    ex_video(level, motion_step);

                                }
                                before_motion_step = motion_step;


                                if(level == 1)
                                {
                                    //레벨 1 상체 1
                                    if(motion_step == 1)
                                    {
                                        //cpp_tts(tts);
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv1_up_1_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv1_up_1_1);

                                        ex_text.setText(num + " / 10");
                                    }

                                    //레벨 1 상체 2
                                    if(motion_step == 2)
                                    {
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv1_up_2_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv1_up_2_1);

                                        ex_text.setText(num + " / 10");

                                    }

                                    //레벨 1 하체 1
                                    if(motion_step == 3)
                                    {
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv1_down_1_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv1_down_1_1);

                                        ex_text.setText(num + " / 15");

                                    }
                                }
                                if(level == 2)
                                {

                                    //레벨 2 상체 1
                                    if(motion_step == 1)
                                    {
                                        //cpp_tts(tts);
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv2_up_1_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv2_up_1_1);

                                        ex_text.setText(num + " / 10");
                                    }

                                    //레벨 2 상체 2
                                    if(motion_step == 2)
                                    {
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv2_up_2_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv2_up_2_1);

                                        ex_text.setText(num + " / 10");

                                    }

                                    //레벨 2 하체 1
                                    if(motion_step == 3)
                                    {
                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv2_down_1_2);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv2_down_1_3);
                                        else if(posestep == 3)
                                            ex.setImageResource(R.drawable.lv2_down_1_1);

                                        ex_text.setText(num + " / 20");
                                    }
                                }

                                if(level == 3)
                                {
                                    //레벨 3 운동 1 상체 1
                                    if(motion_step == 1)
                                    {
                                        //cpp_tts(tts);

                                        //사진 변경 나중에 2, 4 바꾸기
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv3_up_1_1);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv3_up_1_4);
                                        else if(posestep == 3)
                                            ex.setImageResource(R.drawable.lv3_up_1_3);
                                        else if(posestep == 4)
                                            ex.setImageResource(R.drawable.lv3_up_1_2);

                                        ex_text.setText(num + " / 10");
                                    }

                                    //레벨 3 운동 2 상체 2
                                    if(motion_step == 2)
                                    {
                                        //cpp_tts(tts);

                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv3_up_2_1);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv3_up_2_2);

                                        ex_text.setText(num + " / 10");
                                    }

                                    //레벨 1 운동 3 하체 1
                                    if(motion_step == 3)
                                    {
                                        //cpp_tts(tts);

                                        //사진 변경
                                        if(posestep == 1)
                                            ex.setImageResource(R.drawable.lv3_down_1_1);
                                        else if(posestep == 2)
                                            ex.setImageResource(R.drawable.lv3_down_1_2);
                                        else if(posestep == 3)
                                            ex.setImageResource(R.drawable.lv3_down_1_3);
                                        else if(posestep == 4)
                                            ex.setImageResource(R.drawable.lv3_down_1_4);


                                        ex_text.setText(num + " / 10");
                                    }
                                }


                            }
                        });

                        try {
                            // 일시 정지
                            Thread.sleep(1000); // 1초
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // 백그라운드 스레드 시작
            backgroundThread.start();
        }

        public void stop() {
            isRunning = false;
        }
    }

    private void ex_back() {

        // RelativeLayout 가져오기
        RL = (RelativeLayout) findViewById(R.id.ex_RL);

        // 동적으로 TextView 생성
        TextView textView = new TextView(this);

        // TextView에 글자 비우기
        textView.setText("");

        // TextView 배경색 설정 (흰색)
        textView.setBackgroundColor(Color.WHITE);

        // 이미지 뷰 설정
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // 너비 (원하는 크기로 변경)
                RelativeLayout.LayoutParams.MATCH_PARENT  // 높이 (원하는 크기로 변경)
        );

        // 설정 적용
        textView.setLayoutParams(layoutParams);

        // TextView 레이아웃에 추가
        RL.addView(textView);

        // 10초 후에 TextView 삭제 코드
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RL.removeView(textView);
            }
        }, 20000); // 12초 후 삭제
    }


    private void ex_video(int levelnum, int step) {
        // RelativeLayout 가져오기
        RL = (RelativeLayout) findViewById(R.id.ex_RL);

        // 동적으로 TextView 생성
        TextView textView = new TextView(this);

        // TextView에 글자 비우기
        textView.setText("");

        // TextView 배경색 설정 (흰색)
        textView.setBackgroundColor(Color.WHITE);

        // 이미지 뷰 설정
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, // 너비 (원하는 크기로 변경)
                RelativeLayout.LayoutParams.MATCH_PARENT  // 높이 (원하는 크기로 변경)
        );

        // 설정 적용
        textView.setLayoutParams(layoutParams);

        // TextView 레이아웃에 추가
        RL.addView(textView);

        // 동적으로 VideoView 생성
        VideoView videoView = new VideoView(this);

        // 비디오 경로 설정
        if (levelnum == 1) {
            if(step == 1 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv1_up_1));
            else if(step == 2 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv1_up_2));
            else if(step == 3 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv1_down_1));
        }
        else if (levelnum == 2) {
            if(step == 1 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv2_up_1));
            else if(step == 2 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv2_up_2));
            else if(step == 3 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv2_down_1));
        }
        else if (levelnum == 3) {
            if(step == 1 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv3_up_1));
            else if(step == 2 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv3_up_2));
            else if(step == 3 )
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.lv3_down_1));
        }

        videoView.setLayoutParams(layoutParams);

        // VideoView 레이아웃에 추가
        RL.addView(videoView);

        // Handler를 사용하여 비디오 재생을 5초 후에 시작
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 5초 후에 비디오 재생 시작
                videoView.start();
            }
        }, 5000); // 5000 밀리초 (5초) 지연


        // 비디오 재생이 끝났을 때 이벤트 처리
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 비디오 재생 완료 후 처리
                RL.removeView(videoView);
                RL.removeView(textView);
                ncnnbodypose.setIsStart(true);
                // 여기에 TTS 메시지 재생 또는 기타 처리
            }
        });
    }


    private void reload()
    {
        Log.e("MoveNet", "save");

        boolean ret_init = ncnnbodypose.loadModel(getAssets(), current_model, current_cpugpu);

        if (!ret_init)
        {
            Log.e("MoveNet", "ncnnbodypose loadModel failed");
        }
        else{
            Log.e("MoveNet", "ncnnbodypose loadModel save");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        ncnnbodypose.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        ncnnbodypose.openCamera(facing);
    }

    @Override
    public void onPause() {
        super.onPause();

        ncnnbodypose.closeCamera();

        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.KOREAN); // TTS 언어 설정 (한국어)

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 언어 데이터가 없거나 지원하지 않는 경우 처리
            } else {
                isTTSInitialized = true;
                // TTS 초기화가 완료되면 여기서 "안녕하세요" TTS를 실행
                tts_go("화면에 나오는 영상을 보고 따라해보세요!", "hello");
            }
        } else {
            // TTS 초기화 실패 처리
        }
    }


    private void tts_go(String textToSpeak, String utteranceId) {
        if (isTTSInitialized) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    private void cpp_tts(int tts) {
        if(tts == 1)
        {
            tts_go("팔을 모으세요", "cpp_tts_1");
        }
        if(tts == 2)
        {
            tts_go("팔을 올리세요", "cpp_tts_2");
        }
        if(tts == 3)
        {
            tts_go("팔꿈치를 어깨까지 올리세요", "cpp_tts_3");
        }
        if(tts == 4)
        {
            tts_go("오른 팔을 조금 더 올리세요", "cpp_tts_4");
        }
        if(tts == 5)
        {
            tts_go("왼 팔을 조금 더 올리세요", "cpp_tts_5");
        }
        if(tts == 6)
        {
            tts_go("잘 하고 있어요! 팔을 유지하세요", "cpp_tts_6");
        }
        if(tts == 7)
        {
            tts_go("팔을 뻗어주세요", "cpp_tts_7");
        }
        if(tts == 8)
        {
            tts_go("팔꿈치를 어깨까지 올리세요", "cpp_tts_8");
        }
        if(tts == 9)
        {
            tts_go("팔을 수직으로 유지해주세요", "cpp_tts_9");
        }
        if(tts == 10)
        {
            tts_go("팔을 뻗어주세요", "cpp_tts_10");
        }
        if(tts == 11)
        {
            tts_go("팔을 내리고 잠시 휴식해주세요! 불편감이 있다면 억지로 하지 마세요. 힘들면 멈춰도 됩니다.", "cpp_tts_11");
        }
        if(tts == 12)
        {
            tts_go("운동이 끝났습니다. 고생하셨어요!", "cpp_tts_12");
        }
    }

    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}