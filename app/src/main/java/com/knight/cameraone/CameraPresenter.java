package com.knight.cameraone;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.knight.cameraone.utils.ImageUtil;
import com.knight.cameraone.utils.SystemUtil;
import com.knight.cameraone.utils.ThreadPoolUtil;
import com.knight.cameraone.utils.ToastUtil;
import com.knight.cameraone.view.FaceDeteView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author created by knight
 * @organize
 * @Date 2019/9/24 18:29
 * @descript:
 */

public class CameraPresenter implements Camera.PreviewCallback {


    //相机对象
    private Camera mCamera;
    //相机对象参数设置
    private Camera.Parameters mParameters;
    //自定义照相机页面
    private AppCompatActivity mAppCompatActivity;
    //surfaceView 用于预览对象
    private SurfaceView mSurfaceView;
    //SurfaceHolder对象
    private SurfaceHolder mSurfaceHolder;

    //摄像头Id 默认后置 0,前置的值是1
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    //预览旋转的角度
    private int orientation;

    //自定义回调
    private CameraCallBack mCameraCallBack;
    //手机宽和高
    private int screenWidth, screenHeight;
    //拍照数量
    private int photoNum = 0;
    //拍照存放的文件
    private File photosFile = null;

    //当前缩放具体值
    private int mZoom;

    //视频录制
    private MediaRecorder mediaRecorder;
    //录制视频的videoSize
    private int height,width;

    //检测头像的FaceView
    private FaceDeteView mFaceView;




