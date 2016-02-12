package com.example.conor.routetracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MapView map;
    FloatingActionButton startFab;
    FloatingActionButton endFab;
    IMapController mapController;
    ArrayList<GeoPoint> waypoints;
    ArrayList<GeoPoint> loadedWaypoints;
    ArrayList<Long> timelapseList;
    ArrayList<GeoPoint> loadedRoadPts;
    ArrayList<GeoPoint> loadedRoute;
    int loadedRoadCounter;
    RoadManager roadManager;
    File file;
    GeoPoint firstPt;
    int timeLapseIndex = 0;

    boolean timerStarted = false;
    long startTime = 0;
    ArrayList<Long> loadedTimeLapses;
    boolean firstTime = true;
    boolean loadedFile;
    String loadedPath = "";

    static final int LOCATION_CHANGED = 3;

    Messenger mServiceMessenger = null;
    /** Messenger for receiving messages from the service. ddf*/
    Messenger mClientMessenger = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    double currentLat = 0.0;
    double currentLon = 0.0;

    Utils utils;
    ProgressDialog bindDialog;

    boolean applyToRoute = false;
    boolean gpsEnabled = false;
    boolean routeStarted = false;

    Marker currentPos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = Utils.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Route Tracker");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        startFab = (FloatingActionButton) findViewById(R.id.startFab);
        startFab.setOnClickListener(this);

        endFab = (FloatingActionButton) findViewById(R.id.endFab);
        endFab.setOnClickListener(this);

        waypoints = new ArrayList<GeoPoint>();
        loadedWaypoints = new ArrayList<GeoPoint>();
        loadedRoadPts = new ArrayList<GeoPoint>();
        loadedTimeLapses = new ArrayList<Long>();
        loadedRoute = new ArrayList<GeoPoint>();
        timelapseList = new ArrayList<Long>();
        firstPt = new GeoPoint(0,0);
        roadManager = new OSRMRoadManager();
        loadedRoadCounter = 0;
        bindDialog = new ProgressDialog(this);
        createMap();
        //createRouteFile();  test

        currentPos = new Marker(map);

        /**
         * Handler thread to avoid running on the main thread (UI)
         */
        HandlerThread handlerThread = new HandlerThread("GPSServiceThread");
        handlerThread.start();
        IncomingHandler handler = new IncomingHandler(handlerThread);
        mClientMessenger = new Messenger(handler);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.startFab:
                checkLocationSettings();
                if(gpsEnabled) {
                    //if(!mBound)
                        bindService();
                    bindDialog.setMessage("Getting location");
                    bindDialog.setCancelable(true);
                    bindDialog.setInverseBackgroundForced(false);
                    bindDialog.create();
                    bindDialog.show();
                    routeStarted = true;
                }
                break;
            case R.id.endFab:
                stopRouteDialog();
                break;
            default:
                Toast.makeText(MainActivity.this, "Something else", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRoute()
    {
        startFab.setVisibility(View.GONE);
        waypoints.clear();
        utils.createFile();
        timerStarted = false;
        applyToRoute = true;
        endFab.setVisibility(View.VISIBLE);
        utils.createFile(); //previously done in onCreate
    }

    private void saveFileDialog()
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Do you want to save?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent saveRouteIntent = new Intent(getApplicationContext(), SaveRouteActivity.class);

                        calcDistance(utils.getFilePath());
                        saveRouteIntent.putExtra("routeFile", utils.getFilePath());
                        startActivityForResult(saveRouteIntent, 2);
                        //startActivity(saveRouteIntent);
                    }
                }
        );

        dialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                }
        );

        AlertDialog saveDialog = dialogBuilder.create();
        saveDialog.show();
    }

    private void stopRouteDialog()
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Do you want to stop?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopService(new Intent(getBaseContext(), GPSService.class));
                        unbindService();
                        endFab.setVisibility(View.GONE);
                        startFab.setVisibility(View.VISIBLE);
                        boolean check = utils.isMyServiceRunning(GPSService.class);
                        applyToRoute = false;
                        routeStarted = false;
                        firstTime = true;
                        saveFileDialog();
                    }
                }
        );

        dialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                }
        );

        AlertDialog saveDialog = dialogBuilder.create();
        saveDialog.show();
    }

    class IncomingHandler extends Handler {

        public IncomingHandler(HandlerThread thr) {
            super(thr.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;

                case LOCATION_CHANGED:
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (routeStarted)
                                startRoute();
                        }
                    });
                    Bundle bundle = msg.getData();
                    double lat = Double.parseDouble(bundle.getString("latitude"));
                    double lon = Double.parseDouble(bundle.getString("longitude"));
                    final GeoPoint geoPt = new GeoPoint(lat, lon);

                    currentLat = lat;
                    currentLon = lon;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(applyToRoute) {
                                drawRoad(geoPt);
                                utils.writeRouteToFile(geoPt);
                                map.getOverlays().remove(currentPos);
                                currentPos.setPosition(geoPt);
                                currentPos.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                map.getOverlays().add(currentPos);
                            }

                            if(bindDialog.isShowing())
                                bindDialog.dismiss();
                        }
                    });

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mServiceMessenger = new Messenger(service);

            // Now that we have the service messenger, lets send our messenger
            Message msg = Message.obtain(null, LOCATION_CHANGED, 0, 0);
            msg.replyTo = mClientMessenger;

            /*
             * In case we would want to send extra data, we could use Bundles:
             * Bundle b = new Bundle(); b.putString("key", "hello world");
             * msg.setData(b);
             */

            try {
                mServiceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mServiceMessenger = null;
            mBound = false;
        }
    };

    public boolean bindService() {
        Intent i = new Intent(getApplicationContext(), GPSService.class);
        return bindService(i, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void drawRoad(GeoPoint currentPt)
    {
        long timeLapse;

        if(!timerStarted)
        {
            startTime = SystemClock.elapsedRealtime();
            timerStarted = true;
        }

        timeLapse = SystemClock.elapsedRealtime() - startTime;

        if(firstTime)
        {
            Marker startMarker = new Marker(map);
            startMarker.setPosition(currentPt);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(startMarker);
            startMarker.setTitle("Start point");
            firstTime = false;
        }

        mapController.setCenter(currentPt);

        waypoints.add(currentPt);

        Road road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
        map.getOverlays().add(roadOverlay);

        if(loadedFile)
        {
            while(timeLapse >= loadedTimeLapses.get(timeLapseIndex))
            {
                loadedRoute.add(loadedWaypoints.get(timeLapseIndex));
                timeLapseIndex++;
            }
        }

        Road loadedRoad = roadManager.getRoad(loadedRoute);
        Polyline loadedRoadOverlay = RoadManager.buildRoadOverlay(loadedRoad, this);
        loadedRoadOverlay.setColor(Color.RED);
        map.getOverlays().add(loadedRoadOverlay);

        //map.invalidate();
    }

    public void onDestroy()
    {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), GPSService.class));

        unbindService();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Exiting will cancel your current route. Are you sure you want to exit?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                }
        );

        dialogBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }
        );

        AlertDialog exitDialog = dialogBuilder.create();
        exitDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.screenshot)
        {
            utils.screenShot(getWindow());
        }
        else if(id == R.id.listFiles)
        {
            startActivityForResult(new Intent(getApplicationContext(), RouteList.class), 1);
        }
        else if(id == R.id.weatherCheck)
        {
            Intent weatherIntent = new Intent(this, WeatherList.class);
            if(loadedFile) {
                weatherIntent.putExtra("lat", firstPt.getLatitude());
                weatherIntent.putExtra("lon", firstPt.getLongitude());
            }
            else
            {
                weatherIntent.putExtra("lat", currentLat);
                weatherIntent.putExtra("lon", currentLon);
            }

            startActivity(weatherIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkLocationSettings()
    {
        LocationManager locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        gpsEnabled = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!gpsEnabled)
        {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage("You need to enable your GPS");

            dialogBuilder.setPositiveButton(
                    "Enable",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent gpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsIntent);
                        }
                    });

            dialogBuilder.setNegativeButton(
                    "Leave",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                }
            );

            AlertDialog gpsDialog = dialogBuilder.create();
            gpsDialog.show();
        }
    }

    public void onResume()
    {
        super.onResume();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                loadedPath = path;
                if (path == null)
                    loadedFile = false;
                else
                    loadedFile = true;

                if (loadedFile) {
                    map.getOverlays().clear();
                    loadedWaypoints = utils.loadWaypointsFromFile(path);
                    loadedTimeLapses = utils.loadTimeLapsesFromFile(path);
                    firstPt = utils.getFirstPoint(path);
                    drawLoadedRoute(firstPt);
                }
            }
        }
        else if(requestCode == 2)
        {
            map.getOverlays().clear();
        }
    }

    public void drawLoadedRoute(GeoPoint currentPt)
    {
        Marker startMarker = new Marker(map);
        startMarker.setPosition(currentPt);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(startMarker);
        startMarker.setTitle("Start point");

        mapController.setCenter(currentPt);

        Road road = roadManager.getRoad(loadedWaypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road, this);
        roadOverlay.setColor(Color.GREEN);
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }

    public void createMap()
    {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController = map.getController();
        mapController.setZoom(15);
    }

    public void calcDistance(String path)
    {
        //Utils utils = new Utils(this);
        //ArrayList<GeoPoint> wayPts = utils.loadWaypointsFromFile(loadedPath);
        ArrayList<GeoPoint> wayPts = utils.loadWaypointsFromFile(path);
        Location location = new Location("");
        float [] results = new float[wayPts.size()];
        float distance = 0;
        for(int i = 1; i < wayPts.size(); i++)
        {
            location.distanceBetween(wayPts.get(i-1).getLatitude(), wayPts.get(i-1).getLongitude(),
                    wayPts.get(i).getLatitude(), wayPts.get(i).getLongitude(), results);
            distance += results[0];
        }

        FileOutputStream stream;

        try {
            stream = new FileOutputStream(utils.getFilePath(), true);
            stream.write((" Distance: " + String.valueOf(distance / 1000)).getBytes());
            stream.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void onPause()
    {
        super.onPause();
    }

    public void onProviderDisabled(String provider)
    {

    }

    public void onProviderEnabled(String provider)
    {

    }

    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }
}
