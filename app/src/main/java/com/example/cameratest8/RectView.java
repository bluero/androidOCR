package com.example.cameratest8;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

public class RectView extends SurfaceView implements Callback {
    protected SurfaceHolder sh = this.getHolder();
    private float moveY;
    private float moveX;
    private int mWidth;
    private int mHeight;
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    void setAspectRatio(int width, int height)
    {
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }
//    @Override
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
//    {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec)*6/8;
//        int height = MeasureSpec.getSize(heightMeasureSpec)*6/8;
//        if (0 == mRatioWidth || 0 == mRatioHeight)
//        {
//            setMeasuredDimension(width, height);
//        }
//        else
//        {
//            if (width < height * mRatioWidth / mRatioHeight)
//            {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//            }
//            else
//            {
//                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//            }
//        }
//    }

    public RectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.sh.addCallback(this);
        this.sh.setFormat(-2);
        this.setZOrderOnTop(true);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
        this.mWidth = w;
        this.mHeight = h;
    }

    public void surfaceCreated(SurfaceHolder arg0) {
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
    }

    void clearDraw() {
        Canvas canvas = this.sh.lockCanvas();
        canvas.drawColor(-16776961);
        this.sh.unlockCanvasAndPost(canvas);
    }

    public void drawLine() {
        float scaleX = (float) getWidth() / (float) mWidth;
        float scaleY = (float) getHeight() / (float) mHeight;
        Canvas canvas = this.sh.lockCanvas();
        canvas.drawColor(0);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.WHITE);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(8);
        canvas.drawRect(135,180,946,1260,p);
        canvas.drawRect(0,0,4000,4000,p);
        p.setColor(-65536);
        p.setStyle(Style.FILL);
        canvas.drawRect(0,0,3000,1440,p);
        this.sh.unlockCanvasAndPost(canvas);

    }

    public void drawFrame(int a,int b,int c,int d) {
        Canvas canvas = this.sh.lockCanvas();
        canvas.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.WHITE);
        p.setStyle(Style.FILL);
        p.setStrokeWidth(8);
        canvas.drawRect(a,b,c,d,p);
        this.sh.unlockCanvasAndPost(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                moveX = event.getX();
//                moveY = event.getY();
                moveX=event.getRawX();
                moveY=event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                setTranslationX(getX() + (event.getX() - moveX));
                setTranslationY(getY() + (event.getY() - moveY));

                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

}

