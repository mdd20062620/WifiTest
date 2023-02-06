package com.ruiguan.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void showToast(Context context, String content) {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                Toast.makeText(context, content, Toast.LENGTH_LONG).show();
//            }
//        });
        Toast.makeText(context, content, Toast.LENGTH_LONG).show();
    }
}