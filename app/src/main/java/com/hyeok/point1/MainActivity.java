package com.hyeok.point1;

/*
 마지막작업 1002
 */

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DataBaseHandler objectDatabaseHandler;
    public List<String> objectList;

    public static String NameOfStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // activity_main.mxl 뷰 실행
        System.out.println("MainActivity 입니다!");

        // 생성된 데이터베이스 가져옴
        try{
            objectDatabaseHandler = new DataBaseHandler(this);
            System.out.println("데이터베이스 생성되었습니다.");
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
    }
}
