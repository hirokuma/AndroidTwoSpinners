package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

import junit.framework.Test;

/**
 * Created by hiroshi on 2016/06/01.
 */
public class Test3 {
    static String MAIN_CATEGORY_NAME = "main3";

    static final Command TEST_MAIN_CATEGORY = new Command() {
        @Override
        String name() {
            return MAIN_CATEGORY_NAME;
        }
    };

    static final Command[] TEST_SUB_CATEGORY =  new Command[] {
            //sub3-1
            new Command() {
                @Override
                public String name() {
                    return "sub3-1";
                }
                @Override
                public void execute() {
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
                public void execute() {
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
                public void execute() {
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
                public void execute() {
                    Log.d(MAIN_CATEGORY_NAME, "sub3-4");
                }
            },
    };
}
