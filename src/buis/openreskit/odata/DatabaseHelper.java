package buis.openreskit.odata;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;


/**
 * Klasse zur Regelung des Zugriff auf die SQLite-Datenbank des Smartphones
 * mittels ORMLite
 *
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "footprint.db";
    private static final int DATABASE_VERSION = 104;
    private Dao<Footprint, Integer> footprintDao;
    private Dao<FootprintPosition, Integer> footprintPositionDao;
    private Dao<AirportPosition, Integer> airportPositionDao;
    private Dao<Flight, Integer> flightsDao;
    private Dao<Car, Integer> carsDao;
    private Dao<PublicTransport, Integer> publicTransportDao;
    private Dao<EnergyConsumption, Integer> energyConsumptionDao;
    private Dao<Airport, Integer> airportDao;
    private Dao<Employee, Integer> employeeDao;
    private Dao<CarData, Integer> carDataDao;
    private Dao<GeoLocation, Integer> geoLocationDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Erstellt eine Tabelle vom Typ der Datenklasse.
     */
    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) { // Create method for ORM
                                                                                             // database tables which is
                                                                                             // called when
                                                                                             // DatabaseHelper is started

        try {
            TableUtils.createTable(connectionSource, Footprint.class);
            TableUtils.createTable(connectionSource, FootprintPosition.class);
            TableUtils.createTable(connectionSource, AirportPosition.class);
            TableUtils.createTable(connectionSource, Flight.class);
            TableUtils.createTable(connectionSource, Car.class);
            TableUtils.createTable(connectionSource, EnergyConsumption.class);
            TableUtils.createTable(connectionSource, PublicTransport.class);
            TableUtils.createTable(connectionSource, Airport.class);
            TableUtils.createTable(connectionSource, Employee.class);
            TableUtils.createTable(connectionSource, CarData.class);
            TableUtils.createTable(connectionSource, GeoLocation.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create database", e);
        }
    }

    /**
     * Löscht die Tabelle einer bestimmten Datenklasse.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            TableUtils.dropTable(connectionSource, Footprint.class, true);
            TableUtils.dropTable(connectionSource, FootprintPosition.class, true);
            TableUtils.dropTable(connectionSource, AirportPosition.class, true);
            TableUtils.dropTable(connectionSource, Flight.class, true);
            TableUtils.dropTable(connectionSource, Car.class, true);
            TableUtils.dropTable(connectionSource, EnergyConsumption.class, true);
            TableUtils.dropTable(connectionSource, PublicTransport.class, true);
            TableUtils.dropTable(connectionSource, Airport.class, true);
            TableUtils.dropTable(connectionSource, Employee.class, true);
            TableUtils.dropTable(connectionSource, CarData.class, true);
            TableUtils.dropTable(connectionSource, GeoLocation.class, true);

            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
        }
    }

    /**
     * Liefert alle Footprints im DAO Format.
     *
     * @return footprintDao
     * @throws SQLException
     */
    public Dao<Footprint, Integer> getFootprintDao() throws SQLException {
        if (footprintDao == null) {
            footprintDao = getDao(Footprint.class);
        }

        return footprintDao;
    }
    
    public Dao<GeoLocation, Integer> getGeoLocationDao() throws SQLException {
        if (geoLocationDao == null) {
            geoLocationDao = getDao(GeoLocation.class);
        }

        return geoLocationDao;
    }    

    /**
     * Liefert alle Footprint-Positionen im DAO Format.
     *
     * @return footprintPositionDao
     * @throws SQLException
     */
    public Dao<FootprintPosition, Integer> getFootprintPositionDao() throws SQLException {
        if (footprintPositionDao == null) {
            footprintPositionDao = getDao(FootprintPosition.class);
        }

        return footprintPositionDao;
    }

    /**
     * Liefert alle Flüge im DAO Format.
     *
     * @return flightsDao
     * @throws SQLException
     */
    public Dao<Flight, Integer> getFlightsDao() throws SQLException {
        if (flightsDao == null) {
            flightsDao = getDao(Flight.class);
        }

        return flightsDao;
    }

    /**
     * Liefert alle AirportPosition im DAO Format.
     *
     * @return airportPositionDao
     * @throws SQLException
     */
    public Dao<AirportPosition, Integer> getAirportPositionDao() throws SQLException {
        if (airportPositionDao == null) {
            airportPositionDao = getDao(AirportPosition.class);
        }

        return airportPositionDao;
    }

    /**
     * Liefert alle Autofahrten im DAO Format.
     *
     * @return carsDao
     * @throws SQLException
     */
    public Dao<Car, Integer> getCarDao() throws SQLException {
        if (carsDao == null) {
            carsDao = getDao(Car.class);
        }

        return carsDao;
    }

    /**
     * Liefert alle Energieverbräuche im DAO Format.
     *
     * @return energyConsumptionDao
     * @throws SQLException
     */
    public Dao<EnergyConsumption, Integer> getEnergyConsumptionDao() throws SQLException {
        if (energyConsumptionDao == null) {
            energyConsumptionDao = getDao(EnergyConsumption.class);
        }

        return energyConsumptionDao;
    }

    /**
     * Liefert alle Fahrten in öffentlichen Verkehrsmitteln im DAO Format.
     *
     * @return publicTransportDao
     * @throws SQLException
     */
    public Dao<PublicTransport, Integer> getPublicTransportDao() throws SQLException {
        if (publicTransportDao == null) {
            publicTransportDao = getDao(PublicTransport.class);
        }

        return publicTransportDao;
    }

    public Dao<Airport, Integer> getAirportDao() throws SQLException {
        if (airportDao == null) {
            airportDao = getDao(Airport.class);
        }

        return airportDao;
    }

    public Dao<Employee, Integer> getEmployeeDao() throws SQLException {
        if (employeeDao == null) {
            employeeDao = getDao(Employee.class);
        }

        return employeeDao;
    }

    public Dao<CarData, Integer> getCarDataDao() throws SQLException {
        if (carDataDao == null) {
            carDataDao = getDao(CarData.class);
        }

        return carDataDao;
    }

    @SuppressWarnings("deprecation")
    public int getNewPositionId() {
        try {
            QueryBuilder<FootprintPosition, Integer> qBuilder = footprintPositionDao.queryBuilder();
            qBuilder.orderBy("internalId", false);
            qBuilder.limit(1);

            FootprintPosition lastFootprintPosition = getFootprintPositionDao().queryForFirst(qBuilder.prepare());

            if (lastFootprintPosition != null) {
                return lastFootprintPosition.getInternalId() + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