    private boolean isFull =false;

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }



    //自定义回调
    public interface CameraCallBack {
        //预览帧回调
        void onPreviewFrame(byte[] data, Camera camera);

        //拍照回调
        void onTakePicture(byte[] data, Camera Camera);

        //人脸检测回调
        void onFaceDetect(ArrayList<RectF> rectFArrayList, Camera camera);

        //拍照路径返回
        void getPhotoFile(String imagePath);
    }

    public void setCameraCallBack(CameraCallBack mCameraCallBack) {
        this.mCameraCallBack = mCameraCallBack;

    }

    public CameraPresenter(AppCompatActivity mAppCompatActivity, SurfaceView mSurfaceView) {
        this.mAppCompatActivity = mAppCompatActivity;
        this.mSurfaceView = mSurfaceView;
      //  mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceHolder = mSurfaceView.getHolder();
        DisplayMetrics dm = new DisplayMetrics();
        mAppCompatActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //获取宽高像素
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        Log.d("sssd-手机宽高尺寸:",screenWidth +"*"+screenHeight);
        //创建文件夹目录
        setUpFile();
        init();
    }

    /**
     *
     * 人脸检测设置检测的View 矩形框
     * @param mFaceView
     */
    public void setFaceView(FaceDeteView mFaceView){
        this.mFaceView = mFaceView;
    }




    /**
     * 设置前置还是后置
     *
     * @param mCameraId 前置还是后置
     */
    public void setFrontOrBack(int mCameraId) {
        this.mCameraId = mCameraId;

    }

    /**
     * 拍照
     */
    public void takePicture(final int takePhotoOrientation) {
        if (mCamera != null) {
            //拍照回调 点击拍照时回调 写一个空实现
            mCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                }
            }, new Camera.PictureCallback() {
                //回调没压缩的原始数据
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {

                }
            }, new Camera.PictureCallback() {
                //回调图片数据 点击拍照后相机返回的照片byte数组，照片数据
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    //拍照后记得调用预览方法，不然会停在拍照图像的界面
                    mCamera.startPreview();
                    //回调
                    mCameraCallBack.onTakePicture(data, camera);
                    //保存图片
                    getPhotoPath(data,takePhotoOrientation);

                }
            });

        }
    }

    /**
     * 初始化增加回调
     */
    public void init() {
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("sssd-宽",mSurfaceView.getMeasuredWidth() + "*" +mSurfaceView.getMeasuredHeight());
                //surface创建时执行
                if (mCamera == null) {
                    openCamera(mCameraId);
                }
                //并设置预览
                startPreview();
                //新增获取系统支持视频
                getVideoSize();
                mediaRecorder = new MediaRecorder();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //surface绘制时执行
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //surface销毁时执行
                releaseCamera();
            }
        });
    }

    /**
     * 打开相机 并且判断是否支持该摄像头
     *
     * @param FaceOrBack 前置还是后置
     * @return
     */
    private boolean openCamera(int FaceOrBack) {
        //是否支持前后摄像头
        boolean isSupportCamera = isSupport(FaceOrBack);
        //如果支持
        if (isSupportCamera) {
            try {
                mCamera = Camera.open(FaceOrBack);
                initParameters(mCamera);
                //设置预览回调
                if (mCamera != null) {
                    mCamera.setPreviewCallback(this);
                }
             } catch (Exception e) {
                e.printStackTrace();
                ToastUtil.showShortToast(mAppCompatActivity, "打开相机失败~");
                return false;
            }

        }

        return isSupportCamera;
    }


    /**
     * 设置相机参数
     *
     * @param camera
     */
    private void initParameters(Camera camera) {
        try {
            //获取Parameters对象
            mParameters = camera.getParameters();
            //设置预览格式
            mParameters.setPreviewFormat(ImageFormat.NV21);
            //mParameters.setExposureCompensation(2);

            if(isFull){
                setPreviewSize(screenWidth,screenHeight);
            } else {
                setPreviewSize(mSurfaceView.getMeasuredWidth(),mSurfaceView.getMeasuredHeight());
            }

            //getOpyimalPreviewSize();

            setPictureSize();
            //连续自动对焦图像
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
                //自动对焦(单次)
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            //给相机设置参数
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShortToast(mAppCompatActivity, "初始化相机失败");
        }


    }


    /**
     *
     * 设置保存图片的尺寸
     */
    private void setPictureSize() {
        List<Camera.Size> localSizes = mParameters.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        Camera.Size previewSize = mParameters.getPreviewSize();//获取预览界面尺寸
        float previewSizeScale = 0;
        if (previewSize != null) {
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                if (biggestSize == null) {
                    biggestSize = size;
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                // 选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }

            // 如果没有选出fitSize, 那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize;
            }
            mParameters.setPictureSize(fitSize.width, fitSize.height);
        }

    }


    /**
     * 设置预览界面尺寸
     */
    public void setPreviewSize(int width,int height) {
        //获取系统支持预览大小
        List<Camera.Size> localSizes = mParameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;//最大分辨率
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();

            if(Float.valueOf(width) / height == 3.0f / 4){
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    //  Log.d("sssd-系统支持的尺寸size.width:",size.width + "*" +size.height);
                    //  Log.d("sssd-系统",1440f / 1080+"");
                    //  Log.d("sssd-系统支持的尺寸比:",Double.valueOf(size.width) / size.height+"");
                    if(Float.valueOf(size.width) / size.height == 4.0f / 3){
                        Log.d("sssd-系统支持的尺寸:","进入");
                        mParameters.setPreviewSize(size.width,size.height);
                        break;
                    }


                }
            } else {
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    Log.d("sssd-系统支持的尺寸:",size.width + "*" +size.height);
                    if (biggestSize == null ||
                            (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                        biggestSize = size;
                    }

                    //如果支持的比例都等于所获取到的宽高
                    if (size.width == height
                            && size.height == width) {
                        fitSize = size;
                        //如果任一宽或者高等于所支持的尺寸
                    } else if (size.width == height
                            || size.height == width) {
                        if (targetSize == null) {
                            targetSize = size;
                            //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                        } else if (size.width < height
                                || size.height < width) {
                            targetSiz2 = size;
                        }
                    }
                }

                if (fitSize == null) {
                    fitSize = targetSize;
                }

                if (fitSize == null) {
                    fitSize = targetSiz2;
                }

                if (fitSize == null) {
                    fitSize = biggestSize;
                }
                Log.d("sssd-最佳预览尺寸:",fitSize.width + "*" + fitSize.height);

                //mParameters.setPreviewSize(640,480);
                mParameters.setPreviewSize(fitSize.width, fitSize.height);
            }





        }
    }


    /**
     * 解决预览变形问题
     *
     *
     */
    private void getOpyimalPreviewSize(){
        List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
        int w = screenWidth;
        int h = screenHeight;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;
        for(Camera.Size size : sizes){
            double ratio = (double) size.width / size.height;
            if(Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;;
            if(Math.abs(size.height - targetHeight) < minDiff){
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }

        }


        if(optimalSize == null){
            minDiff = Double.MAX_VALUE;
            for(Camera.Size size : sizes){
                if(Math.abs(size.height - targetHeight) < minDiff){
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }

        }

        mParameters.setPreviewSize(optimalSize.width,optimalSize.height);
    }




//    private Point screenResolution;
//    private int mCameraPreviewWidth;
//    private int mCameraPreviewHeight;
//    //从底层拿camera支持的previewsize，完了和屏幕分辨率做差，diff最小的就是最佳预览分辨率
//    private void getPreviewSize(int mCameraId) {
//        try {
//            int diffs = Integer.MAX_VALUE;
//            WindowManager windowManager = (WindowManager) mAppCompatActivity.getSystemService(Context.WINDOW_SERVICE);
//            Display display = windowManager.getDefaultDisplay();
//            screenResolution = new Point(display.getWidth(), display.getHeight());
//
//            CameraCharacteristics props = mCameraManager.getCameraCharacteristics(mCameraId);
//            StreamConfigurationMap configurationMap = props.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            Size[] availablePreviewSizes = configurationMap.getOutputSizes(SurfaceTexture.class);
//
//            for (Size previewSize : availablePreviewSizes) {
//              //  Log.v(TAG, " PreviewSizes = " + previewSize);
//                mCameraPreviewWidth = previewSize.getWidth();
//                mCameraPreviewHeight = previewSize.getHeight();
//                int newDiffs = Math.abs(mCameraPreviewWidth - screenResolution.x) + Math.abs(mCameraPreviewHeight - screenResolution.y);
//              //  Log.v(TAG, "newDiffs = " + newDiffs);
//
//                if (newDiffs == 0) {
//                    bestPreviewWidth = mCameraPreviewWidth;
//                    bestPreviewHeight = mCameraPreviewHeight;
//                    break;
//                }
//                if (diffs > newDiffs) {
//                    bestPreviewWidth = mCameraPreviewWidth;
//                    bestPreviewHeight = mCameraPreviewHeight;
//                    diffs = newDiffs;
//                }
//            }
//        } catch (CameraAccessException cae) {
//
//        }
//    }



    /**
     * 变焦
     * @param zoom 缩放系数
     */
    public void setZoom(int zoom){
       if(mCamera == null){
           return;
       }
       //获取Paramters对象
       Camera.Parameters parameters;
       parameters = mCamera.getParameters();
       //如果不支持变焦
       if(!parameters.isZoomSupported()){
           return;
       }
       //
       parameters.setZoom(zoom);
       //Camera对象重新设置Paramters对象参数
       mCamera.setParameters(parameters);
       mZoom = zoom;

    }


    /**
     *
     * 返回缩放值
     * @return 返回缩放值
     */
    public int getZoom(){
        return mZoom;
    }


    /**
     * 获取最大Zoom值
     * @return zoom
     */
    public int getMaxZoom(){
        if(mCamera == null){
            return -1;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if(!parameters.isZoomSupported()){
            return -1;
        }
        return parameters.getMaxZoom() > 50 ? 50 : parameters.getMaxZoom();
    }


    /**
     * 判断是否支持对焦模式
     *
     * @return
     */
    private boolean isSupportFocus(String focusMode) {
        boolean isSupport = false;
        //获取所支持对焦模式
        List<String> listFocus = mParameters.getSupportedFocusModes();
        for (String s : listFocus) {
            //如果存在 返回true
            if (s.equals(focusMode)) {
                isSupport = true;
            }

        }
        return isSupport;
    }


    /**
     * 判断是否支持某个相机
     *
     * @param faceOrBack 前置还是后置
     * @return
     */
    private boolean isSupport(int faceOrBack) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            //返回相机信息
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == faceOrBack) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraCallBack != null) {
            mCameraCallBack.onPreviewFrame(data, camera);
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        try {
            //根据所传入的SurfaceHolder对象来设置实时预览
            mCamera.setPreviewDisplay(mSurfaceHolder);
            //调整预览角度
            setCameraDisplayOrientation(mAppCompatActivity,mCameraId,mCamera);
            mCamera.startPreview();
            //开启人脸检测
            startFaceDetect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测
     */
    private void startFaceDetect() {
        //开始人脸检测，这个要调用startPreview之后调用
//        mCamera.startFaceDetection();
//        //添加回调
//        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
//            @Override
//            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
//          //      mCameraCallBack.onFaceDetect(transForm(faces), camera);
//                mFaceView.setFace(transForm(faces));
//                Log.d("sssd", "检测到" + faces.length + "人脸");
//                for(int i = 0;i < faces.length;i++){
//                    Log.d("第"+(i+1)+"张人脸","分数"+faces[i].score+"左眼"+faces[i].leftEye+"右眼"+faces[i].rightEye+"嘴巴"+faces[i].mouth);
//                }
//            }
//        });
    }

    /**
     * 将相机中用于表示人脸矩形的坐标转换成UI页面的坐标
     *
     * @param faces 人脸数组
     * @return
     */
    private ArrayList<RectF> transForm(Camera.Face[] faces) {
        Matrix matrix = new Matrix();
        boolean mirror;
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mirror = true;
        } else {
            mirror = false;
        }
        //前置需要镜像
        if (mirror) {
            matrix.setScale(-1f, 1f);
        } else {
            matrix.setScale(1f, 1f);
        }
        //后乘旋转角度
        matrix.postRotate(Float.valueOf(orientation));
        //后乘缩放
        matrix.postScale(mSurfaceView.getWidth() / 2000f,mSurfaceView.getHeight() / 2000f);
        //再进行位移
        matrix.postTranslate(mSurfaceView.getWidth() / 2f, mSurfaceView.getHeight() / 2f);
        ArrayList<RectF> arrayList = new ArrayList<>();
        for (Camera.Face rectF : faces) {
            RectF srcRect = new RectF(rectF.rect);
            RectF dstRect = new RectF(0f, 0f, 0f, 0f);
            //通过Matrix映射 将srcRect放入dstRect中
            matrix.mapRect(dstRect, srcRect);
            arrayList.add(dstRect);
        }
        return arrayList;

    }


    /**
     * 保证预览方向正确
     *
     * @param appCompatActivity Activity
     * @param cameraId          相机Id
     * @param camera            相机
     */
    private void setCameraDisplayOrientation(AppCompatActivity appCompatActivity, int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        //rotation是预览Window的旋转方向，对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，
        //rotation=0，这时候没有旋转，当screenOrientation="landScape"时，rotation=1。
        int rotation = appCompatActivity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //计算图像所要旋转的角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        orientation = result;
        //调整预览图像旋转角度
        camera.setDisplayOrientation(result);

    }

    /**
     * 前后摄像切换
     */
    public void switchCamera() {
        //先释放资源
        releaseCamera();
        //在Android P之前 Android设备仍然最多只有前后两个摄像头，在Android p后支持多个摄像头 用户想打开哪个就打开哪个
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        //打开摄像头
        openCamera(mCameraId);
        //切换摄像头之后开启预览
        startPreview();
    }


//    这里可以找准前后摄像头的id
//    int frontIndex = -1;
//    int backIndex = -1;
//    int cameraCount = Camera.getNumberOfCameras();
//    Camera.CameraInfo info = new Camera.CameraInfo();
//        for(int cameraIndex = 0;cameraIndex < cameraCount;cameraIndex ++){
//        Camera.getCameraInfo(cameraIndex,info);
//        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
//            frontIndex = cameraIndex;
//        }else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
//            backIndex = cameraIndex;
//        }
//
//    }
//
//    //跟据传入的type来判断
//        if(type == FRONT && frontIndex != -1){
//
//        openCamera(frontIndex);
//    } else if(type == BACK && backIndex != -1){
//        openCamera(backIndex);
//
//    }


    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mHandler.removeMessages(1);
        }

        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;

        }
    }

    /**
     * 创建拍照照片文件夹
     */
    private void setUpFile() {
        //方式1 这里是app的内部存储 这里要注意 不是外部私有目录 详情请看 Configuration这个类
       // photosFile = new File(Configuration.insidePath);
        //方式2 这里改为app的外部存储 私有存储目录 /storage/emulated/0/Android/data/com.knight.cameraone/Pictures
        photosFile = mAppCompatActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!photosFile.exists() || !photosFile.isDirectory()) {
            boolean isSuccess = false;
            try {
                isSuccess = photosFile.mkdirs();
            } catch (Exception e) {
                ToastUtil.showShortToast(mAppCompatActivity, "创建存放目录失败,请检查磁盘空间~");
                mAppCompatActivity.finish();
            } finally {
                if (!isSuccess) {
                    ToastUtil.showShortToast(mAppCompatActivity, "创建存放目录失败,请检查磁盘空间~");
                    mAppCompatActivity.finish();
                }
            }

        }
    }

    /**
     * @return 返回路径
     */
    private void getPhotoPath(final byte[] data, final int takePhotoOrientation) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis();
                String time = SystemUtil.formatTime(timeMillis);
                //拍照数量+1
                photoNum++;
                //图片名字
                String name = SystemUtil.formatTime(timeMillis, SystemUtil.formatRandom(photoNum) + ".jpg");
                //创建具体文件
                File file = new File(photosFile, name);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        //将数据写入文件
                        fos.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //将图片旋转
                   // rotateImageView(mCameraId,orientation,Configuration.insidePath + file.getName());
                    rotateImageView(mCameraId,takePhotoOrientation,file.getAbsolutePath());
                    //将图片复制到外部 target SDK 设置 Android 10以下
                    //SystemUtil.copyPicture(Configuration.insidePath + file.getName(),Configuration.OUTPATH,file.getName());

                    //将图片保存到手机相册 方式1
               //     SystemUtil.saveAlbum(file.getAbsolutePath(), file.getName(), mAppCompatActivity);
                    //将图片保存到手机相册 方式2
                    ImageUtil.saveAlbum(mAppCompatActivity,file);


                    Message message = new Message();
                    message.what = 1;
                    message.obj = file.getAbsolutePath();
                    mHandler.sendMessage(message);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 旋转图片
     * @param cameraId 前置还是后置
     * @param orientation 拍照时传感器方向
     * @param path 图片路径
     */
    private void rotateImageView(int cameraId,int orientation,String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Matrix matrix = new Matrix();
        matrix.postRotate(Float.valueOf(orientation));
        // 创建新的图片
        Bitmap resizedBitmap;
//        //0是后置
//        if(cameraId == 0){
//            if(orientation == 90){
//                matrix.postRotate(90);
//            }
//        }
//        //1是前置
//        if(cameraId == 1){
//            matrix.postRotate(270);
//        }

        if(cameraId == 1){
            if(orientation == 90){
                matrix.postRotate(180f);
            }
        }
        // 创建新的图片
        resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //新增 如果是前置 需要镜面翻转处理
        if(cameraId == 1){
            Matrix matrix1 = new Matrix();
            matrix1.postScale(-1f,1f);
            resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0,
                    resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix1, true);

        }


        File file = new File(path);
        //重新写入文件
        try{
            // 写入文件
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            //默认jpg
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            resizedBitmap.recycle();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

    }

    //            if ("png".equals(imageType)) {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
