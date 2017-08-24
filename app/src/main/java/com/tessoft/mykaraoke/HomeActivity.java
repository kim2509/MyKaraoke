package com.tessoft.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends BaseActivity
        implements AdapterView.OnItemClickListener{

    ListView myList = null;
    public ArrayList<HashMap> playlistArray = new ArrayList<HashMap>();
    PlayListAdapter adapter = null;

    int REQUEST_LOGIN = 1;
    int REQUEST_MY_PLAYLIST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            myList = (ListView) findViewById(R.id.myList);
            myList.setOnItemClickListener(this);

            myList.addHeaderView(getLayoutInflater().inflate(R.layout.list_header_my_playlist, null));

            guestLogin();

            adapter = new PlayListAdapter(this, 0);

            // 데이터 마이그레이션
            migrateData();
        }
        catch( Exception ex )
        {
            showToastMessage(ex.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            String url = Constants.getServerURL("/playlist/myPlayList.do");
            HashMap param = getDefaultHashMap();
            new HttpPostAsyncTask( this, url, REQUEST_MY_PLAYLIST ).execute(param);
            findViewById(R.id.layoutProgress).setVisibility(ViewGroup.VISIBLE);

//            List<HashMap> playList = new ArrayList();
//            HashMap item = new HashMap();
//            item.put("playListNo","8");
//            item.put("Name","기본");
//            playList.add(item);

//            adapter.clear();
//            adapter.addAll(playList);
//            myList.setAdapter(adapter);
        } catch ( Exception ex ) {
            showToastMessage(ex.getMessage());
        }
    }

    private void guestLogin() throws Exception
    {
        String url = Constants.getServerURL("/user/guestLogin.do");
        HashMap param = getDefaultHashMap();
        new HttpPostAsyncTask( this, url, REQUEST_LOGIN ).execute( param );
        findViewById(R.id.marker_progress).setVisibility(ViewGroup.VISIBLE);
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
            else
            {
                recentSongsV2 = getMetaInfoString("recentSearchSongsV2");
                if ( !TextUtils.isEmpty( recentSongsV2 ) ){
                    ObjectMapper mapper = new ObjectMapper();
                    List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>(){});
                    if ( songs != null && songs.size() > 0 ) {
                        for ( int i = 0; i < songs.size(); i++ ){
                            HashMap song = songs.get(i);
                            //if ( Util.isEmptyForKey( song, "itemNo" ) )
                            song.put("itemNo", i + 1 );
                        }

                        setMetaInfo("recentSearchSongsV2", mapper.writeValueAsString( songs ));
                    }
                }
            }
        }
        catch( Exception ex )
        {
            showToastMessage(ex.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if ( parent == myList )
        {
            Intent intent = new Intent( this, PlayListMainActivity.class);
            PlayListViewHolder viewHolder = (PlayListViewHolder) view.getTag();
            intent.putExtra("playListNo", viewHolder.playListNo );
            intent.putExtra("playListName", viewHolder.txtName.getText().toString() );
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.goSetting) {

            Intent intent = new Intent( this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {

        try
        {
            findViewById(R.id.layoutProgress).setVisibility(ViewGroup.GONE);

            if ( Constants.FAIL.equals(result) )
            {
                showOKDialog("통신중 오류가 발생했습니다.\r\n다시 시도해 주십시오.", null);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

            if ( "0000".equals( response.getResCode() ) )
            {
                if ( requestCode == REQUEST_LOGIN ) {
                    HashMap data = (HashMap) response.getData();
                    data = (HashMap) data.get("tempUser");
                    setMetaInfo("userInfo", mapper.writeValueAsString(data) );
                }
                else if ( requestCode == REQUEST_MY_PLAYLIST ){

                    HashMap data = (HashMap) response.getData();
                    List<HashMap> playList = (List<HashMap>) data.get("playList");

                    adapter.clear();
                    adapter.addAll(playList);
                    myList.setAdapter(adapter);
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
            showToastMessage(ex.getMessage());
        }


    }
}
