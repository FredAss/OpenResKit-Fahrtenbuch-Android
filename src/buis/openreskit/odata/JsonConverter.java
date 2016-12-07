package buis.openreskit.odata;

import com.j256.ormlite.dao.Dao;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;


public class JsonConverter {
    /**
     * Funktion zum Umwandeln der in der SQLite DB gespeicherten Objekte in JSON Arrays.
     * Dazu werden zunächste die Footprints umgewandelt und anschließend die dazugehörenden Positionen
     * je nach Typ ebenfalls umgewandelt und in das JSON Objekt des zugehörigen Footprints integriert.
     * @param footprint
     * @param helper
     * @param serverIp
     * @return footprintObject
     */
    public static JSONObject convertFootprintToJson(Footprint footprint, DatabaseHelper helper, String serverIp) {
        JSONArray footprintPositionData = new JSONArray();
        JSONObject footprintObject = null;

        footprintObject = new JSONObject();

        try {
            footprintObject.put("odata.type", "OpenResKit.DomainModel.CarbonFootprint");
            footprintObject.put("Id", footprint.getInternalId());
            footprintObject.put("Name", footprint.getName());
            footprintObject.put("Description", footprint.getDescription());
            footprintObject.put("SiteLocation", footprint.getSiteLocation());
            footprintObject.put("Employees", footprint.getEmployees());
            footprintObject.put("Calculation", footprint.getCalculation());

            for (FootprintPosition footprintPosition : footprint.getFootprintPositions()) {
                JSONObject positionNavProp = new JSONObject();
                positionNavProp.put("uri", "http://" + serverIp + ":7000/OpenResKitHub/CarbonFootprintPositions(" + footprintPosition.getId() + ")");

                JSONObject positionNavPropMetadata = new JSONObject();
                positionNavPropMetadata.put("__metadata", positionNavProp);
                footprintPositionData.put(positionNavPropMetadata);
            }

            footprintObject.put("Positions", footprintPositionData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return footprintObject;
    }

    public static JSONObject convertPositionToJson(FootprintPosition footprintPosition, DatabaseHelper helper, Boolean addMetadata, String serverIp) {
        Dao<Flight, Integer> flightDao;
        Dao<Car, Integer> carDao;
        Dao<PublicTransport, Integer> publicTransportDao;
        JSONObject footprintPositionObject = null;

        try {
            footprintPositionObject = new JSONObject();

            LocalDateTime dateTimeFormat = new LocalDateTime();
            @SuppressWarnings("static-access")
            LocalDateTime date = dateTimeFormat.parse(footprintPosition.getDate());

            footprintPositionObject.put("odata.type", footprintPosition.getPositionType());

            if (addMetadata) {
                JSONObject measureNavProp = new JSONObject();
                measureNavProp.put("type", footprintPosition.getPositionType());
                footprintPositionObject.put("__metadata", measureNavProp);
            }

            if (date != null) {
                footprintPositionObject.put("Start", footprintPosition.getDate());
            } else {
                footprintPositionObject.put("Start", new DateTime());
            }

            if (date != null) {
                footprintPositionObject.put("Finish", footprintPosition.getDate());
            } else {
                footprintPositionObject.put("Finish", new DateTime());
            }

            footprintPositionObject.put("Description", footprintPosition.getDescription());
            footprintPositionObject.put("IconId", footprintPosition.getIconId());
            footprintPositionObject.put("Name", footprintPosition.getName());
            footprintPositionObject.put("Tag", footprintPosition.getCarbonFootprintCategoryId());
            footprintPositionObject.put("Calculation", footprintPosition.getCalculation());

            JSONObject respSubjNavProp = new JSONObject();
            respSubjNavProp.put("uri", "http://" + serverIp + ":7000/OpenResKitHub/ResponsibleSubjects(" + footprintPosition.getResponsibleSubject().getId() + ")");

            JSONObject respSubjNavPropMetadata = new JSONObject();
            respSubjNavPropMetadata.put("__metadata", respSubjNavProp);
            footprintPositionObject.put("ResponsibleSubject", respSubjNavPropMetadata);

            if (!footprintPosition.getPositionType().equalsIgnoreCase("OpenResKit.DomainModel.Flight") && !footprintPosition.getPositionType().equalsIgnoreCase("OpenResKit.DomainModel.AirportBasedFlight")) {
                footprintPositionObject.put("StartName", footprintPosition.getStartAddress());
                footprintPositionObject.put("StartLatitude", footprintPosition.getStartLat());
                footprintPositionObject.put("StartLongitude", footprintPosition.getStartLng());
                footprintPositionObject.put("DestinationName", footprintPosition.getEndAddress());
                footprintPositionObject.put("DestinationLatitude", footprintPosition.getEndLat());
                footprintPositionObject.put("DestinationLongitude", footprintPosition.getEndLng());
            }

            JSONArray geoLocationsData = new JSONArray();

            for (GeoLocation geoLocation : footprintPosition.getGeoLocations()) {
                JSONObject positionNavProp = new JSONObject();
                positionNavProp.put("uri", "http://" + serverIp + ":7000/OpenResKitHub/GeoLocations(" + geoLocation.getId() + ")");

                JSONObject positionNavPropMetadata = new JSONObject();
                positionNavPropMetadata.put("__metadata", positionNavProp);
                geoLocationsData.put(positionNavPropMetadata);
            }

            footprintPositionObject.put("GeoLocations", geoLocationsData);            
            
            
            String positionType = footprintPosition.getPositionType();

            //ToDo: GeoLocatedFlight
            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.AirportBasedFlight")) {
                flightDao = helper.getFlightsDao();

                for (Flight flight : flightDao) {
                    if (flight.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                        JSONArray airportPositionData = new JSONArray();

                        for (AirportPosition airportPosition : flight.getAirportPositions()) {
                            JSONObject positionNavProp = new JSONObject();
                            positionNavProp.put("uri", "http://" + serverIp + ":7000/OpenResKitHub/AirportPositions(" + airportPosition.getId() + ")");

                            JSONObject positionNavPropMetadata = new JSONObject();
                            positionNavPropMetadata.put("__metadata", positionNavProp);
                            airportPositionData.put(positionNavPropMetadata);
                        }

                        footprintPositionObject.put("Airports", airportPositionData);
                    }
                }
            }

            //ToDo: GeoLocatedCar
            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.GeoLocatedCar")) {
                carDao = helper.getCarDao();

                for (Car car : carDao) {
                    if (car.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                        footprintPositionObject.put("Consumption", car.getConsumption());
                        footprintPositionObject.put("CarbonProduction", car.getCarbonProduction());
                        footprintPositionObject.put("Kilometrage", (int) Math.round(car.getDistance()));
                        footprintPositionObject.put("m_Fuel", car.getFuel());
                        footprintPositionObject.put("CarId", car.getCarId());
                    }
                }
            }

            //ToDo: GeoLocatedPublicTransport
            if (positionType.equalsIgnoreCase("OpenResKit.DomainModel.GeoLocatedPublicTransport")) {
                publicTransportDao = helper.getPublicTransportDao();

                for (PublicTransport publicTransport : publicTransportDao) {
                    if (publicTransport.getFootprintPosition().getInternalId() == footprintPosition.getInternalId()) {
                        footprintPositionObject.put("Kilometrage", (int) Math.round(publicTransport.getDistance()));
                        footprintPositionObject.put("m_TransportType", publicTransport.getTransportType());
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return footprintPositionObject;
    }

    public static JSONObject convertAirportPositionToJson(AirportPosition airportPosition, DatabaseHelper helper) {
        JSONObject airportPositionJsonObject = null;

        airportPositionJsonObject = new JSONObject();

        try {
            airportPositionJsonObject.put("odata.type", "OpenResKit.DomainModel.AirportPosition");
            airportPositionJsonObject.put("Id", airportPosition.getInternalId());
            airportPositionJsonObject.put("AirportID", airportPosition.getAirportId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return airportPositionJsonObject;
    }
    
    public static JSONObject convertGeoLocationToJson(GeoLocation geoLocation, DatabaseHelper helper) {
        JSONObject geoLocationJsonObject = null;

        geoLocationJsonObject = new JSONObject();

        try {
            geoLocationJsonObject.put("odata.type", "OpenResKit.DomainModel.GeoLocation");
            geoLocationJsonObject.put("Id", geoLocation.getInternalId());
            geoLocationJsonObject.put("Latitude", geoLocation.getLatitude());
            geoLocationJsonObject.put("Longitude", geoLocation.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return geoLocationJsonObject;
    }    
    
}
