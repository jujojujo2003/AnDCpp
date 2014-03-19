package com.phinmadvader.andcpp;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Settings extends Activity{
	
	private EditText inputDirectoryTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//although it's called file list, it's the exact code needed here, so I'm not going to make a new view.
		setContentView(R.layout.file_list);
		ListView lv = (ListView)findViewById(R.id.listView);
		String []settingsOptionsArray = getResources().getStringArray(R.array.settings_options_list);
		
		ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.settings_list_element_layout, R.id.elementString, settingsOptionsArray);
		
		lv.setAdapter(myArrayAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parentAdapterView, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Log.d("andcpp", "settings item "+id+" clicked , position "+position);
				switch(position){
				case 0:
					changeDownloadsDirectory();
					return;
				default:
					Log.d("andcpp", "this is unimplemented");
				}
			}
		});
	}
	
	public void changeDownloadsDirectory(){
		DialogInterface.OnClickListener positive;
		positive = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				putDCDownloadsDirectory(inputDirectoryTextView.getText().toString(), getApplicationContext());
				Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
			}
		};
		inputDirectoryTextView	= new EditText(Settings.this);
		inputDirectoryTextView.setHint("Enter the directory here...");
		
		DisplayAlert("Enter new Downloads Directory", 
				"Current Directory is "+getDCDownloadsDirectory(getApplicationContext()), 
				"Save", positive, 
				"Cancel", null,
				inputDirectoryTextView);
	}
	
	public String DisplayAlert(String title, //Title for the alert
			String message, //Message for the alert
			String positiveString, //Positive option string
			DialogInterface.OnClickListener positiveOnClick, //Positive on click listener
			String negativeString, //Negative option string
			DialogInterface.OnClickListener negativeOnClick, //Negative on click listener
			View v //Extra view to be added
			){
	//lv.setAdapter();
	
	AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(Settings.this);
	AlertDialog myAlert = myAlertBuilder
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(positiveString, positiveOnClick)
			.setNegativeButton(negativeString, negativeOnClick)
			.setCancelable(false)
			.setView(v)
			.create();
			myAlert.show();
	return null;
	}
	
	// Helper functions.
	public static String getDCDownloadsDirectory(Context c) {
		SharedPreferences settingsSharedPreferences = c.getSharedPreferences(
				"settingsSharedPreferences", 0);
		String dcDir = settingsSharedPreferences.getString("dcDirectory",
				Constants.dcDirectory);
		// Check if download folder exists , else create
		File folder = new File(dcDir);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
			Toast.makeText(c, "Created download directory!",
					Toast.LENGTH_SHORT).show();
		}
		if (!success) {
			Toast.makeText(c, "Unable to create/access download directory",
					Toast.LENGTH_LONG).show();
		}
		return dcDir;
	}
       
    public static void putDCDownloadsDirectory(String dcDir, Context c) {
    	SharedPreferences settingsSharedPreferences = c.getSharedPreferences("settingsSharedPreferences", 0);
    	settingsSharedPreferences.edit().putString("dcDirectory", dcDir).commit();
    }
}
