package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

/**
 * Created by hiroshi on 2016/06/01.
 */
public class Test1 {
    static String MAIN_CATEGORY_NAME = "main1";

    static final Command TEST_MAIN_CATEGORY = new Command() {
        @Override
        String name() {
            return MAIN_CATEGORY_NAME;
        }
    };

    static final Command[] TEST_SUB_CATEGORY = new Command[] {
            //sub1-1
            new Command() {
                @Override
                public String name() {
                    return "sub1-1";
                }
                @Override
                public void execute() {
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
                public void execute() {
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
                public void execute() {
                    Log.d(MAIN_CATEGORY_NAME, "sub1-3");
                }
            },
    };

}
