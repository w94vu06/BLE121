package com.noahliu.ble_example.Controller;

import com.noahliu.ble_example.Module.Enitiy.ScannedData;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noahliu.ble_example.Module.Service.BluetoothLeService;
import com.noahliu.ble_example.R;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WaveActivity extends AppCompatActivity {

    private final static String TAG = "CommunicationWithBT";

    private WaveUtil waveUtil1;
    private WaveView wave_view1;

    private TextView tv5;
    float dad;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_item);

        waveUtil1 = new WaveUtil();
        wave_view1 = findViewById(R.id.wave_view1);
        waveUtil1.showWaveData(wave_view1);

        tv5 = findViewById(R.id.textView5);
        tv5.setText("---");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);//從服務中接受(收)數據
        registerReceiver(mGattUpdateReceiver, intentFilter);
    }
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            /**接收來自藍芽傳回的資料*/
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] getByteData = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                StringBuilder stringBuilder = new StringBuilder(getByteData.length);
                for (byte byteChar : getByteData)
                    stringBuilder.append(String.format("%02X ", byteChar));
                tv5.setText(BluetoothLeService.bytesToAscii(getByteData));
                //Log.d(TAG, ""+BluetoothLeService.byteArrayToFloat(getByteData));
                dad = BluetoothLeService.byteArrayToFloat(getByteData);
            }
        }
    };
    public class WaveUtil {
        private Timer timer;
        private TimerTask timerTask;
        public void showWaveData(WaveView waveView){

            Log.d(TAG, "showWaveData: "+dad);
            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run(){

                    waveView.showLine(dad);
                }
            };
            //500表示调用schedule方法后等待500ms后调用run方法，50表示以后调用run方法的时间间隔
            timer.schedule(timerTask,100,50);
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
        waveUtil1.stop();
    }

    public static float byteArrayToFloat (byte[] bytes) {
        int intBits =
                bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
        return Float.intBitsToFloat(intBits);
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
