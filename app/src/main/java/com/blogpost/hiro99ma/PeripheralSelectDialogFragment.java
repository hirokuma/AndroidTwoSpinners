package com.blogpost.hiro99ma;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.blogpost.hiro99ma.twospinners.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/06/03.
 */
public class PeripheralSelectDialogFragment extends DialogFragment {
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner scanner = null;
    private boolean mScanning = false;
    private ListAdapter mLeDeviceListAdapter = new ListAdapter();

    public void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    //Scanした機器一覧のリスト
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        scanLeDevices();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.app_name)
                .setSingleChoiceItems(mLeDeviceListAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("ListView", "onClick");
                    }
                })
                .setPositiveButton(R.string.fire, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                        Log.d("dialog", "ok button");
                        setScanState(false);
                        scanner.stopScan(mLeScanCallback);                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d("dialog", "cancel button");
                        setScanState(false);
                        scanner.stopScan(mLeScanCallback);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("dialog", "onDismiss");
        setScanState(false);
        scanner.stopScan(mLeScanCallback);    }

    //Scan動作の本体
    private void scanLeDevices() {
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        List<ScanFilter> filters;
        filters = new ArrayList<ScanFilter>();
        ScanFilterFactory filter_factory = ScanFilterFactory.getInstance();
        filters.add(filter_factory.getScanFilter());
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        setScanState(true);
        scanner.startScan(filters, settings, mLeScanCallback);
    }

    //Scan結果のコールバック関数
    private ScanCallback mLeScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, final ScanResult result) {
            Log.d("scancb", result.getDevice().getName());
            mLeDeviceListAdapter.addDevice(result.getDevice());
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    };

    private void setScanState(boolean value) {
        mScanning = value;
//        ((Button)this.findViewById(R.id.button_scan)).setText(value ? "Stop" : "Scan");
//        if (mScanning) {
//            showMsg("Scanning...");
//        } else {
//            showMsg("");
//        }
    }

    static class ViewHolder {
        public TextView text;
    }

    //Scanした結果の機器リスト用
    // adaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        public ListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }

        public void addDevice(BluetoothDevice device) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = PeripheralSelectDialogFragment.this.getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                viewHolder = new ViewHolder();
                viewHolder.text = (TextView)view.findViewById(android.R.id.text1);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if ((deviceName != null) && (deviceName.length() > 0)) {
                viewHolder.text.setText(deviceName);
            } else {
                viewHolder.text.setText("unknown device");
            }

            return view;
        }
    }
}
