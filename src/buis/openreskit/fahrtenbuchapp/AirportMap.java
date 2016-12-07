package buis.openreskit.fahrtenbuchapp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import buis.openreskit.odata.Airport;
import buis.openreskit.odata.DatabaseHelper;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class AirportMap extends MapFragment implements OnMarkerClickListener {
    GoogleMap mMap;
    Context ctx;
    Marker airportMarker;
    ProgressDialog pdia;
    PopupWindow popUp;
    Button startButton;
    Button endButton;
    Boolean isStart;
    LatLng clickedCoords;
    FlightPositionFragment flightPositionFragment;
    private Dao<Airport, Integer> airportDao;
    DatabaseHelper helper;
    List<Airport> airportList;
    Boolean routeDrawn = false;
    private HashMap<Airport, Marker> airportsAtMarker = new HashMap<Airport, Marker>();
    private ArrayList<Polyline> polylinesOnMap = new ArrayList<Polyline>();
//    private ArrayList<Marker> markersOnMap = new ArrayList<Marker>();
    private float currentZoom;
    
    View.OnClickListener setListener = new View.OnClickListener() {
        public void onClick(View v) {
            popUp.dismiss();
            flightPositionFragment = (FlightPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);

            if (flightPositionFragment != null) {
                if (startButton.getId() == ((Button) v).getId()) {
                    isStart = true;
                    flightPositionFragment.setStartAirport(clickedCoords);
                } else if (endButton.getId() == ((Button) v).getId()) {
                    isStart = false;
                    flightPositionFragment.setEndAirport(clickedCoords);
                }
            } else {
                EntryFragment newEntryFragment = new EntryFragment();
                Bundle args = new Bundle();
                newEntryFragment.setArguments(args);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.placeholder, newEntryFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    };        
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
        ctx = getActivity();
        flightPositionFragment = (FlightPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapframe)).getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(true);        
        
        try {
            airportDao = helper.getAirportDao();
            airportList = new ArrayList<Airport>();
            airportList = airportDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
                     
        updateMarkersOnMap();
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
					
			@Override
			public void onCameraChange(CameraPosition position) {
				currentZoom = position.zoom;
				updateMarkersOnMap();													
			}
		});               
    }

    private void updateMarkersOnMap(){

		if (!airportList.isEmpty()) {
			LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
			
            for (Airport airport : airportList) {   
            	
            	if(currentZoom < 7 && airport.getSize() < 2){
            		continue;
            	}
            	
            	LatLng airportLocation = new LatLng(airport.getLatitude(), airport.getLongitude());
            	
            	if(bounds.contains(airportLocation)){
            		if(!airportsAtMarker.containsKey(airport)){
        				Marker marker = mMap.addMarker(new MarkerOptions().position(airportLocation).title(airport.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));		
        				airportsAtMarker.put(airport, marker);		
            		}	            		
            	}else{
            		if(airportsAtMarker.containsKey(airport)){
            			airportsAtMarker.get(airport).remove();
            			airportsAtMarker.remove(airport);
            		}
            	}	            		            		            	                
            }
            mMap.setOnMarkerClickListener(this);
		}
    }
    
    private class UpdateMarkers extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {

			
			return null;
		}    	
		
		@Override
		protected void onPostExecute(Void result) {
			Iterator<Airport> keyset = airportsAtMarker.keySet().iterator();
			
			while(keyset.hasNext()){
				Airport airport = keyset.next();
				Marker marker = airportsAtMarker.get(airport);
				if(currentZoom > 7 && airport.getSize() < 2){
					marker.setVisible(true);
				}else if(airport.getSize() == 2){
					marker.setVisible(true);
				}else{
					marker.setVisible(false);
				}
			}
		}
    }
    
    public void drawRoute(ArrayList<Airport> airports) {

		if(mMap == null){
	    	mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapframe)).getMap();
		}
	
        for(Polyline polyline : polylinesOnMap){
        	polyline.remove();
        }

        PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);
        
        if(airports.size() > 1){
            for(int i = 1; i < airports.size();i++){
            	rectLine.add(new LatLng(airports.get(i-1).getLatitude(), airports.get(i-1).getLongitude()), new LatLng(airports.get(i).getLatitude(), airports.get(i).getLongitude()));            	
            }                      
        }
        
        routeDrawn = true;
        polylinesOnMap.add(mMap.addPolyline(rectLine));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
    	marker.showInfoWindow();
    	
        LatLng clickedAirport = marker.getPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        
        displayFromToPopup(ctx, marker.getPosition());
        
        
        return true;
    }

    public void displayFromToPopup(Context context, LatLng point) {
        clickedCoords = point;        

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.popup, null, true);
        
        
        popUp = new PopupWindow(layout);
        popUp.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popUp.setBackgroundDrawable(new BitmapDrawable());
        popUp.setOutsideTouchable(true);
        popUp.setTouchInterceptor(new OnTouchListener() {
                //close if outside is pressed
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                        popUp.dismiss();

                        return true;
                    }

                    return false;
                }
            });

        startButton = (Button) layout.findViewById(R.id.setStart);
        startButton.setOnClickListener(setListener);
        endButton = (Button) layout.findViewById(R.id.setEnd);
        endButton.setOnClickListener(setListener);

        popUp.showAtLocation(getView(), Gravity.LEFT, 30, 0);
    }
}
