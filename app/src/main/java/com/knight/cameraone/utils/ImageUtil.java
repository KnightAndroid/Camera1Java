package com.knight.cameraone.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author created by luguian
 * @organize 车童网
 * @Date 2020/9/3 19:45
 * @descript:照片处理
 */
public class ImageUtil {

    public static void  saveAlbum(Context context, File targetFile){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            ContentValues contentValues = new ContentValues();
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,targetFile.getName());
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
            //方式1 会在Pictures / Camera 文件夹下生成图片
            //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/Camera");
            //方式2 直接在Pictures生成
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            Uri insertUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            BufferedInputStream inputStream;
            OutputStream outputStream;

            try {
                inputStream  = new BufferedInputStream(new FileInputStream(targetFile));
                 if(insertUri != null){
                   outputStream = context.getContentResolver().openOutputStream(insertUri);
                   if(outputStream != null){
                       byte[] buffer = new byte[1024];
                       int length = -1;
                       while ((length = inputStream.read(buffer)) != -1){
                            outputStream.write(buffer,0,length);
                       }
                       outputStream.close();
                       inputStream.close();
                   }

                 }
            }catch (IOException e){
               return;
            }

        } else {
            SystemUtil.saveAlbum(targetFile.getAbsolutePath(), targetFile.getName(), context);
        }


    }



}
