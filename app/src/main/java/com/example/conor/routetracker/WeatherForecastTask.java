package com.example.conor.routetracker;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mohitd on 6/28/15.
 */
public class WeatherForecastTask extends AsyncTask<Double, Void, ArrayList<WeatherItem>> {
    private WeatherForecastAdapter adapter;

    public WeatherForecastTask(WeatherForecastAdapter adapter)
    {
        this.adapter = adapter;
    }
    @Override
    protected ArrayList<WeatherItem> doInBackground(Double... params) {
        ArrayList<WeatherItem> list = new ArrayList<WeatherItem>();

        try
        {
            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat="
                    + params[0]
                    + "&lon="
                    + params[1]
                    + "&units=metric&cnt=7&APPID=05ad5f0f01dada361dcb1afd7b4f054c");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            int response = connection.getResponseCode();
            if(response == HttpURLConnection.HTTP_OK)
            {
                InputStream is = connection.getInputStream();
                String json = convertStreamToString(is);
                is.close();
                list.addAll(parseJSON(json));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onPostExecute(ArrayList<WeatherItem> weathers) {
        super.onPostExecute(weathers);
        adapter.setWeatherList(weathers);
        adapter.notifyDataSetChanged();
    }

    // Helper method to parse the JSON that OpenWeatherMap returns
    private ArrayList<WeatherItem> parseJSON(String json) throws JSONException {
        ArrayList<WeatherItem> forecast = new ArrayList<WeatherItem>();
        JSONArray jsonArray = new JSONObject(json).getJSONArray("list");
        for (int i = 0; i < jsonArray.length(); i++) {
            WeatherItem weather = new WeatherItem();
            JSONObject jsonDay = jsonArray.getJSONObject(i);
            weather.setTimestamp(jsonDay.getInt("dt"));
            weather.setHigh(jsonDay.getJSONObject("temp").getDouble("max"));
            weather.setLow(jsonDay.getJSONObject("temp").getDouble("min"));

            JSONObject jsonWeather = jsonDay.getJSONArray("weather").getJSONObject(0);
            weather.setWeather(jsonWeather.getString("main"));
            forecast.add(weather);
        }
        return forecast;
    }

    // Helper method to convert the output from OpenWeatherMap to a String
    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }
}
