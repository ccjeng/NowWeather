package com.oddsoft.nowweather.app;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NowWeather extends Application {

    public static final String TAG = NowWeather.class.getSimpleName();

    // Debugging switch
    public static final boolean APPDEBUG = false;

    public static final String FLICKR_API_KEY = "dc7cb6cc8e546dd0fb6d3a1ba5bfa971";

    private RequestQueue mRequestQueue;

    private static NowWeather mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    /**
     * Singleton main method. Provides the global static instance of the helper class.
     * @return The Singleton instance.
     */
    public static synchronized NowWeather getInstance() {
        return mInstance;
    }

    /**
     * Provides the general Volley request queue.
     */
    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    /**
     * Adds the request to the general queue.
     * @param req The object Request
     * @param <T> The type of the request result.
     */
    public <T> void add(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancels all the pending requests.
     */
    public void cancel() {
        mRequestQueue.cancelAll(TAG);
    }

}