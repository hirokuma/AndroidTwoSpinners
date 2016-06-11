package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

import com.blogpost.hiro99ma.ble.BleAdapterService;
import com.blogpost.hiro99ma.ble.Utility;

/**
 * Created on 2016/06/01.
 */
public class Test1 implements ITestForm {
    private static final Test1 mInstance = new Test1();
    public static Test1 getInstance() { return mInstance; }
    private Test1() {}

    private static final String MAIN_CATEGORY_NAME = "main1";

    private static final Command[] SUB_CATEGORY = new Command[]{
            //sub1-1
            new Command() {
                @Override
                public String name() {
                    return "sub1-1";
                }

                @Override
                public void execute(MainActivity activity) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-1");

                    BleAdapterService srv = activity.getBleAdapterService();
                    byte[] wrt = new byte[] { 0x12, 0x34, 0x56 };
                    srv.writeCharacteristic(BleAdapterService.READ_WRITE_SERVICE_SERVICE_UUID, BleAdapterService.WRITE_CHARACTERISTIC_UUID, wrt);
                }
            },
            //sub1-2
            new Command() {
                @Override
                public String name() {
                    return "sub1-2";
                }

                @Override
                public void execute(MainActivity activity) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-2");

                    BleAdapterService srv = activity.getBleAdapterService();
                    srv.readCharacteristic(BleAdapterService.READ_WRITE_SERVICE_SERVICE_UUID, BleAdapterService.READ_CHARACTERISTIC_UUID);
                }
            },
            //sub1-3
            new Command() {
                @Override
                public String name() {
                    return "sub1-3";
                }

                @Override
                public void execute(MainActivity activity) {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-3");
                }
            },
    };

    @Override
    public String getCategoryName() {
        return MAIN_CATEGORY_NAME;
    }

    @Override
    public Command[] getSubCategory() {
        return SUB_CATEGORY;
    }
}
