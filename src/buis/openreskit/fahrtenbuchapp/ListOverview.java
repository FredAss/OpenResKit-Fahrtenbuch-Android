package buis.openreskit.fahrtenbuchapp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import buis.openreskit.fahrtenbuchapp.MapFrag.GetAddressTask;
import buis.openreskit.odata.Car;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Flight;
import buis.openreskit.odata.Footprint;
import buis.openreskit.odata.FootprintPosition;
import buis.openreskit.odata.PublicTransport;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class ListOverview extends ListFragment {
    private DatabaseHelper helper;
    private Dao<Footprint, Integer> footprintDao = null;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private Activity ctx;
    private FootprintPosition clickedFootprintPosition = null;
    private int mCfId;
    private SharedPreferences mPrefs;
    private List<FootprintPosition> listOfPositions = new ArrayList<FootprintPosition>();
    private ListView positionsListView;
    private PositionsAdapter positionsAdpater;
    private OnItemClickListener listClickListener = new OnItemClickListener() {
//    private EntryFragment entryFrag;

		@Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            //			ArrayList<Integer> footprintPositionList = new ArrayList<Integer>();  				  	  	 
            try {
                footprintDao = helper.getFootprintDao();                
                publicTransportDao = helper.getPublicTransportDao();
            } catch (SQLException e) {
                e.printStackTrace();
            }
//            entryFrag = (EntryFragment) getFragmentManager().findFragmentByTag("entryFragment");
            clickedFootprintPosition = (FootprintPosition) positionsAdpater.getItem(position);

            if (clickedFootprintPosition.getName().equalsIgnoreCase("Fahrzeug") || clickedFootprintPosition.getName().equalsIgnoreCase("Herstellerfahrzeug")) {
                ActionBar actionBar = getActivity().getActionBar();
                actionBar.setSelectedNavigationItem(1);

                Fragment newCarPositionFragment = new CarPositionFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.positionFragment, newCarPositionFragment).commit();

                Bundle args = new Bundle();
                args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                newCarPositionFragment.setArguments(args);
            }if (clickedFootprintPosition.getName().equalsIgnoreCase("Fahrzeug (GPS)")) {
                ActionBar actionBar = getActivity().getActionBar();
                actionBar.setSelectedNavigationItem(1);

                Fragment newGpsCarPositionFragment = new GpsCarPositionFragment(clickedFootprintPosition.getGeoLocations());
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.positionFragment, newGpsCarPositionFragment).commit();

                Bundle args = new Bundle();
                args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                newGpsCarPositionFragment.setArguments(args);
            } else if (clickedFootprintPosition.getName().equalsIgnoreCase("Flug") || clickedFootprintPosition.getName().equalsIgnoreCase("Zielbasierter Flug")) {
                ActionBar actionBar = getActivity().getActionBar();
                actionBar.setSelectedNavigationItem(1);

                Fragment newFlightPositionFragment = new FlightPositionFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.positionFragment, newFlightPositionFragment).commit();

                Bundle args = new Bundle();
                args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                newFlightPositionFragment.setArguments(args);
            } else if (clickedFootprintPosition.getName().equalsIgnoreCase("Öffentlicher Verkehr")) {
                for (PublicTransport publicTransport : publicTransportDao) {
                    if (publicTransport.getFootprintPosition().getInternalId() == clickedFootprintPosition.getInternalId()) {
                        if (publicTransport.getTransportType() == 4) {
                            ActionBar actionBar = getActivity().getActionBar();
                            actionBar.setSelectedNavigationItem(1);

                            Fragment newBusPositionFragment = new BusPositionFragment();
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.positionFragment, newBusPositionFragment).commit();

                            Bundle args = new Bundle();
                            args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                            newBusPositionFragment.setArguments(args);
                        } else if ((publicTransport.getTransportType() == 0) || (publicTransport.getTransportType() == 1) || (publicTransport.getTransportType() == 2) || (publicTransport.getTransportType() == 3)) {
                            ActionBar actionBar = getActivity().getActionBar();
                            actionBar.setSelectedNavigationItem(1);

                            Fragment newTrainPositionFragment = new TrainPositionFragment();
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                            fragmentTransaction.replace(R.id.positionFragment, newTrainPositionFragment).commit();

                            Bundle args = new Bundle();
                            args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                            newTrainPositionFragment.setArguments(args);
                        }
                    }
                }
            }else if(clickedFootprintPosition.getName().equalsIgnoreCase("Öffentlicher Verkehr (GPS)")){
                for (PublicTransport publicTransport : publicTransportDao) {
                    if (publicTransport.getFootprintPosition().getInternalId() == clickedFootprintPosition.getInternalId()) {
                        if ((publicTransport.getTransportType() == 0) || (publicTransport.getTransportType() == 1) || (publicTransport.getTransportType() == 2) || (publicTransport.getTransportType() == 3)) {
                            ActionBar actionBar = getActivity().getActionBar();
                            actionBar.setSelectedNavigationItem(1);

                            Fragment newTrainPositionFragment = new GpsTrainPositionFragment(clickedFootprintPosition.getGeoLocations());
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                            fragmentTransaction.replace(R.id.positionFragment, newTrainPositionFragment).commit();

                            Bundle args = new Bundle();
                            args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                            newTrainPositionFragment.setArguments(args);
                        }else if (publicTransport.getTransportType() == 4) {
                            ActionBar actionBar = getActivity().getActionBar();
                            actionBar.setSelectedNavigationItem(1);

                            Fragment newGpsBusPositionFragment = new GpsBusPositionFragment(clickedFootprintPosition.getGeoLocations());
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                            fragmentTransaction.replace(R.id.positionFragment, newGpsBusPositionFragment).commit();

                            Bundle args = new Bundle();
                            args.putInt("clickedPosition", clickedFootprintPosition.getInternalId());
                            newGpsBusPositionFragment.setArguments(args);
                        }
                    }
                }            	
            }else {
//                Toast.makeText(ctx, "Eintrag kann nicht bearbeitet werden.", Toast.LENGTH_LONG).show();
            }
        }
    };

    private OnSharedPreferenceChangeListener prefListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key == "carbonfootprint") {
                initializeList();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ctx = getActivity();

        View view = inflater.inflate(R.layout.listfragment, container, false);
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        try {
            footprintDao = helper.getFootprintDao();
            initializeList();
        } catch (SQLException e2) {
            e2.printStackTrace();
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        positionsListView = this.getListView();

        if (positionsListView != null) {
            /**
             * Listener durch den ein Formular mit bestehenden Daten geöffnet wird,
             * wenn eine Position zum Editieren angeklickt wird.
             */
            positionsListView.setOnItemClickListener(listClickListener);
            registerForContextMenu(positionsListView);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
      
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(listOfPositions.get(info.position).description);                
        menu.add(Menu.NONE, 0, 0, "Löschen");              
    }    
    
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
    	builder
    	.setTitle("Carbon Footprintposition löschen")
    	.setMessage("Sind Sie sicher?")
    	.setIcon(android.R.drawable.ic_dialog_alert)
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {			      	
    	    	try {
    	    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	    		FootprintPosition footprintPosition = listOfPositions.get(info.position);
    	    		deleteFootprintPosition(footprintPosition);						
//    				initializeList();    				
    			} catch (SQLException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();    				
    			}       	    	
    	    }
    	})
    	.setNegativeButton("No", null)						//Do nothing on no
    	.show();    	
    	
    	return true;
 	
    }
            
    private void deleteFootprintPosition(FootprintPosition footprintPosition) throws SQLException{
    	new DeleteFootprintPositionTask().execute(footprintPosition);        	
    }
    
    public void addFootprintPositionToList(FootprintPosition footprintPosition){
    	if(!listOfPositions.contains(footprintPosition)){
	    	listOfPositions.add(footprintPosition);
	    	positionsAdpater.notifyDataSetChanged();
    	}
    }
    
    public void initializeList() {
        listOfPositions.clear();
        getCarbonFootprintIdFromSettings();

        if (mCfId != 0) {
            if (footprintDao != null) {
                try {
                    Footprint footprint = footprintDao.queryForId(mCfId);

                    if (footprint != null) {
                        for (FootprintPosition fp : footprint.getFootprintPositions()) {
                            listOfPositions.add(fp);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        positionsAdpater = new PositionsAdapter(ctx, listOfPositions);
        setListAdapter(positionsAdpater);
    }

    /**
     * Nach Erstellung eines neuen Footprints wird die Liste der Positionen erneuert
     */
    @Override
    public void onResume() {
        super.onResume();
        initializeList();
    }
        
    private void getCarbonFootprintIdFromSettings() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mPrefs.registerOnSharedPreferenceChangeListener(prefListener);

        String cfIdString = mPrefs.getString("carbonfootprint", "none");

        if (cfIdString != "none") {
            mCfId = Integer.parseInt(cfIdString);
        }
    }
    
    private class DeleteFootprintPositionTask extends AsyncTask<FootprintPosition, Void, Void>{

    	private FootprintPosition footprintPosition = null;
    	
		@Override
		protected Void doInBackground(FootprintPosition... params) {
			// TODO Auto-generated method stub
			
			footprintPosition = params[0];
			
			if(footprintPosition == null){
				return null;
			}			
			
			try {
		    	if (footprintPosition.getName().equals("Flug") || footprintPosition.getName().equals("Zielbasierter Flug") || footprintPosition.getName().equals("Linienflug")) {
		    		
						for (Flight flight : helper.getFlightsDao()) {
						    if (flight.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
						    	helper.getFlightsDao().delete(flight);
						    	break;
						    }
						}
					
		    	}
		    	else if (footprintPosition.getName().equals("Fahrzeug") || footprintPosition.getName().equals("Fahrzeug (GPS)") || footprintPosition.getName().equals("Herstellerfahrzeug") || footprintPosition.getName().equals("Fahrtenbuch-Autofahrt")) {
		    		for (Car car : helper.getCarDao()) {
		                if (car.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
		                	helper.getCarDao().delete(car);
		                	break;
		                }
		            }
		    	}
		    	else if (footprintPosition.getName().equalsIgnoreCase("Öffentlicher Verkehr") || footprintPosition.getName().equalsIgnoreCase("Öffentlicher Verkehr (GPS)")) {
		    		for (PublicTransport publicTransport : helper.getPublicTransportDao()) {
		                if (publicTransport.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
		                	helper.getPublicTransportDao().delete(publicTransport);
		                	break;
		                }                
		            }
		    	}    	
		    	helper.getFootprintPositionDao().delete(footprintPosition);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
	    	listOfPositions.remove(footprintPosition);
	    	positionsAdpater.notifyDataSetChanged();
		}
		
	}
}
