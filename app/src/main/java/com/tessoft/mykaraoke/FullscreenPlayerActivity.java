package com.tessoft.mykaraoke;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.HashMap;

/**
 * Created by Daeyong on 2017-08-17.
 */
public class FullscreenPlayerActivity extends YouTubeBaseActivity
implements  com.google.android.youtube.player.YouTubePlayer.OnInitializedListener{

    public static final String API_KEY = "AIzaSyD0WWUaXGrcaV7DFAkaf2zyr11-q-iPx4w";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_fullscreen_player);

            // Initializing YouTube player view
            YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
            youTubePlayerView.initialize(API_KEY, this);

        } catch (Exception ex) {
            showToastMessage( ex.getMessage() );
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

        if ( getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("item")){
            HashMap item = (HashMap) getIntent().getExtras().get("item");
            if ( item != null ){
                HashMap idElement = (HashMap) item.get("id");
                String videoID = Util.getStringFromHash( idElement, "videoId");
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
                showToastMessage(arg0.toString() + " 다음곡을 재생합니다.");

                finish();
                Intent intent = new Intent("PLAY_NEXT_SONG");
                sendBroadcast(intent);
            }

            @Override
            public void onLoaded(String arg0) {
                youTubePlayer.play();
                duration = youTubePlayer.getDurationMillis();
            }

            @Override
            public void onLoading() {
            }

            @Override
            public void onVideoEnded() {

                int offset = player.getCurrentTimeMillis();
                showToastMessage( String.valueOf( offset ));

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

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        showToastMessage(youTubeInitializationResult.toString());

    }

    public void showToastMessage( String message )
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
            showToastMessage("다음곡부터 원래 순서대로 재생됩니다.");
        else
            showToastMessage("다음곡부터 임의대로 재생됩니다.");

        Intent intent = new Intent("SHUFFLE_SONGS");
        sendBroadcast(intent);
    }

    public void mode(View v)
    {
        final CharSequence[] items = {"뮤직비디오", "금영", "태진", "노래방", "음악방송"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("재생모드를 선택해 주세요.");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getApplicationContext(), items[item] + "모드로 전환합니다.", Toast.LENGTH_SHORT).show();
                setMetaInfo("example_text", items[item].toString());

                finish();
                Intent intent = new Intent("CHANGE_PLAY_MODE");
                sendBroadcast(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void setMetaInfo( String key, String value )
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString( key, value );
        editor.commit();
    }
}
