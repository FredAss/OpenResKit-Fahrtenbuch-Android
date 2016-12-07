package buis.openreskit.fahrtenbuchapp;

import java.util.ArrayList;
import java.util.Map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

public class GpsTrackingFragment extends Fragment   {
	private Context ctx;
	private ToggleButton gpsExecute;
	private GpsTrackingFragment me;
	private boolean isExecuting = false;	
	private ArrayList<Location> locations = new ArrayList<Location>();
	private GoogleMap map;
	private GPSTracker gps;
	private ToggleButton carButton;
	private ToggleButton trainButton;
	private ToggleButton busButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub    	
    	super.onCreate(savedInstanceState); 
    	me = this;
    	ctx = getActivity();
    	gps = new GPSTracker(ctx, me);    	    
    	
        MapFragment mapFragment = (MapFragment) MainActivity.getFragmentRepository().getFragment("MapFrag");
        MainActivity.getFragmentRepository().activateFragment(R.id.mapframe, "MapFrag", getFragmentManager());
        
        if (mapFragment != null) {
            map = mapFragment.getMap();
        } else {
            Toast.makeText(ctx, "Fehler bei Kartenabfrage", Toast.LENGTH_SHORT);            
        }            
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.gpstrackingfragment, container, false);		

        carButton = (ToggleButton) view.findViewById(R.id.toggleCar);
        trainButton = (ToggleButton) view.findViewById(R.id.toggleTrain);
        busButton = (ToggleButton) view.findViewById(R.id.toggleBus);				
		gpsExecute = (ToggleButton) view.findViewById(R.id.gpsExecute);
		
		InitializeButtonListener();
				
		return view;
	}
	
	private void InitializeButtonListener() {
		
		gpsExecute.setOnClickListener(new GpsExecuteButtonListener());		
		
        //http://stackoverflow.com/questions/2379527/android-how-to-get-a-radiogroup-with-togglebuttons
        carButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {                    
                    trainButton.setChecked(false);
                    busButton.setChecked(false);
                    
                    Fragment newGpsCarPositionFragment = new GpsCarPositionFragment(locations, gps);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.gpsPositionFragment, newGpsCarPositionFragment).commit();
                }
            });

        trainButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    carButton.setChecked(false);                    
                    busButton.setChecked(false);                    
                    
                    Fragment newGpsTrainFragment = new GpsTrainPositionFragment(locations, gps);                    
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.gpsPositionFragment, newGpsTrainFragment).commit();
                }
            });
        busButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    carButton.setChecked(false);
                    trainButton.setChecked(false);
                    
                    Fragment newGpsBusPositionFragment = new GpsBusPositionFragment(locations, gps);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.gpsPositionFragment, newGpsBusPositionFragment).commit();
                }
            });
		
	}
	
	public void addLocation(Location location){
		locations.add(location);			
		updateMap();
		if(getActivity() != null){
			Toast.makeText(getActivity().getApplicationContext(), "Anzahl Standorte: " + locations.size(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void updateMap() {
		// TODO Auto-generated method stub
		if(locations.size() > 1){
			map.clear();
			PolylineOptions rectLine = new PolylineOptions();
			rectLine.width(3).color(Color.RED);
			for(Location location : locations){
				rectLine.add(new LatLng(location.getLatitude(), location.getLongitude()));
			}
					
	        map.addPolyline(rectLine);	        
		}
	}

	public void ResetGpsTracking(){
		locations.clear();
		gpsExecute.setChecked(false);		
		map.clear();
		isExecuting = false;
	}
	
	private class GpsExecuteButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(isExecuting){				
				gps.stopUsingGPS();
				isExecuting = false;
			}else{
				isExecuting = true;
				gps.getLocation();
				// check if GPS enabled		
		        if(!gps.canGetLocation()){
		        	gps.showSettingsAlert();		        			        			        
		        }
			}
		}
	}

}
