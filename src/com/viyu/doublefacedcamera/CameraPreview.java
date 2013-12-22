package com.viyu.doublefacedcamera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private final String TAG = "CameraPreview";

	private SurfaceHolder holder = null;
	private Camera camera = null;

	private Paint paint = null;
	
	public CameraPreview(Context context) {
		this(context, null);
	}

	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, TAG + "CameraPreview()");
		holder = getHolder();
		holder.addCallback(this);

		paint = new Paint(); 
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
		paint.setAlpha(255);
		
		getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						LayoutParams layout = getLayoutParams();
						layout.width = 4 * getHeight() / 3;
						setLayoutParams(layout);
					//	getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
				});
	}
	
	/**
	 * 
	 * @param camera
	 */
	public void setCamera(Camera camera) {
		Log.i(TAG, TAG + "setCamera()");
		this.camera = camera;
		// 调整Camera的分辨率为其支持的最大分辨率
		Camera.Parameters cameraParams = camera.getParameters();
		List<Size> sizes = cameraParams.getSupportedPictureSizes();
		int previewWidth = 200, previewHeight = 200;
		for (Size size : sizes) {// 使用最大分辨率
			previewWidth = size.width;
			previewHeight = size.height;
			break;
		}
		cameraParams.setPictureSize(previewWidth, previewHeight);
		camera.setParameters(cameraParams);
		//
		try {
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, TAG + "surfaceCreated()");
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, TAG + "surfaceDestroyed()");
	} // The release work is assigned to CameraActivity, not here

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i(TAG, TAG + "surfaceChanged()");
		if (holder.getSurface() == null) {
			return;
		}
		//
		try {
			camera.stopPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}