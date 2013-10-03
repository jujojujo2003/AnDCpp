package com.phinmadvader.andcpp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCFileList;

/**
 * Created by invader on 8/13/13.
 */
public class UserListView extends LinearLayout{
    ConnectActivity connectActivity;
    View view;
    ViewPageAdapter pageAdapter;
    ListView userListView;
    TextView connectedUserCount;


    public UserListView( final ConnectActivity connectActivity, final ViewPageAdapter pageAdapter) {
        super(connectActivity);
        this.connectActivity = connectActivity;
        this.pageAdapter = pageAdapter;
        LayoutInflater mInflater = (LayoutInflater)connectActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.userlist_layout, this, true);
        userListView = (ListView) findViewById(R.id.listView);
        userListView.setAdapter(pageAdapter.userListAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connectActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pageAdapter.resetToStackSize(2);
                    }
                });
                final String nick = connectActivity.adapter.nickList.get(i);
                Toast.makeText(connectActivity,"Fetching file list",Toast.LENGTH_SHORT).show();
                pageAdapter.chosenNick = nick;
                Thread fileDownloadThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final DCFileList fileList = connectActivity.mService.get_file_list(nick);
                        if(fileList == null)
                        {
                            connectActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(connectActivity, "User not in active mode", Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        connectActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(connectActivity, "User download done", Toast.LENGTH_LONG).show();
                                pageAdapter.fileList = fileList;
                                pageAdapter.atLocation.clear();
                                if(pageAdapter.fileListViewList.isEmpty())
                                    pageAdapter.fileListViewList.add(new FileListView(connectActivity,pageAdapter));
                                else
                                {
                                    pageAdapter.fileListViewList.get(0).refreshLocation();
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
        connectedUserCount = (TextView) findViewById(R.id.textView2);
    }

    public void setConnectedUserCount(int count) {
        connectedUserCount.setText(String.valueOf(count));
    }
}
