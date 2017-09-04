package com.tessoft.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import adapter.SearchResultAdater;
import adapter.SearchResultViewHolder;

/**
 * Created by Daeyong on 2017-08-17.
 */
public class SearchResultActivity extends BaseActivity
    implements AdapterView.OnItemClickListener{

    String YoutubeApiKey = "AIzaSyD0WWUaXGrcaV7DFAkaf2zyr11-q-iPx4w";
    HashMap item = null;
    ListView listSearch = null;
    SearchResultAdater adapter = null;
    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            setContentView(R.layout.activity_search_result);
            listSearch = (ListView) findViewById(R.id.listSearch);
            adapter = new SearchResultAdater(this, 0);
            listSearch.setAdapter(adapter);
            listSearch.setOnItemClickListener(this);

            if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("playListItem"))
            {
                item = (HashMap) getIntent().getExtras().get("playListItem");
                String title = Util.getStringFromHash(item, "title");
                String singer = Util.getStringFromHash(item, "singer");
                title += " " + singer + " " + application.getMetaInfoString("example_text");

                String url = Constants.getServerURL("/playlist/searchSong.do");
                HashMap param = application.getDefaultHashMap();
                param.put("title", title );
                new HttpPostAsyncTask( this, url, 1 ).execute(param);
            }

        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadItem( String itemNo ) throws Exception {
        String recentSongsV2 = application.getMetaInfoString("recentSearchSongsV2");
        if ( !TextUtils.isEmpty(recentSongsV2) ){
            ObjectMapper mapper = new ObjectMapper();
            List<HashMap> songs = mapper.readValue(recentSongsV2, new TypeReference<List<HashMap>>() {
            });
            if ( songs != null && songs.size() > 0 ) {
                for ( int i = 0; i < songs.size(); i++ ){
                    HashMap song = songs.get(i);
                    if ( Util.getStringFromHash(song, "itemNo").equals( itemNo ) )
                        item = song;
                }
            }
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
                HashMap data = (HashMap) response.getData();
                List<HashMap> items = (List<HashMap>) data.get("items");

                adapter.clear();
                adapter.addAll( items );
                adapter.notifyDataSetChanged();

                if ( items != null && items.size() > 0 ) {
                    findViewById(R.id.layoutGuide).setVisibility(ViewGroup.VISIBLE);

                    TimerTask updateSecond = new UpdateSecond();
                    timer.scheduleAtFixedRate(updateSecond, 0, 1000);
//                    listSearch.setSelection(0);
//                    listSearch.getSelectedView().setSelected(true);
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
            Toast.makeText(this, result.toString(), Toast.LENGTH_LONG).show();
            finish();
            Intent intent = new Intent("PLAY_NEXT_SONG");
            sendBroadcast(intent);
        }
    }

    public void tick()
    {
        TextView txtSecond = (TextView) findViewById(R.id.txtSecond);
        int second = Integer.parseInt(txtSecond.getText().toString());

        if ( second == 1 ){
            go(adapter.getItem(0), true);
        } else {
            Message msg = new Message();
            msg.obj = String.valueOf( second -1 );
            h.sendMessage(msg);
        }
    }

    final Handler h = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            TextView txtSecond = (TextView) findViewById(R.id.txtSecond);
            txtSecond.setText( msg.obj.toString() );

            return false;
        }
    });

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchResultViewHolder viewHolder = (SearchResultViewHolder) view.getTag();
        go( viewHolder.item , true );
    }

    public void go( HashMap item, boolean bFinish ){

        timer.cancel();

        Intent intent = new Intent( this, FullscreenPlayerActivity.class);

        if ( getIntent() != null && getIntent().getExtras().get("playListItem") != null )
            intent.putExtra("playListItem", (HashMap) getIntent().getExtras().get("playListItem"));

        intent.putExtra("songItem", item);
        startActivity(intent);
        if ( bFinish )
            finish();
    }

    class UpdateSecond extends TimerTask {

        public void run() {
            tick();
        }
    }

    public void cancelTimer( View v ){
        timer.cancel();
        findViewById(R.id.layoutGuide).setVisibility(ViewGroup.GONE);
    }

    @Override
    public void finish() {
        super.finish();
        timer.cancel();
    }
}
