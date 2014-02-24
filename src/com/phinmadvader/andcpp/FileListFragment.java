package com.phinmadvader.andcpp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Path.FillType;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCFileList;

public class FileListFragment extends Fragment {
	private MainActivity mainActivity;
	public boolean is_ready = false;

	public List<DCFileList> filelist_stack;

	private FileListSectionAdapter mFilelistSectionAdapter;
	private ViewPager mViewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		filelist_stack = new ArrayList<DCFileList>();

		View rootview = inflater.inflate(R.layout.file_list_view_pager,
				container, false);
		mFilelistSectionAdapter = new FileListSectionAdapter(
				getFragmentManager());
		mViewPager = (ViewPager) rootview.findViewById(R.id.pager_file_list);
		// mViewPager.setOffscreenPageLimit(32); //TODO: fix hack, to prevent
		// unloading fragments
		mViewPager.setAdapter(mFilelistSectionAdapter);

		is_ready = true;
		return rootview;
	}

	public void refreshToNewFileList() {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				filelist_stack.clear();
				filelist_stack.add(mainActivity.rootfileList);
				mFilelistSectionAdapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * Element at depth , adds a new directory listing of dir at depth = depth +
	 * 1
	 * 
	 * @param depth
	 * @param dir
	 */
	public void openDirectory(int depth, DCFileList dir) {
		while (filelist_stack.size() > depth + 1)
			filelist_stack.remove(filelist_stack.size() - 1);
		filelist_stack.add(dir);
		mFilelistSectionAdapter.notifyDataSetChanged();
		mViewPager.setCurrentItem(filelist_stack.size());
		// make new pager update
	}

	public class FileListSectionAdapter extends FragmentPagerAdapter {

		public FileListSectionAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int pos) {
			return DirectoryViewFragment.newInstance(pos);
		}

		@Override
		public int getCount() {
			return filelist_stack.size();
		}

	}
}
