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
    ListView popularList = null;
    public ArrayList<HashMap> playlistArray = new ArrayList<HashMap>();
    PlayListAdapter myListAdapter = null;
    PlayListAdapter popularListAdapter = null;

    int REQUEST_LOGIN = 1;
    int REQUEST_MAIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            myList = (ListView) findViewById(R.id.myList);
            popularList = (ListView) findViewById(R.id.listPopular);
            popularList.setOnItemClickListener(this);
            myList.setOnItemClickListener(this);

            myList.addHeaderView(getLayoutInflater().inflate(R.layout.list_header_my_playlist, null));

            guestLogin();

            myListAdapter = new PlayListAdapter(this, 0);
            popularListAdapter = new PlayListAdapter(this, 0);

            // 데이터 마이그레이션
            migrateData();
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            String url = Constants.getServerURL("/playlist/mainInfo.do");
            HashMap param = getDefaultHashMap();
            new HttpPostAsyncTask( this, url, REQUEST_MAIN ).execute(param);
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
            application.showToastMessage(ex.getMessage());
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
            String recentSongsV2 = application.getMetaInfoString("recentSearchSongsV2");
            if (TextUtils.isEmpty(recentSongsV2))
            {
                String recentSongs = application.getMetaInfoString("recentSearchSongs");

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

                    application.setMetaInfo("recentSearchSongsV2", songsV2.toString());
                }
            }
            else
            {
                recentSongsV2 = application.getMetaInfoString("recentSearchSongsV2");
                if ( !TextUtils.isEmpty( recentSongsV2 ) ){
                    ObjectMapper mapper = new ObjectMapper();
                    List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>(){});
                    if ( songs != null && songs.size() > 0 ) {
                        for ( int i = 0; i < songs.size(); i++ ){
                            HashMap song = songs.get(i);
                            //if ( Util.isEmptyForKey( song, "itemNo" ) )
                            song.put("itemNo", i + 1 );
                        }

                        application.setMetaInfo("recentSearchSongsV2", mapper.writeValueAsString(songs));
                    }
                }
            }
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex.getMessage());
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
        else if ( parent == popularList )
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
                    data.put("tempUserNo","4");
                    application.setMetaInfo("userInfo", mapper.writeValueAsString(data));
                }
                else if ( requestCode == REQUEST_MAIN ){

                    HashMap data = (HashMap) response.getData();

                    List<HashMap> playList = (List<HashMap>) data.get("myPlayList");

                    myListAdapter.clear();
                    myListAdapter.addAll(playList);
                    myList.setAdapter(myListAdapter);

                    List<HashMap> popularPlayList = (List<HashMap>) data.get("popularList");

                    popularListAdapter.clear();
                    popularListAdapter.addAll(popularPlayList);
                    popularList.setAdapter(popularListAdapter);
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

    public void showMyList(View v )
    {
        myList.setVisibility(ViewGroup.VISIBLE);
        popularList.setVisibility(ViewGroup.GONE);
    }

    public void showPopularList(View v)
    {
        myList.setVisibility(ViewGroup.GONE);
        popularList.setVisibility(ViewGroup.VISIBLE);
    }
}
