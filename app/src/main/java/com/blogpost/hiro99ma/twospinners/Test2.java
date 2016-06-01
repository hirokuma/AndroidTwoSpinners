package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

/**
 * Created on 2016/06/01.
 */
public class Test2 implements ITestForm {
    private static final Test2 mInstance = new Test2();
    public static Test2 getInstance() { return mInstance; }
    private Test2() {}

    private static String MAIN_CATEGORY_NAME = "main2";


    private static final Command[] SUB_CATEGORY = new Command[] {
            //sub2-1
            new Command() {
                @Override
                public String name() {
                    return "sub2-1";
                }
                @Override
                public void execute(MainActivity activity) {
                    Log.d(MAIN_CATEGORY_NAME, "sub2-1");
                }
            },
            //sub2-2
            new Command() {
                @Override
                public String name() {
                    return "sub2-2";
                }
                @Override
                public void execute(MainActivity activity) {
                    Log.d(MAIN_CATEGORY_NAME, "sub2-2");
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
