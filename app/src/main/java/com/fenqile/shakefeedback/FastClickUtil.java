package com.fenqile.shakefeedback;

import android.util.Log;

public class FastClickUtil {
    private static final int MIN_DELAY_TIME = 500;  // 两次点击间隔不能少于50ms
    private static long lastClickTime;
    private static long lastInvokeTime;

    /**
     * 判断是否多次点击
     *
     * @return 是否快速点击
     */
    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    public static boolean isFastClick(int internalTime) {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= internalTime) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    //判断是否短时间重复调用
    public static boolean isFastInvoke(int interval) {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        Log.e("xx", String.valueOf((currentClickTime - lastInvokeTime)));
        if ((currentClickTime - lastInvokeTime) >= interval) {
            flag = false;
        }
        lastInvokeTime = currentClickTime;
        return flag;
    }
}