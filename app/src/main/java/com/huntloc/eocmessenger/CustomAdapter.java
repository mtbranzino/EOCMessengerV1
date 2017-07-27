package com.huntloc.eocmessenger;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    Context context;
    private ArrayList<HashMap<String, String>> list;
    private HashMap<String,Boolean> selectedGroups;
    public CustomAdapter(MainActivity activity, ArrayList<HashMap<String, String>> list, HashMap<String,Boolean> selectedGroups) {
        this.list = list;
        this.selectedGroups = selectedGroups;
        context = activity;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int position) {
        Object toReturn = null;
        try {
            toReturn = list.get(position);
        } catch (Exception e) {
        }
        return toReturn;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    public View getView(final int position, View row, ViewGroup parent) {

        View rowView;
        rowView = inflater.inflate(R.layout.settings_group_list_row, null);
        TextView name = (TextView) rowView.findViewById(R.id.group_name);
        CheckBox checkBox = (CheckBox) rowView.findViewById(R.id.group_checkBox);
        try {
            name.setText(list.get(position).get("name"));
            if (selectedGroups.get(list.get(position).get("name"))!=null) {
                checkBox.setChecked(true);
            }
        } catch (Exception e) {
            Log.d("Exception",e.toString());
        }

        return rowView;
    }
}
