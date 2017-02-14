package com.kyounghyun.iotproject.main;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.condition.ConditionActivity;
import com.kyounghyun.iotproject.database.DbOpenHelper;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.lock.LockActivity;
import com.kyounghyun.iotproject.main.presenter.MainAdapter;
import com.kyounghyun.iotproject.main.view.MainView;
import com.kyounghyun.iotproject.scan.DeviceSearchActivity;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kyounghyun.iotproject.advertising.Constants.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity implements MainView {


    @BindView(R.id.mainList)
    RecyclerView recyclerView;
    @BindView(R.id.deviceRegisterArea)
    LinearLayout deviceRegisterArea;
    @BindView(R.id.conditionRegisterArea)
    LinearLayout conditionRegisterArea;
    @BindView(R.id.unlockArea)
    LinearLayout unlockArea;


    LinearLayoutManager mLayoutManager;
    ArrayList<ItemData> itemDatas;
    MainAdapter mAdapter;

    DbOpenHelper mDbOpenHelper;

    public final int REQUESTCODE = 1;


    private boolean mScanning;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;



    //Back 키 두번 클릭 여부 확인
    private final long FINSH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;

        itemDatas = new ArrayList<ItemData>();
        mAdapter = new MainAdapter(itemDatas , this);
        recyclerView.setAdapter(mAdapter);

        //각 item의 크기가 일정할 경우 고정
        recyclerView.setHasFixedSize(true);

        // layoutManager 설정
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);



        itemDatas = mDbOpenHelper.DbMainSelect();
        mAdapter.renewDatas(itemDatas);


        /**
         *
         */
        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }


        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            Log.i("myTag"," Device: " + device.getName() + " : " + device.getAddress());

        }


        // Initializes list view adapter.
//        mLeDeviceListAdapter = new DeviceAdapter(getApplicationContext());
//        listView.setAdapter(mLeDeviceListAdapter);

        setPermission();

        scanLeDevice(true);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) {
                    scanLeDevice(true);
                }

                else if(resultCode == RESULT_CANCELED) {
                    scanLeDevice(false);
                    finish();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * android 6.0 permission
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void setPermission(){

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.BLUETOOTH");

            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, 1001); //Any number
            }


            permissionCheck = this.checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");

            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 1001); //Any number
            }


            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001); //Any number
            }

            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
//                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,}, 1001); //Any number
            }


        }else{
            Log.i("myTag", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUESTCODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();

                    mAdapter.setOffline();

                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);




            ArrayList<ItemData> tempList = mDbOpenHelper.DbMainSelect();

            for(int i = 0; i<tempList.size(); i++){
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(tempList.get(i).identNum);

                mAdapter.setOnlineCheck(device);
            }


        }
        invalidateOptionsMenu();
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            if(mDbOpenHelper.DbFind(device.getAddress()) != null){


                                if(device.getBondState() == BluetoothDevice.BOND_BONDED || device.getBondState() == BluetoothDevice.BOND_BONDING) {
                                    mAdapter.setOnline(device.getAddress());
                                }
                                else{
                                    pairDevice(device);

                                    mAdapter.setOnlineCheck(device);


//                                Log.i("myTag", "scanRecord : " +  ByteArrayToString(scanRecord));
//                            Log.i("myTag", "string : " +  device.toString());

                                    String scanHex = bytesToHex(scanRecord);
//                                Log.i("myTag", "data:" + scanHex);

                                    int startByte = 2;
                                    boolean patternFound = true;
                                    while (startByte <= 5) {
                                        if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                                                ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                                            patternFound = true;
                                            break;
                                        }
                                        startByte++;
                                    }

                                    if (patternFound) {
                                        //Convert to hex String
                                        byte[] uuidBytes = new byte[16];
                                        System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                                        String hexString = bytesToHex(uuidBytes);

                                        //Here is your UUID
                                        String uuid =  hexString.substring(0,8) + "-" +
                                                hexString.substring(8,12) + "-" +
                                                hexString.substring(12,16) + "-" +
                                                hexString.substring(16,20) + "-" +
                                                hexString.substring(20,32);

//                                    Log.i("myTag", "uuid : " +  uuid);

                                        //Here is your Major value
                                        int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

//                                    Log.i("myTag", "major : " +  major);

                                        //Here is your Minor value
                                        int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

//                                    Log.i("myTag", "minor : " +  minor);


                                    }
                                }




                            }



                        }
                    });


                }


            };


    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private void pairDevice(BluetoothDevice device) {

        Log.i("myTag", "name : " + String.valueOf(device.getName()));
        Log.i("myTag", "address : " + String.valueOf(device.getAddress()));

        device.createBond();

    }

    @OnClick(R.id.deviceRegisterArea)
    public void moveDeviceRegister(){
        Intent intent = new Intent(getApplicationContext(), DeviceSearchActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.conditionRegisterArea)
    public void moveConditionRegister(){
        Intent intent = new Intent(getApplicationContext(), ConditionActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.unlockArea)
    public void moveLock(){
        Intent intent = new Intent(getApplicationContext(), LockActivity.class);
        startActivity(intent);
    }



    @Override
    protected void onRestart() {
        super.onRestart();

        itemDatas = mDbOpenHelper.DbMainSelect();
        mAdapter.renewDatas(itemDatas);
        scanLeDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    @Override
    public void onBackPressed() {
        long tempTime        = System.currentTimeMillis();
        long intervalTime    = tempTime - backPressedTime;

        /**
         * Back키 두번 연속 클릭 시 앱 종료
         */

        if ( 0 <= intervalTime && FINSH_INTERVAL_TIME >= intervalTime ) {
            super.onBackPressed();
        }
        else {
            backPressedTime = tempTime;
            Toast.makeText(getApplicationContext(),"뒤로 가기 키을 한번 더 누르시면 종료됩니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deleteModule(final int position) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // 여기서 부터는 알림창의 속성 설정
        builder.setMessage("삭제하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){


                        mDbOpenHelper.DbDelete(String.valueOf(itemDatas.get(position).Id));
                        itemDatas = mDbOpenHelper.DbMainSelect();
                        mAdapter.renewDatas(itemDatas);


                        Toast.makeText(getApplicationContext(),"삭제 완료",Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = builder.create();    // 알림창 객체 생성
        dialog.show();

    }

    public void checkBlueTooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 장치가 블루투스 지원하지 않는 경우
            Toast.makeText(getApplicationContext(),"해당 디바이스는 블루투스를 지원하지 않습니다.",Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();   // 어플리케이션 종료
                }
            }, 2000);

        } else {
            // 장치가 블루투스 지원하는 경우
            if (!mBluetoothAdapter.isEnabled()) {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요첨
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                // 블루투스를 지원하며 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택.
            }
        }
    }
}
