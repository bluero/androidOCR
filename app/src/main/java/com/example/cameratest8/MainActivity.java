package com.example.cameratest8;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
public class MainActivity extends Activity
{
	private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

	static {
		ORIENTATIONS.append(Surface.ROTATION_0, 90);
		ORIENTATIONS.append(Surface.ROTATION_90, 0);
		ORIENTATIONS.append(Surface.ROTATION_180, 270);
		ORIENTATIONS.append(Surface.ROTATION_270, 180);
	}
	// 定义界面上根布局管理器
	private FrameLayout rootLayout;
	// 定义自定义的AutoFitTextureView组件,用于预览摄像头照片
	private AutoFitTextureView textureView;
	// 摄像头ID（通常0代表后置摄像头，1代表前置摄像头）
	private String mCameraId = "0";
	// 定义代表摄像头的成员变量
	private CameraDevice cameraDevice;
	// 预览尺寸
	private Size previewSize;
	private CaptureRequest.Builder previewRequestBuilder;
	// 定义用于预览照片的捕获请求
	private CaptureRequest previewRequest;
	// 定义CameraCaptureSession成员变量
	private CameraCaptureSession captureSession;
	private ImageReader imageReader;
	private ImageView imageView;
	private SVDraw svdraw;
   	public Bitmap bitmap11;
   	private MediaProjection mediaProjection;
   	private MediaProjectionManager projectionManager;
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE };
	private RectView rectView;
	private VirtualDisplay virtualDisplay;
	private int screenDensity;
	private int displayWidth=720;
	private int displayHeigh=1080;
	private String mImageName;
	private ImageReader mImageReader;


	private final TextureView.SurfaceTextureListener mSurfaceTextureListener
			= new TextureView.SurfaceTextureListener()
	{
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture texture
				, int width, int height)
		{
			// 当TextureView可用时，打开摄像头
			openCamera(width, height);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture texture
				, int width, int height)
		{
			configureTransform(width, height);
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture texture)
		{
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture texture)
		{
		}
	};
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
	{
		//  摄像头被打开时激发该方法
		@Override
		public void onOpened(@NonNull CameraDevice cameraDevice)
		{
			com.example.cameratest8.MainActivity.this.cameraDevice = cameraDevice;
			// 开始预览
			createCameraPreviewSession();  // ②
		}

		// 摄像头断开连接时激发该方法
		@Override
		public void onDisconnected(CameraDevice cameraDevice)
		{
			cameraDevice.close();
			com.example.cameratest8.MainActivity.this.cameraDevice = null;
		}

		// 打开摄像头出现错误时激发该方法
		@Override
		public void onError(CameraDevice cameraDevice, int error)
		{
			cameraDevice.close();
			com.example.cameratest8.MainActivity.this.cameraDevice = null;
			com.example.cameratest8.MainActivity.this.finish();
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		rootLayout = findViewById(R.id.root);
		requestPermissions(new String[]{Manifest.permission.CAMERA}, 0x123);
		verifyStoragePermissions(this);
		DisplayMetrics metrics=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenDensity=metrics.densityDpi;
		projectionManager=(MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);

	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == 0x123 && grantResults.length == 1
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			// 创建预览摄像头图片的TextureView组件
			textureView = new AutoFitTextureView(com.example.cameratest8.MainActivity.this, null);
			// 为TextureView组件设置监听器
//			svdraw=(SVDraw) findViewById(R.id.image1);

//			svdraw.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
			textureView.setSurfaceTextureListener(mSurfaceTextureListener);
			//svdraw=new SVDraw(com.example.cameratest8.MainActivity.this,null);
			rootLayout.addView(textureView);


			findViewById(R.id.capture).setOnClickListener(view -> captureStillPicture());
		}
	}

	private void captureStillPicture()
	{
		try {
			if (cameraDevice == null) {
				return;
			}
			// 创建作为拍照的CaptureRequest.Builder
			CaptureRequest.Builder captureRequestBuilder = cameraDevice
					.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			// 将imageReader的surface作为CaptureRequest.Builder的目标
			captureRequestBuilder.addTarget(imageReader.getSurface());
			// 设置自动对焦模式
			captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
					CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			// 设置自动曝光模式
			captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
					CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
			// 获取设备方向
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			// 根据设备方向计算设置照片的方向
			captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,
					ORIENTATIONS.get(rotation));
			// 停止连续取景
			captureSession.stopRepeating();
			// 捕获静态图像
			captureSession.capture(captureRequestBuilder.build(),
					new CameraCaptureSession.CaptureCallback()  // ⑤
					{
						// 拍照完成时激发该方法
						@Override
						public void onCaptureCompleted(@NonNull CameraCaptureSession session,
													   @NonNull CaptureRequest request, @NonNull TotalCaptureResult result)
						{
							try {
								// 重设自动对焦模式
								previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
										CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
								// 设置自动曝光模式
								previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
										CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
								// 打开连续取景模式
								captureSession.setRepeatingRequest(previewRequest, null, null);
							} catch (CameraAccessException e) {
								e.printStackTrace();
							}
						}
					}, null);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}
	// 根据手机的旋转方向确定预览图像的方向
	private void configureTransform(int viewWidth, int viewHeight) {
		if (null == previewSize) {
			return;
		}
		// 获取手机的旋转方向
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		Matrix matrix = new Matrix();
		RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
		RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
		float centerX = viewRect.centerX();
		float centerY = viewRect.centerY();
		// 处理手机横屏的情况
		if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
			bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
			matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
			float scale = Math.max(
					(float) viewHeight / previewSize.getHeight(),
					(float) viewWidth / previewSize.getWidth());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		}
		// 处理手机倒置的情况
		else if (Surface.ROTATION_180 == rotation)
		{
			matrix.postRotate(180, centerX, centerY);
		}
		textureView.setTransform(matrix);
	}
	// 打开摄像头
	private void openCamera(int width, int height)
	{
		setUpCameraOutputs(width, height);
		configureTransform(width, height);

		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			// 如果用户没有授权使用摄像头，直接返回
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			// 打开摄像头
			manager.openCamera(mCameraId, stateCallback, null); // ①

		}
		catch (CameraAccessException e)
		{
			e.printStackTrace();
		}
	}
	private void createCameraPreviewSession()
	{
		try
		{
			SurfaceTexture texture = textureView.getSurfaceTexture();
 			texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
 			int a=textureView.getMeasuredWidth()*6/8;
 			int b=textureView.getMeasuredHeight()*6/8;
			svdraw=new SVDraw(com.example.cameratest8.MainActivity.this,null);
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					a,
					b
			);
			p.leftMargin=(textureView.getMeasuredWidth()-a)/2;
			p.topMargin=(textureView.getMeasuredHeight()-b)/2;
			svdraw.setLayoutParams(p);
			rootLayout.addView(svdraw);
			rectView=new RectView(MainActivity.this,null);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					200,
					200
			);
			params.leftMargin=(textureView.getMeasuredWidth()-a)/2;
			params.topMargin=(textureView.getMeasuredHeight()-b)/2;
			rectView.setLayoutParams(params);
			rootLayout.addView(rectView);



			//linearParams.height = 1440;
			//linearParams.width=1080;
			//svdraw.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

