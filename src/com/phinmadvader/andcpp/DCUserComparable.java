package com.phinmadvader.andcpp;

import java.util.Comparator;

import android.util.Log;

import com.phinvader.libjdcpp.DCUser;

public class DCUserComparable extends DCUser implements Comparable<DCUser>{

	@Override
	public int compareTo(DCUser another) {
		String s1 = this.nick;
		String s2 = another.nick;
		if(s1.equalsIgnoreCase(s2))
				return 0;
		return -1;
	}
	
	public DCUserComparable(DCUser dcuser){
		super(dcuser);
	}
	
	public DCUserComparable(String nick){
		// DUmmy Constructor, only to be used for delete
		this.nick = nick;
		
	}
	
	
	public static class ShareSizeCompare implements Comparator<DCUserComparable>{

		@Override
		public int compare(DCUserComparable arg0, DCUserComparable arg1) {
			return (int)arg0.share_size - (int)arg1.share_size;
		}
		
	}

}
