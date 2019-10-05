package com.knight.cameraone.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.knight.cameraone.R;
import com.knight.cameraone.utils.PhotoAlbumUtil;
import com.knight.cameraone.utils.SystemUtil;
import com.knight.cameraone.utils.ToastUtil;
import com.knight.cameraone.utils.permissionsUtil.Permissions;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //调用系统相机
    private Button btn_system_camera;
    //调用系统相册
    private Button btn_system_photo;
    //调用自定义相机
    private Button btn_custom_camera;
    //调用系统拍照返回的uri
    private Uri uri;
    //拍照照片的路径
    private File cameraSavePath;
    //展示照片的View
    private ImageView iv_photo;
    //权限申请
    String[] needPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBind();
        initListener();
        checkNeedPermissions();
    }


    private void initBind(){
        btn_system_camera = findViewById(R.id.btn_system_camera);
        iv_photo = findViewById(R.id.iv_photo);
        btn_system_photo = findViewById(R.id.btn_system_photo);
        btn_custom_camera = findViewById(R.id.btn_custom_camera);
    }



    /**
     * 给按钮添加点击事件
     *
     */
    private void initListener(){
        btn_system_camera.setOnClickListener(this);
        btn_system_photo.setOnClickListener(this);
        btn_custom_camera.setOnClickListener(this);
    }

    /**
     * 检测需要申请的权限
     *
     */
    private void checkNeedPermissions(){
        //6.0以上需要动态申请权限 动态权限校验 Android 6.0 的 oppo & vivo 手机时，始终返回 权限已被允许 但是当真正用到该权限时，却又弹出权限申请框。
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                //多个权限一起申请
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                }, 1);

            }

        }
    }



    /**
     * 动态处理申请权限的结果
     * 用户点击同意或者拒绝后触发
     *
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果码
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                //获取权限一一验证
                if (grantResults.length > 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                            if(grantResults[2] == PackageManager.PERMISSION_GRANTED){
                                //全部授于可以进行往下操作
                            }

                        } else {
                            //拒绝就要强行跳转设置界面
                            Permissions.showPermissionsSettingDialog(this, permissions[1]);
                        }
                    } else {
                        //拒绝就要强行跳转设置界面
                        Permissions.showPermissionsSettingDialog(this, permissions[0]);
                    }
                } else {
                    ToastUtil.showShortToast(this, "请重新尝试~");
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //跳转系统相机
            case R.id.btn_system_camera:
                goSystemCamera();
                break;
            //跳转系统相册
            case R.id.btn_system_photo:
                goSystemPhoto();
                break;
            case R.id.btn_custom_camera:
                startActivity(new Intent(this,CustomCameraActivity.class));
                break;
            default:
                break;
        }
    }


    /**
     * 调用系统相机
     *
     */
    private void goSystemCamera(){
        //在根目录创建jpg文件
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() +".jpg");
        //指定跳到系统拍照
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //适配Android 7.0以上版本应用私有目录限制被访问
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
           uri = FileProvider.getUriForFile(this, SystemUtil.getPackageName(getApplicationContext()) + ".fileprovider",cameraSavePath);
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            //7.0以下
            uri = Uri.fromFile(cameraSavePath);
        }
        //指定ACTION为MediaStore.EXTRA_OUTPUT
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        //请求码赋值为1
        startActivityForResult(intent,1);
    }

    /**
     * 跳转系统相册
     */
    private void goSystemPhoto(){
        Intent intent = new Intent();
        //设置Intent.ACTION_PICK
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        String photoPath;
        //处理拍照后返回的图片路径
        if(requestCode == 1 && resultCode == RESULT_OK){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
               photoPath = String.valueOf(cameraSavePath);
            }else{
               photoPath = uri.getEncodedPath();
            }
            Log.d("拍照返回图片的路径:",photoPath);
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
            .override(iv_photo.getWidth(),iv_photo.getHeight())
            .error(R.drawable.default_person_icon))
            .into(iv_photo);
        }else if(requestCode == 2 && resultCode == RESULT_OK){
            //处理调用相册返回的路径
            photoPath = PhotoAlbumUtil.getRealPathFromUri(this,data.getData());
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
                    .override(iv_photo.getWidth(),iv_photo.getHeight())
                    .error(R.drawable.default_person_icon))
                    .into(iv_photo);

        }
        super.onActivityResult(requestCode, resultCode, data);

    }



}
