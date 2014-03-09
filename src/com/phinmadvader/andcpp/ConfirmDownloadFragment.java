package com.phinmadvader.andcpp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ConfirmDownloadFragment extends DialogFragment {
	
	public MainActivity mainActivity;
	public String localFilePath ;
	public String remotePath;
	public long fileSize;
	public String fileName;
	public String nick;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View dialogView = inflater.inflate(R.layout.download_dialog, null);
	    builder.setView(dialogView)
	    // Add action buttons
	           .setPositiveButton("Download", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   	//SOmething :D
	            	   Log.i("MACT",mainActivity.chosenNick+"++"+remotePath);
	            	   
	            	   mainActivity.mService.download_file(
								nick,
								localFilePath, remotePath ,
								fileSize);
/*
						mainActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mainActivity,
										"Download Started",
										Toast.LENGTH_LONG).show();
							}
						});
	         */   	   
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   //NOTHING :P
	               }
	           });  
	    TextView title = (TextView)dialogView.findViewById(R.id.title);
	    title.setText(fileName);
	    TextView user = (TextView)dialogView.findViewById(R.id.user);
	    user.setText(nick);
	    
	    return builder.create();
	}

}
