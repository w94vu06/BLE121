package com.Clerk.ble_example.Controller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.Clerk.ble_example.Module.Service.BluetoothLeService;
import com.Clerk.ble_example.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class WaveActivity extends AppCompatActivity {

    private final static String TAG = "Wave";
    private WaveUtil waveUtil;
    private WaveView wave_view1;
    private TextView tv5,tv_countDown;
    private Button btn_ms;
    public ArrayList<String> result = new ArrayList<>();
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_item);

        initECG();
        EventBus.getDefault().register(this);
    }

    public void initECG() {
        waveUtil = new WaveUtil();
        wave_view1 = findViewById(R.id.wave_view1);
        tv_countDown = findViewById(R.id.tv_countDown);
        tv5 = findViewById(R.id.textView5);
        btn_ms = findViewById(R.id.btn_ms);
        tv5.setText("---");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收)數據
        registerReceiver(LineDataReceiver, intentFilter);
    }

    public final BroadcastReceiver LineDataReceiver = new BroadcastReceiver() {
        int[] values;
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Intent post = new Intent(WaveActivity.this, ResultActivity.class);
            post.setClass(WaveActivity.this, ResultActivity.class);

            //接收來自藍芽傳回的資料
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Intent it = new Intent();
                it.setClass(WaveActivity.this, ResultActivity.class);

                btn_ms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        result.clear();
                        new CountDownTimer(30000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                tv_countDown.setText(""+millisUntilFinished/1000);
                            }
                            @Override
                            public void onFinish() {
                                tv_countDown.setText("0");
                                waveUtil.stop();
                                post.putExtra("resultData", result);
                                startActivity(post);
                                Log.d("size", "onFinish: " + result.size());
                            }
                        }.start();
                    }
                });
            }
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] getByteData = new byte[20];
                getByteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String hex = BluetoothLeService.bytesToAscii(getByteData);
                hex = hex.trim();
//                tv5.setText(ascii);
//                ascii = ascii.trim();
//                ascii = ascii.replaceAll("\n+", "!");
//                String[] tokens = ascii.split("!");
//                ArrayList<String> list = new ArrayList<String>(Arrays.asList(tokens));
//                values = new int[list.size()];
//                for (i = 0; i < list.size(); i++) {
//                    try {
//                        values[i] = Integer.parseInt(list.get(i));
////                        waveUtil.showWaveData(values[i]);
//                    } catch (Exception e) {
//
//                    }
//                }
                //waveUtil.showWaveData(values[i]);
                Log.d("gggg", "心率:"+ Arrays.toString(values));
                Log.d("dddd", "心率:"+ getByteData);
                Log.d("aaaa", "心率:\n"+hex);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收)數據
        registerReceiver(LineDataReceiver, intentFilter);
    }
    public void onStop() {
        super.onStop();
        unregisterReceiver(LineDataReceiver);
    }

    public class WaveUtil {
        private Timer timer;
        private TimerTask timerTask;

        String hexResult = null;

        public void showWaveData(int val){
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run(){
                    try {
                        hexResult = String.valueOf(val);
                        result.add(hexResult);
                        wave_view1.showLine(val);
                    } catch (OutOfMemoryError error) {
                        Log.d(TAG, "run: " + error);
                        return;
                    }
                }
            };
            //500表示調用schedule方法後等待500ms後調用run方法，50表示以後調用run方法的時間間隔
            timer.schedule(timerTask,0,999999999);
        }

        public void stop(){
            if(timer != null){
                timer.cancel();
                timer.purge();
                timer = null;
            }
            if(null != timerTask) {
                timerTask.cancel();
                timerTask = null;
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        waveUtil.stop();
//        unregisterReceiver(LineDataReceiver);
    }

    public static String bytesToAscii(byte[] bytes, int offset, int dateLen) {
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dateLen <= 0)) {
            return null;
        }
        if ((offset >= bytes.length) || (bytes.length - offset < dateLen)) {
            return null;
        }

        String asciiStr = null;
        byte[] data = new byte[dateLen];
        System.arraycopy(bytes, offset, data, 0, dateLen);
        try {
            asciiStr = new String(data, "ISO8859-1");
        } catch (UnsupportedEncodingException e) {

        }
        return asciiStr;
    }

    public static String bytesToAscii(byte[] bytes, int dateLen) {
        return bytesToAscii(bytes, 0, dateLen);
    }
    public static String bytesToAscii(byte[] bytes) {
        return bytesToAscii(bytes, 0, bytes.length);
    }

    public static char byteAsciiToChar(int ascii) {
        char ch = (char) ascii;
        return ch;
    }

}
