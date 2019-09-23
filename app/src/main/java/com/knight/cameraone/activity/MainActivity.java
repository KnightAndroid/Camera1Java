package com.knight.cameraone.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import android.view.SurfaceHolder;
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
    //是否已经申请权限
    private boolean havePermission;
    //Camera对象
    private Camera mCamera;
    //Camera设置参数对象
    private Camera.Parameters mParameters;
    //SurfaceHolder对象
    private SurfaceHolder mSurfaceHolder;
    //通过这个类创建传感器服务实例，可以访问传感器列表、获取方位信息
    private SensorManager mSensorManager;
    //创建传感器实例
    private Sensor mSensor;
    //调用系统相机
    private Button btn_system_camera;
    //调用系统相册
    private Button btn_system_photo;

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
        btn_system_camera = findViewById(R.id.btn_system_camera);
        iv_photo = findViewById(R.id.iv_photo);
        btn_system_photo = findViewById(R.id.btn_system_photo);
        initListener();
        //6.0以上需要动态申请权限 动态权限校验 Android 6.0 的 oppo & vivo 手机时，始终返回 权限已被允许 但是当真正用到该权限时，却又弹出权限申请框。
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //多个权限一起申请
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);

            } else {
                //已经全部申请
                initCamera();
            }

        }else{
            initCamera();
        }
    }


    /**
     * 初始化相机资源
     */
    private void initCamera() {

    }

    /**
     * 初始化相机参数
     */
    private void initParameter() {

    }


    private void initListener(){
        btn_system_camera.setOnClickListener(this);
        btn_system_photo.setOnClickListener(this);
    }


    /**
     * 动态处理申请权限的结果
     * 用户点击同意或者拒绝后触发
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
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
                            initCamera();
                        } else {
                            Permissions.showPermissionsSettingDialog(this, permissions[1]);
                        }
                    } else {
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
            case R.id.btn_system_camera:
                goSystemCamera();
                break;
            case R.id.btn_system_photo:
                goSystemPhoto();
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
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() +".jpg");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //调用相机适配>=6.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
           uri = FileProvider.getUriForFile(this, SystemUtil.getPackageName(getApplicationContext()) + ".fileprovider",cameraSavePath);
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        startActivityForResult(intent,1);
    }


    private void goSystemPhoto(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,2);
    }


    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        String photoPath;
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
            photoPath = PhotoAlbumUtil.getRealPathFromUri(this,data.getData());
            Glide.with(this).load(photoPath).apply(RequestOptions.noTransformation()
                    .override(iv_photo.getWidth(),iv_photo.getHeight())
                    .error(R.drawable.default_person_icon))
                    .into(iv_photo);

        }
        super.onActivityResult(requestCode, resultCode, data);

    }



}
