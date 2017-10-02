package activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tessoft.mykaraoke.APIResponse;
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.HttpPostAsyncTask;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import adapter.SongArrayAdapter;
import adapter.SongViewHolder;

public class PlayListMainActivity extends BaseActivity
        implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, OnClickListener
{

    ListView listRecentSearch = null;
    public static HashMap playListItem = null;
    public static List<HashMap> songList = null;
    SongArrayAdapter adapter = null;

    int REQUEST_SHARE_PLAYLIST = 1;
    int REQUEST_LOAD_PLAYLIST_SONG = 2;
    int REQUEST_DELETE_ITEM = 3;
    int REQUEST_UNSHARE_PLAYLIST = 4;
    int REQUEST_ADD_SONG_TO_PLAYLIST = 5;

    int REQUEST_CONFIRM_DELETE = 100;
    int REQUEST_CONFIRM_UNSHARE = 101;

    public static int sortMode = 0;

    public static int SORT_ALPHA_ASC = 1;
    public static int SORT_ALPHA_DESC = 2;
    public static int SORT_SHUFFLE = 3;
    public static int SORT_KARAOKE_PLAYCOUNT_DESC = 4;
    public static int SORT_MV_PLAYCOUNT_DESC = 5;

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

            registerForContextMenu(listRecentSearch);

            // broadcast 등록
            registerBroadcasts();

            // 파라미터 읽어와서 플레이리스트로딩
            loadIntentParameter();

            Spinner spinnerPlayMode = (Spinner) findViewById(R.id.spinnerPlayMode);
            spinnerPlayMode.setOnItemSelectedListener(this);

            loadPlayListSongs();

            if ( !"Y".equals( application.getMetaInfoString( Constants.GUIDE_DO_NOT_PLAY_MODE )))
                showGuideDialog();

