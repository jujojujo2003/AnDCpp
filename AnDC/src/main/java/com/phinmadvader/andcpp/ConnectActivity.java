package com.phinmadvader.andcpp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ConnectActivity extends FragmentActivity {
    // Declare Variables
    public ViewPager viewPager;
    public PagerAdapter adapter;
    public Intent serviceIntent;
    public DCPPService mService;
    SharedPreferences settings;
    boolean mBound = false;

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
    }

    // Not using options menu in this tutorial
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connect, menu);
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
    }

    public void moveToPage(int page) {
        viewPager.setCurrentItem(page);
    }
}
