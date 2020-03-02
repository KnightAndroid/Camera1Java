package com.knight.cameraone.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.knight.cameraone.R;

/**
 * @ProjectName: Camera1Java
 * @Package: com.knight.cameraone.activity
 * @ClassName: TestActivity
 * @Description: java类作用描述
 * @Author: knight
 * @CreateDate: 2020-02-29 18:41
 * @UpdateUser: 更新者
 * @UpdateDate: 2020-02-29 18:41
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_test);
    }
}
