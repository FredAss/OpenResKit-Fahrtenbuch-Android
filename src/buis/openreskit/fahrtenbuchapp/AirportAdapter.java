package buis.openreskit.fahrtenbuchapp;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import buis.openreskit.odata.Airport;


public class AirportAdapter extends ArrayAdapter<Airport> {
    private Context context;

    public AirportAdapter(Context context, int textViewResourceId, Airport[] airports) {
        super(context, textViewResourceId, airports);
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
        text.setText(getItem(position).getName());

        return view;
    }
}
