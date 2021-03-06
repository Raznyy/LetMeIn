package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mconnectionStatus;
    private Button mConnectionButton;
    private LinearLayout mConnectionLayout;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    public String pinCode;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                String extraData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.e(TAG, extraData.equals("declined")+ " extra data " + extraData);
                String[] extraDataParsed = extraData.split("\n");

                Log.e(TAG, extraDataParsed[0]);
                if(extraDataParsed[0].equals("declined"))
                {
                    mconnectionStatus.setText("User not provided. Please enter PIN code to connect.");
                    requestPinCode(new alertDialogResponse() {
                                       @Override
                                       public void setPinCode(String pin)
                                       {
                                           for(int i = 0; i < mGattCharacteristics.size(); i ++)
                                           {
                                               final ArrayList characteristicData = mGattCharacteristics.get(i);
                                               for(int j = 0; j < characteristicData.size(); j ++)
                                               {
                                                   BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(i).get(j);
                                                   if( characteristic.getUuid().toString().equals(SampleGattAttributes.WRITE_CHARACTERISTICS))
                                                   {
                                                       mBluetoothLeService.writeCharacteristic(characteristic, "PIN_CHECK", pin);
                                                   }
                                               }
                                           }
                                       }});
                }
                else if ( extraDataParsed[0].equals("PINVERfalse") )
                {
                    Toast.makeText( context , "Sorry! Wrong PIN code.", Toast.LENGTH_SHORT).show();
                    ColorDrawable[] color = {new ColorDrawable(0xFDC01299), new ColorDrawable(Color.RED)};
                    TransitionDrawable trans = new TransitionDrawable(color);
                    mConnectionLayout.setBackground(trans);
                    trans.startTransition(500);
                    mBluetoothLeService.disconnect();
                    mconnectionStatus.setText("Wrong PIN code.");
                    mConnectionButton.setText("Reconnect.");
                    mConnectionButton.setEnabled(true);
                }
                else if ( extraDataParsed[0].equals("PINVERtrue") || extraDataParsed[0].equals("approved"))
                {
                    ColorDrawable[] color = {new ColorDrawable(0xFDC01299), new ColorDrawable(Color.GREEN)};
                    TransitionDrawable trans = new TransitionDrawable(color);
                    mConnectionLayout.setBackground(trans);
                    trans.startTransition(500);
                    mconnectionStatus.setText(" Welcome home :) Door's unlocked for 10 second.");
                    mConnectionButton.setEnabled(false);
                    new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            mConnectionButton.setText("Open for: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            mConnectionButton.setText("Doors closed!");
                            mBluetoothLeService.disconnect();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run()
                                {
                                    mConnectionButton.setEnabled(true);
                                    mConnectionButton.setText("Reopen the door.");
                                }
                            }, 1000);
                        }

                    }.start();
                }

                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void requestPinCode(final alertDialogResponse alertDialogResponse)
    {
         final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        LayoutInflater mLayoutInflater = this.getLayoutInflater();
        View pinEntryTextView = mLayoutInflater.inflate(R.layout.pin_alert, null);

        alertDialog.setTitle("Please provide PIN to enter.");

        final PinEntryEditText txtPinEntry = (PinEntryEditText) pinEntryTextView.findViewById(R.id.txt_pin_entry);
        alertDialog.setView(pinEntryTextView);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "ENTER", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(txtPinEntry.getPinCode().length()<4)
                {
                    Toast.makeText( getApplicationContext() , "Please enter pin code to proceed.", Toast.LENGTH_SHORT).show();
                }
                else if( txtPinEntry.getPinCode().length()==4)
                {
                    dialog.dismiss();
                }
                alertDialogResponse.setPinCode(txtPinEntry.getPinCode());
            }
        });

        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
        alertDialog.show();
    }

    interface alertDialogResponse
    {
        void setPinCode(String pin);
    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener()
    {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,int childPosition, long id)
        {
            if (mGattCharacteristics != null)
            {
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                {
                     // If there is an active notification on a characteristic, clear
                     // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null)
                    {
                        mBluetoothLeService.setCharacteristicNotification( mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification( characteristic, true);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification( characteristic, true);
                    mBluetoothLeService.writeCharacteristic(characteristic, "", "");
                }
                return true;
            }
            return false;
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);

        mConnectionButton = (Button)findViewById(R.id.unlockButton);
        mConnectionButton.setEnabled(false);
        mConnectionLayout = (LinearLayout)findViewById(R.id.connectionLayout);
        mconnectionStatus = (TextView) findViewById(R.id.connectionStatus);

        mConnectionButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                mBluetoothLeService.connect(mDeviceAddress);
                mConnectionButton.setText("Connecting...");
            }
        });

        getActionBar().setTitle("Connected to : " + mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null)
        {
            mDataField.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices)
        {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put( LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =  new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put( LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
