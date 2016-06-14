package com.blogpost.hiro99ma.twospinners;

import android.content.Context;
import android.util.Log;

import com.blogpost.hiro99ma.ble.BleAdapterService;

/**
 * Created on 2016/06/01.
 */
abstract class Command {
    ///spinnerのアイテム名
    abstract String name();

    ///実行する内容
    void execute(BleAdapterService service) { Log.d("Command", "nothing"); }
}

