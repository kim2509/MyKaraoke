package com.tessoft.mykaraoke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    ListView listRecentSearch = null;
    ArrayList<JSONObject> songList = new ArrayList<JSONObject>();
    SongArrayAdapter adapter = null;

    EditText txtSongTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_main);

            listRecentSearch = (ListView) findViewById(R.id.listRecentSearch);

            listRecentSearch.setOnItemClickListener(this);

            migrateData();

            String recentSongs = getMetaInfoString("recentSearchSongsV2");

            if ( recentSongs.length() > 0 ){
                JSONArray songs = new JSONArray(recentSongs);
                for ( int i = 0; i < songs.length(); i++ )
                {
                    songList.add( songs.getJSONObject(i) );
                }
            }

            // 어댑터 준비

            adapter = new SongArrayAdapter(this, 0);
            adapter.addAll(songList);
            listRecentSearch.setAdapter(adapter);
            listRecentSearch.setOnItemLongClickListener(this);


            txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);
            txtSongTitle.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {

                    try
                    {
                        if ( s.length() == 0 ){
                            adapter.clear();
                            adapter.addAll(songList);
                        }
                        else
                        {
                            ArrayList<JSONObject> tmp = new ArrayList<JSONObject>();
                            for ( int i = 0; i < songList.size(); i++ )
                            {
                                JSONObject obj = songList.get(i);
                                String title = obj.getString("title");
                                if ( title.replaceAll(" ", "").indexOf( s.toString().replaceAll(" ", "") ) >= 0 )
                                    tmp.add(obj);
                            }

                            adapter.clear();
                            adapter.addAll(tmp);
                        }
                        listRecentSearch.setAdapter(adapter);
                    }
                    catch( Exception ex )
                    {

                    }
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }
        catch(Exception ex )
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void migrateData()
    {
        try
        {
            String recentSongsV2 = getMetaInfoString("recentSearchSongsV2");
            if (TextUtils.isEmpty(recentSongsV2))
            {
                String recentSongs = getMetaInfoString("recentSearchSongs");

                if ( !TextUtils.isEmpty( recentSongs ) )
                {
                    JSONArray songs = new JSONArray(recentSongs);
                    JSONArray songsV2 = new JSONArray();
                    for ( int i = 0; i < songs.length(); i++ )
                    {
                        JSONObject obj = new JSONObject();
                        obj.put("title", songs.getString(i));
                        songsV2.put(obj);
                    }

                    setMetaInfo("recentSearchSongsV2", songsV2.toString());
                }
            }
        }
        catch( Exception ex )
        {

        }
    }

    public void searchOnYouTube( View view ) throws Exception
    {
        try {
            EditText edtSongTitle = (EditText) findViewById(R.id.txtSongTitle);

            JSONObject obj = new JSONObject();
            obj.put("title", edtSongTitle.getText().toString() );
            songList.add(0, obj );
            adapter.notifyDataSetChanged();

            JSONArray recentSong = new JSONArray();
            for ( int i = 0; i < songList.size(); i++ ) {
                recentSong.put(songList.get(i));
            }
            setMetaInfo("recentSearchSongsV2", recentSong.toString() );

            goToYoutube(edtSongTitle.getText().toString());
            edtSongTitle.setText("");
        }
        catch( Exception ex )
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try
        {
            TextView txtView = (TextView) view.findViewById(R.id.txtSongTitle);
            String title = txtView.getText().toString();

            goToYoutube(title);
        }
        catch ( Exception ex )
        {
        }
    }

    private void goToYoutube( String title ) throws Exception
    {
        String encodedTitle = URLEncoder.encode( title, "utf-8");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + encodedTitle +"+mr")));
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( key, value );
        editor.commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        try {


        }
        catch( Exception ex )
        {

        }


        return false;
    }
}
