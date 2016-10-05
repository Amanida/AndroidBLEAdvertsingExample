package com.example.amanida.yourgraduationproject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btn_advertise;
    Button btn_discover;
    TextView textView;
    private BluetoothLeScanner mBluetoothLeScanner;

    private Handler mHandler = new Handler();
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if(result == null)
                return;
//            StringBuilder builder = new StringBuilder(result.getDevice().getName());
//            StringBuilder builder = new StringBuilder(result.getDevice().getName());
//            builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
            ParcelUuid uuid = result.getScanRecord().getServiceUuids().get(0);
            byte[] data = result.getScanRecord().getServiceData(uuid);
            if (data == null)
                return;
            Log.i("BLE", "result UUID : " + uuid.toString());
            Log.i("BLE", "result data : " + data);
            String res = new String(result.getScanRecord().getServiceData(uuid), Charset.forName("UTF-8"));
            Log.i("BLE", "onScanResult" + res);
            Toast.makeText(MainActivity.this, "Scan Result", Toast.LENGTH_SHORT).show();
            textView.setText(res);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_advertise = (Button) findViewById(R.id.btn_advertise);
        btn_discover = (Button) findViewById(R.id.btn_discover);
        textView = (TextView) findViewById(R.id.text_result);

        btn_advertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                advertise();
            }
        });

        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover();
            }
        });

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }


    public void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        if (advertiser == null) {
            // 블루투스 꺼져있으면 advertiser = null
            Toast.makeText(this, "Please turn on bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Advertise Setting
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)     // Advertise 주기
                    /*
                    다른 옵션들
                    The ADVERTISE_MODE_LOW_POWER option is the default setting for advertising and transmits the least frequently in order to conserve the most power.
                    The ADVERTISE_MODE_BALANCED option attempts to conserve power without waiting too long between advertisements.
                    */
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)         // Tx 신호 세기 (신호가 갈 수 있는 거리 달라짐)
                .setConnectable(false)
                .build();

        // Set Advertise Data
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.ble_uuid)));  //ble_uuid는 value/string.xml에.
        final AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)    // Device Name 넣으니까 Size 넘었다고 함;
                .addServiceUuid(pUuid)
                .addServiceData(pUuid, "Data".getBytes(Charset.forName("UTF-8")))       //여기다가 데이터 넣는듯?
                .build();

        // Set Advertise Callback
        AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d("BLE", "Advertising Success");
                Toast.makeText(MainActivity.this, "Advertising Success", Toast.LENGTH_SHORT).show();
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.d("BLE", "Data.tostring : " + data.toString());
                Log.d("BLE", "Data.serviceData.tostring : " + data.getServiceData());
                Log.d("BLE", "Data.serviceData.size : " + data.getServiceData().size());

                String description = "";
                if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED)
                    description = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS)
                    description = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED)
                    description = "ADVERTISE_FAILED_ALREADY_STARTED";
                else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE)
                    description = "ADVERTISE_FAILED_DATA_TOO_LARGE";
                else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR)
                    description = "ADVERTISE_FAILED_INTERNAL_ERROR";
                else description = "unknown";

                Log.e("BLE", "Advertising Failure : " + description);
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, data, advertiseCallback);
    }

    public void discover() {
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        if (mBluetoothLeScanner == null) {
            // 블루투스 꺼져있거나 ble 미지원이면 널 날아옴
            Toast.makeText(this, "Please turn on bluetooth.", Toast.LENGTH_SHORT).show();
            return;
        }
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(UUID.fromString( getString(R.string.ble_uuid ) ) ) )
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 10000); // 10초 뒤 scan stop
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
