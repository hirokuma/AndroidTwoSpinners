package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

/**
 * Created by hiroshi on 2016/06/01.
 */
public class Test2 {
    static String MAIN_CATEGORY_NAME = "main2";

    static final Command TEST_MAIN_CATEGORY = new Command() {
        @Override
        String name() {
            return MAIN_CATEGORY_NAME;
        }
    };

    static final Command[] TEST_SUB_CATEGORY = new Command[] {
            //sub2-1
            new Command() {
                @Override
                public String name() {
                    return "sub2-1";
                }
                @Override
                public void execute() {
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
                public void execute() {
                    Log.d(MAIN_CATEGORY_NAME, "sub2-2");
                }
            },
    };
}
