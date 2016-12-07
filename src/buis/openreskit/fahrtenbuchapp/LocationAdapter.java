package buis.openreskit.fahrtenbuchapp;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocationAdapter extends ArrayAdapter<Location>{

    private Context context;

    public LocationAdapter(Context context, int textViewResourceId, ArrayList<Location> locations) {
        super(context, textViewResourceId, locations);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;

        if (convertView == null) {
            view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        } else {
            view = convertView;
        }

        text = (TextView) view;
        text.setText(getItem(position).getLatitude() + " - " + getItem(position).getLongitude());

        return view;
    }	
	
}
