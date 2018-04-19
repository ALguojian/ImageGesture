package com.alguojian.imagegesture.util;

import android.content.Context;

/**
 * Created by Hello on 2017/5/17.
 */

//防止用户多次点击同一个按钮造成多余操作
public class NoDoubleClickUtils {

    private static long lastClickTime = 0;
    private final static int SPACE_TIME = 5000;

    public static boolean isDoubleClick(Context context) {

        boolean isClick2 = false;

        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastClickTime) >= SPACE_TIME) {
            isClick2 = true;
        }
        lastClickTime = currentTime;
        return isClick2;
    }
}
