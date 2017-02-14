package com.kyounghyun.iotproject.scan;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kyounghyun.iotproject.R;
import com.kyounghyun.iotproject.advertising.Constants;
import com.kyounghyun.iotproject.application.ApplicationController;
import com.kyounghyun.iotproject.database.DbOpenHelper;
import com.kyounghyun.iotproject.database.ItemData;
import com.kyounghyun.iotproject.scan.presenter.DeviceAdapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;
import static android.util.Log.i;


public class DeviceSearchActivity extends ActionBarActivity {

    @BindView(R.id.listview)
    ListView listView;

    private DeviceAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;


    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;


    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final long SERVICE_DISCOVERY_TIMEOUT = 10000;

    DbOpenHelper mDbOpenHelper;

    private final BroadcastReceiver mPairingRequestReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i("myTag", "get Action : " + action);

            if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                try {
                    BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                    int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                    //the pin in case you need to accept for an specific pin
                    Log.i("myTag", "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",1234));
                    byte[] pinBytes;
                    pinBytes = (""+pin).getBytes("UTF-8");
                    device.setPin(pinBytes);
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true);
                } catch (Exception e) {
                    Log.i("myTag", "Error occurs when trying to auto pair");
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_search);

        ButterKnife.bind(this);


        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // ActionBar의 배경색 변경
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF3F51B5));

        getSupportActionBar().setElevation(0); // 그림자 없애기

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.actionbar_layout, null);

        getSupportActionBar().setCustomView(mCustomView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


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

        mDbOpenHelper = ApplicationController.getInstance().mDbOpenHelper;

//        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//
//        Log.i("testLog","start");
//
//        mScanCallback = new SampleScanCallback();
//        mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);


        Log.i("myTag","registerReceiver start");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mPairingRequestReceiver, filter);

        Log.i("myTag","registerReceiver end");






    }




    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        // Comment out the below line to see all BLE devices around you
        builder.setServiceUuid(Constants.Service_UUID);
        scanFilters.add(builder.build());

        return scanFilters;
    }

    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * BLE 서비스를 등록
         */

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new DeviceAdapter(getApplicationContext());
        listView.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);

        /**
         * 검색된 블루투스 디바이스에 대한 클릭이벤트
         */

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                scanLeDevice(false);

                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

                if(mDbOpenHelper.DbFind(device.getAddress()) != null){
                    Toast.makeText(getApplicationContext(),"이미 등록된 장치입니다.",Toast.LENGTH_SHORT).show();

                }
                else{
//                    unpairDevice(device);

                    ItemData inputData = new ItemData(0,device.getName(),device.getAddress(),0);
                    registerDevice(inputData);


                }



            }
        });

    }




    public void registerDevice(final ItemData inputData){

        final AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSearchActivity.this);

        // 여기서 부터는 알림창의 속성 설정
        builder.setTitle("등록되지 않은 장치입니다.")
                .setMessage("등록하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){


                        /**
                         * 연결 시도
                         */

                        String address = inputData.identNum;
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                        pairDevice(device);

                        mDbOpenHelper.DbInsert(inputData);
                        Toast.makeText(getApplicationContext(),"등록 완료",Toast.LENGTH_SHORT).show();



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

//    private void pairDevice(BluetoothDevice device) {
//        try {
////            if(device.getBondState() != BOND_BONDED && device.getBondState() != BOND_BONDING)
////                Log.i("myTag", "Start Pairing...");
//
//
//            Method m = device.getClass().getMethod("createBond", (Class[]) null);
//            m.invoke(device, (Object[]) null);
//
//
//
////            if(device.getBondState() == BOND_BONDED)
////                Log.i("myTag", "Pairing finished.");
//
//        } catch (Exception e) {
//            Log.i("myTag", e.getMessage());
//        }
//
//
////        Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
////        intent.putExtra(EXTRA_DEVICE, device);
////        int PAIRING_VARIANT_PIN = 272;
////        intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
////        sendBroadcast(intent);
//
////        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
////        startActivityForResult(intent, REQUEST_PAIR_DEVICE);
//
//
//    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.i("myTag", e.getMessage());
        }
    }

    private void pairDevice(BluetoothDevice device) {


        Log.i("myTag", "name : " + String.valueOf(device.getName()));
        Log.i("myTag", "address : " + String.valueOf(device.getAddress()));

        device.createBond();

//        try {
//            Log.i("myTag", "Start Pairing... with: " + device.getName());
//
////            i("myTag", "state : " + device.getBondState());
//
////            ParcelUuid[] parcelUuid = device.getUuids();
////
////            for(int i = 0; i< parcelUuid.length; i++){
////                Log.i("myTag", "Uuid "+ i +" : " + parcelUuid[i]);
////
////            }
//
//            if(device.getBondState() != BOND_BONDED && device.getBondState() != BOND_BONDING){
//
//                if(device.createBond()) {
//                    Log.i("myTag", "Pairing finished.");
//                }
//
//            }
//            else{
//                Log.i("myTag", "alreadey Pairing " );
//
//            }
//
//
////            BluetoothClass test = device.();
////            Log.i("myTag", String.valueOf(test.hashCode()));
//
//
//        } catch (Exception e) {
//            Log.i("myTag", e.getMessage());
//        }

//        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
//        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
//        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
//        intent.putExtra(EXTRA_DEVICE, device);
//        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
//        int PAIRING_VARIANT_PIN = 0;
//        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();

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
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    public static String ByteArrayToString(byte[] ba)
    {
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (byte b : ba)
            hex.append(b + " ");

        return hex.toString();
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            i("myTag", String.valueOf(device.getName()));
//                            i("myTag", "scanRecord : " +  ByteArrayToString(scanRecord));
//                            Log.i("myTag", "string : " +  device.toString());

                            int len = scanRecord.length;
                            String scanHex = bytesToHex(scanRecord);
//                            i("myTag", "data:" + scanHex);

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

//                                i("myTag", "uuid : " +  uuid);

                                //Here is your Major value
                                int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

//                                i("myTag", "major : " +  major);

                                //Here is your Minor value
                                int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);

//                                i("myTag", "minor : " +  minor);


                            }



                            if(mDbOpenHelper.DbFind(device.getAddress()) == null){

                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
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


    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);

            for (ScanResult result : results) {

                i("testLog",result.getDevice().getName());
                i("testLog",result.getDevice().toString());
            }

        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            i("testLog",result.getDevice().getName());
            i("testLog",result.getDevice().toString());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

        }
    }

}