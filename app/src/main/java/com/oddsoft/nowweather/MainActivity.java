package com.oddsoft.nowweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.oddsoft.nowweather.app.JsonRequest;
import com.oddsoft.nowweather.app.Singleton;
import com.oddsoft.nowweather.app.Utils;
import com.oddsoft.nowweather.icon.Weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    final String TAG = "MainActivity";
    ImageView mImageView;
    TextView mTxtCity, mTxtDegrees, mTxtWeather, mTxtDetail, mTxtUpdate, mTxtError;

    Singleton helper = Singleton.getInstance();
    //int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int mainColor = Color.parseColor("#FF5722");
    //SharedPreferences mSharedPref;

    String weatherKeyword;
    Typeface weatherFont;

    final static String
            FLICKR_API_KEY = "dc7cb6cc8e546dd0fb6d3a1ba5bfa971",
            IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
                    "&group_id=1463451@N25&tag_mode=all&api_key=",

    RECENT_API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?";

    //SHARED_PREFS_IMG_KEY = "img",
    //       SHARED_PREFS_DAY_KEY = "day";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views setup
        mImageView = (ImageView) findViewById(R.id.main_bg);
        mTxtCity = (TextView) findViewById(R.id.city);
        mTxtDegrees = (TextView) findViewById(R.id.degrees);
        mTxtWeather = (TextView) findViewById(R.id.weather);
        mTxtDetail = (TextView) findViewById(R.id.detail);
        mTxtUpdate = (TextView) findViewById(R.id.update);
        mTxtError = (TextView) findViewById(R.id.error);


        // Font
        mTxtWeather.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/weather.ttf"));
        mTxtCity.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtDegrees.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtDetail.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtUpdate.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));

        // Backgrand Transparent
        getActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        // SharedPreferences setup
        //mSharedPref = getPreferences(Context.MODE_PRIVATE);


        // Weather data
        loadWeatherData();


    }

    @Override
    protected void onStop() {
        super.onStop();
        // This will tell to Volley to cancel all the pending requests
        helper.cancel();
    }

    /**
     * Fetches a random picture of Mars, using Flickr APIs, and then displays it.
     *
     * @throws Exception When a working API key is not provided.
     */
    private void searchRandomImage(String keyword) throws Exception {
        if (FLICKR_API_KEY.equals(""))
            throw new Exception("You didn't provide a working Flickr API key!");

        Log.d(TAG, "searchRandomImage = " + keyword);

        String tag = "&tags=" + keyword + ",weather";
        JsonRequest request = new JsonRequest
                (Request.Method.GET, IMAGES_API_ENDPOINT + FLICKR_API_KEY + tag, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // if you want to debug: Log.v(getString(R.string.app_name), response.toString());

                        try {
                            JSONArray images = response.getJSONObject("photos").getJSONArray("photo");
                            int index = new Random().nextInt(images.length());

                            JSONObject imageItem = images.getJSONObject(index);

                            Utils url = new Utils();
                            String imageUrl = url.getFlickrImageURL(imageItem.getString("farm")
                                    , imageItem.getString("server")
                                    , imageItem.getString("id")
                                    , imageItem.getString("secret"));

                            // store the pict of the day
                            //SharedPreferences.Editor editor = mSharedPref.edit();
                            //editor.putInt(SHARED_PREFS_DAY_KEY, today);
                            //editor.putString(SHARED_PREFS_IMG_KEY, imageUrl);
                            //editor.commit();

                            // and finally load it
                            loadImg(imageUrl);

                        } catch (Exception e) {
                            imageError(e);
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imageError(error);
                    }
                });

        request.setPriority(Request.Priority.LOW);
        helper.add(request);

    }

    /**
     * Downloads and displays the picture using Volley.
     *
     * @param imageUrl the URL of the picture.
     */
    private void loadImg(String imageUrl) {
        // Retrieves an image specified by the URL, and displays it in the UI
        ImageRequest request = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        imageError(error);
                    }
                });

        // we don't need to set the priority here;
        // ImageRequest already comes in with
        // priority set to LOW, that is exactly what we need.
        helper.add(request);
    }

    /**
     * Fetches and displays the weather data of Mars.
     */
    private void loadWeatherData() {

        JsonRequest request = new JsonRequest
                (Request.Method.GET, RECENT_API_ENDPOINT + "units=metric&lat=25.0925009&lon=121.5312909", null, new Response.Listener<JSONObject>() {

                    //For temperature in Fahrenheit use units=imperial
                    //For temperature in Celsius use units=metric

                    @Override
                    public void onResponse(JSONObject response) {
                        // if you want to debug: Log.v(getString(R.string.app_name), response.toString());
                        try {

                            renderWeather(response);

                            // Picture
                            // search and load a random mars pict.
                            try {
                                searchRandomImage(weatherKeyword);
                            } catch (Exception e) {
                                // please remember to set your own Flickr API!
                                // otherwise I won't be able to show
                                // a random Mars picture
                                imageError(e);
                            }


                        } catch (Exception e) {
                            txtError(e);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtError(error);
                    }
                });

        request.setPriority(Request.Priority.HIGH);
        helper.add(request);

    }

    private void renderWeather(JSONObject json) {
        try {

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");

            weatherKeyword = details.getString("main");

            mTxtDetail.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + getString(R.string.humidity) + ": " + main.getString("humidity") + "%" +
                            "\n" + getString(R.string.pressure) + ": " + main.getString("pressure") + " hPa");

            mTxtDegrees.setText(
                    String.format("%.2f", main.getDouble("temp")) + " ℃");

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));

            getActionBar().setTitle(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));
            getActionBar().setSubtitle(updatedOn);

            Weather w = new Weather();
            int icon = w.getWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);
            mTxtWeather.setText(icon);

        } catch (Exception e) {
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    private void imageError(Exception e) {
        mImageView.setBackgroundColor(mainColor);
        e.printStackTrace();
    }

    private void txtError(Exception e) {
        mTxtError.setVisibility(View.VISIBLE);
        e.printStackTrace();
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
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
