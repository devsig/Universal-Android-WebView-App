package com.robotemplates.webviewapp.view;

import android.content.Context;
import android.util.AttributeSet;

import name.cpr.VideoEnabledWebView;

public class RoboWebView extends VideoEnabledWebView {
	private OnScrollListener mOnScrollListener = null;

	public interface OnScrollListener {
		void onScrollChanged(RoboWebView roboWebView, int x, int y, int oldx, int oldy);
	}

	public RoboWebView(Context context) {
		super(context);
	}

	public RoboWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoboWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		super.onScrollChanged(x, y, oldx, oldy);
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollChanged(this, x, y, oldx, oldy);
		}
	}

	public void setOnScrollListener(OnScrollListener onScrollListener) {
		mOnScrollListener = onScrollListener;
	}
}
