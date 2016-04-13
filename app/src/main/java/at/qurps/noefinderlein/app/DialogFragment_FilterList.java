package at.qurps.noefinderlein.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;


public class DialogFragment_FilterList extends DialogFragment {

	ImageView[] imgviewlist=new ImageView[13];
	RelativeLayout[] RelativeLayoutlist=new RelativeLayout[13];
	TextView[] textviewlist=new TextView[13];
	RelativeLayout RelativeLayoutfilter_all;
	ImageView imgviewfilter_all;
	TextView textviewfilter_all;
	boolean[] filterlist=new boolean[13];
    public static final String TAG = "FilterListDialogFragment";

	private NoticeDialogListener mListener;
	private int bgcolor_notactive=Color.rgb(224, 224, 224);

	float brightness = (float)(50);
	float[] colorMatrix = {
            0.33f, 0.33f, 0.33f, 0, brightness, //red
            0.33f, 0.33f, 0.33f, 0, brightness, //green
            0.33f, 0.33f, 0.33f, 0, brightness, //blue
            0, 0, 0, 1, 0    //alpha
          };

    ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

	public DialogFragment_FilterList() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		String[] dest_typ_list = getResources().getStringArray(R.array.filter_typ_list);
		for(int i=0;i<filterlist.length;i++)
		{
			filterlist[i]=sharedPref.getBoolean(dest_typ_list[i], true);
		}
		
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View View_kompl =inflater.inflate(R.layout.dialog_filter, null);
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		imgviewlist[0]= (ImageView)View_kompl.findViewById(R.id.toggletop);
		imgviewlist[1]= (ImageView)View_kompl.findViewById(R.id.togglestift);
		imgviewlist[2]= (ImageView)View_kompl.findViewById(R.id.toggleburguschloe);
		imgviewlist[3]= (ImageView)View_kompl.findViewById(R.id.togglemuseeuausstell);
		imgviewlist[4]= (ImageView)View_kompl.findViewById(R.id.toggleerlebnatur);
		imgviewlist[5]= (ImageView)View_kompl.findViewById(R.id.togglesportufreiz);
		imgviewlist[6]= (ImageView)View_kompl.findViewById(R.id.togglebergbahn);
		imgviewlist[7]= (ImageView)View_kompl.findViewById(R.id.toggleschiffahrt);
		imgviewlist[8]= (ImageView)View_kompl.findViewById(R.id.togglelokalbahn);
        imgviewlist[9]= (ImageView)View_kompl.findViewById(R.id.togglehund);
        imgviewlist[10]= (ImageView)View_kompl.findViewById(R.id.togglerollstuhl);
        imgviewlist[11]= (ImageView)View_kompl.findViewById(R.id.togglekinderwagen);
        imgviewlist[12]= (ImageView)View_kompl.findViewById(R.id.togglegruppen);
		
