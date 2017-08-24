package com.tessoft.mykaraoke;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Created by Daeyong on 2017-03-31.
 */
public class SongArrayAdapter extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;

    public SongArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        try {
            HashMap item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.song_list, parent, false);
            }

            TextView txtItemNo = (TextView) row.findViewById(R.id.txtItemNo);
            TextView txtSongTitle = (TextView) row.findViewById(R.id.txtSongTitle);
            TextView txtSinger = (TextView) row.findViewById(R.id.txtSinger);
            TextView txtPlayCount = (TextView) row.findViewById(R.id.txtPlayCount);

            if ( item != null )
            {
                if ( item.containsKey("itemNo") && item.get("itemNo") != null )
                    txtItemNo.setText( Util.getStringFromHash(item, "itemNo"));

                if (item.get("title") != null)
                    txtSongTitle.setText(Util.getStringFromHash(item, "title"));
                else
                    txtSongTitle.setText("");

                txtSinger.setText( Util.getStringFromHash(item, "singer"));

                if ( item.containsKey("playCount") )
                    txtPlayCount.setText( Util.getStringFromHash( item, "playCount") );
                else
                    txtPlayCount.setText( "0" );
            }

        } catch (Exception ex) {

        }

        return row;
    }
}
