package com.example.paulofrazao.bluetoothdemo;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothSocket;


import android.content.Intent;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LocationListener{

    // input/output streams
    private InputStream in;
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

    // variable for Location API speed calculations
    private FusedLocationProviderClient mFusedProviderClient;
    private LocationRequest mLocationRequest;
    private LocationManager lMan;
    private Location netLoc;
    private Location GPSLoc;
    private Location lastLoc;
    private Location newLoc;


    // bluetooth connection elements
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket btSocket;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SECONDS_IN_HOUR = 3600;
    static final float METERS_IN_MILE = 1609.344f;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // sets splash screen and then home screen

        // swapped the two!
        setTheme(R.style.AppTheme);
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
       // llMetrics.getLayoutParams().height = (int) (height * 0.45);
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

        // lists the connected device
       // mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
     //   Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        /* Here, we check the paired devices against the MAC address of the Arduino to display
           the device name in the corresponding TextView. In the final product, should have way to
           differentiate luggage from other devices. */


        // sets button listeners
        btAssistiveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // begins the thread to commence information transferring process
                // bt.start();

                if (btAssistiveMode.isChecked())
                    Toast.makeText(getApplicationContext(), "Assistive Mode is ON!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Assistive Mode is OFF!", Toast.LENGTH_SHORT).show();
            }
        });

        btStairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btEscalator.isChecked()) btEscalator.setChecked(false);
                if (btSidewalk.isChecked()) btSidewalk.setChecked(false);

                if (btStairs.isChecked())
                    Toast.makeText(getApplicationContext(), "Ready for stairs!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Not ready for stairs :(", Toast.LENGTH_SHORT).show();
            }
        });

        btEscalator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btStairs.isChecked()) btStairs.setChecked(false);
                if (btSidewalk.isChecked()) btSidewalk.setChecked(false);

                if (btEscalator.isChecked())
                    Toast.makeText(getApplicationContext(), "Ready for an escalator!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Not ready for an escalator :(", Toast.LENGTH_SHORT).show();
            }
        });

        btSidewalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btStairs.isChecked()) btStairs.setChecked(false);
                if (btEscalator.isChecked()) btEscalator.setChecked(false);

                if (btSidewalk.isChecked())
                    Toast.makeText(getApplicationContext(), "Ready for a moving sidewalk!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Not ready for a moving sidewalk :(", Toast.LENGTH_SHORT).show();
            }
        });

        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Will display settings!", Toast.LENGTH_SHORT).show();
            }
        });

        ivLuggage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Nice!", Toast.LENGTH_SHORT).show();
                dispatchTakePictureIntent();
            }
        });

        // handles Location API setup
        lMan = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            mFusedProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Log.d("TAG", "onCreate: NOPE");
        }


        // ensures that fine location can be accessed
        if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "onCreate: Was not able to get permissions :(");
        }

        if(lMan != null) {
            // lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
            lMan.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            GPSLoc = lMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d("Maybe?", "onCreate: " + GPSLoc);
        }

        if (GPSLoc == null) {
            Log.d("ODD", "onCreate: Shouldn't happen");
        }
        else {
            lastLoc = new Location("");
            lastLoc.setLatitude(GPSLoc.getLatitude());
            lastLoc.setLongitude(GPSLoc.getLongitude());

            newLoc = new Location("");
            newLoc.setLatitude(GPSLoc.getLatitude());
            newLoc.setLongitude(GPSLoc.getLongitude());
        }

        Log.d("NOTE", "onCreate: " + lastLoc.getLatitude() + " " + lastLoc.getLongitude());


        /*
        // handles the bluetooth setup
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        // if bluetooth is not enabled, enables it
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3); }

        // checks to make sure device is found
        BluetoothDevice dev = (BluetoothDevice) devices.toArray()[0];
        Log.d("tag", "onCreate: " + dev.getName());

        // establishes connection
        BluetoothConnection bt = new BluetoothConnection(dev);
        if (bt == null) Log.d("TAG", "onCreate: NTUS"); */

        // begins the speed calculation
        beginSpeedCalculation();
    }

    // callback function used by the FusedLocationProviderClient API
    // each time it can, updates the locates -> every x seconds, polled by the thread and used to update speed
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            Location location = null;
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                location = locationList.get(locationList.size() - 1);
                Log.d("TEMP", "Location: " + location.getLatitude() + " " + location.getLongitude());
            }

            Log.d("HERE", "onLocationResult: " + newLoc);
            // case where app is just launching

            lastLoc = newLoc;
            newLoc = location;
        }
    };

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
    public void onLocationChanged(Location loc) {
     //   Log.d("TAG", "onLocationChanged: " + loc);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
     //   Log.d("TAG", "onStatusChanged: " +status);
    }

    private void beginSpeedCalculation() {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
     //   final Random rnd = new Random();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
              //      float nxt = rnd.nextFloat();
                    // float speed = 20 * nxt;
                    float speed = getRecentSpeed();
                    float roundedSpeed = speed * 1000;

                    // float roundedSpeed = speed * 1000;
                    roundedSpeed = (float) ((int) roundedSpeed);
                    roundedSpeed /= 1000;
                    final float tempRoundedSpeed = roundedSpeed;

                    runningSum += speed;
                    runningCount++;

                    float avg = ((float) (int) (runningSum / runningCount * 1000)) / 1000;
                    final float tempAvg = avg;

                    // updates UI
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            tvSpeedData.setText(Float.toString(tempRoundedSpeed));
                            tvAvgSpeedData.setText(Float.toString(tempAvg));

                        }
                    });
                } catch (Exception e) {
                    Log.d("oh no", "run: oh no!" + e);
                }

            }
        }, 0, 300, TimeUnit.MILLISECONDS);
    }

    float getRecentSpeed() {
        // calculates distance b/w new point and old point
        float[] res = new float[1];
        Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), this.newLoc.getLatitude(), this.newLoc.getLongitude(), res);
        Log.d("DIST", "getRecentSpeed: " + res[0]);

        // converts meters / second to miles / hour
        float speedMetersPerHour = res[0] * SECONDS_IN_HOUR;
        float speedMilesPerHour = speedMetersPerHour * (1/METERS_IN_MILE);

        Log.d("BAM", "Recent Speed: " + speedMilesPerHour);

        return speedMilesPerHour;

    }

    @Override
    protected void onDestroy() {
        //   unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // handles the picture-taking process
    /* Need to figure out how to make the picture persist */
    private void dispatchTakePictureIntent() {
        Intent takePicIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicIntent, REQUEST_IMAGE_CAPTURE);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivLuggage.setImageBitmap(imageBitmap);
        }
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
                Log.d("TAG", "BluetoothConnection: kirsten");
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        public void run() {
            // cancels discovery to save resources
            mBluetoothAdapter.cancelDiscovery();
            Log.d("TAG", "run: HEYO");

            try {
                // tries to connect to the Arduino
                mmSocket.connect();
            } catch (Exception e) {
                Log.d("ERR", "run: Socket couldn't connect");
            }

            Log.d("TAG", "run: " + mmSocket.isConnected());
            btSocket = mmSocket;

            // establishes output stream
            try {
                out = btSocket.getOutputStream();}
            catch (Exception e) {
                Log.d("tag", "onCreate: couldn't out");
            }

            // establishes input stream
            try {
                in = btSocket.getInputStream();}
            catch (Exception e) {
                Log.d("tag", "onCreate: couldn't in");
            }

            // tests writing words
            String[] words = {"hi", "there", "my", "dude"};
            for (int i = 0; i < words.length; i++) {
                this.write(words[i].getBytes());
            }
        }

        public void write(byte[] buffer) {
        //    while (true) {
                try {
                    //write the data to socket stream
                    ByteBuffer b = ByteBuffer.allocate(8);
                    b.putDouble(5);
                    byte[] arr = b.array();
                    for (int i = 0; i < 8; i++) Log.d("TAG", "write: " + arr[i]);
                    out.write(arr);
                    Log.d("TAG", "write: " + buffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("TAG", "write: OHNO");
                }
        //    }
        }

        // stops the thread
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}
