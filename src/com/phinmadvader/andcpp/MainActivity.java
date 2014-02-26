package com.phinmadvader.andcpp;

import java.io.File;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;

public class MainActivity extends FragmentActivity implements
		OnQueryTextListener, DCCommand {

	DCPPService mService;
	boolean mBound = false;

	private Poller poller;
	private SearchView searchview;
	private MenuItem searchmenuitem;

	private TabPagerAdapter tab_page_adapter;
	private ViewPager view_pager;
	private LoginFragment login_fragment;
	private MessageBoardFragment messageboard_fragment;
	private UserListFragment userlist_fragment;
	private FileListFragment filelist_fragment;
	private SearchResultsFragment searchresult_fragment;

	// FileList stack variables
	// TODO: This ought to be nested in some filelist management class
	// (filelistfragment? sth new?) and not strewn around randomly as presently
	public DCFileList rootfileList;
	public String chosenNick;

	public FileListFragment get_filelist_fragment() {
		return filelist_fragment;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DCPPService.LocalBinder binder = (DCPPService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
			Log.d("andcpp", "mservice is bound");
			// All callbacks forwarded by MainActivity
			mService.setBoard_message_handler(MainActivity.this);
			mService.setSearch_handler(MainActivity.this);
			mService.setUser_handler(MainActivity.this);
			// Reffer onCommand(msg) for above callback setups
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			finish();
			Log.d("andcpp", "mservice is got disconnected");
		}
	};

	@Override
	public void onCommand(final DCMessage msg) {
		// These all cause UI changes, so force run on UI thread by default
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (msg.command.equals("MyINFO")) {
					if (userlist_fragment != null && userlist_fragment.is_ready)
						userlist_fragment.addOneNick(msg);
				} else if (msg.command.equals("Quit")) {
					if (userlist_fragment != null && userlist_fragment.is_ready)
						userlist_fragment.delNick(new DCUserComparable(
								msg.quit_s));
				} else if (msg.command.equals("SR")) {
					searchresult_fragment.add_search_result(msg);
				} else if (msg.command.equals("BoardMessage")) {
					if (messageboard_fragment != null
							&& messageboard_fragment.is_ready)
						messageboard_fragment.add_msg(msg.msg_s);
				}
			}
		});
	}

	public SharedPreferences getprefs() {
		return getSharedPreferences(Constants.PREFS_NAME, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, DCPPService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		setContentView(R.layout.main_activity);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		view_pager = (ViewPager) findViewById(R.id.pager_root);
		view_pager.setOffscreenPageLimit(32); // TODO : FIXTHIS
		// ^ this is a hack, basically without this fragments keep getting
		// recreated which is painful for us to maintain state, moreover
		// saveinstance doesn't seem to work on loginframgnet, resulting in
		// loginfragment being reset to a state without a disconnect button
		// which is problematic
		tab_page_adapter = new TabPagerAdapter(getSupportFragmentManager(),
				getActionBar(), view_pager); // attaches and manages
												// view_pager

		login_fragment = new LoginFragment();
		messageboard_fragment = new MessageBoardFragment();
		userlist_fragment = new UserListFragment();
		filelist_fragment = new FileListFragment();
		searchresult_fragment = new SearchResultsFragment();
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_LOGININFO,
				(Fragment) login_fragment, "Login Info");
		Log.d("andcpp", "CREATION");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// DON'T Save fragment states..
		// TODO get aroun this workaround
		// This was introduced to fix a bug where rotating display after
		// connecting
		// breaks everything. because service is not ready but android continues
		// previous fragments.
		// Possible solutioN: don't allow fragments to use service directly, and
		// proxy somehow
		// super.onSaveInstanceState(outState); // if uncommented will save and
		// may
		// break
		Log.d("andcpp", "MainActivity Saving state");
	}

	@Override
	protected void onDestroy() {
		tab_page_adapter = null;
		login_fragment = null;
		messageboard_fragment = null;
		userlist_fragment = null;
		filelist_fragment = null;
		searchresult_fragment = null;
		chosenNick = null;
		rootfileList = null;
		view_pager.removeAllViews();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connect, menu);
		searchmenuitem = menu.findItem(R.id.action_search);
		searchview = (SearchView) searchmenuitem.getActionView();
		searchview.setSubmitButtonEnabled(true);
		searchview.setOnQueryTextListener(this);
		searchmenuitem.setVisible(false);
		return true;
	}

	@Override
	protected void onPause() {
		poller.stop();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		poller = new Poller(this); // start poller, will take care of service
									// connect/disconnect updates
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Nothing to do here , not possible auto-complete
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		searchview.setQuery("", false);
		searchview.clearFocus();
		
		searchresult_fragment.clear_search_results();
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_SEARCHLIST,
				searchresult_fragment, "Search Results");
		mService.make_search(query);
		return false;
	}

	public void startBackgroundService(String nick, String ip) {
		// Check if download folder exists , else create
		File folder = new File(Constants.dcDirectory);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
			Toast.makeText(this, "Created download directory!",
					Toast.LENGTH_SHORT).show();
		}
		if (!success) {
			Toast.makeText(this, "Unable to create/access download directory",
					Toast.LENGTH_LONG).show();
		}

		Intent serviceIntent = new Intent(this, DCPPService.class);
		serviceIntent.putExtra("nick", nick);
		serviceIntent.putExtra("ip", ip);
		startService(serviceIntent);
	}

	public void stopBackgroundService() {
		mService.shutdown();
	}

	/**
	 * Called by Poller when background dcppservice connects Only UI changes to
	 * be carried out (called on UI Thread)
	 */
	public void onConnectUIChanges() {
		Log.d("andcpp", "Running Connect UI changes");
		searchmenuitem.setVisible(true);
		login_fragment.setViewState(true);
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_HUBCHAT,
				messageboard_fragment, "HubChat");
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_USERLIST,
				userlist_fragment, "UserList");
	}

	/**
	 * Called by Poller Change UI to disconnected state will be called on UI
	 * thread
	 */
	public void onDisonnectUIChanges() {
		Log.d("andcpp", "Running disconnect UI changes");
		searchmenuitem.setVisible(false);
		login_fragment.setViewState(false);
		tab_page_adapter.hide_tab(TabPagerAdapter.TAB_HUBCHAT);
		tab_page_adapter.hide_tab(TabPagerAdapter.TAB_USERLIST);
		tab_page_adapter.hide_tab(TabPagerAdapter.TAB_FILELIST);
		tab_page_adapter.hide_tab(TabPagerAdapter.TAB_SEARCHLIST);
	}

	public void open_file_list(DCUserComparable user) {
		// TODO: COMPLETE CODE FOR OPENING FILE LIST
		final String nick = user.nick;
		if (!user.active) {
			Toast.makeText(this, "Cannot download from Passive (Red) users.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Toast.makeText(this, "Fetching file list of " + nick,
				Toast.LENGTH_SHORT).show();
		Thread fileDownloadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				final DCFileList fileList = mService.get_file_list(nick);

				if (fileList == null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this,
									"Slot not available.", Toast.LENGTH_LONG)
									.show();
						}
					});
					return;
				}
				Log.i("andcpp", "File size : " + Long.toString(fileList.size));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								"User FileList download done",
								Toast.LENGTH_LONG).show();
						MainActivity.this.rootfileList = fileList;
						MainActivity.this.chosenNick = nick;
						filelist_fragment.refreshToNewFileList();
						Log.d("andcpp", "First file name : "
								+ fileList.children.get(0).name);
						tab_page_adapter.add_tab(TabPagerAdapter.TAB_FILELIST,
								filelist_fragment, nick + "'s FileList");
						// now launch tab
						// fileList is ur file lsit
						// Make a FileListView and append
						// TODO DO STH
					}
				});

			}
		});
		fileDownloadThread.start();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{  
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	    	int currentItem = view_pager.getCurrentItem();
	    	if(currentItem > 0)
	    		 view_pager.setCurrentItem(currentItem - 1);
	    }
	    return false; 
	}
	

	
}
