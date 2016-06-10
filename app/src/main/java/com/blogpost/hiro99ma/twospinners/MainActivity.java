package com.blogpost.hiro99ma.twospinners;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.blogpost.hiro99ma.ble.BleAdapterService;
import com.blogpost.hiro99ma.ble.Constants;
import com.blogpost.hiro99ma.ble.PeripheralSelectDialogFragment;
import com.blogpost.hiro99ma.ble.Utility;


public class MainActivity extends Activity implements PeripheralSelectDialogFragment.BleScanResultListener {
    //BLE
    private static final int REQUEST_LOCATION = 0;
    private boolean permissions_granted=false;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BleAdapterService mBluetoothLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BleAdapterService.LocalBinder)service).getService();
            mBluetoothLeService.setActivityHandler(mMessageHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };
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

        //BLE
        onCreateBle();
        //BLE

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
        mAdapterSub = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        Button buttonExec = (Button)findViewById(R.id.button_exec);
        if ((spinMain == null) || (mSpinSub == null) || (buttonExec == null)) {
            mSpinSub = null;
            mAdapterSub = null;
            return;
        }

        //main category
        ArrayAdapter<String> adapterMain = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
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

        //BLE
        //button : execute Scan
        Button buttonScan = (Button)findViewById(R.id.button_scan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start BLE Scan
                if (permissions_granted) {
                    PeripheralSelectDialogFragment dlg = PeripheralSelectDialogFragment.newInstance();
                    dlg.show(getFragmentManager(), "scan");
                }
            }
        });
        //BLE
    }

    //BLE
    @Override
    public void onScanResult(BluetoothDevice device) {
        Log.d("Activity", "onScanResult : " + device.getName());

        if (mBluetoothLeService != null) {
            if (mBluetoothLeService.connect(device.getAddress())) {
                Log.d("Activity", "onScanResult : connect !");
            }
            else {
                Log.d("Activity", "onScanResult : fail connect");
            }
        }
    }
    //BLE

    /////////////////////////////////////////////////////////////////////////////////////////
    //BLE
    private void onCreateBle() {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // BLE enable ?
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return;
        }
        Log.d(Constants.TAG, "Bluetooth is switched on");

        //have Locate Permission ?
        //https://developer.android.com/reference/android/os/Build.VERSION_CODES.html?hl=ja#M
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

        //サービスにバインド
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    //位置情報の許可
    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Log.i(Constants.TAG, "■shouldShowRequestPermissionRationale : true");
            //2回目以降は説明ダイアログを表示してから位置情報許可を求めている
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("許可がいるのです");
            builder.setMessage("アプリの位置情報を許可しないと、BLE機器のスキャンができません");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    //位置情報許可を求める
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            //初回
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
            } else {
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //BLE
    /////////////////////////////////////////////////////////////////////////////////////////


    // Service message handler
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid="";
            String characteristic_uuid="";
            String descriptor_uuid="";
            byte[] b=null;
            TextView value_text=null;

            switch (msg.what) {
                case BleAdapterService.GATT_CONNECTED:
//                    ((Button) PeripheralControlActivity.this.findViewById(R.id.button_connect)).setEnabled(false);
//                    // we're connected
//                    enableGattOpButtons();
//                    enableGattOpEditTexts();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
//                    ((Button) PeripheralControlActivity.this.findViewById(R.id.button_connect)).setEnabled(true);
//                    PeripheralControlActivity.this.stopTimer();
//                    disableGattOpButtons();
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    Log.d(Constants.TAG, "Services discovered");

                    // start off the rssi reading timer
//                    PeripheralControlActivity.this.startReadRssiTimer();

                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_READ:
//                    Log.d(Constants.TAG, "Handler received characteristic read result");
//                    bundle = msg.getData();
//                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
//                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
//                    Log.d(Constants.TAG, "Handler processing characteristic " + characteristic_uuid + " of " + service_uuid);
//                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
//                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
//                    value_text = (TextView) findViewByUUIDs(VIEW_TYPE_TEXT_VIEW, service_uuid, characteristic_uuid);
//                    if (value_text != null) {
//                        Log.d(Constants.TAG, "Handler found TextView for characteristic value");
//                        value_text.setText(Utility.byteArrayAsHexString(b));
//                    }
//                    enableGattOpButtons();
                    break;
                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
//                    Log.d(Constants.TAG, "Handler received characteristic written result");
//                    bundle = msg.getData();
//                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
//                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
//                    Log.d(Constants.TAG, "characteristic " + characteristic_uuid + " of " + service_uuid+" written OK");
//                    enableGattOpButtons();
                    break;
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
//                    Log.d(Constants.TAG, "Handler received descriptor written result");
//                    bundle = msg.getData();
//                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
//                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
//                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
//                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of " + "characteristic " + characteristic_uuid + " of " + service_uuid+" written OK");
//                    enableGattOpButtons();
                    break;
                case BleAdapterService.NOTIFICATION_RECEIVED:
//                    Log.d(Constants.TAG, "Handler received notification");
//                    bundle = msg.getData();
//                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
//                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
//                    Log.d(Constants.TAG, "Handler processing characteristic " + characteristic_uuid + " of " + service_uuid);
//                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
//                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
//                    value_text = (TextView) findViewByUUIDs(VIEW_TYPE_TEXT_VIEW, service_uuid, characteristic_uuid);
//                    if (value_text != null) {
//                        Log.d(Constants.TAG, "Handler found TextView for characteristic value");
//                        value_text.setText(Utility.byteArrayAsHexString(b));
//                    }
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
//                    bundle = msg.getData();
//                    int rssi = bundle.getInt(BleAdapterService.PARCEL_RSSI);
//                    PeripheralControlActivity.this.updateRssi(rssi);
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
//                    showMsg(text);
                    break;
                case BleAdapterService.ERROR:
                    bundle = msg.getData();
                    String error = bundle.getString(BleAdapterService.PARCEL_ERROR);
//                    showMsg(error);
//                    enableGattOpButtons();
            }
        }
    };
}
