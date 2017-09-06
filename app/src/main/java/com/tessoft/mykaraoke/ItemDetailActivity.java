package com.tessoft.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class ItemDetailActivity extends BaseActivity {

    HashMap item = null;
    int REQUEST_SONG_DETAIL = 1;
    int REQUEST_UPDATE_ITEM = 2;

    int REQUEST_RESET_KARAOKE_LINK = 100;
    int REQUEST_RESET_MV_LINK = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            loadItem();

        } catch( Exception ex ) {

        }
    }

    private void loadItem() throws Exception {
        if ( getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getExtras().get("playListItem") != null ) {

            HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

            String url = Constants.getServerURL("/playlistItem/detail.do");
            HashMap param = application.getDefaultHashMap();
            playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
            new HttpPostAsyncTask( this, url, REQUEST_SONG_DETAIL ).execute(playListItem);
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
                        item = (HashMap) data.get("itemInfo");
                        displaySongInfo();
                    }
                }
                else if ( requestCode == REQUEST_UPDATE_ITEM ) {
                    showOKDialog("수정되었습니다", null );
                    loadItem();
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

    public void displaySongInfo(){

        setTitle(Util.getStringFromHash(item, "title"));

        TextView txtItemNo2 = (TextView) findViewById(R.id.txtItemNo2);
        txtItemNo2.setText( Util.getStringFromHash(item, "itemNo"));

        TextView txtSongTitle2 = (TextView) findViewById(R.id.txtSongTitle2);
        txtSongTitle2.setText( Util.getStringFromHash(item, "title"));

        TextView txtSinger2 = (TextView) findViewById(R.id.txtSinger2);
        txtSinger2.setText( Util.getStringFromHash(item, "singer"));

        String videoID1 = Util.getStringFromHash( item, "videoID1");
        String videoID2 = Util.getStringFromHash( item, "videoID2");

        TextView txtKaraokeLink2 = (TextView) findViewById(R.id.txtKaraokeLink2);
        txtKaraokeLink2.setText( videoID1 );
        if ( !Util.isEmptyString(videoID1)) {
            findViewById(R.id.btnResetKaraokeLink).setVisibility(ViewGroup.VISIBLE);
            findViewById(R.id.btnPlayKaraokeLink).setVisibility(ViewGroup.VISIBLE);
        }
        else {
            txtKaraokeLink2.setText("미설정");
            findViewById(R.id.btnResetKaraokeLink).setVisibility(ViewGroup.GONE);
            findViewById(R.id.btnPlayKaraokeLink).setVisibility(ViewGroup.GONE);
        }

        TextView txtMVLink2 = (TextView) findViewById(R.id.txtMVLink2);
        txtMVLink2.setText( videoID2 );
        if ( !Util.isEmptyString(videoID2)) {
            findViewById(R.id.btnResetMVLink).setVisibility(ViewGroup.VISIBLE);
            findViewById(R.id.btnPlayMVLink).setVisibility(ViewGroup.VISIBLE);
        }
        else {
            txtMVLink2.setText( "미설정" );
            findViewById(R.id.btnResetMVLink).setVisibility(ViewGroup.GONE);
            findViewById(R.id.btnPlayMVLink).setVisibility(ViewGroup.GONE);
        }
    }

    public void updateKaraokeLinkAsNull( View v ) {
        try {
            showYesNoDialog("확인", "정말 재설정하시겠습니까?", REQUEST_RESET_KARAOKE_LINK, null);
        } catch ( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    public void updateMVLinkAsNull( View v ) {
        try {
            showYesNoDialog("확인", "정말 재설정하시겠습니까?", REQUEST_RESET_MV_LINK, null);
        } catch ( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    public void yesClicked(int requestCode, Object param) {
        try {
            super.yesClicked(requestCode, param);

            if ( requestCode == REQUEST_RESET_KARAOKE_LINK )
                updateVideoIDAsNull("1");
            else if ( requestCode == REQUEST_RESET_MV_LINK )
                updateVideoIDAsNull("2");

        } catch ( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    private void updateVideoIDAsNull( String type ) throws Exception {
        if ( getIntent() != null && getIntent().getExtras() != null &&
                getIntent().getExtras().get("playListItem") != null ) {

            HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");
            String url = Constants.getServerURL("/playlistItem/updateVideoIDAsNull.do");
            HashMap param = application.getDefaultHashMap();
            playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
            playListItem.put("type", type);
            new HttpPostAsyncTask( this, url, REQUEST_UPDATE_ITEM ).execute(playListItem);
        }
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
            if (id == R.id.menu_edit) {

                if ( getIntent() != null && getIntent().getExtras() != null &&
                        getIntent().getExtras().get("playListItem") != null ) {

                    HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");
                    Intent intent = new Intent( this, EditItemActivity.class);
                    intent.putExtra("playListItem", playListItem);
                    startActivity(intent);

                }

                return true;
            }

        } catch( Exception ex ) {
            application.showToastMessage( ex.getMessage() );
        }

        return super.onOptionsItemSelected(menuItem);
    }
}
