package com.example.conor.routetracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

public class SaveRouteActivity extends AppCompatActivity implements TextWatcher {

    String routeFilePath;
    EditText editTextfileName;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_route);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Save Route");

        routeFilePath = getIntent().getStringExtra("routeFile");
        editTextfileName = (EditText) findViewById(R.id.saveFileName);

        editTextfileName.addTextChangedListener(this);

        int y = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save_route, menu);
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

        return super.onOptionsItemSelected(item);
    }

    public void saveFile(String savePath)
    {
        File saveFile = new File(savePath);
        File routeFile = new File(routeFilePath);

        if(saveFile.exists()) {
            boolean deleted = saveFile.delete();
        }
        try{
            copyFile(routeFile, saveFile);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Intent mapIntent = new Intent();
        setResult(RESULT_OK, mapIntent);
        finish();
    }

    public void overwriteDialog(final String savePath)
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("A file with this name already exists. Do you want to overwrite it?");
        dialogBuilder.setCancelable(true);

        dialogBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveFile(savePath);
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

        AlertDialog overwriteDialog = dialogBuilder.create();
        overwriteDialog.show();
    }

    public void checkFileName(View view)
    {
        ArrayList<String> files = getFiles();
        File appDir = new File(Environment.getExternalStorageDirectory()+"/LocationTracker/");
        String savePath = appDir.getPath()+"/Routes/" + editTextfileName.getText() + ".txt";
        String fileName = editTextfileName.getText() + ".txt";

        for(String file : files)
        {
            if(file.compareTo(fileName)==0) {
                overwriteDialog(savePath);
                return;
            }
        }
        saveFile(savePath);
    }

    public ArrayList<String> getFiles()
    {
        String path = Environment.getExternalStorageDirectory()+"/LocationTracker/Routes/";

        // Read all files sorted into the values-array
        ArrayList<String> files = new ArrayList();
        File dir = new File(path);

        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public void cancelSave(View view)
    {
        finish();
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        toolbar.setTitle("Save Route: " + s);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
