package com.kyounghyun.iotproject.lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.database.DbOpenHelper;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.dialog.DialogConparePinNum;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockActivity extends AppCompatActivity {


    @BindView(R.id.countupText)
    TextView countupText;
    @BindView(R.id.stateImg)
    ImageView stateImg;
//    @BindView(R.id.openOrder)
//    TextView openOrder;
//    @BindView(R.id.closeOrder)
//    TextView closeOrder;

    int countUp = 0;

    int connectDeviceMax = 0;
    int connectDeviceCount = 0;

    int targetDeviceMax = 0;
    int targetDeviceCount = 0;

    Boolean isRunning = true;

    Boolean isPinCheck = false;
    Boolean countCheck = false;
    Boolean targetCheck = false;

    DialogConparePinNum dialogConparePinNum;
    DbOpenHelper mDbOpenHelper;

    ArrayList<ItemData> moduleList;
    private BluetoothAdapter mBluetoothAdapter;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                Log.i("myTag", "Connected");

                connectDeviceCount++;

                if(targetDeviceCount >= connectDeviceMax)
                    countCheck = true;


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i("myTag", "Disconnected");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        ButterKnife.bind(this);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;
        moduleList = mDbOpenHelper.DbMainSelect();


        final TimerHandler timer = new TimerHandler();

        Thread remianThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(isRunning){
                    Message msg = timer.obtainMessage();
                    timer.sendMessage(msg);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        });

        remianThread.start();

        connectDeviceMax = mDbOpenHelper.DbMainSelect().size();
        targetDeviceMax = mDbOpenHelper.DbTarget().size();


        /**
         * 연결된 기기 갯수 파악
         */




        new Thread(new Runnable() {
            @Override
            public void run() {

                for(int i = 0 ; i< moduleList.size(); i++){
                    connectBle(moduleList.get(i).identNum);
                }

                if(targetDeviceMax ==  targetDeviceCount)
                    targetCheck = true;



                Log.i("myTag","count_check : " + countCheck);
            }
        }).start();



        /**
         * 타겟 기기 유무 조사
         */
        if(ApplicationController.connectInfo.getBoolean("target_check", false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    for(int i = 0 ; i< moduleList.size(); i++){
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(moduleList.get(i).identNum);

                        if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                            targetDeviceCount++;
                        }

                    }

                    if(targetDeviceMax ==  targetDeviceCount)
                        targetCheck = true;



                    Log.i("myTag","target_check : " + targetCheck);
                }
            }).start();
        }
        else
            targetCheck = true;


        /**
         * pin 유무 체크
         */
        if(ApplicationController.connectInfo.getBoolean("pin_check", false))
        {
            showInputPinDialog();

        }
        else{
            isPinCheck = true;
            Log.i("myTag","pin_check : " + isPinCheck);

        }



    }


    public Boolean connectBle(String mDeviceAddress){
        Log.i("myTag", "Connect request address=" + mDeviceAddress);

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

        if (device == null) {
            Log.w("myTag", "Device not found.  Unable to connect.");
            return false;
        }

        device.connectGatt(this, false, mGattCallback);


        return false;

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    @OnClick(R.id.openOrder)
//    public void requestOpenOrder(){
//
//        stateImg.setImageResource(R.drawable.unlock);
//        openOrder.setBackgroundResource(R.drawable.border_circle_background);
//        openOrder.setTextColor(Color.parseColor("#ffffff"));
//        closeOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
//        closeOrder.setTextColor(Color.parseColor("#000000"));
//    }
//
//    @OnClick(R.id.closeOrder)
//    public void requestCloseOrder(){
//
//        stateImg.setImageResource(R.drawable.lock);
//        closeOrder.setBackgroundResource(R.drawable.border_circle_background);
//        closeOrder.setTextColor(Color.parseColor("#ffffff"));
//        openOrder.setBackgroundResource(R.drawable.border_circle_background_empty);
//        openOrder.setTextColor(Color.parseColor("#000000"));
//    }


    public void showInputPinDialog(){
        WindowManager.LayoutParams loginParams;
        dialogConparePinNum = new DialogConparePinNum(this, R.style.DialogTheme, completeEvent, cancelEvent);

        dialogConparePinNum.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogConparePinNum.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogConparePinNum.setCanceledOnTouchOutside(false);


        loginParams = dialogConparePinNum.getWindow().getAttributes();

        // Dialog 사이즈 조절 하기
        loginParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        loginParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogConparePinNum.getWindow().setAttributes(loginParams);
        dialogConparePinNum.show();
    }


    private View.OnClickListener completeEvent = new View.OnClickListener() {
        public void onClick(View v) {

            String num = dialogConparePinNum.getPinNum();

            if(num.length() < 6)
            {
                Toast.makeText(getApplicationContext(),"6글자로 구성해주세요.",Toast.LENGTH_SHORT).show();
            }
            else{

                String temp = ApplicationController.connectInfo.getString("pinNum", "");

                if(temp.equals(num)){
                    isPinCheck = true;
                    Toast.makeText(getApplicationContext(),"인증 성공!",Toast.LENGTH_SHORT).show();



                    Log.i("myTag","pin_check : " + isPinCheck);

                    dialogConparePinNum.dismiss();
                }
                else{
                    isPinCheck = false;
                    Toast.makeText(getApplicationContext(),"인증 실패!",Toast.LENGTH_SHORT).show();


                    Log.i("myTag","pin_check : " + isPinCheck);

                }

            }



        }

    };


    private View.OnClickListener cancelEvent = new View.OnClickListener() {
        public void onClick(View v) {
            dialogConparePinNum.dismiss();
            finish();

        }

    };


    @Override
    protected void onDestroy() {
        isRunning = false;
        super.onDestroy();


    }

    public class TimerHandler extends android.os.Handler{
        @Override
        public void handleMessage(Message msg) {
            countUp++;
            String temp = "";

            if(countUp / 60 < 10 )
                temp = "0" + countUp / 60 + ":";
            else
                temp = countUp / 60 + ":";


            if(countUp % 60 < 10 )
                temp = temp + "0" + countUp % 60;
            else
                temp = temp + countUp % 60;


            countupText.setText(temp);


            if(isPinCheck && countCheck && targetCheck)
            {
                stateImg.setImageResource(R.drawable.unlock);
                isRunning = false;
            }


        }


    }


}
