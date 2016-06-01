package com.blogpost.hiro99ma.twospinners;

import android.util.Log;

/**
 * Created by hiroshi on 2016/06/01.
 */
abstract class Command {
    ///spinnerのアイテム名
    abstract String name();

    ///実行する内容
    void execute() { Log.d("Command", "nothing"); }
}

