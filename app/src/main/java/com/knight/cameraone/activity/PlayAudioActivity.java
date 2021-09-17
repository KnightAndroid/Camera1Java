package com.knight.cameraone.activity;


import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.knight.cameraone.R;



public class PlayAudioActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{


    private SurfaceView sf_play;
    private MediaPlayer player;
    private int surfaceWidth;
    private int surfaceHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playaudio);
        sf_play = findViewById(R.id.sf_play);
        //下面开始实例化MediaPlayer对象
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        //设置数据源，也就是播放文件地址，可以是网络地址
        sf_play.post(new Runnable() {
            @Override
            public void run() {
                surfaceWidth = sf_play.getWidth();
                surfaceHeight = sf_play.getHeight();
            }
        });

        String dataPath = getIntent().getStringExtra("videoPath");
    //    String dataPath = Configuration.OUTPATH + "/videomp4";
        try {
            player.setDataSource(dataPath);


        } catch (Exception e) {
            e.printStackTrace();
        }

        sf_play.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //将播放器和SurfaceView关联起来
                player.setDisplay(holder);

                //异步缓冲当前视频文件，也有一个同步接口
                player.prepareAsync();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                changeVideoSize();
            }
        });

    }

    /**
     *
     * 设置循环播放
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        player.start();
        player.setLooping(true);
    }

    /**
     * 准备播放
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
    }


    public void changeVideoSize() {
        int videoWidth = player.getVideoWidth();
        int videoHeight = player.getVideoHeight();

        //根据视频尺寸去计算->视频可以在sufaceView中放大的最大倍数。
        float max;
        if (getResources().getConfiguration().orientation== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            //竖屏模式下按视频宽度计算放大倍数值
            max = Math.max((float) videoWidth / (float) surfaceWidth,(float) videoHeight / (float) surfaceHeight);
        } else{
            //横屏模式下按视频高度计算放大倍数值
            max = Math.max(((float) videoWidth/(float) surfaceHeight),(float) videoHeight/(float) surfaceWidth);
        }

        //视频宽高分别/最大倍数值 计算出放大后的视频尺寸
        videoWidth = (int) Math.ceil((float) videoWidth / max);
        videoHeight = (int) Math.ceil((float) videoHeight / max);

        //无法直接设置视频尺寸，将计算出的视频尺寸设置到surfaceView 让视频自动填充。
        ConstraintLayout.LayoutParams sfPlayLayoutParams = (ConstraintLayout.LayoutParams) sf_play.getLayoutParams();
        sfPlayLayoutParams.height = videoHeight;
        sfPlayLayoutParams.width = videoWidth;
        sf_play.setLayoutParams(sfPlayLayoutParams);
    }

    /**
     * 释放资源
     *
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(player != null){
            player.reset();
            player.release();
            player = null;

        }
    }


}
