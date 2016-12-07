package buis.openreskit.fahrtenbuchapp;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDateTime;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import buis.openreskit.odata.Calculation;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Employee;
import buis.openreskit.odata.Footprint;
import buis.openreskit.odata.FootprintPosition;
import buis.openreskit.odata.GeoLocation;
import buis.openreskit.odata.PublicTransport;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;


public class GpsTrainPositionFragment extends GpsFragment {

    private EditText descriptionView;

    private EntryFragment entryFragment;

    private int roundedEmissions;
    private DatabaseHelper helper;
    private FootprintPosition footprintPosition = new FootprintPosition();
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<Footprint, Integer> footprintDao;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private Dao<Employee, Integer> employeeDao;

    private PublicTransport publicTransport;
    private Footprint footprint;
    private int clickedPosition;
    private FootprintPosition editedPosition;
    private PublicTransport editedTrain;
    private Spinner trainTypeSpinner;
    private GpsTrainPositionFragment me;

	private Button acceptButton;
    
    public GpsTrainPositionFragment(ArrayList<Location> gpsLocations, GPSTracker gpsTracker){
    	super(gpsLocations, gpsTracker);
    }
    
    public GpsTrainPositionFragment(Collection<GeoLocation> geoLocations) {
		super(geoLocations);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gpstrainfragment, container, false);
        me = this;
        ctx = getActivity();
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
        getCarbonFootprintIdFromSettings();        
        
        entryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.placeholder);

        ImageView datePickerButton = (ImageView) view.findViewById(R.id.datepicker);

        dateView = (EditText) view.findViewById(R.id.date);

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        String formattedDate = dateFormat.format(date);
        dateView.setText(formattedDate);
        descriptionView = (EditText) view.findViewById(R.id.description);
        trainTypeSpinner = (Spinner) view.findViewById(R.id.traintype);
                       
        
        ArrayAdapter<CharSequence> trainTypesSpinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.transporttypes_array, android.R.layout.simple_spinner_item);
        trainTypesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trainTypeSpinner.setAdapter(trainTypesSpinnerAdapter);
        
        acceptButton = (Button) view.findViewById(R.id.accept);
        final EditText descriptionView = (EditText) view.findViewById(R.id.description);

        gpsLocationList = (ListView) view.findViewById(R.id.gpsLocationList);
        
        
        //Neue Position erstellen
        acceptButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    acceptButtonAction(descriptionView);
                }

				private void acceptButtonAction(
						final EditText descriptionView) {
					ArrayList<FootprintPosition> footprintPositionList = new ArrayList<FootprintPosition>();
                    Boolean distanceOk = false;
                    Boolean consumptionOk = false;
                    String description = descriptionView.getText().toString();

                    /**
                     * Eine neue Position wird erstellt.
                     */
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

                    footprintPosition.setName("Öffentlicher Verkehr (GPS)");
                    footprintPosition.setIconId("CfPublicTransport.png");
                    footprintPosition.setPositionType("OpenResKit.DomainModel.GeoLocatedPublicTransport");
                    footprintPosition.setCarbonFootprintCategoryId("Öffentliche Verkehrsmittel");

                    ArrayList<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
                    
                    for(Location location : gpsLocations){      
                    	GeoLocation geoLocation = new GeoLocation(location, footprintPosition);
                    	try {
                    		geoLocation.setInternalId((int) helper.getGeoLocationDao().countOf() + 1);
							helper.getGeoLocationDao().create(geoLocation);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    	geoLocations.add(geoLocation);
                    }
                    
                    footprintPosition.setGeoLocations(geoLocations);
                    
                    distance = 0d;
                    
                    for (int l = 1; l < gpsLocations.size(); l++) {
                        Location startLocation = new Location("test");
                        startLocation.setLatitude(gpsLocations.get(l - 1).getLatitude());
                        startLocation.setLongitude(gpsLocations.get(l - 1).getLongitude());

                        Location endLocation = new Location("test");
                        endLocation.setLatitude(gpsLocations.get(l).getLatitude());
                        endLocation.setLongitude(gpsLocations.get(l).getLongitude());

                        distance += (startLocation.distanceTo(endLocation) / 1000);
                    }                    
                    
                    publicTransport.setFootprintPosition(footprintPosition);
                    publicTransport.setTransportType(trainTypeSpinner.getSelectedItemPosition());

                    if (distance != null) {
                        publicTransport.setDistance(distance);
                        distanceOk = true;
                    } else {
                        Toast.makeText(ctx, "Route nicht vollständig. Bitte Eingaben überprüfen.", Toast.LENGTH_LONG).show();

                        return;
                    }

                    consumptionOk = true;

                    Calculation calculation = new Calculation();
                    Double emissions = calculation.PublicTransportCalculation(publicTransport);
                    roundedEmissions = (int) Math.round(emissions);

                    /**
                     * Die bestehenden Daten werden übernommen, wenn der Flug bereits vorhanden war
                     * und zum Editieren geöffnet wurde.
                     */
                    String currentEmployee = sharedPreferences.getString("employee", null);

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

                            //      	  		currentFootprint.setCalculation(totalFp+(currentFp/1000));
                            if (footprint != null) {
                                footprint.setFootprintPositions(footprintPositionList);
                            }

                            footprintPositionList.add(footprintPosition);

                            /**
                             * Persistierung der Daten in der SQLite-DB.
                             */
                            try {
                            	
                            	ListOverview lo = (ListOverview) getFragmentManager().findFragmentByTag("overviewFragment");
                            	
                                if (editedTrain == null) {
                                    footprintDao.createOrUpdate(footprint);
                                    footprintPositionDao.createOrUpdate(footprintPosition);

                                    if (publicTransport != null) {
                                        publicTransportDao.createOrUpdate(publicTransport);
                                    }
                                } else {
                                    footprintDao.update(footprint);
                                    footprintPositionDao.update(footprintPosition);
                                    publicTransportDao.update(publicTransport);
                                    
                                    if (lo != null) {
                                        ((PositionsAdapter)lo.getListAdapter()).notifyDataSetChanged();
                                    }
                                    
                                    if (lo != null) {
                                    	lo.addFootprintPositionToList(footprintPosition);
                                    }                                    
                                    
                                }                                                                                            

                                //select overviewTab
                                ActionBar bar = ((Activity) ctx).getActionBar();
                                bar.setSelectedNavigationItem(0);

                                if(gpsTracker != null){
                                	gpsTracker.stopUsingGPS();
                                }
                                
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
                    DialogFragment newFragment = new DateDialogFragment(GpsTrainPositionFragment.this);
                    newFragment.show(ft, "dialog");
                }
            });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            footprintPositionDao = helper.getFootprintPositionDao();
            publicTransportDao = helper.getPublicTransportDao();
            footprintDao = helper.getFootprintDao();
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
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            
            if(editedPosition != null){
            	acceptButton.setText("Bestätigen");
            }             

            footprintPosition = editedPosition;
            entryFragment.setTrainChecked();
        	            
            String dateString = editedPosition.getDate();

            if (dateString != null) {
                String unformattedDate = LocalDateTime.parse(dateString).toString();
                String[] dateOnly = unformattedDate.split("T");
                String[] dateParts = dateOnly[0].split("-");
                String formattedDate2 = dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
                dateView.setText(formattedDate2);
            }

            descriptionView.setText(editedPosition.getDescription());

            for (PublicTransport publicTransport2 : publicTransportDao) {
                if (publicTransport2.getFootprintPosition().getInternalId() == editedPosition.getInternalId()) {
                    try {
                        editedTrain = publicTransportDao.queryForId(publicTransport2.getInternalId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    publicTransport = editedTrain;
                    trainTypeSpinner.setSelection(editedTrain.getTransportType());
                }
            }
        } else {
            try {
                footprint = footprintDao.queryForId(getCarbonFootprintIdFromSettings());
            } catch (SQLException e2) {
                e2.printStackTrace();
            }

            int newId = helper.getNewPositionId();
            footprintPosition = new FootprintPosition();
            footprintPosition.setId(0);
            footprintPosition.setInternalId(newId);
            publicTransport = new PublicTransport();
            publicTransport.setId(0);
            publicTransport.setInternalId(newId);
            footprintPosition.setFootprint(footprint);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        //http://stackoverflow.com/questions/4993063/how-to-call-android-contacts-list-and-select-one-phone-number-from-its-details-s
        String bla = null;

        if (data != null) {
            Uri uri = data.getData();
            String[] projection = { Phone.NUMBER };
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            //		            while (cursor.moveToNext()) {
            int column = cursor.getColumnIndex(Phone.NUMBER);
            bla = cursor.getString(column);
            cursor.close();
            showSelectedNumber(bla);
        }
    }

    public void showSelectedNumber(String adress) {
        Toast.makeText(ctx, adress, Toast.LENGTH_LONG).show();
    }

    public static GeoPoint searchLocationByName(Context context, String locationName) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        GeoPoint gp = null;

        try {
            List<Address> addresses = geoCoder.getFromLocationName(locationName, 1);

            for (Address address : addresses) {
                gp = new GeoPoint((int) (address.getLatitude() * 1E6), (int) (address.getLongitude() * 1E6));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return gp;
    }		
}
