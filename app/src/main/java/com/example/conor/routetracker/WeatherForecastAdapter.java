package com.example.conor.routetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Conor on 2016-01-10.
 */
public class WeatherForecastAdapter extends ArrayAdapter<WeatherItem> {

    private ArrayList<WeatherItem> weatherList;

    public WeatherForecastAdapter(Context context, int resource) {
        super(context, resource);

        weatherList = new ArrayList<WeatherItem>();
    }

    public void setWeatherList(ArrayList<WeatherItem> weatherList)
    {
        this.weatherList = weatherList;
    }

    public int getCount()
    {
        return weatherList.size();
    }

    public WeatherItem getItem(int position)
    {
        return weatherList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        WeatherItem weatherItem = weatherList.get(position);
        WeatherViewHolder weatherViewHolder;

        if(convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.weather_item, parent, false);
            weatherViewHolder = new WeatherViewHolder(convertView);
            convertView.setTag(weatherViewHolder);
        }
        else
            weatherViewHolder = (WeatherViewHolder) convertView.getTag();

        String[] daysOfTheWeek = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(weatherItem.getTimestamp() * 1000);
        String day = daysOfTheWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        weatherViewHolder.day.setText(day);
        weatherViewHolder.weather.setText(weatherItem.getWeather());
        weatherViewHolder.high.setText(String.valueOf(weatherItem.getHigh()));
        weatherViewHolder.low.setText(String.valueOf(weatherItem.getLow()));

        return convertView;
    }

    private static class WeatherViewHolder{
        TextView high, low, weather, day;

        public WeatherViewHolder(View weatherItem)
        {
            high = (TextView) weatherItem.findViewById(R.id.high);
            low = (TextView) weatherItem.findViewById(R.id.low);
            weather = (TextView) weatherItem.findViewById(R.id.weather);
            day = (TextView) weatherItem.findViewById(R.id.day);
        }
    }
}
