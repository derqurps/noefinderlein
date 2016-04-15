package at.qurps.noefinderlein.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayAdapter_Mainlist extends ArrayAdapter<Location_NoeC> /*implements SectionIndexer*/ {

	//private final List<Location> list;
	private List<Location_NoeC> originalData = null;
	private List<Location_NoeC> filteredData = null;
	private final Context context;
	private String filterstring;
	private boolean[] filtertyp;
	private static final String TAG = "Mainlist-ArrayAdapter";
    //private static String sections = "abcdefghijklmnopqrstuvwxyz";

	public ArrayAdapter_Mainlist(Context context, List<Location_NoeC> list) {
		super(context, R.layout.listitem_main, list);
		this.context = context;
		//this.list = list;
		this.filteredData = list ;
		this.originalData = list ;
	}

	static class ViewHolder {
		protected TextView sortnumber;
		protected TextView name;
		protected TextView ort;
		protected ImageView topausflug;
		protected ImageView burgstiftusw;
        protected ImageView hund;
        protected ImageView rollstuhl;
        protected ImageView kinderwagen;
        protected ImageView gruppe;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = LayoutInflater.from(context);
			view = inflator.inflate(R.layout.listitem_main, null);
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.sortnumber = (TextView) view.findViewById(R.id.menuitem_sortnumber);
			viewHolder.name = (TextView) view.findViewById(R.id.menuitem_name);
			viewHolder.ort = (TextView) view.findViewById(R.id.menuitem_ort);
			viewHolder.topausflug = (ImageView) view.findViewById(R.id.topausflug);
			viewHolder.burgstiftusw = (ImageView) view.findViewById(R.id.burgschloessusw);
            viewHolder.hund = (ImageView) view.findViewById(R.id.menuitem_hund);
            viewHolder.rollstuhl = (ImageView) view.findViewById(R.id.menuitem_rollstuhl);
            viewHolder.kinderwagen = (ImageView) view.findViewById(R.id.menuitem_kinderwagen);
            viewHolder.gruppe = (ImageView) view.findViewById(R.id.menuitem_gruppe);

			view.setTag(viewHolder);
			//viewHolder.checkbox.setTag(list.get(position));
		} else {
			view = convertView;
			//((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		//Log.d(TAG,String.valueOf(list.get(position).getSort()));
		int nummer = filteredData.get(position).getNummer();
		if(nummer!= 0) {
			holder.sortnumber.setVisibility(View.VISIBLE);
			holder.sortnumber.setText(String.valueOf(nummer));
		}else{
			holder.sortnumber.setVisibility(View.GONE);
		}
		holder.name.setText(filteredData.get(position).getName());
		holder.ort.setText(filteredData.get(position).getAdr_ort());
		if (!filteredData.get(position).getTop_ausflugsziel())
		{
			holder.topausflug.setVisibility(View.GONE);
		}
		else
		{
			holder.topausflug.setVisibility(View.VISIBLE);
		}
        int category = filteredData.get(position).getKat();
		switch (category)
		{
		case 1:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_stifte);
			break;
		}
		case 2:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_burgen_schloesser);
			break;
		}
		case 3:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_museen_ausstellungen);
			break;
		}
		case 4:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_erlebnis_natur);
			break;
		}
		case 5:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_sport_und_freizeit);
			break;
		}
		case 6:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_bergbahnen);
			break;
		}
		case 7:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_schifffahrt);
			break;
		}
		case 8:
		{
			holder.burgstiftusw.setImageResource(R.mipmap.ic_lokalbahn);
			break;
		}
		}
        if (!filteredData.get(position).getHund())
        {
            holder.hund.setVisibility(View.GONE);
        }
        else
        {
            holder.hund.setVisibility(View.VISIBLE);
        }
        if (!filteredData.get(position).getRollstuhl())
        {
            holder.rollstuhl.setVisibility(View.GONE);
        }
        else
        {
            holder.rollstuhl.setVisibility(View.VISIBLE);
        }
        if (!filteredData.get(position).getKinderwagen())
        {
            holder.kinderwagen.setVisibility(View.GONE);
        }
        else
        {
            holder.kinderwagen.setVisibility(View.VISIBLE);
        }
        if (!filteredData.get(position).getGruppe())
        {
            holder.gruppe.setVisibility(View.GONE);
        }
        else
        {
            holder.gruppe.setVisibility(View.VISIBLE);
        }
		return view;
	}
	@Override
	public int getCount() {
		return filteredData!=null ? filteredData.size() : 0;
	}
	public Location_NoeC getLocationtoPosition(int position) {
		return filteredData.get(position);
	}
	public int getNumbertoPosition(int position) {
		return filteredData.get(position).getNummer();
	}
	public int getIdtoPosition(int position) {
		return filteredData.get(position).getId();
	}
    public int getJahrtoPosition(int position) {
        return filteredData.get(position).getJahr();
    }
	public void filterwithtyp(boolean[] filtertyploc)
	{
		filtertyp=filtertyploc;
		executefilter();
	}
	public void filterwithstring(String filterstr)
	{
		filterstring=filterstr;
		if(filtertyp==null)
		{
            filtertyp=new boolean[10];
			Arrays.fill(filtertyp, Boolean.TRUE);
		}
		executefilter();
	}
	public String getItemsString()
	{
		String returnstring="";
		for (int i = 0; i < filteredData.size(); i++) {
			returnstring=returnstring+String.valueOf(filteredData.get(i).getId())+";";
		}
		return returnstring;
	}
    public int getItemsYear() {
        int returnyear=0;
        returnyear=filteredData.get(0).getJahr();
        return returnyear;
    }
    public float getGesamtErsparnis() {
        float ersparnis = 0;
        for (int i = 0; i < filteredData.size(); i++) {
            try {
                ersparnis = ersparnis + Float.parseFloat(filteredData.get(i).getErsparnis().replace("€","").replace(" " ,"").replace("-",""));
            }
            catch (Exception e) {

            }
        }
        return ersparnis;
    }
	private void executefilter()
	{
		boolean[] filterbool= filtertyp;

		final List<Location_NoeC> firstlist = originalData;
		int count = firstlist.size();
		final ArrayList<Location_NoeC> firstnlist = new ArrayList<Location_NoeC>(count);
		boolean zusfilt = true;
		for (boolean t:filterbool) {
			if(t){
				zusfilt = false;
				break;
			}
		}
		Location_NoeC filterableLocation ;
		for (int i = 0; i < count; i++) {
			
			filterableLocation = firstlist.get(i);
			int[] bla = {0,0,0,0,0,0,0,0,0,0,0,0,0};
			if(zusfilt){
				firstnlist.add(filterableLocation);
				continue;
			}
			

            if(filterbool.length>8 && filterbool[8]) {
                if (filterableLocation.getHund()) {
                    bla[8]=1;
                }else {
					bla[8]=2;
				}
            }
            if(filterbool.length>9 && filterbool[9]) {
                if (filterableLocation.getRollstuhl()) {
					bla[9]=1;
                }else {
					bla[9]=2;
				}
            }
            if(filterbool.length>10 && filterbool[10]) {
                if (filterableLocation.getKinderwagen()) {
					bla[10]=1;
                }else {
					bla[10]=2;
				}
            }
            if(filterbool.length>11 && filterbool[11]) {
                if (filterableLocation.getGruppe()) {
					bla[11]=1;
                }else {
					bla[11]=2;
				}
            }
			if(filterbool.length>12 && filterbool[12]) {
				if (filterableLocation.getTop_ausflugsziel()) {
					bla[12]=1;
				}else {
					bla[12]=2;
				}
			}
			boolean minZusFilter = true;
			for (int j = 8; j < bla.length; j++) {
				if(bla[j]==2){
					minZusFilter = false;
					break;
				}
			}
			if(!minZusFilter){
				continue;
			}

			int category = filterableLocation.getKat();
			for(int j=1;j<filterbool.length;j++)
			{
				if(filterbool[j-1] && category==j){
					firstnlist.add(filterableLocation);
					break;
				}
			}	
		}


		int nummer;
		String filterString = filterstring;
		final List<Location_NoeC> list = firstnlist;
		if(filterString != null && filterString.length() > 0)
		{
			count = list.size();
			final ArrayList<Location_NoeC> nlist = new ArrayList<Location_NoeC>(count);

			try
			{
				nummer = Integer.valueOf(filterstring);
				for (int i = 0; i < count; i++) {
					filterableLocation = list.get(i);

					if (filterableLocation.getNummer()==nummer) {
						nlist.add(filterableLocation);
					}
				}
			}
			catch  (Exception e)
			{
				for (int i = 0; i < count; i++) {
					filterableLocation = list.get(i);

					if (filterableLocation.getName().toLowerCase().contains(filterString)) {
						nlist.add(filterableLocation);
					}
				}
			}


			filteredData = nlist;
		}
		else
		{
			filteredData = list;
		}
		
		if (filteredData.size() > 0) {
			notifyDataSetChanged();
		} else {
			notifyDataSetInvalidated();
		}
	}
	
}
