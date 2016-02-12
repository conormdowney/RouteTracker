package com.example.conor.routetracker;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Conor on 2016-01-11.
 */
public class WeatherList extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView listView;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_list);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Weather");
        toolbar.setTitleTextColor(Color.WHITE);

        listView = (ListView)findViewById(R.id.weatherList);
        listView.setOnItemClickListener(this);
        WeatherForecastAdapter adapter = new WeatherForecastAdapter(this, R.layout.weather_item);
        listView.setAdapter(adapter);

        Intent weatherIntent = getIntent();

        double lon = weatherIntent.getDoubleExtra("lon", 0);
        double lat = weatherIntent.getDoubleExtra("lat", 0);

        WeatherForecastTask task = new WeatherForecastTask(adapter);
        task.execute(lat, lon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent calendarIntent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(Calendar.HOUR_OF_DAY, 0);
        beginTime.add(Calendar.DAY_OF_MONTH, position);

        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
        calendarIntent.putExtra(CalendarContract.Events.TITLE, "Run");

        Intent chooser = Intent.createChooser(calendarIntent, "Please choose");
        if (calendarIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }
}