//			svdraw.setLayoutParams(linearParams);
//			SVDraw svDraw1=new SVDraw(MainActivity.this,null);
//			LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) svDraw1.getLayoutParams();
//			// 取控件aaa当前的布局参数
//			linearParams.height = 150;        // 当控件的高强制设成150象素
//			linearParams.weight = 300;
//			svDraw1.setLayoutParams(linearParams); // 使设置好的布局参数应用到控件
			//rootLayout.addView(svdraw);
			Log.e("tag",previewSize.getWidth()+""+previewSize.getHeight()+"");
			//3264*2448
			Surface surface = new Surface(texture);
			// 创建作为预览的CaptureRequest.Builder
			previewRequestBuilder = cameraDevice
					.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			// 将textureView的surface作为CaptureRequest.Builder的目标
			previewRequestBuilder.addTarget(new Surface(texture));
			// 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求

			cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
				new CameraCaptureSession.StateCallback() // ③
				{
					@Override
					public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession)
					{

						// 如果摄像头为null，直接结束方法
						if (null == cameraDevice)
						{
							return;
						}
						// 当摄像头已经准备好时，开始显示预览
						captureSession = cameraCaptureSession;
						// 设置自动对焦模式
						previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
							CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
						// 设置自动曝光模式
						previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
							CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
						// 开始显示相机预览
						previewRequest = previewRequestBuilder.build();
						//imageView=(ImageView)findViewById(R.id.image1);
						//MyView myView=findViewById(R.id.image1);
						//myView.bringToFront();
						svdraw.drawFrame(0,0,a,b);
						rectView.drawFrame(0,0,200,200);
//						mSVDraw.setAspectRatio(previewSize.getWidth(),previewSize.getHeight());
//						mSVDraw.setVisibility(View.VISIBLE);
//						mSVDraw.drawLine();
						//mSVDraw.bringToFront();
						//Bitmap bitmap=BitmapFactory.decodeResource(com.example.cameratest7.MainActivity.this.getResources(), R.drawable.white);
						//imageView.setImageBitmap(bitmap);
						//bitmap.setPixel(20,30,Color.RED);
//						Bitmap copybiBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
//						//[2.2]想作画需要一个画笔
//						Paint paint = new Paint();
//						//[2.3]创建一个画布  把白纸铺到画布上
//						Canvas canvas = new Canvas(copybiBitmap);
//						//[2.4]开始作画
//						Matrix matrix = new Matrix();
//						//[2.5]对图片进行旋转
//						matrix.setRotate(20, bitmap.getWidth()/2, bitmap.getHeight()/2);
//						//[2.5]对图片进行
//						matrix.setScale(0.5f, 0.5f);
//						// [2.6]对图片进行平移
//						matrix.setTranslate(30, 0);
//						canvas.drawBitmap(bitmap,matrix , paint);
//						Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//						Canvas canvas = new Canvas(mutableBitmap);
//						Paint paint = new Paint();
//						paint.setColor(Color.RED);
//						paint.setStyle(Paint.Style.STROKE);//不填充
//						paint.setStrokeWidth(10);  //线的宽度
//						canvas.drawRect(250, 75, 350, 120, paint);

//Canvas canvas = new Canvas(imageBitmap);

						//drawRectangles(bitmap,a,b);
						//imageView.setImageBitmap(Bitmap.createBitmap(bitmap,0,0,20,20));
						//imageView.setImageResource(R.drawable.black);
//						Bitmap btm1 = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.black);
//						BitmapDrawable bd1= new BitmapDrawable(btm1);
//						imageView.setBackgroundDrawable(bd1);
						//imageView.setColorFilter(0XFFFFFFFF);
						//imageView.setImageBitmap(mutableBitmap);
						//imageView.bringToFront();
						//setContentView(imageView);
						try {
							// 设置预览时连续捕获图像数据
							captureSession.setRepeatingRequest(previewRequest, null, null);  // ④
						}
						catch (CameraAccessException e)
						{
							e.printStackTrace();
						}
					}
					@Override public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession)
					{
						Toast.makeText(com.example.cameratest8.MainActivity.this, "配置失败！",
								Toast.LENGTH_SHORT).show();
					}
				}, null);
		}
		catch (CameraAccessException e)
		{
			e.printStackTrace();
		}
	}
	private void setUpCameraOutputs(int width, int height)
	{
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try {
			// 获取指定摄像头的特性
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
			// 获取摄像头支持的配置属性
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.
					SCALER_STREAM_CONFIGURATION_MAP);
			// 获取摄像头支持的最大尺寸
			Size largest = Collections.max(
					Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
		 			new CompareSizesByArea());
			// 创建一个ImageReader对象，用于获取摄像头的图像数据
			imageReader = ImageReader.newInstance(largest.getWidth(),
					largest.getHeight(), ImageFormat.JPEG, 2);
			imageReader.setOnImageAvailableListener(reader -> {
					// 当照片数据可用时激发该方法
					// 获取捕获的照片数据
					int [] location=new int[2];
					rectView.getLocationOnScreen(location);
					int x=location[0];
					int y=location[1];
//					Intent intent=projectionManager.createScreenCaptureIntent();
//					startActivityForResult(intent,0x123);
					Bitmap bitmap12=loadBitmapFromViewBySystem(rootLayout);
					Image image = reader.acquireNextImage();
					ByteBuffer buffer = image.getPlanes()[0].getBuffer();
					byte[] bytes = new byte[buffer.remaining()];
					// 使用IO流将照片写入指定文件
					File file = new File(getExternalFilesDir(null), "pic.jpg");
					buffer.get(bytes);
					int length=bytes.length;
					Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,length);
					bitmap=cropBitmap(bitmap);
				    Bitmap bitmap2=Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas d = new Canvas(bitmap2);

					//使用bitmap构建一个Canvas，绘制的所有内容都是绘制在此Bitmap上的
					// Drawable bgDrawable = mSVDraw.getBackground();
           			//bgDrawable.draw(d);//绘制背景
					Paint paint=new Paint();
					paint.setAntiAlias(true);
					paint.setColor(-65536);
					paint.setStyle(Paint.Style.FILL);
					//picture:4608,3456
					//View:3254*2448
					//1080,1440
				    float width1=((float)bitmap.getWidth()/previewSize.getHeight());
				    float height2=((float)bitmap.getHeight()/previewSize.getWidth());
				    float width2=(float) 4608/1440;
					d.drawRect( 200*width2,200*width2,300*width2,1000*width2,paint);
					Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),bitmap.getConfig());
					Canvas canvas=new Canvas(bitmap1);
					canvas.drawBitmap(bitmap,new Matrix(),null);
					canvas.drawBitmap(bitmap2,0,0,null);
					try (
						FileOutputStream output = new FileOutputStream(file))
					{
						output.write(bytes);
						try {
							MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), file.getAbsolutePath(), file.getName(), null);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} // 最后通知图库更新
						Toast.makeText(com.example.cameratest8.MainActivity.this, "保存: "
								+ file, Toast.LENGTH_SHORT).show();

						getApplication().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + "")));
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						image.close();
					}
			},null);
			// 获取最佳的预览尺寸
			previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
				width, height, largest);
			// 根据选中的预览尺寸来调整预览组件（TextureView的）的长宽比
			int orientation = getResources().getConfiguration().orientation;
			if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
				textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
