package com.tessoft.mykaraoke;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity
    implements TextWatcher {

    EditText txtSongTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 노래 곡목 검색 기능 초기화
        initSearchText();
    }

    private void initSearchText() {
        txtSongTitle = (EditText) findViewById(R.id.txtSongTitle);
        txtSongTitle.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        /*
        try {
            if (s.length() == 0) {
                adapter.clear();
                adapter.addAll(songList);
            } else {
                ArrayList<HashMap> tmp = new ArrayList<HashMap>();
                for (int i = 0; i < songList.size(); i++) {
                    HashMap obj = songList.get(i);
                    String title = Util.getStringFromHash(obj, "title");
                    if (title.replaceAll(" ", "").indexOf(s.toString().replaceAll(" ", "")) >= 0)
                        tmp.add(obj);
                }

                adapter.clear();
                adapter.addAll(tmp);
            }
            listRecentSearch.setAdapter(adapter);
        } catch (Exception ex) {

        }
        */
    }
}
