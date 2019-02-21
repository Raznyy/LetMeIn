package com.dawidrazny.letmein;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    BluetoothAdapter mBluetoothAdapter;
    //Button btnONOFF = (Button) findViewById(R.id.bluetoothButton);
    //Button btnEnableDisableDiscoverability = (Button) findViewById(R.id.enableDiscoverabilityButton);

    BluetoothConnectionService mBluetoothConnection;

    Button btnStartConnection;
    Button btnSend;
    EditText etSend;

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothDevice mBTDevice;

    public static final String TAG = "LMI";

    public ArrayList<BluetoothDevice> mBTDevices= new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mBrodcastRecevier1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "OnRecive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "OnRecive: STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "OnRecive: STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "OnRecive: STATE_ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBrodcastRecevier2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "OnRecive: SCAN_MODE_CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "OnRecive: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "OnRecive: SCAN_MODE_NONE");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "OnRecive: STATE_CONNECTED");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "OnRecive: STATE_CONNECTING");
                        break;
                }
            }
        }
    };
    private final BroadcastReceiver mBrodcastRecevier3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onRecive: Action FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onRecive:" + device.getName() + ":" + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter( mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBrodcastRecevier4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onRecive: Action FOUND");

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if( mDevice.getBondState() == BluetoothDevice.BOND_BONDED )
                {
                    Log.d(TAG, "onRecive: BOND_BONDED");
                    mBTDevice = mDevice;
                }
                if ( mDevice.getBondState() == BluetoothDevice.BOND_BONDING )
                {
                    Log.d(TAG, "onRecive: BOND_BONDING");
                }
                if( mDevice.getBondState() == BluetoothDevice.BOND_NONE )
                {
                    Log.d(TAG, "onRecive: BOND_NONE");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "called");
        super.onDestroy();
        unregisterReceiver(mBrodcastRecevier1);
        unregisterReceiver(mBrodcastRecevier2);
        unregisterReceiver(mBrodcastRecevier3);
        unregisterReceiver(mBrodcastRecevier4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button bluetoothButton = (Button) findViewById(R.id.bluetoothButton);
        Button enableDiscoverabilityButton = (Button) findViewById(R.id.enableDiscoverabilityButton);
        Button btnDiscover = (Button) findViewById(R.id.btnDiscover);

        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        btnStartConnection = (Button) findViewById(R.id.btnStartConnection);
        btnSend = (Button) findViewById(R.id.sendData);
        etSend = (EditText) findViewById(R.id.editText);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBrodcastRecevier4, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBT();
            }
        });

        enableDiscoverabilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnEnableDisableDiscoverability(v);
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDiscover(v);
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });

    }

    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    public void startBTConnection( BluetoothDevice device, UUID uuid)
    {
        Log.d(TAG, "Starting BT Connection");

        mBluetoothConnection.startClient(device,uuid);
    }


    public void enableDisableBT()
    {
        if( mBluetoothAdapter == null)
        {
            Log.d(TAG, "YES");
        }
        if ( !mBluetoothAdapter.isEnabled() )
        {
            Log.d(TAG, "enabling BT");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBrodcastRecevier1, BTIntent);
        }
        if( mBluetoothAdapter.isEnabled() )
        {
            Log.d(TAG, "turning off BT");
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBrodcastRecevier1, BTIntent);
        }
    }

    public void btnEnableDisableDiscoverability( View view)
    {
        Log.d(TAG, "DISCOVERABLE for 300 sec");

        Intent discoverableIntent = new Intent((BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE));
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

        startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBrodcastRecevier2,  intentFilter);
    }

    public  void btnDiscover(View view)
    {
        Log.d(TAG, "YES");

        if(mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "cancelDiscovery");

            //check permissions in Manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent  = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBrodcastRecevier3,  discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering())
        {
            //check permissions in Manifest
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            Log.d(TAG, "STARTED");
            IntentFilter discoverDevicesIntent  = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBrodcastRecevier3,  discoverDevicesIntent);
            Log.d(TAG, "jestem");
        }
    }


    /**
     * This method is required for all devices running API23+
     * Android must programmatically check the permissions for bluetooth. Putting the proper permissions
     * in the manifest is not enough.
     *
     * NOTE: This will only execute on versions > LOLLIPOP because it is not needed otherwise.
     */
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int i, long id)
    {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onRecive: ITEM CLICKED");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "name: " + " " + deviceName);
        Log.d(TAG, "adress: " + " " + deviceAddress);

        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
        {
            Log.d(TAG, "Trying to pair  with: " + " " + deviceName);
            mBTDevices.get(i).createBond();

            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }
}
