package org.openintents.cloudsync;

import java.util.Calendar;
import java.util.Date;

import org.openintents.cloudsync.util.Ulg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class SyncTimeArrayAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
 
	public SyncTimeArrayAdapter(Context context, String[] values) {
		super(context, R.layout.list_mobile, values);
		this.context = context;
		this.values = values;
		Ulg.d("values of this.values inside the adapter:-> "+this.values.length);
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View rowView = inflater.inflate(R.layout.list_mobile, parent, false);
		TextView textViewAbove = (TextView) rowView.findViewById(R.id.above_cell_text);
		TextView textViewBelow = (TextView) rowView.findViewById(R.id.below_cell_text);
		
		ImageView imageView = (ImageView) rowView.findViewById(R.id.logo);
        //textViewAbove.setText(values[position]);
		textViewAbove.setText("OI NotePad");
		if(values[position].equalsIgnoreCase("noval")) {
			textViewBelow.setText("Not Synced Yet!");
		} else {
			long timeInMillis = Long.parseLong(values[position]);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timeInMillis);
			Date date = cal.getTime();
			textViewBelow.setText((CharSequence) date.toString());
		}
		// Change icon based on name
		String s = values[position];
//		if (s.equals("WindowsMobile")) {
//			imageView.setImageResource(R.drawable.logo_windows_30);
		
		return rowView;
	}
}