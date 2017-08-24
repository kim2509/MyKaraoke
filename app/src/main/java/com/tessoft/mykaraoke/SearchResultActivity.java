package com.tessoft.mykaraoke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Daeyong on 2017-08-17.
 */
public class SearchResultActivity extends AppCompatActivity
    implements TransactionDelegate{

    String YoutubeApiKey = "AIzaSyD0WWUaXGrcaV7DFAkaf2zyr11-q-iPx4w";
    HashMap item = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            setContentView(R.layout.activity_search_result);

            if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("itemNo"))
            {
                String itemNo = getIntent().getExtras().getString("itemNo");
                loadItem(itemNo);
                String title = Util.getStringFromHash(item, "title");
                String singer = Util.getStringFromHash(item, "singer");
                title += " " + singer + " " + getMetaInfoString("example_text");

                title = URLEncoder.encode( title );
                String url = "https://www.googleapis.com/youtube/v3/search?part=id,snippet&q=" + title +
                        "&type=video&key=" + YoutubeApiKey;
                new HttpAsyncTask( this, url, 1 ).execute();
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadItem( String itemNo ) throws Exception {
        String recentSongsV2 = getMetaInfoString("recentSearchSongsV2");
        if ( !TextUtils.isEmpty(recentSongsV2) ){
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>() {
            });
            if ( songs != null && songs.size() > 0 ) {
                for ( int i = 0; i < songs.size(); i++ ){
                    HashMap song = songs.get(i);
                    if ( Util.getStringFromHash(song, "itemNo").equals( itemNo ) )
                        item = song;
                }
            }
        }
    }

    public String getMetaInfoString( String key )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if ( settings.contains(key) )
            return settings.getString(key, "");
        else return "";
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {

        try
        {
            ObjectMapper mapper = new ObjectMapper();

            HashMap param = mapper.readValue(result.toString(), new TypeReference<HashMap>(){});

            List<HashMap> items = (List<HashMap>) param.get("items");

            for ( int i = 0; i < items.size(); i++ )
            {
                HashMap idObj = (HashMap) items.get(i).get("id");
                String videoID = idObj.get("videoId").toString();

                Intent intent = new Intent( this, FullscreenPlayerActivity.class);
                intent.putExtra("videoID", videoID);
                startActivity(intent);
                finish();

                break;
            }


        }
        catch( Exception ex )
        {
            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent("PLAY_NEXT_SONG");
            sendBroadcast(intent);
        }


    }
}