//				svdraw=new SVDraw(com.example.cameratest8.MainActivity.this,null);
//				svdraw.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
//				rootLayout.addView(svdraw);
			} else {
				textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
//				svdraw=new SVDraw(com.example.cameratest8.MainActivity.this,null);
//				svdraw.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
//				rootLayout.addView(svdraw);
			}
		}
		catch (CameraAccessException e)
		{
			e.printStackTrace();
		}
		catch (NullPointerException e)
		{
			System.out.println("出现错误。");
		}
	}
	private static Size chooseOptimalSize(Size[] choices
			, int width, int height, Size aspectRatio)
	{
		// 收集摄像头支持的打过预览Surface的分辨率
		List<Size> bigEnough = new ArrayList<>();
		int w = aspectRatio.getWidth();
		int h = aspectRatio.getHeight();
		for (Size option : choices)
		{
			if (option.getHeight() == option.getWidth() * h / w &&
					option.getWidth() >= width && option.getHeight() >= height)
			{
				bigEnough.add(option);
			}
		}
		// 如果找到多个预览尺寸，获取其中面积最小的。
		if (bigEnough.size() > 0)
		{
			return Collections.min(bigEnough, new CompareSizesByArea());
		}
		else
		{
			System.out.println("找不到合适的预览尺寸！！！");
			return choices[0];
		}
	}
	// 为Size定义一个比较器Comparator
	static class CompareSizesByArea implements Comparator<Size>
	{
		@Override
		public int compare(Size lhs, Size rhs)
		{
			// 强转为long保证不会发生溢出
			return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
					(long) rhs.getWidth() * rhs.getHeight());
		}
	}
	private void drawRectangles(Bitmap imageBitmap, int[] keywordRects,
								int[] valueRects) {
		int left, top, right, bottom;
		Bitmap mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(mutableBitmap);
//Canvas canvas = new Canvas(imageBitmap);
		Paint paint = new Paint();
		for (int i = 0; i < 8; i++) {
			left = valueRects[i * 4];
			top = valueRects[i * 4 + 1];
			right = valueRects[i * 4 + 2];
			bottom = valueRects[i * 4 + 3];
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);//不填充
			paint.setStrokeWidth(10);  //线的宽度
			canvas.drawRect(left, top, right, bottom, paint);
		}
		for (int i = 0; i < 6; i++) {
			left = keywordRects[i * 4];
			top = keywordRects[i * 4 + 1];
			right = keywordRects[i * 4 + 2];
			bottom = keywordRects[i * 4 + 3];
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(10);
			canvas.drawRect(left, top, right, bottom, paint);
		}
		//img: 定义在xml布局中的ImagView控件
