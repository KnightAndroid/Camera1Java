package com.knight.cameraone;

import android.os.Environment;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/29 9:39
 * @descript:一些常量
 */

public class Configuration {

    //这是app内部存储 格式如下 /data/data/包名/xxx/
    public static String insidePath = "/data/data/com.knight.cameraone/pic/";
    //外部路径
    public static String OUTPATH = Environment.getExternalStorageDirectory() + "/拍照-相册/";
}
