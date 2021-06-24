package com.robotemplates.webviewapp;

import com.robotemplates.webviewapp.view.PullToRefreshMode;

import static com.robotemplates.webviewapp.view.PullToRefreshMode.*;

public class WebViewAppConfig {
	// Envato purchase code
	public static final String PURCHASE_CODE = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";

	// true for showing action bar
	public static final boolean ACTION_BAR = true;

	// true for showing html title rather than navigation title in the action bar
	public static final boolean ACTION_BAR_HTML_TITLE = false;

	// true for enabling navigation drawer menu
	public static final boolean NAVIGATION_DRAWER = true;

	// true for enabling background image in the header of the navigation drawer menu,
	// otherwise accent color will be used,
	// background image is stored in navigation_header_bg.png
	public static final boolean NAVIGATION_DRAWER_HEADER_IMAGE = true;

	// true for enabling icon tint in the navigation drawer menu,
	// note that only transparent PNG icons can be tinted,
	// tint color is defined in @color/navigation_icon_tint
	public static final boolean NAVIGATION_DRAWER_ICON_TINT = true;

	// true for enabling exit confirmation when back button is pressed
	public static final boolean EXIT_CONFIRMATION = true;

	// true for enabling geolocation
	public static final boolean GEOLOCATION = true;

	// true for enabling progress placeholder when loading a first page
	public static final boolean PROGRESS_PLACEHOLDER = true;

	// pull-to-refresh gesture for refreshing the content,
	// set ENABLED to enable the gesture, set DISABLED to disable the gesture,
	// set PROGRESS to disable the gesture and show only progress indicator
	public static final PullToRefreshMode PULL_TO_REFRESH = ENABLED;

	// frequency of showing rate my app prompt,
	// prompt will be shown after each x launches of the app,
	// set 0 if you do not want to show rate my app prompt
	public static final int RATE_APP_PROMPT_FREQUENCY = 10;

	// duration of showing rate my app prompt in milliseconds
	public static final int RATE_APP_PROMPT_DURATION = 10000;

	// custom user agent string for the webview,
	// leave this constant empty if you do not want to set custom user agent string
	public static final String WEBVIEW_USER_AGENT = "";

	// frequency of showing AdMob interstitial ad,
	// ad will be shown after each x url loadings or clicks on navigation drawer menu
	public static final int ADMOB_INTERSTITIAL_FREQUENCY = 3;

	// URL link to your privacy policy for GDPR consent form,
	// leave this constant empty if you do not want to show GDPR consent form
	public static final String GDPR_PRIVACY_POLICY_URL = "https://link.to/privacy-policy.html";

	// true for opening webview links in external web browser rather than directly in the webview
	public static final boolean OPEN_LINKS_IN_EXTERNAL_BROWSER = false;

	// rules for opening links in external browser,
	// if URL link contains the string, it will be opened in external browser,
	// these rules have higher priority than OPEN_LINKS_IN_EXTERNAL_BROWSER option
	public static final String[] LINKS_OPENED_IN_EXTERNAL_BROWSER = {
			"target=blank",
			"target=external",
			"play.google.com/store",
			"youtube.com/watch",
			"facebook.com/sharer",
			"twitter.com/share"
	};

	// rules for opening links in internal webview,
	// if URL link contains the string, it will be loaded in internal webview,
	// these rules have higher priority than OPEN_LINKS_IN_EXTERNAL_BROWSER option
	public static final String[] LINKS_OPENED_IN_INTERNAL_WEBVIEW = {
			"target=webview",
			"target=internal"
	};

	// list of file extensions or expressions for download,
	// if webview URL matches with this regular expression, the file will be downloaded via download manager,
	// leave this array empty if you do not want to use download manager
	public static final String[] DOWNLOAD_FILE_TYPES = {
			".*zip$", ".*rar$", ".*pdf$", ".*doc$", ".*xls$",
			".*mp3$", ".*wma$", ".*ogg$", ".*m4a$", ".*wav$",
			".*avi$", ".*mov$", ".*mp4$", ".*mpg$", ".*3gp$",
			".*drive.google.com.*file.*",
			".*dropbox.com/s/.*"
	};

	// debug logs, value is set via build config in build.gradle
	public static final boolean LOGS = BuildConfig.LOGS;

	// development environment, value is set via build config in build.gradle
	public static final boolean DEV_ENVIRONMENT = BuildConfig.DEV_ENVIRONMENT;

	// AdMob test ads, value is set via build config in build.gradle
	public static final boolean TEST_ADS = BuildConfig.TEST_ADS;
}
