package com.phinmadvader.andcpp;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListAdapter extends ArrayAdapter<DCUserComparable> {

	private final Context context;
	private final List<DCUserComparable> nickList;
	private List<DCUserComparable> filtered;
	private Filter filter;

	public UserListAdapter(Context context, int textViewResourceId,
			List<DCUserComparable> nickList) {
		super(context, textViewResourceId, nickList);

		this.context = context;
		
			this.nickList = nickList;
		this.filtered = nickList;
		

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = inflater.inflate(R.layout.row_element, parent, false);
		TextView title = (TextView) rowView.findViewById(R.id.text11);
		TextView support = (TextView) rowView.findViewById(R.id.text12);
		ImageView image = (ImageView) rowView.findViewById(R.id.icon);

		DCUserComparable user;

		user = nickList.get(position);

		title.setText(user.nick);
		support.setText("Share :"
				+ Double.toString(user.share_size / (1024 * 1024 * 1024))
				+ "GB \t" + user.email);
		if (user.active)
			image.setImageResource(R.drawable.green_user);
		else
			image.setImageResource(R.drawable.red_user);
		return rowView;

	}

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new DCUserFilter();
		return filter;
	}

	public class DCUserFilter extends Filter {

		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence con) {
			FilterResults result = new FilterResults();
			

			String constraint = con.toString().toLowerCase();
			Log.i("CONSTRAINT", Integer.toString(nickList.size()));
			if (constraint != null && constraint.length() > 0) {
				ArrayList<DCUserComparable> filteredDCUsers = new ArrayList<DCUserComparable>();
				ArrayList<DCUserComparable> allDCUsers = new ArrayList<DCUserComparable>();
				synchronized (this) {
					for (int i = 0; i < nickList.size(); i++)
						allDCUsers.add(nickList.get(i));
				}
				for (int i = 0; i < allDCUsers.size(); i++) {
					DCUserComparable user = allDCUsers.get(i);
					if (user.nick.toLowerCase().contains(constraint)) {
						filteredDCUsers.add(user);
					}
				}
				result.count = filteredDCUsers.size();
				result.values = filteredDCUsers;
			} else {
				synchronized (this) {
					result.count = nickList.size();
					result.values = nickList;
				}
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			filtered = (ArrayList<DCUserComparable>) results.values;
			notifyDataSetChanged();
			clear();

			for (int i = 0, l = filtered.size(); i < l; i++)
				add(filtered.get(i));
			notifyDataSetInvalidated();

		}

	}
}
