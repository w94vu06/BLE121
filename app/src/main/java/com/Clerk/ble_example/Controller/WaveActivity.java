package com.Clerk.ble_example.Controller;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Clerk.ble_example.Module.Service.BluetoothLeService;
import com.Clerk.ble_example.R;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
        String hex = null;
        int LineData = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Intent post = new Intent(WaveActivity.this, ResultActivity.class);
            post.setClass(WaveActivity.this, ResultActivity.class);

            //接收來自藍芽傳回的資料
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Intent it = new Intent();
                it.setClass(WaveActivity.this, ResultActivity.class);
                waveUtil.showWaveData(LineData);
                btn_ms.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new CountDownTimer(3000, 1000) {
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
                            }
                        }.start();
                    }
                });
            }
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                byte[] getByteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                String ascii = BluetoothLeService.byteArrayToHexStr(getByteData);

                String[] tokens = ascii.split("0A|0D");
                for (String token : tokens) {
                    hex = BluetoothLeService.hexToAscii(token);
                }
                try {
                    LineData = Integer.parseInt(hex);
                } catch (NumberFormatException e) {
                    return ;
                }
                tv5.setText(hex);
                Log.d("HeartBeat", "心率: "+hex);

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
            timer.schedule(timerTask,500,999999999);
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

}
