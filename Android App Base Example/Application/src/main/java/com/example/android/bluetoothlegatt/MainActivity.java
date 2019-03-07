package com.example.android.bluetoothlegatt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity
{

    private DeviceScanActivity deviceScanActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        LayoutInflater inflater = getLayoutInflater();
//        View deviceScanView = getLayoutInflater().inflate(R.layout.device_scan_layout , null);
//        LinearLayout scan_wrapper = (LinearLayout)findViewById(R.id.scan_wrapper);
//        scan_wrapper.addView(deviceScanView);

        View view;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.device_scan_layout, null);

        //RelativeLayout item = (RelativeLayout) view.findViewById(R.id.device_scan_layout);

    }

}
