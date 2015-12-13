package com.github.znacloud.loadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2015/12/5.
 */
public class FourAngleLoadingView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "FourAngleLoadingView";
    private final double PI = Math.PI;
    private final double HALF_PI = 0.5*Math.PI;

    private final int DEFAULT_WIDTH = 350;
    private final int DEFAULT_HEIGHT = 350;

    private final int BALL_COUNT = 40;
    private final float BASE_SIZE = 0.1f;
    private final float DIV_SIZE = 5f;

    //speed enum
    private final int SLOW = 1;
    private final int NORMAL = 2;
    private final int FAST = 4;

    private int mSpeed = NORMAL;

    //four colors
    private int mFirstColor = Color.RED;
    private int mSecondColor = Color.BLUE;
    private int mThirdColor = Color.YELLOW;
    private int mFouthColor = Color.GREEN;

    private int mWidth = DEFAULT_WIDTH;
    private int mHeight = DEFAULT_HEIGHT;

    private long tick = 0L;
    private DrawThread mDrawThread;
    private Paint mPaint;


    public FourAngleLoadingView(Context context) {
        this(context, null);
    }

    public FourAngleLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FourAngleLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray a =  context.obtainStyledAttributes(attrs,R.styleable.FourAngleLoadingView,defStyleAttr,0);
        mFirstColor = a.getColor(R.styleable.FourAngleLoadingView_firstColor,Color.RED);
        mSecondColor = a.getColor(R.styleable.FourAngleLoadingView_secondColor,Color.BLUE);
        mThirdColor = a.getColor(R.styleable.FourAngleLoadingView_thirdColor,Color.YELLOW);
        mFouthColor = a.getColor(R.styleable.FourAngleLoadingView_fouthColor,Color.GREEN);

        mSpeed = a.getInt(R.styleable.FourAngleLoadingView_speed,NORMAL);
        a.recycle();
    }

    private void init() {
        Log.d(TAG,"init loading view");

        getHolder().addCallback(this);
        mDrawThread = new DrawThread(getHolder());
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if(widthSpecMode == MeasureSpec.AT_MOST){
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, MeasureSpec.EXACTLY);
        }
        if(heightSpecMode == MeasureSpec.AT_MOST){
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
        mWidth = mHeight = Math.min(mWidth,mHeight);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG,"surface created");
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG,"surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG,"surface destroyed");
        if(mDrawThread != null) {
            mDrawThread.close();
            mDrawThread.interrupt();
        }
    }



    private class DrawThread extends Thread {
        private volatile boolean isRun = true;
        private final SurfaceHolder holder;

        public DrawThread(SurfaceHolder pHolder) {
            holder = pHolder;
        }

        @Override
        public void run() {
//            int mTimeValue = 0;
            Canvas canvas = null;
            while (isRun) {
                Log.d(TAG,"draw view "+ tick);

                try {
                    canvas = holder.lockCanvas();
                    canvas.translate(getWidth() / 2, getHeight() / 2);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);


                    float angle = tick/16f;
                    float radius = (-50 + (float) Math.sin(tick / 30f) * 100f) * mWidth / DEFAULT_WIDTH;
                    float size = 0;

                    for(int i = 0;i<BALL_COUNT;i++){
                        angle += PI / 64f;
                        radius += i / 30f;
                        size = BASE_SIZE + i /DIV_SIZE;

                        //draw first angle
                        mPaint.setColor(mFirstColor);
                        canvas.drawCircle((float)(Math.cos(angle)*radius),
                                          (float)(Math.sin(angle)*radius), size,mPaint);

                        //draw second angle
                        mPaint.setColor(mSecondColor);
                        canvas.drawCircle((float)(Math.cos(angle)*(-radius)),
                                (float)(Math.sin(angle)*(-radius)), size,mPaint);

                        //draw third angle
                        mPaint.setColor(mThirdColor);
                        canvas.drawCircle((float)(Math.cos(angle+HALF_PI)*radius),
                                (float)(Math.sin(angle+HALF_PI)*radius), size,mPaint);

                        //draw FOURTH angle
                        mPaint.setColor(mFouthColor);
                        canvas.drawCircle((float)(Math.cos(angle+HALF_PI)*(-radius)),
                                (float)(Math.sin(angle+HALF_PI)*(-radius)), size,mPaint);
                    }
                    holder.unlockCanvasAndPost(canvas);
                    Log.d(TAG, "draw view " + tick + " end");
                    sleep(20);
                    tick += mSpeed;

                } catch (Exception e) {
                    Log.e(TAG, "exception =>" + e.getMessage());
                    isRun = false;
                }
            }

        }

        public void close(){
            isRun = false;
        }
    }
}
