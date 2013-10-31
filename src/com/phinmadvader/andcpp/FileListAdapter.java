package com.phinmadvader.andcpp;

import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.phinvader.libjdcpp.DCFileList;

public class FileListAdapter extends ArrayAdapter<DCFileList>{

	Context context;
	List<DCFileList> files ;
	

	public static class FileType{
		public static int MUSIC = 1;
		public static int PDF = 2; 
		public static int VIDEO = 3; 
		public static int DOC = 4; 
		public static int IMAGE = 5;
		public static int EXE = 6;
		public static int INTERNET = 7;
		public static int ISO = 8;
		public static int TEXT = 9 ;
		public static int COMPRESSED = 10 ; 
		public static int OTHER = 100;
		
		public static String[] VideoExtensions = {"mkv","mp4","rmvb","avi","mpeg","flv","mpg","wmv","divx"};
		public static String[] MusicExtensions = {"mp3","wav","aac","ogg"};
		public static String[] ImageExtensions = {"bmp","jpg","jpeg","png","gif","ico","jpe"};
		public static String[] DocExtensions = {"xls","doc","ppt","xlsx","pptx","docx","odt","odp"};
		public static String[] exeExtensions = {"exe","msi","dll"};
		public static String[] pdfExtensions = {"pdf"};
		public static String[] isoExtensions = {"iso"};
		public static String[] internetExtensions = {"htm","html","js","css"};
		public static String[] textExtensions = {"txt","dat","xml","rtf","srt","chm"};
		public static String[] compressedExtensions = {"zip","7z","tar","gz","rar"};
		
		
		public static boolean isContained(String ext,String[] list){
			for(int i = 0 ; i < list.length; i++){
				if(ext.trim().equalsIgnoreCase(list[i])){
					return true;
				}
			}
			return false;
		}
		
		public static int getType(String ext){
			if(isContained(ext, VideoExtensions))
				return VIDEO;
			if(isContained(ext, MusicExtensions))
				return MUSIC;
			if(isContained(ext, ImageExtensions))
				return IMAGE;
			if(isContained(ext, DocExtensions))
				return DOC;
			if(isContained(ext, exeExtensions))
				return EXE;
			if(isContained(ext, internetExtensions))
				return INTERNET;
			if(isContained(ext, isoExtensions))
				return ISO;
			if(isContained(ext, textExtensions))
				return TEXT;
			if(isContained(ext, compressedExtensions))
				return COMPRESSED;
			if(isContained(ext, pdfExtensions))
				return PDF;
			return OTHER;
		}
	}
	
	
	
	public FileListAdapter(Context context, int textViewResourceId,
			List<DCFileList> files) {
		super(context, textViewResourceId, files);
		this.context = context;
		this.files = files;

	}
	
	private int getFileExtension(String filename){
		String[] splits = filename.split("\\.");
		String ext = splits[splits.length-1];
		return FileType.getType(ext);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	
	LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	// TODO Auto-generated constructor stub
	View rowView = inflater.inflate(R.layout.row_element, parent, false);

	TextView title = (TextView) rowView.findViewById(R.id.text11);
	TextView support = (TextView) rowView.findViewById(R.id.text12);
	ImageView image = (ImageView) rowView.findViewById(R.id.icon);
	
	DCFileList file = files.get(position);
	title.setText(file.name);
	
	int icon =  R.drawable.other;
	
	support.setText(Long.toString(file.size));
	

	if(file.isDirectory()){
		icon = R.drawable.folder;
	}
	
	int ext = getFileExtension(file.name);
	if(ext == FileType.MUSIC)
		icon = R.drawable.music;
	else if(ext == FileType.VIDEO)
		icon = R.drawable.video;
	else if(ext == FileType.DOC)
		icon = R.drawable.office;
	else if(ext == FileType.PDF)
		icon = R.drawable.pdf;
	else if (ext == FileType.IMAGE)
		icon = R.drawable.image;
	else if(ext == FileType.INTERNET)
		icon = R.drawable.internet;
	else if(ext == FileType.EXE)
		icon = R.drawable.exe;
	else if(ext==FileType.ISO)
		icon = R.drawable.iso;
	else if(ext==FileType.TEXT)
		icon = R.drawable.txt;
	else if(ext==FileType.COMPRESSED)
		icon = R.drawable.zip;
	
	image.setImageResource(icon);
	
	return rowView;
	}


}
