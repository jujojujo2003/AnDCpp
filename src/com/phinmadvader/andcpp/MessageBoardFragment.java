package com.phinmadvader.andcpp;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class MessageBoardFragment extends Fragment {
	private MainActivity mainActivity;
	private TextView msg_board;
	public boolean is_ready = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.mainActivity = (MainActivity) getActivity();

		ScrollView sv = new ScrollView(mainActivity);
		msg_board = new TextView(mainActivity);
		sv.addView(msg_board);

		List<String> current_messages = mainActivity.mService
				.get_board_messages();
		if (current_messages != null) {
			for (String msg : current_messages) {
				msg_board.append(msg + "\n");
			}
		}
		is_ready = true;
		return sv;
	}

	public void add_msg(final String msg) {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				msg_board.append(msg + "\n");
			}
		});
	}
}
