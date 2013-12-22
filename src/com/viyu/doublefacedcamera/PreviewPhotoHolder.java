package com.viyu.doublefacedcamera;

import android.graphics.Bitmap;


public class PreviewPhotoHolder {

	private Bitmap previewPhoto_back = null;
	private Bitmap previewPhoto_front = null;
	
	private PreviewPhotoHolder() {
	}
	
	private static class InstanceHolder {
		private static PreviewPhotoHolder instance = new PreviewPhotoHolder();
	}
	
	public static PreviewPhotoHolder getInstance() {
		return InstanceHolder.instance;
	}
	
	public void setPreviewPhoto(Bitmap photo_back, Bitmap photo_front) {
		clearPhotos();
		this.previewPhoto_back = photo_back;
		this.previewPhoto_front = photo_front;
	}
	
	public void clearPhotos() {
		if(this.previewPhoto_back != null) {
			this.previewPhoto_back.recycle();
			this.previewPhoto_back = null;
		}
		if(this.previewPhoto_front != null) {
			this.previewPhoto_front.recycle();
			this.previewPhoto_front = null;
		}
	}
	
	public Bitmap getPreviewPhoto_back() {
		return this.previewPhoto_back;
	}
	
	public Bitmap getPreviewPhoto_front() {
		return this.previewPhoto_front;
	}
}