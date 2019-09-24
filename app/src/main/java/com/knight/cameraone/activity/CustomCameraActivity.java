package com.knight.cameraone.activity;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.TextView;

import com.knight.cameraone.R;


/**
 * @author created by knight
 * @organize
 * @Date 2019/9/24 11:06
 * @descript:
 */

public class CustomCameraActivity extends AppCompatActivity implements View.OnClickListener {

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
    //拍照
    private TextView tv_takephoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customcamera);
        initBind();
        initListener();
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
    }


    /**
     * 添加点击事件
     *
     */
    private void initListener(){
        tv_takephoto.setOnClickListener(this);
    }


    /**
     * 初始化相机
     *
     */
    private void initCamera(){

    }


    /**
     * 初始化相机参数
     */
    private void initParameter() {

    }









}
