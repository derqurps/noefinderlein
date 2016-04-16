package at.qurps.noefinderlein.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ArrayAdapter_Region extends ArrayAdapter<Region_Picture_and_Text> {

	private final Activity context;
	private static final String TAG = "Region-ArrayAdapter";
	private List<Region_Picture_and_Text> list = null;

	public ArrayAdapter_Region(Activity context, List<Region_Picture_and_Text> list)
	{
		super(context, R.layout.listitem_region, list);
		this.context = context;
		this.list=list;
	}
	
	static class ViewHolder {
		protected ImageView regionbild;
		protected TextView regionname;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		View view = null;
		if (convertView == null) 
		{
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.listitem_region, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.regionbild = (ImageView) view.findViewById(R.id.region_list_img);
			viewHolder.regionname = (TextView) view.findViewById(R.id.region_list_name);
			view.setTag(viewHolder);
		}
		else
		{
			view = convertView;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		String bla=list.get(position).getEntry();
		
		holder.regionname.setText(list.get(position).getEntry());
		holder.regionbild.setImageResource(list.get(position).getDrawable());
		return view;
	}
	
	@Override
	public int getCount() {
		return list!=null ? list.size() : 0;
	}
	
	public int getNumbertoPosition(int position) {
		return list.get(position).getNummer();
	}
	public String getNametoPosition(int position) {
		return list.get(position).getEntry();
	}
}
