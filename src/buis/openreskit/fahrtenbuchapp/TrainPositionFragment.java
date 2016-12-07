package buis.openreskit.fahrtenbuchapp;

import android.app.ActionBar;
import android.app.Activity;

import android.app.DatePickerDialog.OnDateSetListener;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;

import android.graphics.Color;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import android.net.Uri;

import android.os.AsyncTask;
import android.os.Bundle;

import android.preference.PreferenceManager;

import android.provider.ContactsContract;

import android.provider.ContactsContract.CommonDataKinds.Phone;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;

import android.view.View.OnClickListener;

import android.view.ViewGroup;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemSelectedListener;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import buis.openreskit.odata.Calculation;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Employee;
import buis.openreskit.odata.Footprint;
import buis.openreskit.odata.FootprintPosition;
import buis.openreskit.odata.PublicTransport;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.joda.time.LocalDateTime;

import org.w3c.dom.Document;

import java.io.IOException;

import java.sql.SQLException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TrainPositionFragment extends Fragment implements OnDateSetListener {
    private Context ctx;
    private EditText descriptionView;
    private EditText dateView;
    private Spinner startEntryTypesSpinner;
    private Spinner endEntryTypesSpinner;
    private AutoCompleteTextView startAddress;
    private AutoCompleteTextView endAddress;
    private GoogleMap mMap;
    private SharedPreferences mPrefs;
    private ProgressDialog pdia;
    private EntryFragment entryFragment;
    private ImageView startImage;
    private ImageView endImage;
    private MapFragment mapFrag;
    private GMapV2Direction md = new GMapV2Direction();
    private Document doc;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private int roundedEmissions;
    private DatabaseHelper helper;
    private FootprintPosition footprintPosition = new FootprintPosition();
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<Footprint, Integer> footprintDao;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private Dao<Employee, Integer> employeeDao;
    private Double distance;
    private int mCfId;
    private PublicTransport publicTransport;
    private Footprint footprint;
    private int clickedPosition;
    private FootprintPosition editedPosition;
    private PublicTransport editedTrain;
    private Spinner trainTypeSpinner;
    private TrainPositionFragment me;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trainfragment, container, false);
        me = this;
        ctx = getActivity();
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
        getCarbonFootprintIdFromSettings();

        entryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.placeholder);
        startImage = (ImageView) view.findViewById(R.id.startSources);
        endImage = (ImageView) view.findViewById(R.id.endSources);

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
        
    	mapFrag = (MapFragment) MainActivity.getFragmentRepository().getFragment("MapFrag");
    	mMap = mapFrag.getMap();
    	MainActivity.getFragmentRepository().activateFragment(R.id.mapframe, "MapFrag", getFragmentManager());

        startAddress = (AutoCompleteTextView) view.findViewById(R.id.start);
        startAddress.setAdapter(new AutoCompleteAdapter(ctx));

        endAddress = (AutoCompleteTextView) view.findViewById(R.id.end);
        endAddress.setAdapter(new AutoCompleteAdapter(ctx));

        Button acceptButton = (Button) view.findViewById(R.id.accept);
        final EditText descriptionView = (EditText) view.findViewById(R.id.description);

        startImage.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    startEntryTypesSpinner.performClick();
                }
            });
        endImage.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    endEntryTypesSpinner.performClick();
                }
            });
        //    entryTypesSpinner = new EntryTypesSpinner(ctx);
        startEntryTypesSpinner = (Spinner) view.findViewById(R.id.startTypesSpinner);

        ArrayAdapter<CharSequence> startEntryTypesSpinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.entrytypes_array, android.R.layout.simple_spinner_item);
        startEntryTypesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startEntryTypesSpinner.setAdapter(startEntryTypesSpinnerAdapter);
        startEntryTypesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View view, int pos, long id) {
                    switch (pos) {
                    case 0:
                        startAddress.setSelected(true);

                        break;

                    case 1:

                        //Aus Adressbuch
                        //http://stackoverflow.com/questions/866769/how-to-call-android-contacts-list
                        Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(contactsIntent, 1);

                        break;

                    case 2:

                        //Standortfavorit
                        Double favoriteLatitude = Double.parseDouble(mPrefs.getString("favorite_latitude", "0.0"));
                        Double favoriteLongitude = Double.parseDouble(mPrefs.getString("favorite_longitude", "0.0"));

                        if ((favoriteLatitude != 0.0) && (favoriteLongitude != 0.0)) {
                            LatLng favoriteLatLng = new LatLng(favoriteLatitude, favoriteLongitude);
                            setStartCoords(favoriteLatLng);

                            Location favoriteLocation = new Location("favoriteLocation");
                            favoriteLocation.setLatitude(favoriteLatitude);
                            favoriteLocation.setLongitude(favoriteLongitude);

                            MapFrag mapFrag = new MapFrag();
                            TrainPositionFragment newBusPositionFragment = (TrainPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);
                            mapFrag.startGetAddressTask(ctx, favoriteLocation, true, newBusPositionFragment);

                            break;
                        } else {
                            Toast.makeText(ctx, "Kein Standortfavorit gespeichert.", Toast.LENGTH_LONG).show();
                        }

                        break;

                    case 3:

                        //Eigener Standort
                        LocationManager locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String provider = locMan.getBestProvider(criteria, false);
                        Location location = locMan.getLastKnownLocation(provider);

                        if (location != null) {
                            LatLng myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            //    		            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 15.0f));
                            setStartCoords(myCurrentLocation);

                            Location myLocation = new Location("favoriteLocation");
                            ;
                            myLocation.setLatitude(myCurrentLocation.latitude);
                            myLocation.setLongitude(myCurrentLocation.longitude);

                            MapFrag mapFrag = new MapFrag();
                            TrainPositionFragment newBusPositionFragment = (TrainPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);
                            mapFrag.startGetAddressTask(ctx, myLocation, true, newBusPositionFragment);
                        } else {
                            Toast.makeText(ctx, "Standort kann nicht lokalisiert werden.", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    default:
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        endEntryTypesSpinner = (Spinner) view.findViewById(R.id.endTypesSpinner);

        ArrayAdapter<CharSequence> endEntryTypesSpinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.entrytypes_array, android.R.layout.simple_spinner_item);
        endEntryTypesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        endEntryTypesSpinner.setAdapter(endEntryTypesSpinnerAdapter);
        endEntryTypesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View view, int pos, long id) {
                    switch (pos) {
                    case 0:
                        //Eingabe
                        //	    		    	endAddress.setText("");
                        endAddress.setSelected(true);

                        break;

                    case 1:

                        //Aus Adressbuch
                        //http://stackoverflow.com/questions/866769/how-to-call-android-contacts-list
                        Intent contactsIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(contactsIntent, 1);

                        break;

                    case 2:

                        //Standortfavorit
                        Double favoriteLatitude = Double.parseDouble(mPrefs.getString("favorite_latitude", "0.0"));
                        Double favoriteLongitude = Double.parseDouble(mPrefs.getString("favorite_longitude", "0.0"));

                        if ((favoriteLatitude != 0.0) && (favoriteLongitude != 0.0)) {
                            LatLng favoriteLatLng = new LatLng(favoriteLatitude, favoriteLongitude);
                            setEndCoords(favoriteLatLng);

                            Location favoriteLocation = new Location("favoriteLocation");
                            ;
                            favoriteLocation.setLatitude(favoriteLatitude);
                            favoriteLocation.setLongitude(favoriteLongitude);

                            MapFrag mapFrag = new MapFrag();
                            TrainPositionFragment newTrainPositionFragment = (TrainPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);
                            mapFrag.startGetAddressTask(ctx, favoriteLocation, false, newTrainPositionFragment);

                            break;
                        } else {
                            Toast.makeText(ctx, "Kein Standortfavorit gespeichert.", Toast.LENGTH_LONG).show();
                        }

                        break;

                    case 3:

                        //Eigener Standort
                        LocationManager locMan = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        String provider = locMan.getBestProvider(criteria, false);
                        Location location = locMan.getLastKnownLocation(provider);

                        if (location != null) {
                            LatLng myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            //		            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 15.0f));
                            setEndCoords(myCurrentLocation);

                            Location myLocation = new Location("favoriteLocation");
                            ;
                            myLocation.setLatitude(myCurrentLocation.latitude);
                            myLocation.setLongitude(myCurrentLocation.longitude);

                            MapFrag mapFrag = new MapFrag();
                            TrainPositionFragment newTrainPositionFragment = (TrainPositionFragment) getFragmentManager().findFragmentById(R.id.positionFragment);
                            mapFrag.startGetAddressTask(ctx, myLocation, false, newTrainPositionFragment);
                        } else {
                            Toast.makeText(ctx, "Standort kann nicht lokalisiert werden.", Toast.LENGTH_SHORT).show();
                        }

                        break;

                    default:
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

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
                    footprintPosition.setStartAddress(startAddress.getText().toString());
                    footprintPosition.setEndAddress(endAddress.getText().toString());
                    footprintPosition.setName("Öffentlicher Verkehr");
                    footprintPosition.setIconId("CfPublicTransport.png");
                    footprintPosition.setPositionType("OpenResKit.DomainModel.GeoLocatedPublicTransport");
                    footprintPosition.setCarbonFootprintCategoryId("Öffentliche Verkehrsmittel");

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

                            //      	  		currentFootprint.setCalculation(totalFp+(currentFp/1000));
                            if (footprint != null) {
                                footprint.setFootprintPositions(footprintPositionList);
                            }

                            footprintPositionList.add(footprintPosition);

                            //      	  		for (FootprintPosition fPos : footprint.getFootprintPositions())
                            //      	  		{
                            //      	  			footprintPositionList.add(fPos);
                            //      	  		}
                            /**
                             * Persistierung der Daten in der SQLite-DB.
                             */
                            try {
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
                                }

                                //update list after adding position
                                ListOverview lo = (ListOverview) getFragmentManager().findFragmentByTag("overviewFragment");

                                if (lo != null) {
                                	lo.addFootprintPositionToList(footprintPosition);
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
        startAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        GeoPoint start = searchLocationByName(ctx, startAddress.getText().toString());

                        if (start != null) {
                            startLatLng = new LatLng(start.getLatitudeE6() / 1E6, start.getLongitudeE6() / 1E6);
                            setStartCoords(startLatLng);

                            //	    		      	if (startLatLng != null && endLatLng != null) {
                            //	    		    		(new GetRoadDistanceTask()).execute();
                            //	    		    	}
                        }
                    }
                }
            });
        endAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        GeoPoint end = searchLocationByName(ctx, endAddress.getText().toString());

                        if (end != null) {
                            endLatLng = (new LatLng(end.getLatitudeE6() / 1E6, end.getLongitudeE6() / 1E6));
                            setEndCoords(endLatLng);

                            //	    		      	if (startLatLng != null && endLatLng != null) {
                            //	    		    		(new GetRoadDistanceTask()).execute();
                            //	    		    	}
                        }
                    }
                }
            });
        datePickerButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    DialogFragment newFragment = new DateDialogFragment(TrainPositionFragment.this);
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

            footprintPosition = editedPosition;
            entryFragment.setTrainChecked();
            startAddress.setText(editedPosition.getStartAddress());
            endAddress.setText(editedPosition.getEndAddress());
            setStartCoords(new LatLng(editedPosition.getStartLat(), editedPosition.getStartLng()));
            setEndCoords(new LatLng(editedPosition.getEndLat(), editedPosition.getEndLng()));

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
                footprint = footprintDao.queryForId(mCfId);
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

    @SuppressWarnings("unused")
    public void setStartCoords(LatLng start) {
        startLatLng = start;
        footprintPosition.setStartLat(startLatLng.latitude);
        footprintPosition.setStartLng(startLatLng.longitude);

        if (mMap != null) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15.0f));

            Marker startMarker = mMap.addMarker(new MarkerOptions().position(startLatLng).title("Start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            if ((startLatLng != null) && (endLatLng != null)) {
                (new GetRoadDistanceTask()).execute();
            }
        }
    }

    @SuppressWarnings("unused")
    public void setEndCoords(LatLng end) {
        endLatLng = end;
        footprintPosition.setEndLat(endLatLng.latitude);
        footprintPosition.setEndLng(endLatLng.longitude);

        if (mMap != null) {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(endLatLng, 15.0f));

            Marker endMarker = mMap.addMarker(new MarkerOptions().position(endLatLng).title("Ende").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            if ((startLatLng != null) && (endLatLng != null)) {
                (new GetRoadDistanceTask()).execute();
            }
        }
    }

    public void setAddressText(String item, Boolean isStart) {
        TextView view = null;
        
        if(getView() == null){
        	return;
        }
        
        if (isStart) {
            view = (TextView) getView().findViewById(R.id.start);
        } else if (!isStart) {
            view = (TextView) getView().findViewById(R.id.end);
        }

        view.setText(item);
    }

    class GetRoadDistanceTask extends AsyncTask<Location, Void, Double> {
        @Override
        protected Double doInBackground(Location... params) {
            doc = md.getDocument(startLatLng, endLatLng, GMapV2Direction.MODE_DRIVING);
            distance = md.getDistanceValue(doc);

            return distance;
        }

        @SuppressWarnings("unused")
        protected void onPostExecute(Double result) {
            ArrayList<LatLng> directionPoint = md.getDirection(doc);
            PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }

            mMap.clear();

            Marker startMarker = mMap.addMarker(new MarkerOptions().position(startLatLng).title("Ende").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            Marker endMarker = mMap.addMarker(new MarkerOptions().position(endLatLng).title("Ende").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMap.addPolyline(rectLine);
            pdia.dismiss();
        }

        protected void onPreExecute() {
            pdia = new ProgressDialog(ctx);
            pdia.setMessage("Route berechnen");
            pdia.show();
        }
    }
}
