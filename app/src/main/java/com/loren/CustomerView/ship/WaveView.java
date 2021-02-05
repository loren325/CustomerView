package com.loren.CustomerView.ship;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.loren.CustomerView.R;


public class WaveView extends View {

    private Paint mPaint;
    private Path mPath;
    // 水波长度
    private int waveLength = 800;
    // 水波高度
    private int waveHeight = 150;
    private int mHeight;
    private int halfWaveLength = waveLength / 2;
    private float mDeltaX;
    private Bitmap mBitMap;
    private PathMeasure mPathMeasure;
    private Matrix mMatrix;
    private float mDistance;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.sea_blue));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);

        mPath = new Path();

        mMatrix = new Matrix();
        mPathMeasure = new PathMeasure();

        Options opts = new Options();
        opts.inSampleSize = 3;
        mBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.ship, opts);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;

        startAnim();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWave(canvas);

        drawBitmap(canvas);

    }

    /**
     * 绘制水波
     * @param canvas
     */
    private void drawWave(Canvas canvas){
        mPath.reset();
        mPath.moveTo(0 - mDeltaX, mHeight / 2);
        for (int i = 0; i <= getWidth() + waveLength; i += waveLength) {
            mPath.rQuadTo(halfWaveLength / 2, waveHeight, halfWaveLength, 0);
            mPath.rQuadTo(halfWaveLength / 2, -waveHeight, halfWaveLength, 0);
        }
        mPath.lineTo(getWidth() + waveLength, getHeight());
        mPath.lineTo(0, getHeight());
        mPath.close();

        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 绘制小船
     * @param canvas
     */
    private void drawBitmap(Canvas canvas) {
        mPathMeasure.setPath(mPath, false);
        mMatrix.reset();
        mPathMeasure.getMatrix(mDistance, mMatrix, PathMeasure.TANGENT_MATRIX_FLAG | PathMeasure.POSITION_MATRIX_FLAG);
        mMatrix.preTranslate(- mBitMap.getWidth() / 2, - mBitMap.getHeight());
        canvas.drawBitmap(mBitMap,mMatrix,mPaint);
    }

    /**
     * 水波平移动画
     */
    private void startAnim(){
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(animation -> {
            mDeltaX = waveLength * ((float) animation.getAnimatedValue());

            mDistance = (getWidth() + waveLength + halfWaveLength) * ((float)animation.getAnimatedValue());
            postInvalidate();
        });
        animator.setDuration(13000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

}
