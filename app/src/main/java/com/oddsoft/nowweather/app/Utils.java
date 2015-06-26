package com.oddsoft.nowweather.app;

/**
 * Created by andycheng on 2015/6/26.
 */
public class Utils {

    public String getFlickrImageURL(String farm, String server, String id, String secret){

        /* Format
            https://farm{farm-id}.staticflickr.com/{server-id}/{id}_{secret}_[mstzb].jpg
        * */
        return "http://farm" + farm + ".static.flickr.com/" + server + "/" +
                id + "_" + secret + "_" + "c.jpg";


    }
}
