package com.phinmadvader.andcpp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCUser;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by invader on 8/11/13.
 */
public class LoginView extends LinearLayout {
    ConnectActivity connectActivity;
    View view;
    ViewPageAdapter pageAdapter;
    TextView downloadLocation;
    Button connectButton;
    EditText nickText;
    EditText ipText;
    public static String ConnectString = "Connect";
    public static String DisconnectString = "Disconnect";

    public LoginView( ConnectActivity connectActivity, ViewPageAdapter pageAdapter) {
        super(connectActivity);
        this.connectActivity = connectActivity;
        this.pageAdapter = pageAdapter;
        LayoutInflater mInflater = (LayoutInflater)connectActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.login_layout, this, true);
        downloadLocation = (TextView) findViewById(R.id.textView5);
        nickText = (EditText) findViewById(R.id.editText2);
        ipText = (EditText) findViewById(R.id.editText);
        downloadLocation.setText(Constants.dcDirectory);
        connectButton = (Button) findViewById(R.id.button);
        connectButton.setText(ConnectString);
        connectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectButton.getText().equals(ConnectString))
                    connect();
                else
                    disconnect();
            }
        });

        //Get preference
        String ip = connectActivity.settings.getString(Constants.SETTINGS_IP_KEY,"");
        String nick = connectActivity.settings.getString(Constants.SETTINGS_NICK_KEY,"");
        nickText.setText(nick);
        ipText.setText(ip);
    }

    private void disconnect() {
        connectActivity.mService.shutdown();
        pageAdapter.resetToStackSize(1);
        connectButton.setText(ConnectString);
    }

    private void connect() {

        String nick = nickText.getText().toString();
        String ip = ipText.getText().toString();

        //Check Valid IP
        if(!InetAddressUtils.isIPv4Address(ip)) {
            Toast.makeText(connectActivity,"Invalid IP Address", Toast.LENGTH_LONG).show();
            return;
        }

        if(nick.length() == 0) {
            Toast.makeText(connectActivity,"Invalid Nickname", Toast.LENGTH_LONG).show();
            return;
        }

        //Store in preference
        SharedPreferences.Editor editor = connectActivity.settings.edit();
        editor.putString(Constants.SETTINGS_NICK_KEY, nick);
        editor.putString(Constants.SETTINGS_IP_KEY, ip);
        editor.commit();

        //Check if download folder exists , else create
        File folder = new File(Constants.dcDirectory);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
            Toast.makeText(connectActivity,"Created download directory!", Toast.LENGTH_SHORT).show();
        }
        if(!success) {
            Toast.makeText(connectActivity,"Unable to create/access download directory", Toast.LENGTH_LONG).show();
        }
        //Connect
        connectActivity.serviceIntent = new Intent(connectActivity, DCPPService.class);
        connectActivity.serviceIntent.putExtra("nick", nick);
        connectActivity.serviceIntent.putExtra("ip", ip);
        connectActivity.mService.setUser_handler(new DCCommand() {
            @Override
            public void onCommand(DCMessage dcMessage) {
                pageAdapter.userHandler(dcMessage);
            }
        });
        connectActivity.startService(connectActivity.serviceIntent);
    }
   public void refreshNickList(){
       if(connectActivity.mService == null)
           return;
       if(connectActivity.mService.get_status() != DCPPService.DCClientStatus.CONNECTED)
           return;
       pageAdapter.resetToStackSize(1);
       pageAdapter.incrementStack();
       connectButton.setText(DisconnectString);
       connectActivity.moveToPage(1);
       List<DCUser> nickList = connectActivity.mService.get_nick_list();
       List<String> nickListString = new ArrayList<String>();
       for(int i=0;i<nickList.size();i++){
          nickListString.add(nickList.get(i).nick);
       }
       Log.e("YEAH HERE WOTH ",""+nickListString.size());
       connectActivity.adapter.addNick(nickListString);
       Log.e("YEah shud be done","done");
       connectActivity.adapter.userListView.setConnectedUserCount(connectActivity.adapter.nickList.size());
   }
}
