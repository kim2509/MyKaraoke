package com.tessoft.mykaraoke;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import adapter.PopularItemAdapter;
import adapter.PopularItemViewHolder;

/**
 * Created by Daeyong on 2016-04-18.
 */
public class PopularMVFragment extends BaseFragment implements AdapterView.OnItemClickListener{

    protected View rootView = null;
    ListView list = null;
    PopularItemAdapter adapter = null;

    int REQUEST_POPULAR_LIST = 1;
    int selectedItemIndex = 0;

    // TODO: Rename and change types and number of parameters
    public static PopularMVFragment newInstance() {
        PopularMVFragment fragment = new PopularMVFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Constants.BROADCAST_PLAY_NEXT_MV));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                if ( intent.getAction().equals(Constants.BROADCAST_PLAY_NEXT_MV))
                    playNext();
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
                rootView = inflater.inflate(R.layout.fragment_popular_song, container, false);
            }

            list = (ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(this);
            adapter = new PopularItemAdapter(getActivity(), 0);
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
        param.put("type", "2");
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

        PopularItemViewHolder viewHolder = (PopularItemViewHolder) view.getTag();

        Intent intent = new Intent( getActivity(), FullscreenPlayerActivity.class);

        intent.putExtra("songItem", viewHolder.item );
        intent.putExtra("playFrom", Constants.PLAY_FROM_POPULAR_MV_LIST );

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mMessageReceiver);
    }
}
