package com.example.android.sunshine.app;

/**
 * Created by konst on 13.04.16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int id = item.getItemId();
        if( id == R.id.action_refresh ) {
            // http://api.openweathermap.org/data/2.5/forecast/daily?q=94043,us&mode=json&units=metric&cnt=7
            // API key = 308685849097e82758cc9a3a00a72d07
            updateWeather();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

/*
        String[] forecastArray = {
                "Today - sunny - 11/11",
                "Tomorrow - sunny - 22/22",
                "Wed - sunny - 33/33",
                "Thu - sunny - 44/44",
                "Fri - sunny - 55/55",
                "Sat - sunny - 66/66",
                "Sun - sunny - 77/77"
        };
        List<String> weekForecast = new ArrayList<String>( Arrays.asList(forecastArray) );
*/

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
/*
                Toast toast = Toast.makeText(getActivity(), mForecastAdapter.getItem(i), Toast.LENGTH_SHORT);
                toast.show();
*/
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(i));
                startActivity(intent);

                // Verify that the intent will resolve to an activity
/*
                if( sendIntent.resolveActivity(getPackageManager()) != null ) {
                    startActivity(sendIntent);
                }
*/
            }
        });

        return rootView;
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(SettingsActivity.KEY_PREF_LOCAION, getString(R.string.pref_location_default));

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);

        // lines for debug, to delete in prod
        Toast toast = Toast.makeText(getActivity(), location, Toast.LENGTH_SHORT);
        toast.show();

        weatherTask.execute(location);
    }

    // закоментарено == как бы удалено для использования нового FetchWeatherTask -- see FetchWeatherTask.java
