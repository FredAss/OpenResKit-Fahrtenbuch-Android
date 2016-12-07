package buis.openreskit.fahrtenbuchapp;

import android.app.ActionBar;

import android.app.ActionBar.Tab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.os.Bundle;
import android.os.Debug;

import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import buis.openreskit.odata.Airport;
import buis.openreskit.odata.AirportPosition;
import buis.openreskit.odata.Car;
import buis.openreskit.odata.CarData;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Employee;
import buis.openreskit.odata.EnergyConsumption;
import buis.openreskit.odata.Flight;
import buis.openreskit.odata.Footprint;
import buis.openreskit.odata.FootprintPosition;
import buis.openreskit.odata.GeoLocation;
import buis.openreskit.odata.PublicTransport;
import buis.openreskit.odata.Synchronization;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.io.IOException;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    MapFrag mMapFragment;
    GoogleMap mMap;
    EntryFragment entryFragment;
    Button setStart;
    PopupWindow popUp;
    Context ctx;
    Button startButton;
    Button endButton;
    LatLng clickedCoords = null;
    Location mLocation;
    DatabaseHelper helper;
    Activity currentActivity;
    private Dao<Footprint, Integer> footprintDao = null;
    private Dao<FootprintPosition, Integer> footprintPositionDao = null;
    private Dao<AirportPosition, Integer> airportPositionDao = null;
    private Dao<Flight, Integer> flightDao = null;
    private Dao<Car, Integer> carDao = null;
    private Dao<EnergyConsumption, Integer> energyConsumptionDao = null;
    private Dao<PublicTransport, Integer> publicTransportDao = null;
    private Dao<Airport, Integer> airportDao = null;
    private Dao<Employee, Integer> employeeDao = null;
    private SharedPreferences mPrefs;
    private ActionBar mActionbar;
    private int mCfId;
    private Tab entryTab;
    private OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == "carbonfootprint") {
                getCarbonFootprintIdFromSettings();
                checkAndAddEntryTab();
            }
        }
    };

    private Dao<CarData, Integer> carDataDao;
	private Dao<GeoLocation, Integer> geoLocationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int checkGooglePlayServices = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
            // google play services is missing!!!!
            /*
             * Returns status code indicating whether there was an error.
             * Can be one of following in ConnectionResult: SUCCESS,
             * SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
             * SERVICE_DISABLED, SERVICE_INVALID.
             */
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
        }

        //        Debug.startMethodTracing("Fahrtenbuch");
        ctx = this;
//        		ctx.deleteDatabase("footprint.db");
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);

        mActionbar = getActionBar();
        mActionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //set the Tab listener. Now we can listen for clicks.
        Tab listTab = mActionbar.newTab().setText("Übersicht");
        listTab.setTabListener(new MyTabListener<ListOverview>(this, "overviewFragment", ListOverview.class));
        listTab.setTag("overview");
        mActionbar.addTab(listTab);

        getCarbonFootprintIdFromSettings();
        checkAndAddEntryTab();
        initializeFragments();
