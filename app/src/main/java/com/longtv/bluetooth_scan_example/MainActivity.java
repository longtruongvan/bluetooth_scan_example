package com.longtv.bluetooth_scan_example;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private ScanFilter mScanFilter;
    private ScanSettings mScanSettings;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(receiver, intentFilter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        setScanFilter();
        setScanSettings();
//        mBluetoothAdapter.startDiscovery();
//        mBluetoothLeScanner.startScan(Arrays.asList(mScanFilter), mScanSettings, mScanCallback);
        mBluetoothLeScanner.startScan(mScanCallback);
    }

    String nameDevice = "";
    protected ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord mScanRecord = result.getScanRecord();
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes(), System.currentTimeMillis());
            final boolean isIBeacon = BeaconUtils.getBeaconType(deviceLe) == BeaconType.IBEACON;

            String deviceName = deviceLe.getAdRecordStore().getLocalNameComplete();
            if (deviceName.equals("")) {
                return;
            }
            if (isIBeacon) {
                if(!nameDevice.contains(deviceName)){
                    nameDevice +=deviceName+ " - ";
                    Log.d("LONGTV","12345");
                }
                final IBeaconManufacturerData iBeaconData = new IBeaconManufacturerData(deviceLe);
                String uuid = iBeaconData.getUUID();
                String major = String.valueOf(iBeaconData.getMajor());
                String minor = String.valueOf(iBeaconData.getMinor());
                String ranging = "unknown";
                String distance = "unknown";
                String accuracy = "unknown";
                int txPower = 0;
                IBeaconDevice beacon = new IBeaconDevice(deviceLe);
                uuid = beacon.getUUID().toString();
                minor = String.valueOf(beacon.getMinor());
                major = String.valueOf(beacon.getMajor());
                txPower = beacon.getCalibratedTxPower();
                distance = beacon.getDistanceDescriptor().toString().toLowerCase(Locale.US);
                accuracy = String.valueOf(beacon.getAccuracy());
                // send data to server
            }
        }
    };

    public double calculateDistance(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    private static SparseArray<byte[]> extractMetaData(final byte[] scanRecord) {
        int index = 0;
        final SparseArray<byte[]> map = new SparseArray<byte[]>();
        final int scanRecordLength = scanRecord.length;
        while (index < scanRecordLength) {
            final int length = scanRecord[index++];

            if (length == 0) {
                break;
            }

            final int type = scanRecord[index];

            if (type == 0) {
                break;
            }

            final byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

            map.put(type, data);

            index += length;
        }

        return map;
    }

    private void setScanSettings() {
        ScanSettings.Builder mBuilder = new ScanSettings.Builder();
        mBuilder.setReportDelay(0);
        mBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        mScanSettings = mBuilder.build();
    }

    private void setScanFilter() {
//        ScanFilter.Builder mBuilder = new ScanFilter.Builder();
//        ByteBuffer mManufacturerData = ByteBuffer.allocate(23);
//        ByteBuffer mManufacturerDataMask = ByteBuffer.allocate(24);
//        byte[] uuid = getIdAsByte(UUID.fromString("0CF052C297CA407C84F8B62AAC4E9020");
//        mManufacturerData.put(0, (byte)0xBE);
//        mManufacturerData.put(1, (byte)0xAC);
//        for (int i=2; i<=17; i++) {
//            mManufacturerData.put(i, uuid[i-2]);
//        }
//        for (int i=0; i<=17; i++) {
//            mManufacturerDataMask.put((byte)0x01);
//        }
//        mBuilder.setManufacturerData(224, mManufacturerData.array(), mManufacturerDataMask.array());
//        mScanFilter = mBuilder.build();
    }
}