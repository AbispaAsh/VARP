package com.example.iotproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.IOException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightEventListener;
    private TextView textView;
    private TextView connectedView;
    private float maxValue;
    private int currentLower=0;
    private int currentUpper=20;

    private int prevValue;
    private ConstraintLayout root;
//    private Button btnScan;
    private ImageButton btnScan;
    private Button btnDscnct;
    public String postUrl="";
    public String postBody="{\n" +
            "    \"name\": \"morpheus\",\n" +
            "    \"job\": \"leader\"\n" +
            "}";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root);
        textView = findViewById(R.id.textView);
        connectedView = findViewById(R.id.connectedView);
//        btnScan = findViewById(R.id.button);
        btnScan = findViewById(R.id.imageButton2);

        btnDscnct = findViewById(R.id.buttonD);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        btnDscnct.setEnabled(false);
        btnScan.setOnClickListener(v->{scanCode();});
        btnDscnct.setOnClickListener(v->{disconnect();});

        if (lightSensor == null) {
            Toast.makeText(this, "The device has no light sensor !", Toast.LENGTH_SHORT).show();
            finish();
        }

        // max value for light sensor
        maxValue = lightSensor.getMaximumRange();
        lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];
//                getSupportActionBar().setTitle("Luminosity : " + value + " lx");
                textView.setText("Luminosity : " + value + " lx");
                // between 0 and 255
//                int newValue = (int) (255f * value / maxValue);
//                root.setBackgroundColor(Color.rgb(newValue, newValue, newValue));

                if(postUrl.length() != 0) {
                    if ((int) value < currentLower || (int) value >currentUpper){
                        int brightness = 0;
                        if((int) value > 1000) {
                            brightness = 100;
                            currentLower = 1000;
                            currentUpper = 40000;
                        } else if ((int) value > 500) {
                            brightness = 85;
                            currentLower = 500;
                            currentUpper = 1000;
                        } else if ((int) value > 350) {
                            brightness = 65;
                            currentLower = 350;
                            currentUpper = 500;
                        } else if ((int) value > 50) {
                            brightness = 45;
                            currentLower = 50;
                            currentUpper = 350;
                        } else if ((int) value > 20) {
                            brightness = 20;
                            currentLower = 20;
                            currentUpper = 50;
                        } else {
                            brightness = 0;
                            currentLower = 0;
                            currentUpper = 20;
                        }
                        postBody = "{\n" +
                                "    \"luminosity\": \"" + brightness + "\"" +
                                "}";
                        try {
                            requestRun(postUrl, postBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
//                    if (Math.abs(prevValue - (int) value) > 1000) {
//                        prevValue = (int) value;
//                        int brightness = prevValue / 400;
//                        postBody = "{\n" +
//                                "    \"luminosity\": \"" + brightness + "\"" +
//                                "}";
//                        try {
//                            requestRun(postUrl, postBody);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }

    private void disconnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("Result");
        try{
            String cnctURL = postUrl + "disconnect";
            requestConnect(cnctURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(postUrl);
        builder.setMessage("Disconnected");
        postUrl = "";
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        }).show();
        btnDscnct.setEnabled(false);
        btnDscnct.setVisibility(View.INVISIBLE);
    }
    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            postUrl = result.getContents();
            try {
                requestTest();
            } catch (IOException e) {
                e.printStackTrace();
            }




        }
    });

    void invalidQR() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Invalid QR");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        }).show();
    }
    void requestTest() throws IOException {
        OkHttpClient client = new OkHttpClient();
        try {
            new URL(postUrl).toURI();
        } catch (Exception e) {
            invalidQR();
            return;
        }

        Request request = new Request.Builder()
                .url(postUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                System.out.println("what");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Handle UI here
                        invalidQR();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                System.out.println("Yes");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Handle UI here
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//            builder.setTitle("Result");
                        try{
                            String cnctURL = postUrl + "connect";
                            requestConnect(cnctURL);
                            btnDscnct.setEnabled(true);
                            btnDscnct.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(postUrl);
                        String text = "Connected to : " + postUrl.substring(postUrl.indexOf('/', postUrl.indexOf('/') + 1) + 1, postUrl.lastIndexOf(':'));
                        builder.setMessage(text);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                dialogInterface.dismiss();
                            }
                        }).show();
                    }
                });

            }
        });
    }
    void requestConnect(String cnctUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(cnctUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                System.out.println(cnctUrl);
                System.out.println("what");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                System.out.println("Yes");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Handle UI here
                        if(cnctUrl.endsWith("disconnect")){
                            connectedView.setText("Scan to Connect");
                        } else {
                            String text = postUrl.substring(postUrl.indexOf('/', postUrl.indexOf('/') + 1) + 1, postUrl.lastIndexOf(':'));
                            connectedView.setText(text);
                        }
                    }
                });

            }
        });
    }
    void requestRun(String postUrl,String postBody) throws IOException {
        //System.out.println("HERE~~");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(postBody,JSON);
        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                System.out.println("whay");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("TAG",response.body().string());
                System.out.println("OKay");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightEventListener);
    }
}