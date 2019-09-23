package com.knight.cameraone.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/23 18:42
 * @descript: 从相册内获取照片路径
 */

public class PhotoAlbumUtil {


    /**
     * 根据Uri来获取图片的绝对路径
     * @param context 上下文
     * @param uri 图片uri
     * @return
     */
    public static String getRealPathFromUri(Context context, Uri uri){
        if(Build.VERSION.SDK_INT >= 19){
           return getRealPathFromUriUpAPI19(context,uri);
        }else{
           return getRealPathFromUriDown19(context,uri);
        }
    }

    @SuppressLint("NewApi")
    private static String getRealPathFromUriUpAPI19(Context context, Uri uri){
       String filePath = null;
       //如果是document类型的uri，则通过document id来进行处理
       if(DocumentsContract.isDocumentUri(context, uri)){
           String documentId = DocumentsContract.getDocumentId(uri);
           if(isMediaDocument(uri)){
              //使用":"分割
              String id = documentId.split(":")[1];

              String selection = MediaStore.Images.Media._ID + "=?";
              String[] selectionArgs = {id};
              filePath = getDataColumn(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection,selectionArgs);

          } else if(isDownloadsDocument(uri)){
               Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
               filePath = getDataColumn(context, contentUri, null, null);

           }

       } else if("content".equalsIgnoreCase(uri.getScheme())){
           // 如果是 content 类型的 Uri
           filePath = getDataColumn(context, uri, null, null);
       } else if("file".equals(uri.getScheme())){
           // 如果是 file 类型的 Uri,直接获取图片对应的路径
           filePath = uri.getPath();
       }
        return filePath;
    }

    /**
     * 版本19以下，根据Uri来获取图片的绝对路径
     * @param context 上下文
     * @param uri 图片的Uri
     * @return 返回图片的绝对路径
     */
    private static String getRealPathFromUriDown19(Context context,Uri uri){
       return getDataColumn(context,uri,null,null);
    }


    /**
     * 根据数据库表中的_data列，返回Uri对应的文件路径
     * @param context 上下文
     * @param uri 图片的Uri
     * @param selection 要返回哪些行的筛选器
     * @param selectionArgs 替换selection中的?
     * @return
     */
     private static String getDataColumn(Context context,Uri uri,String selection,String[] selectionArgs){
        String path = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;

        try{
            cursor = context.getContentResolver().query(uri,projection,selection,selectionArgs,null);
            if(cursor != null && cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        }catch (Exception e){
            if(cursor != null){
                cursor.close();
            }
        }
        return path;

     }


    /**
     * 判断Uri是否是Mdeia类型的
     * @param uri
     * @return
     */
     private static boolean isMediaDocument(Uri uri){
        return "com.android.provides.media.documents".equals(uri.getAuthority());
     }


    /**
     * 判断Uri是否是downlaod类型
     * @param uri
     * @return
     */
     private static boolean isDownloadsDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
     }









}
