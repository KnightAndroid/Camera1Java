package com.knight.cameraone;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.knight.cameraone.utils.SystemUtil;
import com.knight.cameraone.utils.ThreadPoolUtil;
import com.knight.cameraone.utils.ToastUtil;

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

    //摄像头方向 默认后置
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


    public interface CameraCallBack {
        //第一帧
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
        mSurfaceHolder = mSurfaceView.getHolder();
        DisplayMetrics dm = new DisplayMetrics();
        mAppCompatActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //获取宽高
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        //创建文件夹目录
        setUpFile();
        init();
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
    public void takePicture() {
        if (mCamera != null) {
            //拍照回调
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
                //回调图片数据
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mCamera.startPreview();
                    mCameraCallBack.onTakePicture(data, camera);
                    getPhotoPath(data);

                }
            });

        }
    }

    /**
     * 初始化
     */
    private void init() {
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //surface创建时执行
                if (mCamera == null) {
                    openCamera(mCameraId);
                }
                //并设置预览
                startPreview();
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
        boolean isSupportCamera = isSupport(FaceOrBack);
        //如果支持
        if (isSupportCamera) {
            try {
                mCamera = Camera.open(FaceOrBack);
                initParameters(mCamera);
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
            mParameters = camera.getParameters();
            mParameters.setPreviewFormat(ImageFormat.NV21);
            setPreviewSize();
            setPictureSize();
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
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
     * 获取与指定宽高相等或者最接近的尺寸
     * 设置保存图片的尺寸
     */
    private void setPictureSize() {
        List<Camera.Size> localSizes = mParameters.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        Camera.Size previewSize = mParameters.getPreviewSize();
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
    private void setPreviewSize() {
        List<Camera.Size> localSizes = mParameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                if (biggestSize == null ||
                        (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                    biggestSize = size;
                }

                if (size.width == screenHeight
                        && size.height == screenWidth) {
                    fitSize = size;
                } else if (size.width == screenHeight
                        || size.height == screenWidth) {
                    if (targetSize == null) {
                        targetSize = size;
                    } else if (size.width < screenHeight
                            || size.height < screenWidth) {
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
            mParameters.setPreviewSize(fitSize.width, fitSize.height);
        }
    }


    /**
     * 变焦
     * @param zoom
     */
    public void setZoom(int zoom){
       if(mCamera == null){
           return;
       }
       Camera.Parameters parameters;
       parameters = mCamera.getParameters();
       //不支持变焦
       if(!parameters.isZoomSupported()){
           return;
       }
       parameters.setZoom(zoom);
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
            mCamera.setPreviewDisplay(mSurfaceHolder);
            setCameraDisplayOrientation(mAppCompatActivity,mCameraId,mCamera);
            mCamera.startPreview();
            startFaceDetect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸检测
     */
    private void startFaceDetect() {
        mCamera.startFaceDetection();
        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                mCameraCallBack.onFaceDetect(transForm(faces), camera);
                Log.d("sssd", "检测到" + faces.length + "人脸");
            }
        });
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
        if (mirror) {
            matrix.setScale(-1f, 1f);
        } else {
            matrix.setScale(1f, 1f);
        }
        matrix.postRotate(Float.valueOf(orientation));
        matrix.postScale(mSurfaceView.getWidth() / 2f, mSurfaceView.getHeight() / 2f);
        ArrayList<RectF> arrayList = new ArrayList<>();
        for (Camera.Face rectF : faces) {
            RectF srcRect = new RectF(rectF.rect);
            RectF dstRect = new RectF(0f, 0f, 0f, 0f);
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
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        orientation = result;
        camera.setDisplayOrientation(result);

    }

    /**
     * 前后摄像投切换
     */
    public void switchCamera() {
        releaseCamera();
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        openCamera(mCameraId);
        startPreview();
    }


    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mHandler.removeMessages(1);
        }
    }

    /**
     * 创建拍照照片文件夹
     */
    private void setUpFile() {
        photosFile = new File(Configuration.insidePath);
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
    private void getPhotoPath(final byte[] data) {
        ThreadPoolUtil.execute(new Runnable() {
            @Override
            public void run() {
                long timeMillis = System.currentTimeMillis();
                String time = SystemUtil.formatTime(timeMillis);
                photoNum++;
                String name = SystemUtil.formatTime(timeMillis, SystemUtil.formatTime(photoNum) + ".jpg");
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
                    rotaeImageView(mCameraId,orientation,Configuration.insidePath + file.getName());
                    //将图片保存到内部
                    SystemUtil.saveAlbum(Configuration.insidePath + file.getName(), file.getName(), mAppCompatActivity);
                    //将图片复制到外部
                    SystemUtil.coptPicture(Configuration.insidePath + file.getName(),Configuration.OUTPATH,file.getName());
                    Message message = new Message();
                    message.what = 1;
                    message.obj = Configuration.insidePath + file.getName();
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
    private void rotaeImageView(int cameraId,int orientation,String path){
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        //后转
        if(cameraId == 1){
            if(orientation == 90){
                matrix.postRotate(90);
            }
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        File file = new File(path);
        //重新写入文件
        try{
            // 写入文件
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            //默认jpg
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            if ("png".equals(imageType)) {
//                bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
//            } else {
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
//            }

            fos.flush();
            fos.close();
            resizedBitmap.recycle();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

    }
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

}
