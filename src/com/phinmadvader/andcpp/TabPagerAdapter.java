package com.phinmadvader.andcpp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public class TabPagerAdapter extends FragmentPagerAdapter implements
		TabListener, OnPageChangeListener {

	public final static int TAB_LOGININFO = 0;
	public final static int TAB_HUBCHAT = 1;
	public final static int TAB_USERLIST = 2;
	public final static int TAB_SEARCHLIST = 3;
	public final static int TAB_FILELIST = 4;
	private final static int TOTAL_TABS = 5;

	private boolean[] tab_is_visible;
	private Fragment[] tab_fragment;
	private Tab[] tab_list;
	private String[] tab_title;

	private ActionBar actionbar;
	private ViewPager viewpager;

	public TabPagerAdapter(FragmentManager fm, ActionBar actionbar,
			ViewPager viewpager) {
		super(fm);
		this.actionbar = actionbar;
		tab_is_visible = new boolean[TOTAL_TABS];
		tab_fragment = new Fragment[TOTAL_TABS];
		tab_list = new Tab[TOTAL_TABS];
		viewpager.setOnPageChangeListener(this);
		viewpager.setAdapter(this);
	}

	public void hide_tab(int tabid) {
		tab_is_visible[tabid] = false;
		tab_fragment[tabid] = null;
		actionbar.removeTab(tab_list[tabid]);
		tab_list[tabid] = null;
		notifyDataSetChanged();
	}

	public void add_tab(int tabid, Fragment fragment, String title) {
		if (tab_is_visible[tabid] == false) {
			tab_list[tabid] = actionbar.newTab();
			tab_list[tabid].setTabListener(this);
		}
		tab_is_visible[tabid] = true;
		tab_fragment[tabid] = fragment;
		tab_title[tabid] = title;
		tab_list[tabid].setText(title);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		int tab_count = 0;
		for (int i = 0; i < TOTAL_TABS; i++)
			if (tab_is_visible[i])
				tab_count++;
		return tab_count;
	}

	private int get_tabid(int position) {
		for (int i = 0; i < TOTAL_TABS; i++) {
			if (tab_is_visible[i])
				position--;
			if (position < 0) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public Fragment getItem(int position) {
		int tabid = get_tabid(position);
		if (tabid >= 0)
			return tab_fragment[tabid];
		return null;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		int tabid = get_tabid(position);
		if (tabid >= 0)
			return tab_title[tabid];
		return super.getPageTitle(position);
	}

	@Override
	public void onPageSelected(int position) {
		actionbar.setSelectedNavigationItem(position);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewpager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}
}
