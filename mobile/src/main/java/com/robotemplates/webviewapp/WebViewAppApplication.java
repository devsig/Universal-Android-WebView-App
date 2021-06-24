package com.robotemplates.webviewapp;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;
import com.robotemplates.kozuza.BaseApplication;
import com.robotemplates.kozuza.Kozuza;
import com.robotemplates.webviewapp.fcm.OneSignalNotificationOpenedHandler;
import com.robotemplates.webviewapp.utility.Preferences;

import org.alfonz.utility.Logcat;

public class WebViewAppApplication extends BaseApplication {
	@Override
	public void onCreate() {
		super.onCreate();

		// init logcat
		Logcat.init(WebViewAppConfig.LOGS, "WEBVIEWAPP");

		// init analytics
		FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!WebViewAppConfig.DEV_ENVIRONMENT);

		// init AdMob
		MobileAds.initialize(this);

		// init OneSignal
		initOneSignal(getString(R.string.onesignal_app_id));
	}

	@Override
	public String getPurchaseCode() {
		return WebViewAppConfig.PURCHASE_CODE;
	}

	@Override
	public String getProduct() {
		return Kozuza.PRODUCT_WEBVIEWAPP;
	}

	private void initOneSignal(String oneSignalAppId) {
		if (!oneSignalAppId.equals("")) {
			OneSignal.startInit(this)
					.setNotificationOpenedHandler(new OneSignalNotificationOpenedHandler())
					.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
					.unsubscribeWhenNotificationsAreDisabled(true)
					.init();
			saveOneSignalUserId();
		}
	}

	private void saveOneSignalUserId() {
		String userId = OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId();
		if (userId != null) {
			Logcat.d("userId = " + userId);
			Preferences preferences = new Preferences();
			preferences.setOneSignalUserId(userId);
		}
	}
}
