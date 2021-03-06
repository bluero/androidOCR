package com.example.cameratest8;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Description:<br>
 * 网站: <a href="http://www.crazyit.org">疯狂Java联盟</a><br>
 * Copyright (C), 2001-2020, Yeeku.H.Lee<br>
 * This program is protected by copyright laws.<br>
 * Program Name:<br>
 * Date:<br>
 *
 * @author Yeeku.H.Lee kongyeeku@163.com<br>
 * @version 1.0
 */
public class AutoFitTextureView extends TextureView
{
	private int mRatioWidth = 0;
	private int mRatioHeight = 0;

	public AutoFitTextureView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	void setAspectRatio(int width, int height)
	{
		mRatioWidth = width;
		mRatioHeight = height;
		requestLayout();
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if (0 == mRatioWidth || 0 == mRatioHeight)
		{
			setMeasuredDimension(width, height);
		}
		else
		{
			if (width < height * mRatioWidth / mRatioHeight)
			{
				height=width * mRatioHeight / mRatioWidth;
				setMeasuredDimension(width, height);
			}
			else
			{
				width=height * mRatioWidth / mRatioHeight;
				setMeasuredDimension(width, height);
			}
		}
	}

	public void draw(){
		Canvas canvas = lockCanvas();
		Log.e("Test",canvas.isHardwareAccelerated()+","+isHardwareAccelerated());
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		canvas.drawRect(new Rect(0,0,200,200), paint);
		unlockCanvasAndPost(canvas);
	}
}
