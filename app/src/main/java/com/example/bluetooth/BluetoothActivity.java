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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class BluetoothActivity extends AppCompatActivity {

    private TextView textView;//定义textView用于显示相关信息
    public final int bluetoothNumUsing = 3;//整体使用的蓝牙设备的个数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        textView = findViewById(R.id.textView);//textView绑定
        //滚动条
        TextView textView=(TextView)findViewById(R.id.textView);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        activeBluetooth();//启用蓝牙模块
    }

    public static final String TAG = "mBluetooth";//Logcat debug
    private BluetoothAdapter bluetoothAdapter;//本地蓝牙适配器
    private String[] RssiArrName = new String[bluetoothNumUsing];//蓝牙名称
    private double[] RssiArrNew = new double[bluetoothNumUsing];//单次测量的Rssi数据

//-----------------------------------------------用求和的平均的方式--------------------------------------------------------------------------
    private double[] RssiArrSum = new double[bluetoothNumUsing];//多次测量Rssi的和
    private double[] RssiArrNum = new double[bluetoothNumUsing];//测量次数
    private double[] RssiResult = new double[bluetoothNumUsing];//Rssi最终结果
    private String result;//用于knn匹配结果输出
//-----------------------------------------------用求和的平均的方式--------------------------------------------------------------------------


//-----------------------------------------------Kalman--------------------------------------------------------------------------
    private double[][] kalmanDate = new double[3][20];//测量值
    private int[] kalmanSearchNum = new int[3];//测量次数
    private double[][] kalmanNew = new double[3][20];// kalman新的估计预测值
    private double[] kalmanResult = new double[3];//kalman过滤后的最终结果
    private Kalman[] kalman = new Kalman[3];//R固定，Q越大，代表越信任侧量值，Q无穷代表只用测量值；反之，Q越小代表越信任模型预测值，Q为零则是只用模型预测。

    private String result_kalman;//用于knn匹配结果输出

