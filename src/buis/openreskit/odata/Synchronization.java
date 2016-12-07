package buis.openreskit.odata;

import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.SharedPreferences;

import android.location.Location;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.AsyncTask;

import android.preference.PreferenceManager;

import android.util.Base64;
import android.util.Log;

import android.widget.Toast;

import buis.openreskit.fahrtenbuchapp.ListOverview;
import buis.openreskit.fahrtenbuchapp.MainActivity;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.android.gms.maps.model.LatLng;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;

import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Funktion zur Durchführung der Datensynchronisation mit einem OpenResKit-Hub.
 * Dies beinhaltet eine Upload- und eine Download-Funktion.
 *
 */
public class Synchronization {
    private Context ctx;
    private DatabaseHelper helper;
    private Dao<Footprint, Integer> footprintDao;
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<AirportPosition, Integer> airportPositionDao;
    private Dao<Flight, Integer> flightDao;
    private Dao<Car, Integer> carDao;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private Dao<Employee, Integer> employeeDao;
    private String jsonEmployeeText = null;
    private String jsonAirportPositionsText = null;
    private String jsonGeoLocationsText = null;
    private ObjectMapper mapper = new ObjectMapper();
    private Activity act;
    private ProgressDialog pdia;
    private String jsonText = null;
    private JSONArray footprintData = null;
    private Boolean uploadOk = false;
    private SharedPreferences prefs;
    private String serverIp = null;
    private Boolean inetTest = false;
    private Dao<GeoLocation, Integer> geoLocationDao;

    public Synchronization() {
    }

    /**
     * Methode zum Aufrufen des asynchronen Tasks zum Herunterladen der Daten von einem Server.
     * @param context
     * @param activity
     */
    public void getData(Context context, Activity activity, boolean updateCatalogs) {
        ctx = context;
        act = activity;
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (!prefs.getString("ip", "").equalsIgnoreCase("")) {
            serverIp = prefs.getString("ip", "");
        } else {
            serverIp = "141.45.165.154";
        }

        if (isOnline()) {
        	if(updateCatalogs){
        		new GetAirports(updateCatalogs).execute((Void[]) null);
            }else{
            	new GetFootprints(false).execute();
            }
        } else {
            Toast.makeText(ctx, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Methode zum Aufrufen des asynchronen Tasks zum Hochladen von Daten zu einem Server.
     * @param context
     * @param databaseHelper
     */
    public void writeData(Context context, DatabaseHelper databaseHelper) {
        ctx = context;
        helper = databaseHelper;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (!prefs.getString("ip", "").equalsIgnoreCase("")) {
            serverIp = prefs.getString("ip", "");
        } else {
            serverIp = "141.45.165.154";
        }

        if (isOnline()) {
            new WriteData().execute((Void[]) null);
        } else {
            Toast.makeText(ctx, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
        }
    }

    public void GetCarData() {
        new GetEuro4Cars(true).execute();
    }

    /**
     * Methode zur Prüfung, ob das Smartphone über eine aktive Online-Verbindung verfügt.
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if ((netInfo != null) && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

    /**
     * Methode zur Prüfung, ob der eingegebene Server erreichbar ist.
     */

    //ToDo: UrlConnection liefert -1 als Antwort
    public boolean isReachable() {
        URL url;

        try {
            url = new URL("http://" + serverIp + ":7000/OpenResKitHub/");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept-Encoding", "identity");

            String creds = "root:ork123";
            String encoded = new String(Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP));
            urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inetTest = true;

                return true;
            } else {
                return false;
            }
        } catch (IOException e2) {
            Log.e("Error", e2.getMessage());
            e2.printStackTrace();

            return false;
        }
    }

    /**
     * Methode zum Konvertieren eines Input Streams in einen JSON String.
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     * Siehe
     * http://stackoverflow.com/questions/4480363/android-java-utf-8-httpclient-problem
     */
    public String convertStreamToString(InputStream is) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }

    /**
     * Methode zum Upload aller auf dem Gerät in der SQLite DB gespeicherten Datensätze auf einen
     * OpenResKit-Hub. Dies wird durch einen asynchronen Task bewerkstelligt. Vor dem Upload
     * wird die lokale DB abgefragt und eine Fortschrittsanzeige geöffnet. Der Upload wird mittelt
     * HTTPPost bewerkstelligt. Wurden alle Daten hochgeladen, wird der Fortschrittsdialog geschlossen.
     */
    private class WriteData extends AsyncTask<Void, Void, Boolean> {
        protected Boolean writeFootprint(Footprint footprint) throws ClientProtocolException, IOException, JSONException {
            HttpPost requestFootprint;
            HttpParams httpParams = new BasicHttpParams();
            HttpResponse response;
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            JSONObject jsonFootprint;
            StringEntity stringEntityFootprint;

            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

            httpParams.setBooleanParameter("http.protocol.expect-continue", false);

            jsonFootprint = JsonConverter.convertFootprintToJson(footprint, helper, serverIp);
            stringEntityFootprint = new StringEntity(jsonFootprint.toString(), HTTP.UTF_8);
            stringEntityFootprint.setContentType("application/json");
            requestFootprint = null;

            if (footprint.getId() == 0) {
                requestFootprint = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprints");
                requestFootprint.setHeader("X-HTTP-Method-Override", "PUT");
            } else if (footprint.getId() > 0) {
                requestFootprint = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprints(" + footprint.getId() + ")");
                requestFootprint.setHeader("X-HTTP-Method", "MERGE");
            }

            requestFootprint.setHeader("Accept", "application/json");
            requestFootprint.setHeader("Content-type", "application/json;odata=verbose");
            requestFootprint.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));
            requestFootprint.setEntity(stringEntityFootprint);

            response = httpClient.execute(requestFootprint);

            if (response.getEntity() != null) {
                HttpEntity responseEntity = response.getEntity();
                char[] buffer = new char[(int) responseEntity.getContentLength()];
                InputStream stream = responseEntity.getContent();
                InputStreamReader reader = new InputStreamReader(stream);
                reader.read(buffer);
                stream.close();
                uploadOk = true;

                JSONObject answer = new JSONObject(new String(buffer));
                System.out.print(answer);
            } else if ((response.getStatusLine().getStatusCode() == 204) && (footprint.getId() > 0)) {
                uploadOk = true;
            }

            return uploadOk;
        }

