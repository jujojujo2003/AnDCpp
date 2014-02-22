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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		OnQueryTextListener {
	DCPPService mService;
	boolean mBound = false;

	private Poller poller;
	private SearchView searchview;
	private MenuItem searchmenuitem;

	private TabPagerAdapter tab_page_adapter;
	private ViewPager view_pager;
	private LoginFragment login_fragment;
	private MessageBoardFragment messageboard_fragment;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			DCPPService.LocalBinder binder = (DCPPService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			finish();
		}
	};
	
	public SharedPreferences getprefs() {
		return getSharedPreferences(Constants.PREFS_NAME, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_activity);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		view_pager = (ViewPager) findViewById(R.id.pager);
		tab_page_adapter = new TabPagerAdapter(getSupportFragmentManager(),
				getActionBar(), view_pager); // attaches and manages view_pager

		login_fragment = new LoginFragment();
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_LOGININFO,
				(Fragment) login_fragment, "Login Info");
		messageboard_fragment = new MessageBoardFragment();
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
		super.onPause();
		poller.stop();
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		poller = new Poller(this); // start poller, will take care of service
									// connect/disconnect updates
		bindService(new Intent(this, DCPPService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Nothing to do here , not possible auto-complete
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
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
		Log.d("andcpp","Running Connect UI changes");
		searchmenuitem.setVisible(true);
		login_fragment.setViewState(true);
		tab_page_adapter.add_tab(TabPagerAdapter.TAB_HUBCHAT,
				messageboard_fragment, "HubChat");
	}

	/**
	 * Called by Poller Change UI to disconnected state will be called on UI
	 * thread
	 */
	public void onDisonnectUIChanges() {
		Log.d("andcpp","Running disconnect UI changes");
		searchmenuitem.setVisible(false);
		login_fragment.setViewState(false);
		tab_page_adapter.hide_tab(TabPagerAdapter.TAB_HUBCHAT);
	}

}
