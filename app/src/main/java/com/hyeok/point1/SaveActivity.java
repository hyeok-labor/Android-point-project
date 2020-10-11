package com.hyeok.point1;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

/*** 데이터 저장 기능을 갖는 클래스 ***/
public class SaveActivity extends AppCompatActivity {
    private EditText imageDetailsET;
    private ImageView objectImageView;

    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri imageFilePath;

    private Bitmap imageToStore;
    DataBaseHandler objectDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        System.out.println("SaveActivity 입니다!");
        Toast.makeText(this, "앨범 버튼을 클릭해 사진 업로드", Toast.LENGTH_SHORT).show();


        try{
            imageDetailsET=findViewById(R.id.imageNameET);
            objectImageView=findViewById(R.id.image);

            //textView = (TextView) findViewById(R.id.ImgName);
            Bundle bundle = getIntent().getExtras();         // Alliance 에서 Intent
            String message = bundle.getString("key");   // String 을 message에 저장
            imageDetailsET.setText(message);                 // 현재 Activity textview에 setText한다.

            objectDatabaseHandler=new DataBaseHandler(this);
        }catch(Exception e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Button_Back 생성
        Button Button_BACK = (Button) findViewById(R.id.button_BACK);
        Button_BACK.setOnClickListener(new View.OnClickListener() {
            // onClick --> View 이동 ( MainActivity )
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChooseActivity.class);
                startActivity(intent);
            }
        });

    }

    //  이미지 뷰 클릭시 발생 이벤트
    public void chooseImage(View objectView){
        try{
            Intent objectIntent = new Intent();
            objectIntent.setType("image/*");

            objectIntent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(objectIntent,PICK_IMAGE_REQUEST);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST&& resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            imageFilePath=data.getData();
            try {
                imageToStore=MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            objectImageView.setImageBitmap(imageToStore);
        }
    }
    public void storeImage(View view){      // Button 클릭시 활성화 되는 함수.
        try {
            if(!imageDetailsET.getText().toString().isEmpty() && objectImageView.getDrawable()!=null&&imageToStore!=null){
                objectDatabaseHandler.storeImage(new ModelClass(imageDetailsET.getText().toString(),imageToStore));

                startActivity(new Intent(this,MainActivity.class));
            }
            else{
                Toast.makeText(this, "바코드를 등록해 주세요.", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
