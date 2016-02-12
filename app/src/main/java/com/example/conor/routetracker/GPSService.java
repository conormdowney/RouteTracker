package com.example.conor.routetracker;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Created by User on 13/11/2014.
 */
public class GPSService extends Service implements LocationListener{

    private static final int PERMISSION_REQUEST = 1;
    IBinder binder;
    LocationManager locMan;
    Thread serviceThread;
    boolean timerStarted = false;
    long startTime = 0;
    File file;
    String saveLoc;
    boolean running = true;

    Messenger mess;

    static final int MSG_REGISTER = 1;
    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 2;

    static final int LOCATION_CHANGED = 3;

    final Messenger mServiceMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER:
                /*
                 * Do whatever we want with the client messenger: Messenger
                 * clientMessenger = msg.replyTo
                 */
                    mess = msg.replyTo;
                    //Toast.makeText(getApplicationContext(), "Service : received client Messenger!", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_SAY_HELLO:
                /*
                 * Do whatever we want with the client messenger: Messenger
                 * clientMessenger = msg.replyTo
                 */
                    //Toast.makeText(getApplicationContext(), "Service : Client said hello!", Toast.LENGTH_SHORT).show();
                    break;
                case LOCATION_CHANGED:
                /*
                 * Do whatever we want with the client messenger: Messenger
                 * clientMessenger = msg.replyTo
                 */
                    mess = msg.replyTo;
                    //Toast.makeText(getApplicationContext(),"Service : Location Message Received", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        //Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mServiceMessenger.getBinder();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //createLogFile();

        /*locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 1, this);
        addLocationListener();

        if(errorLog.exists()) {
            boolean deleted = errorLog.delete();
        }

        try{
            boolean created = errorLog.createNewFile();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        Log("onStartCommand\n");*/

        return Service.START_STICKY;

        //move code from onCreate to onStartCommand
    }

    public void onCreate()
    {
        super.onCreate();

        //Need to have the permissions code in the checkPermission method and then have
        //code to stop it being fired if the OS is not 23
        checkPermission();

        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, this);
        addLocationListener();
    }

    @TargetApi(23)
    public void checkPermission()
    {
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //requestPermissions requires an activity - need to look at again
            //ActivityCompat.requestPermissions(getApplicationContext(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        //running = false;
        //checkPermission();
        //locMan.removeUpdates(this);
    }



    private void addLocationListener()
    {
        serviceThread = new Thread(new Runnable(){
            public void run(){
                if(running) {
                    try {
                        Looper.prepare();//Initialise the current thread as a looper.

                        Looper.loop();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, "LocationThread");
        serviceThread.start();
    }

    public void onLocationChanged(Location location)
    {
        if(location != null)
        {
            double lat = location.getLatitude();
            double lon = location.getLongitude();

            //Toast.makeText(getBaseContext().getApplicationContext(), "in location changed", Toast.LENGTH_SHORT).show();

            //Message m = new Message();                  //create the message to send back to the client
            Message  m = Message.obtain(null, LOCATION_CHANGED, 0, 0);
            Bundle b = new Bundle();                    //Just to show how to send other objects with it
            b.putString("latitude", String.valueOf(lat)); //this could be any parceable object
            b.putString("longitude", String.valueOf(lon));
            m.setData(b);                               //adds the bundle to the message

            try {
                mess.send(m);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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
