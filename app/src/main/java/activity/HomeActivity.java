package activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tessoft.mykaraoke.APIResponse;
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.HttpPostAsyncTask;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import java.util.HashMap;

import adapter.MainPagerAdapter;

public class HomeActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    ViewPager pager = null;
    MainPagerAdapter pagerAdapter = null;
    PagerSlidingTabStrip tabs = null;

    int REQUEST_LOGIN = 1;
    int REQUEST_MAIN = 2;
    int selectedTabIndex = 0;

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
            tabs.setOnPageChangeListener(this);

            guestLogin();
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
        else if ( id == R.id.reload ) {
            Intent intent = null;
            if ( selectedTabIndex == 0 ) {
                intent = new Intent(Constants.BROADCAST_RELOAD_POPULAR_MV);
            } else if ( selectedTabIndex == 1 ) {
                intent = new Intent(Constants.BROADCAST_RELOAD_POPULAR_SONG);
            } else if ( selectedTabIndex == 3 ) {
                intent = new Intent(Constants.BROADCAST_RELOAD_POPULAR_PLAYLIST);
            } else if ( selectedTabIndex == 4 ) {
                intent = new Intent(Constants.BROADCAST_LOAD_MY_PLAYLIST);
            }
            sendBroadcast(intent);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectedTabIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
