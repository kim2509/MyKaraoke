package activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tessoft.mykaraoke.APIResponse;
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.HttpPostAsyncTask;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import java.util.HashMap;
import java.util.List;

import adapter.SearchResultViewHolder;
import adapter.SearchSongAdapter;

public class SearchActivity extends BaseActivity
    implements TextWatcher, AdapterView.OnItemClickListener, TextView.OnEditorActionListener{

    EditText txtSongTitle = null;
    int REQUEST_SEARCH_MY_SONG = 1;
    int REQUEST_ADD_SONG_TO_PLAYLIST = 2;
    ListView listSearch = null;
    SearchSongAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listSearch = (ListView) findViewById(R.id.listSearch);
        adapter = new SearchSongAdapter(this, 0);
        listSearch.setAdapter(adapter);
        listSearch.setOnItemClickListener(this);

        // 노래 곡목 검색 기능 초기화
        initSearchText();
    }

    private void initSearchText() {
        txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);
        txtSongTitle.addTextChangedListener(this);
        txtSongTitle.setOnEditorActionListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        try {

            searchSongInPlayList(s.toString());

        } catch ( Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            EditText txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);
            searchSongInPlayList( txtSongTitle.getText().toString() );

        } catch ( Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    private void searchSongInPlayList( String keyword ) throws Exception {

        if ( !Util.isEmptyString(keyword) ) {

            String url = Constants.getServerURL("/playlist/searchMySong.do");
            HashMap param = application.getDefaultHashMap();

            if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("playListNo") &&
                    !Util.isEmptyString( getIntent().getExtras().getString("playListNo")))
                param.put("playListNo", getIntent().getExtras().getString("playListNo"));

            param.put("keyword", keyword );

            new HttpPostAsyncTask( this, url, REQUEST_SEARCH_MY_SONG ).execute(param);

        }
        else{
            adapter.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {
        try {

            super.doPostTransaction(requestCode, result);

            ObjectMapper mapper = new ObjectMapper();
            APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>() {
            });

            if ( "0000".equals( response.getResCode() ) )
            {
                if ( requestCode == REQUEST_SEARCH_MY_SONG ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("songList") && data.get("songList") != null ) {
                        List<HashMap> songList = (List<HashMap>) data.get("songList");
                        adapter.clear();
                        adapter.addAll(songList);
                        adapter.notifyDataSetChanged();

                        if ( songList.size() > 0 ){
                            findViewById(R.id.listSearch).setVisibility(ViewGroup.VISIBLE);
                            findViewById(R.id.txtEmptyGuide).setVisibility(ViewGroup.GONE);
                        } else {
                            findViewById(R.id.listSearch).setVisibility(ViewGroup.GONE);
                            findViewById(R.id.txtEmptyGuide).setVisibility(ViewGroup.VISIBLE);
                        }
                    }
                }
                else if ( requestCode == REQUEST_ADD_SONG_TO_PLAYLIST ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.get("song") != null ) {
                        HashMap item = (HashMap) data.get("song");
                        Intent intent = new Intent(this, SearchResultActivity.class);
                        intent.putExtra("item", item);
                        startActivity(intent);
                    }
                }
            }
            else
            {
                showOKDialog("경고", response.getResMsg(), null);
                return;
            }

        } catch( Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try
        {
            SearchResultViewHolder viewHolder = (SearchResultViewHolder)view.getTag();

            playSong(viewHolder.item);

            application.showToastMessage(viewHolder.txtTitle.getText().toString());
        }
        catch ( Exception ex )
        {
            application.showToastMessage(ex);
        }
    }

    private void playSong(HashMap item) {

        boolean bExistsVideoID = false;

        if ("뮤직비디오".equals( application.getMetaInfoString(Constants.PREF_PLAY_MODE)) ) {
            if ( !Util.isEmptyForKey(item, "videoID2") ) bExistsVideoID = true;
        } else {
            if ( !Util.isEmptyForKey(item, "videoID1") ) bExistsVideoID = true;
        }

        if ( bExistsVideoID == false ) {

            Intent intent = new Intent(this, SearchResultActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);

        } else {
            Intent intent = new Intent( this, FullscreenPlayerActivity.class);
            intent.putExtra("playListItem", item);
            intent.putExtra("playFrom", Constants.PLAY_FROM_PLAYLIST );
            startActivity(intent);
        }

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        return false;
    }

    private void addSong(String title) throws Exception{

        if (!Util.isEmptyString(title)) {

            String url = Constants.getServerURL("/playlistItem/add.do");
            HashMap param = application.getDefaultHashMap();

            if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("playListNo") &&
                    !Util.isEmptyString( getIntent().getExtras().getString("playListNo")))
                param.put("playListNo", getIntent().getExtras().getString("playListNo"));

            param.put("title", title);

            new HttpPostAsyncTask( this, url, REQUEST_ADD_SONG_TO_PLAYLIST ).execute(param);
        }
    }

    public void searchKeyword( View v ) {

        try {

            EditText txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);

            String title = txtSongTitle.getText().toString();

            if ( !"Y".equals( application.getMetaInfoString( Constants.GUIDE_DO_NOT_SHOW_BASIC_PLAYLIST_SAVE )))
                showGuideDialog(title);
            else
                addSong(title);

        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    public void showGuideDialog(final String title){

        // custom dialog
        final Dialog dialog = new Dialog( this );
        dialog.setContentView(R.layout.dialog_playlist_basic_guide);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    CheckBox chkShow = (CheckBox) dialog.findViewById(R.id.chkShow);
                    if ( chkShow.isChecked() )
                        application.setMetaInfo( Constants.GUIDE_DO_NOT_SHOW_BASIC_PLAYLIST_SAVE, "Y");
                    dialog.dismiss();

                    addSong(title);

                } catch (Exception ex) {
                    application.showToastMessage(ex.getMessage());
                }
            }
        });

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        // if button is clicked, close the custom dialog
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
