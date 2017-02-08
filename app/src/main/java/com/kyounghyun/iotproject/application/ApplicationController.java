package com.kyounghyun.iotproject.application;

import android.app.Application;
import android.content.SharedPreferences;

import com.kyounghyun.iotproject.database.DbOpenHelper;

import java.sql.SQLException;

public class ApplicationController extends Application {

    private static ApplicationController instance;
    public DbOpenHelper mDbOpenHelper;



    public static SharedPreferences connectInfo;
    public static SharedPreferences.Editor editor;

    public static ApplicationController getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationController.instance = this;

        this.buildDB();

        //

        /**
         * SharedPreference 설정
         */
        connectInfo = getSharedPreferences("iot_info", 0);
        editor= connectInfo.edit();


    }

    public void buildDB() {
        // DB Create and Open
        mDbOpenHelper = new DbOpenHelper(this);
        try {
            mDbOpenHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
