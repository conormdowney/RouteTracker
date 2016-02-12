package com.example.conor.routetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Conor on 2016-01-07.
 */
public class RouteListAdapter extends ArrayAdapter<RouteItem> {

    ArrayList<RouteItem> itemsList;
    ArrayList<String> routeFiles;

    public RouteListAdapter(Context context, int resource) {
        super(context, resource);

        itemsList = new ArrayList<RouteItem>();
    }

    public int getCount()
    {
        return itemsList.size();
    }

    public void setItemList(ArrayList<RouteItem> routes)
    {
        itemsList = routes;
    }

    public ArrayList getItemList()
    {
        return itemsList;
    }

    public RouteItem getItem(int position)
    {
        return itemsList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        RouteItem item = itemsList.get(position);
        ItemViewHolder viewHolder;

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_list_item, parent, false);
            viewHolder = new ItemViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ItemViewHolder) convertView.getTag();

        viewHolder.startPtView.setText(itemsList.get(position).getStartPoint());

        viewHolder.endPtView.setText(itemsList.get(position).getEndPoint());

        viewHolder.distance.setText(String.valueOf(item.getDistance()));

        viewHolder.fileName.setText(itemsList.get(position).getFileName());

        return convertView;
    }

    private static class ItemViewHolder
    {
        public TextView startPtView, endPtView, distance, fileName;

        public ItemViewHolder(View listItem)
        {
            startPtView = (TextView) listItem.findViewById(R.id.startLocation);
            endPtView = (TextView) listItem.findViewById(R.id.endLocation);
            distance = (TextView) listItem.findViewById(R.id.distance);
            fileName = (TextView) listItem.findViewById(R.id.fileName);
        }
    }
}
