package com.robotemplates.webviewapp.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.WebViewAppApplication;

public class Preferences {
	private Context mContext;
	private SharedPreferences mSharedPreferences;

	public Preferences() {
		mContext = WebViewAppApplication.getContext();
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public void clearPreferences() {
		mSharedPreferences.edit().clear().apply();
	}

	public int getRateCounter() {
		String key = mContext.getString(R.string.prefs_key_rate_counter);
		return mSharedPreferences.getInt(key, 0);
	}

	public void setRateCounter(int rateCounter) {
		String key = mContext.getString(R.string.prefs_key_rate_counter);
		mSharedPreferences.edit().putInt(key, rateCounter).apply();
	}

	public String getFcmRegistrationToken() {
		String key = mContext.getString(R.string.prefs_key_fcm_registration_token);
		return mSharedPreferences.getString(key, "");
	}

	public void setFcmRegistrationToken(String fcmRegistrationToken) {
		String key = mContext.getString(R.string.prefs_key_fcm_registration_token);
		mSharedPreferences.edit().putString(key, fcmRegistrationToken).apply();
	}

	public String getOneSignalUserId() {
		String key = mContext.getString(R.string.prefs_key_one_signal_user_id);
		return mSharedPreferences.getString(key, "");
	}

	public void setOneSignalUserId(String oneSignalUserId) {
		String key = mContext.getString(R.string.prefs_key_one_signal_user_id);
		mSharedPreferences.edit().putString(key, oneSignalUserId).apply();
	}
}
