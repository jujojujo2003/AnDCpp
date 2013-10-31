package com.phinmadvader.andcpp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phinvader.libjdcpp.DCFileList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by invader on 8/13/13.
 */
public class FileListView extends LinearLayout{
    ConnectActivity connectActivity;
    View view;
    ViewPageAdapter pageAdapter;
    ListView fileListView;
    TextView connectedUserCount;

    ArrayAdapter<DCFileList> filesAdapter ;
    List<DCFileList> curFiles;
    List<DCFileList> fileList;
    int depth = 0;//Depth of adapter

    public void refreshLocation()
    {
        //Populate files with the intended list of files
        int atlevel = 0;
        depth = pageAdapter.atLocation.size();
        List<DCFileList> fileLists  = pageAdapter.fileList.children;
        while(atlevel != pageAdapter.atLocation.size())
        {
            for(int i=0;i<fileLists.size();i++)
                if(fileLists.get(i).name.compareTo(pageAdapter.atLocation.get(atlevel)) == 0)
                {
                    fileLists = fileLists.get(i).children;
                }
            atlevel++;
        }
        curFiles.clear();
        fileList = fileLists;
        for(int i=0;i<fileLists.size();i++)
        {
            curFiles.add(fileLists.get(i));
        }
        filesAdapter.notifyDataSetChanged();
        while(pageAdapter.atLocation.size() != depth)
            pageAdapter.atLocation.remove(pageAdapter.atLocation.size()-1);

    }
    public FileListView(final ConnectActivity connectActivity, final ViewPageAdapter pageAdapter) {
        super(connectActivity);
        this.connectActivity = connectActivity;
        this.pageAdapter = pageAdapter;
        curFiles = new ArrayList<DCFileList>();
        filesAdapter = new FileListAdapter(connectActivity, android.R.layout.simple_list_item_1, curFiles );
        LayoutInflater mInflater = (LayoutInflater)connectActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.file_list, this, true);
        fileListView = (ListView) findViewById(R.id.listView);
        fileListView.setAdapter(filesAdapter);
        fileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(fileList.get(i).isDirectory())
                {
                    while(pageAdapter.atLocation.size() != depth)
                        pageAdapter.atLocation.remove(pageAdapter.atLocation.size()-1);


                         pageAdapter.atLocation.add(curFiles.get(i).name);

                    if(pageAdapter.fileListViewList.size()>=depth+2){
                             pageAdapter.fileListViewList.get(depth+1).refreshLocation();

                    }else{
                             pageAdapter.fileListViewList.add(new FileListView(connectActivity,pageAdapter));

                    }
                         pageAdapter.resetToStackSize(3+depth+1);
                         connectActivity.moveToPage(3+depth+1);

                } else {
                    //Download File
                    String path = pageAdapter.atLocation.get(0);
                    for(int j=1;j<pageAdapter.atLocation.size();j++)
                        path += "\\"+pageAdapter.atLocation.get(j);
                    path += "\\"+fileList.get(i).name;
                    Log.e("PATH",path);
                    connectActivity.mService.download_file(pageAdapter.chosenNick,Constants.dcDirectory+"/"+fileList.get(i).name,path,fileList.get(i).size);
                    connectActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(connectActivity, "Download Started", Toast.LENGTH_LONG).show();
                        }
                    });
                }
        }});
        refreshLocation();
    }
}
