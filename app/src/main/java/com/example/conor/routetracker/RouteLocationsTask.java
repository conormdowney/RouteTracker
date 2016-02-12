package com.example.conor.routetracker;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ListView;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Conor on 2016-01-09.
 */
public class RouteLocationsTask extends AsyncTask<Void, Void, ArrayList<RouteItem>>{
    private RouteListAdapter adapter;
    private ArrayList<RouteItem> itemsList;
    private ArrayList<String> routeFiles;
    private Context context;
    private ListView listView;

    public RouteLocationsTask(ListView listView, RouteListAdapter adapter, ArrayList<String> routeFiles, Context context)
    {
        this.adapter = adapter;
        itemsList = new ArrayList<RouteItem>();
        this.routeFiles = routeFiles;
        this.context = context;
        this.listView = listView;
    }

    @Override
    protected ArrayList<RouteItem> doInBackground(Void... params) {
        for (int i = 0; i < routeFiles.size(); i++) {

            File loadFile = new File(Environment.getExternalStorageDirectory()+"/LocationTracker/Routes/"+routeFiles.get(i));
            try {
                BufferedReader br = new BufferedReader(new FileReader(loadFile));

                String firstEntry = br.readLine();
                GeoPoint startPt = getGeoPointFromString(firstEntry);

                String lastEntry = tail(loadFile);
                GeoPoint endPt = getGeoPointFromString(lastEntry);

                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses;

                addresses = geocoder.getFromLocation(startPt.getLatitude(), startPt.getLongitude(), 1);
                String startPtStr = addresses.get(0).getAddressLine(0);

                addresses = geocoder.getFromLocation(endPt.getLatitude(), endPt.getLongitude(), 1);
                String endPtStr = addresses.get(0).getAddressLine(0);

                String completeDistanceStr = lastEntry.substring(lastEntry.lastIndexOf(":") + 1);
                double distance = Double.parseDouble(completeDistanceStr);

                String formattedDistance = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH) ).format(distance) + " km";

                itemsList.add(new RouteItem(startPtStr, endPtStr, formattedDistance, loadFile.getAbsolutePath()));
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return itemsList;
    }

    public void onPostExecute(ArrayList<RouteItem> itemsList)
    {
        super.onPostExecute(itemsList);

        adapter.setItemList(itemsList);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

     public GeoPoint getGeoPointFromString(String coordinates)
    {
        String loadedLatStr = coordinates.substring(coordinates.indexOf("(") + 1, coordinates.indexOf(","));
        String loadedLongStr = coordinates.substring(coordinates.indexOf(",") + 1, coordinates.lastIndexOf(","));

        double loadedLat = Double.parseDouble(loadedLatStr);
        double loadedLong = Double.parseDouble(loadedLongStr);

        return new GeoPoint(loadedLat, loadedLong);
    }

    public String tail( File file ) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for(long filePointer = fileLength; filePointer != -1; filePointer--){
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    }
                    break;

                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    }
                    break;
                }

                sb.append( ( char ) readByte );
            }

            return sb.reverse().toString();
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null )
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
