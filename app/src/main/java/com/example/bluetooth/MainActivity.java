package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private double distanceX, distanceY;
    public static final String TAG = "mBluetooth";
    private BluetoothAdapter bluetoothAdapter;//本地蓝牙适配器
    private String[] RssiArrName=new String[3];
    private double[] RssiArrNew = new double[3];
    private double[] RssiArrSum = new double[3];
    private double[] RssiArrNum = new double[3];
    private double[] RssiResult = new double[3];
    private String result;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activeBluetooth();//蓝牙模块
    }

    public void activeBluetooth() {
        Log.d(TAG, "蓝牙模块启动");
        initRssi();
        initBluetooth();//初始化
        openBluetooth();//开启
        registerBoard();//注册


    }


    public void initRssi() {
        RssiArrName[0]="EWF1341805A";
        RssiArrName[1]="EWD8DA94E6E";
        RssiArrName[2]="EWC6093F0E5";
        RssiArrNew[0] = RssiArrNew[1] = RssiArrNew[2] = -1.0;
        RssiArrSum[0] = RssiArrSum[2] = RssiArrSum[2] = 0;
        RssiArrNum[0] = RssiArrNum[2] = RssiArrNum[2] = 0;

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

    private int numSearch=0;
    int numFlag=0;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name;
                short rssi;
                double Rssi;
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    name = device.getName();
                    if (RssiArrName[0].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);
                        RssiArrSum[0] += Rssi;
                        RssiArrNum[0]++;
                        numFlag++;
                    } else if (RssiArrName[1].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);
                        RssiArrSum[1] += Rssi;
                        RssiArrNum[1]++;
                        numFlag++;
                    } else if (RssiArrName[2].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);
                        RssiArrSum[2] += Rssi;
                        RssiArrNum[2]++;
                        numFlag++;
                    }
                    if(numFlag>=3){
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, numSearch+"次结束搜索");
                if(numSearch++<2) {
                    numFlag=0;
                    bluetoothAdapter.startDiscovery();
                }
                else{
                    Log.e(TAG, "全部10次扫描完成");
                    Log.e(TAG,"F1:"+RssiArrSum[0]/RssiArrNum[0]+"  D8:"+RssiArrSum[1]/RssiArrNum[1]+"  C6:"+RssiArrSum[2]/RssiArrNum[2]);
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Rssi");
//                    distance(0, 12, 0, 0, 6, 4.8, getDistance(RssiArrNew[0]), getDistance(RssiArrNew[1]), getDistance(RssiArrNew[2]));


                    for (int k=0;k<3;k++){
                        RssiResult[k] =RssiArrSum[k]/RssiArrNum[k];
                    }
                    KNN knn = new KNN();

                    knn.init();
                    knn.Euclid(RssiResult);


                    result=knn.knn_result();

                    builder.setMessage("F1:"+RssiResult[0]+"\nD8:"+RssiResult[1]+"\nC6:"+RssiResult[2]+"\nResult:"+result);


//                    builder.setMessage(
//                            "F1 - " + RssiArrNew[0] + "dBm - " + getDistance(RssiArrNew[0]) + "\n" +
//                                    "D8 - " + RssiArrNew[1] + "dBm - " + getDistance(RssiArrNew[1]) + "\n" +
//                                    "C6 - " + RssiArrNew[2] + "dBm - " + getDistance(RssiArrNew[2]) + "\n" +
//                                    "坐标：" + distanceX + "," + distanceY);
                    builder.setPositiveButton("确定", null);
                    builder.show();
                }



            }
        }
    };

    public double getDistance(double Rssi) {
        Rssi = (Rssi - 60) / 36.0;
        double d = Math.pow(10.0, Rssi);
        d = (double) Math.round(d * 100) / 100;
        return d;
    }

    public void distance(double a1, double b1, double a2, double b2, double a3, double b3, double s1, double s2, double s3) {
        distanceX = Math.sqrt((((s1 + s2 - 12) * (s1 + s2 + 12) * (s1 - s2 + 12) * (s2 - s1 + 12)) / 144)) / 2;
        distanceX = (double) Math.round(distanceX * 100) / 100;
        distanceY = -s1 * s1 / 24 + s2 * s2 / 24 + 6;
        distanceY = (double) Math.round(distanceY * 100) / 100;

    }

    public void registerBoard() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void startDiscovery(View view) {
        Toast toast = Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT);
        toast.show();
        Log.e(TAG, "搜索模块启动");
        numFlag=0;
        numSearch=0;
        initRssi();
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
    }
}
