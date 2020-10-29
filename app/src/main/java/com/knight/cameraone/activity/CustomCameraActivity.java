package com.knight.cameraone.activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.knight.cameraone.CameraPresenter;
import com.knight.cameraone.CircleButtonView;
import com.knight.cameraone.R;
import com.knight.cameraone.adapter.PhotosAdapter;
import com.knight.cameraone.utils.SystemUtil;
import com.knight.cameraone.utils.ToastUtil;
import com.knight.cameraone.view.FaceDeteView;

import java.util.ArrayList;
import java.util.List;


/**
 * @author created by knight
 * @organize
 * @Date 2019/9/24 11:06
 * @descript:
 */

public class CustomCameraActivity extends AppCompatActivity implements View.OnClickListener, CameraPresenter.CameraCallBack, View.OnTouchListener, PhotosAdapter.OnItemClickListener {

    //拍照
    private CircleButtonView tv_takephoto;
    //逻辑层
    private CameraPresenter mCameraPresenter;
    //SurfaceView
    private SurfaceView sf_camera;
    //显示拍下来的图片
    private ImageView iv_photo;
    //更换摄像头
    private TextView tv_change_camera;
    //闪光灯
    private TextView tv_flash;
    //开启关闭人脸识别按钮
    private TextView tv_facedetect;
    //人脸检测框
    private FaceDeteView faceView;

    //全屏还是4：3
    private TextView tv_matchorwrap;

    private static final int MODE_INIT = 0;
    //两个触摸点触摸屏幕状态
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
    private boolean isMove = false;
    //闪光灯开关
    private boolean isTurn = true;
    //开启人脸识别
    private boolean isFaceDetect = true;

    int tempWidth, tempHeight;
    private boolean isFull = false;

    private ConstraintLayout cl_parent;


    //屏幕的宽
    private int screenWidth;
    private int screenHeight;

    private ImageView iv_test;

    //
    private int aftersufaceViewWidth;
    private int aftersurfaceViewHeight;

    //增加传感器
    private OrientationEventListener mOrientationEventListener;
    //当前角度
    private float mCurrentOrientation = 0f;
    //拍照时的传感器方向
    private int takePhotoOrientation = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
           supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        // 去掉通知栏
     //   getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
      //          WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_customcamera);


        //获取屏幕宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        getScreenBrightness();
        //绑定View
        initBind();
        //添加点击，触摸事件等监听
        initListener();

        initOrientate();
        //初始化CameraPresenter
        mCameraPresenter = new CameraPresenter(this, sf_camera);
        //设置后置摄像头
        mCameraPresenter.setFrontOrBack(Camera.CameraInfo.CAMERA_FACING_BACK);
        //添加监听
        mCameraPresenter.setCameraCallBack(this);
        //添加人脸检测
        mCameraPresenter.setFaceView(faceView);
        //默认关闭人脸检测
        mCameraPresenter.turnFaceDetect(false);

