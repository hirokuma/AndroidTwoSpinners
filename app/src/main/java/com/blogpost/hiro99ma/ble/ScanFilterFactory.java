package com.blogpost.hiro99ma.ble;

import android.bluetooth.le.ScanFilter;

/**
 * Created by mwoolley on 20/11/2015.
 */
public class ScanFilterFactory {
    private static ScanFilterFactory instance;

    private ScanFilterFactory() {

    }

    public static synchronized ScanFilterFactory getInstance() {
        if (instance == null) {
            instance = new ScanFilterFactory();
        }
        return instance;
    }

    public ScanFilter getScanFilter() {
        //検索結果のフィルタ
        //https://developer.android.com/reference/android/bluetooth/le/ScanFilter.Builder.html

        //none
        return new ScanFilter.Builder().build();

        //Device Name
        //String device_name = "検索するDeviceName";
        //return new ScanFilter.Builder().setDeviceName(device_name).build();

        //Device Address
        //return new ScanFilter.Builder().setDeviceAddress("").build();

        //Manufacturer Data
        //return new ScanFilter.Builder().setManufacturerData(int manufacturerId, byte[] manufacturerData).build();
        //return new ScanFilter.Builder().setManufacturerData(int manufacturerId, byte[] manufacturerData, byte[] manufacturerDataMask).build();

        //Service Data
        //ParcelUuid serviceUuid = new ParcelUuid(UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB"));
        //return new ScanFilter.Builder().setServiceData(serviceDataUuid, byte[] serviceData).build();
        //return new ScanFilter.Builder().setServiceData(serviceDataUuid, byte[] serviceData, byte[] serviceDataMask).build();

        //Service Uuid
        //ParcelUuid serviceUuid = new ParcelUuid(UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB"));
        //return new ScanFilter.Builder().setServiceUuid(serviceUuid).build();
        //return new ScanFilter.Builder().setServiceUuid(serviceUuid, ParcelUuid uuidMask).build();
    }
}
