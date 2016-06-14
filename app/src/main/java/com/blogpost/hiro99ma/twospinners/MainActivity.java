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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blogpost.hiro99ma.ble.BleAdapterService;
import com.blogpost.hiro99ma.ble.PeripheralSelectDialogFragment;


public class MainActivity extends Activity implements PeripheralSelectDialogFragment.BleScanResultListener {
    //BLE
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_LOCATION_SERVICE = 1;
    private boolean permissions_granted = false;
    private BleAdapterService mBluetoothLeService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //BLE接続の通知を受けるので、その前に設定する
            mBluetoothLeService = ((BleAdapterService.LocalBinder) service).getService();
            mBluetoothLeService.setBleCallback(mBleCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };
    private BleAdapterService.BleCallback mBleCallback = new BleAdapterService.BleCallback() {
        @Override
        public void onBleConnected() {
            Log.d(TAG, "接続しました");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonScan.setEnabled(true);
                            mButtonScan.setText(R.string.connected);
                            mButtonExec.setEnabled(true);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onBleDisconnected() {
            Log.d(TAG, "切断しました");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonScan.setEnabled(true);
                            mButtonScan.setText(R.string.disconnected);
                            mButtonExec.setEnabled(false);
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onBleNotification(Bundle bundle) {
            String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
            Log.d(TAG, "Notification:" + text);
            TextView tv = (TextView)findViewById(R.id.text_log);
            tv.setText(tv.getText() + "\r\n" + text);
        }
    };
    //BLE


    private Spinner mSpinSub;
    Button mButtonExec;
    Button mButtonScan;
    private ArrayAdapter<String> mAdapterSub;

    private int mMainCategoryIdx = 0;
    private int mSubCategoryIdx = 0;
    private Command[][] mExecCommands;
    private static final String TAG = "MainActivity";

    //ここにテストクラスのインスタンスを追加する
    private ITestForm[] mTestForm = new ITestForm[]{
            Test1.getInstance(),
            Test2.getInstance(),
            Test3.getInstance(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //BLE
        boolean ble_enabled = onCreateBle(true);
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
        mButtonExec = (Button) findViewById(R.id.button_exec);
        mButtonScan = (Button) findViewById(R.id.button_scan);
        if ((spinMain == null) || (mSpinSub == null) || (mButtonExec == null)) {
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
                mMainCategoryIdx = (int) id;
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
                mSubCategoryIdx = (int) id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //button : execute TEST
        mButtonExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("EXEC " + String.valueOf(mMainCategoryIdx + 1) + "-" + String.valueOf(mSubCategoryIdx + 1), "[" + mSpinSub.getSelectedItem().toString() + "]");
                //mExecCommands[mMainCategoryIdx + 1][mSubCategoryIdx].execute(MainActivity.this);
                ExecTestCommand cmd = new ExecTestCommand();
                //同時に1つしか動かさないけど、まずはこうしておこう
                cmd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
            }
        });

        //BLE
        //button : execute Scan
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start BLE Scan
                if (permissions_granted) {
                    if (!mBluetoothLeService.isConnected()) {
                        //切断中
                        PeripheralSelectDialogFragment dlg = PeripheralSelectDialogFragment.newInstance();
                        dlg.show(getFragmentManager(), "scan");
                    } else {
                        //接続中
                        mBluetoothLeService.disconnect();
                        //押せなくする
                        mButtonScan.setEnabled(false);
                    }
                }
            }
        });
        //BLE
        mButtonScan.setEnabled(ble_enabled);
        mButtonExec.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);  //これをしないと終了時にmServiceConnectionがリークする
        mBluetoothLeService = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean ble_enabled = onCreateBle(false);
        mButtonScan.setEnabled(ble_enabled);
        if (mBluetoothLeService != null) {
            ble_enabled &= mBluetoothLeService.isConnected();
        }
        else {
            ble_enabled = false;
        }
        mButtonExec.setEnabled(ble_enabled);
    }


    //テストの実行スレッド
    public class ExecTestCommand extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            Log.d("ExecTestCommand", "onPreExecute");
            mButtonExec.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mExecCommands[mMainCategoryIdx + 1][mSubCategoryIdx].execute(MainActivity.this.mBluetoothLeService);
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {

        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d("ExecTestCommand", "onPostExecute");
            mButtonExec.setEnabled(true);
        }
    }

    //BLEスキャン結果で「接続」をタップした
    @Override
    public void onScanResult(BluetoothDevice device) {
        Log.d("Activity", "onScanResult : " + device.getName());

        if (mBluetoothLeService != null) {
            if (mBluetoothLeService.connect(device.getAddress())) {
                //押せなくする
                mButtonScan.setEnabled(false);
                mButtonScan.setText(R.string.connecting);
            } else {
                Log.d("Activity", "onScanResult : fail connect");
            }
        }
    }
    //BLE

    /////////////////////////////////////////////////////////////////////////////////////////
    //BLE
    private boolean onCreateBle(boolean gotoSetting) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // BLE enable ?
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Toast.makeText(this, R.string.ble_disable_bluetooth, Toast.LENGTH_SHORT).show();
            if (gotoSetting) {
                startActivity(enableBtIntent);
            }
            return false;
        }
        Log.d(TAG, "Bluetooth is switched on");

        //位置情報サービス
        final boolean locationEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF;
        if (!locationEnabled) {
            Toast.makeText(this, R.string.ble_disable_location_service, Toast.LENGTH_SHORT).show();
            if (gotoSetting) {
                Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableLocationIntent, REQUEST_LOCATION_SERVICE);
            }
            return false;
        }

        //アプリの位置情報許可
        //https://developer.android.com/reference/android/os/Build.VERSION_CODES.html?hl=ja#M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions_granted = false;
                Toast.makeText(this, R.string.ble_disable_location_app, Toast.LENGTH_SHORT).show();
                if (gotoSetting) {
                    requestLocationPermission();
                }
                return false;
            } else {
                Log.i(TAG, "Location permission has already been granted. Starting scanning.");
                permissions_granted = true;
            }
        } else {
            // the ACCESS_COARSE_LOCATION permission did not exist before M so....
            permissions_granted = true;
        }

        //サービスにバインド
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        return true;
    }


    //位置情報の許可
    private void requestLocationPermission() {
        Log.i(TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.i(TAG, "■shouldShowRequestPermissionRationale : true");
            //2回目以降は説明ダイアログを表示してから位置情報許可を求めている
            Log.i(TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("許可がいるのです");
            builder.setMessage("アプリの位置情報を許可しないと、BLE機器のスキャンができません");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    //位置情報許可を求める
                    Log.d(TAG, "Requesting permissions after explanation");
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
            Log.i(TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;
            } else {
                Log.i(TAG, "Location permission was NOT granted.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOCATION_SERVICE) {
            Log.d("onActivityResult", "resultCode=" + resultCode);
        }
    }
}
