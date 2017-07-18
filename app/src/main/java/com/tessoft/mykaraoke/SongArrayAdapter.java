package com.tessoft.mykaraoke;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by Daeyong on 2017-03-31.
 */
public class SongArrayAdapter extends ArrayAdapter<JSONObject> {

    LayoutInflater inflater = null;

    public SongArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        try {
            JSONObject item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.song_list, parent, false);
            }

            TextView txtSongTitle = (TextView) row.findViewById(R.id.txtSongTitle);
            if (item != null && item.get("title") != null)
                txtSongTitle.setText(item.getString("title"));
            else
                txtSongTitle.setText("");
        } catch (Exception ex) {

        }

        return row;
    }
}
