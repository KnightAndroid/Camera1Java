package com.knight.cameraone.utils.permissionsUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/23 16:00
 * @descript:
 */

public class Permissions {


    /**
     * 弹出权限提示框
     * @param context 上下文
     * @param permission 具体权限名称
     */
    public static void showPermissionsSettingDialog(final Context context, String permission){
        String msg = "本App需要"+permission+"权限才能正常运行，请点击确定，进入设置界面进行授权处理~";
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       showSettings(context);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }


    /**
     * 如果授权失败，就要进入App权限设置界面
     *
     * @param context 上下文
     */
    public static void showSettings(Context context){
       Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",context.getPackageName(),null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }


}
