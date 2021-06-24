package com.robotemplates.webviewapp.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.robotemplates.webviewapp.R;
import com.robotemplates.webviewapp.WebViewAppConfig;
import com.robotemplates.webviewapp.ads.AdMobUtility;
import com.robotemplates.webviewapp.listener.DrawerStateListener;
import com.robotemplates.webviewapp.listener.LoadUrlListener;
import com.robotemplates.webviewapp.listener.WebViewOnKeyListener;
import com.robotemplates.webviewapp.listener.WebViewOnTouchListener;
import com.robotemplates.webviewapp.utility.DownloadFileUtility;
import com.robotemplates.webviewapp.utility.IntentUtility;
import com.robotemplates.webviewapp.utility.PermissionRationaleHandler;
import com.robotemplates.webviewapp.utility.Preferences;
import com.robotemplates.webviewapp.view.PullToRefreshMode;

import org.alfonz.utility.Logcat;
import org.alfonz.utility.NetworkUtility;
import org.alfonz.utility.PermissionManager;
import org.alfonz.view.StatefulLayout;

import im.delight.android.webview.AdvancedWebView;
import name.cpr.VideoEnabledWebChromeClient;
import name.cpr.VideoEnabledWebView;

public class MainFragment extends TaskFragment implements SwipeRefreshLayout.OnRefreshListener, AdvancedWebView.Listener {
	private static final String ARGUMENT_URL = "url";
	private static final String ARGUMENT_SHARE = "share";

	private boolean mProgress = false;
	private View mRootView;
	private StatefulLayout mStatefulLayout;
	private AdvancedWebView mWebView;
	private String mUrl = "about:blank";
	private String mShare;
	private boolean mLocal = false;
	private int mStoredActivityRequestCode;
	private int mStoredActivityResultCode;
	private Intent mStoredActivityResultIntent;
	private PermissionManager mPermissionManager = new PermissionManager(new PermissionRationaleHandler());

	public static MainFragment newInstance(String url, String share) {
		MainFragment fragment = new MainFragment();

		// arguments
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_URL, url);
		arguments.putString(ARGUMENT_SHARE, share);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		// handle fragment arguments
		Bundle arguments = getArguments();
		if (arguments != null) {
			handleArguments(arguments);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int layout = WebViewAppConfig.PULL_TO_REFRESH == PullToRefreshMode.DISABLED ? R.layout.fragment_main : R.layout.fragment_main_swipeable;
		mRootView = inflater.inflate(layout, container, false);
		mWebView = mRootView.findViewById(R.id.main_webview);
		return mRootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// restore webview state
		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		}

		// setup webview
		setupView();

		// pull to refresh
		setupSwipeRefreshLayout();

		// setup stateful layout
		setupStatefulLayout(savedInstanceState);

		// load data
		if (mStatefulLayout.getState() == StatefulLayout.EMPTY) loadData();

		// progress popup
		showProgress(mProgress);

