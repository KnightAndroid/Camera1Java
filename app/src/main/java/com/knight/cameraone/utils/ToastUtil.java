package com.knight.cameraone.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/23 16:17
 * @descript:
 */

public class ToastUtil {
    private static Toast mToast = null;


    /**
     * 弹出短提示
     * @param context 上下文
     * @param message 文本提示
     */
    public static void showShortToast(Context context,String message){
        showToastMessage(context,message,Toast.LENGTH_SHORT);

    }


    /**
     * 弹出Toast提示
     * @param context 上下文
     * @param message 要显示的message
     * @param duration 时间长短
     */
    public static void showToastMessage(Context context,String message,int duration){
        if(mToast == null){
            mToast = Toast.makeText(context,message,duration);
        }else{
            mToast.setText(message);
            mToast.setDuration(duration);
        }
        mToast.show();

    }
}
