package com.phinmadvader.andcpp;

import java.util.ArrayList;
import java.util.List;

import com.phinvader.libjdcpp.DCFileList;

/**
 * This is a copy of FileListFragment. Intends to just manage the set of
 * filelists, without actually generating views This is used for an approach to
 * avoid the issue (#14) with nested fragments.
 * 
 * @author phinfinity
 * 
 */
public class FileListManager {
	private MainActivity mainActivity;
	public List<DCFileList> filelist_stack;

	public FileListManager(MainActivity activity) {
		mainActivity = activity;
		filelist_stack = new ArrayList<DCFileList>();
		filelist_stack.add(mainActivity.rootfileList);
	}

	/**
	 * Clears the filelist , and resetns to entry from mainActivity MUST CALL
	 * notifyDatasetChanged() on TabPagerAdapter after this
	 */
	public void refreshToNewFileList() {
		filelist_stack.clear();
		filelist_stack.add(mainActivity.rootfileList);
		mainActivity.tab_page_adapter.notifyDataSetChanged();
	}

	/**
	 * Element at depth , adds a new directory listing of dir at depth = depth +
	 * 1 returns the position of the added element (size - 1) MUST CALL
	 * notifyDatasetChanged() on TabPagerAdapter after this and also move the
	 * page automaticall
	 * 
	 * @param depth
	 * @param dir
	 */
	public void openDirectory(int depth, DCFileList dir) {
		while (filelist_stack.size() > depth + 1)
			filelist_stack.remove(filelist_stack.size() - 1);
		filelist_stack.add(dir);
		mainActivity.tab_page_adapter.notifyDataSetChanged();
		mainActivity.tab_page_adapter.move_to_last_page();

	}

	public int getStackSize() {
		return filelist_stack.size();
	}
}
