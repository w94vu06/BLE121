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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.Clerk.ble_example.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                makeCSV();
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
            /** 檔名 */
            String date = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault()).format(System.currentTimeMillis());
            String fileName = "[" + date + "]revlis.csv";
            String[] title = {"Lead2"};
            StringBuffer csvText = new StringBuffer();
            for (int i = 0; i < title.length; i++) {
                csvText.append(title[i] + ",");
            }
            /** 內容 */
            for (int i = 0; i < list.size(); i++) {
                csvText.append("\n" + list.get(i));
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
                    FileUpload.run(fileLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }//makeCSV
    public static class FileUpload {
        private static final MediaType MEDIA_TYPE_CSV = MediaType.parse("text/csv");
        private static final OkHttpClient client = new OkHttpClient();

        public static void run(File f) throws Exception {
            String date = new SimpleDateFormat("yyyyMMddHHmmss_888888",
                    Locale.getDefault()).format(System.currentTimeMillis());
            String fileName = "[" + date + "]";
            final File file = f;
            new Thread(){
                @Override
                public void run(){
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("title", "Square Logo")
                            .addFormDataPart("file", fileName+".csv",
                                    RequestBody.create(MEDIA_TYPE_CSV, file))
                            .build();
                    Request request = new Request.Builder()
                            .url("http://192.168.2.210:8090")
                            .post(requestBody)
                            .build();
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                        Log.d("return", "run: "+ response.body().string());
                        //System.out.println(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

    }
}
