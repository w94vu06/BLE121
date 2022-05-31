package com.Clerk.ble_example.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Clerk.ble_example.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {
    private TextView tv_result;
    private ArrayList<String> list;
    private Button btn_get;
    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_result);
        tv_result = findViewById(R.id.tv_result);
        btn_get = findViewById(R.id.btn_get);
        btn_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptData();
//                makeCSV();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void acceptData() {
        list = (getIntent().getStringArrayListExtra("resultData"));
        tv_result.setText(""+list);
        Log.d("s1", "onCreate: " + list);
    }

    private void makeCSV() {
        new Thread(()->{
            String date = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(System.currentTimeMillis());
            String fileName = "[" + date + "]revlis.csv";
            String[] title = {"heart"};
            StringBuffer csvText = new StringBuffer();
            for (int i = 0; i < title.length; i++) {
                csvText.append(title[i] + ",");
            }
            for (int i = 0; i < list.size(); i++) {
                csvText.append(list);
            }
            Log.d("CSV", "makeCSV: "+csvText);
            runOnUiThread(()->{
                try {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    builder.detectFileUriExposure();
                    FileOutputStream out = openFileOutput(fileName,Context.MODE_PRIVATE);
                    out.write((csvText.toString().getBytes()));
                    out.close();
                    File fileLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
                    FileOutputStream fos = new FileOutputStream(fileLocation);
                    fos.write(csvText.toString().getBytes());
                    Uri path = Uri.fromFile(fileLocation);
                    Intent fileIntent = new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent,"輸出檔案"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();

    }

}
