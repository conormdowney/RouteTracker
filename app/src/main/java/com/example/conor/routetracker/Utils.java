package com.example.conor.routetracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Conor on 2016-01-17.
 */
public final class Utils {

    private boolean timerStarted = false;
    private long startTime = 0;
    private File file;
    private static Context context;
    private boolean firstTime = true;

    private static Utils utils = new Utils();

    private Utils() {}

    public static Utils getInstance(Context contextParam){
        context = contextParam;
        return utils;
    }

    public boolean createFile()
    {
        firstTime = true;
        timerStarted = false;
        PackageManager pkgMan = context.getPackageManager();
        String pkg = context.getPackageName();
        String pkgDir  = "";
        try {
            PackageInfo p = pkgMan.getPackageInfo(pkg, 0);
            pkgDir = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String saveLoc = pkgDir + "/locationTrack.txt";
        file = new File(saveLoc);

        if(file.exists()) {
            boolean deleted = file.delete();
        }

        try{
            boolean created = file.createNewFile();
        }
        catch(IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void writeRouteToFile(GeoPoint geoPt)
    {
        if(!timerStarted)
        {
            startTime = SystemClock.elapsedRealtime();
            timerStarted = true;
        }

        long timeLapse = SystemClock.elapsedRealtime() - startTime;
        FileOutputStream stream;

        String startOfString = "\n";
        if(firstTime) {
            startOfString = "";
            firstTime = false;
        }

        try {
            stream = new FileOutputStream(file, true);
            String str = startOfString + "(" + geoPt.getLatitude() + "," + geoPt.getLongitude() + ", T:" + timeLapse + ")";
            stream.write(str.getBytes());
            stream.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getFilePath()
    {
        return file.getAbsolutePath();
    }

    public void screenShot(Window window)
    {
        String mPath = Environment.getExternalStorageDirectory().toString() + "/Download/test.jpg";

        Toast.makeText(context, mPath, Toast.LENGTH_LONG).show();

        Bitmap bitmap;
        View v1 = window.getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<GeoPoint> loadWaypointsFromFile(String fileName)
    {
        File loadFile = new File(fileName);
        ArrayList<GeoPoint> loadedWaypoints = new ArrayList<GeoPoint>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(loadFile));
            String line;

            //while((line = br.readLine()) != null)
            //{
              //  int y = 0;
            //}

            while ((line = br.readLine()) != null) {
                //if(line.compareTo("") != 0) {
                    String loadedLatStr = line.substring(line.indexOf("(") + 1, line.indexOf(","));
                    String loadedLongStr = line.substring(line.indexOf(",") + 1, line.lastIndexOf(","));

                    double loadedLat = Double.parseDouble(loadedLatStr);
                    double loadedLong = Double.parseDouble(loadedLongStr);

                    loadedWaypoints.add(new GeoPoint(loadedLat, loadedLong));
                //}
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return loadedWaypoints;
    }

    public ArrayList<Long> loadTimeLapsesFromFile(String fileName)
    {
        File loadFile = new File(fileName);
        ArrayList<Long> loadedTimeLapses = new ArrayList<Long>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(loadFile));
            String line;

            while ((line = br.readLine()) != null) {
                String loadedTimeLapseStr = line.substring(line.indexOf(":") + 1, line.indexOf(")"));

                long loadedTimeLapse = Long.parseLong(loadedTimeLapseStr);

                loadedTimeLapses.add(loadedTimeLapse);
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return loadedTimeLapses;
    }

    public GeoPoint getFirstPoint(String fileName)
    {
        File loadFile = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(loadFile));
            String line;

            line = br.readLine();
            String loadedLatStr = line.substring(line.indexOf("(") + 1, line.indexOf(","));
            String loadedLongStr = line.substring(line.indexOf(",") + 1, line.lastIndexOf(","));

            double loadedLat = Double.parseDouble(loadedLatStr);
            double loadedLong = Double.parseDouble(loadedLongStr);

            return new GeoPoint(loadedLat, loadedLong);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return new GeoPoint(0, 0);
    }

    /*public void loadFile(String fileName)
    {
        File loadFile = new File(fileName);
        boolean firstPoint = true;
        try {
            BufferedReader br = new BufferedReader(new FileReader(loadFile));
            String line;

            while ((line = br.readLine()) != null) {
                String loadedLatStr = line.substring(line.indexOf("(") + 1, line.indexOf(","));
                String loadedLongStr = line.substring(line.indexOf(",") + 1, line.lastIndexOf(","));
                String loadedTimeLapseStr = line.substring(line.indexOf(":") + 1, line.indexOf(")"));

                double loadedLat = Double.parseDouble(loadedLatStr);
                double loadedLong = Double.parseDouble(loadedLongStr);
                long loadedTimeLapse = Long.parseLong(loadedTimeLapseStr);

                if(firstPoint)
                {
                    firstPt = new GeoPoint(loadedLat, loadedLong);
                    firstPoint = false;
                }

                loadedWaypoints.add(new GeoPoint(loadedLat, loadedLong));
                loadedTimeLapses.add(loadedTimeLapse);
            }
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        drawLoadedRoute(firstPt);
    }*/
}
