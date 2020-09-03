package com.knight.cameraone.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/23 17:28
 * @descript:
 */

public class SystemUtil {

    /**
     * 获取包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化时间
     * @param time
     * @return "yyyy-MM-dd HH:mm:ss"
     */
    public static String formatTime(long time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(time);// 获取当前时间
        return formatter.format(curDate);

    }

    /**
     * 格式化时间
     * @param time 时间
     * @param file 文件命名
     * @return
     */
    public static String formatTime(long time,String file){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(time);// 获取当前时间
        return formatter.format(curDate) + file;

    }

    /**
     * 格式化三位随机数
     * @param i 传入的数字
     * @return
     */
    public static String formatRandom(int i){
        String s = i + "";
        if(s.length() == 1){
            return "000" + s;
        }else if(s.length() == 2){
            return "00" + s;
        }else if(s.length() == 3){
            return  "0" + s;
        }else{
            return s;
        }
    }

    /**
     * 将图片保存到手机相册
     * @param path 路径
     * @param name 文件名字
     * @param context 上下文
     */
    public static void saveAlbum(String path,String name,Context context){
       //把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),path,name,null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }


    /**
     * 将内部照片复制到外部
     * @param srcPath app内包内路径
     * @param dstPath 外部路径
     * @param name 名字
     */
    public static void copyPicture(String srcPath,String dstPath,String name){
        //源文件流
        FileInputStream fileInputStream = null;
        //结果流
        FileOutputStream fileOutputStream = null;
        File dir = new File(dstPath);
        if(!dir.exists() || !dir.isDirectory()){
            dir.mkdir();
        }

        try {
            fileInputStream = new FileInputStream(srcPath);
            fileOutputStream = new FileOutputStream(dstPath + name);
            byte[] bytes = new byte[1024];
            int by;
            while ((by = fileInputStream.read(bytes)) != -1){
                fileOutputStream.write(bytes,0,by);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
               if(fileInputStream != null){
                   fileInputStream.close();
               }
               if(fileOutputStream != null){
                   fileOutputStream.close();
               }
            } catch (Exception e){
               e.printStackTrace();
            }
        }


    }


    /**
     * 两点的距离
     * @param event 事件
     * @return
     */
    public static float twoPointDistance(MotionEvent event){
        if(event == null){
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


}