      //  ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(screenHeight, ConstraintLayout.LayoutParams.WRAP_CONTENT);
      //  sf_camera.setLayoutParams(layoutParams);
        photoList = new ArrayList<>();
        mPhotosAdapter = new PhotosAdapter(photoList);
        mPhotosAdapter.setOnItemClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) layoutManager).setOrientation(OrientationHelper.VERTICAL);
        cy_photo.setLayoutManager(layoutManager);
        cy_photo.setAdapter(mPhotosAdapter);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_photo:
                cy_photo.setVisibility(cy_photo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            //改变摄像头
            case R.id.tv_change_camera:
                mCameraPresenter.switchCamera();
                break;
            //关闭还是开启闪光灯
            case R.id.tv_flash:
                mCameraPresenter.turnLight(isTurn);
                tv_flash.setBackgroundResource(isTurn ? R.drawable.icon_turnon : R.drawable.icon_turnoff);
                isTurn = !isTurn;
                break;
            //开启人脸检测
            case R.id.tv_facedetect:
                mCameraPresenter.turnFaceDetect(isFaceDetect);
                tv_facedetect.setBackgroundResource(isFaceDetect ? R.drawable.icon_facedetect_on : R.drawable.icon_facedetect_off);
                isFaceDetect = !isFaceDetect;
                break;
            //全屏还是4：3
            case R.id.tv_matchorwrap:
                cl_parent.removeView(sf_camera);
                int screen[] = getScreen();
                //定义布局参数
                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                if(isFull){
                    //是全屏 切换成4：3
                    layoutParams.width = (int) (screen[0]);
                    layoutParams.height = (int) (screen[0] * 4/3);
                } else {
                    //不是全屏
                    //是全屏 切换成4：3
                    layoutParams.width = (int) (screen[0]);
                    layoutParams.height = (int) (screen[1]);
                }
                sf_camera.setLayoutParams(layoutParams);
                isFull = !isFull;
                mCameraPresenter.setFull(isFull);
                cl_parent.addView(sf_camera,0,layoutParams);
                break;
            default:
                break;
        }
    }



    @Override
    protected void onResume(){
        super.onResume();
//        if(isFull){
//            ViewGroup.LayoutParams params = sf_camera.getLayoutParams();
//
//            params.height = screenHeight;
//            params.width = screenWidth;
//            // params.width = view.getWidth();
//            Log.d("sssd--重新进入的宽",params.width+"");
//            sf_camera.setLayoutParams(params);
//        } else {
//            ViewGroup.LayoutParams params = sf_camera.getLayoutParams();
//
//            params.height =screenWidth * 4/ 3;
//         //   params.width = screenWidth ;
//            // params.width = view.getWidth();
//            sf_camera.setLayoutParams(params);
//        }
    }

    /**
     * 绑定view组件
     */
    private void initBind() {
        tv_takephoto = findViewById(R.id.tv_takephoto);
        sf_camera = findViewById(R.id.sf_camera);
        iv_photo = findViewById(R.id.iv_photo);
        cy_photo = findViewById(R.id.cy_photo);
        tv_change_camera = findViewById(R.id.tv_change_camera);
        tv_flash = findViewById(R.id.tv_flash);
        tv_facedetect = findViewById(R.id.tv_facedetect);
        faceView = findViewById(R.id.faceView);
        tv_matchorwrap = findViewById(R.id.tv_matchorwrap);
        cl_parent = findViewById(R.id.cl_parent);
        iv_test = findViewById(R.id.iv_test);
    }


    /**
     * 添加点击事件 触摸事件
     */
    private void initListener() {
        sf_camera.setOnTouchListener(this);
        iv_photo.setOnClickListener(this);
        tv_change_camera.setOnClickListener(this);
        tv_flash.setOnClickListener(this);
        //点击事件
        tv_takephoto.setOnClickListener(new CircleButtonView.OnClickListener() {
            @Override
            public void onClick() {
                //拍照的调用方法
                mCameraPresenter.takePicture(takePhotoOrientation);
            }
        });

        //长按事件
        tv_takephoto.setOnLongClickListener(new CircleButtonView.OnLongClickListener() {
            @Override
            public void onLongClick() {


               // mCameraPresenter.startRecord(Configuration.OUTPATH, "video");
                mCameraPresenter.startRecord(getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath(),"video");

            }

            @Override
            public void onNoMinRecord(int currentTime) {
                ToastUtil.showShortToast(CustomCameraActivity.this, "录制时间太短～");
            }

            @Override
            public void onRecordFinishedListener() {
                mCameraPresenter.stopRecord();
                startActivity(new Intent(CustomCameraActivity.this, PlayAudioActivity.class)
                   .putExtra("videoPath",mCameraPresenter.getVideoFilePath()));
            }
        });
        tv_facedetect.setOnClickListener(this);
        tv_matchorwrap.setOnClickListener(this);
    }


    /**
     * Activity 销毁回调方法 释放各种资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraPresenter != null) {
            mCameraPresenter.releaseCamera();
        }

        if(mOrientationEventListener != null){
            mOrientationEventListener.disable();
        }
    }

    /**
     * 预览回调
     *
     * @param data 预览数据
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    /**
     * 拍照回调
     *
     * @param data 拍照数据
     */
    @Override
    public void onTakePicture(byte[] data, Camera camera) {

    }

    /**
     * 人脸检测回调
     *
     * @param rectFArrayList
     */
    @Override
    public void onFaceDetect(ArrayList<RectF> rectFArrayList, Camera camera) {

    }

    /**
     * 返回拍照后的照片
     *
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

    @Override
    public void getVideoFile(String videoFilePath) {

    }


    /**
     * 触摸回调
     *
     * @param v     添加Touch事件具体的view
     * @param event 具体事件
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //无论多少跟手指加进来，都是MotionEvent.ACTION_DWON MotionEvent.ACTION_POINTER_DOWN
        //MotionEvent.ACTION_MOVE:
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //手指按下屏幕
            case MotionEvent.ACTION_DOWN:
                mode = MODE_INIT;
                break;
            //当屏幕上已经有触摸点按下的状态的时候，再有新的触摸点被按下时会触发
            case MotionEvent.ACTION_POINTER_DOWN:
                mode = MODE_ZOOM;
                //计算两个手指的距离 两点的距离
                startDis = SystemUtil.twoPointDistance(event);
                break;
            //移动的时候回调
            case MotionEvent.ACTION_MOVE:
                isMove = true;
                //这里主要判断有两个触摸点的时候才触发
                if (mode == MODE_ZOOM) {
                    //只有两个点同时触屏才执行
                    if (event.getPointerCount() < 2) {
                        return true;
                    }
                    //获取结束的距离
                    float endDis = SystemUtil.twoPointDistance(event);
                    //每变化10f zoom变1
                    int scale = (int) ((endDis - startDis) / 10f);
                    if (scale >= 1 || scale <= -1) {
                        int zoom = mCameraPresenter.getZoom() + scale;
                        //判断zoom是否超出变焦距离
                        if (zoom > mCameraPresenter.getMaxZoom()) {
                            zoom = mCameraPresenter.getMaxZoom();
                        }
                        //如果系数小于0
                        if (zoom < 0) {
                            zoom = 0;
                        }
                        //设置焦距
                        mCameraPresenter.setZoom(zoom);
                        //将最后一次的距离设为当前距离
                        startDis = endDis;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //判断是否点击屏幕 如果是自动聚焦
                if (isMove == false) {
                    //自动聚焦
                    mCameraPresenter.autoFoucus();
                }
                isMove = false;
                break;
        }
        return true;
    }

    /**
     * 加入调整亮度
     */
    private void getScreenBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //screenBrightness的值是0.0-1.0 从0到1.0 亮度逐渐增大 如果是-1，那就是跟随系统亮度
        lp.screenBrightness = Float.valueOf(200) * (1f / 255f);
        getWindow().setAttributes(lp);
    }


    /**
     * 跳转到大图
     *
     * @param v
     * @param path
     */
    @Override
    public void onItemClick(View v, String path) {
        startActivity(new Intent(CustomCameraActivity.this, BigPhotoActivity.class).putExtra("imagePhoto", path));
    }


    /**
     * 获取屏幕宽高
     */
    private int[] getScreen() {
        int[] screens = new int[2];
        //获取屏幕宽度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        //宽
        screens[0] = width;
        //高
        screens[1] = height;

        return screens;


    }


    /**
     * 放大缩小动画
     *
     * @param v
     */
    private void scaleSurfaceView(View v, boolean isFull) {
        ScaleAnimation scamleAni;
        if (isFull) {
            scamleAni = new ScaleAnimation(1f, 1, 1f, 0.5f, screenWidth / 2, screenHeight / 2);
            // scamleAni = new ScaleAnimation(screenWidth / 2,screenWidth / 2,screenHeight / 2,screenWidth  / 2 * 4 / 3, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        } else {
            // scamleAni = new ScaleAnimation(v.getMeasuredWidth() / 2,screenWidth / 2,v.getMeasuredHeight() / 2,screenHeight / 2, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            scamleAni = new ScaleAnimation(1, 1f, 1, 1.5f, screenWidth / 2, screenHeight / 2);
        }

        //设置动画执行的时间，单位是毫秒
        scamleAni.setDuration(1500);
        scamleAni.setFillAfter(true);
        v.startAnimation(scamleAni);


    }

    public  void changeViewHeightAnimatorStart(final View view, final int startHeight, final int endHeight) {
        if (view != null && startHeight >= 0 && endHeight >= 0) {

            ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override

                public void onAnimationUpdate(ValueAnimator animation) {

                    ViewGroup.LayoutParams params = view.getLayoutParams();

                    params.height = (int) animation.getAnimatedValue();

                    String s  = String.valueOf(animation.getAnimatedValue());
                 //   params.width = (int) (Float.valueOf(s) / 4.0f * 3);
                    view.setLayoutParams(params);


                }

            });


            animator.start();

        }
    }


    /**
     * 初始化传感器方向
     *
     *
     */
    private void  initOrientate(){
        if(mOrientationEventListener == null){
            mOrientationEventListener = new OrientationEventListener(CustomCameraActivity.this) {
                @Override
                public void onOrientationChanged(int orientation) {
                    // i的范围是0-359
                    // 屏幕左边在顶部的时候 i = 90;
                    // 屏幕顶部在底部的时候 i = 180;
                    // 屏幕右边在底部的时候 i = 270;
                    // 正常的情况默认i = 0;
                    if(45 <= orientation && orientation < 135){
                        takePhotoOrientation = 180;
                        mCurrentOrientation = -180;
                    } else if(135 <= orientation && orientation < 225){
                        takePhotoOrientation = 270;
                        mCurrentOrientation = 90;
                    } else if(225 <= orientation && orientation < 315){
                        takePhotoOrientation = 0;
                        mCurrentOrientation = 0;
                    } else {
                        takePhotoOrientation = 90;
                        mCurrentOrientation = -90;
                    }



                }
            };

        }
        mOrientationEventListener.enable();

    }


}
