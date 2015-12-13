package com.github.znacloud.loadingview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Stephan on 2015/12/12.
 */
public class ColorfuleProgressBar extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "ColorfulProgressBar";

    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;
    private final int MIN_WIDTH = (int) (300 * DENSITY);
    private final int MIN_HEIGHT = (int) (100 * DENSITY);
    private final int DEFAULT_BAR_HEIGHT = (int) (12 * DENSITY);
    private final int DEFAULT_BAR_WIDTH = (int) (270 * DENSITY);

    private final int PARTICLE_RATE = 7;
    private final int HUE_START = 0;
    private final int HUE_END = 120;
    private final int GRAVITY = (int) (1.4 * DENSITY);

    private int mWidth = MIN_WIDTH;
    private int mHeight = MIN_HEIGHT;

    private int mBarWidth = DEFAULT_BAR_WIDTH;
    private int mBarHeight = DEFAULT_BAR_HEIGHT;

    private Rect mBgRect, mFgRect;

    private DrawThread mDrawThread;
    private Paint mPaint;
    private long tick = 0L;

    private float mLoaded = 0f;

    private ArrayList<Particle> mParticles = new ArrayList<>(PARTICLE_RATE);

    private int hue = 0;


    public ColorfuleProgressBar(Context context) {
        this(context, null);
    }

    public ColorfuleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorfuleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FourAngleLoadingView, defStyleAttr, 0);

        a.recycle();
    }

    private void init(Context pContext) {
        Log.d(TAG, "init loading view");

        getHolder().addCallback(this);
        mDrawThread = new DrawThread(getHolder());
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

//        高度固定为20dp
//        mBarWidth = (int)(pContext.getResources().getDisplayMetrics().density*20);

        mBgRect = new Rect((mWidth - mBarWidth) / 2, (mHeight - mBarHeight) / 2,
                (mWidth - mBarWidth) / 2 + mBarWidth, (mHeight - mBarHeight) / 2 + mBarHeight);

        mFgRect = new Rect((mWidth - mBarWidth) / 2, (mHeight - mBarHeight) / 2,
                (mWidth - mBarWidth) / 2 + (int) (mLoaded / 100f * mBarWidth), (mHeight - mBarHeight) / 2 + mBarHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(MIN_WIDTH, MeasureSpec.EXACTLY);
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MIN_HEIGHT, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = Math.max(MIN_WIDTH, getWidth());
        mHeight = Math.max(MIN_HEIGHT, getHeight());
        mBarWidth = (int) (0.8f * mWidth);
//        mBarHeight = (int)(0.2f*mHeight);
        mBgRect = new Rect((mWidth - mBarWidth) / 2, (mHeight - mBarHeight) / 2,
                (mWidth - mBarWidth) / 2 + mBarWidth, (mHeight - mBarHeight) / 2 + mBarHeight);
        mFgRect = new Rect((mWidth - mBarWidth) / 2, (mHeight - mBarHeight) / 2,
                (mWidth - mBarWidth) / 2 + (int) (mLoaded / 100f * mBarWidth), (mHeight - mBarHeight) / 2 + mBarHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface created");
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface destroyed");
        if (mDrawThread != null) {
            mDrawThread.close();
            mDrawThread.interrupt();
        }
    }


    /**
     * 返回start-end之间的随机数
     *
     * @param start 起始值
     * @param end   结束值
     * @return
     */
    private float rand(float start, float end) {
        return (float) (Math.random() * (end - start) + start);
    }

    /**
     * 判断两颗粒子是否相撞
     *
     * @param x1 粒子1的x坐标
     * @param y1 粒子1的y坐标
     * @param w1 粒子1的宽度
     * @param h1 粒子1的高度
     * @param x2 粒子2的x坐标
     * @param y2 粒子2的y坐标
     * @param w2 粒子2的宽度
     * @param h2 粒子2的高度
     * @return
     */
    private boolean hitTest(float x1, float y1, float w1, float h1,
                            float x2, float y2, float w2, float h2) {
        return !(x1 + w1 < x2 || x2 + w2 < x1 || y1 + h1 < y2 || y2 + h2 < y1);
    }

    /**
     * 设置进度条进度
     *
     * @param progress 0-100的值
     */
    public void setProgress(int progress) {
        if (progress > 100) {
            mLoaded = 100;
        } else if (progress < 0) {
            mLoaded = 0;
        } else {
            mLoaded = progress;
        }
    }

    private void createParticles() {
        int n = PARTICLE_RATE;
        for (int i = 0; i < n; i++) {
            mParticles.add(new Particle());
        }
    }

    private void updateParticles() {
        int length = mParticles.size();
        for (int i = length - 1; i >= 0; i--) {
            mParticles.get(i).update(i);
        }
    }

    private void drawParticles(Canvas pCanvas) {
        for (Particle particle : mParticles) {
            particle.render(pCanvas);
        }
    }

    private void drawProgressBar(Canvas pCanvas) {
        //背景
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.LTGRAY);
        pCanvas.drawRect(mBgRect, mPaint);

        //前景
        hue = HUE_START + (int) (mLoaded / 100f * (HUE_END - HUE_START));
        mFgRect.set((mWidth - mBarWidth) / 2, (mHeight - mBarHeight) / 2,
                (mWidth - mBarWidth) / 2 + (int) (mLoaded / 100f * mBarWidth), (mHeight - mBarHeight) / 2 + mBarHeight);
        mPaint.setColor(Color.HSVToColor(new float[]{hue, 1f, 0.95f}));
        pCanvas.drawRect(mFgRect, mPaint);

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
//            createParticles();
            while (isRun) {
                Log.d(TAG, "draw view " + tick);
                //每次创建4科粒子
                createParticles();
                try {
                    canvas = holder.lockCanvas();
//                    canvas.translate(getWidth() / 2, getHeight() / 2);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    drawCanvas(canvas);
                    updateParticles();

                    holder.unlockCanvasAndPost(canvas);
                    Log.d(TAG, "draw view " + tick + " end");
                    sleep(20);
                    tick += 1;

                } catch (Exception e) {
                    Log.e(TAG, "exception =>" + e.getMessage());
                    e.printStackTrace();
                    isRun = false;
                }
            }

        }

        private void drawCanvas(Canvas pCanvas) {
            drawProgressBar(pCanvas);
            drawParticles(pCanvas);
        }

        public void close() {
            isRun = false;
        }
    }

    private class Particle {
        int x, y, vx, vy, width, height;
        float huep;

        public Particle() {
            x = (mWidth - mBarWidth) / 2 + (int) (mLoaded / 100f * mBarWidth - rand(0, 3 * DENSITY));
            y = mHeight / 2 + (int) (rand(0, mBarHeight)) - mBarHeight / 2;
            vx = (int) ((-2 + rand(0, 4)) * 0.18 * DENSITY);
            vy = (int) ((-12 + rand(0, 6)) * DENSITY);//(int)rand(0,PARTICLE_LIFT) - 2*PARTICLE_LIFT;
            width = (int) (rand(1, 3) * DENSITY);
            height = (int) (rand(1, 3) * DENSITY);
            huep = hue;
        }

        void update(int index) {
            vx += (int) ((-3 + rand(0, 6)) * 0.18 * DENSITY);
            vy += GRAVITY;
            x += vx;
            y += vy;

            if (y > (mHeight + mBarHeight) / 2 + 30 * DENSITY) {
                mParticles.remove(Particle.this);
            }
        }

        void render(Canvas pCanvas) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.HSVToColor((int) (rand(20, 100) / 100f * 255),
                    new float[]{huep, 1, rand(70, 90) / 100f}));
            pCanvas.drawRect(x, y, x + width, y + height, mPaint);
        }


    }
}
