package com.example.accessibilityservicetest;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

public class MyService extends AccessibilityService {

	private WindowManager windows;
	private LinearLayout view;

	@Override
	public void onCreate() {
		super.onCreate();
		
		windows = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		view = new LinearLayout(this);
		
		LayoutParams layout = generateLayout();
		windows.addView(view, layout);
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSPARENT);
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
		return layoutParams;
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		int windowId = event.getWindowId();
		CharSequence packageName = event.getPackageName();
		int eventType = event.getEventType();
		
		Log.i("AccessibilityEvent", "Window: " + windowId + " Package: " + packageName + " Type: " + eventType);
		
		if (packageName.equals("com.sonyericsson.home")) {
//			view.setVisibility(View.VISIBLE);
		}
		
		//Event type TYPE_WINDOW_STATE_CHANGED seems to mean change of app/window
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}
