package com.oddsoft.nowweather.app;

import android.util.Log;

import java.util.Locale;

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

    public String getLanguageCode() {
        String code="en";

        switch (Locale.getDefault().toString().toLowerCase()) {
            case "en_us":
                code = "en"; //English - en
                break;
            case "zh_tw":
            case "zh":
                code = "zh_tw"; //Chinese Traditional - zh_tw
                break;
            case "zh_cn":
                code = "zh_cn"; //Chinese Simplified - zh (or zh_cn)
                break;
            case "it":
            case "it_it":
                code = "it"; //Italian - it
                break;
            case "fr":
            case "fr_ca":
                code = "fr"; //French - fr
                break;
            case "tr":
                code = "tr"; //Turkish - tr
                break;
            case "ru":
                code = "ru"; //Russian - ru
                break;
            case "es":
                code = "es"; //Spanish - es (or sp)
                break;
            case "de":
                code = "de"; //German - de
                break;
            case "fi":
                code = "fi"; //Finnish - fi
                break;
            case "pt":
                code = "pt"; //Portuguese - pt
                break;
            case "pl":
                code = "pl"; //Polish - pl
                break;
            case "uk":
                code = "uk"; //Ukrainian - uk (or ua)
                break;
            case "nl":
                code = "nl"; //Dutch - nl
                break;
            case "ro":
                code = "ro"; //Romanian - ro
                break;
            case "sv":
                code = "sv"; //Swedish - sv (or se)
                break;
            case "ca":
                code = "ca"; //Catalan - ca
                break;
            case "bg":
                code = "bg"; //Dutch - nl
                break;
            case "hr":
                code = "hr"; //Croatian - hr
                break;
            default:
                code = "en";
                break;
        }
        return code;
    }
}
