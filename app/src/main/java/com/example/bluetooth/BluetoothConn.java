package com.example.bluetooth;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.*;
import android.bluetooth.*;
import android.util.Log;

public class BluetoothConn {
    public static final String TAG = "mBluetooth";
    private BluetoothAdapter bluetoothAdapter;//本地蓝牙适配器
    ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> bluetoothAddresses = new ArrayList<>();
    private ArrayList<String> rssiArr;//rssi的值
    private ArrayList<String> rssiName; //rssi的名字

    public void activeBluetooth() {
        Log.d(TAG, "蓝牙模块启动");
        initRssi();
        initBluetooth();//初始化
        openBluetooth();//开启
        registerBoard();//注册
        startDiscovery();//搜索


    }

    public void initRssi() {
        rssiName = new ArrayList<String>();
        rssiName.add("EWF1341805A");//增加信标
        rssiName.add("EWD8DA94E6E");
        rssiName.add("EWC6093F0E5");

        rssiArr = new ArrayList<String>();//距离由rssi获取
        rssiArr.add(null);
        rssiArr.add(null);
        rssiArr.add(null);

    }

    public void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.d(TAG, "设备不支持蓝牙");
        } else {
            Log.d(TAG, "设备支持蓝牙");
        }
    }

    public void openBluetooth() {
        boolean openResult = bluetoothAdapter.enable();
        if (openResult) {
            Log.d(TAG, "蓝牙设备启动");
        } else {
            Log.d(TAG, "蓝牙设备未启动");
        }
    }

    public void closeBluetooth() {
        boolean closeResult = bluetoothAdapter.disable();
        if (closeResult) {
            Log.d(TAG, "蓝牙设备关闭");

        } else {
            Log.d(TAG, "蓝牙设备关闭失败");

        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name, address, rssi, deviceString = "";
                for (int i = 0; i < rssiName.size(); i++) {
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        name = device.getName();
                        address = device.getAddress();
                        rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
                        if (!bluetoothAddresses.contains(address)) {
                            bluetoothAddresses.add(address);
                            if (name == null || name.equals("")) {
                                deviceString = address + " - RSSI " + rssi + "dBm";
                            } else {
                                deviceString = name + " - RSSI " + rssi + "dBm";
                            }
                            bluetoothDevices.add(deviceString);
                        }
                        Log.e(TAG, "-------------------");
                        Log.e(TAG, "discover:" + deviceString+address);
                        if (deviceString != null && rssiName.get(i).equals(name)) {
                            Log.e(TAG, "录入:" + deviceString);

                            rssiArr.set(i, rssiArr.get(i) + Integer.valueOf(rssi));





                        }
                    }
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.e(TAG, "重新搜索");
                bluetoothAdapter.cancelDiscovery();
                for(int i=0;i<rssiName.size();i++){
                    Log.e(TAG, "录入的Rssi信息："+rssiName.get(i)+rssiArr.get(i));
                }
            }
        }
    };

    public void registerBoard() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //registerReceiver(broadcastReceiver, filter);
    }

    private void startDiscovery() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(intent, 1);
        }
    }
}


