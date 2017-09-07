package com.tessoft.mykaraoke;

/**
 * Created by Daeyong on 2017-08-22.
 */
public class Constants {

    public static boolean bReal = true;

    static String devIP = "192.168.0.200:8080";
//    static String devIP = "192.168.219.175:8080";
    public static String FAIL = "9999";

    public static String GUIDE_DO_NOT_SHOW_BASIC_PLAYLIST_SAVE = "do_not_show_basic_play_list_save_guide";

    public static String BROADCAST_LOAD_MY_PLAYLIST = "BROADCAST_LOAD_MY_PLAYLIST";
    public static String BROADCAST_PLAY_NEXT_MV= "BROADCAST_PLAY_NEXT_MV";
    public static String BROADCAST_PLAY_NEXT_POPULAR_SONG = "BROADCAST_PLAY_NEXT_POPULAR_SONG";

    public static String PREF_PLAY_MODE = "play_mode";

    public static String PLAY_FROM_POPULAR_MV_LIST = "PLAY_FROM_POPULAR_MV_LIST";
    public static String PLAY_FROM_POPULAR_SONG_LIST = "PLAY_FROM_POPULAR_SONG_LIST";
    public static String PLAY_FROM_PLAYLIST = "PLAY_FROM_PLAYLIST";
    public static String PLAY_FROM_SEARCH_RESULT = "PLAY_FROM_SEARCH_RESULT";

    public static String getServerURL( String relativePath )
    {
        if ( bReal )
            return "http://www.hereby.co.kr/karaoke/" + relativePath;
        else
            return "http://" + devIP + "/karaoke/" + relativePath;
    }
}
