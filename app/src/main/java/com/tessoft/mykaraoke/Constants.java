package com.tessoft.mykaraoke;

/**
 * Created by Daeyong on 2017-08-22.
 */
public class Constants {

    public static boolean bReal = true;

    static String devIP = "192.168.0.200:8080";
    public static String FAIL = "9999";

    public static String GUIDE_DO_NOT_SHOW_BASIC_PLAYLIST_SAVE = "do_not_show_basic_play_list_save_guide";

    public static String getServerURL( String relativePath )
    {
        if ( bReal )
            return "http://www.hereby.co.kr/karaoke/" + relativePath;
        else
            return "http://" + devIP + "/karaoke/" + relativePath;
    }
}
