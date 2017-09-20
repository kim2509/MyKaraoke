package com.tessoft.mykaraoke;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LimitedAgeMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by Daeyong on 2017-08-25.
 */
public class KaraokeApplication extends Application {

    @Override
    public void onCreate() {
        try {
            super.onCreate();

            // 초기 환경변수 셋팅
            initializeGlobalPreferences();

            // Universal image loader 초기화
            initializeUniversalImageLoader();
        }
        catch( Exception ex ){

        }
    }

    private void initializeGlobalPreferences()
    {
        // 재생모드가 공백이거나 뮤직비디오일때 뮤직으로 셋팅
        if ( Util.isEmptyString( getMetaInfoString( Constants.PREF_PLAY_MODE )) ||
                Constants.PLAY_MODE_MUSIC.equals( getMetaInfoString( Constants.PREF_PLAY_MODE)))
            setMetaInfo( Constants.PREF_PLAY_MODE, Constants.PLAY_MODE_MUSIC);

//        setMetaInfo( Constants.AGREE_TERMS, "" );
//        setMetaInfo( Constants.GUIDE_DO_NOT_DATA_WARNING, "" );
//        setMetaInfo( Constants.GUIDE_DO_NOT_PLAY_MODE, "" );
//        setMetaInfo( Constants.GUIDE_DO_NOT_SHOW_BASIC_PLAYLIST_SAVE, "" );
    }

    private void initializeUniversalImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .delayBeforeLoading(100)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheExtraOptions(100, 100) // default = device screen dimensions
                .diskCacheExtraOptions(100, 100, null)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 2) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
//			.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCache(new LimitedAgeMemoryCache(new LruMemoryCache(2 * 1024 * 1024), 60 ))
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(13) // default
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .defaultDisplayImageOptions(options) // default
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void checkIfAdminUser()
    {
        try
        {
            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            //Get the text file
            File file = new File(sdcard,"karaoke.txt");

            if ( !file.exists() ) return;

            //Read text from file
            StringBuilder text = new StringBuilder();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();

            String loginInfo = text.toString();

            if ( Util.isEmptyString(loginInfo) ) return;

            String[] tokens = loginInfo.split("\\;");

            String server = "";
            String tempUserNo = "";

            for ( int i = 0; i < tokens.length; i++ )
            {
                String key = tokens[i].split("\\=")[0];
                String value = tokens[i].split("\\=")[1];
                if ( "server".equals( key ) )
                    server = value.trim();
                else if ( "tempUserNo".equals( key ) )
                    tempUserNo = value.trim();
                else if ( "devIP".equals( key ) )
                    Constants.devIP = value.trim();
            }

            if ( "REAL".equals( server ) )
                Constants.bReal = true;
            else if ( "DEV".equals( server ) )
                Constants.bReal = false;
            else
                Constants.bReal = true;

            if ( !Util.isEmptyString( tempUserNo ) ){
                HashMap userInfo = getUserInfoMap();
                userInfo.put("tempUserNo", tempUserNo);

                ObjectMapper mapper = new ObjectMapper();
                setMetaInfo("userInfo", mapper.writeValueAsString( userInfo ));
            }

        }
        catch( Exception ex )
        {
            showToastMessage(ex.getMessage());
        }
    }

    public String getMetaInfoString( String key )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if ( settings.contains(key) )
            return settings.getString(key, "");
        else return "";
    }

    public void setMetaInfo( String key, String value )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( key, value );
        editor.commit();
    }

    public void showToastMessage( String message )
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void showToastMessage( Exception ex )
    {
        if ( ex != null )
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getApplicationContext(), "ex is null", Toast.LENGTH_LONG).show();
    }

    public HashMap getUserInfoMap() throws Exception {
        String userInfoString = getMetaInfoString("userInfo");
        if (!Util.isEmptyString(userInfoString)) {
            ObjectMapper mapper = new ObjectMapper();
            HashMap userInfo = mapper.readValue(userInfoString, new TypeReference<HashMap>() {});
            return userInfo;
        }

        return new HashMap();
    }

    public HashMap getDefaultHashMap() throws Exception
    {
        HashMap param = getUserInfoMap();
        if ( param == null || param.size() == 0 )
        {
            param = new HashMap();
            param.put("OSVersion", getOSVersion());
            param.put("appVersion", getPackageVersion());
        }

        return param;
    }

    public String getOSVersion()
    {
        return Build.VERSION.RELEASE;
    }

    public String getPackageVersion()
    {
        PackageInfo pInfo;
        try {

            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }
}
