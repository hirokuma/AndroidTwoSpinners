package com.blogpost.hiro99ma.ble;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.blogpost.hiro99ma.twospinners.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/06/03.
 */
public class PeripheralSelectDialogFragment extends DialogFragment {
    private BluetoothLeScanner scanner = null;
    private ListAdapter mLeDeviceListAdapter = new ListAdapter();
    //private int mSelect = AdapterView.INVALID_POSITION;

    public interface BleScanResultListener {
        void onScanResult(BluetoothDevice device);
    }

    public static PeripheralSelectDialogFragment newInstance() {
        return new PeripheralSelectDialogFragment();
    }

    //Scanした機器一覧のリスト
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ表示中はスキャンし続ける
        scanLeDevices();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.app_name)
                //onClick()してもダイアログを閉じたくないのでsetSingleChoiceItems()を使う(邪道かもしれん)
                .setSingleChoiceItems(mLeDeviceListAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Log.d("ListView", "setAdapter - onClick");
                        //mSelect = position;
                    }
                })
                .setPositiveButton(R.string.ble_connect, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //接続したい
                        //int position = mSelect;

                        ListView lv = ((AlertDialog)dialog).getListView();
                        int position = lv.getCheckedItemPosition();
                        if (position != AdapterView.INVALID_POSITION) {
                            Log.d("dialog", "ok button : " + mLeDeviceListAdapter.getDevice(position).getName());
                            ((BleScanResultListener)getActivity()).onScanResult(mLeDeviceListAdapter.getDevice(position));
                        }
                        else {
                            Log.d("dialog", "ok button : not selected");
                        }

                        scanner.stopScan(mLeScanCallback);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        Log.d("dialog", "cancel button");
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
        scanner.stopScan(mLeScanCallback);
    }

    //Scan動作の本体
    private void scanLeDevices() {
        BluetoothManager bluetoothManager = (BluetoothManager)getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilterFactory filter_factory = ScanFilterFactory.getInstance();
        filters.add(filter_factory.getScanFilter());
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        scanner.startScan(filters, settings, mLeScanCallback);
    }

    //Scan結果のコールバック関数
    private ScanCallback mLeScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, final ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                Log.d("scancb", device.getName());
                mLeDeviceListAdapter.addDevice(result.getDevice());
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
            else {
                Log.d("scancb", "no device");
            }
        }
    };


    //リスト1行分
    static class ViewHolder {
        public TextView text;
    }

    //Scanした結果のadaptor
    private class ListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        public ListAdapter() {
            super();
            mLeDevices = new ArrayList<>();
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
                //https://android.googlesource.com/platform/frameworks/base/+/android-5.0.0_r1/core/res/res/layout/simple_list_item_single_choice.xml
                view = PeripheralSelectDialogFragment.this.getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_single_choice, null);
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
                viewHolder.text.setText(R.string.ble_unknown_device);
            }

            return view;
        }
    }
}