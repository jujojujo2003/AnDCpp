package com.phinmadvader.andcpp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.phinmadvader.andcpp.DCPPService.LocalBinder;
import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCUser;

import java.util.ArrayList;
import java.util.Collections;

public class TestActivity extends Activity {
    private Handler mHandler = new Handler();
    private DCPPService mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            finish();
        }
    };
    private LinearLayout l2;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout l = new LinearLayout(this);
        Button start_service = new Button(this);
        start_service.setText("Start Service");
        start_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, DCPPService.class);
                intent.putExtra("nick", "n7libjdcpp");
                intent.putExtra("ip", "10.2.16.126");
                startService(intent);
            }
        });
        l.addView(start_service);
        Button shutdown = new Button(this);
        shutdown.setText("Shutdown Service");
        shutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.shutdown();
            }
        });
        l.addView(shutdown);
        Button get_list = new Button(this);
        get_list.setText("Get NickList");
        get_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService != null && mService.get_status() == DCPPService.DCClientStatus.CONNECTED) {
                    String s = "";
                    ArrayList<String> a = new ArrayList<String>();
                    for (DCUser user : mService.get_nick_list()) {
                        a.add(user.nick + (user.active ? "(A)" : "(P)") + ",");
                    }
                    Collections.sort(a);
                    for (String u : a) {
                        s += u;
                    }
                    tv.setText(s);
                } else {
                    tv.setText("No Service");
                }
            }
        });
        l.addView(get_list);
        Button get_status = new Button(this);
        get_status.setText("Get STatus");
        get_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    tv.setText(mService.get_status().toString());
                } else {
                    tv.setText("No bound");
                }
            }
        });
        l.addView(get_status);
        Button start_listener = new Button(this);
        start_listener.setText("Start Handlers");
        start_listener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (b.getText().toString().equals("Start Handlers")) {
                    DCCommand my_handler = new DCCommand() {
                        @Override
                        public void onCommand(final DCMessage dcMessage) {
                            mHandler.post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            tv.append("\n" + dcMessage.toString());
                                        }
                                    });
                        }
                    };
                    mService.setBoard_message_handler(my_handler);
                    mService.setUser_handler(my_handler);
                    mService.setSearch_handler(my_handler);
                    b.setText("Stop Handlers");
                } else {
                    mService.setBoard_message_handler(null);
                    mService.setUser_handler(null);
                    mService.setSearch_handler(null);
                    b.setText("Start Handlers");
                }
            }
        });
        l.addView(start_listener);
        l2 = new LinearLayout(this);
        l2.setOrientation(LinearLayout.VERTICAL);
        tv = new TextView(this);
        tv.setText("Hello World from no xml!");
        l2.addView(l);
        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        l2.addView(sv);
        setContentView(l2);
        //setContentView(R.layout.connect_activity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
}