//    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        /** The system calls this to perform work in a worker thread and
//         * delivers it the parameters given to AsyncTask.execute() */
//        protected String[] doInBackground(String... params) {
//
//            if( params.length == 0 ) {
//                return null;
//            }
//            // These two need to be declared outside the try/catch
//            // so that they can be closed in the finally block.
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String forecastJsonStr = null;
//
//            String format = "";
//            String units = "metric";  // mode - possible values are xml and html. If mode parameter is empty the format is JSON by default. see at - http://openweathermap.org/current#format
//            int numDays = 7;
//
//            try {
//                // Construct the URL for the OpenWeatherMap query
//                // Possible parameters are avaiable at OWM's forecast API page, at
//                // http://openweathermap.org/API#forecast
//                // http://openweathermap.org/current
//                // See example at - http://openweathermap.org/weather-conditions
////                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
////                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
////                URL url = new URL(baseUrl.concat(apiKey));
//                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
//                final String QUERY_PARAM = "q";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//                final String APPID_PARAM = "APPID";
//
//                Uri builtUri = Uri.parse( FORECAST_BASE_URL ).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, params[0])
//                        .appendQueryParameter(FORMAT_PARAM, format)
//                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY )
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//
//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
//
//                // Create the request to OpenWeatherMap, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                forecastJsonStr = buffer.toString();
//
//                Log.v(LOG_TAG, "Forecast JSON string: " + forecastJsonStr);
//
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the weather data, there's no point in attemping
//                // to parse it.
//                return null;
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            int daysNum = 7;
//            String[] weather = new String[daysNum];
//            try {
//                weather = getWeatherDataFromJson(forecastJsonStr, daysNum);
//            }
//            catch(JSONException e) {
//                Log.e(LOG_TAG, "String format is wrong for 'getWeatherDataFromJson' " + forecastJsonStr);
//            }
//
//            Log.i(LOG_TAG, "For " + daysNum + " forecast is " + weather.toString());
//
//            return weather;
//        }
//
//        /** The system calls this to perform work in the UI thread and delivers
//         * the result from doInBackground() */
//        protected void onPostExecute(String[] forecastArray) {
//            if( forecastArray == null ) { return; }
//
//            mForecastAdapter.clear();
//            for( String dayForecast : forecastArray ) {
//                mForecastAdapter.add(dayForecast);
//            }
//        }
//        /* The date/time conversion code is going to be moved outside the asynctask later,
//         * so for convenience we're breaking it out into its own method now.
//         */
//        private String getReadableDateString(long time){
//            // Because the API returns a unix timestamp (measured in seconds),
//            // it must be converted to milliseconds in order to be converted to valid date.
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            return shortenedDateFormat.format(time);
//        }
//
//        /**
//         * Prepare the weather high/lows for presentation.
//         */
//        private String formatHighLows(double high, double low) {
//            // For presentation, assume the user doesn't care about tenths of a degree.
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String units = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
//            // default is Metric units here
//            if( units.equals(getString(R.string.pref_units_imperial)) ) {
//                high = high * 1.8 + 32;
//                low = low * 1.8 + 32;
//            }
//            else if( !units.equals(getString(R.string.pref_units_default)) ) {
//                Log.d(LOG_TAG, "Unit type undefined: " + units);
//            }
//
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//            String highLowStr = roundedHigh + "/" + roundedLow;
//
//            return highLowStr;
//        }
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//                throws JSONException {
//
//            // Here is the link to some usefull JSON formatter - https://jsonformatter.curiousconcept.com/
//            // These are the names of the JSON objects that need to be extracted.
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//            final String OWM_DESCRIPTION = "main";
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            // OWM returns daily forecasts based upon the local time of the city that is being
//            // asked for, which means that we need to know the GMT offset to translate this data
//            // properly.
//
//            // Since this data is also sent in-order and the first day is always the
//            // current day, we're going to take advantage of that to get a nice
//            // normalized UTC date for all of our weather.
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            // we start at the day returned by local time. Otherwise this is a mess.
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            // now we work exclusively in UTC
//            dayTime = new Time();
//            // << see comments about time  on https://gist.github.com/udacityandroid/4ee49df1694da9129af9
///*
//            This code gives the format of day that we want (Wed Jul 1) and is elegant too:
//            //create a Gregorian Calendar, which is in current date
//            GregorianCalendar gc = new GregorianCalendar();
//            //add i dates to current date of calendar
//            gc.add(GregorianCalendar.DATE, i);
//            //get that date, format it, and "save" it on variable day
//            Date time = gc.getTime();
//            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//            day = shortenedDateFormat.format(time);
//
//            So from the given code (Github gist) you not need the code on lines 1-9, 44-60, 70-77.
//            And put the code I give right before the code:
//
//            // description is in a child array called "weather", which is 1 element long.
//            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObjec
//
//            >>> plus correction
//            I think we can also do
//            GregorianCalendar gc = new GregorianCalendar();
//            outside the loop, and use
//            gc.add(GregorianCalendar.DATE, 1); // GregorianCalendar.DATE =+ 1
//            instead of
//            gc.add(GregorianCalendar.DATE, i);
//            to reuse the GregorianCalendar we create.
//*/
//
//
//
//            String[] resultStrs = new String[numDays];
//            for(int i = 0; i < weatherArray.length(); i++) {
//                // For now, using the format "Day, description, hi/low"
//                String day;
//                String description;
//                String highAndLow;
//
//                // !!! JSON Tutorial at W3Schools Home - http://www.w3schools.com/json/
//
//                // Get the JSON object representing the day
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                // The date/time is returned as a long.  We need to convert that
//                // into something human-readable, since most people won't read "1400356800" as
//                // "this saturday".
//                long dateTime;
//                // Cheating to convert this to UTC time, which is what we want anyhow
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//                day = getReadableDateString(dateTime);
//
//                // description is in a child array called "weather", which is 1 element long.
//                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//
//                // Temperatures are in a child object called "temp".  Try not to name variables
//                // "temp" when working with temperature.  It confuses everybody.
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                highAndLow = formatHighLows(high, low);
//                resultStrs[i] = day + " - " + description + " - " + highAndLow;
//            }
//
//            for (String s : resultStrs) {
//                Log.v(LOG_TAG, "Forecast entry: " + s);
//            }
//            return resultStrs;
//
//        }
//    }
}

