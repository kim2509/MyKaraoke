package com.tessoft.mykaraoke;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class EditItemActivity extends BaseActivity {

    HashMap item = null;

    int REQUEST_SONG_DETAIL = 1;
    int REQUEST_UPDATE_ITEM = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_item);

//            loadItemInfo();


        }
        catch( Exception ex )
        {

        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            if ( getIntent() != null && getIntent().getExtras() != null &&
                    getIntent().getExtras().get("playListItem") != null ) {

                HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

                String url = Constants.getServerURL("/playlistItem/detail.do");
                HashMap param = application.getDefaultHashMap();
                playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
                new HttpPostAsyncTask( this, url, REQUEST_SONG_DETAIL ).execute(playListItem);
            }

        } catch( Exception ex ) {

        }
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {
        try
        {
            if ( Constants.FAIL.equals(result) )
            {
                showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

            if ( "0000".equals( response.getResCode() ) )
            {
                if ( requestCode == REQUEST_SONG_DETAIL ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.get("itemInfo") != null ) {
                        displaySongInfo( (HashMap) data.get("itemInfo") );
                    }
                }
                else if ( requestCode == REQUEST_UPDATE_ITEM ) {
                    showOKDialog("수정되었습니다", null );
                }
            }
            else
            {
                showOKDialog("경고", response.getResMsg(), null);
                return;
            }
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex.getMessage());
        }
    }

    public void displaySongInfo( HashMap songInfo ){

        TextView txtItemNo2 = (TextView) findViewById(R.id.txtItemNo2);
        txtItemNo2.setText( Util.getStringFromHash(songInfo, "itemNo"));

        EditText edtSongTitle = (EditText) findViewById(R.id.edtSongTitle);
        edtSongTitle.setText( Util.getStringFromHash(songInfo, "title"));

        EditText edtSinger = (EditText) findViewById(R.id.edtSinger);
        edtSinger.setText( Util.getStringFromHash(songInfo, "singer"));

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

        if ( getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getExtras().get("playListItem") != null ) {

            EditText edtSongTitle = (EditText) findViewById(R.id.edtSongTitle);
            EditText edtSinger = (EditText) findViewById(R.id.edtSinger);

            HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

            String url = Constants.getServerURL("/playlist/updatePlayListItem.do");
            HashMap param = application.getDefaultHashMap();
            playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
            playListItem.put("title", edtSongTitle.getText().toString());
            playListItem.put("singer", edtSinger.getText().toString());

            new HttpPostAsyncTask( this, url, REQUEST_UPDATE_ITEM ).execute(playListItem);
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

                saveItem();

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
