package com.dawidrazny.letmein;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService
{
    private static final String TAG = "BCTAG";

    private static final String appName = "MyApp";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private  AcceptThread mInsecureAcceptThread;

    private ConnectionThread mConnectThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private ConnectedThread mConnectedThread;

    public BluetoothConnectionService(Context context)
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        start();
    }

    private class AcceptThread extends  Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d("BCTAG", "Connected using" + MY_UUID_INSECURE);
            } catch (IOException e) {

            }

            mmServerSocket = tmp;
        }
        public void run()
        {
            Log.d("BCTAG", "run");

            BluetoothSocket socket = null;

            try
            {
                Log.d("BCTAG", "start");
                socket = mmServerSocket.accept();
                Log.d("BCTAG", "Succeded");

            }catch (IOException e) {
                Log.d("BCTAG", "FAILED");
            }

            if(socket != null) {
                connected(socket, mmDevice);
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
            }
        }
    }

    private class ConnectionThread extends Thread
    {
        private BluetoothSocket mmSocket;

        public ConnectionThread(BluetoothDevice device, UUID  uuid)
        {
            Log.d(TAG, "Started Connection");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run()
        {
            BluetoothSocket tmp = null;
            Log.d(TAG, "Run ConnevtThread");
            try{
                Log.d(TAG, "Trying to create Socket using uuid" + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch (IOException e) {
                Log.d(TAG, "Cannot create" + MY_UUID_INSECURE);
            }

            mmSocket = tmp;

            mBluetoothAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
                Log.d(TAG, "Connected");
            }catch (IOException e) {
                Log.d(TAG, "Socket closed");
                try{
                    mmSocket.close();
                    Log.d(TAG, "Closed");
                }catch (IOException e1) {
                    Log.d(TAG, "Unableto close" + e1.getMessage());
                }

                Log.d(TAG, "Could not connect to " + MY_UUID_INSECURE);
            }

            connected( mmSocket, mmDevice);
        }

        public void cancel() {
            Log.d(TAG, "cancel: Closing Socket");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Close of mmSocket failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start()
    {
        Log.d(TAG, "START");

        if( mConnectThread != null )
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if ( mInsecureAcceptThread == null )
        {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device,UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //initprogress dialog
        mProgressDialog = ProgressDialog.show(mContext,"Connecting Bluetooth"
                ,"Please Wait...",true);

        mConnectThread = new ConnectionThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            Log.d(TAG, "ConnectionThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut= null;

            try {
                mProgressDialog.dismiss();
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }

            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true)
            {
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream:" + incomingMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error reading InputStream:");
                    break;
                }

            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Closing Socket");
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(TAG, "cancel: Close of mmSocket failed. " + e.getMessage());
            }
        }

        public void write(byte[] bytes)
        {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "Writing to output" + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connected ( BluetoothSocket mmSocket, BluetoothDevice mmDevice)
    {
        Log.d(TAG, "ConnectionThread: Starting");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();

    }

    public void write(byte[] out){
        ConnectedThread r;

        Log.d(TAG, "Write called");
        mConnectedThread.write(out);
    }


}
