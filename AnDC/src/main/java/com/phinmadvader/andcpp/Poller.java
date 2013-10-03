package com.phinmadvader.andcpp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by invader on 15/9/13.
 *
 * This class does polling activities
 */
public class Poller {
    ConnectActivity connectActivity;
    Poller(ConnectActivity connectActivity) {
        this.connectActivity = connectActivity;
        Timer mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                poll();
            }
        }, 0, Constants.POLLING_CLASS_TIME_IN_MILLIS);
    }

    //Monitor Service State
    private DCPPService.DCClientStatus clientStatus = DCPPService.DCClientStatus.DISCONNECTED;
    void poll() {
        if(connectActivity.mService!=null)
        if(connectActivity.mService.get_status() != clientStatus) {

            //Disconnected->Connected
            if(clientStatus == DCPPService.DCClientStatus.DISCONNECTED && connectActivity.mService.get_status() == DCPPService.DCClientStatus.CONNECTED) {
                connectActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectActivity.adapter.loginView.refreshNickList();
                    }
                });
            }
            clientStatus = connectActivity.mService.get_status();
        }
    }
}
