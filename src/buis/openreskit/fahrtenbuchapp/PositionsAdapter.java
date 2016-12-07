package buis.openreskit.fahrtenbuchapp;

import android.app.Activity;

import android.content.Context;
import android.content.DialogInterface;

import android.content.DialogInterface.OnClickListener;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import buis.openreskit.odata.Car;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Flight;
import buis.openreskit.odata.FootprintPosition;
import buis.openreskit.odata.PublicTransport;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.joda.time.LocalDateTime;

import java.sql.SQLException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.List;


/**
 * Adapter für das Darstellen der Argumente (Datum, Treibhauspotential, etc.)
 * einer Footprint-Position in die einzelnen View Elemente der Listenzeilen mit den Positionen.
 * Siehe:
 * http://code.google.com/p/myandroidwidgets/source/browse/trunk/Phonebook/src/com/abeanie/PhonebookAdapter.java
 */
public class PositionsAdapter extends BaseAdapter implements OnClickListener {
    private DatabaseHelper helper;
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<Flight, Integer> flightsDao;
    private Dao<Car, Integer> carsDao;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private List<FootprintPosition> listPositions;
    FootprintPosition footprintPosition;
    Activity act;
    Context ctx;

    public PositionsAdapter(Activity activity, List<FootprintPosition> listPositions) {
        this.listPositions = listPositions;
        this.act = activity;
    }

    public int getCount() {
        return listPositions.size();
    }

    public Object getItem(int position) {
        return listPositions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * Methode die eine Zeile der Liste zurück liefert.
     * Je nach Positionstyp wird ein anderes Icon (Flugzeug, Auto, etc.) dargestellt.
     * Außerdem die Emissionen sowie der Name der Position.
     * @return View element with data for one Footprint
     */
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ctx = act.getBaseContext();

        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);

        LayoutInflater lf = act.getLayoutInflater();
        convertView = lf.inflate(R.layout.position_list_row, viewGroup, false);

        View row = convertView;

        FootprintPosition entry = listPositions.get(position);
        String convertedTotalCalculation;
        NumberFormat formatter = new DecimalFormat("#0.0");
        Double convertedCalculation = null;
        convertedCalculation = entry.getCalculation();

        if (convertedCalculation != null) {
            convertedTotalCalculation = (formatter.format(convertedCalculation)).toString();
        } else {
            convertedTotalCalculation = "0";
        }

        TextView calculation = (TextView) row.findViewById(R.id.calculation);
        calculation.setText(convertedTotalCalculation + " kg");

        ImageView positionType = (ImageView) row.findViewById(R.id.position_img);

        try {
            footprintPositionDao = helper.getFootprintPositionDao();
            flightsDao = helper.getFlightsDao();
            carsDao = helper.getCarDao();
            publicTransportDao = helper.getPublicTransportDao();
            footprintPosition = footprintPositionDao.queryForId(entry.getInternalId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextView distanceTxt = (TextView) row.findViewById(R.id.distance);

        if (entry.getName().equals("Flug") || entry.getName().equals("Linienflug") || entry.getName().equals("Zielbasierter Flug")) {
            for (Flight flight : flightsDao) {
                if (flight.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                    try {
                        flightsDao.refresh(flight);
                        flightsDao.queryForId(flight.getInternalId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    positionType.setImageResource(R.drawable.flight);
                    distanceTxt.setText((formatter.format(flight.getDistance())).toString() + " km");
                }
            }
        }

        if (entry.getName().equals("Fahrzeug") || entry.getName().equals("Fahrzeug (GPS)") || entry.getName().equals("Herstellerfahrzeug") || entry.getName().equals("Fahrtenbuch-Autofahrt")) {
            for (Car car : carsDao) {
                if (car.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                    try {
                        carsDao.refresh(car);
                        carsDao.queryForId(car.getInternalId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                	if(entry.getName().equalsIgnoreCase("Fahrzeug (GPS)")){
                		positionType.setImageResource(R.drawable.car_gps);                		
                	}else{
                		positionType.setImageResource(R.drawable.car);	
                	}                      
                                        
                    distanceTxt.setText((formatter.format(car.getDistance())).toString() + " km");
                }
            }
        }

        if (entry.getName().equals("Energieverbrauch") || entry.getName().equals("Maschinenverbrauch")) {
            positionType.setImageResource(R.drawable.energyconsumption);
        }

        if (entry.getName().equalsIgnoreCase("Öffentlicher Verkehr") || entry.getName().equalsIgnoreCase("Öffentlicher Verkehr (GPS)")) {
            for (PublicTransport publicTransport : publicTransportDao) {
            	
            	if(publicTransport.getFootprintPosition() == null){
            		continue;
            	}
            	
                if (publicTransport.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                    try {
                        publicTransportDao.refresh(publicTransport);
                        publicTransportDao.queryForId(publicTransport.getInternalId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    distanceTxt.setText((formatter.format(publicTransport.getDistance())).toString() + " km");

                    switch (publicTransport.getTransportType()) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    	
                    	if(entry.getName().equalsIgnoreCase("Öffentlicher Verkehr (GPS)")){
                    		positionType.setImageResource(R.drawable.train_gps);
                    		break;
                    	}                       	
                    	
                        //Metro
                        positionType.setImageResource(R.drawable.train);

                        break;

                    case 4:
                    	
                    	if(entry.getName().equalsIgnoreCase("Öffentlicher Verkehr (GPS)")){
                    		positionType.setImageResource(R.drawable.bus_gps);
                    		break;
                    	}                    	
                    	
                        //Straßenbahn
                        positionType.setImageResource(R.drawable.bus);

                        break;
                    }
                }
            }
        }

        TextView dateTextView = (TextView) row.findViewById(R.id.datetxt);
        TextView startTextView = (TextView) row.findViewById(R.id.starttxt);
        TextView endTextView = (TextView) row.findViewById(R.id.endtxt);

        String dateString = footprintPosition.getDate();

        if (dateString != null) {
            String unformattedDate = LocalDateTime.parse(dateString).toString();
            String[] dateOnly = unformattedDate.split("T");
            String[] dateParts = dateOnly[0].split("-");
            String formattedDate = dateParts[2] + "." + dateParts[1] + "." + dateParts[0];
            dateTextView.setText(formattedDate);
        }

        startTextView.setText(footprintPosition.getStartAddress());
        endTextView.setText(footprintPosition.getEndAddress());

        ImageView cfparrow = (ImageView) row.findViewById(R.id.arrowcf_img);
        cfparrow.setImageResource(R.drawable.arrowcf);

        return convertView;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }
}
