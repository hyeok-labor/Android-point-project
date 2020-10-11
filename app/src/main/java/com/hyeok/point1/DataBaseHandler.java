package com.hyeok.point1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/***** 데이터베이스 생성 및 쿼리를 통해 데이터의 조작이 이루어 지는 클래스 ******/

public class DataBaseHandler extends SQLiteOpenHelper {

    Context context;

    private static final String TAG = "DatabaseHandler:" ;

    private static String DATABASE_NAME="mydb.db";
    private static int DATABASE_VERSION=1;

    private static String createTableQuery="create table imageInfo (imageName TEXT" +
            ",image BLOB)";

    private ByteArrayOutputStream objectArrayOutputStream;
    private byte[] imageInBytes;

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(createTableQuery);
            Toast.makeText(context, "Table created successfully inside our database", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void storeImage(ModelClass objectModelClass){
        try{
            SQLiteDatabase objectSqLiteDatabase=this.getWritableDatabase();
            Bitmap imageToStoreBitmap = objectModelClass.getImage();

            objectArrayOutputStream=new ByteArrayOutputStream();
            imageToStoreBitmap.compress(Bitmap.CompressFormat.JPEG,100,objectArrayOutputStream);

            imageInBytes= objectArrayOutputStream.toByteArray();
            ContentValues objectContentValues = new ContentValues();

            objectContentValues.put("imageName",objectModelClass.getImageName());
            objectContentValues.put("image",imageInBytes);

            long checkifQueryRuns = objectSqLiteDatabase.insert("imageInfo",null,objectContentValues);
            if(checkifQueryRuns!=-1){
                Toast.makeText(context, "데이터가 저장되었습니다", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "데이터 저장 실패!!", Toast.LENGTH_SHORT).show();
            }


        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //
    public void deleteImage(ModelClass objectModelClass){
        SQLiteDatabase objectSqLiteDatabase=this.getWritableDatabase();
        objectSqLiteDatabase.execSQL("DELETE FROM imageInfo WHERE imageName = '" + objectModelClass.getImageName() + "';");
    }

    public void chooseImage(ModelClass objectModelClass){

    }
    // 테이블에 있는 모든 데이터를 ArrayList 저장
    public ArrayList<ModelClass> getAllImagesData(){
        try{
            SQLiteDatabase objectSqLiteDatabase = this.getReadableDatabase();
            ArrayList<ModelClass> objectModelClassList = new ArrayList<>();

            Cursor objectCursor= objectSqLiteDatabase.rawQuery("SELECT * FROM imageInfo",null); // sql문을 통해 저장된 데이타 가져옴

            if(objectCursor.getCount()!=0){
                while(objectCursor.moveToNext()){
                    String nameOfImage=objectCursor.getString(0);
                    byte [] imageBytes=objectCursor.getBlob(1);

                    Bitmap objectBitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
                    objectModelClassList.add(new ModelClass(nameOfImage,objectBitmap));
                }
                return objectModelClassList;
            }
            else{
                Toast.makeText(context, "데이터가 존재하지 않습니다...", Toast.LENGTH_SHORT).show();
                return null;
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // string 을 매개변수로 쿼리 조작하는 메서드 - SELECT
    public ArrayList<ModelClass> getSelectdedImagesData(String string){
        try{
            SQLiteDatabase objectSqLiteDatabase = this.getReadableDatabase();
            ArrayList<ModelClass> objectModelClassList = new ArrayList<>();
            Cursor objectCursor = null;

            objectCursor=objectSqLiteDatabase.rawQuery("SELECT * FROM imageInfo WHERE imageName = '" + string + "'",null);

            if(objectCursor.getCount()!=0){             // rawquery에 해당되는 데이터를 컬럼인덱스 순서대로 출력
                while(objectCursor.moveToNext()){
                    String nameOfImage=objectCursor.getString(0);
                    byte [] imageBytes=objectCursor.getBlob(1);

                    Bitmap objectBitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
                    objectModelClassList.add(new ModelClass(nameOfImage,objectBitmap));
                }
                return objectModelClassList;
            }
            else{
                Toast.makeText(context, "찾는 데이터가 존재하지 않습니다...", Toast.LENGTH_SHORT).show();
                return null;
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // 데이터베이스에 존재하는 데이터를 생성된 순서대로 List 배열에 삽입하는 메서드
    public List<String> getNameArray(){
        try{
            SQLiteDatabase objectSqLiteDatabase = this.getReadableDatabase();
            List<String> objectModelClassList = new ArrayList<>();

            Cursor objectCursor= objectSqLiteDatabase.rawQuery("SELECT * FROM imageInfo",null); // sql문을 통해 저장된 데이타 가져옴

            if(objectCursor.getCount()!=0){
                while(objectCursor.moveToNext()){
                    String nameOfImage=objectCursor.getString(0);
                    objectModelClassList.add(nameOfImage);
                }
                return objectModelClassList;
            }
            else{
                Toast.makeText(context, "데이터가 존재하지 않습니다...", Toast.LENGTH_SHORT).show();
                return null;
            }
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
