package com.tessoft.mykaraoke;

/**
 * Created by Daeyong on 2017-08-22.
 */
public class Constants {

    public static boolean bReal = true;

    static String devIP = "192.168.0.200:8080";
    public static String FAIL = "9999";

    public static String getServerURL( String relativePath )
    {
        if ( bReal )
            return "http://www.hereby.co.kr/karaoke/" + relativePath;
        else
            return "http://" + devIP + "/karaoke/" + relativePath;
    }
}
