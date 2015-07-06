package com.example.d038395.audioupanddown;

/**
 * Created by d038395 on 2015-07-06.
 */

public class MyUtils {

    //format the milliseconds in to hh:mm:ss
    static String formatMilliToHMS(int milli){
        int hour, minute,second;
        second=milli/1000;
        minute=second/60;
        second%=60;
        hour=minute/60;
        minute%=60;
        return String.format("%d:%02d:%02d",hour,minute,second);
    }
}
