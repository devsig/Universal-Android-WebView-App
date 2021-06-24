package com.robotemplates.webviewapp.ads;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.WebViewAppApplication;
import com.robotemplates.webviewapp.WebViewAppConfig;

public class AdMobInterstitialHelper {
	private static int sInterstitialCounter = 1;

	private InterstitialAd mInterstitialAd;

	public void setupAd(Context context) {
		String unitId = getUnitId();
		if (!unitId.equals("")) {
			mInterstitialAd = new InterstitialAd(context);
			mInterstitialAd.setAdUnitId(unitId);
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdClosed() {
					loadAd();
				}
			});
			loadAd();
		}
	}

	public void checkAd() {
		if (WebViewAppConfig.ADMOB_INTERSTITIAL_FREQUENCY > 0 && sInterstitialCounter % WebViewAppConfig.ADMOB_INTERSTITIAL_FREQUENCY == 0) {
			showAd();
		}
		sInterstitialCounter++;
	}

	private void loadAd() {
		if (mInterstitialAd != null) {
			mInterstitialAd.loadAd(AdMobUtility.createAdRequest());
		}
	}

	private void showAd() {
		String unitId = getUnitId();
		if (!unitId.equals("")) {
			if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
				mInterstitialAd.show();
			}
		}
	}

	private String getUnitId() {
		Context context = WebViewAppApplication.getContext();
		return WebViewAppConfig.TEST_ADS ? context.getString(R.string.admob_test_unit_id_interstitial) : context.getString(R.string.admob_unit_id_interstitial);
	}
}
