package com.robotemplates.webviewapp.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.robotemplates.webviewapp.R;

public final class LocationUtility {
	private static final int LOCATION_SETTINGS_PROMPT_DURATION = 10000;

	private LocationUtility() {}

	public static boolean isLocationEnabled(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int locationMode;
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		} else {
			String locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}
	}

	@SuppressLint("WrongConstant")
	public static void showLocationSettingsPrompt(final View view) {
		Context context = view.getContext();
		Snackbar
				.make(view, context.getString(R.string.location_settings_snackbar), LOCATION_SETTINGS_PROMPT_DURATION)
				.setAction(context.getString(R.string.location_settings_confirm), v -> {
					final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					view.getContext().startActivity(intent);
				})
				.show();
	}
}
