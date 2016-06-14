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
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class BleAdapterService extends Service {

    private static final String TAG = "BDS:BleAdapterService";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private MessageHandler mMessageHandler = null;
    private BluetoothDevice mDevice;
    private BluetoothGattDescriptor mDescriptor;

    private CountDownLatch mThreadJoin;
    private boolean mConnected = false;
    private BleCallback mBleCallback;
    private Bundle mCallbackBundle = null;

    public BluetoothDevice getDevice() {
        return mDevice;
    }
    public void JoinCountStart() throws InterruptedException {
        mThreadJoin = new CountDownLatch(1);
        mThreadJoin.await();
    }
    public void JoinCountDown() {
        mThreadJoin.countDown();
    }
    public void setBleCallback(BleCallback cb) {
        mBleCallback = cb;
    }

    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int NOTIFICATION_RECEIVED = 4;
    public static final int GATT_REMOTE_RSSI = 5;
    public static final int MESSAGE = 6;
    public static final int ERROR = 7;

    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";
    public static final String PARCEL_ERROR = "ERROR";

    // UUIDs
    public static final String READ_WRITE_SERVICE_SERVICE_UUID = "B7B851C6-D7B8-47B3-8F93-773756B46F9A";

    public static final String READ_CHARACTERISTIC_UUID = "B7B80010-D7B8-47B3-8F93-773756B46F9A";
    public static final String WRITE_CHARACTERISTIC_UUID = "B7B80011-D7B8-47B3-8F93-773756B46F9A";
    public static final String WWR_CHARACTERISTIC_UUID = "B7B80012-D7B8-47B3-8F93-773756B46F9A";
    public static final String NOTIFY_CHARACTERISTIC_UUID = "B7B80013-D7B8-47B3-8F93-773756B46F9A";
    public static final String INDICATE_CHARACTERISTIC_UUID = "B7B80014-D7B8-47B3-8F93-773756B46F9A";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public interface BleCallback {
        void onBleConnected();
        void onBleDisconnected();
        void onBleNotification(Bundle bundle);
    }

    public boolean isConnected() {
        return mConnected;
    }

    // Ble Gatt Callback ///////////////////////
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                sendConsoleMessage("Connected");
                mConnected = true;
                Message msg = Message.obtain(mMessageHandler, GATT_CONNECTED);
                msg.sendToTarget();
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnected = false;
                sendConsoleMessage("Disconnected");
                Message msg = Message.obtain(mMessageHandler, GATT_DISCONNECT);
                msg.sendToTarget();
                mBluetoothGatt.close();
                mBleCallback.onBleDisconnected();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Message msg = Message.obtain(mMessageHandler, GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
            mBleCallback.onBleConnected();
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
                mCallbackBundle = bundle;
                JoinCountDown();
            } else {
                Log.w(TAG, "characteristic read err:" + status);
                sendConsoleMessage("characteristic read err:" + status);
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
                mCallbackBundle = bundle;
                JoinCountDown();
            } else {
                Log.w(TAG, "characteristic write err:" + status);
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
            Message msg = Message.obtain(mMessageHandler,NOTIFICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
            mBleCallback.onBleNotification(bundle);
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
                mCallbackBundle = bundle;
                JoinCountDown();
            } else {
                Log.w(TAG, "Descriptor write err:" + status);
                reportError("Descriptor write err:" + status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message.obtain(mMessageHandler, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                Log.w(TAG, "RSSI read err:" + status);
                reportError("RSSI read err:" + status);
            }
        }
    };

    private static class MessageHandler extends Handler {
        private byte[] mBytes;

        public MessageHandler() {
            super(Looper.getMainLooper());
        }

        public byte[] getBytes() { return mBytes; }

        @Override
        public void handleMessage(Message msg) {
            final String TAG = "service handler";

            Bundle bundle;
            String service_uuid;
            String characteristic_uuid;

            mBytes = new byte[] {};
            switch (msg.what) {
                case BleAdapterService.GATT_CONNECTED:
                    Log.d(TAG, "GATT_CONNECTED");
                    //UIを有効にするなどの処理(接続ボタンがあれば無効にする)
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    Log.d(TAG, "GATT_DISCONNECT");
                    //UIを無効にするなどの処理(接続ボタンがあれば有効にする)
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    Log.d(TAG, "GATT_SERVICES_DISCOVERED");
                    break;
                case BleAdapterService.NOTIFICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    Log.d(TAG, "NOTIFICATION_RECEIVED: " + characteristic_uuid + ":" + service_uuid);
                    mBytes = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(TAG, "  Value=" + Utility.byteArrayAsHexString(mBytes));
                    break;
                case BleAdapterService.GATT_REMOTE_RSSI:
                    break;
//                case BleAdapterService.MESSAGE:
//                    bundle = msg.getData();
//                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
//                    Log.d(TAG, "MESSAGE: " + text);
//                    break;
                case BleAdapterService.ERROR:
                    bundle = msg.getData();
                    String error = bundle.getString(BleAdapterService.PARCEL_ERROR);
                    Log.d(TAG, "ERROR: " + error);
            }
        }
    }

    // service binder ////////////////
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BleAdapterService getService() {
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

        mMessageHandler = new MessageHandler();
    }

    // connect to the device
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "connect: mBluetoothAdapter=null");
            sendConsoleMessage("connect: mBluetoothAdapter=null");
            return false;
        }

        mDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Log.w(TAG, "connect: device=null");
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
            Log.w(TAG, "disconnect: mBluetoothAdapter|mBluetoothGatt null");
            sendConsoleMessage("disconnect: mBluetoothAdapter|mBluetoothGatt null");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    // return list of supported services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    // writes a value to a characteristic with response required