		RelativeLayoutlist[0]= (RelativeLayout)View_kompl.findViewById(R.id.rel_toggletop);
		RelativeLayoutlist[1]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglestift);
		RelativeLayoutlist[2]= (RelativeLayout)View_kompl.findViewById(R.id.rel_toggleburguschloe);
		RelativeLayoutlist[3]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglemuseeuausstell);
		RelativeLayoutlist[4]= (RelativeLayout)View_kompl.findViewById(R.id.rel_toggleerlebnatur);
		RelativeLayoutlist[5]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglesportufreiz);
		RelativeLayoutlist[6]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglebergbahn);
		RelativeLayoutlist[7]= (RelativeLayout)View_kompl.findViewById(R.id.rel_toggleschiffahrt);
		RelativeLayoutlist[8]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglelokalbahn);
        RelativeLayoutlist[9]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglehund);
        RelativeLayoutlist[10]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglerollstuhl);
        RelativeLayoutlist[11]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglekinderwagen);
        RelativeLayoutlist[12]= (RelativeLayout)View_kompl.findViewById(R.id.rel_togglegruppen);
		
		textviewlist[0]=(TextView)View_kompl.findViewById(R.id.text_toggletop);
		textviewlist[1]=(TextView)View_kompl.findViewById(R.id.text_togglestift);
		textviewlist[2]=(TextView)View_kompl.findViewById(R.id.text_toggleburguschloe);
		textviewlist[3]=(TextView)View_kompl.findViewById(R.id.text_togglemuseeuausstell);
		textviewlist[4]=(TextView)View_kompl.findViewById(R.id.text_toggleerlebnatur);
		textviewlist[5]=(TextView)View_kompl.findViewById(R.id.text_togglesportufreiz);
		textviewlist[6]=(TextView)View_kompl.findViewById(R.id.text_togglebergbahn);
		textviewlist[7]=(TextView)View_kompl.findViewById(R.id.text_toggleschiffahrt);
		textviewlist[8]=(TextView)View_kompl.findViewById(R.id.text_togglelokalbahn);
        textviewlist[9]=(TextView)View_kompl.findViewById(R.id.text_togglehund);
        textviewlist[10]=(TextView)View_kompl.findViewById(R.id.text_togglerollstuhl);
        textviewlist[11]=(TextView)View_kompl.findViewById(R.id.text_togglekinderwagen);
        textviewlist[12]=(TextView)View_kompl.findViewById(R.id.text_togglegruppen);
		
		RelativeLayoutfilter_all= (RelativeLayout)View_kompl.findViewById(R.id.rel_toggleall);
		imgviewfilter_all= (ImageView)View_kompl.findViewById(R.id.toggleall);
		textviewfilter_all=(TextView)View_kompl.findViewById(R.id.text_toggleall);

        if(areAllFiltered()){
            for (int i = 0; i < filterlist.length; i++) {
                setInactive(i);
                filterlist[i]=false;
            }
        }
        else {
            for (int i = 0; i < filterlist.length; i++) {
                if (!filterlist[i]) {
                    setInactive(i);
                } else {
                    setActive(i);
                }
            }
        }
		setAllOnOffFilter();
		Dialog builder = new AlertDialog.Builder(getActivity())
		.setIcon(R.drawable.ic_filter_full)
		.setTitle(R.string.title_menu_filter)
		// Add action buttons
		.setView(View_kompl)
		.setInverseBackgroundForced(true)
		.setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				String[] dest_typ_list = getResources().getStringArray(R.array.filter_typ_list);
                boolean[] neueliste = filterlistForPositive(filterlist);
				for(int i=0;i<neueliste.length;i++)
				{
					editor.putBoolean(dest_typ_list[i], neueliste[i]);
				}
				editor.commit();

				mListener.onDialogPositiveClick(neueliste);
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				DialogFragment_FilterList.this.getDialog().cancel();
			}
		}).create(); 
		
		RelativeLayoutfilter_all.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int anzchecked=checkifAllfiltered();
				if(anzchecked==filterlist.length)
				{
					Arrays.fill(filterlist, Boolean.FALSE);
				}
				else if(anzchecked==0)
				{
					Arrays.fill(filterlist, Boolean.TRUE);
				}
				else
				{
					Arrays.fill(filterlist, Boolean.TRUE);
				}
				for(int i=0;i<filterlist.length;i++)
				{
					if(filterlist[i])
						setActive(i);
					else
						setInactive(i);
				}
				setAllOnOffFilter();
			}
		});
		
		
		for(int i=0;i<filterlist.length;i++)
		{
			final int j=i;
			RelativeLayoutlist[i].setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					filterlist[j]=!filterlist[j]; // change its state to the oposite one

					if(filterlist[j])
					{
						setActive(j);
					}
					else
					{
						setInactive(j);
					}
					setAllOnOffFilter();
				}
			});
		}
		return builder;
	}
	private boolean[] filterlistForPositive(boolean[] filterlistinnen){

        if(areNoneFiltered(filterlistinnen)){
            boolean[] returnfilterlist = new boolean[filterlistinnen.length];
            Arrays.fill(returnfilterlist, Boolean.TRUE);
            return returnfilterlist;
        }
        else {
            return filterlistinnen;
        }
    }
	private void setInactive(int i)
	{
		imgviewlist[i].setColorFilter(colorFilter);
		RelativeLayoutlist[i].setBackgroundColor(bgcolor_notactive);
	}
	private void setActive(int i)
	{
		imgviewlist[i].setColorFilter(null);
		RelativeLayoutlist[i].setBackgroundColor(Color.TRANSPARENT);
	}
	public interface NoticeDialogListener {
        public void onDialogPositiveClick(boolean[] filterlist);
    }
	
	public void setListener(NoticeDialogListener listener) {
        mListener = listener;
    }
	private int checkifAllfiltered(){
		int truevar=0;
		for(int i=0;i<filterlist.length;i++)
		{
			if(filterlist[i])
				truevar++;
		}
		return truevar;
	}
    public boolean areNoneFiltered(){
        return areNoneFiltered(filterlist);
    }
    public boolean areNoneFiltered(boolean[] liste){
        boolean returnvar = true;
        for(int i=0;i<liste.length;i++)
        {
            if(liste[i])
                returnvar=false;
        }
        return returnvar;
    }
    public boolean areAllFiltered(){
        boolean returnvar = true;
        for(int i=0;i<filterlist.length;i++)
        {
            if(!filterlist[i])
                returnvar=false;
        }
        return returnvar;
    }
	private void setAllOnOffFilter(){
		int truevar=checkifAllfiltered();
		if(truevar==filterlist.length)
		{
			RelativeLayoutfilter_all.setBackgroundColor(Color.TRANSPARENT);
			imgviewfilter_all.setImageResource(R.mipmap.ic_hakerl_active);
			//textviewfilter_all;
		}
		else if(truevar==0)
		{
			RelativeLayoutfilter_all.setBackgroundColor(bgcolor_notactive);
			imgviewfilter_all.setImageResource(R.mipmap.ic_hakerl_not_active);
		}
		else
		{
			RelativeLayoutfilter_all.setBackgroundColor(Color.TRANSPARENT);
			imgviewfilter_all.setImageResource(R.mipmap.ic_hakerl_half_active);
		}
	}
}