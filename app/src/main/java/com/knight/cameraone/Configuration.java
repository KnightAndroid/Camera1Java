package com.knight.cameraone;

import android.os.Environment;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/29 9:39
 * @descript:一些常量
 */

public class Configuration {

    //这是app内部存储 格式如下 /data/data/包名/xxx/ 内部存储在Android系统对应的根目录是 /data/data/，这个目录普通用户是无权访问的，用户需要root权限才可以查看
    public static String insidePath = "/data/data/com.knight.cameraone/pic/";
    //这是app外部存储的私有存储目录 沙盒模式(Android 10)
    //context.getExternalFilesDir(String type)
    /**
     * 1.如果type为""，那么获取到的目录是 /storage/emulated/0/Android/data/package_name/files
     * 2.如果type不为空，则会在/storage/emulated/0/Android/data/package_name/files目录下创建一个以传入的type值为名称的目录，例如你将type设为了test，那么就会创建/storage/emulated/0/Android/data/package_name/files/test目录，这个其实有点类似于内部存储getDir方法传入的name参数。但是android官方推荐使用以下的type类型
     *  public static String DIRECTORY_MUSIC = "Music";
     *  public static String DIRECTORY_PODCASTS = "Podcasts";
     *  public static String DIRECTORY_RINGTONES = "Ringtones";
     *  public static String DIRECTORY_ALARMS = "Alarms";
     *  public static String DIRECTORY_NOTIFICATIONS = "Notifications";
     *  public static String DIRECTORY_PICTURES = "Pictures";
     *  public static String DIRECTORY_MOVIES = "Movies";
     *  public static String DIRECTORY_DOWNLOADS = "Download";
     *  public static String DIRECTORY_DCIM = "DCIM";
     *  public static String DIRECTORY_DOCUMENTS = "Documents";
     *
     *
     */
    //外部路径
    public static String OUTPATH = Environment.getExternalStorageDirectory() + "/拍照-相册/";
}
