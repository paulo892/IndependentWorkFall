package com.example.paulofrazao.bluetoothdemo;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
/*
public class MainActivity extends AppCompatActivity {

    TextView tv1;
    BluetoothAdapter ba;
    Set<BluetoothDevice> paired;
    ListView deviceList;
    private final static int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);


        try {
            ba = establishConnection();
        } catch (Exception e) {
            System.out.println("Error thrown on connection establishment.");
            return;
        }

        paired = ba.getBondedDevices();
        ArrayList list = new ArrayList();

        if (paired.size() > 0) {
            for (BluetoothDevice bt : paired) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

                // Make an intent to start next activity.
                Intent i = new Intent(MainActivity.this, ledControl.class);

                //Change the activity.
                i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
                startActivity(i);
        }
    };


        // makes device discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(discoverableIntent, 2);

        // begins connection process

        boolean worked = ba.startDiscovery();

        // discovers devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        BluetoothServerSocket socket = null;

        // connects
        UUID id = UUID.fromString("7f3176b3-16e6-48b1-a971-26520708d74e");
        try {
            socket = ba.listenUsingRfcommWithServiceRecord("Bluetooth Test Server", id);
        } catch (Exception e){
            Log.d("WHOOPS", "WHOOOOPS");

        }

        BluetoothSocket sock;
        try {
            sock = socket.accept();
        } catch (Exception e){
            Log.d("YIKES", "onCreate: YIKES");
        }



        Log.d("TAG", worked + "");

        // opens server connection
        AcceptThread at = new AcceptThread();
        at.run();
}

private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                UUID id = UUID.fromString("7f3176b3-16e6-48b1-a971-26520708d74e");
                tmp = ba.listenUsingRfcommWithServiceRecord("Bluetooth Test Server", id);
            } catch (IOException e) {
                Log.e("IO", "Socket's listen() method failed", e);
            }

            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;

            Log.d("TAG", "Starting run");
            // NEEDS TO BE MADE WHILE TRUE
            int i = 0;
            Log.d("", "AHH");
            while (i < 1000) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e("IO2", "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    manageMyConnectedSocket(socket);
                    try {
                        mmServerSocket.close();

                    } catch (Exception e) {
                        // temp solution
                    }
                    break;
                }

                Log.d("", i + "");
                i++;
            }
        }

        public void manageMyConnectedSocket(BluetoothSocket sock) {
            Log.d("SOCKET PROB", "REACHED PART WHERE HAVE TO HANDLE CONNECTED SOCKET");
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e("IO3", "Could not close connect socket", e);
            }
        }
}

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // gets BT device and its info from intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAccess = device.getAddress();
            }
        }
    };

    BluetoothAdapter establishConnection() throws Exception {
        BluetoothAdapter mBA = BluetoothAdapter.getDefaultAdapter();

        if (mBA == null) throw new Exception("Device doesn't support BT.");

        // enables BT
        if (!mBA.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        return mBA;

    }

    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        ba.cancelDiscovery();
    }
} */

public class temp extends ListActivity {
    TextView tv1;
    ListView lv1;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private InputStream in;
    private OutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      //  tv1 = findViewById(R.id.tv1);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        List<String> s = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices)
            s.add(bt.getName());

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s));


        BluetoothDevice dev = (BluetoothDevice) pairedDevices.toArray()[0];
        Log.d("tag", "onCreate: " + dev.getName());

        BluetoothSocket socket = null;

        try {

            Method m= dev.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket=    (BluetoothSocket) m.invoke(dev, 1);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (socket == null) return;

        try { socket.connect();}
        catch (Exception e) {
            Log.d("tag", "onCreate: could not connect"); }

/*
        ParcelUuid[] supportedUuids = dev.getUuids();
        try { //socket = dev.createRfcommSocketToServiceRecord(supportedUuids[0].getUuid());
            socket = dev.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
             }
        catch (Exception e) { Log.d("tag", "onCreate: couldn't create BT socket"); }

        try {socket.connect(); }
        catch (Exception e) {
            Log.d("tag", "onCreate: couldn't connect");
            try {
                Log.e("","trying fallback...");

                socket =(BluetoothSocket) dev.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(dev,1);
                socket.connect();

                Log.e("","Connected");
            }
            catch (Exception e2) {
                Log.e("", "Couldn't establish Bluetooth connection!");
            }
        }

        try {
            out = socket.getOutputStream();}
        catch (Exception e) {
            Log.d("tag", "onCreate: couldn't out");
        }

        try {
            in = socket.getInputStream();}
        catch (Exception e) {
            Log.d("tag", "onCreate: couldn't in");
        }

        Log.d("tag", "onCreate: REACHED THE END"); */



        /*
        mBluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); */
    }

    @Override
    protected void onDestroy() {
        //   unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
                Log.i("BT", device.getName() + "\n" + device.getAddress());
                lv1.setAdapter(new ArrayAdapter<String>(context,
                        android.R.layout.simple_list_item_1, mDeviceList));
            }
        }
    };
}
