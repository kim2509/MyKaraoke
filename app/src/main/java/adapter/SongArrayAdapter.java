package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

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
        SongViewHolder viewHolder = null;

        try {
            HashMap item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.song_list, parent, false);

                viewHolder = new SongViewHolder();
                viewHolder.txtNo = (TextView) row.findViewById(R.id.txtNo);
                viewHolder.txtSongTitle = (TextView) row.findViewById(R.id.txtSongTitle);
                viewHolder.txtSinger = (TextView) row.findViewById(R.id.txtSinger);
                viewHolder.txtPlayCount = (TextView) row.findViewById(R.id.txtPlayCount);
                viewHolder.txtPlayCount2 = (TextView) row.findViewById(R.id.txtPlayCount2);
                row.setTag( viewHolder );
            } else {
                viewHolder = (SongViewHolder) row.getTag();
            }

            viewHolder.item = item;

            if ( item != null )
            {
                viewHolder.txtNo.setText(String.valueOf(position + 1));
                viewHolder.txtSongTitle.setText(Util.getStringFromHash(item, "title"));
                viewHolder.txtSinger.setText( Util.getStringFromHash(item, "singer"));

                if ( item.containsKey("playCount1") )
                    viewHolder.txtPlayCount.setText( Util.getStringFromHash( item, "playCount1") );
                else
                    viewHolder.txtPlayCount.setText( "0" );

                if ( item.containsKey("playCount2") )
                    viewHolder.txtPlayCount2.setText( Util.getStringFromHash( item, "playCount2") );
                else
                    viewHolder.txtPlayCount2.setText( "0" );
            }

        } catch (Exception ex) {

        }

        return row;
    }
}
