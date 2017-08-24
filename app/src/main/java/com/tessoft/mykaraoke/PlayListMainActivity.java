package com.tessoft.mykaraoke;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayListMainActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, TextWatcher {

    ListView listRecentSearch = null;
    public static ArrayList<HashMap> songList = new ArrayList<HashMap>();
    SongArrayAdapter adapter = null;

    EditText txtSongTitle = null;

    int REQUEST_SHARE_PLAYLIST = 1;
    int REQUEST_LOAD_PLAYLIST_SONG = 2;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                if ( intent.getAction().equals("PLAY_NEXT_SONG"))
                    playNextSong();
                else if ( intent.getAction().equals("PLAY_PREVIOUS_SONG"))
                    playPreviousSong();
                else if ( intent.getAction().equals("SHUFFLE_SONGS"))
                    shuffle(null);
                else if ( intent.getAction().equals("CHANGE_PLAY_MODE"))
                    playAgain();
            }
            catch( Exception ex )
            {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_main);

            listRecentSearch = (ListView) findViewById(R.id.listRecentSearch);

            listRecentSearch.setOnItemClickListener(this);

            // 어댑터 준비
            adapter = new SongArrayAdapter(this, 0);
            listRecentSearch.setAdapter(adapter);
            listRecentSearch.setOnItemLongClickListener(this);

            // 노래 곡목 검색 기능 초기화
            initSearchText();

            registerForContextMenu(listRecentSearch);

            // broadcast 등록
            registerBroadcasts();

            // 재생목록 이름 설정
            if ( getIntent() != null && getIntent().getExtras().containsKey("playListName") &&
                    !Util.isEmptyString( getIntent().getExtras().get("playListName") ) )
                setTitle( getIntent().getExtras().getString("playListName"));
        }
        catch(Exception ex )
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            if ( bShuffle == false )
                songList.clear();

            // 노래목록 불러오기
            loadSongList();
            adapter.clear();
            adapter.addAll(songList);
            adapter.notifyDataSetChanged();

            /*
            loadPlayListSongs();
*/

        } catch (Exception ex ) {
            showToastMessage(ex.getMessage());
        }
    }

    private void registerBroadcasts() {
        registerReceiver(mMessageReceiver, new IntentFilter("PLAY_PREVIOUS_SONG"));
        registerReceiver(mMessageReceiver, new IntentFilter("PLAY_NEXT_SONG"));
        registerReceiver(mMessageReceiver, new IntentFilter("SHUFFLE_SONGS"));
        registerReceiver(mMessageReceiver, new IntentFilter("CHANGE_PLAY_MODE"));
    }

    private void initSearchText() {
        txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);
        txtSongTitle.addTextChangedListener( this );
    }

    private void loadSongList() throws Exception {
        String recentSongs = getMetaInfoString("recentSearchSongsV2");

        if ( songList.size() > 0 ) return;

        if ( recentSongs.length() > 0 ){
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> songs = mapper.readValue(recentSongs, new TypeReference<List<HashMap>>(){});

            //JSONArray songs = new JSONArray(recentSongs);
            for ( int i = 0; i < songs.size(); i++ )
            {
                songList.add( songs.get(i) );
            }
        }
    }

    public void searchOnYouTube( View view ) throws Exception
    {
        try {
            EditText edtSongTitle = (EditText) findViewById(R.id.txtSongTitle);

            HashMap obj = new HashMap();
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

    public static int selectedItemIndex = 0;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try
        {
            TextView txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
            TextView txtView = (TextView) view.findViewById(R.id.txtSongTitle);
            String title = txtView.getText().toString();

            selectedItemIndex = position;

            playSong(txtItemNo.getText().toString());

            showToastMessage(title);

        }
        catch ( Exception ex )
        {
        }
    }

    public void playPreviousSong(){

        if ( selectedItemIndex - 1 >= 0 ){

            listRecentSearch.smoothScrollToPosition(selectedItemIndex - 1);

            listRecentSearch.performItemClick(
                    listRecentSearch.getAdapter().getView(selectedItemIndex - 1, null, null),
                    selectedItemIndex - 1,
                    listRecentSearch.getAdapter().getItemId(selectedItemIndex - 1));
        }

    }

    public void playAgain(){
        listRecentSearch.smoothScrollToPosition(selectedItemIndex);
        listRecentSearch.performItemClick(
                listRecentSearch.getAdapter().getView(selectedItemIndex, null, null),
                selectedItemIndex,
                listRecentSearch.getAdapter().getItemId(selectedItemIndex));
    }

    public void playNextSong(){

        if ( selectedItemIndex + 1 < adapter.getCount() ){

            listRecentSearch.smoothScrollToPosition(selectedItemIndex + 1);

            listRecentSearch.performItemClick(
                    listRecentSearch.getAdapter().getView(selectedItemIndex+1, null, null),
                    selectedItemIndex + 1,
                    listRecentSearch.getAdapter().getItemId(selectedItemIndex+1));
        }
//        else
//        {
//            listRecentSearch.smoothScrollToPosition(0);
//
//            listRecentSearch.performItemClick(
//                    listRecentSearch.getAdapter().getView(0, null, null),
//                    0,
//                    listRecentSearch.getAdapter().getItemId(0));
//        }
    }

    private void playSong(String itemNo) {
    /*
    String recentSongs = getMetaInfoString("recentSearchSongsV2");

    int playCount = 0;

    if ( recentSongs.length() > 0 ){
        JSONArray songs = new JSONArray(recentSongs);
        JSONObject obj = null;

        for ( int i = 0; i < songs.length(); i++ ){
            obj = songs.getJSONObject(i);
            if ( title.equals( obj.getString("title")))
                break;
        }

        if ( !obj.has("playCount") )
            playCount = 0;
        else
            playCount = obj.getInt("playCount");

        obj.put("playCount", ++playCount);
        txtPlayCount.setText( String.valueOf( playCount ));
        setMetaInfo("recentSearchSongsV2", songs.toString());
    }
    */

        //goToYoutube(title);

        Intent intent = new Intent( this, SearchResultActivity.class);
        intent.putExtra("itemNo", itemNo);
        startActivity(intent);
    }

    private void goToYoutube( String title ) throws Exception
    {
        String encodedTitle = URLEncoder.encode(title, "utf-8");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + encodedTitle + "+mr")));
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


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select The Action");
        menu.add(0, v.getId(), 0, "상세정보");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "수정");//groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "삭제");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String itemNo = ((TextView) info.targetView.findViewById(R.id.txtItemNo)).getText().toString();
        String title = ((TextView) info.targetView.findViewById(R.id.txtSongTitle)).getText().toString();

        if(item.getTitle()=="상세정보"){
            Intent intent = new Intent( this, ItemDetailActivity.class);
            intent.putExtra("itemNo", itemNo);
            intent.putExtra("title", title);
            startActivity(intent);
        }
        else if(item.getTitle()=="수정"){
            Intent intent = new Intent( this, EditItemActivity.class);
            intent.putExtra("itemNo", itemNo);
            intent.putExtra("title", title);
            startActivity(intent);
        }
        else if(item.getTitle()=="삭제"){
            Toast.makeText(getApplicationContext(),"sending sms code",Toast.LENGTH_LONG).show();
        }else{
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {

            showShareDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        try{

            // custom dialog
            final Dialog dialog = new Dialog( this );
            dialog.setContentView(R.layout.dialog_share);
            dialog.setTitle("공유");

            if ( getIntent() != null && getIntent().getExtras() != null &&
                    getIntent().getExtras().containsKey("playListName") &&
                    !Util.isEmptyString( getIntent().getExtras().getString("playListName")))
            {
                EditText edtPlayListName = (EditText) dialog.findViewById(R.id.edtPlayListName);
                edtPlayListName.setText(getIntent().getExtras().getString("playListName"));
            }

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        EditText edtPlayListName = (EditText) dialog.findViewById(R.id.edtPlayListName);

                        if ("기본".equals( edtPlayListName.getText().toString() ))
                        {
                            edtPlayListName.setError("다른 이름으로 지정해 주시기 바랍니다.");
                            return;
                        }

                        dialog.dismiss();
                        sharePlayList();
                    } catch ( Exception ex ) {
                        showToastMessage(ex.getMessage());
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
        catch(Exception ex ){
            showToastMessage(ex.getMessage());
        }
    }

    public void sharePlayList() throws Exception
    {
        String url = Constants.getServerURL("/playlist/addSongs.do");
        HashMap param = getDefaultHashMap();

        if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("playListNo") &&
                !Util.isEmptyString( getIntent().getExtras().getString("playListNo")))
            param.put("playListNo", getIntent().getExtras().getString("playListNo"));

        param.put("songList", songList );
        new HttpPostAsyncTask( this, url, REQUEST_SHARE_PLAYLIST ).execute(param);
        findViewById(R.id.layoutProgress).setVisibility(ViewGroup.VISIBLE);
    }

    public void loadPlayListSongs() throws Exception
    {
        String url = Constants.getServerURL("/playlist/playList.do");
        HashMap param = getDefaultHashMap();

        if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("playListNo") &&
                !Util.isEmptyString( getIntent().getExtras().getString("playListNo")))
            param.put("playListNo", getIntent().getExtras().getString("playListNo"));

        new HttpPostAsyncTask( this, url, REQUEST_LOAD_PLAYLIST_SONG ).execute(param);
        findViewById(R.id.layoutProgress).setVisibility(ViewGroup.VISIBLE);
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
                if ( requestCode == REQUEST_SHARE_PLAYLIST ) {

                }
                else if ( requestCode == REQUEST_LOAD_PLAYLIST_SONG ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("songList") && data.get("songList") != null ) {
//                        List<HashMap> songList = (List<HashMap>) data.get("songList");
//                        adapter.clear();
//                        adapter.addAll(songList);
//                        adapter.notifyDataSetChanged();

//                        setMetaInfo("recentSearchSongsV2", mapper.writeValueAsString( songList ) );
//                        showToastMessage(mapper.writeValueAsString(songList));
                    }
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

    public static boolean bShuffle = false;
    public void shuffle( View v )
    {
        try
        {
            if ( bShuffle == false )
            {
                Collections.shuffle(songList);
                Collections.shuffle(songList);
                Collections.shuffle(songList);
            }
            else
            {
                songList.clear();
                loadSongList();
            }

            bShuffle = !bShuffle;

            selectedItemIndex = 0;
            listRecentSearch.smoothScrollToPosition(0);

            adapter.clear();
            adapter.addAll(songList);
            adapter.notifyDataSetChanged();
        }
        catch( Exception ex )
        {
            showToastMessage(ex.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        try
        {
            super.onDestroy();

            unregisterReceiver(mMessageReceiver);
        }
        catch( Exception ex )
        {
        }
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
            if (s.length() == 0) {
                adapter.clear();
                adapter.addAll(songList);
            } else {
                ArrayList<HashMap> tmp = new ArrayList<HashMap>();
                for (int i = 0; i < songList.size(); i++) {
                    HashMap obj = songList.get(i);
                    String title = Util.getStringFromHash(obj, "title");
                    if (title.replaceAll(" ", "").indexOf(s.toString().replaceAll(" ", "")) >= 0)
                        tmp.add(obj);
                }

                adapter.clear();
                adapter.addAll(tmp);
            }
            listRecentSearch.setAdapter(adapter);
        } catch (Exception ex) {

        }
    }
}