package com.phinmadvader.andcpp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCFileList;
import com.phinvader.libjdcpp.DCMessage;

public class SearchResultsFragment extends Fragment {
	private MainActivity mainActivity;
	public boolean is_ready = false;

	private ListView fileListView;
	private ArrayAdapter<DCFileList> filesAdapter; // adapter adapted on
													// curFiles
	private List<DCFileList> curFiles; // adapter list being displayed
	private List<DCMessage> searchResults;

	public SearchResultsFragment() {
		super();
		curFiles = new ArrayList<DCFileList>();
		searchResults = new ArrayList<DCMessage>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		ArrayAdapter<DCFileList> myFileAdapter = new FileListAdapter(
				mainActivity, android.R.layout.simple_list_item_1, curFiles);
		View rootview = inflater.inflate(R.layout.file_list, container, false);
		fileListView = (ListView) rootview.findViewById(R.id.listView);
		fileListView.setAdapter(myFileAdapter);
		fileListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView,
							View view, int i, long l) {
						// Download File
						DCMessage selected_search_result = searchResults.get(i);
						String path = selected_search_result.file_path;
						long file_size = selected_search_result.file_size;
						String nick = selected_search_result.hisinfo.nick;

						String[] file_parts = path.split("\\\\");
						String filename = file_parts[file_parts.length - 1];

						Log.e("andcpp", path);
						mainActivity.mService.download_file(nick,
								Constants.dcDirectory + "/" + filename, path,
								file_size);

						mainActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mainActivity,
										"Download Started", Toast.LENGTH_LONG)
										.show();
							}
						});
					}
				});
		this.filesAdapter = myFileAdapter;
		is_ready = true;
		return rootview;
	}

	/**
	 * Adds a new search result entry.
	 * This must be called on UI thread.
	 * 
	 * @param sr
	 */
	public void add_search_result(DCMessage sr) {
		String[] file_parts = sr.file_path.split("\\\\");
		String name = file_parts[file_parts.length - 1];
		name = sr.hisinfo.nick + ": " + name;
		DCFileList entry = new DCFileList(name, sr.file_size);

		curFiles.add(entry);
		searchResults.add(sr);
		if (is_ready)
			filesAdapter.notifyDataSetChanged();
	}

	/**
	 * Clears all search results
	 * Must call on UI thread
	 */
	public void clear_search_results() {
		curFiles.clear();
		searchResults.clear();
		if(is_ready)
			filesAdapter.notifyDataSetChanged();
	}

}