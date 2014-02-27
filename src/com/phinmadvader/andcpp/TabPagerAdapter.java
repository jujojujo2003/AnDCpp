package com.phinmadvader.andcpp;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

public class TabPagerAdapter extends FragmentStatePagerAdapter implements
		TabListener, OnPageChangeListener {

	public final static int TAB_LOGININFO = 0;
	public final static int TAB_HUBCHAT = 1;
	public final static int TAB_USERLIST = 2;
	public final static int TAB_SEARCHLIST = 3;
	public final static int TAB_FILELIST = 4;
	private final static int TOTAL_TABS = 5;

	private boolean[] tab_is_visible;
	private int active_tab_count = 0;
	private Fragment[] tab_fragment;
	private Tab[] tab_list;
	private String[] tab_title;

	private ActionBar actionbar;
	private ViewPager viewpager;

	private FileListManager filelist_manager;

	public TabPagerAdapter(FragmentManager fm, ActionBar actionbar,
			ViewPager viewpager, FileListManager flist_manager) {
		super(fm);
		this.actionbar = actionbar;
		this.viewpager = viewpager;
		this.filelist_manager = flist_manager;
		tab_is_visible = new boolean[TOTAL_TABS];
		tab_fragment = new Fragment[TOTAL_TABS];
		tab_list = new Tab[TOTAL_TABS];
		tab_title = new String[TOTAL_TABS];
		active_tab_count = 0;
		viewpager.setOnPageChangeListener(this);
		viewpager.setAdapter(this);
	}

	@Override
	public int getItemPosition(Object object) {
		// This ensures fragments don't get cached globally by position
		// As a result loss in performance, with fragments frequently re-rendered
		// Selectively return new position/POSITION_NONE based on shuffle/removal
		// or unchanged when no change
		return POSITION_NONE; // THIS IS HACK, :-/
	}

	public void hide_tab(int tabid) {
		if (tab_is_visible[tabid]) {
			tab_is_visible[tabid] = false;
			tab_fragment[tabid] = null;
			active_tab_count--;
			actionbar.removeTab(tab_list[tabid]);
			tab_list[tabid] = null;
			notifyDataSetChanged();
		}
	}

	public void add_tab(int tabid, Fragment fragment, String title) {
		boolean tab_exists = tab_is_visible[tabid];
		// Update pager responsibilities
		tab_is_visible[tabid] = true;
		tab_fragment[tabid] = fragment;
		tab_title[tabid] = title;
		notifyDataSetChanged();
		// Update Tab responsibilities (Must be AFTER Pager work)
		if (!tab_exists) {
			int tabpos = 0;
			for (int i = 0; i < tabid; i++)
				if (tab_is_visible[i])
					tabpos++;
			tab_list[tabid] = actionbar.newTab();
			tab_list[tabid].setTabListener(this);
			active_tab_count++;
			actionbar.addTab(tab_list[tabid], tabpos);
		}
		tab_list[tabid].setText(title);
		if (actionbar != null)
			actionbar.setSelectedNavigationItem(tab_list[tabid].getPosition());
	}

	@Override
	public int getCount() {
		int tab_count = 0;
		for (int i = 0; i < TOTAL_TABS; i++)
			if (tab_is_visible[i])
				tab_count++;
		if (tab_is_visible[TAB_FILELIST])
			tab_count += filelist_manager.getStackSize() - 1;
		// ^ -1 for 1 added in for loop because tab is visible
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
		if (tab_is_visible[TAB_FILELIST])
			return TAB_FILELIST;
		else
			return -1;
	}

	@Override
	public Fragment getItem(int position) {
		int tabid = get_tabid(position);
		if (tabid >= 0 && tabid != TAB_FILELIST)
			return tab_fragment[tabid];
		else if (tabid == TAB_FILELIST) {
			for (int i = 0; i < TAB_FILELIST; i++)
				if (tab_is_visible[i])
					position--;
			return DirectoryViewFragment.newInstance(position);

		} else {
			return null;
		}
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
		if (actionbar != null) {
			// Set tab to filelist; (last tab) if position out of range
			if (position >= active_tab_count)
				position = active_tab_count - 1;
			actionbar.setSelectedNavigationItem(position);
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (viewpager != null) {
			viewpager.setCurrentItem(tab.getPosition());
		} else {
			Log.d("andcpp", "ViewPAGER NULL :-/ cannot switch tab on page");
		}
	}

	/**
	 * used on opening new directories of filelist
	 */
	public void move_to_last_page() {
		viewpager.setCurrentItem(getCount() - 1);
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
