package com.knight.cameraone.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

/**
 * @author created by luguian
 * @organize
 * @Date 2019/10/11 13:54
 * @descript:
 */

public class FaceDeteView extends View {

    private Paint mPaint;
    private String mColor = "#42ed45";
    private ArrayList<RectF> mFaces = null;
    public FaceDeteView(Context context) {
        super(context);
        init(context);
    }

    public FaceDeteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceDeteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }



    private void init(Context context){
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(mColor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,1f,context.getResources().getDisplayMetrics()));
        mPaint.setAntiAlias(true);
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(mFaces != null){
            for(RectF face:mFaces){
                canvas.drawRect(face,mPaint);
            }

        }
    }


    /**
     * 设置人人脸信息
     */
    public void setFace(ArrayList<RectF> mFaces){
       this.mFaces = mFaces;
       invalidate();
    }








}
