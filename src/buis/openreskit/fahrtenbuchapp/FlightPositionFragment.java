package buis.openreskit.fahrtenbuchapp;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDateTime;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import buis.openreskit.odata.Airport;
import buis.openreskit.odata.AirportPosition;
import buis.openreskit.odata.Calculation;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Employee;
import buis.openreskit.odata.Flight;
import buis.openreskit.odata.Footprint;
import buis.openreskit.odata.FootprintPosition;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class FlightPositionFragment extends Fragment implements OnDateSetListener {
    private EditText descriptionView;
    private EditText dateView;
    private SharedPreferences mPrefs;
    private AirportMap airportMap;
    private EntryFragment entryFragment;

    private Location startLoc = new Location("Test");
    private Location endLoc = new Location("Test");    
    
    private LatLng startLatLng;
    private LatLng endLatLng;
    private ArrayList<Airport> flightAssignedAirports = new ArrayList<Airport>();
    private Airport[] airports;
    private boolean resumeHasRun = false;
    private int roundedEmissions;
    private DatabaseHelper helper;
    private Flight flight;
    private FootprintPosition footprintPosition = new FootprintPosition();
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<Footprint, Integer> footprintDao;
    private Dao<Flight, Integer> flightDao;

    private Dao<Employee, Integer> employeeDao;
    private Double distance = null;
    private Spinner startAirportsSpinner;
    private Spinner endAirportsSpinner;
    private Spinner flightTypeSpinner;
    private Context ctx;
    private Footprint footprint;
    private int clickedPosition;
    private FootprintPosition editedPosition;
    private Flight editedFlight;
    private FlightPositionFragment me;
    private Integer mCfId;
	private int startSpinnerAccessCount = 0;
	private int endSpinnerAccessCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.flightfragment, container, false);
        
        me = this;
        ctx = getActivity();
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
        getCarbonFootprintIdFromSettings();

        entryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.placeholder);

        startAirportsSpinner = (Spinner) view.findViewById(R.id.airportstart);
        endAirportsSpinner = (Spinner) view.findViewById(R.id.airportend);

        flightTypeSpinner = (Spinner) view.findViewById(R.id.flighttype);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.flighttypes_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flightTypeSpinner.setAdapter(spinnerAdapter);

        ImageView datePickerButton = (ImageView) view.findViewById(R.id.datepicker);

        dateView = (EditText) view.findViewById(R.id.date);

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        String formattedDate = dateFormat.format(date);
        dateView.setText(formattedDate);

        descriptionView = (EditText) view.findViewById(R.id.description);
        
        airportMap = (AirportMap) MainActivity.getFragmentRepository().getFragment("AirportMap");    	
    	MainActivity.getFragmentRepository().activateFragment(R.id.mapframe, "AirportMap", getFragmentManager());        
        
        
