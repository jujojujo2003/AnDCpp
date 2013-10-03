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

import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by invader on 8/11/13.
 */
public class ViewPageAdapter extends PagerAdapter{
    // Declare Variables
    LayoutInflater inflater;
    int uiStackSize = 1;
    ConnectActivity connectActivity;
    ArrayList<String> nickList;
    Set<String> nickSet;
    ArrayAdapter<String> userListAdapter;
    List<FileListView> fileListViewList;
    List<String> atLocation;
    DCFileList fileList;
    String chosenNick;

    //Views
    public LoginView loginView;
    public UserListView userListView;

    public ViewPageAdapter(ConnectActivity connectActivity) {
        this.connectActivity = connectActivity;
        nickList = new ArrayList<String>();
        userListAdapter = new ArrayAdapter<String>(connectActivity, android.R.layout.simple_list_item_1, nickList );
        loginView = new LoginView(connectActivity, this);
        userListView = new UserListView(connectActivity, this);
        nickSet = new TreeSet<String>();
        fileListViewList = new ArrayList<FileListView>();
        atLocation = new ArrayList<String>();
    }

    public void addNick(final List<String> nicks) {
        synchronized (nickSet) {
        for (int i = 0;i<nicks.size();i++)
          if(nickSet.contains(nicks.get(i)))
          {
              nicks.remove(i);
          }
        ((Activity)connectActivity).runOnUiThread(new Runnable()
        {
            public void run()
            {

        nickSet.addAll(nicks);
        nickList.addAll(nicks);
        Collections.sort(nickList);
        userListAdapter.notifyDataSetChanged();
            }});
        }
    }

    public void delNick(final String nick) {
        synchronized (nickSet) {
        if(!nickSet.contains(nick))
            return;
        ((Activity)connectActivity).runOnUiThread(new Runnable()
        {
            public void run()
            {
                for(int i = 0 ; i < nickList.size() ; i++) {
                    if(nickList.get(i).equals(nick)) {
                        nickList.remove(i);
                        break;
                    }
                }
                nickSet.remove(nick);
                userListAdapter.notifyDataSetChanged();
            }});
        }
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
            itemView = fileListViewList.get(position-2);
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
                fileListViewList.clear();
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
                    List<String> nlist = new ArrayList<String>();
                    nlist.add(message.myinfo.nick);
                    addNick(nlist);
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
                    delNick(message.quit_s);
                    if(userListView != null) {
                        userListView.setConnectedUserCount(nickList.size());
                    }
                }
            }
            );
        }
    }
}