//-----------------------------------------------Kalman--------------------------------------------------------------------------

    public void activeBluetooth() {
        Log.d(TAG, "蓝牙模块启动");
        initRssi();//初始化Rssi数据
        initBluetooth();//初始化
        openBluetooth();//开启
        registerBoard();//注册
    }

    public void initRssi() {
        RssiArrName[0] = "EWF1341805A";
        RssiArrName[1] = "EWD8DA94E6E";
        RssiArrName[2] = "EWC6093F0E5";
        RssiArrNew[0] = RssiArrNew[1] = RssiArrNew[2] = 100;
        RssiArrSum[0] = RssiArrSum[1] = RssiArrSum[2] = 0;
        RssiArrNum[0] = RssiArrNum[1] = RssiArrNum[2] = 0;

        kalmanSearchNum[0] = kalmanSearchNum[1] = kalmanSearchNum[2] = 0;
        kalman[0]=new Kalman(30,12);
        kalman[1]=new Kalman(30,12);
        kalman[2]=new Kalman(30,12);

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


    private int numSearch = 0;
    int numFlag = 0;//优化搜索，但检测到3个设备时停止搜索
    public final int sumSearchNum = 3;//总搜索次数
    private String out1 = "";
    String name;
    short rssi;
    double Rssi;


    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {


                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    name = device.getName();
                    if (RssiArrName[0].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);

//-----------------------------------------------Kalman--------------------------------------------------------------------------
                        kalmanDate[0][kalmanSearchNum[0]] = Rssi;
                        kalmanSearchNum[0]++;
//-----------------------------------------------Kalman--------------------------------------------------------------------------

//-----------------------------------------------求和平均--------------------------------------------------------------------------
                        RssiArrSum[0] += Rssi;
                        RssiArrNum[0]++;
//-----------------------------------------------求和平均--------------------------------------------------------------------------


                        numFlag++;//优化搜索，但检测到3个设备时停止搜索
                    } else if (RssiArrName[1].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);


                        kalmanDate[1][kalmanSearchNum[1]] = Rssi;
                        kalmanSearchNum[1]++;


                        RssiArrSum[1] += Rssi;
                        RssiArrNum[1]++;
                        numFlag++;
                    } else if (RssiArrName[2].equals(name)) {
                        rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        Rssi = Math.abs(rssi);
                        Log.e(TAG, "录入:" + name + "=" + Rssi);

                        kalmanDate[2][kalmanSearchNum[2]] = Rssi;
                        kalmanSearchNum[2]++;


                        RssiArrSum[2] += Rssi;
                        RssiArrNum[2]++;
                        numFlag++;
                    }
                    if (numFlag >= 3) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e(TAG, numSearch + "次结束搜索");
                if (++numSearch < sumSearchNum) {
                    numFlag = 0;
                    bluetoothAdapter.startDiscovery();
                } else {
                    Log.e(TAG, "全部扫描完成");

//-----------------------------------------------Kalman--------------------------------------------------------------------------

                    Log.e(TAG, "-------------Kalman--------------");
                    for (int k = 0; k < 3; k++) {
                        for (int m = 0; m < kalmanSearchNum[k]; m++) {
                            Log.e(TAG, "kalmanDate" + k + " is " + kalmanDate[k][m]);
                        }
                    }
                    for (int k = 0; k < 3; k++) {
                        for (int m = 0; m < kalmanSearchNum[k]; m++) {
                            kalmanNew[k][m] = kalman[k].kalmanFilter(kalmanDate[k][m]);
                            Log.e(TAG, "kalmanNew" + k + " is " + kalmanNew[k][m]);
                        }
                        //取过滤到最后的结果
                        kalmanResult[k] = kalmanNew[k][kalmanSearchNum[k] - 1];
                        Log.e(TAG, "kalmanResult" + k + " is " + kalmanResult[k]);
                    }

//-----------------------------------------------Kalman-------------------------------------------------------------------------

//-----------------------------------------------KNN匹配(kalman结果)--------------------------------------------------------------------------
                    KNN knn_Kalman = new KNN();
                    try {
                        InputStream is = getAssets().open("rssi.txt");
                        knn_Kalman.init(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    knn_Kalman.Euclid(kalmanResult);
                    result_kalman = knn_Kalman.knn_result();

//-----------------------------------------------KNN匹配(kalman结果)--------------------------------------------------------------------------

//-----------------------------------------------求和平均--------------------------------------------------------------------------

                    for (int k = 0; k < 3; k++) {
                        if (RssiArrNum[k] == 0) {
                            RssiResult[k] = 100;//一个都没找到

                        } else {
                            RssiResult[k] = RssiArrSum[k] / RssiArrNum[k];
                        }
                    }
//-----------------------------------------------求和平均--------------------------------------------------------------------------

//-----------------------------------------------KNN匹配(求和平均结果)--------------------------------------------------------------------------
                    KNN knn = new KNN();
                    try {
                        InputStream is = getAssets().open("rssi.txt");
                        knn.init(is);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    knn.Euclid(RssiResult);
                    result = knn.knn_result();
//-----------------------------------------------KNN匹配(求和平均结果)--------------------------------------------------------------------------


//-----------------------------------------------TextView输出--------------------------------------------------------------------------
                    out1 += "\n------------------------------------------" +
                            "\nF1:" + (int)RssiResult[0] + "\nD8:" + (int)RssiResult[1] + "\nC6:" + (int)RssiResult[2] + "\n求和平均结果:" + result+
                            "\n\nF1:" + (int)kalmanResult[0] +"\nD8:" + (int)kalmanResult[1] + "\nC6:" + (int)kalmanResult[2] + "\nkalman结果:" + result_kalman;
                    Log.e(TAG, out1);
                    textView.setText(out1);
//-----------------------------------------------TextView输出--------------------------------------------------------------------------

                    //循环执行搜索
                    //关闭广播
                    bluetoothAdapter.cancelDiscovery();
                    //初始化
                    numFlag = 0;
                    numSearch = 0;
                    initRssi();
                    Log.e(TAG, "开始搜索");
                    bluetoothAdapter.startDiscovery();
                }
            }
        }
    };

    public void registerBoard() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void startDiscovery(View view) {
        Toast toast = Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT);
        toast.show();
        numFlag = 0;
        if (bluetoothAdapter.isEnabled()) {
            numSearch = 0;
            initRssi();
            Log.e(TAG, "开始搜索");
            bluetoothAdapter.startDiscovery();
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
    }
}
