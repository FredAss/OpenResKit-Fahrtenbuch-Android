package buis.openreskit.fahrtenbuchapp;

import android.app.Activity;

import android.os.Bundle;


public class SetPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new Preferences()).commit();
    }
}
