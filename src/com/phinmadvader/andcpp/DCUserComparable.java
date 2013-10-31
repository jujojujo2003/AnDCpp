package com.phinmadvader.andcpp;

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

}
