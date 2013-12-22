package com.viyu.doublefacedcamera;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraActivity extends Activity implements PictureCallback, OnTouchListener {
	private String TAG = "CameraActivity";
	
	private CameraPreview cameraPreview = null;
	
	private Camera frontCamera = null;
	private Camera backCamera = null;

	private Bitmap picFromBack = null;
	private Bitmap picFromFront = null;
	
	private ImageButton captureButton = null;
	private ProgressDialog progressDialog = null;
	private String backToFrontMsg = null;
	private String photoCreatingMsg = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, TAG + ".onCreate()");
		super.onCreate(savedInstanceState);
		//
		if(Camera.getNumberOfCameras() < 2) {
			Log.e(TAG, TAG + "@string/error.thereisno2cameras");
			Toast.makeText(this, getResources().getString(R.string.error_thereisno2cameras), Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		//
		setContentView(R.layout.activity_camera);
		
		captureButton = (ImageButton)findViewById(R.id.id_button_capture);
		cameraPreview = (CameraPreview)findViewById(R.id.id_camera_preview);
		backToFrontMsg = getResources().getString(R.string.msg_isbeingtofrontcamera);
		photoCreatingMsg = getResources().getString(R.string.msg_photoisbeingcreated);
		ImageView imageView = (ImageView)findViewById(R.id.id_camera_preview_front);
		imageView.setOnTouchListener(this); 
	}

	private float preX = 0;
	private float preY = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, TAG + "X, Y is : " + event.getRawX() + ", " + event.getRawY());
		Log.d(TAG, TAG + "vX, vY is : " + v.getX() + ", " + v.getY());
		Log.d(TAG, TAG + ".......................");
		
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			preX = event.getRawX();
			preY = event.getRawY();
			return true;
		}
		if(event.getAction() == MotionEvent.ACTION_UP) {
			float newX = v.getX() + (event.getRawX() - preX);
			float newY = v.getY() + (event.getRawY() - preY);
			v.setX(newX);
			v.setY(newY);
			preX = event.getRawX();
			preY = event.getRawY();
			
		}
		if(event.getAction() == MotionEvent.ACTION_MOVE) {
			float newX = v.getX() + (event.getRawX() - preX);
			float newY = v.getY() + (event.getRawY() - preY);

			if(newX < cameraPreview.getX())
				newX = cameraPreview.getX();
			if(newY < cameraPreview.getY())
				newY = cameraPreview.getY();
			if(newX > cameraPreview.getX() + cameraPreview.getWidth() - v.getWidth())
				newX = cameraPreview.getX() + cameraPreview.getWidth() - v.getWidth();
			if(newY > cameraPreview.getY() + cameraPreview.getHeight() - v.getHeight())
				newY = cameraPreview.getY() + cameraPreview.getHeight() - v.getHeight();
			
			v.setX(newX);
			v.setY(newY);
			preX = event.getRawX();
			preY = event.getRawY();
		}
		return true;
	}
	
	@Override
	protected void onStart() {
		Log.d(TAG, TAG + ".onStart()");
		super.onStart();
		//
		try {
			backCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, getResources().getString(R.string.error_failtogetbackcamera), Toast.LENGTH_LONG).show();
			releaseBackCamera();
			finish();
			return;
		}
		cameraPreview.setCamera(backCamera);
	}
		
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(TAG, TAG + ".onPictureTaken()");
		if(picFromBack == null) {
			picFromBack = BitmapFactory.decodeByteArray(data, 0, data.length);
			//release back camera
			releaseBackCamera();
			//switch to front camera
			try {
				frontCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
				progressDialog.dismiss();
				progressDialog = null;
				cameraPreview.setCamera(frontCamera);
			} catch (Exception e) {
				Toast.makeText(this, getResources().getString(R.string.error_failtogetfrontcamera), Toast.LENGTH_LONG).show();
				releasefrontCamera();
				finish();
				return;
			}

			(new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... params) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				};
				
				@Override
				protected void onPostExecute(String result) {
					frontCamera.takePicture(null, null, CameraActivity.this);
					progressDialog = ProgressDialog.show(CameraActivity.this, "", photoCreatingMsg);
				};
				
			}).execute();
			
		} else if(picFromFront == null) {
			picFromFront = BitmapFactory.decodeByteArray(data, 0, data.length);
			releasefrontCamera();
			if(picFromFront != null) {
				PreviewPhotoHolder.getInstance().setPreviewPhoto(picFromBack, picFromFront);
				progressDialog.dismiss();
				progressDialog = null;
				captureButton.setEnabled(true);
				//
				mergeBitmap(picFromBack, picFromFront);
				/*Intent intent = new Intent(this, PhotoPreviewActivity.class);
				startActivity(intent);*/
			}
		} 
	}
	
	private Bitmap mergeBitmap(Bitmap bitmapBack, Bitmap bitmapFront) {
		OutputStream outputStream = null;
		Bitmap mergedBimap = null;
	    try {
	    	Config config = bitmapBack.getConfig();
	    	mergedBimap = bitmapBack.copy(config, true);
	        Canvas canvas = new Canvas(mergedBimap);
	        canvas.drawBitmap(bitmapFront, 0, 0, null);
	        String tmpImg = String.valueOf(System.currentTimeMillis()) + ".jpg";
	        outputStream = new FileOutputStream(Environment.getExternalStorageDirectory() + "/" + tmpImg);
	        mergedBimap.compress(CompressFormat.JPEG, 100, outputStream);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    } finally {
	    	if(outputStream != null) {
	    		try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    }
	    return mergedBimap;
	}

	public void captureButtonClicked(View view) {
		if(picFromBack == null && picFromFront == null) {
			backCamera.takePicture(null, null, this);
			//disable button
			captureButton.setEnabled(false);
			//show progress
			progressDialog = ProgressDialog.show(this, "", backToFrontMsg);
		}
	}

	private Camera getCameraInstance(int id) throws Exception {
		Camera camera = null;
		try {
			camera = Camera.open(id);
		} catch (Exception e) {
			throw e;
		}
		return camera;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, TAG+".onPause()");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, TAG+".onResume()");
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, TAG+".onStop()");
		releasefrontCamera();
		releaseBackCamera();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, TAG+".onDestroy()");
		releasefrontCamera();
		releaseBackCamera();
		super.onDestroy();
	}

	private void releasefrontCamera() {
		Log.d(TAG, TAG+".releasefrontCamera()");
		if(frontCamera != null) {
			frontCamera.release();
			frontCamera = null;
		}
	}
	
	private void releaseBackCamera() {
		Log.d(TAG, TAG+".releaseBackCamera()");
		if(backCamera != null) {
			backCamera.release();
			backCamera = null;
		}
	}
}
