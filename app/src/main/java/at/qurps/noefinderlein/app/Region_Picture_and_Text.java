package at.qurps.noefinderlein.app;

public class Region_Picture_and_Text {

	String _entry;
	int _drawable;
	int _nummer;

	public Region_Picture_and_Text(){

	}
	public Region_Picture_and_Text(String entry, int drawable){
		this._entry=entry;
		this._drawable=drawable;
	}
	public Region_Picture_and_Text(String entry, int drawable, int nummer){
		this._entry=entry;
		this._drawable=drawable;
		this._nummer=nummer;
	}
	public String getEntry()
	{
		return this._entry;
	}
	public void setEntry(String entry)
	{
		this._entry=entry;
	}
	
	public int getDrawable()
	{
		return this._drawable;
	}
	public void setDrawable(int drawable)
	{
		this._drawable=drawable;
	}
	
	public int getNummer()
	{
		return this._nummer;
	}
	public void setNummer(int nummer)
	{
		this._nummer=nummer;
	}
}
