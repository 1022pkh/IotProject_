package com.kyounghyun.iotproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DB에 대한 함수가 정의된 곳
 *
 */
public class DbOpenHelper {

    private static final String DATABASE_NAME = "modulelist.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private ArrayList<ItemData> itemDatas = null;
    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);

        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ DataBases.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public DbOpenHelper(Context context){
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }
    /**
     * DB에 데이터 추가
    */
    public void DbInsert(ItemData itemData ){

        mDB = mDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();


        if(itemData.identName == null || itemData.identName.equals(""))
            values.put("ident_name","UnKnown");
        else
            values.put("ident_name",itemData.identName);



        values.put("ident_num",itemData.identNum);
        values.put("target_check",0);

        mDB.insert("moduleinfo",null,values);

    }

    /**
     * DB항목 업그레이드 - 수정할 때 사용
     */
    public void DbUpdate(ItemData itemData ){

        ContentValues values = new ContentValues();
//        values.put("ident_name",itemData.identName);
//        values.put("ident_num",itemData.identNum);
        values.put("target_check",itemData.targetCheck);


        mDB.update("moduleinfo", values, "ident_num=?", new String[]{itemData.identNum});

    }

    /**
     * 항목 삭제하는 함수
     * @param id
     */
    public void DbDelete(String id) {
        mDB.delete("moduleinfo", "_id=?", new String[]{id});
    }


    /**
     * modulelist테이블에 저장되어있는 값들을 반환하는 함수 - 리스트뷰 뿌릴 때 호출
     * @return
     */
    public ArrayList<ItemData> DbMainSelect(){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo" , null);

        itemDatas = new ArrayList<ItemData>();
//

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            int targetCheck = c.getInt(c.getColumnIndex("target_check"));

            ItemData listViewItem = new ItemData(_id,identName,identNum,targetCheck);

            itemDatas.add(listViewItem);

        }


        return itemDatas;
    }

   public ItemData DbFind(String address){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo where ident_num = '"+ address+"'" , null);

        ItemData item = null;

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            int targetCheck = c.getInt(c.getColumnIndex("target_check"));

            item = new ItemData(_id,identName,identNum,targetCheck);

        }


        return item;
    }


    public ItemData DbTargetFind(String address){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo where target_check = 1 AND ident_num = '"+ address+"'" , null);

        ItemData item = null;

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            int targetCheck = c.getInt(c.getColumnIndex("target_check"));

            item = new ItemData(_id,identName,identNum,targetCheck);

        }


        return item;
    }

    public ArrayList<ItemData> DbTarget(){
        SQLiteDatabase getDb;
        getDb = mDBHelper.getReadableDatabase();
        Cursor c = getDb.rawQuery( "select * from moduleinfo where target_check = 1" , null);

        itemDatas = new ArrayList<ItemData>();

        while(c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            String identName = c.getString(c.getColumnIndex("ident_name"));
            String identNum = c.getString(c.getColumnIndex("ident_num"));
            int targetCheck = c.getInt(c.getColumnIndex("target_check"));

            ItemData listViewItem = new ItemData(_id,identName,identNum,targetCheck);

            itemDatas.add(listViewItem);

        }


        return itemDatas;
    }

    public void close(){
        mDB.close();
    }

}
