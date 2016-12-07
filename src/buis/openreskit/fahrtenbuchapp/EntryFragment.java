package buis.openreskit.fahrtenbuchapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;

import android.view.View.OnClickListener;

import android.view.ViewGroup;

import android.widget.ToggleButton;

import com.google.android.gms.drive.internal.GetMetadataRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class EntryFragment extends Fragment {
    private static GoogleMap mMap;
    private static PolylineOptions rectLine;
    public static LatLng start;
    public static LatLng end;
    private Activity ctx;
    private ToggleButton carButton;
    private ToggleButton planeButton;
    private ToggleButton trainButton;
    private ToggleButton busButton;
    private ToggleButton gpsButton;
    int lastFootprint = 0;
    public boolean airportMapShown = false;
    Double distance;
    CarPositionFragment newCarPositionFragment = new CarPositionFragment();
	FlightPositionFragment newFlightPositionFragment = new FlightPositionFragment();
    BusPositionFragment newBusPositionFragment = new BusPositionFragment();
    TrainPositionFragment newTrainPositionFragment = new TrainPositionFragment();
//	private MapFrag smapFrag;
	private MapFrag mMapFragment;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.entryfragment, container, false);

        carButton = (ToggleButton) view.findViewById(R.id.toggleCar);
        planeButton = (ToggleButton) view.findViewById(R.id.togglePlane);
        trainButton = (ToggleButton) view.findViewById(R.id.toggleTrain);
        busButton = (ToggleButton) view.findViewById(R.id.toggleBus);
        gpsButton = (ToggleButton) view.findViewById(R.id.toggleGps);

        carButton.setChecked(false);
        planeButton.setChecked(false);
        trainButton.setChecked(false);
        busButton.setChecked(false);
        gpsButton.setChecked(false);

        ctx = getActivity();

        //handling the map
//        mMapFragment = new MapFrag();
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.mapframe, mMapFragment);
//        fragmentTransaction.commit();        
        
//        FragmentManager fm = ctx.getFragmentManager();
//        smapFrag = (MapFrag) fm.findFragmentById(R.id.mapframe);
//        mMap = mMapFragment.getMap();
        
        mMap = ((MapFrag)MainActivity.getFragmentRepository().getFragment("MapFrag")).getMap();

        
        //http://stackoverflow.com/questions/2379527/android-how-to-get-a-radiogroup-with-togglebuttons
        carButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setCarChecked();
                    
                    Fragment newCarPositionFragment = new CarPositionFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.positionFragment, newCarPositionFragment).commit();
                    
                }
            });
        planeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setFlightChecked();
                    Fragment newFlightPositionFragment = new FlightPositionFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.positionFragment, newFlightPositionFragment).commit();
                }
            });
        trainButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setTrainChecked();
                    Fragment newTrainPositionFragment = new TrainPositionFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.positionFragment, newTrainPositionFragment).commit();
                }
            });
        busButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    setBusChecked();
                    Fragment newBusPositionFragment = new BusPositionFragment();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.positionFragment, newBusPositionFragment).commit();
                }
            });
        
        gpsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		setGpsChecked();
                Fragment newGpsFragment = new GpsTrackingFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.positionFragment, newGpsFragment).commit();                                                
            }
        });        
        

        return view;
    }

    public static void setRoute(List<LatLng> routeSections) {
        for (int i = 0; i < routeSections.size(); i++) {
            if (i == (routeSections.size() - 1)) {
                end = routeSections.get(i);
            }
        }

        for (LatLng routeSection : routeSections) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routeSection, 15.0f));

            rectLine.add(routeSection);
            mMap.addPolyline(rectLine);
        }
    }

    public static EntryFragment newInstance() {
        EntryFragment f = new EntryFragment();

        return f;
    }

    public void setCarChecked() {
        planeButton.setChecked(false);
        trainButton.setChecked(false);
        busButton.setChecked(false);
        carButton.setChecked(true);
        gpsButton.setChecked(false);
    }

    public void setBusChecked() {
        planeButton.setChecked(false);
        trainButton.setChecked(false);
        busButton.setChecked(true);
        carButton.setChecked(false);
        gpsButton.setChecked(false);
    }

    public void setTrainChecked() {
        planeButton.setChecked(false);
        trainButton.setChecked(true);
        busButton.setChecked(false);
        carButton.setChecked(false);
        gpsButton.setChecked(false);
    }

    public void setFlightChecked() {
        planeButton.setChecked(true);
        trainButton.setChecked(false);
        busButton.setChecked(false);
        carButton.setChecked(false);
        gpsButton.setChecked(false);
    }
    
    public void setGpsChecked() {
        planeButton.setChecked(false);
        trainButton.setChecked(false);
        busButton.setChecked(false);
        carButton.setChecked(false);
        gpsButton.setChecked(true);
    }    
}