		// check permissions
		if (WebViewAppConfig.GEOLOCATION) {
			mPermissionManager.request(
					this,
					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
					(requestable, permissionsResult) -> handleLocationPermissionsResult(permissionsResult));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mWebView.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		// check permissions
		String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
		PermissionManager.PermissionsResult result = PermissionManager.check(getContext(), permissions);

		if (result.isGranted()) {
			// permitted
			mWebView.onActivityResult(requestCode, resultCode, intent);
		} else {
			// not permitted
			mStoredActivityRequestCode = requestCode;
			mStoredActivityResultCode = resultCode;
			mStoredActivityResultIntent = intent;

			mPermissionManager.request(
					this,
					permissions,
					(requestable, permissionsResult) -> handleUploadPermissionsResult(permissionsResult));
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		// save current instance state
		super.onSaveInstanceState(outState);

		// stateful layout state
		if (mStatefulLayout != null) mStatefulLayout.saveInstanceState(outState);

		// save webview state
		mWebView.saveState(outState);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		// action bar menu
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_main, menu);

		// show or hide share button
		MenuItem share = menu.findItem(R.id.menu_main_share);
		share.setVisible(mShare != null && !mShare.trim().equals(""));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// action bar menu behavior
		if (item.getItemId() == R.id.menu_main_share) {
			IntentUtility.startShareActivity(getContext(), getString(R.string.app_name), getShareText(mShare));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		mPermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	@Override
	public void onRefresh() {
		runTaskCallback(this::refreshData);
	}

	@Override
	public void onPageStarted(String url, Bitmap favicon) {
		Logcat.d("");
	}

	@Override
	public void onPageFinished(String url) {
		Logcat.d("");
	}

	@Override
	public void onPageError(int errorCode, String description, String failingUrl) {
		Logcat.d("");
	}

	@Override
	public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
		Logcat.d(url + " / " + suggestedFilename + " / " + mimeType + " / " + userAgent);
		mPermissionManager.request(this,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				requestable -> requestable.handleDownloadPermissionGranted(url, suggestedFilename, mimeType, userAgent));
	}

	@Override
	public void onExternalPageRequest(String url) {
		Logcat.d("");
	}

	public void refreshData() {
		if (NetworkUtility.isOnline(getActivity()) || mLocal) {
			// show progress popup
			showProgress(true);

			// load web url
			String url = mWebView.getUrl();
			if (url == null || url.equals("")) url = mUrl;
			mWebView.loadUrl(url);
		} else {
			showProgress(false);
			Toast.makeText(getActivity(), R.string.global_network_offline, Toast.LENGTH_LONG).show();
		}
	}

	private void handleLocationPermissionsResult(PermissionManager.PermissionsResult permissionsResult) {
		Logcat.d(String.format("granted = %b", permissionsResult.isGranted()));
	}

	private void handleUploadPermissionsResult(PermissionManager.PermissionsResult permissionsResult) {
		Logcat.d(String.format("granted = %b", permissionsResult.isGranted()));
		if (permissionsResult.isGranted()) {
			// continue with activity result handling
			if (mStoredActivityResultIntent != null) {
				mWebView.onActivityResult(mStoredActivityRequestCode, mStoredActivityResultCode, mStoredActivityResultIntent);
				mStoredActivityRequestCode = 0;
				mStoredActivityResultCode = 0;
				mStoredActivityResultIntent = null;
			}
		}
	}

	private void handleDownloadPermissionGranted(String url, String suggestedFilename, String mimeType, String userAgent) {
		Toast.makeText(getActivity(), R.string.main_downloading, Toast.LENGTH_LONG).show();
		DownloadFileUtility.downloadFile(getActivity(), url, suggestedFilename, mimeType, userAgent);
	}

	private void handleArguments(Bundle arguments) {
		if (arguments.containsKey(ARGUMENT_URL)) {
			mUrl = getWebViewUrl(arguments.getString(ARGUMENT_URL));
			mLocal = mUrl.contains("file://");
		}
		if (arguments.containsKey(ARGUMENT_SHARE)) {
			mShare = arguments.getString(ARGUMENT_SHARE);
		}
	}

	private void loadData() {
		if (NetworkUtility.isOnline(getActivity()) || mLocal) {
			// show progress
			if (WebViewAppConfig.PROGRESS_PLACEHOLDER) {
				mStatefulLayout.showProgress();
			} else {
				mStatefulLayout.showContent();
			}

			// load web url
			Logcat.d(mUrl);
			mWebView.loadUrl(mUrl);
		} else {
			mStatefulLayout.showOffline();
		}
	}

	private void showProgress(boolean visible) {
		// show pull to refresh progress bar
		SwipeRefreshLayout contentSwipeRefreshLayout = mRootView.findViewById(R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = mRootView.findViewById(R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = mRootView.findViewById(R.id.container_empty_swipeable);

		if (contentSwipeRefreshLayout != null) contentSwipeRefreshLayout.setRefreshing(visible);
		if (offlineSwipeRefreshLayout != null) offlineSwipeRefreshLayout.setRefreshing(visible);
		if (emptySwipeRefreshLayout != null) emptySwipeRefreshLayout.setRefreshing(visible);

		mProgress = visible;
	}

	private void showContent(final long delay) {
		final Handler timerHandler = new Handler();
		final Runnable timerRunnable = () -> runTaskCallback(() -> {
			if (getActivity() != null && mRootView != null) {
				Logcat.d("timer");
				mStatefulLayout.showContent();
			}
		});
		timerHandler.postDelayed(timerRunnable, delay);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setupView() {
		// webview settings
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setGeolocationEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(false);

		// user agent
		if (WebViewAppConfig.WEBVIEW_USER_AGENT != null && !WebViewAppConfig.WEBVIEW_USER_AGENT.equals("")) {
			mWebView.getSettings().setUserAgentString(WebViewAppConfig.WEBVIEW_USER_AGENT);
		}

		// advanced webview settings
		mWebView.setListener(getActivity(), this);
		mWebView.setGeolocationEnabled(true);

		// webview style
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // fixes scrollbar on Froyo

		// webview hardware acceleration
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}

		// webview chrome client
		View nonVideoLayout = getActivity().findViewById(R.id.main_non_video_layout);
		ViewGroup videoLayout = getActivity().findViewById(R.id.main_video_layout);
		View progressView = getActivity().getLayoutInflater().inflate(R.layout.placeholder_progress, null);
		VideoEnabledWebChromeClient webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, progressView, (VideoEnabledWebView) mWebView);
		webChromeClient.setOnToggledFullscreen(new MyToggledFullscreenCallback());
		mWebView.setWebChromeClient(webChromeClient);

		// webview client
		mWebView.setWebViewClient(new MyWebViewClient());

		// webview key listener
		mWebView.setOnKeyListener(new WebViewOnKeyListener((DrawerStateListener) getActivity()));

		// webview touch listener
		mWebView.requestFocus(View.FOCUS_DOWN); // http://android24hours.blogspot.cz/2011/12/android-soft-keyboard-not-showing-on.html
		mWebView.setOnTouchListener(new WebViewOnTouchListener());

		// webview scroll listener
		//((RoboWebView) mWebView).setOnScrollListener(new WebViewOnScrollListener()); // not used

		// admob
		setupBannerView();
	}

	private void setupBannerView() {
		String productionUnitId = getString(R.string.admob_unit_id_banner);
		String testUnitId = getString(R.string.admob_test_unit_id_banner);

		if (!productionUnitId.equals("") && NetworkUtility.isOnline(getActivity())) {
			String unitId = WebViewAppConfig.TEST_ADS ? testUnitId : productionUnitId;
			ViewGroup contentLayout = mRootView.findViewById(R.id.container_content);
			AdMobUtility.createAdView(getActivity(), unitId, AdMobUtility.getAdaptiveBannerAdSize(getActivity()), contentLayout);
		}
	}

	private void controlBack() {
		if (mWebView.canGoBack()) mWebView.goBack();
	}

	private void controlForward() {
		if (mWebView.canGoForward()) mWebView.goForward();
	}

	private void controlStop() {
		mWebView.stopLoading();
	}

	private void controlReload() {
		mWebView.reload();
	}

	private void setupStatefulLayout(Bundle savedInstanceState) {
		// reference
		mStatefulLayout = (StatefulLayout) mRootView;

		// state change listener
		mStatefulLayout.setOnStateChangeListener((view, state) -> {
			Logcat.d(String.valueOf(state));
			// do nothing
		});

		// restore state
		mStatefulLayout.restoreInstanceState(savedInstanceState);
	}

	private void setupSwipeRefreshLayout() {
		SwipeRefreshLayout contentSwipeRefreshLayout = mRootView.findViewById(R.id.container_content_swipeable);
		SwipeRefreshLayout offlineSwipeRefreshLayout = mRootView.findViewById(R.id.container_offline_swipeable);
		SwipeRefreshLayout emptySwipeRefreshLayout = mRootView.findViewById(R.id.container_empty_swipeable);

		if (WebViewAppConfig.PULL_TO_REFRESH == PullToRefreshMode.ENABLED) {
			contentSwipeRefreshLayout.setOnRefreshListener(this);
			offlineSwipeRefreshLayout.setOnRefreshListener(this);
			emptySwipeRefreshLayout.setOnRefreshListener(this);
		} else if (WebViewAppConfig.PULL_TO_REFRESH == PullToRefreshMode.PROGRESS) {
			contentSwipeRefreshLayout.setDistanceToTriggerSync(Integer.MAX_VALUE);
			offlineSwipeRefreshLayout.setDistanceToTriggerSync(Integer.MAX_VALUE);
			emptySwipeRefreshLayout.setDistanceToTriggerSync(Integer.MAX_VALUE);
		}
	}

	@SuppressWarnings("RegExpRedundantEscape")
	private String getWebViewUrl(String url) {
		Preferences preferences = new Preferences();
		url = url.replaceAll("\\{FCM_REGISTRATION_TOKEN\\}", preferences.getFcmRegistrationToken());
		url = url.replaceAll("\\{ONE_SIGNAL_USER_ID\\}", preferences.getOneSignalUserId());
		return url;
	}

	@SuppressWarnings("RegExpRedundantEscape")
	private String getShareText(String text) {
		if (mWebView != null) {
			if (mWebView.getTitle() != null) {
				text = text.replaceAll("\\{TITLE\\}", mWebView.getTitle());
			}
			if (mWebView.getUrl() != null) {
				text = text.replaceAll("\\{URL\\}", mWebView.getUrl());
			}
		}
		return text;
	}

	private boolean isLinkExternal(String url) {
		for (String rule : WebViewAppConfig.LINKS_OPENED_IN_EXTERNAL_BROWSER) {
			if (url.contains(rule)) return true;
		}
		return false;
	}

	private boolean isLinkInternal(String url) {
		for (String rule : WebViewAppConfig.LINKS_OPENED_IN_INTERNAL_WEBVIEW) {
			if (url.contains(rule)) return true;
		}
		return false;
	}

	private class MyToggledFullscreenCallback implements VideoEnabledWebChromeClient.ToggledFullscreenCallback {
		@Override
		public void toggledFullscreen(boolean fullscreen) {
			if (fullscreen) {
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				getActivity().getWindow().setAttributes(attrs);
				getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			} else {
				WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				getActivity().getWindow().setAttributes(attrs);
				getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
	}

	private class MyWebViewClient extends WebViewClient {
		private boolean mSuccess = true;

		@Override
		public void onPageFinished(final WebView view, final String url) {
			runTaskCallback(() -> {
				if (getActivity() != null && mSuccess) {
					showContent(500); // hide progress bar with delay to show webview content smoothly
					showProgress(false);
					if (WebViewAppConfig.ACTION_BAR_HTML_TITLE) {
						((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(view.getTitle());
					}
					CookieSyncManager.getInstance().sync(); // save cookies
				}
				mSuccess = true;
			});
		}

		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
			runTaskCallback(() -> {
				if (getActivity() != null) {
					mSuccess = false;
					mStatefulLayout.showEmpty();
					showProgress(false);
				}
			});
		}

		@TargetApi(Build.VERSION_CODES.M)
		@Override
		public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
			// forward to deprecated method
			onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (DownloadFileUtility.isDownloadableFile(url)) {
				onDownloadRequested(url, DownloadFileUtility.getFileName(url), null, 0, null, null);
				return true;
			} else if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
				// load url listener
				((LoadUrlListener) getActivity()).onLoadUrl(url);

				// determine for opening the link externally or internally
				boolean external = isLinkExternal(url);
				boolean internal = isLinkInternal(url);
				if (!external && !internal) {
					external = WebViewAppConfig.OPEN_LINKS_IN_EXTERNAL_BROWSER;
				}

				// open the link
				if (external) {
					IntentUtility.startWebActivity(getContext(), url);
					return true;
				} else {
					showProgress(true);
					return false;
				}
			} else if (url != null && url.startsWith("file://")) {
				// load url listener
				((LoadUrlListener) getActivity()).onLoadUrl(url);
				return false;
			} else {
				return IntentUtility.startIntentActivity(getContext(), url);
			}
		}
	}
}
