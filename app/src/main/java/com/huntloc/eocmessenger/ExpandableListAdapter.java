package com.huntloc.eocmessenger;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by dmoran on 7/19/2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<Client>> listDataChild;
    public void setListDataHeader(List<String> listDataHeader) {
        this.listDataHeader = listDataHeader;
    }
    public void setListDataChild(HashMap<String, List<Client>> listDataChild) {
        this.listDataChild = listDataChild;
    }

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<Client>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
    }
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final Client childClient = (Client) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_list_row, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.client_name);
        if(childClient.isAdmin()){
            txtListChild.setText(childClient.getName()+"(Administrator)");
        }
        else{
            txtListChild.setText(childClient.getName());
        }
        ImageView received = (ImageView) convertView.findViewById(R.id.client_received);
        Log.d("getGroup",getGroup(groupPosition).toString());
        Log.d("childClient.getGroups",childClient.getGroups().toString());
        if (childClient.getGroups().get(getGroup(groupPosition))!=null && childClient.getGroups().get(getGroup(groupPosition))) {
            received.setVisibility(View.VISIBLE);
        } else {
            received.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_list_header, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.group_name);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
