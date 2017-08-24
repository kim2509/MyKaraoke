package com.tessoft.mykaraoke;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;

public class EditItemActivity extends AppCompatActivity {

    HashMap item = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_item);

            loadItemInfo();

            actionBarSetup();
        }
        catch( Exception ex )
        {

        }
    }

    public void loadItemInfo() throws Exception
    {
        if ( getIntent() != null && getIntent().getExtras().containsKey("itemNo"))
        {
            String songNo = getIntent().getExtras().getString("itemNo");
            loadItem(songNo);

            if ( item != null )
            {
                TextView txtItemNo = (TextView) findViewById(R.id.txtItemNo2);
                txtItemNo.setText( Util.getStringFromHash(item,"itemNo"));

                EditText edtSongTitle = (EditText) findViewById(R.id.edtSongTitle);
                edtSongTitle.setText(Util.getStringFromHash(item, "title"));

                EditText edtSinger = (EditText) findViewById(R.id.edtSinger);
                edtSinger.setText(Util.getStringFromHash(item, "singer"));
            }
        }
    }

    /**
     * Sets the Action Bar for new Android versions.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getActionBar();
            if ( item != null ) {
                ab.setTitle(Util.getStringFromHash(item, "title"));

                getSupportActionBar().setTitle(Util.getStringFromHash(item, "title"));
            }
        }
    }

    private void loadItem( String songNo ) throws Exception {
        String recentSongsV2 = getMetaInfoString("recentSearchSongsV2");
        if ( !TextUtils.isEmpty(recentSongsV2) ){
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>() {
            });
            if ( songs != null && songs.size() > 0 ) {
                for ( int i = 0; i < songs.size(); i++ ){
                    HashMap song = songs.get(i);
                    if ( Util.getStringFromHash(song, "itemNo").equals( songNo ) )
                        item = song;
                }
            }
        }
    }

    private HashMap saveItem() throws Exception {
        String recentSongsV2 = getMetaInfoString("recentSearchSongsV2");
        if ( !TextUtils.isEmpty(recentSongsV2) ){
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>() {
            });
            if ( songs != null && songs.size() > 0 ) {
                for ( int i = 0; i < songs.size(); i++ ){
                    HashMap song = songs.get(i);
                    if ( Util.getStringFromHash(song, "itemNo").equals(
                        Util.getStringFromHash(item,"itemNo") ) )
                    {
                        songs.set(i, item);
                    }
                }
            }

            setMetaInfo("recentSearchSongsV2", mapper.writeValueAsString( songs ));
        }

        return null;
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
        editor.putString(key, value);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        try {
            //noinspection SimplifiableIfStatement
            if (id == R.id.save) {

                EditText edtSongTitle = (EditText) findViewById(R.id.edtSongTitle);
                EditText edtSinger = (EditText) findViewById(R.id.edtSinger);

                item.put("title", edtSongTitle.getText().toString() );
                item.put("singer", edtSinger.getText().toString() );

                saveItem();

                showToastMessage("저장되었습니다.");

                return true;
            }

        } catch( Exception ex ) {
            showToastMessage( ex.getMessage() );
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public void showToastMessage( String message )
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
