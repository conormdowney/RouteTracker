package com.example.conor.routetracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Conor on 2016-01-04.
 */
public class RouteList extends AppCompatActivity implements AbsListView.MultiChoiceModeListener, AbsListView.OnItemClickListener {
    ArrayList<RouteItem> itemsList;
    ArrayList<String> routeFiles;
    ArrayList<RouteItem> selected;
    RouteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);

        itemsList = new ArrayList<RouteItem>();
        selected = new ArrayList<RouteItem>();

        adapter = new RouteListAdapter(this, R.layout.route_list_item);
        ListView listView = (ListView) findViewById(R.id.routeList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        listView.setOnItemClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Routes");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        getRouteFiles();
        RouteLocationsTask task = new RouteLocationsTask(listView, adapter, routeFiles, getApplicationContext());
        task.execute();
    }

    public void getRouteFiles()
    {
        String path = Environment.getExternalStorageDirectory()+"/LocationTracker/Routes/";

        // Read all files sorted into the values-array
        routeFiles = new ArrayList();
        File dir = new File(path);

        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    routeFiles.add(file);
                }
            }
        }
        Collections.sort(routeFiles);
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        int x = adapter.getCount();
        //RouteItem routeItem = adapter.getItem(position);
        if(checked)
            selected.add(adapter.getItem(position));
        else
            selected.remove(adapter.getItem(position));

        mode.setTitle(String.valueOf(selected.size()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.listview_cab_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch(item.getItemId()){
            case R.id.delete:
                itemsList = adapter.getItemList();
                itemsList.removeAll(selected);
                adapter.notifyDataSetChanged();

                deleteSelectedFiles();

                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selected.clear();
    }

    public void deleteSelectedFiles()
    {
        for(RouteItem routeItem : selected)
        {
            File file = new File(routeItem.getFileName());
            file.delete();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Intent mapIntent = new Intent(this, MainActivity.class);
        Intent mapIntent = new Intent();
        RouteItem routeItem = adapter.getItem(position);
        String fileName = String.valueOf(routeItem.getFileName());
        mapIntent.putExtra("path", fileName);
        //startActivity(mapIntent);
        setResult(RESULT_OK, mapIntent);
        finish();
    }
}
