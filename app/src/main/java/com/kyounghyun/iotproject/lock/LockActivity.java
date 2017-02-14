package com.kyounghyun.iotproject.lock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.bluetooth.BluetoothLeService;
import com.kyounghyun.iotproject.database.DbOpenHelper;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.dialog.DialogConparePinNum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockActivity extends AppCompatActivity {


    @BindView(R.id.countupText)
    TextView countupText;
    @BindView(R.id.stateImg)
    ImageView stateImg;
    @BindView(R.id.countStateText)
    TextView countStateText;
    @BindView(R.id.targetStateText)
    TextView targetStateText;
    @BindView(R.id.pinStateText)
    TextView pinStateText;
    @BindView(R.id.logArea)
    LinearLayout logArea;


    int countUp = 0;

    int totalDeviceSize = 0;
    int connectDeviceMax = 0;
    int connectDeviceCount = 0;
    int unconnectDeviceCount = 0;

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

    private BluetoothLeService mBluetoothLeService;




    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";



    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            Log.i("myTestLog","onServiceConnected" );

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }


            /**
             * 연결된 기기 갯수 파악
             */

            new Thread(new Runnable() {
                @Override
                public void run() {

                    for(int i = 0 ; i< moduleList.size(); i++){


                        Log.i("myTestLog","counting attempt : " + moduleList.get(i).identNum);

                        connectBle(moduleList.get(i).identNum);

                    }

                    Log.i("myTag","count_check : " + countCheck);

                }
            }).start();


            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i("myTag","application onServiceDisconnected");
        }
    };



    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;

            Log.i("myTestLog"," >>>> onConnectionStateChange");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;

                final BluetoothDevice device = gatt.getDevice();


                connectDeviceCount++;

                if(mDbOpenHelper.DbTargetFind(device.getAddress()) != null) {
                    targetDeviceCount++;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                addLog("Connected Device : " + device.getName());
                            }
                        });
                    }
                }).start();

