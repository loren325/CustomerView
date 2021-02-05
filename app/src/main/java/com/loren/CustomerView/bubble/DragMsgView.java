package com.loren.CustomerView.bubble;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.loren.CustomerView.R;

public class DragMsgView extends AppCompatTextView {

    private DragDotView mDragDotView;
    private float mWidth, mHeight;
    private OnDragListener mDragListener;

    public DragMsgView(Context context) {
        this(context, null);
    }

    public DragMsgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //获得根View
        View rootView = getRootView();
        //获得触摸位置在全屏所在位置
        float mRawX = event.getRawX();
        float mRawY = event.getRawY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                //获得当前View在屏幕上的位置
                int[] cLocation = new int[2];
                getLocationOnScreen(cLocation);

                if(rootView instanceof ViewGroup){
                    mDragDotView = new DragDotView(getContext());

                    //设置固定圆和移动圆的圆心坐标
                    mDragDotView.setDragPoint(cLocation[0] + mWidth / 2, cLocation[1] + mHeight / 2, mRawX, mRawY);

                    Bitmap bitmap = getBitmapFromView(this);
                    if(bitmap != null){
                        mDragDotView.setCacheBitmap(bitmap);
                        ((ViewGroup) rootView).addView(mDragDotView);
                        setVisibility(INVISIBLE);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if(mDragDotView != null){
                    mDragDotView.move(mRawX, mRawY);
                }
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                if(mDragDotView != null){
                    mDragDotView.up();
                }
                break;
        }
        return true;
    }