            String tempUserNo = Util.getStringFromHash( playListItem, "tempUserNo");
            HashMap defaultMap = application.getDefaultHashMap();
            if ( Util.getStringFromHash( defaultMap, "tempUserNo").equals( tempUserNo )) {
                Button btn = (Button) findViewById(R.id.btnAddSong);
                btn.setVisibility(ViewGroup.VISIBLE);
                btn.setOnClickListener(this);
            } else {
                findViewById(R.id.btnAddSong).setVisibility(ViewGroup.GONE);
            }
        }
        catch(Exception ex )
        {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadIntentParameter() {
        if ( getIntent() != null && getIntent().getExtras().containsKey("playListItem") &&
                getIntent().getExtras().get("playListItem") != null ) {
            playListItem = (HashMap) getIntent().getExtras().get("playListItem");
            setTitle( Util.getStringFromHash(playListItem, "Name"));

            // 기존에 로딩된 곡 리스트와 선택한 재생목록이 다를 경우 리셋
            String playListNo = Util.getStringFromHash(playListItem, "playListNo");

            if (songList != null && songList.size() > 0) {
                for ( int i = 0; i < songList.size(); i++ ) {
                    if ( !playListNo.equals( Util.getStringFromHash( songList.get(i), "playListNo"))){
                        songList = null;
                        sortMode = 0;
                        break;
                    }
                }
            }
        }

        if ( songList != null && songList.size() > 0 ) {
            adapter.clear();
            adapter.addAll(songList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            // 현재 play mode 설정
            String playMode = application.getMetaInfoString(Constants.PREF_PLAY_MODE);
            Spinner spinnerPlayMode = (Spinner) findViewById(R.id.spinnerPlayMode);
            for ( int i = 0; i < spinnerPlayMode.getAdapter().getCount(); i++ ) {
                if ( playMode.equals( spinnerPlayMode.getAdapter().getItem(i) )) {
                    spinnerPlayMode.setSelection(i);
                }
            }

        } catch (Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.optionLayout).setVisibility(ViewGroup.GONE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            findViewById(R.id.optionLayout).setVisibility(ViewGroup.VISIBLE);
        }
    }

    private void registerBroadcasts() {
        registerReceiver(mMessageReceiver, new IntentFilter("PLAY_PREVIOUS_SONG"));
        registerReceiver(mMessageReceiver, new IntentFilter("PLAY_NEXT_SONG"));
        registerReceiver(mMessageReceiver, new IntentFilter("SHUFFLE_SONGS"));
        registerReceiver(mMessageReceiver, new IntentFilter("CHANGE_PLAY_MODE"));
    }

    private void loadSongList() throws Exception {
        String recentSongs = application.getMetaInfoString("recentSearchSongsV2");

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
            application.setMetaInfo("recentSearchSongsV2", recentSong.toString());

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
            SongViewHolder viewHolder = (SongViewHolder)view.getTag();
            selectedItemIndex = position;
            playSong(viewHolder.item);
            application.showToastMessage(viewHolder.txtSongTitle.getText().toString());
        }
        catch ( Exception ex )
        {
            application.showToastMessage(ex);
        }
    }

    public void showGuideDialog(){

        // custom dialog
        final Dialog dialog = new Dialog( this, R.style.noTitleTheme );
        dialog.setContentView(R.layout.dialog_playmode_guide);

        TextView txtGuide = (TextView) dialog.findViewById(R.id.txtGuide);
        if ( Constants.PLAY_MODE_ALL.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)) ||
                Constants.PLAY_MODE_MUSIC.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)) )
            txtGuide.setText("가요모드로 재생됩니다.\n\n하단 툴바에서 노래방으로 변경하실수 있습니다.");
        else
            txtGuide.setText("노래방모드로 재생됩니다.\n\n하단 툴바에서 가요모드로 변경하실수 있습니다.");

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    CheckBox chkShow = (CheckBox) dialog.findViewById(R.id.chkShow);
                    if ( chkShow.isChecked() )
                        application.setMetaInfo( Constants.GUIDE_DO_NOT_PLAY_MODE, "Y");
                    dialog.dismiss();

                } catch (Exception ex) {
                    application.showToastMessage(ex.getMessage());
                }
            }
        });

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        // if button is clicked, close the custom dialog
        dialogButtonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
                    listRecentSearch.getAdapter().getView(selectedItemIndex + 1, null, null),
                    selectedItemIndex + 1,
                    listRecentSearch.getAdapter().getItemId(selectedItemIndex + 1));
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

    private void playSong(HashMap item) {

        boolean bExistsVideoID = false;

        if (Constants.PLAY_MODE_ALL.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)) ||
                Constants.PLAY_MODE_MUSIC.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)) ) {
            if ( !Util.isEmptyForKey(item, "videoID2") ) bExistsVideoID = true;
        } else {
            if ( !Util.isEmptyForKey(item, "videoID1") ) bExistsVideoID = true;
        }

        if ( bExistsVideoID == false ) {

            Intent intent = new Intent(this, SearchResultActivity.class);
            intent.putExtra("item", item);
            intent.putExtra("playListItem", item);
            intent.putExtra("searchMode", application.getMetaInfoString(Constants.PREF_PLAY_MODE) );
            intent.putExtra("playFrom", Constants.PLAY_FROM_PLAYLIST );
            startActivity(intent);

        } else {
            Intent intent = new Intent( this, FullscreenPlayerActivity.class);
            intent.putExtra("playListItem", item);
            intent.putExtra("playFrom", Constants.PLAY_FROM_PLAYLIST );
            startActivity(intent);
        }

    }

    private void goToYoutube( String title ) throws Exception
    {
        String encodedTitle = URLEncoder.encode(title, "utf-8");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + encodedTitle + "+mr")));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        try {
            String tempUserNo = Util.getStringFromHash( playListItem, "tempUserNo");

            super.onCreateContextMenu(menu, v, menuInfo);
            menu.setHeaderTitle("Select The Action");
            menu.add(0, v.getId(), 0, "상세정보");//groupId, itemId, order, title

            if ( tempUserNo.equals( Util.getStringFromHash( application.getDefaultHashMap(), "tempUserNo"))) {
                menu.add(0, v.getId(), 0, "수정");//groupId, itemId, order, title
                menu.add(0, v.getId(), 0, "삭제");
            }
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SongViewHolder viewHolder = (SongViewHolder) info.targetView.getTag();

        if(item.getTitle()=="상세정보"){
            Intent intent = new Intent( this, ItemDetailActivity.class);
            intent.putExtra("playListItem", viewHolder.item);
            startActivity(intent);
        }
        else if(item.getTitle()=="수정"){
            Intent intent = new Intent( this, EditItemActivity.class);
            intent.putExtra("playListItem", viewHolder.item);
            startActivity(intent);
        }
        else if(item.getTitle()=="삭제"){
            showYesNoDialog("경고", "정말 삭제하시겠습니까?", REQUEST_CONFIRM_DELETE, viewHolder.item );
        }else{
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        try {

            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);

            HashMap defaultHashMap = (HashMap) application.getDefaultHashMap();
            String myTempUserNo = Util.getStringFromHash(defaultHashMap, "tempUserNo");
            if ( playListItem != null && myTempUserNo.equals( Util.getStringFromHash(playListItem,"tempUserNo") ) )
            {
                MenuItem item = menu.findItem(R.id.share);
                item.setVisible(true);
            }
            else {
                MenuItem item = menu.findItem(R.id.share);
                item.setVisible(false);
            }


        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        try {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = menuItem.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.goSearch) {

                Intent intent = new Intent(this, SearchActivity.class);
                if ( playListItem != null )
                    intent.putExtra("playListItem", playListItem );

                startActivity(intent);

                return true;
            } else if (id == R.id.share) {

                if (!"Y".equals( Util.getStringFromHash(playListItem, "shareYN")))
                    showShareDialog();
                else
                    showYesNoDialog("경고", "현재 해당목록은 공유된 상태입니다.\r\n공유를 해제하시겠습니까?", REQUEST_CONFIRM_UNSHARE, null);

                return true;
            } else if ( id == R.id.reload ) {
                loadPlayListSongs();
            }
        } catch ( Exception ex ) {
            application.showToastMessage(ex);
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private void showShareDialog() {
        try{

            // custom dialog
            final Dialog dialog = new Dialog( this, R.style.noTitleTheme );
            dialog.setContentView(R.layout.dialog_share);
            dialog.setTitle("공유");

            EditText edtPlayListName = (EditText) dialog.findViewById(R.id.edtPlayListName);
            edtPlayListName.setText(getTitle());

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        EditText edtPlayListName = (EditText) dialog.findViewById(R.id.edtPlayListName);

                        if ("기본".equals(edtPlayListName.getText().toString())) {
                            edtPlayListName.setError("다른 이름으로 지정해 주시기 바랍니다.");
                            return;
                        }

                        dialog.dismiss();
                        sharePlayList(edtPlayListName.getText().toString());
                    } catch (Exception ex) {
                        application.showToastMessage(ex.getMessage());
                    }
                }
            });

            Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
            // if button is clicked, close the custom dialog
            dialogButtonCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
        catch(Exception ex ){
            application.showToastMessage(ex.getMessage());
        }
    }

    public void sharePlayList( String playListName ) throws Exception
    {
        String url = Constants.getServerURL("/playlist/share.do");
        HashMap param = application.getDefaultHashMap();

        if ( playListItem != null )
            param.put("playListNo", Util.getStringFromHash(playListItem, "playListNo"));

        param.put("Name", playListName);
        param.put("shareYN", "Y");
        new HttpPostAsyncTask( this, url, REQUEST_SHARE_PLAYLIST ).execute(param);
        findViewById(R.id.layoutProgress).setVisibility(ViewGroup.VISIBLE);
    }

    public void loadPlayListSongs() throws Exception
    {
        String url = Constants.getServerURL("/playlist/playList.do");
        HashMap param = application.getDefaultHashMap();

        if ( playListItem != null )
            param.put("playListNo", Util.getStringFromHash(playListItem, "playListNo"));

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
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("item") && data.get("item") != null ) {
                        setTitle( Util.getStringFromHash( (HashMap) data.get("item"), "Name"));
                        playListItem = (HashMap) data.get("item");
                    }

                    showOKDialog("알림", "공유되었습니다.", null);
                }
                else if ( requestCode == REQUEST_LOAD_PLAYLIST_SONG ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("songList") && data.get("songList") != null ) {
                        songList = (List<HashMap>) data.get("songList");

                        if ( songList != null && songList.size() > 0) {
                            adapter.clear();
                            adapter.addAll(songList);
                            adapter.notifyDataSetChanged();
                            findViewById(R.id.txtEmpty).setVisibility(ViewGroup.GONE);
                            findViewById(R.id.listRecentSearch).setVisibility(ViewGroup.VISIBLE);

                            if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
                                findViewById(R.id.optionLayout).setVisibility(ViewGroup.VISIBLE);

                            // 정렬 초기화
                            sortMode = 0;
                            selectedItemIndex = 0;
                            Button btnSort = (Button) findViewById(R.id.btnSort);
                            btnSort.setText("정렬");
                            Button btnShuffle = (Button) findViewById(R.id.btnShuffle);
                            btnShuffle.setText("최근순");

                        } else {
                            findViewById(R.id.txtEmpty).setVisibility(ViewGroup.VISIBLE);
                            findViewById(R.id.listRecentSearch).setVisibility(ViewGroup.GONE);
                            findViewById(R.id.optionLayout).setVisibility(ViewGroup.GONE);
                        }

                    }
                } else if ( requestCode == REQUEST_UNSHARE_PLAYLIST) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("item") && data.get("item") != null ) {
                        playListItem = (HashMap) data.get("item");
                    }
                    showOKDialog("알림", "공유가 해제되었습니다.", null);
                } else if ( requestCode == REQUEST_ADD_SONG_TO_PLAYLIST ) {
                    loadPlayListSongs();
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

    public void shuffle( View v )
    {
        try
        {
            Button btnShuffle = (Button) findViewById(R.id.btnShuffle);

            if ( sortMode == 0 )
            {
                Collections.shuffle(songList);
                Collections.shuffle(songList);
                Collections.shuffle(songList);
                sortMode = SORT_SHUFFLE;
                btnShuffle.setText("임의대로");
            }
            else
            {
                loadPlayListSongs();
                sortMode = 0;
                btnShuffle.setText("최근순");
            }

            selectedItemIndex = 0;
            listRecentSearch.smoothScrollToPosition(0);

            adapter.clear();
            adapter.addAll(songList);
            adapter.notifyDataSetChanged();
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex.getMessage());
        }
    }

    public void sort( View v )
    {
        final CharSequence[] items = {"오름차순", "내림차순", "노래방재생순", "뮤비재생순"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("정렬 옵션을 선택해 주세요.");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                changeSort(item);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void changeSort( int sortOption ) {

        Button btnSort = (Button) findViewById(R.id.btnSort);

        if ( sortOption == 0 && sortMode != SORT_ALPHA_ASC ) {

            Collections.sort(songList, new Comparator<HashMap>() {
                @Override
                public int compare(HashMap lhs, HashMap rhs) {

                    String titleLeft = Util.getStringFromHash(lhs, "title");
                    String titleRight = Util.getStringFromHash(rhs, "title");

                    return titleLeft.compareTo(titleRight);
                }
            });

            sortMode = SORT_ALPHA_ASC;
            btnSort.setText("오름차순");

        } else if ( sortOption == 1 && sortMode != SORT_ALPHA_DESC) {

            Collections.sort(songList, new Comparator<HashMap>() {
                @Override
                public int compare(HashMap lhs, HashMap rhs) {

                    String titleLeft = Util.getStringFromHash(lhs, "title");
                    String titleRight = Util.getStringFromHash(rhs, "title");

                    return titleLeft.compareTo(titleRight) * -1;
                }
            });

            sortMode = SORT_ALPHA_DESC;
            btnSort.setText("내림차순");
        } else if ( sortOption == 2 && sortMode != SORT_KARAOKE_PLAYCOUNT_DESC ) {

            Collections.sort(songList, new Comparator<HashMap>() {
                @Override
                public int compare(HashMap lhs, HashMap rhs) {

                    String playCountLeft = Util.getStringFromHash(lhs, "playCount1");
                    String playCountRight = Util.getStringFromHash(rhs, "playCount1");

                    return playCountLeft.compareTo(playCountRight) * -1;
                }
            });

            sortMode = SORT_KARAOKE_PLAYCOUNT_DESC;
            btnSort.setText("노래방순");
        } else if ( sortOption == 3 && sortMode != SORT_MV_PLAYCOUNT_DESC) {

            Collections.sort(songList, new Comparator<HashMap>() {
                @Override
                public int compare(HashMap lhs, HashMap rhs) {

                    String playCountLeft = Util.getStringFromHash(lhs, "playCount2");
                    String playCountRight = Util.getStringFromHash(rhs, "playCount2");

                    return playCountLeft.compareTo(playCountRight) * -1;
                }
            });

            sortMode = SORT_MV_PLAYCOUNT_DESC;
            btnSort.setText("뮤비순");
        }

        adapter.clear();
        adapter.addAll(songList);
        adapter.notifyDataSetChanged();
    }

    public void playAll( View v ) {
        listRecentSearch.smoothScrollToPosition(0);

        listRecentSearch.performItemClick(
                listRecentSearch.getAdapter().getView(0, null, null),
                0,
                listRecentSearch.getAdapter().getItemId(0));
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
    public void yesClicked( int requestCode, Object param) {

        super.yesClicked(requestCode, param);

        try {

            if ( requestCode == REQUEST_CONFIRM_DELETE ) {

                String url = Constants.getServerURL("/playlist/deleteItem.do");
                HashMap postParam = (HashMap) param;
                postParam.put("tempUserNo", Util.getStringFromHash(application.getDefaultHashMap(), "tempUserNo"));

                new HttpPostAsyncTask( this, url, REQUEST_DELETE_ITEM).execute(postParam);

                songList.remove(param);
                adapter.clear();
                adapter.addAll(songList);
                adapter.notifyDataSetChanged();
            }
            else if ( requestCode == REQUEST_CONFIRM_UNSHARE ) {

                String url = Constants.getServerURL("/playlist/share.do");
                HashMap postParam = application.getDefaultHashMap();

                postParam.put("playListNo", Util.getStringFromHash(playListItem, "playListNo"));
                postParam.put("Name", Util.getStringFromHash(playListItem, "Name"));
                postParam.put("shareYN", "N");
                new HttpPostAsyncTask( this, url, REQUEST_UNSHARE_PLAYLIST ).execute(postParam);
            }

        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if ( view != null ) {
            TextView textView = (TextView) view;
            application.setMetaInfo(Constants.PREF_PLAY_MODE, textView.getText().toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        try {
            if ( v.getId() == R.id.btnAddSong ) {
                showAddSongDialog();
            }
            else if ( v.getId() == R.id.dialogButtonOK || v.getId() == R.id.dialogButtonAdd ) {

                Dialog dialog = (Dialog) v.getTag();
                EditText edtSongTitle = (EditText) dialog.findViewById(R.id.edtSongTitle);

                if (Util.isEmptyString(edtSongTitle.getText())) {
                    edtSongTitle.setError("노래제목을 입력해 주십시오.");
                    return;
                }

                EditText edtSinger = (EditText) dialog.findViewById(R.id.edtSinger);
                addSong(edtSongTitle.getText().toString(), edtSinger.getText().toString());

                edtSongTitle.setText("");
                edtSinger.setText("");

                if ( v.getId() == R.id.dialogButtonOK )
                    dialog.dismiss();
                else {
                    edtSongTitle.requestFocus();
                }

            }
        } catch ( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    public void showAddSongDialog(){

        // custom dialog
        final Dialog dialog = new Dialog( this, R.style.noTitleTheme );
        dialog.setContentView(R.layout.dialog_add_song);
        dialog.setCancelable(false);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        Button dialogButtonAdd = (Button) dialog.findViewById(R.id.dialogButtonAdd);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener( this );
        dialogButtonAdd.setOnClickListener(this);
        dialogButton.setTag(dialog);
        dialogButtonAdd.setTag( dialog );

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        // if button is clicked, close the custom dialog
        dialogButtonCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void addSong( String title, String singer ) throws Exception{

        String url = Constants.getServerURL("/playlistItem/add.do");
        HashMap param = application.getDefaultHashMap();

        param.put("playListNo", Util.getStringFromHash( playListItem, "playListNo"));
        param.put("title", title);
        param.put("singer", singer);

        new HttpPostAsyncTask( this, url, REQUEST_ADD_SONG_TO_PLAYLIST ).execute(param);

    }
}
