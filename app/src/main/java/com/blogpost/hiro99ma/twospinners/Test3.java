package com.blogpost.hiro99ma.twospinners;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.blogpost.hiro99ma.ble.BleAdapterService;

import junit.framework.Test;

/**
 * Created on 2016/06/01.
 */
public class Test3 implements ITestForm {
    private static final Test3 mInstance = new Test3();
    public static Test3 getInstance() { return mInstance; }
    private Test3() {}

    private static String MAIN_CATEGORY_NAME = "main3";

    private static final Command[] SUB_CATEGORY =  new Command[] {
            //sub3-1
            new Command() {
                @Override
                public String name() {
                    return "sub3-1";
                }
                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub3-1");
                }
            },
            //sub3-2
            new Command() {
                @Override
                public String name() {
                    return "sub3-2";
                }
                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub3-2");
                }
            },
            //sub3-3
            new Command() {
                @Override
                public String name() {
                    return "sub3-3";
                }
                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "sub3-3");
                }
            },
            //sub3-4
            new Command() {
                @Override
                public String name() {
                    return "sub3-4";
                }
                @Override
                public void execute(BleAdapterService service) {
                    Log.d(MAIN_CATEGORY_NAME, "test sub3-4");
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
