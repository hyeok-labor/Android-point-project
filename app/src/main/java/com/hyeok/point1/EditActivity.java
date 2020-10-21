package com.hyeok.point1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// 이 클래스는 사용자의 지갑에 있는 데이터를 보여주고, 새로운 데이터를 추가하는 기능 가진다.
public class EditActivity extends AppCompatActivity {

    private DataBaseHandler objectDatabaseHandler;
    private RecyclerView objectRecyclerView;
    private RVAdapter objectRvAdapter;

    public List<String> objectModelClassList;

    public static String NameOfStore;

    private static final String TAG = "EditActivity_example";

    private Boolean deleteMode = false;   // deleteMode 를 통해 삭제모드를 활성/비활성화 합니다.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        System.out.println("EditActivity 입니다!");

        // Button_Back 생성
        Button Button_Back = (Button) findViewById(R.id.Button_Back);
        Button_Back.setOnClickListener(new View.OnClickListener() {
            // onClick --> View 이동 ( MainActivity )
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        // Button_Add 생성
        Button Button_Add = (Button) findViewById(R.id.Button_Add);
        Button_Add.setOnClickListener(new View.OnClickListener() {
            // onClick --> View 이동 ( AllianceActivity )
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChooseActivity.class);
                startActivity(intent);
            }
        });
        // 생성된 데이터베이스 가져옴
        try{
            objectRecyclerView=findViewById(R.id.imagesRV);
            objectDatabaseHandler=new DataBaseHandler(this);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Intent를 통해 받은 데이터가 있는경우, getSelectedData 메서드 실행
        Bundle bundle = getIntent().getExtras();         // Data from MapActivity

        if(bundle != null) {
            NameOfStore = bundle.getString("key");
            getSelectedData(NameOfStore);
        }
    }
    private boolean walletMode = false;
    public void getData(View view){     // DB에 저장된 모든 데이타를 불러옴
        // deleteMode 활성화를 위한 버튼 생성
        Button Button_Delete = (Button) findViewById(R.id.Button_Delete);
        Button Button_showImagesBtn = (Button) findViewById(R.id.showImagesBtn);
        if( walletMode == false){
            try {
                objectRvAdapter = new RVAdapter(objectDatabaseHandler.getAllImagesData());
                objectRecyclerView.setHasFixedSize(true);

                objectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                objectRecyclerView.setAdapter(objectRvAdapter);

                // 클릭된 object 값 반환 ==> 삭제모드 활성화 시
                objectRvAdapter.setOnItemClickListener(new RVAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) { // getPosition() 을 통해 Data Index를 얻는다.
                        Toast.makeText(EditActivity.this, "data:"+objectRvAdapter.getPosition(), Toast.LENGTH_SHORT).show();

                        if(deleteMode==true){
                            objectModelClassList = objectDatabaseHandler.getNameArray();            // get list which stored only image names
                            deleteImage(objectModelClassList.get(objectRvAdapter.getPosition()));   // get imagename from position index then delete
                        }
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.getMessage());
            }

            // 지갑이 열리면 삭제버튼 Visible
            Button_Delete.setVisibility(View.VISIBLE);
            //Button_Delete.setVisibility(Button_Delete.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            // 지갑버튼 설정
            Button_showImagesBtn.setText("지갑닫기");
            walletMode = true;
        }
        else{
            Button_showImagesBtn.setText("지갑열기");
            objectRvAdapter = new RVAdapter(objectDatabaseHandler.getSelectdedImagesData(""));
            objectRecyclerView.setHasFixedSize(true);

            objectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            objectRecyclerView.setAdapter(objectRvAdapter);
            // 지갑이 열리면 삭제버튼 Invisible
            Button_Delete.setVisibility(View.GONE);
            //Button_Delete.setVisibility(Button_Delete.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
            walletMode = false;
        }

    }

    // MainActivity에서 검색 후 저장된 텍스트를 매개변수
    public void getSelectedData(String nameofstore){
        try {
            Toast.makeText(this, nameofstore, Toast.LENGTH_SHORT).show();
            objectRvAdapter = new RVAdapter(objectDatabaseHandler.getSelectdedImagesData(nameofstore));
            objectRecyclerView.setHasFixedSize(true);

            objectRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            objectRecyclerView.setAdapter(objectRvAdapter);

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, e.getMessage());
        }
    }

    public void deleteImage(String str){
        try{
            SQLiteDatabase objectSqLiteDatabase = objectDatabaseHandler.getReadableDatabase();
            Cursor objectCursor= objectSqLiteDatabase.rawQuery("select * from imageInfo",null); // sql문을 통해 저장된 데이타 가져옴
            if(objectCursor.getCount()!=0){
                objectDatabaseHandler.deleteImage(new ModelClass(str));
                Toast.makeText(this,str+" 이 삭제되었다.", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setDeleteMode(View view){   // 버튼을 누르면 deleteMode 활성/비활성화
        Button Button_Delete = (Button) findViewById(R.id.Button_Delete);
        if(deleteMode==false){
            Toast.makeText(EditActivity.this,"삭제할 데이터를 선택하세요.",Toast.LENGTH_SHORT).show();
            deleteMode = true;
            Button_Delete.setBackgroundColor(Color.RED);
            Button_Delete.setTextColor(Color.WHITE);
        }
        else {
            Toast.makeText(EditActivity.this, "삭제모드 종료", Toast.LENGTH_SHORT).show();
            deleteMode = false;
            Button_Delete.setBackgroundColor(Color.WHITE);
            Button_Delete.setTextColor(Color.BLACK);
        }
    }

}