//        //handling the map
//        mMapFragment = new MapFrag();
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.mapframe, mMapFragment);
//        fragmentTransaction.commit();

        //        Debug.stopMethodTracing();
    }

    private void initializeFragments() {
		// TODO Auto-generated method stub
		getFragmentRepository().addFragment("MapFrag", new MapFrag());
		getFragmentRepository().addFragment("AirportMap", new AirportMap());
		getFragmentRepository().activateFragment(R.id.mapframe, "MapFrag", getFragmentManager());
	}

	private void addEntryTab() {
        entryTab = mActionbar.newTab().setText("Neuer Eintrag");
        entryTab.setTabListener(new MyTabListener<EntryFragment>(this, "entryFragment", EntryFragment.class));
        entryTab.setTag("entry");
        mActionbar.addTab(entryTab);
    }

    private void getCarbonFootprintIdFromSettings() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        String cfIdString = mPrefs.getString("carbonfootprint", "none");

        if (cfIdString != "none") {
            mCfId = Integer.parseInt(cfIdString);
        }
    }

    public void checkAndAddEntryTab() {
        if (mCfId != 0) {
            if (mActionbar.getTabCount() == 1) {
                addEntryTab();
            }
        } else {
            if (mActionbar.getTabCount() == 2) {
                mActionbar.removeTabAt(1);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.showPrefsDialog:

            Intent preferencesIntent = new Intent();
            preferencesIntent.setClass(MainActivity.this, SetPreferenceActivity.class);
            startActivityForResult(preferencesIntent, 0);

            return true;

        case R.id.startUpload:

            Synchronization uploadSync = new Synchronization();
            uploadSync.writeData(ctx, helper);

            return true;

        case R.id.startDownload:

            Synchronization downloadSync = new Synchronization();
            downloadSync.getData(ctx, this, false);

            return true;

        case R.id.setFavorite:

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Standortfavoriten");
            alert.setMessage("Bitte geben Sie den Ortsnamen ein, den Sie speichern möchten.");

            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapframe);

            if (mapFragment != null) {
                mMap = mapFragment.getMap();
            } else {
                Toast.makeText(ctx, "Fehler bei Kartenabfrage", Toast.LENGTH_SHORT);

                return false;
            }

            final AutoCompleteTextView input = new AutoCompleteTextView(this);
            input.setAdapter(new AutoCompleteAdapter(ctx));
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Geocoder geoCoder = new Geocoder(ctx, Locale.getDefault());
                        LatLng savedLatLng = null;

                        try {
                            List<Address> addresses = geoCoder.getFromLocationName(input.getText().toString(), 1);

                            for (Address address : addresses) {
                                savedLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                                if (mMap != null) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(savedLatLng, 15.0f));
                                }

                                Toast.makeText(ctx, "Standortfavorit gespeichert!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        SharedPreferences.Editor prefEditor = mPrefs.edit();
                        prefEditor.putString("favorite_latitude", (savedLatLng.latitude) + "");
                        prefEditor.putString("favorite_longitude", (savedLatLng.longitude) + "");
                        prefEditor.commit();
                    }
                });


            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });


            alert.show();

            return true;

        case R.id.deleteData:

            try {
                deleteLocalData();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;

        case R.id.catalogUpdate:
                        
            new Synchronization().getData(ctx, this, true);

            return true;        	
        	
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Methode zum Löschen aller auf dem Gerät vorhandener Einträge zu Footprints
     * und der Positionen in der SQLite Datenbank.
     * @throws SQLException
     */
    private void deleteLocalData() throws SQLException {
        try {
            footprintDao = helper.getFootprintDao();
            footprintPositionDao = helper.getFootprintPositionDao();
            airportPositionDao = helper.getAirportPositionDao();
            flightDao = helper.getFlightsDao();
            carDao = helper.getCarDao();
            energyConsumptionDao = helper.getEnergyConsumptionDao();
            publicTransportDao = helper.getPublicTransportDao();
            airportDao = helper.getAirportDao();
            employeeDao = helper.getEmployeeDao();
            carDataDao = helper.getCarDataDao();
            geoLocationDao = helper.getGeoLocationDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        footprintDao.executeRaw("Delete FROM footprint");
        footprintPositionDao.executeRaw("Delete FROM footprintPosition");
        airportPositionDao.executeRaw("Delete FROM airportposition");
        flightDao.executeRaw("Delete FROM flight");
        carDao.executeRaw("Delete From car");
        energyConsumptionDao.executeRaw("Delete From energyConsumption");
        publicTransportDao.executeRaw("Delete From publicTransport");
        airportDao.executeRaw("Delete From airport");
        employeeDao.executeRaw("Delete From employee");
        carDataDao.executeRaw("Delete From carData");
        geoLocationDao.executeRaw("Delete From geoLocation");
        Toast.makeText(ctx, "Datenbank geleert", Toast.LENGTH_SHORT).show();

        mCfId = 0;

        SharedPreferences.Editor prefEditor = mPrefs.edit();
        prefEditor.remove("carbonfootprint");
        prefEditor.remove("employee");
        prefEditor.commit();

        ListOverview listOverview = (ListOverview) getFragmentManager().findFragmentByTag("overviewFragment");

        if (listOverview != null) {
            listOverview.initializeList();
        } else {
            ListOverview listOverview1 = new ListOverview();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.placeholder, listOverview1, "overviewFragment");
            fragmentTransaction.commit();
        }
    }
    
    private static FragmentRepository fragmentRepository;
    
    public static FragmentRepository getFragmentRepository(){
    	if(fragmentRepository == null){
    		fragmentRepository = new FragmentRepository();
    	}
    	
    	return fragmentRepository;
    }
    
    public static class FragmentRepository{
    	private HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();
    	
    	public void addFragment(String key, Fragment fragment){
    		if(!fragments.containsKey(key)){
    			fragments.put(key, fragment);
    		}
    	}
    	
    	public Fragment getFragment(String key){    		
    		if(fragments.containsKey(key)){
    			return fragments.get(key);	
    		}else{
    			return null;
    		}    		    		
    	}
    	
    	public void activateFragment(int containerViewId, String key, FragmentManager fragmentManager){    		    		    		
    		if(fragments.containsKey(key) && (fragmentManager.findFragmentById(containerViewId) == null || fragmentManager.findFragmentById(containerViewId).getClass() != fragments.get(key).getClass())){    		
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(containerViewId, fragments.get(key));
				fragmentTransaction.commit();  
			}  		    		
    	}
    	
    }
    
}
