package com.blogpost.hiro99ma.twospinners;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.blogpost.hiro99ma.Constants;
import com.blogpost.hiro99ma.PeripheralSelectDialogFragment;


public class MainActivity extends Activity implements PeripheralSelectDialogFragment.ClickListener {
    //BLE
    private static final int REQUEST_LOCATION = 0;
    private boolean permissions_granted=false;
    private BluetoothAdapter mBluetoothAdapter = null;
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
    }


    @Override
    public void onClickPositive(BluetoothDevice device) {
        Log.d("Activity", "onClickPositive : " + device.getName());
    }

    @Override
    public void onClickNegative() {
        Log.d("Activity", "onClickNegative");
    }

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
}
