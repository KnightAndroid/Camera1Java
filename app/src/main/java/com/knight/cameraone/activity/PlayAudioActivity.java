package com.knight.cameraone.activity;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.knight.cameraone.R;



public class PlayAudioActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener{


    private SurfaceView sf_play;
    private MediaPlayer player;

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
