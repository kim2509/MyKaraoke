package activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tessoft.mykaraoke.APIResponse;
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.HttpPostAsyncTask;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import java.util.HashMap;

public class EditPlayListActivity extends BaseActivity {

    int REQUEST_PLAYLIST_DETAIL = 1;
    int REQUEST_UPDATE_PLAYLIST = 2;

    HashMap playListItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_edit_play_list);
        } catch( Exception ex ) {
            application.showToastMessage(ex);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();

            if ( getIntent() != null && getIntent().getExtras() != null &&
                    getIntent().getExtras().get("playListItem") != null ) {

                HashMap playListItem = (HashMap) getIntent().getExtras().get("playListItem");

                String url = Constants.getServerURL("/playlist/detail.do");
                HashMap param = application.getDefaultHashMap();
                playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
                new HttpPostAsyncTask( this, url, REQUEST_PLAYLIST_DETAIL ).execute(playListItem);
            }
        } catch( Exception ex ) {
            application.showToastMessage(ex);
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
                if ( requestCode == REQUEST_PLAYLIST_DETAIL ) {
                    HashMap data = (HashMap) response.getData();
                    if ( data.get("playlistInfo") != null ) {
                        playListItem = (HashMap) data.get("playlistInfo");
                        displayPlayListInfo();
                    }
                }
                else if ( requestCode == REQUEST_UPDATE_PLAYLIST ) {
                    showOKDialog("수정되었습니다", null );
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

    public void displayPlayListInfo(){

        TextView txtPlayListNo2 = (TextView) findViewById(R.id.txtPlayListNo2);
        txtPlayListNo2.setText( Util.getStringFromHash(playListItem, "playListNo"));

        EditText edtName = (EditText) findViewById(R.id.edtName);
        edtName.setText( Util.getStringFromHash(playListItem, "Name"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = menuItem.getItemId();

        try {
            //noinspection SimplifiableIfStatement
            if (id == R.id.menu_edit) {

                saveItem();

                return true;
            }

        } catch( Exception ex ) {
            application.showToastMessage( ex.getMessage() );
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private HashMap saveItem() throws Exception {

        if ( playListItem != null ) {

            EditText edtName = (EditText) findViewById(R.id.edtName);

            String url = Constants.getServerURL("/playlist/update.do");
            HashMap param = application.getDefaultHashMap();
            playListItem.put("tempUserNo", Util.getStringFromHash(param, "tempUserNo"));
            playListItem.put("Name", edtName.getText().toString());

            new HttpPostAsyncTask( this, url, REQUEST_UPDATE_PLAYLIST ).execute(playListItem);
        }

        return null;
    }
}