//    public boolean writeCharacteristic(String serviceUuid,String characteristicUuid, byte[] value) {
//        return writeCharacteristic(serviceUuid,characteristicUuid, value, true);
//    }

    // writes a value to a characteristic with/without response
    public byte[] writeCharacteristic(String serviceUuid,String characteristicUuid, byte[] value, boolean require_response) {
        Log.d(TAG, "writeCharacteristic serviceUuid="+serviceUuid+" characteristicUuid="+characteristicUuid+" require_response="+require_response);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "writeCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            sendConsoleMessage("writeCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            return new byte[] {};
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            Log.w(TAG, "writeCharacteristic: gattService null");
            sendConsoleMessage("writeCharacteristic: gattService null");
            return new byte[] {};
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            Log.w(TAG, "writeCharacteristic: gattChar null");
            sendConsoleMessage("writeCharacteristic: gattChar null");
            return new byte[] {};
        }
        gattChar.setValue(value);

        if (require_response) {
            gattChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        } else {
            gattChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }

        boolean ret = mBluetoothGatt.writeCharacteristic(gattChar);
        if (!ret) {
            Log.w(TAG, "writeCharacteristic: writeCharacteristic false");
            sendConsoleMessage("writeCharacteristic: writeCharacteristic false");
            return new byte[] {};
        }

        //完了待ち
        byte[] bytes;
        try {
            JoinCountStart();
            String service_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
            String characteristic_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
            bytes = mCallbackBundle.getByteArray(BleAdapterService.PARCEL_VALUE);
            Log.d(TAG, "GATT_CHARACTERISTIC_WRITTEN: " + characteristic_uuid + ":" + service_uuid + "  Value=" + Utility.byteArrayAsHexString(bytes));
        } catch (InterruptedException e) {
            bytes = new byte[] {};
        }
        mCallbackBundle = null;

        return bytes;
    }

    // read value from service
    public byte[] readCharacteristic(String serviceUuid, String characteristicUuid) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "readCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            sendConsoleMessage("readCharacteristic: mBluetoothAdapter|mBluetoothGatt null");
            return new byte[] {};
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            Log.w(TAG, "readCharacteristic: gattService null");
            sendConsoleMessage("readCharacteristic: gattService null");
            return new byte[] {};
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            Log.w(TAG, "readCharacteristic: gattChar null");
            sendConsoleMessage("readCharacteristic: gattChar null");
            return new byte[] {};
        }
        boolean ret = mBluetoothGatt.readCharacteristic(gattChar);
        if (!ret) {
            Log.w(TAG, "readCharacteristic: readCharacteristic false");
            sendConsoleMessage("readCharacteristic: readCharacteristic false");
            return new byte[] {};
        }

        //完了待ち
        byte[] bytes;
        try {
            JoinCountStart();
            String service_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
            String characteristic_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
            bytes = mCallbackBundle.getByteArray(BleAdapterService.PARCEL_VALUE);
            Log.d(TAG, "GATT_CHARACTERISTIC_READ: " + characteristic_uuid + ":" + service_uuid + "  Value=" + Utility.byteArrayAsHexString(bytes));
        } catch (InterruptedException e) {
            bytes = new byte[] {};
        }
        mCallbackBundle = null;

        return bytes;
    }

    public boolean setNotificationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        return setDescriptorState(serviceUuid, characteristicUuid, enabled, true);
    }

    public boolean setIndicationsState(String serviceUuid, String characteristicUuid, boolean enabled) {
        return setDescriptorState(serviceUuid, characteristicUuid, enabled, false);
    }

    private boolean setDescriptorState(String serviceUuid, String characteristicUuid, boolean enabled, boolean notification) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "setDescriptorState: mBluetoothAdapter|mBluetoothGatt null");
            sendConsoleMessage("setDescriptorState: mBluetoothAdapter|mBluetoothGatt null");
            return false;
        }

        BluetoothGattService gattService = mBluetoothGatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            Log.w(TAG, "setDescriptorState: gattService null");
            sendConsoleMessage("setDescriptorState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            Log.w(TAG, "setDescriptorState: gattChar null");
            sendConsoleMessage("setDescriptorState: gattChar null");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        mDescriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        Log.d(TAG, "Descriptor:" + mDescriptor.getUuid());
        byte[] value;
        if (notification) {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        }
        else {
            value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        }
        mDescriptor.setValue(value);

        boolean ret = mBluetoothGatt.writeDescriptor(mDescriptor);
        if (!ret) {
            Log.w(TAG, "setDescriptorState: writeDescriptor false");
            sendConsoleMessage("setDescriptorState: writeDescriptor false");
            return false;
        }
        //完了待ち
        try {
            JoinCountStart();
            String service_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
            String characteristic_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
            String descriptor_uuid = mCallbackBundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
            Log.d(TAG, "GATT_DESCRIPTOR_WRITTEN: " + descriptor_uuid + "(" + characteristic_uuid + ":" + service_uuid + ")");
        } catch (InterruptedException e) {
            ret = false;
        }
        mCallbackBundle = null;

        return ret;
    }

    public void readRemoteRssi() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readRemoteRssi();
    }

    private void reportError(String text) {
        Log.d(TAG, "ERROR: "+text);
        Message msg = Message.obtain(mMessageHandler, ERROR);
        Bundle data = new Bundle();
        data.putString(PARCEL_ERROR, text);
        msg.setData(data);
        msg.sendToTarget();
    }

    private void sendConsoleMessage(String text) {
        Log.d(TAG, text);
        Message msg = Message.obtain(mMessageHandler, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }
}
