package com.viyu.doublefacedcamera;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;


public class PhotoPreviewActivity extends Activity  {
	
	private final String TAG = "PhotoPreviewActivity";
	
	private ImageView backView = null;
	private ImageView frontView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photopreview);
		backView = (ImageView)findViewById(R.id.photo_preview_view_back);
		backView.setImageBitmap(PreviewPhotoHolder.getInstance().getPreviewPhoto_back());
		frontView = (ImageView)findViewById(R.id.photo_preview_view_front);
		frontView.setImageBitmap(PreviewPhotoHolder.getInstance().getPreviewPhoto_front());
		
	}

	private Bitmap mergeBitmap(Bitmap bitmapBack, Bitmap bitmapFront) {
		OutputStream outputStream = null;
		Bitmap mergedBimap = null;
	    try {
	    	mergedBimap = bitmapBack.copy(bitmapBack.getConfig(), true);
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

}