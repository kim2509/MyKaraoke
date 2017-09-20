package fragment;

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
import com.tessoft.mykaraoke.APIResponse;
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.HttpPostAsyncTask;
import com.tessoft.mykaraoke.R;

import java.util.HashMap;
import java.util.List;

import activity.PlayListMainActivity;
import adapter.PlayListAdapter;
import adapter.PlayListViewHolder;

/**
 * Created by Daeyong on 2016-04-18.
 */
public class PopularPlaylistFragment extends BaseFragment implements AdapterView.OnItemClickListener{

    protected View rootView = null;
    ListView list = null;
    PlayListAdapter adapter = null;

    int REQUEST_POPULAR_LIST = 1;

    // TODO: Rename and change types and number of parameters
    public static PopularPlaylistFragment newInstance() {
        PopularPlaylistFragment fragment = new PopularPlaylistFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Constants.BROADCAST_RELOAD_POPULAR_PLAYLIST));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                if ( intent.getAction().equals(Constants.BROADCAST_RELOAD_POPULAR_PLAYLIST))
                    loadPopularList();
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
                rootView = inflater.inflate(R.layout.fragment_popular_playlist, container, false);
            }

            list = (ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(this);
            adapter = new PlayListAdapter(getActivity(), 0, 1);
            list.setAdapter(adapter);

            loadPopularList();
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

        } catch( Exception ex ){
            application.showToastMessage(ex);
        }
    }

    private void loadPopularList() throws Exception {
        String url = Constants.getServerURL("/playlist/popularList.do");
        HashMap param = application.getDefaultHashMap();
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
        intent.putExtra("playListItem", viewHolder.item);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mMessageReceiver);
    }
}
