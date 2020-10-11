package com.hyeok.point1;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ChooseActivity extends AppCompatActivity {

    ListView listview ;
    ListViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        System.out.println("AllianceActivity 입니다!");

        Button Button_Back = (Button) findViewById(R.id.Button_Back);
        Button_Back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(intent);
            }
        });

        // Adapter 생성
        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        // 선택창에 나타낼 Item들을 추가합니다.
        ItemList(listview,adapter);

        // 리스트뷰 클릭 이벤트
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);

                String titleStr = item.getTitle();
                Drawable iconDrawable = item.getIcon();

                // TODO : 선택된 object가 가지는 text를 반환함
                Intent i = new Intent(ChooseActivity.this,SaveActivity.class);
                i.putExtra("key",titleStr);
                startActivity(i);
            }
        }) ;
    }

    public void ItemList(ListView listview, ListViewAdapter adapter){
        // Image의 크기는 100x100 pixel로 고정되어야함.
        // Todo : adapter.addItem() 메서드를 통해 클라이언트가 요구한 포인트를 업로드 하시오.
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_happypoint),
                "HAPPYPOINT") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_cjone),
                "CJONE") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_gspoint),
                "GSPOINT") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_cupoint),
                "CUPOINT") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_tmembership),
                "TMEMBERSHIP") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_twosomeplace),
                "TWOSOMEPLACE") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_starbucks),
                "STARBUCKS") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_okcashbag),
                "OKCASHBAG") ;
        adapter.addItem(ContextCompat.getDrawable(this, R.drawable.ic_lpoint),
                "LPOINT") ;
    }
}