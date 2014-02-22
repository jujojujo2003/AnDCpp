package com.phinmadvader.andcpp;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.phinmadvader.andcpp.DCPPService.DCClientStatus;

/**
 * Created by invader on 15/9/13.
 * 
 * This class does polling activities
 */
public class Poller {
	MainActivity mainActivity;
	private TimerTask task;
	private Timer mTimer;
	Poller(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		mTimer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				poll();
			}
		};
		mTimer.scheduleAtFixedRate(task, 0, Constants.POLLING_CLASS_TIME_IN_MILLIS);
	}
	
	public void stop() {
		mTimer.cancel();
	}

	// Monitor Service State
	private DCClientStatus currentStatus = DCClientStatus.DISCONNECTED;

	void poll() {
		DCClientStatus newStatus;
		if (mainActivity.mService == null)
			newStatus = DCClientStatus.DISCONNECTED;
		else
			newStatus = mainActivity.mService.get_status();
		if (newStatus != currentStatus) {
			if (currentStatus == DCClientStatus.DISCONNECTED
					&& newStatus == DCClientStatus.CONNECTED) {
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mainActivity.onConnectUIChanges();
					}
				});
			} else if (currentStatus == DCClientStatus.CONNECTED
					&& newStatus == DCClientStatus.DISCONNECTED) {
				mainActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mainActivity.onDisonnectUIChanges();
					}
				});
			}
			currentStatus = newStatus;
		}
	}
}
