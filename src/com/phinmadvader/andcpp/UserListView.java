package com.phinmadvader.andcpp;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCFileList;

/**
 * Created by invader on 8/13/13.
 */
public class UserListView extends LinearLayout {
	ConnectActivity connectActivity;
	View view;
	ViewPageAdapter pageAdapter;
	ListView userListView;
	TextView connectedUserCount;
	EditText filterInput;

	public UserListView(final ConnectActivity connectActivity,
			final ViewPageAdapter pageAdapter) {
		super(connectActivity);
		this.connectActivity = connectActivity;
		this.pageAdapter = pageAdapter;
		LayoutInflater mInflater = (LayoutInflater) connectActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.userlist_layout, this, true);
		userListView = (ListView) findViewById(R.id.listView);
		userListView.setAdapter(pageAdapter.userListAdapter);
		userListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						connectActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								pageAdapter.resetToStackSize(2);
							}
						});
						final DCUserComparable user = connectActivity.adapter.nickList
								.get(i);
						
						final String nick = user.nick;
						
						if(!user.active){
							Toast.makeText(connectActivity,"Cannot download from Passive (Red) users.",Toast.LENGTH_SHORT).show();
							return;
						}
						
						Toast.makeText(connectActivity,
								"Fetching file list of " + nick,
								Toast.LENGTH_SHORT).show();
						pageAdapter.chosenNick = nick;
						Thread fileDownloadThread = new Thread(new Runnable() {
							@Override
							public void run() {
								final DCFileList fileList = connectActivity.mService
										.get_file_list(nick);

								if (fileList == null) {
									connectActivity
											.runOnUiThread(new Runnable() {
												@Override
												public void run() {
													Toast.makeText(
															connectActivity,
															"Slot not available.",
															Toast.LENGTH_LONG)
															.show();
												}
											});
									return;
								}
								Log.i("SIZE",Long.toString(fileList.size));
								connectActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(connectActivity,
												"User download done",
												Toast.LENGTH_LONG).show();
										pageAdapter.fileList = fileList;
										pageAdapter.atLocation.clear();
										if (pageAdapter.fileListViewList
												.isEmpty())
											pageAdapter.fileListViewList
													.add(new FileListView(
															connectActivity,
															pageAdapter));
										else {
											pageAdapter.fileListViewList.get(0)
													.refreshLocation();
										}

										pageAdapter.resetToStackSize(3);
										connectActivity.moveToPage(3);
									}
								});

							}
						});
						fileDownloadThread.start();
					}
				});

		filterInput = (EditText) findViewById(R.id.filterText);
		filterInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int start, int before,
					int count) {
				if (before > count) {

					// TODO : A better way to reload list on <BACKSPACE>
					// This is just a stub for the functionality.

					pageAdapter.nickList.clear();
					pageAdapter.clearNicklistConstraints();
				}
				if (arg0.length() > 0)
					pageAdapter.userListAdapter.getFilter().filter(arg0);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});

		connectedUserCount = (TextView) findViewById(R.id.textView2);
	}

	public void setConnectedUserCount(int count) {
		connectedUserCount.setText(String.valueOf(count));
	}
}
