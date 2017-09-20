package fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

import activity.FullscreenPlayerActivity;
import activity.SearchResultActivity;
import adapter.PopularItemAdapter;
import adapter.PopularItemViewHolder;

/**
 * Created by Daeyong on 2016-04-18.
 */
public class SongSearchFragment extends BaseFragment
        implements AdapterView.OnItemClickListener, TextWatcher, View.OnClickListener, AdapterView.OnItemSelectedListener {

    protected View rootView = null;
    EditText txtSongTitle = null;
    ListView list = null;
    PopularItemAdapter adapter = null;

    int REQUEST_SEARCH_SONG = 1;
    int REQUEST_ADD_SONG_TO_PLAYLIST = 2;

    int selectedItemIndex = 0;

    // TODO: Rename and change types and number of parameters
    public static SongSearchFragment newInstance() {
        SongSearchFragment fragment = new SongSearchFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Constants.BROADCAST_PLAY_NEXT_POPULAR_SONG));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {

            }
            catch( Exception ex )
            {
                application.showToastMessage(ex);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try
        {
            // Inflate the layout for this fragment
            if ( rootView == null )
            {
                rootView = inflater.inflate(R.layout.fragment_search, container, false);
            }


            list = (ListView) rootView.findViewById(R.id.listSearch);
            list.setOnItemClickListener(this);
            adapter = new PopularItemAdapter(getActivity(), 0);
            list.setAdapter(adapter);

            // 노래 곡목 검색 기능 초기화
            initSearchText();

            Button btnSearch = (Button) rootView.findViewById(R.id.btnSearch);
            btnSearch.setOnClickListener(this);

            Spinner spinnerPlayMode = (Spinner) rootView.findViewById(R.id.spinnerPlayMode);
            spinnerPlayMode.setOnItemSelectedListener(this);
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex);
        }

        return rootView;
    }

    private void initSearchText() {
        txtSongTitle = (EditText) rootView.findViewById(R.id.txtSongTitle);
        txtSongTitle.addTextChangedListener(this);
    }

    @Override
    public void onResume() {
        try {

            super.onResume();

            // 현재 play mode 설정
            String playMode = application.getMetaInfoString(Constants.PREF_PLAY_MODE);
            Spinner spinnerPlayMode = (Spinner) rootView.findViewById(R.id.spinnerPlayMode);
            for ( int i = 0; i < spinnerPlayMode.getAdapter().getCount(); i++ ) {
                if ( playMode.equals( spinnerPlayMode.getAdapter().getItem(i) )) {
                    spinnerPlayMode.setSelection(i);
                }
            }

        } catch( Exception ex ){
            application.showToastMessage(ex);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        PopularItemViewHolder viewHolder = (PopularItemViewHolder) view.getTag();

        Intent intent = new Intent( getActivity(), FullscreenPlayerActivity.class);

        intent.putExtra("songItem", viewHolder.item );
        intent.putExtra("playFrom", Constants.PLAY_FROM_SEARCH_FRAGMENT );

        selectedItemIndex = position;

        startActivity(intent);

    }

    public void playNext() {
        if ( selectedItemIndex + 1 < adapter.getCount() ){

            list.smoothScrollToPosition(selectedItemIndex + 1);

            list.performItemClick(
                    list.getAdapter().getView(selectedItemIndex+1, null, null),
                    selectedItemIndex + 1,
                    list.getAdapter().getItemId(selectedItemIndex+1));
        }
    }

    public void playPreviousSong(){

        if ( selectedItemIndex - 1 >= 0 ){

            list.smoothScrollToPosition(selectedItemIndex - 1);

            list.performItemClick(
                    list.getAdapter().getView(selectedItemIndex - 1, null, null),
                    selectedItemIndex - 1,
                    list.getAdapter().getItemId(selectedItemIndex - 1));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mMessageReceiver);
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

            searchSong(s.toString());

        } catch ( Exception ex ) {
            application.showToastMessage(ex.getMessage());
        }
    }

    private void searchSong( String keyword ) throws Exception {

        if ( !Util.isEmptyString(keyword) ) {
            String url = Constants.getServerURL("/song/search.do");
            HashMap param = application.getDefaultHashMap();
            param.put("keyword", keyword );
            if ( application.getMetaInfoString(Constants.PREF_PLAY_MODE).equals(Constants.PLAY_MODE_MUSIC)){
                param.put("type","2");
            } else {
                param.put("type","1");
            }

            new HttpPostAsyncTask( this, url, REQUEST_SEARCH_SONG ).execute(param);
        }
        else{
            adapter.clear();
            adapter.notifyDataSetChanged();
            list.setVisibility(ViewGroup.GONE);
            rootView.findViewById(R.id.txtEmptyGuide).setVisibility(ViewGroup.GONE);
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
                if ( requestCode == REQUEST_SEARCH_SONG ){
                    HashMap data = (HashMap) response.getData();
                    if ( data.containsKey("songList") && data.get("songList") != null ) {
                        List<HashMap> songList = (List<HashMap>) data.get("songList");
                        adapter.clear();
                        adapter.addAll(songList);
                        adapter.notifyDataSetChanged();

                        if ( songList.size() > 0 ){
                            rootView.findViewById(R.id.listSearch).setVisibility(ViewGroup.VISIBLE);
                            rootView.findViewById(R.id.txtEmptyGuide).setVisibility(ViewGroup.GONE);
                        } else {
                            rootView.findViewById(R.id.listSearch).setVisibility(ViewGroup.GONE);
                            rootView.findViewById(R.id.txtEmptyGuide).setVisibility(ViewGroup.VISIBLE);
                        }
                    }
                }
                else if ( requestCode == REQUEST_ADD_SONG_TO_PLAYLIST ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.get("song") != null ) {
                        HashMap item = (HashMap) data.get("song");
                        Intent intent = new Intent(getActivity(), SearchResultActivity.class);
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
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex.getMessage());
        }

    }

    private void addSong(String title, String singer) throws Exception{

        if (!Util.isEmptyString(title)) {

            String url = Constants.getServerURL("/playlistItem/add.do");
            HashMap param = application.getDefaultHashMap();

            param.put("title", title);
            param.put("singer", singer);

            new HttpPostAsyncTask( this, url, REQUEST_ADD_SONG_TO_PLAYLIST ).execute(param);
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if ( v.getId() == R.id.btnSearch && txtSongTitle != null ) {

                EditText edtSinger = (EditText) rootView.findViewById(R.id.edtSinger);

                if ( !"Y".equals( application.getMetaInfoString( Constants.AGREE_TERMS )))
                    showGuideDialog( edtSinger.getText().toString());
                else
                {
                    String keyword = txtSongTitle.getText().toString();
                    addSong(keyword, edtSinger.getText().toString());
                }
            }
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    public void showGuideDialog(final String singer){

        // custom dialog
        final Dialog dialog = new Dialog( getActivity(), R.style.noTitleTheme );
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_terms_agree);

        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    CheckBox chkShow = (CheckBox) dialog.findViewById(R.id.chkShow);
                    if (chkShow.isChecked()) {
                        application.setMetaInfo(Constants.AGREE_TERMS, "Y");
                        dialog.dismiss();
                        String keyword = txtSongTitle.getText().toString();
                        addSong(keyword, singer);
                    } else {
                        showOKDialog("경고", "약관에 동의하여 주시기 바랍니다.", null );
                    }

                } catch (Exception ex) {
                    application.showToastMessage(ex.getMessage());
                }
            }
        });

        TextView txtTermsDetail = (TextView) dialog.findViewById(R.id.txtTermsDetail);
        txtTermsDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String url = Constants.getServerURL("/user/terms.do");
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try{
            if ( view != null ) {
                TextView textView = (TextView) view;
                application.setMetaInfo( Constants.PREF_PLAY_MODE, textView.getText().toString() );
            }

            if ( txtSongTitle != null )
                searchSong( txtSongTitle.getText().toString());
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
