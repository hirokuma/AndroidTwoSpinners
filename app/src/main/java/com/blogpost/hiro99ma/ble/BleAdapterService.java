package com.blogpost.hiro99ma.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class BleAdapterService extends Service {

    private static final String TAG = "BDS Android App";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private Handler mActivityHandler = null;
    private BluetoothDevice mDevice;
    private BluetoothGattDescriptor mDescriptor;

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int GATT_CHARACTERISTIC_READ = 4;
    public static final int GATT_REMOTE_RSSI = 5;
    public static final int MESSAGE = 6;
    public static final int NOTIFICATION_RECEIVED = 7;
    public static final int SIMULATED_NOTIFICATION_RECEIVED = 8;
    public static final int GATT_CHARACTERISTIC_WRITTEN = 9;
    public static final int GATT_DESCRIPTOR_WRITTEN = 10;
    public static final int ERROR = 11;

    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";
    public static final String PARCEL_ERROR = "ERROR";

    // UUIDs
    public static final String READ_WRITE_SERVICE_SERVICE_UUID = "B7B851C6D7B847B38F93773756B46F9A";

    public static final String READ_CHARACTERISTIC_UUID = "B7B820DDD7B847B38F93773756B46F9A";
    public static final String WRITE_CHARACTERISTIC_UUID = "B7B85DB0D7B847B38F93773756B46F9A";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    // Ble Gatt Callback ///////////////////////
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                sendConsoleMessage("Connected");
                Message msg = Message.obtain(mActivityHandler, GATT_CONNECTED);
                msg.sendToTarget();
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                sendConsoleMessage("Disconnected");
                Message msg = Message.obtain(mActivityHandler, GATT_DISCONNECT);
                msg.sendToTarget();
                mBluetoothGatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Message msg = Message.obtain(mActivityHandler, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage("characteristic read OK");
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(mActivityHandler,GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic read err:"+status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage("Characteristic " + characteristic.getUuid().toString() + " written OK");
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(mActivityHandler, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                reportError("characteristic write err:" + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            Message msg = Message.obtain(mActivityHandler,NOTIFICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage("Descriptor " + descriptor.getUuid().toString() + " written OK");
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_DESCRIPTOR_UUID, descriptor.getUuid().toString());
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, descriptor.getCharacteristic().getService().getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, descriptor.getCharacteristic().getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, descriptor.getValue());
                Message msg = Message.obtain(mActivityHandler, GATT_DESCRIPTOR_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                reportError("Descriptor write err:" + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(mActivityHandler, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                reportError("RSSI read err:"+status);
            }
        }
    };

    // service binder ////////////////
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BleAdapterService getService() {
            return BleAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
    }

    // connect to the device
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            sendConsoleMessage("connect: mBluetoothAdapter=null");
            return false;
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            sendConsoleMessage("connect: device=null");
            return false;
        }

        // set auto connect to true
        mBluetoothGatt = mDevice.connectGatt(this, true, mGattCallback);
        sendConsoleMessage("connect: auto connect set to true");
        return true;
    }

    // disconnect from device
    public void disconnect() {
        sendConsoleMessage("disconnect");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            sendConsoleMessage("disconnect: mBluetoothAdapter|mBluetoothGatt null");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    // set activity the will receive the messages
    public void setActivityHandler(Handler handler) {
        mActivityHandler = handler;
    }

    // return list of supported services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    // writes a value to a characteristic with response required
    public boolean writeCharacteristic(String serviceUuid,String characteristicUuid, byte[] value) {
        return writeCharacteristic(serviceUuid,characteristicUuid, value,true);
    }

    // writes a value to a characteristic with/without response
    public boolean writeCharacteristic(String serviceUuid,String characteristicUuid, byte[] value, boolean require_response) {
        Log.d(TAG, "writeCharacteristic serviceUuid="+serviceUuid+" characteristicUuid="+characteristicUuid+" require_response="+require_response);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            sendConsoleMessage("writeCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("writeCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("writeCharacteristic: gattChar null");
            return false;
        }
        gattChar.setValue(value);

        if (require_response) {
            gattChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        } else {
            gattChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }

        return mBluetoothGatt.writeCharacteristic(gattChar);
    }

    // read value from service
    public boolean readCharacteristic(String serviceUuid, String characteristicUuid) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            sendConsoleMessage("readCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("readCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("readCharacteristic: gattChar null");
            return false;
        }
        return mBluetoothGatt.readCharacteristic(gattChar);
    }

    public boolean setNotificationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            sendConsoleMessage("setNotificationsState: mBluetoothAdapter|mBluetoothGatt null");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setNotificationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setNotificationsState: gattChar null");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        mDescriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        Log.d(Constants.TAG, "XXXX Descriptor:" + mDescriptor.getUuid());
        mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        return mBluetoothGatt.writeDescriptor(mDescriptor);
    }

    public boolean setIndicationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            sendConsoleMessage("setIndicationsState: mBluetoothAdapter|mBluetoothGatt null");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setIndicationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setIndicationsState: gattChar null");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        mDescriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        Log.d(Constants.TAG, "XXXX Descriptor:" + mDescriptor.getUuid());
        mDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        return mBluetoothGatt.writeDescriptor(mDescriptor);
    }

    public void readRemoteRssi() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    private void reportError(String text) {
        Log.d(Constants.TAG, "ERROR: "+text);
        Message msg = Message.obtain(mActivityHandler, ERROR);
        Bundle data = new Bundle();
        data.putString(PARCEL_ERROR, text);
        msg.setData(data);
        msg.sendToTarget();
    }

    private void sendConsoleMessage(String text) {
        Log.d(Constants.TAG, "XXXX "+text);
        Message msg = Message.obtain(mActivityHandler, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }

}
