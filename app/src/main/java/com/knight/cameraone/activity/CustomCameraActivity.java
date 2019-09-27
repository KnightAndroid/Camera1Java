package com.knight.cameraone.activity;

import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knight.cameraone.CameraPresenter;
import com.knight.cameraone.R;

import java.util.ArrayList;


/**
 * @author created by knight
 * @organize
 * @Date 2019/9/24 11:06
 * @descript:
 */

public class CustomCameraActivity extends AppCompatActivity implements View.OnClickListener,CameraPresenter.CameraCallBack {

    //拍照
    private TextView tv_takephoto;
    //逻辑层
    private CameraPresenter mCameraPresenter;
    //SurfaceView
    private SurfaceView sf_camera;
    //显示拍下来的图片
    private ImageView iv_photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customcamera);
        initBind();
        initListener();
        //初始化CameraPresenter
        mCameraPresenter = new CameraPresenter(this,sf_camera);
        //设置后置摄像头
        mCameraPresenter.setFrontOrBack(Camera.CameraInfo.CAMERA_FACING_BACK);
        //添加监听
        mCameraPresenter.setCameraCallBack(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_takephoto:
                break;
            default:
                break;

        }
    }

    /**
     * 绑定view组件
     */
    private void initBind(){
        tv_takephoto = findViewById(R.id.tv_takephoto);
        sf_camera = findViewById(R.id.sf_camera);
        iv_photo = findViewById(R.id.iv_photo);
    }


    /**
     * 添加点击事件
     *
     */
    private void initListener(){
        tv_takephoto.setOnClickListener(this);
    }


    /**
     * Activity 销毁回调方法 释放各种资源
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(mCameraPresenter != null){
            mCameraPresenter.releaseCamera();
        }
    }

    /**
     * 预览回调
     * @param data 预览数据
     */
    @Override
    public void onPreviewFrame(byte[] data,Camera camera) {

    }

    /**
     * 拍照回调
     * @param data 拍照数据
     */
    @Override
    public void onTakePicture(byte[] data,Camera camera) {

    }

    /**
     * 人脸检测回调
     * @param rectFArrayList
     */
    @Override
    public void onFaceDetect(ArrayList<RectF> rectFArrayList,Camera camera) {

    }
}
