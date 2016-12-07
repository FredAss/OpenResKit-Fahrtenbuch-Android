package buis.openreskit.fahrtenbuchapp;

import android.content.Context;

import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import buis.openreskit.odata.CarData;
import buis.openreskit.odata.DatabaseHelper;
import buis.openreskit.odata.Employee;
import buis.openreskit.odata.Footprint;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


public class Preferences extends PreferenceFragment {
    private DatabaseHelper helper;
    private Context ctx;
    private Dao<Employee, Integer> employeeDao;
    private Dao<Footprint, Integer> carbonFootprintDao;
    private Dao<CarData, Integer> carDataDao;
    private CharSequence[] mEmployee_names;
    private CharSequence[] mEmployee_ids;
    private CharSequence[] mCf_names;
    private CharSequence[] mCf_ids;
    private CharSequence[] mCarData_names;
    private CharSequence[] mCarData_ids;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        ctx = getActivity();
        helper = OpenHelperManager.getHelper(ctx, DatabaseHelper.class);

        try {
            employeeDao = helper.getEmployeeDao();
            carbonFootprintDao = helper.getFootprintDao();
            carDataDao = helper.getCarDataDao();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //get CarData
        List<String> car_data_names = new ArrayList<String>();
        List<String> car_data_ids = new ArrayList<String>();

        try {
            if (carDataDao.queryForAll().size() > 0) {
                for (CarData carData : carDataDao) {
                    car_data_names.add(carData.getManufactur() + " " + carData.getModel() + " " + carData.getDescription());
                    car_data_ids.add(String.valueOf(carData.getId()));
                }
            }

            mCarData_names = car_data_names.toArray(new CharSequence[car_data_names.size()]);
            mCarData_ids = car_data_ids.toArray(new CharSequence[car_data_ids.size()]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ListPreference listPref = new UpdatingListPref(ctx);
        listPref.setKey("cartype"); //Refer to get the pref value
        listPref.setEntries(mCarData_names);
        listPref.setEntryValues(mCarData_ids);
        listPref.setDialogTitle("Bitte wählen sie einen Fahrzeugtyp");
        listPref.setTitle("Fahrzeugtyp");
        listPref.setSummary("%s");

        ((PreferenceScreen) findPreference("PREFSMAIN")).addPreference(listPref);

        //get Employees
        List<String> emp_names = new ArrayList<String>();
        List<String> emp_ids = new ArrayList<String>();

        try {
            if (employeeDao.queryForAll().size() > 0) {
                for (Employee e : employeeDao) {
                    emp_names.add(e.getFirstName() + ", " + e.getLastName());
                    emp_ids.add(String.valueOf(e.getId()));
                }
            }

            mEmployee_names = emp_names.toArray(new CharSequence[emp_names.size()]);
            mEmployee_ids = emp_ids.toArray(new CharSequence[emp_ids.size()]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //default employee
        listPref = new UpdatingListPref(ctx);
        listPref.setKey("employee"); //Refer to get the pref value
        listPref.setEntries(mEmployee_names);
        listPref.setEntryValues(mEmployee_ids);
        listPref.setDialogTitle("Bitte wählen sie einen Mitarbeiter");
        listPref.setTitle("Mitarbeiter");
        listPref.setSummary("%s");

        ((PreferenceScreen) findPreference("PREFSMAIN")).addPreference(listPref);

        //get Carbonfootprints
        List<String> cf_names = new ArrayList<String>();
        List<String> cf_ids = new ArrayList<String>();

        try {
            if (carbonFootprintDao.queryForAll().size() > 0) {
                for (Footprint f : carbonFootprintDao) {
                    cf_names.add(f.getName());
                    cf_ids.add(String.valueOf(f.getId()));
                }
            }

            mCf_names = cf_names.toArray(new CharSequence[cf_names.size()]);
            mCf_ids = cf_ids.toArray(new CharSequence[cf_ids.size()]);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //default employee
        ListPreference cfPref = new UpdatingListPref(ctx);
        cfPref.setKey("carbonfootprint"); //Refer to get the pref value
        cfPref.setEntries(mCf_names);
        cfPref.setEntryValues(mCf_ids);
        cfPref.setDialogTitle("Bitte wählen sie einen Carbon Footprint");
        cfPref.setTitle("Carbon Footprint");
        cfPref.setSummary("%s");

        ((PreferenceScreen) findPreference("PREFSMAIN")).addPreference(cfPref);
    }
}
