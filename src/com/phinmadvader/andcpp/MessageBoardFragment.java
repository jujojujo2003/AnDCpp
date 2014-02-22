package com.phinmadvader.andcpp;

import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCMessage;

/**
 * Created by invader on 8/11/13.
 */
public class MessageBoardFragment extends Fragment {
	private MainActivity mainActivity;
	private TextView msg_board;
	private int rid;
	private final static String MSGBOARD_SAVE_BUNDLE_KEY = "MSGBOARDSAVE";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.mainActivity = (MainActivity) getActivity();

		ScrollView sv = new ScrollView(mainActivity);
		msg_board = new TextView(mainActivity);
		sv.addView(msg_board);
		rid = new Random().nextInt();
		Log.d("andcpp", "MSGBOARD CREATION : " + rid);

		List<String> current_messages = mainActivity.mService
				.get_board_messages();
		if (current_messages != null) {
			for (String msg : current_messages) {
				msg_board.append(msg + "\n");
			}
		}
		return sv;
	}

	public void add_msg(final String msg) {
		if (msg_board != null) {
			mainActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					msg_board.append(msg + "\n");
				}
			});
		}
	}
}
