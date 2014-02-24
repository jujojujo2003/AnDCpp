package com.phinmadvader.andcpp;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MyNestedViewPager extends ViewPager {

	public MyNestedViewPager(Context context) {
		super(context);
	}

	public MyNestedViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		// https://stackoverflow.com/questions/7098868/viewpager-inside-viewpager
		if (v != this && v instanceof ViewPager) {
			ViewPager vp = (ViewPager) v;
			vp.canScrollHorizontally(dx); // requires API14 (ICS+)
		}
		return super.canScroll(v, checkV, dx, x, y);
	}

}
