package com.example.paulofrazao.bluetoothdemo;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Security;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LocationListener{

    // frequency of transmission
    private final int POLLING_FREQ_MILLISECONDS = 1000;

    // helpful constants
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SECONDS_IN_HOUR = 3600;
    static final float METERS_IN_MILE = 1609.344f;

    // input/output streams
    private OutputStream out;

    // components
    private TextView tvAssistiveMode;
    private ToggleButton btAssistiveMode;
    private TextView tvConnectedDevice;
    private TextView tvConnectedDeviceName;
    private ImageView ivLuggage;
    private TextView tvMetrics;
    private TextView tvSpeedHeader;
    private TextView tvSpeedData;
    private TextView tvAvgSpeedHeader;
    private TextView tvAvgSpeedData;
    private TextView tvStairs;
    private TextView tvEscalator;
    private TextView tvSidewalk;
    private ToggleButton btStairs;
    private ToggleButton btEscalator;
    private ToggleButton btSidewalk;
    private Button btSettings;
    private LinearLayout llMetrics;

    // variables for general speed calculations
    private double runningSum = 0.0;
    private int runningCount = 0;

    // variables for Location API speed calculations
    private LocationRequest mLocationRequest;
    private Location lastLoc;
    private Location newLoc;

    // holds the current speed of the bag
    private float curSpeed;

    // executor services to execute speed calcs and transmissions
    private ScheduledExecutorService executorService;
    private ScheduledExecutorService executorService2;

    // indicator of whether on moving sidewalk
    private boolean onMovingSidewalk = false;

    // builder for proximity alert dialog
    private AlertDialog.Builder builder;

    // bluetooth connection elements
    private BluetoothDevice dev;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket btSocket;

    // temporary array used for speed generation
    private float[] nums = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets splash screen and then home screen

        // swapped the two!
        setTheme(R.style.AppTheme);
     /*   try {TimeUnit.SECONDS.sleep(2);}
            catch (Exception e) {
                Log.d("TAG", "onCreate: " + e);} */
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme_Launcher);

        setContentView(R.layout.activity_main);

        // initializes components
        tvAssistiveMode = findViewById(R.id.tvAssistiveMode);
        btAssistiveMode = findViewById(R.id.btAssistiveMode);
        tvConnectedDevice = findViewById(R.id.tvConnectedDevice);
        tvConnectedDeviceName = findViewById(R.id.tvConnectedDeviceName);
        ivLuggage = findViewById(R.id.ivLuggage);
        tvMetrics = findViewById(R.id.tvMetrics);
        tvSpeedData = findViewById(R.id.tvSpeedData);
        tvSpeedHeader = findViewById(R.id.tvSpeedHeader);
        tvAvgSpeedData = findViewById(R.id.tvAvgSpeedData);
        tvAvgSpeedHeader = findViewById(R.id.tvAvgSpeedHeader);
        tvStairs = findViewById(R.id.tvStairs);
        tvEscalator = findViewById(R.id.tvEscalator);
        tvSidewalk = findViewById(R.id.tvSidewalk);
        btStairs = findViewById(R.id.btStairs);
        btEscalator = findViewById(R.id.btEscalator);
        btSidewalk = findViewById(R.id.btSidewalk);
        btSettings = findViewById(R.id.btSettings);
        llMetrics = findViewById(R.id.llMetrics);

        // dynamically scales components
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        tvAssistiveMode.setHeight((int)(height*0.075));
        btAssistiveMode.setHeight((int)(height*0.075));
        tvConnectedDevice.setHeight((int)(height*0.075));
        tvConnectedDeviceName.setHeight((int)(height*0.075));
        tvMetrics.setHeight((int)(height * 0.05));
        tvSpeedHeader.setHeight((int)(height * 0.075));
        tvSpeedData.setHeight((int)(height * 0.1));
        tvAvgSpeedHeader.setHeight((int)(height * 0.075));
        tvAvgSpeedData.setHeight((int)(height * 0.1));
        tvStairs.setHeight((int)(height*0.05));
        tvEscalator.setHeight((int)(height*0.05));
        tvSidewalk.setHeight((int)(height*0.05));
        btStairs.setHeight((int)(height*0.05));
        btEscalator.setHeight((int)(height*0.05));
        btSidewalk.setHeight((int)(height*0.05));
        btSettings.setHeight((int)(height*0.075));
        // llMetrics.getLayoutParams().height = (int) (height * 0.45);

        // handles the bluetooth setup
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Log.d("DEV", devices.toString());

        // if bluetooth is not enabled, enables it
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3); }

        // checks to make sure device is found
        dev = (BluetoothDevice) devices.toArray()[0];
        Log.d("tag", "onCreate: " + dev.getName());

        // establishes connection
        final BluetoothConnection bt = new BluetoothConnection(dev);

        // sets text to bag's name
        tvConnectedDeviceName.setText(dev.getName());

        // sets button listeners
        btAssistiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btAssistiveMode.isChecked()) {
                    bt.start();
                    Toast.makeText(getApplicationContext(), "Assistive Mode is ON!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Assistive Mode is OFF!", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-3f).array());
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't stop reading input");
                    }
                    bt.cancel();
                }
            }
        });

        btStairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btEscalator.isChecked()) btEscalator.setChecked(false);
                if (btSidewalk.isChecked()) {
                    btSidewalk.setChecked(false);
                    onMovingSidewalk = false;
                }

                if (btStairs.isChecked() && btAssistiveMode.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Ready for stairs!", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-1f).array());
                        Log.d("WRR", "onClick: wrote stop" + out);
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't stop bag");
                    }
                }
                else if (!btStairs.isChecked() && btAssistiveMode.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Not ready for stairs :(", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-2f).array());
                        Log.d("WRR", "onClick: wrote start" + out);
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't start bag");
                    }
                }
            }
        });

        btEscalator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btStairs.isChecked()) btStairs.setChecked(false);
                if (btSidewalk.isChecked()) {
                    btSidewalk.setChecked(false);
                    onMovingSidewalk = false;
                }

                if (btEscalator.isChecked() && btAssistiveMode.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Ready for an escalator!", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-1f).array());
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't stop bag");
                    }
                }
                else if (!btEscalator.isChecked() && btAssistiveMode.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Not ready for an escalator :(", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-2f).array());
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't start bag");
                    }
                }
            }
        });

        btSidewalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btStairs.isChecked()) btStairs.setChecked(false);
                if (btEscalator.isChecked()) btEscalator.setChecked(false);

                if (btSidewalk.isChecked()) {
                    Toast.makeText(getApplicationContext(), "Ready for a moving sidewalk!", Toast.LENGTH_SHORT).show();
                    onMovingSidewalk = true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Not ready for a moving sidewalk :(", Toast.LENGTH_SHORT).show();
                    onMovingSidewalk = false;
                }

            }
        });

        // updates photo
        SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
        String encoded = prefs.getString("luggage_image", "bananas");

        if (!encoded.equals("bananas")) {
            byte[] image = Base64.decode(encoded.getBytes(), Base64.DEFAULT);
            ivLuggage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
        }

        ivLuggage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Nice!", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
            }
        });

        // initializes the proximity sensor's alert button
        builder = new AlertDialog.Builder(MainActivity.this);

        builder.setCancelable(false);
        builder.setTitle("You're getting too far from the bag!");
        builder.setMessage("Turning off Assistive Mode.");

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (btAssistiveMode.isChecked()) {
                    btAssistiveMode.setChecked(false);
                    Toast.makeText(getApplicationContext(), "Assistive Mode is OFF!", Toast.LENGTH_SHORT).show();
                    try {
                        out.write(ByteBuffer.allocate(4).putFloat(-3f).array());
                    } catch (IOException e) {
                        Log.d("ERR", "run: didn't stop reading input");
                    }
                    bt.cancel();
                }
            }
        });

        // begins location updates
        startLocationUpdates();

        // begins the speed calculation
        beginSpeedCalculation();
    }

    /*
    private final BroadcastReceiver rec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(act)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.d("RSSI", "RSSI: " + rssi);
            }
        }
    }; */

    // starts location updates
    public void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ_MILLISECONDS);
        mLocationRequest.setFastestInterval(POLLING_FREQ_MILLISECONDS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        try {
            // establishes location change callback function
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,
                    new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    Looper.myLooper());
        } catch (SecurityException e) {
            Log.d("TAG", "startLocationUpdates: Didn't start :(");
        }

    }

    // callback methods necessary for interface necessary for initial location tracking
    @Override
    public void onProviderEnabled(String what) {
     //   Log.d("TAG", "onProviderEnabled: " + true);
    }

    @Override
    public void onProviderDisabled(String what) {
     //   Log.d("TAG", "onProviderDisabled: " + true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //   Log.d("TAG", "onStatusChanged: " +status);
    }

    @Override
    public void onLocationChanged(Location loc) {
     Log.d("TAG", "onLocationChanged: " + loc + loc.getAccuracy());

     // updates the last location and new location for speed calculations
     if (lastLoc == null && newLoc == null) {lastLoc = loc; newLoc = loc;}
     else {lastLoc = newLoc; newLoc = loc;}
    }

    // starts speed calculations
    private void beginSpeedCalculation() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // testing of RSSI fidelity
                    Log.d("RSSI", "RSSI: " + dev.EXTRA_RSSI);

                    float speed = getRecentSpeed();
                    float roundedSpeed = speed * 1000;

                    roundedSpeed = (float) ((int) roundedSpeed);
                    roundedSpeed /= 1000;
                    final float tempRoundedSpeed = roundedSpeed;

                    // assumes that a moving sidewalk moves at 1.4 mph!
                    if (onMovingSidewalk) {
                        curSpeed = Math.max(tempRoundedSpeed - 1.4f, 0f);
                        runningSum += curSpeed;
                        Log.d("MOVS", "speed: " + curSpeed);
                    }
                    else {
                        curSpeed = tempRoundedSpeed;
                        runningSum += curSpeed;
                    }

                    runningCount++;

                    float avg = ((float) (int) (runningSum / runningCount * 1000)) / 1000;
                    final float tempAvg = avg;

                    // updates UI
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                          //  tvSpeedData.setText(Float.toString(tempRoundedSpeed));
                            tvAvgSpeedData.setText(Float.toString(tempAvg));

                        }
                    });
                } catch (Exception e) {
                    // Log.d("oh no", "run: oh no!" + e);
                }

            }
        }, 0, POLLING_FREQ_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    // gets the most recent speed (through calculations)
    float getRecentSpeed() {
        // calculates distance b/w new point and old point
        float[] res = new float[1];
        try {
            Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), this.newLoc.getLatitude(), this.newLoc.getLongitude(), res);
        } catch (Exception e) {
            Log.d("EXC", "getRecentSpeed: " + e);
        }

        // converts meters / second to miles / hour
        float speedMetersPerHour = res[0] * SECONDS_IN_HOUR;
        float speedMilesPerHour = speedMetersPerHour * (1/METERS_IN_MILE);

        return speedMilesPerHour;

    }

    @Override
    protected void onDestroy() {
        //   unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // handles the picture-taking process
    private void dispatchTakePictureIntent() {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // retrieves the photo
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

            // sets the photo
            ivLuggage.setImageBitmap(imageBitmap);

            // logs the photo for later use
            SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // note: must convert bitmap to Base64 String to store it
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] arr = stream.toByteArray();

            String encodedStr = Base64.encodeToString(arr, Base64.DEFAULT);

            editor.putString("luggage_image", encodedStr);
            editor.apply();
        }
    }

    public void launchSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);

        startActivity(intent);
    }

    private class BluetoothConnection extends Thread {
        private final BluetoothSocket mmSocket;
        byte[] buffer;

        // Unique UUID for this application, you may use different
        private final UUID MY_UUID = UUID
                .fromString("00001101-0000-1000-8000-00805f9b34fb");

        public BluetoothConnection(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d("TAG", "BluetoothConnection: ");
                e.printStackTrace();
            }

            // set socket equal to created socket
            mmSocket = tmp;
            Log.d("SOCK", "BluetoothConnection: " + mmSocket);
        }

        public void run() {
            // cancels discovery to save resources
            mBluetoothAdapter.cancelDiscovery();

            try {
                // tries to connect to the Arduino
                mmSocket.connect();
            } catch (Exception e) {
                Log.d("ERR", "run: Socket couldn't connect");
            }

            // weird necessary step
            btSocket = mmSocket;

            // establishes output stream
            try {
                out = mmSocket.getOutputStream();
                try {
                      /*  latch = new CountDownLatch(1);
                        latch.await(); */
                    out.write(ByteBuffer.allocate(4).putFloat(-4f).array());
                } catch (Exception e) {
                    Log.d("ERR", "run: didn't start reading input");
                }
             //   latch.countDown();
            }
            catch (Exception e) {
                Log.d("tag", "onCreate: couldn't out");
            }

            nums[0] = 2; nums[1] = 3; nums[2] = 4;

            // schedules writing thread
            executorService2 = Executors.newSingleThreadScheduledExecutor();
            executorService2.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        float word = nums[new Random().nextInt(nums.length)];
                        Log.d("SPD", "run: " + word);
                        out.write(ByteBuffer.allocate(4).putFloat(curSpeed).array());
                        for (int i = 0; i < 4; i++) {
                            Log.d(i + "", "run: " + ByteBuffer.allocate(4).putFloat(curSpeed).array()[i]);
                        }
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                tvSpeedData.setText(Float.toString(curSpeed));

                            }
                        });
                    } catch (Exception e) {
                        Log.d("oh no", "run: oh no!" + e);
                    }
                }
            },  0, 4000, TimeUnit.MILLISECONDS);
            Log.d("CUR", "run: ");
        }

        // stops the thread
        public void cancel() {
            try {
                mmSocket.close();
                if (executorService2 != null) executorService2.shutdownNow();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}
