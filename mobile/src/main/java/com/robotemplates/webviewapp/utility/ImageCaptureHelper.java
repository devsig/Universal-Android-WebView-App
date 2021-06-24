package com.robotemplates.webviewapp.utility;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.robotemplates.webviewapp.WebViewAppApplication;

import java.io.File;

public class ImageCaptureHelper {
	private Uri mCapturedImageUri;

	public void setupChooserIntent(Intent chooserIntent) {
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{createImageCaptureIntent()});
	}

	public Uri getCapturedImageUri() {
		Uri uri = null;
		if (mCapturedImageUri != null) {
			uri = mCapturedImageUri;
			mCapturedImageUri = null;
		}
		return uri;
	}

	public Uri[] getCapturedImageUris() {
		Uri[] uris = null;
		if (mCapturedImageUri != null) {
			uris = new Uri[]{mCapturedImageUri};
			mCapturedImageUri = null;
		}
		return uris;
	}

	private Intent createImageCaptureIntent() {
		mCapturedImageUri = getImageUri();
		Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri);
		return imageCaptureIntent;
	}

	private Uri getImageUri() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			ContentValues values = new ContentValues();
			String filename = System.currentTimeMillis() + ".jpg";

			values.put(MediaStore.Images.Media.TITLE, filename);
			values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
			values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
			values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
			values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
			values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WebViewApp");

			return WebViewAppApplication.getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		} else {
			File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			File cameraDir = new File(externalDir.getAbsolutePath() + File.separator + "WebViewApp");
			cameraDir.mkdirs();
			String filePath = cameraDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg";
			return Uri.fromFile(new File(filePath));
		}
	}
}