//                Log.i("myTag","connected Device : " + device.getName());
//
//
//                addLog("Connected Device : " + device.getName());
//
//
//                if(mDbOpenHelper.DbTargetFind(device.getAddress()) != null)
//                    targetDeviceCount++;
//
//                if(targetDeviceMax == targetDeviceCount) {
//                    targetCheck = true;
//
//                    addLog("타겟 조건 결과 : Success");
//
//                    targetStateText.setText("Success");
//                    targetStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E
//                }





            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                Log.i("myTag", "---- Disconnected ----");

                final BluetoothDevice device = gatt.getDevice();
                Log.i("myTestLog","disconnected Device : " + device.getName());

                unconnectDeviceCount++;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                addLog("Disconnected Device : " + device.getName());
                            }
                        });
                    }
                }).start();


            }

            else{
                Log.i("myTestLog","newState : " + newState);
            }
        }
    };


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                Log.i("myTestLog"," >>> BroadcastReceiver 연결성공" );

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i("myTestLog", " >>> BroadcastReceiver 연결 해제");

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
                        Thread.sleep(10); // 10 = 1
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            }
        });

        remianThread.start();



        totalDeviceSize = mDbOpenHelper.DbMainSelect().size();

        String temp = ApplicationController.connectInfo.getString("countNum", "");
        if(temp.equals(""))
            connectDeviceMax = 0;
        else
            connectDeviceMax = Integer.parseInt(ApplicationController.connectInfo.getString("countNum", ""));

        targetDeviceMax = mDbOpenHelper.DbTarget().size();






        /**
         * 타겟 기기 유무 조사
         */
        if(ApplicationController.connectInfo.getBoolean("target_check", false)) {


        }
        else {

            addLog("특정 조건 결과 : Success");

            targetCheck = true;

            targetStateText.setText("Success");
            targetStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E
        }


        /**
         * pin 유무 체크
         */
        if(ApplicationController.connectInfo.getBoolean("pin_check", false))
        {
            showInputPinDialog();

        }
        else{
            addLog("핀 조건 결과 : Success");

            isPinCheck = true;
            pinStateText.setText("Success");
            pinStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E
            Log.i("myTag","pin_check : " + isPinCheck);

        }



    }


    public Boolean connectBle(String mDeviceAddress){


        Log.i("myTestLog"," > counting attempt : " + mDeviceAddress);

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);

        if (mBluetoothAdapter == null || mDeviceAddress == null) {
            Log.i("myTestLog", "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (device == null) {
            Log.i("myTestLog"," >> Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        device.connectGatt(this, false, mGattCallback);
        Log.i("myTestLog"," >><<");


//        Log.i("myTestLog"," > counting attempt : " + mDeviceAddress);
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
//
//
//        if (device == null) {
//            Log.i("myTestLog"," >> Device not found.  Unable to connect.");
//
//            return false;
//        }
//
////        device.connectGatt(this, false, mGattCallback);
//
//
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.i("myTestLog"," >> Connect request result = " + result);
//
////
////            while(true){
////                if(result || !result)
////                    break;
////            }
//
//
//            if(result){
//
//                connectDeviceCount++;
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable(){
//                            @Override
//                            public void run() {
//                                addLog("Connected Device : " + device.getName());
//                            }
//                        });
//                    }
//                }).start();
//
//                Log.i("myTag","connected Device : " + device.getName());
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
//
//                if(mDbOpenHelper.DbTargetFind(device.getAddress()) != null)
//                    targetDeviceCount++;
//
//
//                mBluetoothLeService.close(); // 연결확인 후 바로 닫아준다.
//            }
//            else{
//                unconnectDeviceCount++;
//            }
//        }
//        else{
//            Log.i("myTag", "mBluetoothLeService null ");
//            Log.i("myTestLog"," >> mBluetoothLeService null ");
//
//        }
//
//
//
        return false;

    }

    @Override
    protected void onResume() {
        super.onResume();


        /**
         * BLE 서비스를 등록
         */
        Intent gattServiceIntent = new Intent(getApplicationContext(), BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

    }

    @Override
    protected void onPause() {
        super.onPause();


        unregisterReceiver(mGattUpdateReceiver);

    }

    public void addLog(String text){
        TextView addLogText = new TextView(this);
        addLogText.setText(text);
        addLogText.setTextSize(12);

        logArea.addView(addLogText);
    }

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

                    pinStateText.setText("Success");
                    pinStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E

                    Toast.makeText(getApplicationContext(),"Pin 인증 성공!",Toast.LENGTH_SHORT).show();


                    addLog("핀 조건 결과 : Success");

                    Log.i("myTag","pin_check : " + isPinCheck);

                    dialogConparePinNum.dismiss();
                }
                else{
                    isPinCheck = false;
                    Toast.makeText(getApplicationContext(),"pin 인증 실패!",Toast.LENGTH_SHORT).show();

                    pinStateText.setText("Fail");
                    pinStateText.setTextColor(Color.parseColor("#E1092E"));//#E1092E


                    addLog("핀 조건 결과 : Fail");

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

            if((countUp/100) / 60 < 10 )
                temp = "0" + (countUp/100) / 60 + "m ";
            else
                temp = (countUp/100) / 60 + "m ";


            if((countUp/100) % 60 < 10 )
                temp = temp + "0" + (countUp/100) % 60 + "s ";
            else
                temp = temp + (countUp/100) % 60 + "s ";


            if(countUp % 100 < 10 )
                temp = temp + "0" + countUp % 100 + "ms ";
            else
                temp = temp + countUp % 100 + "ms ";



            countupText.setText(temp);


//            Log.i("myTag", countCheck + " / " +isPinCheck  + " / " + targetCheck );

            if(isPinCheck && countCheck && targetCheck)
            {

                addLog("-------- OPEN --------");

                stateImg.setImageResource(R.drawable.unlock);
                isRunning = false;
            }

//            Log.i("myTag", " / " +unconnectDeviceCount+connectDeviceCount  + " / " + connectDeviceMax );



            if(connectDeviceCount >= connectDeviceMax) {

                if(!countCheck)
                    addLog("연결 조건 결과 : Success");

                countStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E
                countStateText.setText("Success");
                countCheck = true;
            }

            if(targetDeviceMax == targetDeviceCount) {

                if(!targetCheck) {
                    addLog("타겟 조건 결과 : Success");
                }

                targetCheck = true;
                targetStateText.setText("Success");
                targetStateText.setTextColor(Color.parseColor("#2D7BD7"));//#E1092E
            }

//            Log.i("myTestLog", unconnectDeviceCount + " / " + connectDeviceCount + " / " + totalDeviceSize);

            if(unconnectDeviceCount + connectDeviceCount == totalDeviceSize){

                isRunning = false;

                if(!countCheck){

                    addLog("연결 조건 결과 : Fail");

                    countStateText.setText("Fail");
                    countStateText.setTextColor(Color.parseColor("#E1092E"));//#E1092E
                }

                if(!targetCheck){

                    addLog("타켓 조건 결과 : Fail");

                    targetStateText.setText("Fail");
                    targetStateText.setTextColor(Color.parseColor("#E1092E"));//#E1092E
                }
            }


        }


    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_CAROUSEL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED_OTA);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECT_OTA);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED_OTA);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CHARACTERISTIC_ERROR);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESS);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_FAILED);
        intentFilter.addAction(BluetoothLeService.ACTION_PAIR_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.EXTRA_BOND_STATE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_COMPLETED);
        return intentFilter;
    }


    private void displayGattServices(List<BluetoothGattService> gattServices) {


        if (gattServices == null){

            Log.i("myTestLog"," >>> gattServices null ");
            return;
        }

        Log.i("myTestLog"," >>> gattServices " + gattServices.size());

        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            Log.i("myTestLog",">>>>>>>>>>>" + uuid);
        }


    }

}
