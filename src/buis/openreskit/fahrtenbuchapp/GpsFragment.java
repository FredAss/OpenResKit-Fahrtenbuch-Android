package buis.openreskit.fahrtenbuchapp;

import java.util.ArrayList;
import java.util.Collection;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import buis.openreskit.odata.GeoLocation;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

public abstract class GpsFragment extends Fragment implements OnDateSetListener, LocationListUser{

	protected EditText dateView;	    

    protected ArrayList<Location> gpsLocations;
    protected GPSTracker gpsTracker;
    protected ListView gpsLocationList;
    protected GoogleMap map;

    protected Context ctx;

	protected SharedPreferences sharedPreferences;
	protected Double distance = 0d;
    
	public GpsFragment(){
		initializeMap();
	}
	
    public GpsFragment(ArrayList<Location> gpsLocations, GPSTracker gpsTracker){    	    	
    	this.gpsLocations = gpsLocations;
    	this.gpsTracker = gpsTracker;
    	this.gpsTracker.addLocationListUser(this);    	
    }
    
    public GpsFragment(Collection<GeoLocation> geoLocations) {
    	this.gpsLocations = new ArrayList<Location>();
    	
    	for(GeoLocation geoLocation : geoLocations){
    		Location location = new Location("");
    		location.setLatitude(geoLocation.getLatitude());
    		location.setLongitude(geoLocation.getLongitude());
    		gpsLocations.add(location);
    	}    	    
	}    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {     	
    	return super.onCreateView(inflater, container, savedInstanceState);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onActivityCreated(savedInstanceState);
    	initializeMap();
    	initializeGeoLocations();    	
    	getCarbonFootprintIdFromSettings();
    }
    
    private void initializeMap(){
        MapFragment mapFrag = (MapFragment) getFragmentManager().findFragmentById(R.id.mapframe);

        if (mapFrag == null || mapFrag instanceof AirportMap) {
            mapFrag = new MapFrag();

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.mapframe, mapFrag).commit();
        }

        map = mapFrag.getMap();
        map.clear();
    }

	private void initializeGeoLocations() {

		gpsLocationList.setAdapter(new LocationAdapter(getActivity(), android.R.layout.simple_list_item_1 , gpsLocations));
		
		if(gpsLocations.size() > 1){
			map.clear();
			PolylineOptions rectLine = new PolylineOptions();
			rectLine.width(3).color(Color.RED);

			for(Location location : gpsLocations){
				rectLine.add(new LatLng(location.getLatitude(), location.getLongitude()));				
			}

			zoomToLatestLocation();
			
			map.addPolyline(rectLine);	        
		}
	}      
    
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
        //do some stuff for example write on log and update TextField on activity
        Log.w("DatePicker", "Date = " + year);
        dateView.setText(dayOfMonth + "." + monthOfYear + "." + year);
		
	}
	
	@Override
	public void OnNewLocationListEntry() {
		ArrayAdapter<Location> adapter = (ArrayAdapter<Location>)gpsLocationList.getAdapter();		
		adapter.notifyDataSetChanged();
		zoomToLatestLocation();
	} 	
	
	private void zoomToLatestLocation(){
		
		if(gpsLocations == null){
			return;
		}
		
		LatLng start = new LatLng(gpsLocations.get(gpsLocations.size()-1).getLatitude(), gpsLocations.get(gpsLocations.size()-1).getLongitude());		
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 15.0f)); 
	}	

    protected int getCarbonFootprintIdFromSettings() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);

        String cfIdString = sharedPreferences.getString("carbonfootprint", "none");

        if (cfIdString != "none") {
            return Integer.parseInt(cfIdString);
        }else{
        	return 0;
        }
    }	
	
}
