package com.oddsoft.nowweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.oddsoft.nowweather.app.JsonRequest;
import com.oddsoft.nowweather.app.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;

public class MainActivity extends ActionBarActivity {

    final String TAG = "MainActivity";
    ImageView mImageView;
    TextView mTxtDegrees, mTxtWeather, mTxtError;

    Singleton helper = Singleton.getInstance();
    //int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int mainColor = Color.parseColor("#FF5722");
    //SharedPreferences mSharedPref;

    String weatherKeyword;


    final static String
            FLICKR_API_KEY = "dc7cb6cc8e546dd0fb6d3a1ba5bfa971",
            IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
                           "&tag_mode=all&api_key=",

            RECENT_API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?";

    //SHARED_PREFS_IMG_KEY = "img",
     //       SHARED_PREFS_DAY_KEY = "day";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views setup
        mImageView = (ImageView) findViewById(R.id.main_bg);
        mTxtDegrees = (TextView) findViewById(R.id.degrees);
        mTxtWeather = (TextView) findViewById(R.id.weather);
        mTxtError = (TextView) findViewById(R.id.error);

        // Font
        //mTxtDegrees.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        //mTxtWeather.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));

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
     * @throws Exception When a working API key is not provided.
     */
    private void searchRandomImage(String keyword) throws Exception {
        if (FLICKR_API_KEY.equals(""))
            throw new Exception("You didn't provide a working Flickr API key!");

        Log.d(TAG, "searchRandomImage = " + keyword);

        String tag = "&tags="+keyword+",weather";
        JsonRequest request = new JsonRequest
                (Request.Method.GET, IMAGES_API_ENDPOINT+FLICKR_API_KEY+tag, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // if you want to debug: Log.v(getString(R.string.app_name), response.toString());

                        try {
                            JSONArray images = response.getJSONObject("photos").getJSONArray("photo");
                            int index = new Random().nextInt(images.length());

                            JSONObject imageItem = images.getJSONObject(index);

                            String imageUrl = "http://farm" + imageItem.getString("farm") +
                                    ".static.flickr.com/" + imageItem.getString("server") + "/" +
                                    imageItem.getString("id") + "_" + imageItem.getString("secret") + "_" + "c.jpg";

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
                (Request.Method.GET, RECENT_API_ENDPOINT+"units=metric&lat=25.0925009&lon=121.5312909", null, new Response.Listener<JSONObject>() {

                    //For temperature in Fahrenheit use units=imperial
                    //For temperature in Celsius use units=metric

                    @Override
                    public void onResponse(JSONObject response) {
                        // if you want to debug: Log.v(getString(R.string.app_name), response.toString());
                        try {

                            String minTemp, maxTemp, curTemp, atmo="";
                            double avgTemp;

                            JSONArray wArray= (JSONArray) response.getJSONArray("weather");

                            for (int i = 0; i < wArray.length(); i++)
                            {
                                atmo = wArray.getJSONObject(i).getString("main");
                                weatherKeyword = atmo;
                            }


                            Log.d(TAG, "weatherKeyword = " + weatherKeyword);

                            //minTemp = response.getString("min_temp"); minTemp = minTemp.substring(0, minTemp.indexOf("."));
                            //maxTemp = response.getString("max_temp"); maxTemp = maxTemp.substring(0, maxTemp.indexOf("."));

                            curTemp = response.getJSONObject("main").getString("temp");


                            avgTemp = Double.parseDouble(curTemp); //(Integer.parseInt(minTemp)+Integer.parseInt(maxTemp))/2;


                            mTxtDegrees.setText(avgTemp+"°");
                            mTxtWeather.setText(atmo);

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
