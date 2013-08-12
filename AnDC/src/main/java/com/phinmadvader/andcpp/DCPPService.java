package com.phinmadvader.andcpp;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.phinvader.libjdcpp.DCClient;
import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;
import com.phinvader.libjdcpp.DCPreferences;
import com.phinvader.libjdcpp.DCUser;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by phinfinity on 11/08/13.
 */
public class DCPPService extends IntentService {
    public static enum DCClientStatus {
        DISCONNECTED, INVALIDIP, CONNECTED
    }

    private DCClientStatus status = DCClientStatus.DISCONNECTED;
    private boolean is_connected = false;
    private DCClient client = null;
    private DCCommand board_message_handler = null;
    private DCCommand user_handler = null;
    private DCCommand search_handler = null;

    public void setBoard_message_handler(DCCommand handler) {
        board_message_handler = handler;
    }
    public void setUser_handler(DCCommand handler) {
        user_handler = handler;
    }
    public void setSearch_handler(DCCommand handler) {
        search_handler = handler;
    }

    private class MyUserHandler implements DCCommand {
        @Override
        public void onCommand(DCMessage dcMessage) {
            if(user_handler != null)
                user_handler.onCommand(dcMessage);
        }
    }
    private class MySearchHandler implements DCCommand {
        @Override
        public void onCommand(DCMessage dcMessage) {
            if(search_handler != null)
                search_handler.onCommand(dcMessage);
        }
    }
    private class MyBoardMessageHandler implements  DCCommand {
        @Override
        public void onCommand(DCMessage dcMessage) {
            if(board_message_handler != null)
                board_message_handler.onCommand(dcMessage);
        }
    }

    public DCClientStatus get_status() {
        return status;
    }

    public List<DCUser> get_nick_list() {
        return client.get_nick_list();
    }

    public DCPPService() {
        super("DCPP Service");
    }


    public class LocalBinder extends Binder {
        DCPPService getService() {
            return DCPPService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    public IBinder onBind(Intent intent) {
        Log.d("dcpp_service", "Binding to service");
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (!is_connected) {
            Bundle data = intent.getExtras();
            if (data.containsKey("nick") && data.containsKey("ip")) {
                String nick = data.getString("nick");
                String ip = data.getString("ip");
                DCPreferences prefs = new DCPreferences(nick, 0, ip);
                DCUser myuser = new DCUser();
                myuser.nick = nick;
                client = new DCClient();
                try {
                    client.connect(ip, 411, prefs);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client.bootstrap();
                client.setCustomUserChangeHandler(new MyUserHandler());
                client.setCustomBoardMessageHandler(new MyBoardMessageHandler());
                client.setCustomSearchHandler(new MySearchHandler());
                client.InitiateDefaultRouting();
                status = DCClientStatus.CONNECTED;
                is_connected = true;
                Intent notification_intent = new Intent(this, ConnectActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notification_intent, 0);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("AnDC++ Client Running")
                        .setContentText("AnDC++ is currently running")
                        .setTicker("Connecting to hub " + ip)
                        .setContentIntent(pendingIntent);
                startForeground(1, mBuilder.build());
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("dcpp_service", "Received intent without nick or ip parameters");
                status = DCClientStatus.INVALIDIP;
            }
        } else {
            Log.e("dcpp_service", "Attempted to connect while already connected");
            status = DCClientStatus.DISCONNECTED;
        }
    }

    public void shutdown() {
        if (is_connected) {
            is_connected = false;
            status=DCClientStatus.DISCONNECTED;
            stopForeground(true);
            synchronized (this) {
                this.notify();
            }
            // TODO Shutdown the DClient.
        }
    }
}