//            } else {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//            }
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler(){
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mCameraCallBack.getPhotoFile(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 自动变焦
     */
    public void autoFoucus(){
        if(mCamera == null){
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        }
    }


    /**
     * 获取输出视频的width和height
     *
     */
    public void getVideoSize(){
        int biggest_width=0 ,biggest_height=0;//最大分辨率
        int fitSize_width=0,fitSize_height=0;
        int fitSize_widthBig=0,fitSize_heightBig=0;
        Camera.Parameters parameters = mCamera.getParameters();
        //得到系统支持视频尺寸
        List<Camera.Size> videoSize = parameters.getSupportedVideoSizes();
        for(int i = 0;i < videoSize.size();i++){
            int w = videoSize.get(i).width;
            int h = videoSize.get(i).height;
            if ((biggest_width == 0 && biggest_height == 0)||
                    (w >= biggest_height && h >= biggest_width)) {
                biggest_width = w;
                biggest_height = h;
            }

            if(w == screenHeight && h == screenWidth){
                width = w;
                height = h;
            }else if(w == screenHeight || h == screenWidth){
                if(width == 0 || height == 0){
                    fitSize_width = w;
                    fitSize_height = h;

                }else if(w < screenHeight || h < screenWidth){
                    fitSize_widthBig = w;
                    fitSize_heightBig = h;

                }
            }
        }

        if(width == 0 && height == 0){
            width = fitSize_width;
            height = fitSize_height;
        }

        if(width == 0 && height == 0){
            width = fitSize_widthBig;
            height = fitSize_heightBig;
        }

        if(width == 0 && height == 0){
            width = biggest_width;
            height = biggest_height;

        }
    }


    /**
     *
     * 停止录制
     */
    public void stopRecord(){
        if(mediaRecorder != null){
            mediaRecorder.release();
            mediaRecorder = null;
        }

        if(mCamera != null){
            mCamera.release();
        }
        openCamera(mCameraId);
        //并设置预览
        startPreview();
    }


    /**
     *
     * 录制方法
     */
    public void startRecord(String path,String name){
        //解锁Camera硬件
        mCamera.unlock();
        if(mediaRecorder == null){
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.setCamera(mCamera);
        //音频源 麦克风
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        //视频源 camera
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //输出格式
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //音频编码
        mediaRecorder.setAudioEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        //视频编码
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        //设置帧频率
        mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024 * 100);
        Log.d("sssd视频宽高：","宽"+width+"高"+height+"");
        mediaRecorder.setVideoSize(width,height);
        //每秒的帧数
        mediaRecorder.setVideoFrameRate(20);
        //调整视频旋转角度 如果不设置 后置和前置都会被旋转播放
        if(mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            if(orientation == 270 || orientation == 90 || orientation == 180){
                mediaRecorder.setOrientationHint(180);
            }else{
                mediaRecorder.setOrientationHint(0);
            }
        }else{
            if(orientation == 90){
                mediaRecorder.setOrientationHint(90);
            }
        }

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }
        //设置输出文件名字
        mediaRecorder.setOutputFile(path + File.separator + name + ".mp4");
        File file1 = new File(path + File.separator + name + ".mp4");
        if(file1.exists()){
            file1.delete();
        }
        //设置预览
        mediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        try {
            //准备录制
            mediaRecorder.prepare();
            //开始录制
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     *
     * 闪光灯
     * @param turnSwitch true 为开启 false 为关闭
     */
    public void turnLight(boolean turnSwitch){
        if(mCamera == null){
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters == null){
            return;
        }

        parameters.setFlashMode(turnSwitch ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
    }


    /**
     * 开启人脸检测
     *
     */
    public void turnFaceDetect(boolean isDetect){
         mFaceView.setVisibility(isDetect ?  View.VISIBLE : View.GONE);
    }


    /**
     *
     *
     * @param mSurfaceView
     */
    public void update(SurfaceView mSurfaceView){
        mSurfaceHolder = mSurfaceView.getHolder();

    }




}
