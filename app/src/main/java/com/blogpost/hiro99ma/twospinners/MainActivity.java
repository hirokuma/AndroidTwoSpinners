package com.blogpost.hiro99ma.twospinners;

import android.Manifest;
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.blogpost.hiro99ma.Constants;
import com.blogpost.hiro99ma.ScanFilterFactory;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //BLE
    private static final int REQUEST_LOCATION = 0;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeScanner scanner = null;
    private boolean permissions_granted=false;
    private boolean mScanning = false;
    private ListAdapter mLeDeviceListAdapter;
    //BLE


    private Spinner mSpinSub;
    private ArrayAdapter<String> mAdapterSub;

    private int mMainCategoryIdx = 0;
    private int mSubCategoryIdx = 0;
    private Command[][] mExecCommands;

    //ここにテストクラスのインスタンスを追加する
    private ITestForm[] mTestForm = new ITestForm[] {
            Test1.getInstance(),
            Test2.getInstance(),
            Test3.getInstance(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreateBle();

        mExecCommands = new Command[mTestForm.length + 1][];
        mExecCommands[0] = new Command[mTestForm.length];
        int idx = 0;
        for (final ITestForm form : mTestForm) {
            mExecCommands[0][idx] = new Command() {
                @Override
                String name() {
                    return form.getCategoryName();
                }
            };
            idx++;
        }
        idx = 1;
        for (ITestForm form : mTestForm) {
            mExecCommands[idx] = form.getSubCategory();
            idx++;
        }

        Spinner spinMain = (Spinner) this.findViewById(R.id.spin_main_category);
        mSpinSub = (Spinner) this.findViewById(R.id.spin_sub_category);
        mAdapterSub = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        Button buttonExec = (Button)findViewById(R.id.button_exec);
        Button buttonScan = (Button)findViewById(R.id.button_scan);
        if ((spinMain == null) || (mSpinSub == null) || (buttonExec == null) || (buttonScan == null)) {
            mSpinSub = null;
            mAdapterSub = null;
            return;
        }

        //main category
        ArrayAdapter<String> adapterMain = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item);
        for (Command cmd : mExecCommands[0]) {
            if (cmd != null) {
                adapterMain.add(cmd.name());
            }
        }
        spinMain.setAdapter(adapterMain);

        spinMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mMainCategoryIdx = (int)id;
                mSubCategoryIdx = 0;

                mAdapterSub.clear();
                //int idx = 1;
                for (Command cmd : mExecCommands[mMainCategoryIdx + 1]) {
                    mAdapterSub.add(cmd.name());
                    //mAdapterSub.add(String.valueOf(idx) + "." + cmd.name());
                    //idx++;
                }
                mSpinSub.setAdapter(mAdapterSub);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //sub category
        mSpinSub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSubCategoryIdx = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //button : execute TEST
        buttonExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("EXEC " + String.valueOf(mMainCategoryIdx + 1) + "-" + String.valueOf(mSubCategoryIdx + 1), "[" + mSpinSub.getSelectedItem().toString() + "]");
                mExecCommands[mMainCategoryIdx + 1][mSubCategoryIdx].execute(MainActivity.this);
            }
        });

        //button : execute Scan
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check bluetooth is available on on
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                    return;
                }
                Log.d(Constants.TAG, "Bluetooth is switched on");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        permissions_granted = false;
                        requestLocationPermission();
                    } else {
                        Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning.");
                        permissions_granted = true;
                    }
                } else {
                    // the ACCESS_COARSE_LOCATION permission did not exist before M so....
                    permissions_granted = true;
                }
                if (permissions_granted) {
                    if (!mScanning) {
                        scanLeDevices();
                    } else {
                        setScanState(false);
                        scanner.stopScan(mLeScanCallback);
                    }
                }

            }
        });
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    //BLE
    private void onCreateBle() {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //Scanした機器一覧のリスト
        mLeDeviceListAdapter = new ListAdapter();

    }

    private void setScanState(boolean value) {
        mScanning = value;
        ((Button)this.findViewById(R.id.button_scan)).setText(value ? "Stop" : "Scan");
//        if (mScanning) {
//            showMsg("Scanning...");
//        } else {
//            showMsg("");
//        }
    }


    //位置情報の許可
    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }
    }

    //位置情報の許可結果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;

                //Scan開始
                scanLeDevices();
            } else {
                Log.i(Constants.TAG, "Location permission was NOT granted.");
                //showMsg("Required permissions not granted so cannot start");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Scan動作の本体
    private void scanLeDevices() {
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        List<ScanFilter> filters;
        filters = new ArrayList<ScanFilter>();
        ScanFilterFactory filter_factory = ScanFilterFactory.getInstance();
        filters.add(filter_factory.getScanFilter());
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        if (permissions_granted) {
            setScanState(true);
            scanner.startScan(filters, settings, mLeScanCallback);
        } else {
            Log.d(Constants.TAG,"Application lacks permission to start Bluetooth scanning");
        }
    }

    //Scan結果のコールバック関数
    private ScanCallback mLeScanCallback = new ScanCallback() {
        public void onScanResult(int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("scancb", result.getDevice().getName());
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };



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
//            ViewHolder viewHolder;
//            // General ListView optimization code.
//            if (view == null) {
//                view = MainActivity.this.getLayoutInflater().inflate(R.layout.list_row, null);
//                viewHolder = new ViewHolder();
//                viewHolder.text = (TextView)view.findViewById(R.id.textView);
//                view.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder)view.getTag();
//            }
//            BluetoothDevice device = mLeDevices.get(i);
//            final String deviceName = device.getName();
//            if ((deviceName != null) && (deviceName.length() > 0)) {
//                viewHolder.text.setText(deviceName);
//            } else {
//                viewHolder.text.setText("unknown device");
//            }

            return view;
        }
    }
    //BLE
    /////////////////////////////////////////////////////////////////////////////////////////
}