//img.setImageBitmap(imageBitmap);
	}
	/**
	 * 保存图片到相册
	 */
	public void saveImageToGallery(Bitmap mBitmap) {
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			Toast.makeText(MainActivity.this, "sdcard未使用",Toast.LENGTH_SHORT);
			return;
		}
		// 首先保存图片
		File appDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsoluteFile();
		if (!appDir.exists()) {
			appDir.mkdir();
		}
		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(appDir, fileName);
//		try {
//			FileOutputStream fos = new FileOutputStream(file);
//			mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//			fos.flush();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
		// 其次把文件插入到系统图库



	}
	public  void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}

	//剪切图片
	private Bitmap cropBitmap(Bitmap bitmap) {
		int w = bitmap.getWidth()*6/8; // 得到图片的宽，高
		int h = bitmap.getHeight()*6/8;
		int a=(bitmap.getWidth()-w)/2;
		int b=(bitmap.getHeight()-h)/2;
		int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
		cropWidth /= 2;
		int cropHeight = (int) (cropWidth / 1.2);
		return Bitmap.createBitmap(bitmap, a, b, w, h, null, false);
	}
	//
	public static Bitmap loadBitmapFromViewBySystem(View v) {
		if (v == null) {
			return null;
		}
		v.setDrawingCacheEnabled(true);
		v.buildDrawingCache();
		Bitmap bitmap = v.getDrawingCache();
		return bitmap;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==0x123){
			if(resultCode!=Activity.RESULT_OK){
				return;
			}
			mediaProjection = projectionManager.getMediaProjection(resultCode, data);
		}

			virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", displayWidth, displayHeigh, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);
			startCapture();

	}

	private void startCapture() {
		mImageName = "截图" + System.currentTimeMillis() + ".png";
		Log.i("TAG", "image name is : " + mImageName);
		Image image = mImageReader.acquireLatestImage();
		if (image == null) {
			Log.e("TAG", "image is null.");
			return;
		}
		int width = image.getWidth();
		int height = image.getHeight();
		final Image.Plane[] planes = image.getPlanes();
		final ByteBuffer buffer = planes[0].getBuffer();
		int pixelStride = planes[0].getPixelStride();
		int rowStride = planes[0].getRowStride();
		int rowPadding = rowStride - pixelStride * width;
		Bitmap mBitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
		mBitmap.copyPixelsFromBuffer(buffer);
		mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height);
		image.close();

		stopScreenCapture();

	}
	private void stopScreenCapture() {
		if (virtualDisplay != null) {
			virtualDisplay.release();
			virtualDisplay = null;
		}
	}
}
