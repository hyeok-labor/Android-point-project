package com.hyeok.point1;

/*
 마지막작업 1002
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context cthis;

    private DataBaseHandler objectDatabaseHandler;
    private MapActivity objectMapActivity;
    public List<String> objectList;

    public static String NameOfStore;

    Intent recognizerIntent;
    SpeechRecognizer mRecognizer;
    TextView txtVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cthis = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main.mxl 뷰 실행
        System.out.println("MainActivity 입니다!");

        // 생성된 데이터베이스 가져옴
        try{
            objectDatabaseHandler = new DataBaseHandler(this);
            System.out.println("데이터베이스 가져오기 성공!");
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }




        Button Button_Edit = (Button) findViewById(R.id.Button_Edit);
        Button_Edit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(intent);
            }
        });

        Button Button_Location = (Button) findViewById(R.id.Button_Location);
        Button_Location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);
            }
        });

        /* 텍스트 자동완성 기능 */
        // 리스트 생성
        objectList = objectDatabaseHandler.getNameArray();

        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        // AutoCompleteTextView 에 Adapter 연결
        autoCompleteTextView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, objectList));

        Button Button_Search = (Button) findViewById(R.id.Button_Search);
        Button_Search.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,EditActivity.class);
                NameOfStore = ((TextView)autoCompleteTextView).getText().toString();
                i.putExtra("key",NameOfStore);
                startActivity(i);
            }
        });

        /* 음성 인식 기능 */
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "eu-US");   // recognition Language

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(cthis);
        mRecognizer.setRecognitionListener(listener);

        txtVoice = (TextView) findViewById(R.id.txtVoice);

        Button Button_Voice=(Button)findViewById(R.id.Button_Voice);
        Button_Voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cthis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(recognizerIntent);
                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });


    }   // end onCreate()

    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle params) {
            txtVoice.setText("찾는 멤버십/포인트를 말하세요.");
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            String key = "";
            // 음성인식 결과값 String 저장
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult = results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            // 결과값
            key = rs[0].toUpperCase();
            System.out.println(""+key);   // 대문자로 바꾼 후 출력

            NameOfStore = objectMapActivity.searchText(key);    // 규격에 맞춘 String return *searchText() 함수 참고
            // Intent를 통해 EditActivity로 전환
            Intent i = new Intent(MainActivity.this,EditActivity.class);
            i.putExtra("key",NameOfStore);
            startActivity(i);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };


}