    /**
     * 将当前view缓存为bitmap，拖动的时候直接绘制此bitmap
     * @param view
     * @return
     */
    public Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public class DragDotView extends View {

        // 气泡默认状态--静止
        private final int BUBBLE_STATE_DEFAULT = 0;
        // 气泡相连
        private final int BUBBLE_STATE_CONNECT = 1;
        // 气泡分离
        private final int BUBBLE_STATE_APART = 2;
        // 气泡消失
        private final int BUBBLE_STATE_DISMISS = 3;

        // 气泡半径
        private float mBubbleRadius = dpToPx(10);
        // 气泡颜色
        private int mBubbleColor;

        // 不动气泡的半径
        private float mBubbleStillRadius;
        // 可动气泡的半径
        private float mBubbleMoveRadius;
        // 不动气泡的圆心
        private PointF mBubStillCenter;
        // 可动气泡的圆心
        private PointF mBubMoveCenter;

        // 气泡的画笔
        private Paint mBubblePaint;
        // 贝塞尔曲线
        private Path mBezierPath;

        private Paint mBurstPaint;
        private Rect mBurstRect;

        // 气泡状态标志
        private int mBubbleState = BUBBLE_STATE_DEFAULT;
        // 两气泡圆心距离
        private float mDist;
        // 气泡相连状态最大圆心距离
        private float mMaxDist;
        // 手指触摸偏移量
        private float MOVE_OFFSET;

        private Bitmap mCacheBitmap;
        // View的宽和高
        private float mWidth, mHeight;

        // 气泡爆炸的bitmap数组
        private Bitmap[] mBurstBitmapArray;
        // 是否在执行气泡爆炸动画
        private boolean mIsBurstAnimStart = false;
        // 当前气泡爆炸图片index
        private int mCurDrawableIndex;
        // 气泡爆炸的图片id数组
        private final int[] mBurstDrawablesArray = {R.drawable.burst_1, R.drawable.burst_2
                , R.drawable.burst_3, R.drawable.burst_4, R.drawable.burst_5};

        public DragDotView(Context context) {
            this(context, null);
        }

        public DragDotView(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DragDotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public DragDotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            init(context, attrs, defStyleAttr);
        }

        private void init(Context context, AttributeSet attrs, int defStyleAttr){
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView, defStyleAttr, 0);
            mBubbleColor = array.getColor(R.styleable.DragBubbleView_bubble_color, Color.RED);
            array.recycle();

            // 抗锯齿
            mBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBubblePaint.setColor(mBubbleColor);
            mBubblePaint.setStyle(Paint.Style.FILL);

            mBezierPath = new Path();

            mBurstPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBurstPaint.setFilterBitmap(true);
            mBurstRect = new Rect();
            mBurstBitmapArray = new Bitmap[mBurstDrawablesArray.length];
            for (int i = 0; i < mBurstDrawablesArray.length; i++) {
                // 将气泡爆炸的drawable转为bitmap
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mBurstDrawablesArray[i]);
                mBurstBitmapArray[i] = bitmap;
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if(mDist < mMaxDist && mBubbleState == BUBBLE_STATE_CONNECT){
                // 画静止的气泡
                canvas.drawCircle(mBubStillCenter.x, mBubStillCenter.y, mBubbleStillRadius, mBubblePaint);

                // 计算控制点坐标，两圆心的中点
                int controlX = (int) ((mBubStillCenter.x + mBubMoveCenter.x) / 2);
                int controlY = (int) ((mBubStillCenter.y + mBubMoveCenter.y) / 2);

                float sin = (mBubMoveCenter.y - mBubStillCenter.y) / mDist;
                float cos = (mBubMoveCenter.x - mBubStillCenter.x) / mDist;

                // 按照图示的位置，此时移动气泡的y坐标比固定气泡的y坐标小，所以sin是负值，故在使用sin值的时候使用加号计算
                // A点
                float bubbleStillStartX = mBubStillCenter.x + mBubbleStillRadius * sin;
                float bubbleStillStartY = mBubStillCenter.y - mBubbleStillRadius * cos;
                // B点
                float bubbleMoveStartX = mBubMoveCenter.x + mBubbleMoveRadius * sin;
                float bubbleMoveStartY = mBubMoveCenter.y - mBubbleMoveRadius * cos;
                // C点
                float bubbleMoveEndX = mBubMoveCenter.x - mBubbleMoveRadius * sin;
                float bubbleMoveEndY = mBubMoveCenter.y + mBubbleMoveRadius * cos;
                // D点
                float bubbleStillEndX = mBubStillCenter.x - mBubbleStillRadius * sin;
                float bubbleStillEndY = mBubStillCenter.y + mBubbleStillRadius * cos;

                mBezierPath.reset();
                // 画上半弧
                mBezierPath.moveTo(bubbleStillStartX, bubbleStillStartY);
                mBezierPath.quadTo(controlX, controlY, bubbleMoveStartX, bubbleMoveStartY);
                // 画下半弧
                mBezierPath.lineTo(bubbleMoveEndX, bubbleMoveEndY);
                mBezierPath.quadTo(controlX, controlY, bubbleStillEndX, bubbleStillEndY);
                mBezierPath.close();
                canvas.drawPath(mBezierPath, mBubblePaint);
            }

            // 绘制拖动的view，也就是缓存的bitmap
            if (mCacheBitmap != null && mBubbleState != BUBBLE_STATE_DISMISS) {
                canvas.drawBitmap(mCacheBitmap,
                        mBubMoveCenter.x - mWidth / 2,
                        mBubMoveCenter.y - mHeight / 2,
                        mBubblePaint);
            }

            if(mBubbleState == BUBBLE_STATE_DISMISS){
                if(mIsBurstAnimStart){
                    mBurstRect.set((int)(mBubMoveCenter.x - mBubbleMoveRadius), (int)(mBubMoveCenter.y - mBubbleMoveRadius),
                            (int)(mBubMoveCenter.x + mBubbleMoveRadius), (int)(mBubMoveCenter.y + mBubbleMoveRadius));
                    canvas.drawBitmap(mBurstBitmapArray[mCurDrawableIndex], null, mBurstRect, mBurstPaint);
                }
            }
        }