        protected Boolean writeFootprintPositions(FootprintPosition footprintPosition) throws ClientProtocolException, IOException, JSONException, SQLException {
            HttpParams httpParams = new BasicHttpParams();
            JSONObject jsonFootprintPosition;
            StringEntity stringEntityFootprintPosition;
            HttpPost requestFootprintPosition;
            HttpResponse response;
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

            httpParams.setBooleanParameter("http.protocol.expect-continue", false);

            jsonFootprintPosition = JsonConverter.convertPositionToJson(footprintPosition, helper, true, serverIp);

            stringEntityFootprintPosition = new StringEntity(jsonFootprintPosition.toString(), HTTP.UTF_8);
            stringEntityFootprintPosition.setContentType("application/json");

            requestFootprintPosition = null;

            if (footprintPosition.getId() == 0) {
                requestFootprintPosition = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprintPositions");
                requestFootprintPosition.setHeader("X-HTTP-Method-Override", "PUT");
            } else if (footprintPosition.getId() > 0) {
                requestFootprintPosition = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprintPositions(" + footprintPosition.getId() + ")");
                requestFootprintPosition.setHeader("X-HTTP-Method", "MERGE");
            }

            requestFootprintPosition.setHeader("Accept", "application/json");
            requestFootprintPosition.setHeader("Content-type", "application/json;odata=verbose");

            requestFootprintPosition.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));
            requestFootprintPosition.setEntity(stringEntityFootprintPosition);

            response = httpClient.execute(requestFootprintPosition);

            if (response.getEntity() != null) {
                HttpEntity responseEntity = response.getEntity();
                char[] buffer = new char[(int) responseEntity.getContentLength()];
                InputStream stream = responseEntity.getContent();
                InputStreamReader reader = new InputStreamReader(stream);
                reader.read(buffer);
                stream.close();
                uploadOk = true;

                JSONObject answer = new JSONObject(new String(buffer));
                System.out.print(answer);

                FootprintPosition footprintPositionAnswer = mapper.readValue(answer.toString(), FootprintPosition.class);
                footprintPosition.setId(footprintPositionAnswer.getId());
                footprintPositionDao.createOrUpdate(footprintPosition);
            } else if ((response.getStatusLine().getStatusCode() == 204) && (footprintPosition.getId() > 0)) {
                uploadOk = true;
            }

            return uploadOk;
        }

        protected Boolean writeAirportPositions(AirportPosition airportPosition) throws ClientProtocolException, IOException, JSONException, SQLException {
            HttpParams httpParams = new BasicHttpParams();
            JSONObject jsonAirportPositionPosition;
            StringEntity stringEntityAirportPosition;
            HttpPost requestAirportPosition;
            HttpResponse response;
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

            httpParams.setBooleanParameter("http.protocol.expect-continue", false);

            jsonAirportPositionPosition = JsonConverter.convertAirportPositionToJson(airportPosition, helper);

            stringEntityAirportPosition = new StringEntity(jsonAirportPositionPosition.toString(), HTTP.UTF_8);
            stringEntityAirportPosition.setContentType("application/json");

            requestAirportPosition = null;

            if (airportPosition.getId() == 0) {
                requestAirportPosition = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/AirportPositions");
                requestAirportPosition.setHeader("X-HTTP-Method-Override", "PUT");
            } else if (airportPosition.getId() > 0) {
                requestAirportPosition = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/AirportPositions(" + airportPosition.getId() + ")");
                requestAirportPosition.setHeader("X-HTTP-Method", "MERGE");
            }

            requestAirportPosition.setHeader("Accept", "application/json");
            requestAirportPosition.setHeader("Content-type", "application/json;odata=verbose");

            requestAirportPosition.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));
            requestAirportPosition.setEntity(stringEntityAirportPosition);

            response = httpClient.execute(requestAirportPosition);

            if (response.getEntity() != null) {
                HttpEntity responseEntity = response.getEntity();
                char[] buffer = new char[(int) responseEntity.getContentLength()];
                InputStream stream = responseEntity.getContent();
                InputStreamReader reader = new InputStreamReader(stream);
                reader.read(buffer);
                stream.close();
                uploadOk = true;

                JSONObject answer = new JSONObject(new String(buffer));
                System.out.print(answer);
        		try{
	                AirportPosition airportPositionAnswer = mapper.readValue(answer.toString(), AirportPosition.class);
	            	airportPosition.setId(airportPositionAnswer.getId());
	            	helper.getAirportPositionDao().createOrUpdate(airportPosition);
	        	}catch(Exception e){
	        		e.printStackTrace();
	        	}
            }

            return uploadOk;
        }
        
        protected Boolean writeGeoLocations(GeoLocation geoLocation) throws ClientProtocolException, IOException, JSONException, SQLException {
            HttpParams httpParams = new BasicHttpParams();
            JSONObject jsonGeoLocation;
            StringEntity stringEntityGeoLocation;
            HttpPost requestGeoLocation;
            HttpResponse response;
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);

            httpParams.setBooleanParameter("http.protocol.expect-continue", false);

            jsonGeoLocation = JsonConverter.convertGeoLocationToJson(geoLocation, helper);

            stringEntityGeoLocation = new StringEntity(jsonGeoLocation.toString(), HTTP.UTF_8);
            stringEntityGeoLocation.setContentType("application/json");

            requestGeoLocation = null;

            if (geoLocation.getId() == 0) {
                requestGeoLocation = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/GeoLocations");
                requestGeoLocation.setHeader("X-HTTP-Method-Override", "PUT");
            } else if (geoLocation.getId() > 0) {
                requestGeoLocation = new HttpPost("http://" + serverIp + ":7000/OpenResKitHub/GeoLocations(" + geoLocation.getId() + ")");
                requestGeoLocation.setHeader("X-HTTP-Method", "MERGE");
            }

            requestGeoLocation.setHeader("Accept", "application/json");
            requestGeoLocation.setHeader("Content-type", "application/json;odata=verbose");

            requestGeoLocation.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));
            requestGeoLocation.setEntity(stringEntityGeoLocation);

            response = httpClient.execute(requestGeoLocation);

            if (response.getEntity() != null) {
                HttpEntity responseEntity = response.getEntity();
                char[] buffer = new char[(int) responseEntity.getContentLength()];
                InputStream stream = responseEntity.getContent();
                InputStreamReader reader = new InputStreamReader(stream);
                reader.read(buffer);
                stream.close();
                uploadOk = true;

                JSONObject answer = new JSONObject(new String(buffer));
                System.out.print(answer);
                try{	
	                GeoLocation geoLocationAnswer = mapper.readValue(answer.toString(), GeoLocation.class);
	            	geoLocation.setId(geoLocationAnswer.getId());
	            	helper.getGeoLocationDao().createOrUpdate(geoLocation);
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            }

            return uploadOk;
        }        

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (isReachable()) {
                    for (AirportPosition airportPosition : helper.getAirportPositionDao()) {
                        writeAirportPositions(airportPosition);
                    }

                    for (GeoLocation geoLocation : helper.getGeoLocationDao()) {
                        writeGeoLocations(geoLocation);
                    }
                    
                    for (Footprint footprint : footprintDao) {
                        for (FootprintPosition footprintPosition : footprint.getFootprintPositions()) {
                            writeFootprintPositions(footprintPosition);
                        }

                        writeFootprint(footprint);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ctx, "Upload fehlgeschlagen!", Toast.LENGTH_LONG).show();
            }

            return true;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pdia = new ProgressDialog(ctx);
            pdia.setMessage("Schreibe Daten");
            pdia.show();

            try {
                footprintPositionDao = helper.getFootprintPositionDao();
                airportPositionDao = helper.getAirportPositionDao();
                footprintDao = helper.getFootprintDao();
                flightDao = helper.getFlightsDao();
                carDao = helper.getCarDao();
                publicTransportDao = helper.getPublicTransportDao();
                employeeDao = helper.getEmployeeDao();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            pdia.dismiss();

            if (uploadOk && inetTest) {
                Toast.makeText(ctx, "Upload erfolgreich!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, "Upload fehlgeschlagen! Bitte überprüfen sie die Serveradresse.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Methode zum Download aller Datensätze auf einem OpenResKit-Hub und Speicherung in der
     * auf dem Gerät befindlichen SQLite DB. Dies wird durch einen asynchronen Task bewerkstelligt.
     * Vor dem Download wird die lokale DB abgefragt und eine Fortschrittsanzeige geöffnet.
     * Der Download wird mittelt HTTPGet bewerkstelligt. Wurden alle Daten heruntergeladen,
     * werden sie lokal gespeicher (falls nicht schon vorhanden) und der Fortschrittsdialog geschlossen.
     * @return footprints
     */
    private class GetFootprints extends AsyncTask<Void, Void, ArrayList<Footprint>> {
        private boolean updateCarData = false;		

        public GetFootprints(boolean updateCarData) {
            this.updateCarData = updateCarData;
        }

        protected ArrayList<Footprint> doInBackground(Void... params) {
            ArrayList<Footprint> footprints = new ArrayList<Footprint>();

            try {
                HttpParams httpParams = new BasicHttpParams();
                HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
                httpParams.setBooleanParameter("http.protocol.expect-continue", false);

                HttpGet request = new HttpGet("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprints?$format=json&$expand=Positions/ResponsibleSubject");
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");
                request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));

                HttpClient httpClient = new DefaultHttpClient(httpParams);

                if (isReachable()) {
                    //get Employees
                    JSONArray employeeData = null;
                    HttpGet employeeRequest = new HttpGet("http://" + serverIp + ":7000/OpenResKitHub/ResponsibleSubjects/OpenResKit.DomainModel.Employee?$format=json");
                    employeeRequest.setHeader("Accept", "application/json");
                    employeeRequest.setHeader("Content-type", "application/json");
                    employeeRequest.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));

                    HttpResponse employeeResponse = httpClient.execute(employeeRequest);

                    JSONObject serverEmployees = null;

                    if (employeeResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = employeeResponse.getEntity();

                        if (entity != null) {
                            InputStream instream = entity.getContent();
                            jsonEmployeeText = convertStreamToString(instream);
                            instream.close();
                        }
                    }

                    serverEmployees = new JSONObject(jsonEmployeeText);
                    employeeData = serverEmployees.getJSONArray("value");

                    for (int k = 0; k < employeeData.length(); k++) {
                        JSONObject oneEmployeeObject = employeeData.getJSONObject(k);
                        String oneEmployeeString = oneEmployeeObject.toString();
                        Employee employee = mapper.readValue(oneEmployeeString, Employee.class);
                        employeeDao.createOrUpdate(employee);
                    }

                    //get CarbonFootprints
                    HttpResponse response = httpClient.execute(request);
                    response = httpClient.execute(request);

                    JSONObject serverFootprints = null;

                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();

                        if (entity != null) {
                            InputStream instream = entity.getContent();
                            jsonText = convertStreamToString(instream);
                            instream.close();
                        }
                    }

                    serverFootprints = new JSONObject(jsonText);
                    footprintData = serverFootprints.getJSONArray("value");

                    int lastFootprint = 0;

                    for (int i = 0; i < footprintData.length(); i++) {
                        JSONObject oneFootprintObject = footprintData.getJSONObject(i);
                        String oneFootprintString = oneFootprintObject.toString();
                        Footprint footprint = mapper.readValue(oneFootprintString, Footprint.class);

                        footprint.setInternalId(footprint.getId());
                        footprint.setCalculationCategory("Carbon");

                        for (Footprint footprintForNr : footprintDao) {
                            if (footprintForNr.getNr() > lastFootprint) {
                                lastFootprint = footprintForNr.getNr();
                            }
                        }

                        lastFootprint = lastFootprint + 1;
                        footprint.setNr(lastFootprint);

                        ArrayList<FootprintPosition> footprintPositionList = new ArrayList<FootprintPosition>();
                        JSONArray positionsArray = oneFootprintObject.getJSONArray("Positions");

                        //cycle positions
                        for (int j = 0; j < positionsArray.length(); j++) {
                            JSONObject onePositionObject = positionsArray.getJSONObject(j);
                            String onePositionString = onePositionObject.toString();

                            FootprintPosition footprintPosition = mapper.readValue(onePositionString, FootprintPosition.class);
                            String positionType = onePositionObject.getString("odata.type");

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Flight")) {
                                continue;
                            }

                            footprintPosition.setInternalId(footprintPosition.getId());
                            footprintPosition.setFootprint(footprint);
                            footprintPositionList.add(footprintPosition);

                            JSONObject responsibleSubjectObject = onePositionObject.getJSONObject("ResponsibleSubject");
                            String responsibleSubjectString = responsibleSubjectObject.toString();
                            Employee employee = mapper.readValue(responsibleSubjectString, Employee.class);
                            footprintPosition.setResponsibleSubject(employee);

                            
                            JSONArray geoLocationsData = null;
                            HttpGet geoLocationsRequest = new HttpGet("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprintPositions(" + footprintPosition.getId() + ")/GeoLocations");
                            geoLocationsRequest.setHeader("Accept", "application/json");
                            geoLocationsRequest.setHeader("Content-type", "application/json");
                            geoLocationsRequest.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));

                            HttpResponse geoLocationsResponse = httpClient.execute(geoLocationsRequest);

                            if (geoLocationsResponse.getStatusLine().getStatusCode() == 200) {
                                HttpEntity entity = geoLocationsResponse.getEntity();

                                if (entity != null) {
                                    InputStream instream = entity.getContent();
                                    jsonGeoLocationsText = convertStreamToString(instream);
                                    instream.close();
                                }
                            }                            
                            
                            JSONObject serverGeoLocations = new JSONObject(jsonGeoLocationsText);
                            geoLocationsData = serverGeoLocations.getJSONArray("value");

                            ArrayList<GeoLocation> geoLocationList = new ArrayList<GeoLocation>();

                            for (int k = 0; k < geoLocationsData.length(); k++) {
                                JSONObject oneGeoLocationsObject = geoLocationsData.getJSONObject(k);
                                String oneLocationStringString = oneGeoLocationsObject.toString();
                                GeoLocation geoLocation = mapper.readValue(oneLocationStringString, GeoLocation.class);

                                geoLocation.setFootprintPosition(footprintPosition);
                                geoLocation.setInternalId(geoLocation.getId());
                                geoLocationDao.createOrUpdate(geoLocation);
                                geoLocationList.add(geoLocation);
                            }

                            footprintPosition.setGeoLocations(geoLocationList);                            

                            footprintPositionDao.createOrUpdate(footprintPosition);

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.AirportBasedFlight")) {
                                Flight flight = mapper.readValue(onePositionString, Flight.class);
                                flight.setInternalId(flight.getId());
                                flight.setFootprintPosition(footprintPosition);
                                flight.setCalculationCategory("Carbon");

                                JSONArray airportPositionsData = null;
                                HttpGet airportPositionsRequest = new HttpGet("http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprintPositions(" + flight.getId() + ")/OpenResKit.DomainModel.AirportBasedFlight/Airports");
                                airportPositionsRequest.setHeader("Accept", "application/json");
                                airportPositionsRequest.setHeader("Content-type", "application/json");
                                airportPositionsRequest.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("root", "ork123"), "UTF-8", false));

                                HttpResponse airportPositionsResponse = httpClient.execute(airportPositionsRequest);

                                if (airportPositionsResponse.getStatusLine().getStatusCode() == 200) {
                                    HttpEntity entity = airportPositionsResponse.getEntity();

                                    if (entity != null) {
                                        InputStream instream = entity.getContent();
                                        jsonAirportPositionsText = convertStreamToString(instream);
                                        instream.close();
                                    }
                                }

                                JSONObject serverAirportPositions = new JSONObject(jsonAirportPositionsText);
                                airportPositionsData = serverAirportPositions.getJSONArray("value");

                                ArrayList<AirportPosition> airportPositionList = new ArrayList<AirportPosition>();

                                for (int k = 0; k < airportPositionsData.length(); k++) {
                                    JSONObject oneAirportPositionsObject = airportPositionsData.getJSONObject(k);
                                    String oneAirportPositionString = oneAirportPositionsObject.toString();
                                    AirportPosition airportPosition = mapper.readValue(oneAirportPositionString, AirportPosition.class);

                                    airportPosition.setFlight(flight);
                                    airportPosition.setInternalId(airportPosition.getId());
                                    airportPositionDao.createOrUpdate(airportPosition);
                                    airportPositionList.add(airportPosition);
                                }

                                flight.setAirportPositions(airportPositionList);

                                double distance = 0d;                                
                                
                                for (int l = 1; l < airportPositionList.size(); l++) {
                                    Location airportStartLocation = new Location("test");
                                    airportStartLocation.setLatitude(helper.getAirportDao().queryForId(airportPositionList.get(l - 1).getAirportId()).getLatitude());
                                    airportStartLocation.setLongitude(helper.getAirportDao().queryForId(airportPositionList.get(l - 1).getAirportId()).getLongitude());

                                    Location airportEndLocation = new Location("test");
                                    airportEndLocation.setLatitude(helper.getAirportDao().queryForId(airportPositionList.get(l).getAirportId()).getLatitude());
                                    airportEndLocation.setLongitude(helper.getAirportDao().queryForId(airportPositionList.get(l).getAirportId()).getLongitude());

                                    distance += (airportStartLocation.distanceTo(airportEndLocation) / 1000);
                                }

                                flight.setDistance(distance);

                                if (airportPositionList.size() > 1) {
                                    footprintPosition.setStartAddress(helper.getAirportDao().queryForId(airportPositionList.get(0).getAirportId()).getName());
                                    footprintPosition.setEndAddress(helper.getAirportDao().queryForId(airportPositionList.get(airportPositionList.size() - 1).getAirportId()).getName());
                                    footprintPositionDao.createOrUpdate(footprintPosition);
                                }

                                flightDao.createOrUpdate(flight);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Flight")) {
                                Flight flight = mapper.readValue(onePositionString, Flight.class);
                                flight.setInternalId(flight.getId());
                                flight.setFootprintPosition(footprintPosition);
                                flight.setCalculationCategory("Carbon");
                                flightDao.createOrUpdate(flight);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.GeoLocatedCar")) {
                                Car car = mapper.readValue(onePositionString, Car.class);
                                car.setInternalId(car.getId());
                                car.setFootprintPosition(footprintPosition);
                                car.setCalculationCategory("Carbon");
                                carDao.createOrUpdate(car);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.Car")) {
                                Car car = mapper.readValue(onePositionString, Car.class);
                                car.setInternalId(car.getId());
                                car.setFootprintPosition(footprintPosition);
                                car.setCalculationCategory("Carbon");
                                carDao.createOrUpdate(car);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.FullyQualifiedCar")) {
                                Car car = mapper.readValue(onePositionString, Car.class);
                                car.setInternalId(car.getId());
                                car.setFootprintPosition(footprintPosition);
                                car.setCalculationCategory("Carbon");
                                carDao.createOrUpdate(car);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.GeoLocatedPublicTransport")) {
                                PublicTransport publicTransport = mapper.readValue(onePositionString, PublicTransport.class);
                                publicTransport.setInternalId(publicTransport.getId());
                                publicTransport.setFootprintPosition(footprintPosition);
                                publicTransport.setCalculationCategory("Carbon");
                                publicTransportDao.createOrUpdate(publicTransport);
                            }

                            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.PublicTransport")) {
                                PublicTransport publicTransport = mapper.readValue(onePositionString, PublicTransport.class);
                                publicTransport.setInternalId(publicTransport.getId());
                                publicTransport.setFootprintPosition(footprintPosition);
                                publicTransport.setCalculationCategory("Carbon");
                                publicTransportDao.createOrUpdate(publicTransport);
                            }
                        }

                        if (footprintPositionList.size() > 0) {
                            footprint.setFootprintPositions(footprintPositionList);
                            footprintDao.createOrUpdate(footprint);
                            footprints.add(footprint);
                        } else {
                            footprintDao.createOrUpdate(footprint);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return footprints;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            String message = "Aktualisiere CarbonFootprints";

            if ((pdia != null) && pdia.isShowing()) {
                pdia.setMessage(message);
            } else {
                pdia = new ProgressDialog(ctx);
                pdia.setMessage(message);
                pdia.show();
            }

            try {
                footprintPositionDao = helper.getFootprintPositionDao();
                airportPositionDao = helper.getAirportPositionDao();
                footprintDao = helper.getFootprintDao();
                flightDao = helper.getFlightsDao();
                carDao = helper.getCarDao();
                publicTransportDao = helper.getPublicTransportDao();
                employeeDao = helper.getEmployeeDao();
                geoLocationDao = helper.getGeoLocationDao();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        protected void onPostExecute(ArrayList<Footprint> footprints) {
            ListOverview overviewFrag = (ListOverview) act.getFragmentManager().findFragmentByTag("overviewFragment");

            if (updateCarData) {
                new GetEuro4Cars(true).execute();
            } else {
                pdia.dismiss();
            }

            ((MainActivity) act).checkAndAddEntryTab();
            overviewFrag.initializeList();

            if (!inetTest) {
                Toast.makeText(ctx, "Download fehlgeschlagen. Server nicht erreichbar!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetEuro4Cars extends AsyncTask<Void, Void, Boolean> {
        private boolean updateCarDataEuro5 = false;

        public GetEuro4Cars(boolean updateCarDataEuro5) {
            this.updateCarDataEuro5 = updateCarDataEuro5;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            String message = "Katalogaktualisierung (Euro 4 Fahrzeuge)";

            if ((pdia != null) && pdia.isShowing()) {
                pdia.setMessage(message);
            } else {
                pdia = new ProgressDialog(ctx);
                pdia.setMessage(message);
                pdia.show();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (updateCarDataEuro5) {
                new GetEuro5Cars(updateCarDataEuro5).execute();
            } else if ((pdia != null) && pdia.isShowing()) {
                pdia.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences prefs;
            String serverIp = null;
            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            if (!prefs.getString("ip", "").equalsIgnoreCase("")) {
                serverIp = prefs.getString("ip", "");
            } else {
                serverIp = "141.45.165.154";
            }

            //			http://chandan-tech.blogspot.in/2010/11/handling-soap-in-android.html
            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://" + serverIp + ":7000/PositionDataContext";
            final String METHOD_NAME = "GetCarDataEuro4";
            final String SOAP_ACTION = "http://tempuri.org/PositionDataContext/GetCarDataEuro4";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
            headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("root:ork123".getBytes())));

            try {
                androidHttpTransport.call(SOAP_ACTION, envelope, headerList);

                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject newDataSet = (SoapObject) resultsRequestSOAP.getProperty("GetCarDataEuro4Result");
                final Dao<CarData, Integer> carDataDao = helper.getCarDataDao();
                final ArrayList<CarData> carDataList = new ArrayList<CarData>();

                for (int i = 0; i < newDataSet.getPropertyCount(); i++) {
                    SoapObject oneCar = (SoapObject) newDataSet.getProperty(i);
                    CarData carData = new CarData(oneCar);
                    carDataList.add(carData);

                    //						carDataDao.createOrUpdate(carData);
                }

                carDataDao.callBatchTasks(new Callable<CarData>() {
                        @Override
                        public CarData call() throws Exception {
                            for (CarData carData : carDataList) {
                                carDataDao.createOrUpdate(carData);
                            }

                            return null;
                        }
                    });
            } catch (HttpResponseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return true;
        }
    }

    public class GetEuro5Cars extends AsyncTask<Void, Void, Boolean> {
        private boolean updateFootprints = false;

        public GetEuro5Cars(boolean updateCatalogs) {
            this.updateFootprints = updateCatalogs;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            String message = "Katalogaktualisierung (Euro 5 Fahrzeuge)";

            if ((pdia != null) && pdia.isShowing()) {
                pdia.setMessage(message);
            } else {
                pdia = new ProgressDialog(ctx);
                pdia.setMessage(message);
                pdia.show();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (updateFootprints) {
                new GetFootprints(false).execute();
            } else if ((pdia != null) && pdia.isShowing()) {
                pdia.dismiss();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences prefs;
            String serverIp = null;
            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            if (!prefs.getString("ip", "").equalsIgnoreCase("")) {
                serverIp = prefs.getString("ip", "");
            } else {
                serverIp = "141.45.165.154";
            }

            //			http://chandan-tech.blogspot.in/2010/11/handling-soap-in-android.html
            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://" + serverIp + ":7000/PositionDataContext";
            final String METHOD_NAME = "GetCarDataEuro5";
            final String SOAP_ACTION = "http://tempuri.org/PositionDataContext/GetCarDataEuro5";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
            headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("root:ork123".getBytes())));

            try {
                androidHttpTransport.call(SOAP_ACTION, envelope, headerList);

                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject newDataSet = (SoapObject) resultsRequestSOAP.getProperty("GetCarDataEuro5Result");
                final Dao<CarData, Integer> carDataDao = helper.getCarDataDao();
                final ArrayList<CarData> carDataList = new ArrayList<CarData>();

                for (int i = 0; i < newDataSet.getPropertyCount(); i++) {
                    SoapObject oneCar = (SoapObject) newDataSet.getProperty(i);
                    CarData carData = new CarData(oneCar);
                    carDataList.add(carData);
                }

                carDataDao.callBatchTasks(new Callable<CarData>() {
                        @Override
                        public CarData call() throws Exception {
                            for (CarData carData : carDataList) {
                                carDataDao.createOrUpdate(carData);
                            }

                            return null;
                        }
                    });
            } catch (Exception e) {
                System.out.println("******* THERE WAS AN ERROR ACCESSING THE WEB SERVICE");
                e.printStackTrace();
            }

            return true;
        }
    }

    public class GetAirports extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<Airport> airports = new ArrayList<Airport>();
        private boolean updateCatalogs = false;

        public GetAirports(boolean updateCatalogs) {
            this.updateCatalogs = updateCatalogs;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SharedPreferences prefs;
            String serverIp = null;
            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

            if (!prefs.getString("ip", "").equalsIgnoreCase("")) {
                serverIp = prefs.getString("ip", "");
            } else {
                serverIp = "141.45.165.154";
            }

            //			http://chandan-tech.blogspot.in/2010/11/handling-soap-in-android.html
            final String NAMESPACE = "http://tempuri.org/";
            final String URL = "http://" + serverIp + ":7000/PositionDataContext";
            final String METHOD_NAME = "GetAirportData";
            final String SOAP_ACTION = "http://tempuri.org/PositionDataContext/GetAirportData";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;

            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);

            List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
            headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode("root:ork123".getBytes())));

            try {
                androidHttpTransport.call(SOAP_ACTION, envelope, headerList);

                SoapObject resultsRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject newDataSet = (SoapObject) resultsRequestSOAP.getProperty("GetAirportDataResult");

                for (int i = 0; i < newDataSet.getPropertyCount(); i++) {
                    SoapObject oneAirport = (SoapObject) newDataSet.getProperty(i);

                    int airportId = Integer.parseInt(oneAirport.getProperty("id").toString());
                    String airportName = oneAirport.getProperty("name").toString();
                    String airportCountry = oneAirport.getProperty("iso_country").toString();

                    Double lat = Double.parseDouble(oneAirport.getProperty("latitude_deg").toString());
                    Double lng = Double.parseDouble(oneAirport.getProperty("longitude_deg").toString());
                    LatLng oneAirportCoords = new LatLng(lat, lng);
                    Airport airport = new Airport();
                    airport.setId(airportId);
                    airport.setName(airportName);
                    airport.setCountry(airportCountry);
                    airport.setLatitude(oneAirportCoords.latitude);
                    airport.setLongitude(oneAirportCoords.longitude);

                    if (oneAirport.getProperty("type").toString().equalsIgnoreCase("large_airport")) {
                        airport.setSize(2);                        
                    }
                    airports.add(airport);
                }
            } catch (Exception e) {
                System.out.println("******* THERE WAS AN ERROR ACCESSING THE WEB SERVICE");
                e.printStackTrace();
            }

            return true;
        }

        protected void onPreExecute() {
            super.onPreExecute();

            String message = "Katalogaktualisierung (Flughäfen)";

            if ((pdia != null) && pdia.isShowing()) {
                pdia.setMessage(message);
            } else {
                pdia = new ProgressDialog(ctx);
                pdia.setMessage(message);
                pdia.show();
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try {
                helper.getAirportDao().callBatchTasks(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            for (Airport airport : airports) {
                                helper.getAirportDao().createOrUpdate(airport);
                            }

                            return null;
                        }
                    });
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (updateCatalogs) {
                new GetEuro4Cars(updateCatalogs).execute();
            } else {
                pdia.dismiss();
            }
        }
    }
}
