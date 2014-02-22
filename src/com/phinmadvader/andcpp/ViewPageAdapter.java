package com.phinmadvader.andcpp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCUser;

/**
 * Created by invader on 8/11/13.
 */
public class ViewPageAdapter extends PagerAdapter {
	// Declare Variables
	LayoutInflater inflater;
	int uiStackSize = 1;
	ConnectActivity connectActivity;
	List<DCUserComparable> nickList;
	Set<DCUserComparable> nickSet;
	UserListAdapter userListAdapter;
	List<FileListView> fileListViewList;
	List<String> atLocation;
	DCFileList fileList;
	String chosenNick;

	// Views
	public LoginView loginView;
	public UserListView userListView;

	public ViewPageAdapter(ConnectActivity connectActivity) {
		this.connectActivity = connectActivity;
		nickList = new ArrayList<DCUserComparable>();
		userListAdapter = new UserListAdapter(connectActivity,
				android.R.layout.simple_list_item_1, nickList);
		loginView = new LoginView(connectActivity, this);
		userListView = new UserListView(connectActivity, this);
		nickSet = new TreeSet<DCUserComparable>();
		fileListViewList = new ArrayList<FileListView>();
		atLocation = new ArrayList<String>();
	}

	public void addNick(final List<DCUserComparable> nicks) {
		synchronized (nickSet) {
			for (int i = 0; i < nicks.size(); i++)
				if (nickSet.contains(nicks.get(i))) {
					nicks.remove(i);
				}
			((Activity) connectActivity).runOnUiThread(new Runnable() {
				public void run() {

					for (int i = 0; i < nicks.size(); i++){
						nickSet.add(nicks.get(i));
						nickList.add(nicks.get(i));
					}
					Collections.sort(nickList);
					userListAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	public void delNick(final DCUserComparable nick) {
		synchronized (nickSet) {
			if (!nickSet.contains(nick))
				return;
			((Activity) connectActivity).runOnUiThread(new Runnable() {
				public void run() {
					for (int i = 0; i < nickList.size(); i++) {
						if (nickList.get(i).equals(nick)) {
							nickList.remove(i);
							break;
						}
					}
					nickSet.remove(nick);
					userListAdapter.notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	public int getCount() {
		return uiStackSize;
	}
	
	public void clearNicklistConstraints(){
	       if(connectActivity.mService == null)
	           return;
	       if(connectActivity.mService.get_status() != DCPPService.DCClientStatus.CONNECTED)
	           return;
	       //connectActivity.moveToPage(1);
	       List<DCUser> nickList = connectActivity.mService.get_nick_list();
	       List<DCUserComparable> nickListString = new ArrayList<DCUserComparable>();
	       for(int i=0;i<nickList.size();i++){
	          nickListString.add(new DCUserComparable(nickList.get(i)));
	       }
	       connectActivity.adapter.addNick(nickListString);
	       connectActivity.adapter.userListView.setConnectedUserCount(connectActivity.adapter.nickList.size());

	}

	public void incrementStack() {
		uiStackSize++;
		notifyDataSetChanged();
	}

	public void decrementStack() {
		uiStackSize--;
		notifyDataSetChanged();
	}

	public void setStackSize(int size) {
		uiStackSize = size;
		notifyDataSetChanged();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((LinearLayout) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		inflater = (LayoutInflater) connectActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView;
		if (position == 0) {
			itemView = loginView;
		} else if (position == 1) {
			itemView = userListView;
		} else {
			itemView = fileListViewList.get(position - 2);
		}
		// Add viewpager_item.xml to ViewPager
		((ViewPager) container).addView(itemView);

		return itemView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// Remove viewpager_item.xml from ViewPager
		((ViewPager) container).removeView((LinearLayout) object);
	}

	public void resetToStackSize(int size) {
		setStackSize(size);

		if (size == 1) {
			((Activity) connectActivity).runOnUiThread(new Runnable() {
				public void run() {
					nickList.clear();
					fileListViewList.clear();
					userListAdapter.notifyDataSetChanged();
					if (userListView != null) {
						userListView.setConnectedUserCount(nickList.size());
					}
				}
			});
		}
	}

	public void userHandler(final DCMessage message) {
		if (message.command.equals("MyINFO")) {

			((Activity) connectActivity).runOnUiThread(new Runnable() {
				public void run() {
					List<DCUserComparable> nlist = new ArrayList<DCUserComparable>();
					nlist.add(new DCUserComparable(message.myinfo));
					addNick(nlist);
					if (userListView != null) {
						userListView.setConnectedUserCount(nickList.size());
					}
				}
			});

		} else if (message.command.equals("Quit")) {
			((Activity) connectActivity).runOnUiThread(new Runnable() {
				public void run() {
					delNick(new DCUserComparable(message.quit_s));
					if (userListView != null) {
						userListView.setConnectedUserCount(nickList.size());
					}
				}
			});
		}
	}
}
