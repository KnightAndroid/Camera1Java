package com.knight.cameraone.activity;

import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.knight.cameraone.CameraPresenter;
import com.knight.cameraone.R;
import com.knight.cameraone.adapter.PhotosAdapter;
import com.knight.cameraone.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author created by knight
 * @organize
 * @Date 2019/9/24 11:06
 * @descript:
 */

public class CustomCameraActivity extends AppCompatActivity implements View.OnClickListener,CameraPresenter.CameraCallBack,View.OnTouchListener {

    //拍照
    private TextView tv_takephoto;
    //逻辑层
    private CameraPresenter mCameraPresenter;
    //SurfaceView
    private SurfaceView sf_camera;
    //显示拍下来的图片
    private ImageView iv_photo;
    //更换摄像头
    private TextView tv_change_camera;

    private static final int MODE_INIT = 0;
    private static final int MODE_ZOOM = 1;
    //标识模式
    private int mode = MODE_INIT;

    //缩放值
    private int mZoom;

    //两点的初始距离
    private float startDis;

    //Recycleview
    private RecyclerView cy_photo;

    //适配器
    private PhotosAdapter mPhotosAdapter;
    //图片List
    private List<String> photoList;





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
        photoList = new ArrayList<>();
        mPhotosAdapter = new PhotosAdapter(photoList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(OrientationHelper.VERTICAL);
        cy_photo.setLayoutManager(layoutManager);
        cy_photo.setAdapter(mPhotosAdapter);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_takephoto:
                //拍照的调用方法
                mCameraPresenter.takePicture();
                break;
            case R.id.iv_photo:
                cy_photo.setVisibility(cy_photo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.tv_change_camera:
                mCameraPresenter.switchCamera();
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
        cy_photo = findViewById(R.id.cy_photo);
        tv_change_camera = findViewById(R.id.tv_change_camera);
    }


    /**
     * 添加点击事件 触摸事件
     *
     */
    private void initListener(){
        tv_takephoto.setOnClickListener(this);
        sf_camera.setOnTouchListener(this);
        iv_photo.setOnClickListener(this);
        tv_change_camera.setOnClickListener(this);
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

    /**
     * 返回拍照后的照片
     * @param imagePath
     */
    @Override
    public void getPhotoFile(String imagePath) {
        //设置头像
        Glide.with(this).load(imagePath)
                .apply(RequestOptions.bitmapTransform(new CircleCrop())
                        .override(iv_photo.getWidth(), iv_photo.getHeight())
                        .error(R.drawable.default_person_icon))
                .into(iv_photo);
        photoList.add(imagePath);
        mPhotosAdapter.notifyDataSetChanged();
    }


    /**
     *
     * 触摸回调
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            //手指按下屏幕
            case MotionEvent.ACTION_DOWN:
               mode = MODE_INIT;
               break;
            //当屏幕上已经有触摸点吃鱼按下的状态的时候，再有新的触电被按下时会触发
            case MotionEvent.ACTION_POINTER_DOWN:
               mode = MODE_ZOOM;
               //计算两个手指的距离 两点的距离
               startDis = SystemUtil.twoPointDistance(event);
               break;
            case MotionEvent.ACTION_MOVE:
               if(mode == MODE_ZOOM){
                   //只有两个点同时触屏才执行
                   if(event.getPointerCount() < 2){
                     return true;
                   }
                   //获取结束的距离
                   float endDis = SystemUtil.twoPointDistance(event);
                   //每变化10f zoom变1
                   int scale = (int) ((endDis - startDis) / 10f);
                   if(scale >= 1 || scale <= -1){
                       int zoom = mCameraPresenter.getZoom() + scale;
                       //判断zoom是否超出变焦j距离
                       if(zoom > mCameraPresenter.getMaxZoom()){
                           zoom = mCameraPresenter.getMaxZoom();
                       }
                       if(zoom < 0 ){
                           zoom = 0;
                       }
                       mCameraPresenter.setZoom(zoom);
                       //将最后一次的距离设为当前距离
                       startDis = endDis;
                   }
               }
               break;
        }
        return true;
    }
}
