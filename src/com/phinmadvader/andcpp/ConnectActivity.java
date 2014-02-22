package com.phinmadvader.andcpp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class ConnectActivity extends FragmentActivity implements OnQueryTextListener {
    // Declare Variables
    public ViewPager viewPager;
    public ViewPageAdapter adapter;
    public Intent serviceIntent;
    public DCPPService mService;
    SharedPreferences settings;
    boolean mBound = false;
    Poller poller;
    private SearchView searchview;
    private MenuItem searchmenuitem;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the view from viewpager_main.xml
        setContentView(R.layout.connect_activity);

        settings = getSharedPreferences(Constants.PREFS_NAME, 0);

        // Locate the ViewPager in viewpager_main.xml
        viewPager = (ViewPager) findViewById(R.id.pager);
        // Pass results to ViewPagerAdapter Class
        adapter = new ViewPageAdapter(this);
        // Binds the Adapter to the ViewPager
        viewPager.setAdapter(adapter);


        //Start Poller
        //poller = new Poller(this);
    }

    // Not using options menu in this tutorial
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect, menu);
        searchmenuitem = menu.findItem(R.id.action_search);
        searchview = (SearchView) searchmenuitem.getActionView();
        searchview.setSubmitButtonEnabled(true);
        searchview.setOnQueryTextListener(this);
        // TODO : when disconnected searchmenuitme.setVisible(false), and on connect only make search visible
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, DCPPService.class), mConnection, Context.BIND_AUTO_CREATE);

        if(mService != null) {
        if(mService.get_status() == DCPPService.DCClientStatus.CONNECTED){
            if(adapter.loginView!=null)
            adapter.loginView.connectButton.setText(adapter.loginView.ConnectString);
        } else {
            if(adapter.loginView!=null)
            adapter.loginView.connectButton.setText(adapter.loginView.DisconnectString);
        }
      }
    }

    public void moveToPage(int page) {
        viewPager.setCurrentItem(page);
    }

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Nothing to do here , not possible auto-complete
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mService.make_search(query);
		return false;
	}
}
