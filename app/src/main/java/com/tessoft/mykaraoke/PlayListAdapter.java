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
public class PlayListAdapter extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;

    public PlayListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        PlayListViewHolder viewHolder = null;

        try {
            HashMap item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.play_list_item, parent, false);

                viewHolder = new PlayListViewHolder();
                viewHolder.txtName = (TextView) row.findViewById(R.id.txtName);
                row.setTag( viewHolder );
            }
            else
                viewHolder = (PlayListViewHolder) row.getTag();

            viewHolder.txtName.setText( Util.getStringFromHash( item, "Name" ) );
            viewHolder.playListNo = Util.getStringFromHash( item, "playListNo" );

        } catch (Exception ex) {

        }

        return row;
    }
}
