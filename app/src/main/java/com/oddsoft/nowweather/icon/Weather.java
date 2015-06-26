package com.oddsoft.nowweather.icon;

import android.app.Activity;

import com.oddsoft.nowweather.R;

import java.util.Date;

/**
 * Created by andycheng on 2015/6/26.
 */
public class Weather {

    public int getWeatherIcon(int actualId, long sunrise, long sunset) {
        //int id = actualId / 100;
        int icon = R.string.wi_day_sunny;

        Boolean night = false;
        long currentTime = new Date().getTime();
        if (currentTime >= sunrise && currentTime < sunset) {
                //icon = (R.string.weather_sunny);
            night = false;
        } else {
            night = true;
                //icon = (R.string.weather_clear_night);
        }

        switch (actualId) {
            case 201:
            case 202:
            case 211:
            case 212:
            case 221:
            case 232:
                icon = (R.string.wi_thunderstorm);
                break;
            case 200:
            case 210:
            case 230:
            case 231:
                icon = (R.string.wi_storm_showers);
                break;
            case 300:
            case 301:
            case 302:
            case 310:
            case 311:
            case 312:
            case 313:
            case 314:
            case 321:
                icon = (R.string.wi_showers);
                break;
            case 500:
            case 501:
            case 520:
            case 521:
                icon = (R.string.wi_rain);
                break;
            case 504:
            case 522:
            case 531:
                icon = (R.string.wi_rain_wind);
                break;
            case 502:
            case 503:
                icon = (R.string.wi_rain_mix);
                break;
            case 511:
                icon = (R.string.wi_sprinkle);
                break;
            case 600:
            case 601:
            case 602:
            case 611:
            case 612:
            case 615:
            case 616:
            case 620:
            case 621:
            case 622:
                icon = (R.string.wi_snow);
                break;
            case 700:
            case 711:
            case 721:
            case 731:
            case 741:
            case 751:
            case 761:
            case 762:
                icon = (R.string.wi_fog);
                break;
            case 781:
            case 900:
            case 901:
            case 902:
            case 960:
            case 961:
            case 962:
                icon = (R.string.wi_tornado);
                break;
            case 771:
            case 957:
            case 958:
            case 959:
                icon = (R.string.wi_strong_wind);
                break;
            case 800:
                if (night) {
                    icon = (R.string.wi_day_sunny);
                } else {
                    icon = (R.string.wi_night_clear);
                }
                break;
            case 801:
                icon = (R.string.wi_day_cloudy);
                break;
            case 802:
                icon = (R.string.wi_day_sunny_overcast);
                break;
            case 803:
            case 804:
                icon = (R.string.wi_cloudy);
                break;
            case 905:
                icon = (R.string.wi_windy);
                break;
            case 906:
                icon = (R.string.wi_hail);
                break;
            case 903:
                icon = (R.string.wi_thermometer_exterior);
                break;
            case 904:
                icon = (R.string.wi_thermometer);
                break;
            case 951:
                icon = (R.string.wi_cloud);
                break;
            case 952:
            case 953:
            case 954:
                icon = (R.string.wi_cloudy_windy);
                break;
            case 955:
            case 956:
                icon = (R.string.wi_cloudy_gusts);
                break;
        }


        return icon;
    }

}
