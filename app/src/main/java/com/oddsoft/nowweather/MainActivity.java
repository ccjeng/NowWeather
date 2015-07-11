package com.oddsoft.nowweather;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.oddsoft.nowweather.app.Analytics;
import com.oddsoft.nowweather.app.JsonRequest;
import com.oddsoft.nowweather.app.NowWeather;
import com.oddsoft.nowweather.app.Utils;
import com.oddsoft.nowweather.ui.WeatherIcon;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = NowWeather.TAG;
    private ImageView mImageView;
    private TextView mTxtCity, mTxtDegrees, mTxtDescr, mTxtWeather, mTxtTempRange, mTxtHumidity
            , mTxtCloudiness, mTxtWind, mTxtError;

    private NowWeather helper = NowWeather.getInstance();
    //int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private int mainColor = Color.parseColor("#FF5722");
    //SharedPreferences mSharedPref;

    private String weatherKeyword;

    final static String
            FLICKR_API_KEY = NowWeather.FLICKR_API_KEY,
            IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
                    "&group_id=1463451@N25&tag_mode=all&api_key=",

    RECENT_API_ENDPOINT = "http://api.openweathermap.org/data/2.5/weather?";


    private String prefUnit;
    private Boolean prefEng;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mLlvDrawerContent;
    private ListView mLsvDrawerMenu;

    // 記錄被選擇的選單指標用
    private int mCurrentMenuItemPosition = -1;

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

    private AnimatorSet mAnimationSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Views setup
        mImageView = (ImageView) findViewById(R.id.main_bg);
        mTxtDegrees = (TextView) findViewById(R.id.degrees);
        mTxtWeather = (TextView) findViewById(R.id.weather);
        mTxtDescr = (TextView) findViewById(R.id.description);
        mTxtTempRange = (TextView) findViewById(R.id.temprange);
        mTxtHumidity = (TextView) findViewById(R.id.humidity);
        mTxtCloudiness = (TextView) findViewById(R.id.cloudiness);
        mTxtWind = (TextView) findViewById(R.id.wind);
        mTxtError = (TextView) findViewById(R.id.error);

        // Font
        mTxtWeather.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/weather1.3.ttf"));
        mTxtDegrees.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtDescr.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtTempRange.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtTempRange.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtHumidity.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtCloudiness.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTxtWind.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));


        Analytics ga = new Analytics();
        if (!NowWeather.APPDEBUG)
            ga.initTracker(this);

        //Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.alpha);
        //mImageView.startAnimation(animation);
