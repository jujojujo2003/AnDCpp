package com.phinmadvader.andcpp;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by invader on 8/11/13.
 */
public class LoginFragment extends Fragment {
	private MainActivity mainActivity;
	private TextView downloadLocation;
	private Button connectButton;
	private EditText nickText;
	private EditText ipText;
	private static String ConnectString = "Connect";
	private static String DisconnectString = "Disconnect";
	private boolean is_connected = false;
	private static String VIEW_IS_CONNECTED_BUNDLE_KEY = "VIEWCONNECTSTATE";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.login_layout, container,
				false);
		downloadLocation = (TextView) rootView.findViewById(R.id.textView5);
		nickText = (EditText) rootView.findViewById(R.id.editText2);
		ipText = (EditText) rootView.findViewById(R.id.editText);
		downloadLocation.setText(Constants.dcDirectory);
		connectButton = (Button) rootView.findViewById(R.id.button);
		connectButton.setText(ConnectString);
		connectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!is_connected)
					connect();
				else
					disconnect();
			}
		});

		// Get preference
		String ip = mainActivity.settings.getString(Constants.SETTINGS_IP_KEY,
				"");
		String nick = mainActivity.settings.getString(
				Constants.SETTINGS_NICK_KEY, "");
		nickText.setText(nick);
		ipText.setText(ip);
		is_connected = savedInstanceState
				.getBoolean(VIEW_IS_CONNECTED_BUNDLE_KEY);
		setViewState(is_connected);
		return rootView;
	}

	/**
	 * Called to update view when poller service changes state (On
	 * connect/disconnect)
	 * 
	 * @param is_connected
	 */
	public void setViewState(boolean is_connected) {
		this.is_connected = is_connected;
		if (is_connected) {
			connectButton.setText(DisconnectString);
			nickText.setEnabled(false);
			ipText.setEnabled(false);
		} else {
			connectButton.setText(ConnectString);
			nickText.setEnabled(true);
			ipText.setEnabled(true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(VIEW_IS_CONNECTED_BUNDLE_KEY, is_connected);
	}

	@Override
	public void onStart() {
		super.onStart();
		this.mainActivity = (MainActivity) getActivity();
	}

	@Override
	public void onStop() {
		super.onStop();
		this.mainActivity = null;
	}

	public void disconnect() {
		mainActivity.stopBackgroundService();
		connectButton.setText(ConnectString);
	}

	public void connect() {
		String nick = nickText.getText().toString();
		String ip = ipText.getText().toString();

		// Check Valid IP
		if (!InetAddressUtils.isIPv4Address(ip)) {
			Toast.makeText(mainActivity, "Invalid IP Address",
					Toast.LENGTH_LONG).show();
			return;
		}

		if (nick.length() == 0) {
			Toast.makeText(mainActivity, "Invalid Nickname", Toast.LENGTH_LONG)
					.show();
			return;
		}

		// Store in preference
		SharedPreferences.Editor editor = mainActivity.settings.edit();
		editor.putString(Constants.SETTINGS_NICK_KEY, nick);
		editor.putString(Constants.SETTINGS_IP_KEY, ip);
		editor.commit();

		// Connect
		mainActivity.startBackgroundService(nick, ip);
	}
}
