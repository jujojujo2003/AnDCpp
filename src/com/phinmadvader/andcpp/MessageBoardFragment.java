package com.phinmadvader.andcpp;

import java.util.List;

import com.phinvader.libjdcpp.DCCommand;
import com.phinvader.libjdcpp.DCMessage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by invader on 8/11/13.
 */
public class MessageBoardFragment extends Fragment implements DCCommand {
	private MainActivity mainActivity;
	private TextView msg_board;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ScrollView sv = new ScrollView(getActivity());
		msg_board = new TextView(getActivity());
		sv.addView(msg_board);
		return sv;
	}

	@Override
	public void onStart() {
		super.onStart();
		this.mainActivity = (MainActivity) getActivity();
		List<String> current_messages = null;
		if (mainActivity != null && mainActivity.mBound)
			current_messages = mainActivity.mService.get_board_messages();
		mainActivity.mService.setBoard_message_handler(this);
		if (current_messages != null) {
			for (String msg : current_messages) {
				msg_board.append(msg + "\n");
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mainActivity.mService.setBoard_message_handler(null);
		this.mainActivity = null;
	}

	@Override
	public void onCommand(final DCMessage msg) {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				msg_board.append(msg.msg_s + "\n");
			}
		});
	}
}