/*
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mImageView, "alpha",  1f, .3f);
        fadeOut.setDuration(1000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mImageView, "alpha", .3f, 1f);
        fadeIn.setDuration(1000);


        mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
            }
        });

        mAnimationSet.start();
*/

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        mImageView.setAnimation(animation);

        //Load Default Value
        mTxtTempRange.setText("-- ~ --");
        mTxtHumidity.setText(getString(R.string.humidity) + ": -- %");
        mTxtCloudiness.setText(getString(R.string.cloudiness) + ": -- %");
        mTxtWind.setText(getString(R.string.wind) + ": ---- ");

        initActionBar();
        initDrawer();
        initDrawerList();

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

        if (keyword.equals("Additional"))
                keyword = "weather";

        String tag = "&tags=" + keyword ;//+ ",weather";

        String url = IMAGES_API_ENDPOINT + FLICKR_API_KEY + tag;
        if (NowWeather.APPDEBUG) {
            Log.d(TAG, "searchRandomImage = " + keyword);
            Log.d(TAG, url);
        }
        JsonRequest request = new JsonRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

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

                        //Set new image
                        mImageView.setImageBitmap(bitmap);
                        //mAnimationSet.end();
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        imageError(error);
                    }
                });

        helper.add(request);
    }


    /**
     * Fetches and displays the weather data of Mars.
     */
    private void loadWeatherData() {

        getPreferences();
        mTxtError.setVisibility(View.GONE);

        myLoc = (currentLocation == null) ? lastLocation : currentLocation;

        //fake location
        if (NowWeather.APPDEBUG) {
            myLoc = new Location("");
            //myLoc.setLatitude(25.175579);
            //myLoc.setLongitude(121.43847);

            //Taipei City
            //myLoc.setLatitude(25.0950492);
            //myLoc.setLongitude(121.5246077);

            //New York
            myLoc.setLatitude(40.767504);
            myLoc.setLongitude(-73.977964);

            //London
            myLoc.setLatitude(51.486257);
            myLoc.setLongitude(-0.150507);
        }

        if (myLoc != null ) {

            Utils utils = new Utils();
            String languageInfo = "&lang=" + utils.getLanguageCode();
            String locationInfo = "&lat=" + myLoc.getLatitude()+"&lon="+myLoc.getLongitude();
            String unitInfo = "";
            if (prefUnit.equals("c")) {
                unitInfo = "units=metric";
            } else {
                unitInfo = "units=imperial";
            }

            if (prefEng) {
                languageInfo = "&lang=en";
            }

            String requestURL = RECENT_API_ENDPOINT + unitInfo + locationInfo + languageInfo;
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
            //http://www.openweathermap.org/weather-data#current
            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            JSONObject wind = json.getJSONObject("wind");
            JSONObject clouds = json.getJSONObject("clouds");


            weatherKeyword = details.getString("main");

            String windUnit;
            if (prefUnit.equals("c")) {
                windUnit = "m/s";
            } else {
                windUnit = "mph";
            }

            mTxtDescr.setText(details.getString("description").toUpperCase(Locale.US));

            mTxtTempRange.setText(main.getString("temp_min") + " ~ " + main.getString("temp_max"));
            mTxtHumidity.setText(getString(R.string.humidity) + ": " + main.getString("humidity") + "%");
            mTxtCloudiness.setText(getString(R.string.cloudiness) + ": " + clouds.getString("all") + "%");
            mTxtWind.setText(getString(R.string.wind) + ": " + wind.getString("speed") + " " + windUnit);

            mTxtDegrees.setText(
                    String.format("%.2f", main.getDouble("temp")));

            DateFormat df = DateFormat.getDateTimeInstance();
            String updatedOn = df.format(new Date(json.getLong("dt") * 1000));

            getActionBar().setTitle(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));
            getActionBar().setSubtitle(updatedOn);

            WeatherIcon w = new WeatherIcon();
            int icon = w.getWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);
            mTxtWeather.setText(icon);

        } catch (Exception e) {
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    private void imageError(Exception e) {
        //mImageView.setBackgroundColor(mainColor);
        e.printStackTrace();
    }

    private void txtError(Exception e) {
        mTxtError.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }

    private void getPreferences() {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        prefUnit = prefs.getString("unit", "c");
        prefEng = prefs.getBoolean("eng", false);


    }

    private void initActionBar(){
        //顯示 Up Button (位在 Logo 左手邊的按鈕圖示)
        getActionBar().setDisplayHomeAsUpEnabled(false);
        //打開 Up Button 的點擊功能
        getActionBar().setHomeButtonEnabled(true);
        // Backgrand Transparent
        getActionBar().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        //Set screen Portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //hide notification bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Translucent status bar
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }

    private void initDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drw_layout);
        // 設定 Drawer 的影子
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,    // 讓 Drawer Toggle 知道母體介面是誰
                R.drawable.ic_drawer, // Drawer 的 Icon
                R.string.app_name, // Drawer 被打開時的描述
                R.string.app_name // Drawer 被關閉時的描述
        ) {
            //被打開後要做的事情
            @Override
            public void onDrawerOpened(View drawerView) {
                // 將 Title 設定為自定義的文字
                //getActionBar().setTitle(R.string.app_name);
            }

            //被關上後要做的事情
            @Override
            public void onDrawerClosed(View drawerView) {
                // 將 Title 設定回 APP 的名稱
                //getActionBar().setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initDrawerList() {

        String[] drawer_menu = this.getResources().getStringArray(R.array.drawer_menu);

        // 定義新宣告的兩個物件：選項清單的 ListView 以及 Drawer內容的 LinearLayou
        mLsvDrawerMenu = (ListView) findViewById(R.id.lsv_drawer_menu);
        mLlvDrawerContent = (LinearLayout) findViewById(R.id.llv_left_drawer);

        int[] iconImage = { android.R.drawable.ic_menu_preferences
                , android.R.drawable.ic_dialog_info };

        List<HashMap<String,String>> lstData = new ArrayList<HashMap<String,String>>();
        for (int i = 0; i < iconImage.length; i++) {
            HashMap<String, String> mapValue = new HashMap<String, String>();
            mapValue.put("icon", Integer.toString(iconImage[i]));
            mapValue.put("title", drawer_menu[i]);
            lstData.add(mapValue);
        }


        SimpleAdapter adapter = new SimpleAdapter(this, lstData
                , R.layout.drawer_item
                , new String[]{"icon", "title"}
                , new int[]{R.id.rowIcon, R.id.rowText});
        mLsvDrawerMenu.setAdapter(adapter);

        // 當清單選項的子物件被點擊時要做的動作
        mLsvDrawerMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(position);
            }
        });

    }

    private void selectMenuItem(int position) {
        mCurrentMenuItemPosition = position;

        switch (mCurrentMenuItemPosition) {
            case 0:
                startActivity(new Intent(this, PrefActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }

        // 將選單的子物件設定為被選擇的狀態
        mLsvDrawerMenu.setItemChecked(position, true);

        // 關掉 Drawer
        mDrawerLayout.closeDrawer(mLlvDrawerContent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.refresh_settings:
                loadWeatherData();
                return true;
        }
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

        if (!NowWeather.APPDEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
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

        if (!NowWeather.APPDEBUG)
            GoogleAnalytics.getInstance(this).reportActivityStart(this);
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
