package com.oddsoft.nowweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oddsoft.nowweather.app.JsonRequest;
import com.oddsoft.nowweather.app.NowWeather;
import com.oddsoft.nowweather.app.Utils;
import com.oddsoft.nowweather.icon.Weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    final String TAG = NowWeather.TAG;
    ImageView mImageView;
    TextView mTxtCity, mTxtDegrees, mTxtWeather, mTxtDetail, mTxtUpdate, mTxtError;

    NowWeather helper = NowWeather.getInstance();
    //int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    int mainColor = Color.parseColor("#FF5722");
    //SharedPreferences mSharedPref;

    String weatherKeyword;

    final static String
            FLICKR_API_KEY = NowWeather.FLICKR_API_KEY,
            IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
                    "&group_id=1463451@N25&tag_mode=all&api_key=",

    RECENT_API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?";

    //SHARED_PREFS_IMG_KEY = "img",
    //       SHARED_PREFS_DAY_KEY = "day";

    /*
    * Define a request code to send to Google Play services This code is returned in
    * Activity.onActivityResult
    */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    // Fields for helping process map and location changes
    private Location lastLocation;
    private Location currentLocation;
    private Location myLoc;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private GoogleApiClient locationClient;

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

        getActionBar().setDisplayShowHomeEnabled(false);
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

        if (isNetworkConnected()) {
            // 建立Google API用戶端物件
            configGoogleApiClient();

            // 建立Location請求物件
            configLocationRequest();

            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        } else {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.network_error)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();

        }


        // SharedPreferences setup
        //mSharedPref = getPreferences(Context.MODE_PRIVATE);


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

        String tag = "&tags=" + keyword ;//+ ",weather";
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

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        //fake location
        if (NowWeather.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(25.175579);
            //myLoc.setLongitude(121.43847);

            //Taipei City
            myLoc.setLatitude(25.0950492);
            myLoc.setLongitude(121.5246077);

        }

        if (myLoc != null ) {

            Utils utils = new Utils();
            String languageInfo = "&lang=" + utils.getLanguageCode();
            //&lat=25.0925009&lon=121.5312909
            String locationInfo = "&lat=" + myLoc.getLatitude()+"&lon="+myLoc.getLongitude();
            String requestURL = RECENT_API_ENDPOINT + "units=metric" + locationInfo + languageInfo;
            if (NowWeather.APPDEBUG)
                Log.d(TAG, requestURL);

            JsonRequest request = new JsonRequest
                    (Request.Method.GET, requestURL
                            , null, new Response.Listener<JSONObject>() {

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

        }else {
            //location error
            new AlertDialog.Builder(MainActivity.this)
                    //.setTitle(R.string.app_name)
                    .setMessage(R.string.location_error)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();
        }


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

    /*
 * Called when the Activity is no longer visible at all. Stop updates and disconnect.
 */
    @Override
    public void onStop() {
        if (locationClient.isConnected()) {
            locationClient.disconnect();
        }
        super.onStop();
        // This will tell to Volley to cancel all the pending requests
        helper.cancel();
    }

    /*
    * Called when the Activity is restarted, even before it becomes visible.
    */
    @Override
    public void onStart() {
        super.onStart();
        // Connect to the location services client
        if (locationClient != null) {
            locationClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (adView != null)
        //    adView.pause();

        // 移除位置請求服務
        if (locationClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    locationClient, this);
        }
    }

    /*
    * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //if (adView != null)
        //    adView.resume();

        // 連線到Google API用戶端
        if (locationClient != null) {
            if (!locationClient.isConnected()) {
                locationClient.connect();
            }
        }
    }

    @Override
    protected void onDestroy() {
        //if (adView != null)
        //    adView.destroy();
        super.onDestroy();
    }

    /*
* check network state
* */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /*
 * Verify that Google Play services is available before making a request.
 *
 * @return true if Google Play services is available, otherwise false
 */
    private boolean isGoogleServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            if (NowWeather.APPDEBUG) {
                Log.d(TAG, "Google play services available");
            }
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Log.d(TAG, "Google play services NOT available");
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                dialog.show();
            }
            return false;
        }
    }


    /*
 * Get the current location
 */
    private Location getLocation() {
        // If Google Play Services is available
        if (isGoogleServicesAvailable()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

    //Google Play Service ConnectionCallbacks
    // 已經連線到Google Services
    @Override
    public void onConnected(Bundle bundle) {
        if (NowWeather.APPDEBUG)
            Log.d(TAG, "onConnected - Connected to location services");

        currentLocation = getLocation();

        // 已經連線到Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);

        if (NowWeather.APPDEBUG)
            Log.d(TAG, "onConnected - isConnected =" + locationClient.isConnected());

        // Weather data
        loadWeatherData();

    }

    // Google Services連線中斷
    // int參數是連線中斷的代號
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    // Google Services連線失敗
    // ConnectionResult參數是連線失敗的資訊
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();
        Log.i(TAG, "GoogleApiClient connection failed");

        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            new AlertDialog.Builder(this)
                    //.setTitle(R.string.app_name)
                    .setMessage(R.string.google_play_service_missing)
                    .setPositiveButton(R.string.ok_label,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    // empty
                                }
                            }).show();
        }

    }

    // 建立Google API用戶端物件
    private synchronized void configGoogleApiClient() {
        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    private void configLocationRequest() {
        // Create a new global location parameters object
        locationRequest = LocationRequest.create();

        // Set the update interval
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use low power
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }

    // 位置改變
    // Location參數是目前的位置
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        if (lastLocation != null) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
    }

}
