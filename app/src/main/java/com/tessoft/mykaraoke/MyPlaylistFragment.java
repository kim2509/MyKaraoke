package com.tessoft.mykaraoke;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import java.util.HashMap;
import java.util.List;

import adapter.PlayListAdapter;
import adapter.PlayListViewHolder;

/**
 * Created by Daeyong on 2016-04-18.
 */
public class MyPlaylistFragment extends BaseFragment
        implements AdapterView.OnItemClickListener, View.OnClickListener{

    protected View rootView = null;
    ListView list = null;
    PlayListAdapter adapter = null;
    View listHeaderView = null;

    int REQUEST_MY_LIST = 1;
    int REQUEST_ADD_PLAYLIST = 2;

    int REQUEST_CONFIRM_DELETE = 100;

    // TODO: Rename and change types and number of parameters
    public static MyPlaylistFragment newInstance() {
        MyPlaylistFragment fragment = new MyPlaylistFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(Constants.BROADCAST_LOAD_MY_PLAYLIST));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try
            {
                if ( intent.getAction().equals(Constants.BROADCAST_LOAD_MY_PLAYLIST))
                    loadMyList();
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
                listHeaderView = getActivity().getLayoutInflater().inflate(R.layout.list_header_my_playlist, null);
            }

            list = (ListView) rootView.findViewById(R.id.list);
            list.setOnItemClickListener(this);
            adapter = new PlayListAdapter(getActivity(), 0);

            setListHeader();

            list.setAdapter(adapter);
            registerForContextMenu(list);
        } catch( Exception ex )
        {
            application.showToastMessage(ex);
        }

        return rootView;
    }

    private void setListHeader() {
        list.removeHeaderView(listHeaderView);
        list.addHeaderView(listHeaderView);

        Button btnAddPlaylist = (Button) listHeaderView.findViewById(R.id.btnAddPlaylist);
        btnAddPlaylist.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        try {

            super.onResume();
            loadMyList();

        } catch( Exception ex ){
            application.showToastMessage(ex);
        }
    }

    private void loadMyList() throws Exception {
        String url = Constants.getServerURL("/playlist/myList.do");
        HashMap param = application.getDefaultHashMap();
        new HttpPostAsyncTask( this, url, REQUEST_MY_LIST ).execute(param);
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
                if ( requestCode == REQUEST_MY_LIST || requestCode == REQUEST_ADD_PLAYLIST ){
                    HashMap data = (HashMap) response.getData();
                    List<HashMap> myList = (List<HashMap>) data.get("myList");
                    adapter.clear();
                    adapter.addAll(myList);
                    adapter.notifyDataSetChanged();

                    TextView txtListTitle = (TextView) listHeaderView.findViewById(R.id.txtListTitle);
                    txtListTitle.setText("전체 " + myList.size() + "개");
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

    private void addlaylist() {
        try{

            // custom dialog
            final Dialog dialog = new Dialog( getActivity() );
            dialog.setContentView(R.layout.dialog_add_playlist);
            dialog.setTitle("재생목록 생성");

            Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        EditText edtPlayListName = (EditText) dialog.findViewById(R.id.edtPlayListName);

                        if ("기본".equals(edtPlayListName.getText().toString())) {
                            edtPlayListName.setError("다른 이름으로 지정해 주시기 바랍니다.");
                            return;
                        }

                        dialog.dismiss();

                        CheckBox chkShare = (CheckBox) dialog.findViewById(R.id.chkShare);

                        String playListName = edtPlayListName.getText().toString();
                        String shareYN = chkShare.isChecked() ? "Y":"N";

                        String url = Constants.getServerURL("/playlist/add.do");
                        HashMap param = application.getDefaultHashMap();
                        param.put("Name", playListName);
                        param.put("shareYN", shareYN);
                        new HttpPostAsyncTask( MyPlaylistFragment.this, url, REQUEST_ADD_PLAYLIST ).execute(param);

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
        catch(Exception ex ){
            application.showToastMessage(ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.btnAddPlaylist ) {
            addlaylist();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        PlayListViewHolder viewHolder = (PlayListViewHolder) info.targetView.getTag();

        if (!"기본".equals( Util.getStringFromHash(viewHolder.item, "Name"))){
            menu.setHeaderTitle("Select The Action");
            menu.add(0, v.getId(), 0, "수정");//groupId, itemId, order, title
            menu.add(0, v.getId(), 0, "삭제");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PlayListViewHolder viewHolder = (PlayListViewHolder) info.targetView.getTag();

        if(item.getTitle()=="수정"){
            Intent intent = new Intent( getActivity(), EditPlayListActivity.class);
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
    public void yesClicked(int requestCode, Object param) {
        try {
            super.yesClicked(requestCode, param);

            if ( requestCode == REQUEST_CONFIRM_DELETE ) {

                String Name = Util.getStringFromHash( (HashMap) param, "Name");

                if ("기본".equals(Name)){

                    showOKDialog("경고", "기본 재생목록은 삭제할 수 없습니다.", null );
                    return;
                }

                String playListNo = Util.getStringFromHash( (HashMap) param, "playListNo");
                String url = Constants.getServerURL("/playlist/delete.do");
                HashMap postParam = application.getDefaultHashMap();
                postParam.put("playListNo", playListNo);
                new HttpPostAsyncTask( MyPlaylistFragment.this, url, REQUEST_ADD_PLAYLIST ).execute(postParam);

            }
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }

    }
}
