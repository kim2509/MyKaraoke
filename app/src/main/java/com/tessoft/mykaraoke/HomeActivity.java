package com.tessoft.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import adapter.MainPagerAdapter;

public class HomeActivity extends BaseActivity{

    ViewPager pager = null;
    MainPagerAdapter pagerAdapter = null;
    PagerSlidingTabStrip tabs = null;

    int REQUEST_LOGIN = 1;
    int REQUEST_MAIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);

            // Initialize the ViewPager and set an adapter
            pager = (ViewPager) findViewById(R.id.pagerMain);
            pagerAdapter = new MainPagerAdapter( getSupportFragmentManager(), application);
            pager.setAdapter(pagerAdapter);

            // Bind the tabs to the ViewPager
            tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            tabs.setViewPager(pager);

            guestLogin();

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

            HashMap param = application.getDefaultHashMap();

            // 게스트로그인 한 경우만 조회
            if ( !Util.isEmptyForKey(param, "tempUserNo") ) {
                loadMainInfo();
            }

        } catch ( Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    private void loadMainInfo() throws Exception{
        String url = Constants.getServerURL("/playlist/mainInfo.do");
        HashMap param = application.getDefaultHashMap();
        new HttpPostAsyncTask( this, url, REQUEST_MAIN ).execute(param);
        findViewById(R.id.layoutProgress).setVisibility(ViewGroup.VISIBLE);
    }

    private void guestLogin() throws Exception
    {
        String url = Constants.getServerURL("/user/guestLogin.do");
        HashMap param = application.getDefaultHashMap();
        new HttpPostAsyncTask( this, url, REQUEST_LOGIN ).execute(param);
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

                    if ( Util.isEmptyForKey( application.getDefaultHashMap(), "tempUserNo") ) {

                        application.setMetaInfo("userInfo", mapper.writeValueAsString(data));

                        Intent intent = new Intent(Constants.BROADCAST_LOAD_MY_PLAYLIST);
                        sendBroadcast(intent);
                    }
                }
                else if ( requestCode == REQUEST_MAIN ){

//                    HashMap data = (HashMap) response.getData();

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
}
