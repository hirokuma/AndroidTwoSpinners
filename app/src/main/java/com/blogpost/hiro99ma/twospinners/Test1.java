package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

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
