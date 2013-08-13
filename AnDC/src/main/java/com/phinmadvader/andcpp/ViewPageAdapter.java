package com.phinmadvader.andcpp;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.phinvader.libjdcpp.DCMessage;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by invader on 8/11/13.
 */
public class ViewPageAdapter extends PagerAdapter{
    // Declare Variables
    LayoutInflater inflater;
    int uiStackSize = 1;
    ConnectActivity connectActivity;
    ArrayList<String> nickList;
    ArrayAdapter<String> userListAdapter;

    //Views
    LoginView loginView;
    UserListView userListView;

    public ViewPageAdapter(ConnectActivity connectActivity) {
        this.connectActivity = connectActivity;
        nickList = new ArrayList<String>();
        userListAdapter = new ArrayAdapter<String>(connectActivity, android.R.layout.simple_list_item_1, nickList );
        loginView = new LoginView(connectActivity, this);
        userListView = new UserListView(connectActivity, this);
    }

    @Override
    public int getCount() {
        return uiStackSize;
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
        if(position == 0) {
            itemView = loginView;
        } else if (position == 1) {
            itemView = userListView;
        } else {
            itemView = inflater.inflate(R.layout.login_layout, container,false);
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
        if(size == 1) {
        ((Activity)connectActivity).runOnUiThread(new Runnable()
        {
            public void run()
            {
                nickList.clear();
                userListAdapter.notifyDataSetChanged();
                if(userListView != null) {
                    userListView.setConnectedUserCount(nickList.size());
                }
            }
        }
        );
        }
    }

    public void userHandler(final DCMessage message) {
        if(message.command.equals("MyINFO")) {


            ((Activity)connectActivity).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    nickList.add(message.myinfo.nick);
                    Collections.sort(nickList);
                    userListAdapter.notifyDataSetChanged();
                    if(userListView != null) {
                        userListView.setConnectedUserCount(nickList.size());
                    }
                }
            }
            );

        } else if (message.command.equals("Quit")) {


            ((Activity)connectActivity).runOnUiThread(new Runnable()
            {
                public void run()
                {
                    for(int i = 0 ; i < nickList.size() ; i++) {
                        if(nickList.get(i).equals(message.quit_s)) {
                            nickList.remove(i);
                            break;
                        }
                    }
                    userListAdapter.notifyDataSetChanged();
                    if(userListView != null) {
                        userListView.setConnectedUserCount(nickList.size());
                    }
                }
            }
            );

        }
    }
}
