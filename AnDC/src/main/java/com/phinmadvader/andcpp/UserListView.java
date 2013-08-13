package com.phinmadvader.andcpp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by invader on 8/13/13.
 */
public class UserListView extends LinearLayout{
    ConnectActivity connectActivity;
    View view;
    ViewPageAdapter pageAdapter;
    ListView userListView;
    TextView connectedUserCount;


    public UserListView( ConnectActivity connectActivity, ViewPageAdapter pageAdapter) {
        super(connectActivity);
        this.connectActivity = connectActivity;
        this.pageAdapter = pageAdapter;
        LayoutInflater mInflater = (LayoutInflater)connectActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.userlist_layout, this, true);
        userListView = (ListView) findViewById(R.id.listView);
        userListView.setAdapter(pageAdapter.userListAdapter);
        connectedUserCount = (TextView) findViewById(R.id.textView2);
    }

    public void setConnectedUserCount(int count) {
        connectedUserCount.setText(String.valueOf(count));
    }
}
