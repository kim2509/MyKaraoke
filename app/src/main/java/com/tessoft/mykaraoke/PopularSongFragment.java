package com.tessoft.mykaraoke;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;

import adapter.PlayListViewHolder;
import adapter.SongArrayAdapter;

/**
 * Created by Daeyong on 2016-04-18.
 */
public class PopularSongFragment extends BaseFragment implements AdapterView.OnItemClickListener{

    protected View rootView = null;
    ListView list = null;
    SongArrayAdapter adapter = null;

    int REQUEST_POPULAR_LIST = 1;

    // TODO: Rename and change types and number of parameters
    public static PopularSongFragment newInstance() {
        PopularSongFragment fragment = new PopularSongFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try
        {
            // Inflate the layout for this fragment
            if ( rootView == null )
            {
                rootView = inflater.inflate(R.layout.fragment_popular_song, container, false);
            }

            list = (ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(this);
            adapter = new SongArrayAdapter(getActivity(), 0);
            list.setAdapter(adapter);
        }
        catch( Exception ex )
        {
            application.showToastMessage(ex);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        try {

            super.onResume();
            loadPopularList();

        } catch( Exception ex ){
            application.showToastMessage(ex);
        }
    }

    private void loadPopularList() throws Exception {
        String url = Constants.getServerURL("/song/popularList.do");
        HashMap param = application.getDefaultHashMap();
        param.put("type", "1");
        new HttpPostAsyncTask( this, url, REQUEST_POPULAR_LIST ).execute(param);
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
                if ( requestCode == REQUEST_POPULAR_LIST ){
                    HashMap data = (HashMap) response.getData();
                    List<HashMap> popularList = (List<HashMap>) data.get("popularList");
                    adapter.clear();
                    adapter.addAll(popularList);
                    adapter.notifyDataSetChanged();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent( getActivity(), PlayListMainActivity.class);
        PlayListViewHolder viewHolder = (PlayListViewHolder) view.getTag();
        intent.putExtra("playListNo", viewHolder.playListNo );
        intent.putExtra("playListName", viewHolder.txtName.getText().toString() );
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
