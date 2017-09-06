package com.tessoft.mykaraoke;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Daeyong on 2017-08-17.
 */
public class FullscreenPlayerActivity extends YouTubeBaseActivity
implements  com.google.android.youtube.player.YouTubePlayer.OnInitializedListener, TransactionDelegate{

    public static final String API_KEY = "AIzaSyD0WWUaXGrcaV7DFAkaf2zyr11-q-iPx4w";
    public KaraokeApplication application = null;
    int REQUEST_UPDATE_PLAYLIST_ITEM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_fullscreen_player);

            // Initializing YouTube player view
            YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
            youTubePlayerView.initialize(API_KEY, this);

            application = (KaraokeApplication) getApplication();

        } catch (Exception ex) {
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        LinearLayout layout = (LinearLayout) findViewById(R.id.toolBar);
        YouTubePlayerView playerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
            params2.addRule(RelativeLayout.LEFT_OF, R.id.toolBar);
            params2.addRule(RelativeLayout.ABOVE, 0);
            playerView.setLayoutParams(params2); //causes layout update

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.VERTICAL);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
            params2.addRule(RelativeLayout.LEFT_OF, 0);
            params2.addRule(RelativeLayout.ABOVE, R.id.toolBar);
            playerView.setLayoutParams(params2); //causes layout update

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layout.setLayoutParams(params);
            layout.setOrientation(LinearLayout.HORIZONTAL);

        }
    }

    int duration = 0;
    YouTubePlayer player = null;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {

        this.player = youTubePlayer;

        if ( getIntent() != null && getIntent().getExtras() != null ) {
            if ( getIntent().getExtras().get("songItem") != null ) {
                HashMap item = (HashMap) getIntent().getExtras().get("songItem");
                if ( item != null ){
                    HashMap idElement = (HashMap) item.get("id");
                    String videoID = Util.getStringFromHash( idElement, "videoId");
                    youTubePlayer.cueVideo(videoID);
                }
            }
            else if ( getIntent().getExtras().get("playListItem") != null ) {
                HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");
                String videoID = "";
                if ("뮤직비디오".equals( application.getMetaInfoString("play_mode")) ) {
                    videoID = Util.getStringFromHash(playListItem, "videoID2");
                } else {
                    videoID = Util.getStringFromHash(playListItem, "videoID1");
                }

                youTubePlayer.cueVideo(videoID);
            }
        }

        // Add listeners to YouTubePlayer instance
        youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
            @Override
            public void onAdStarted() {
            }

            @Override
            public void onError(YouTubePlayer.ErrorReason arg0) {
                application.showToastMessage(arg0.toString() + " 다음곡을 재생합니다.");

                finish();
                Intent intent = new Intent("PLAY_NEXT_SONG");
                sendBroadcast(intent);
            }

            @Override
            public void onLoaded(String arg0) {
                youTubePlayer.play();
                duration = youTubePlayer.getDurationMillis();

                TimerTask updateSecond = new UpdateSecond();
                timer.scheduleAtFixedRate(updateSecond, 0, 2000);
            }

            @Override
            public void onLoading() {
            }

            @Override
            public void onVideoEnded() {

                int offset = player.getCurrentTimeMillis();

                finish();
                Intent intent = new Intent("PLAY_NEXT_SONG");
                sendBroadcast(intent);

            }

            @Override
            public void onVideoStarted() {
            }
        });

        youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {
            }

            @Override
            public void onPaused() {
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onBuffering(boolean b) {
            }

            @Override
            public void onSeekTo(int i) {
            }
        });
    }

    Timer timer = new Timer();

    final Handler h = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            try {

                if ( msg.what == 1 ) {
                    Log.d("timer", "" + player.getCurrentTimeMillis());
                    if ( player.getCurrentTimeMillis() / 1000 > 90 ){
                        Log.d("timer", "90 seconds played. Causing timer cancel");

                        updateItemVideoID();

                        timer.cancel();
                    }
                }

            } catch (Exception ex ){
                application.showToastMessage(ex);
            }

        return false;
        }
    });

    class UpdateSecond extends TimerTask {

        public void run() {
            h.sendEmptyMessage(1);
        }
    }

    public void updateItemVideoID() throws Exception{
        if ( getIntent() != null && getIntent().getExtras() != null ) {

            HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

            if ( getIntent().getExtras().get("songItem") != null){
                HashMap songItem = (HashMap) getIntent().getExtras().get("songItem");
                HashMap idElement = (HashMap) songItem.get("id");
                String videoID = Util.getStringFromHash(idElement, "videoId");

                if ("뮤직비디오".equals( application.getMetaInfoString("play_mode")) ) {
                    playListItem.put("videoID2", videoID);
                } else {
                    playListItem.put("videoID1", videoID);
                }
            }

            if ("뮤직비디오".equals( application.getMetaInfoString("play_mode")) ) {
                playListItem.put("type", "2");
            } else {
                playListItem.put("type", "1");
            }

            String url = Constants.getServerURL("/playlist/updateItem.do");
            String tempUserNo = Util.getStringFromHash(application.getDefaultHashMap(), "tempUserNo");
            playListItem.put("tempUserNo", tempUserNo);

            new HttpPostAsyncTask( this, url, REQUEST_UPDATE_PLAYLIST_ITEM ).execute(playListItem);
        }
    }

    @Override
    public void doPostTransaction(int requestCode, Object result) {
        try
        {
            if ( Constants.FAIL.equals(result) )
            {
                Log.e("karaoke", "통신중 오류가 발생");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            APIResponse response = mapper.readValue(result.toString(), new TypeReference<APIResponse>(){});

            if ( "0000".equals( response.getResCode() ) )
            {
                HashMap data = (HashMap) response.getData();

                if ( requestCode == REQUEST_UPDATE_PLAYLIST_ITEM) {

                }
            }
            else
            {
                Log.e("karaoke", "http 응답오류 " + response.getResMsg());
                return;
            }
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        application.showToastMessage(youTubeInitializationResult.toString());
    }

    public void playPrevious(View v )
    {
        finish();
        Intent intent = new Intent("PLAY_PREVIOUS_SONG");
        sendBroadcast(intent);
    }

    public void playNext(View v )
    {
        finish();
        Intent intent = new Intent("PLAY_NEXT_SONG");
        sendBroadcast(intent);
    }

    public void playRew(View v )
    {
        int curSeek = player.getCurrentTimeMillis();

        if ( curSeek > 5000 )
            player.seekToMillis( curSeek - 5000 );
        else
            player.seekToMillis( 0 );
    }

    public void playFF(View v )
    {
        int curSeek = player.getCurrentTimeMillis();

        if ( curSeek + 5000 < duration )
            player.seekToMillis( curSeek + 5000 );
    }

    public void shuffle(View v )
    {
        if (PlayListMainActivity.sortMode == PlayListMainActivity.SORT_SHUFFLE )
            application.showToastMessage("다음곡부터 원래 순서대로 재생됩니다.");
        else
            application.showToastMessage("다음곡부터 임의대로 재생됩니다.");

        Intent intent = new Intent("SHUFFLE_SONGS");
        sendBroadcast(intent);
    }

    public void mode(View v)
    {
        final CharSequence[] items = {"뮤직비디오", "금영", "태진", "노래방"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("재생모드를 선택해 주세요.");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getApplicationContext(), items[item] + "모드로 전환합니다.", Toast.LENGTH_SHORT).show();
                application.setMetaInfo("play_mode", items[item].toString());

                finish();
                Intent intent = new Intent("CHANGE_PLAY_MODE");
                sendBroadcast(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void info( View v ) {
        try {

            if ( getIntent().getExtras().get("playListItem") != null ) {
                HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

                Intent intent = new Intent( this, ItemDetailActivity.class);
                intent.putExtra("playListItem", playListItem);
                startActivity(intent);
            }
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    public void finish() {
        super.finish();
        timer.cancel();
    }
}