//        airportMap = new AirportMap();
//
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.mapframe, airportMap).commit();

        Button acceptButton = (Button) view.findViewById(R.id.accept);
        final EditText descriptionView = (EditText) view.findViewById(R.id.description);

        //Neue Position erstellen
        acceptButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ArrayList<FootprintPosition> footprintPositionList = new ArrayList<FootprintPosition>();
                    Boolean distanceOk = false;
                    Boolean consumptionOk = false;
                    String description = descriptionView.getText().toString();

                    /**
                     * Eine neue Position wird erstellt.
                     */
                    footprintPosition.setFootprint(footprint);

                    String pattern = "dd.MM.yyyy";
                    String dateString = dateView.getText().toString();

                    Date date = null;

                    try {
                        date = new SimpleDateFormat(pattern, Locale.GERMAN).parse(dateString);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    LocalDateTime dateTimeFormat = new LocalDateTime(date);
                    footprintPosition.setDate(dateTimeFormat.toString());
                    footprintPosition.setStartAddress(startAirportsSpinner.getSelectedItem().toString());
                    footprintPosition.setEndAddress(endAirportsSpinner.getSelectedItem().toString());
                    footprintPosition.setName("Zielbasierter Flug");
                    footprintPosition.setIconId("CfFlight.png");
                    footprintPosition.setPositionType("OpenResKit.DomainModel.AirportBasedFlight");
                    footprintPosition.setCarbonFootprintCategoryId("Flüge");
                    flight.setFootprintPosition(footprintPosition);

                    if ((flight.getAirportPositions() != null) && (flight.getAirportPositions().size() > 1)) {
                        int index = 1;

                        for (AirportPosition airportPosition : flight.getAirportPositions()) {
                            if (index == 1) {
                                airportPosition.setAirportId(flightAssignedAirports.get(0).getId());
                            } else if (index == flight.getAirportPositions().size()) {
                                airportPosition.setAirportId(flightAssignedAirports.get(flightAssignedAirports.size() - 1).getId());
                            }

                            index++;
                        }
                    } else {
                        ArrayList<AirportPosition> positions = new ArrayList<AirportPosition>();
                        AirportPosition airportPosition = new AirportPosition();

                        airportPosition.setAirportId(flightAssignedAirports.get(0).getId());
                        airportPosition.setFlight(flight);

                        try {
                            airportPosition.setInternalId((int) helper.getAirportPositionDao().countOf() + 1);
                            helper.getAirportPositionDao().create(airportPosition);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        positions.add(airportPosition);

                        airportPosition = new AirportPosition();

                        airportPosition.setAirportId(flightAssignedAirports.get(flightAssignedAirports.size() - 1).getId());
                        airportPosition.setFlight(flight);

                        try {
                            airportPosition.setInternalId((int) helper.getAirportPositionDao().countOf() + 1);
                            helper.getAirportPositionDao().create(airportPosition);
                        } catch (SQLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        positions.add(airportPosition);
                        flight.setAirportPositions(positions);
                    }

                    distance = 0d;

                    for (int i = 1; i < flightAssignedAirports.size(); i++) {
                        Location airportStartLocation = new Location("test");
                        airportStartLocation.setLatitude(flightAssignedAirports.get(i - 1).getLatitude());
                        airportStartLocation.setLongitude(flightAssignedAirports.get(i - 1).getLongitude());

                        Location airportEndLocation = new Location("test");
                        airportEndLocation.setLatitude(flightAssignedAirports.get(i).getLatitude());
                        airportEndLocation.setLongitude(flightAssignedAirports.get(i).getLongitude());

                        distance += (airportStartLocation.distanceTo(airportEndLocation) / 1000);
                    }

                    if (distance != null) {
                        flight.setDistance(distance);
                        distanceOk = true;
                    } else {
                        Toast.makeText(ctx, "Route nicht vollständig. Bitte Eingaben überprüfen.", Toast.LENGTH_LONG).show();

                        return;
                    }

                    consumptionOk = true;
                    flight.setmFlighType(flightTypeSpinner.getSelectedItemPosition());
                    flight.setStartAirportNr(startAirportsSpinner.getSelectedItemPosition());
                    flight.setEndAirportNr(endAirportsSpinner.getSelectedItemPosition());

                    Calculation calculation = new Calculation();
                    Double emissions = calculation.FlightCalculation(flight);
                    roundedEmissions = (int) Math.round(emissions);

                    /**
                     * Die bestehenden Daten werden übernommen, wenn der Flug bereits vorhanden war
                     * und zum Editieren geöffnet wurde.
                     */
                    String currentEmployee = mPrefs.getString("employee", null);

                    for (Employee employee : employeeDao) {
                        if (employee.getId().equals(currentEmployee)) {
                            footprintPosition.setResponsibleSubject(employee);
                        }
                    }

                    if (footprintPosition.getResponsibleSubject() != null) {
                        if (distanceOk && consumptionOk) {
                            if (!description.equals("")) {
                                footprintPosition.setDescription(description);
                            } else {
                                footprintPosition.setDescription("ohne Bezeichnung");
                            }

                            /**
                             * Ermittlung des Treibhauspotentials bzw. des Wasservebrauchs abhängig von
                             * der Category des Footprints.
                             */
                            if (roundedEmissions != 0) {
                                footprintPosition.setCalculation((double) roundedEmissions / 1000);
                            }

                            if (footprint != null) {
                                footprint.setFootprintPositions(footprintPositionList);
                            }

                            footprintPositionList.add(footprintPosition);

                            /**
                             * Persistierung der Daten in der SQLite-DB.
                             */
                            try {
                            	
                            	ListOverview lo = (ListOverview) getFragmentManager().findFragmentByTag("overviewFragment");
                            	
                                if (editedFlight == null) {
                                    footprintDao.createOrUpdate(footprint);
                                    footprintPositionDao.createOrUpdate(footprintPosition);

                                    if (flight != null) {
                                        flightDao.createOrUpdate(flight);
                                    }
                                    
                                    if (lo != null) {
                                        lo.addFootprintPositionToList(footprintPosition);
                                    }
                                } else {
                                    footprintDao.update(footprint);
                                    footprintPositionDao.update(footprintPosition);
                                    flightDao.update(flight);
                                    
                                    if (lo != null) {
                                        ((PositionsAdapter)lo.getListAdapter()).notifyDataSetChanged();
                                    } 
                                }
                                
                                //select overviewTab
                                ActionBar bar = ((Activity) ctx).getActionBar();
                                bar.setSelectedNavigationItem(0);

                                //self destruct
                                getFragmentManager().beginTransaction().remove(me).commit();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(ctx, "Gespeichert!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ctx, "Angaben unvollständig. Bitte Eingaben überprüfen.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ctx, "Bitte zunächst einen Mitarbeiter in den Einstellungen festlegen.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        datePickerButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    DialogFragment newFragment = new DateDialogFragment(FlightPositionFragment.this);
                    newFragment.show(ft, "dialog");
                }
            });

        try {
			airports = helper.getAirportDao().queryForAll().toArray(new Airport[(int) helper.getAirportDao().countOf()]);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        
        startAirportsSpinner.setAdapter(new AirportAdapter(ctx, android.R.layout.simple_spinner_item, airports));
        endAirportsSpinner.setAdapter(new AirportAdapter(ctx, android.R.layout.simple_spinner_item, airports));

        startAirportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                	if(startSpinnerAccessCount > 0){
	                    if (flightAssignedAirports.size() == 0) {
	                        flightAssignedAirports.add((Airport) startAirportsSpinner.getAdapter().getItem(position));
	                    } else {
	                        flightAssignedAirports.set(0, (Airport) startAirportsSpinner.getAdapter().getItem(position));
	                    }
	
	                    updateMap();
                    }
                	startSpinnerAccessCount++;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        endAirportsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    if(endSpinnerAccessCount  > 0){
	                	if (flightAssignedAirports.size() == 1) {
	                        flightAssignedAirports.add((Airport) startAirportsSpinner.getAdapter().getItem(position));
	                    } else {
	                    	if (flightAssignedAirports.size() == 0){
	                    		flightAssignedAirports.add(null);
	                    		flightAssignedAirports.add((Airport) startAirportsSpinner.getAdapter().getItem(position));
	                    	}else{
	                    		flightAssignedAirports.set(1, (Airport) startAirportsSpinner.getAdapter().getItem(position));
	                        }
	                    }
	
	                    updateMap();
                    }
                    endSpinnerAccessCount++;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });        
                
        
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        try {
            footprintPositionDao = helper.getFootprintPositionDao();
            footprintDao = helper.getFootprintDao();
            flightDao = helper.getFlightsDao();
            employeeDao = helper.getEmployeeDao();            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bundle arguments = getArguments();

        if (arguments != null) {
            int clickedID = arguments.getInt("clickedPosition");

            if (clickedID != 0) {
                clickedPosition = clickedID;
            }
        }

        if (clickedPosition != 0) {
            try {
                editedPosition = footprintPositionDao.queryForId(clickedPosition);
                footprint = editedPosition.getFootprint();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

            footprintPosition = editedPosition;
            entryFragment.setFlightChecked();

            String dateString = editedPosition.getDate();

            if (dateString != null) {
                String unformattedDate = LocalDateTime.parse(dateString).toString();
                String[] dateOnly = unformattedDate.split("T");
                String[] dateParts = dateOnly[0].split("-");
                String formattedDate2 = dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
                dateView.setText(formattedDate2);
            }

            descriptionView.setText(editedPosition.getDescription());

            for (Flight flightDb : flightDao) {
                if (flightDb.getFootprintPosition().getInternalId() == editedPosition.getInternalId()) {
                    editedFlight = flightDb;
                    flight = editedFlight;
                    flightTypeSpinner.setSelection(flight.getmFlighType());

                    if (flight.getAirportPositions() != null) {
                        int index = 1;

                        for (AirportPosition airportPosition : flight.getAirportPositions()) {
                            try {
                                Airport airport = helper.getAirportDao().queryForId(airportPosition.getAirportId());

                                if ((airport != null) && (index == 1)) {
                                    flightAssignedAirports.add(airport);
                                } else if ((airport != null) && (index == flight.getAirportPositions().size())) {
                                    flightAssignedAirports.add(airport);
                                }
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                            index++;
                        }                        
                    }

                    break;
                }
            }
        } else {
            try {
                footprint = footprintDao.queryForId(mCfId);
            } catch (SQLException e2) {
                e2.printStackTrace();
            }

            int newId = helper.getNewPositionId();
            footprintPosition = new FootprintPosition();
            footprintPosition.setId(0);
            footprintPosition.setInternalId(newId);
            flight = new Flight();
            flight.setId(0);
            flight.setInternalId(newId);
            footprintPosition.setFootprint(footprint);
        }        
        
        if (flightAssignedAirports.size() > 0) {
            int index = 1;

            for (Airport airport : flightAssignedAirports) {
                if ((index == 1) && (airport != null)) {
                    setStartAirport(new LatLng(airport.getLatitude(), airport.getLongitude()));
                } else if (index == flight.getAirportPositions().size()) {
                    setEndAirport(new LatLng(airport.getLatitude(), airport.getLongitude()));
                }

                index++;
            }
        }
        
        updateMap();
        
    }        
    
    private void getCarbonFootprintIdFromSettings() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        String cfIdString = mPrefs.getString("carbonfootprint", "none");

        if (cfIdString != "none") {
            mCfId = Integer.parseInt(cfIdString);
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        //do some stuff for example write on log and update TextField on activity
        Log.w("DatePicker", "Date = " + year);
        dateView.setText(day + "." + month + "." + year);
    }

    public void setStartAirport(LatLng newAirportCoords) {
        //		startAirport = new LatLng(newAirportCoords.latitude, newAirportCoords.longitude);
        for (int i = 0; i < airports.length; i++) {
            Airport airport = airports[i];
            Location airportLocation = new Location("test");
            airportLocation.setLatitude(airport.getLatitude());
            airportLocation.setLongitude(airport.getLongitude());

            Location clickedAirport = new Location("test");
            clickedAirport.setLatitude(newAirportCoords.latitude);
            clickedAirport.setLongitude(newAirportCoords.longitude);

            float distance = airportLocation.distanceTo(clickedAirport);

            if (distance < 100) {
                startAirportsSpinner.setSelection(i);
                startAirportsSpinner.setSelected(true);

                return;
            }
        }
    }

    public void setEndAirport(LatLng newAirportCoords) {
        //		endAirport = new LatLng(newAirportCoords.latitude, newAirportCoords.longitude);	
        for (int i = 0; i < airports.length; i++) {
            Airport airport = airports[i];
            Location airportLocation = new Location("test");
            airportLocation.setLatitude(airport.getLatitude());
            airportLocation.setLongitude(airport.getLongitude());

            Location clickedAirport = new Location("test");
            clickedAirport.setLatitude(newAirportCoords.latitude);
            clickedAirport.setLongitude(newAirportCoords.longitude);

            float distance = airportLocation.distanceTo(clickedAirport);

            if (distance < 100) {
                endAirportsSpinner.setSelection(i);
                endAirportsSpinner.setSelected(true);

                return;
            }
        }
    }

    private void updateMap() {
        if (flightAssignedAirports.size() > 1) {

            if(flightAssignedAirports.get(0) == null || flightAssignedAirports.get(flightAssignedAirports.size() - 1) == null){
            	return;
            }        	
        	
        	Location startLoc = new Location("Test");
            startLoc.setLatitude(flightAssignedAirports.get(0).getLatitude());
            startLoc.setLongitude(flightAssignedAirports.get(0).getLongitude());
            
            Location endLoc = new Location("Test");
            endLoc.setLatitude(flightAssignedAirports.get(flightAssignedAirports.size() - 1).getLatitude());
            endLoc.setLongitude(flightAssignedAirports.get(flightAssignedAirports.size() - 1).getLongitude());

            float unconvertedDistance = startLoc.distanceTo(endLoc);

            if (distance == null) {
                distance = (double) Math.round(unconvertedDistance / 1000);
            }

//            PolylineOptions rectLine = new PolylineOptions().add(new LatLng(flightAssignedAirports.get(0).getLatitude(), flightAssignedAirports.get(0).getLongitude()), new LatLng(flightAssignedAirports.get(flightAssignedAirports.size() - 1).getLatitude(), flightAssignedAirports.get(flightAssignedAirports.size() - 1).getLongitude())).width(3).color(Color.RED);
            airportMap.drawRoute(flightAssignedAirports);
        }
    }

    public void onResume() {
        super.onResume();

        if (!resumeHasRun) {
            resumeHasRun = true;

            return;
        }
    }

    public void setStartCoords(LatLng start) {
        startLatLng = start;
        footprintPosition.setStartLat(startLatLng.latitude);
        footprintPosition.setStartLng(startLatLng.longitude);

        startLoc.setLatitude(startLatLng.latitude);
        startLoc.setLongitude(startLatLng.longitude);

        if ((startLatLng != null) && (endLatLng != null)) {
            float unconvertedDistance = startLoc.distanceTo(endLoc);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString("distance", ((double) Math.round(unconvertedDistance / 1000)) + "");
            prefEditor.commit();
        }
        
        updateMap();
    }

    public void setEndCoords(LatLng end, Context ctx) {
        endLatLng = end;
        footprintPosition.setEndLat(endLatLng.latitude);
        footprintPosition.setEndLng(endLatLng.longitude);

        endLoc = new Location("Test");
        endLoc.setLatitude(endLatLng.latitude);
        endLoc.setLongitude(endLatLng.longitude);

        if ((startLatLng != null) && (endLatLng != null)) {
            float unconvertedDistance = startLoc.distanceTo(endLoc);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString("distance", ((double) Math.round(unconvertedDistance / 1000)) + "");
            prefEditor.commit();
        }
        
        updateMap();
    }
}
