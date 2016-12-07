package buis.openreskit.fahrtenbuchapp;

import android.app.Fragment;
import android.app.FragmentTransaction;

import android.content.Context;

import android.graphics.drawable.BitmapDrawable;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.View.OnTouchListener;

import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;

import java.util.List;
import java.util.Locale;


public class MapFrag extends MapFragment implements OnMapClickListener {
    private CarPositionFragment newCarPositionFragment;
    private BusPositionFragment newBusPositionFragment;
    private FlightPositionFragment newFlightPositionFragment;
    private TrainPositionFragment newTrainPositionFragment;
    private GoogleMap mMap;
    private Context ctx;
    private PopupWindow popUp;
    private Button startButton;
    private Button endButton;
    private ProgressBar mActivityIndicator;
    private Location mLocation = new Location(LocationManager.NETWORK_PROVIDER);
    private LatLng clickedCoords;
    private LatLng startCoords;
    private LatLng endCoords;
    private Boolean isStart;
    View.OnClickListener setListener = new View.OnClickListener() {
        public void onClick(View v) {
            popUp.dismiss();

            Fragment fragment = getFragmentManager().findFragmentById(R.id.positionFragment);

            if(fragment == null){
            	return;
            }
            
            if (fragment.getClass() ==  CarPositionFragment.class) {
                newCarPositionFragment = (CarPositionFragment) fragment;
            }
            

            if (fragment.getClass() == BusPositionFragment.class) {
                newBusPositionFragment = (BusPositionFragment) fragment;
            }

            if (fragment.getClass() ==  FlightPositionFragment.class) {
                newFlightPositionFragment = (FlightPositionFragment) fragment;
            }

            if (fragment.getClass() == TrainPositionFragment.class) {
                newTrainPositionFragment = (TrainPositionFragment) fragment;
            }

            if (newCarPositionFragment != null) {
                if (startButton.getId() == ((Button) v).getId()) {
                    isStart = true;
                    startCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                } else if (endButton.getId() == ((Button) v).getId()) {
                    isStart = false;
                    endCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                }
            }

            if (newBusPositionFragment != null) {
                if (startButton.getId() == ((Button) v).getId()) {
                    isStart = true;
                    startCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                } else if (endButton.getId() == ((Button) v).getId()) {
                    isStart = false;
                    endCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                }
            }

            if (newTrainPositionFragment != null) {
                if (startButton.getId() == ((Button) v).getId()) {
                    isStart = true;
                    startCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                } else if (endButton.getId() == ((Button) v).getId()) {
                    isStart = false;
                    endCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                }
            }

            if (newFlightPositionFragment != null) {
                if (startButton.getId() == ((Button) v).getId()) {
                    isStart = true;
                    startCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                } else if (endButton.getId() == ((Button) v).getId()) {
                    isStart = false;
                    endCoords = clickedCoords;
                    (new GetAddressTask(ctx)).execute(mLocation);
                }
            } else if ((newCarPositionFragment == null) && (newBusPositionFragment == null)) {
//                EntryFragment newEntryFragment = new EntryFragment();
//                Bundle args = new Bundle();
//                newEntryFragment.setArguments(args);
//
//                FragmentTransaction transaction = getFragmentManager().beginTransaction();
//                transaction.replace(R.id.placeholder, newEntryFragment);
//
//                transaction.commit();
            }

            mActivityIndicator = new ProgressBar(ctx);
            mActivityIndicator.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ctx = getActivity().getBaseContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMap = this.getMap();

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            mMap.setOnMapClickListener(this);
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        Fragment entry = (Fragment) getFragmentManager().findFragmentById(R.id.placeholder);
        String fragmentClass = entry.getClass().toString();

        if (fragmentClass.equalsIgnoreCase("class buis.openreskit.fahrtenbuchapp.EntryFragment")) {
            displayFromToPopup(ctx, point);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));
    }

    public void displayFromToPopup(Context context, LatLng point) {
        clickedCoords = point;
        mLocation.setLatitude(point.latitude);
        mLocation.setLongitude(point.longitude);
        popUp = new PopupWindow(context);

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
//        popUp.showAtLocation(getView(), Gravity.CENTER, mMap.getProjection().toScreenLocation(point).x - this.getView().getWidth(), mMap.getProjection().toScreenLocation(point).y);
    }

    public void startGetAddressTask(Context context, Location location, Boolean startEnd, Fragment fragment) {
        if (fragment.getClass().toString().equals("class buis.openreskit.fahrtenbuchapp.NewCarPositionFragment")) {
            newCarPositionFragment = (CarPositionFragment) fragment;
        }

        if (fragment.getClass().toString().equals("class buis.openreskit.fahrtenbuchapp.NewBusPositionFragment")) {
            newBusPositionFragment = (BusPositionFragment) fragment;
        }

        if (fragment.getClass().toString().equals("class buis.openreskit.fahrtenbuchapp.NewTrainPositionFragment")) {
            newTrainPositionFragment = (TrainPositionFragment) fragment;
        }

        if (fragment.getClass().toString().equals("class buis.openreskit.fahrtenbuchapp.NewFlightPositionFragment")) {
            newFlightPositionFragment = (FlightPositionFragment) fragment;
        }

        if (startEnd == true) {
            startCoords = new LatLng(location.getLatitude(), location.getLongitude());
            isStart = true;
        } else {
            endCoords = new LatLng(location.getLatitude(), location.getLongitude());
            isStart = false;
        }

        (new GetAddressTask(context)).execute(location);
    }

    //http://developer.android.com/training/location/display-address.html
    public class GetAddressTask extends AsyncTask<Location, Void, String> {
        Context mContext;

        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

            // Get the current location from the input parameter list
            Location loc = params[0];

            // Create a list to contain the result address
            List<Address> addresses = null;

            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
                e1.printStackTrace();

                return "";
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " + Double.toString(loc.getLatitude()) + " , " + Double.toString(loc.getLongitude()) + " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();

                return "";
            }

            // If the reverse geocode returned an address
            if ((addresses != null) && (addresses.size() > 0)) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);

                return addressText;
            } else {
                return "No address found";
            }
        }

        @Override
        protected void onPostExecute(String address) {
            if (isStart) {
                if (newCarPositionFragment != null) {
                    newCarPositionFragment.setAddressText(address + "", true);
                    newCarPositionFragment.setStartCoords(startCoords);
                }

                if (newBusPositionFragment != null) {
                    newBusPositionFragment.setAddressText(address + "", true);
                    newBusPositionFragment.setStartCoords(startCoords);
                }

                if (newTrainPositionFragment != null) {
                    newTrainPositionFragment.setAddressText(address + "", true);
                    newTrainPositionFragment.setStartCoords(startCoords);
                }
            } else {
                if (newCarPositionFragment != null) {
                    newCarPositionFragment.setAddressText(address + "", false);
                    newCarPositionFragment.setEndCoords(endCoords);
                }

                if (newBusPositionFragment != null) {
                    newBusPositionFragment.setAddressText(address + "", false);
                    newBusPositionFragment.setEndCoords(endCoords);
                }

                if (newTrainPositionFragment != null) {
                    newTrainPositionFragment.setAddressText(address + "", false);
                    newTrainPositionFragment.setEndCoords(endCoords);
                }
            }
        }
    }
}
