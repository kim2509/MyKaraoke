package com.tessoft.mykaraoke;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

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

/**
 * Created by Daeyong on 2017-08-25.
 */
public class KaraokeApplication extends Application {

    @Override
    public void onCreate() {
        try {
            super.onCreate();

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

            checkIfAdminUser();
        }
        catch( Exception ex ){

        }

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

            for ( int i = 0; i < tokens.length; i++ )
            {
                String key = tokens[i].split("\\=")[0];
                String value = tokens[i].split("\\=")[1];
                if ( "server".equals( key ) )
                    server = value.trim();
            }

            if ( "REAL".equals( server ) )
                Constants.bReal = true;
            else if ( "DEV".equals( server ) )
                Constants.bReal = false;
            else
                Constants.bReal = true;

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
}
