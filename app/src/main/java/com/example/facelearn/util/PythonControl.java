package com.example.facelearn.util;

import android.content.Context;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PythonControl {
    private Context context;
    public PythonControl(Context context) {
        this.context = context;
    }

    /**
     * refs: https://zenn.dev/ouma_san/articles/d6734a48743156
     */
    public String callPython() {
        // Pythonコードを実行する前にPython.start()の呼び出しが必要
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this.context));
        }
        // 実行するスクリプト名
        String scriptName = "convert";

        // インスタンスを取得
        Python py = Python.getInstance();
        // 指定したスクリプトのモジュールを取得
        PyObject module = py.getModule(scriptName);

        //float[][] img = new float[512][512];
        //PyObject result = module.callAttr("convert", new Object[]{img});
        PyObject result = module.callAttr("convert", new Object[]{});

        Log.d("MainActivity", "callPython: " + result);
        String str = result.toString();
        Log.d("MainActivity", "str: " + str);
        return str;
    }
}