        /**
         * 气泡爆炸动画
         */
        private void startBubbleBurstAnim() {
            ValueAnimator animator = ValueAnimator.ofInt(0, mBurstDrawablesArray.length);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                mCurDrawableIndex = (int) animator.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsBurstAnimStart = false;
                    if(mDragListener != null){
                        mDragListener.onDismiss();
                    }
                }
            });
            animator.start();
        }

        /**
         * 气泡还原动画
         */
        private void startBubbleRestAnim() {
            mBubbleStillRadius = mBubbleRadius;
            ValueAnimator animator = ValueAnimator.ofObject(new PointEvaluator(), new PointF(mBubMoveCenter.x, mBubMoveCenter.y), new PointF(mBubStillCenter.x, mBubStillCenter.y));
            animator.setDuration(200);
            animator.setInterpolator(input -> {
                float factor = 0.4f;
                return (float) (Math.pow(2, -10 * factor) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
            });
            animator.addUpdateListener(animation -> {
                mBubMoveCenter = (PointF) animation.getAnimatedValue();
                invalidate();
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBubbleState = BUBBLE_STATE_DEFAULT;
                    removeDragView();
                    if(mDragListener != null){
                        mDragListener.onRestore();
                    }
                }
            });
            animator.start();
        }

        /**
         * 设置缓存的bitmap
         * @param bitmap
         */
        public void setCacheBitmap(Bitmap bitmap){
            this.mCacheBitmap = bitmap;
            mWidth = mCacheBitmap.getWidth();
            mHeight = mCacheBitmap.getHeight();
            mBubbleRadius = Math.min(mWidth, mHeight) / 2;
        }

        /**
         * 设置固定圆和移动圆的圆心和半径
         * @param stillX 固定圆的X坐标
         * @param stillY 固定圆的Y坐标
         * @param moveX 移动圆的X坐标
         * @param moveY 移动圆的Y坐标
         */
        public void setDragPoint(float stillX,
                                   float stillY,
                                   float moveX,
                                   float moveY) {
            mBubbleStillRadius = mBubbleRadius;
            mBubbleMoveRadius = mBubbleStillRadius;
            mMaxDist = mBubbleRadius * 8;
            MOVE_OFFSET = mMaxDist / 4;
            if(mBubStillCenter == null){
                mBubStillCenter = new PointF(stillX, stillY);
            }else {
                mBubStillCenter.set(stillX, stillY);
            }
            if(mBubMoveCenter == null){
                mBubMoveCenter = new PointF(moveX, moveY);
            }else {
                mBubMoveCenter.set(moveX, moveY);
            }
            mBubbleState = BUBBLE_STATE_CONNECT;
            invalidate();
        }

        public void move(float curX, float curY) {
            mBubMoveCenter.x = curX;
            mBubMoveCenter.y = curY;
            // Math.hypot(x, y)为求x平方+y平方的平方根
            mDist = (float) Math.hypot(curX - mBubStillCenter.x, curY - mBubStillCenter.y);
            if(mBubbleState == BUBBLE_STATE_CONNECT){
                if(mDist < mMaxDist - MOVE_OFFSET){
                    mBubbleStillRadius = mBubbleRadius - mDist / 10;
                }else {
                    mBubbleState = BUBBLE_STATE_APART;
                }
            }
            invalidate();
        }

        public void up() {
            if(mBubbleState == BUBBLE_STATE_CONNECT){
                startBubbleRestAnim();
            }else if(mBubbleState == BUBBLE_STATE_APART){
                if(mDist < 3 * mBubbleRadius){
                    startBubbleRestAnim();
                }else {
                    mBubbleState = BUBBLE_STATE_DISMISS;
                    mIsBurstAnimStart = true;
                    startBubbleBurstAnim();
                }
            }
            invalidate();
        }

        /**
         * 移除dragview
         */
        private void removeDragView() {
            ViewGroup viewGroup = (ViewGroup) getParent();
            viewGroup.removeView(DragDotView.this);
            DragMsgView.this.setVisibility(VISIBLE);
        }

        /**
         * 转换 dp 至 px
         *
         * @param dp dp像素
         * @return
         */
        protected int dpToPx(float dp) {
            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
            return (int) (dp * metrics.density + 0.5f);
        }
    }

    public interface OnDragListener{
        void onRestore();
        void onDismiss();
    }

    public void setOnDragListener(OnDragListener listener){
        this.mDragListener = listener;
    }
}
