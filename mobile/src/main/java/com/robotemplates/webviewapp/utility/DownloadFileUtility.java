package com.robotemplates.webviewapp.utility;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.WebViewAppConfig;

import static android.content.Context.DOWNLOAD_SERVICE;

public final class DownloadFileUtility {
	private DownloadFileUtility() {}

	public static void downloadFile(@NonNull Context context, @NonNull String url, @NonNull String suggestedFilename, @Nullable String mimeType, @Nullable String userAgent) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

		request.setTitle(suggestedFilename);
		request.setDescription(context.getString(R.string.main_downloading));
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, suggestedFilename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.allowScanningByMediaScanner();

		if (mimeType != null) {
			request.setMimeType(mimeType);
		}

		if (userAgent != null) {
			request.addRequestHeader("User-Agent", userAgent);
		}

		String cookies = CookieManager.getInstance().getCookie(url);
		request.addRequestHeader("cookie", cookies);

		// start download
		DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
		if (manager != null) {
			manager.enqueue(request);
		}
	}

	public static boolean isDownloadableFile(String url) {
		url = url.toLowerCase();

		for (String type : WebViewAppConfig.DOWNLOAD_FILE_TYPES) {
			if (url.matches(type)) return true;
		}

		return false;
	}

	public static String getFileName(String url) {
		int index = url.indexOf("?");
		if (index > -1) {
			url = url.substring(0, index);
		}
		url = url.toLowerCase();

		index = url.lastIndexOf("/");
		if (index > -1) {
			return url.substring(index + 1);
		} else {
			return Long.toString(System.currentTimeMillis());
		}
	}
}
