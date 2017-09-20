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
public class PlayListAdapter extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;

    int from = 0;

    public PlayListAdapter(Context context, int textViewResourceId, int from ) {
        super(context, textViewResourceId);
        inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.from = from;
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
                viewHolder.txtPlayCount = (TextView) row.findViewById(R.id.txtPlayCount);
                viewHolder.txtItemCount = (TextView) row.findViewById(R.id.txtItemCount);
                viewHolder.txtCreatedDate = (TextView) row.findViewById(R.id.txtCreatedDate);
                row.setTag( viewHolder );
            }
            else
                viewHolder = (PlayListViewHolder) row.getTag();

            viewHolder.item = item;
            viewHolder.txtName.setText( Util.getStringFromHash(item, "Name") );
            viewHolder.playListNo = Util.getStringFromHash( item, "playListNo" );
            if ( Util.isEmptyString(Util.getStringFromHash(item, "playCount")))
                viewHolder.txtPlayCount.setVisibility(ViewGroup.GONE);
            else
                viewHolder.txtPlayCount.setVisibility(ViewGroup.VISIBLE);

            viewHolder.txtPlayCount.setText("조회수:" + Util.getStringFromHash(item, "playCount"));

            if ( Util.isEmptyForKey( item, "itemCount"))
                viewHolder.txtItemCount.setText( "곡 수 : 0" );
            else
                viewHolder.txtItemCount.setText( "곡 수 : " + Util.getStringFromHash(item, "itemCount") );

            if ( from == 1 ) {
                if ( !Util.isEmptyString(Util.getStringFromHash(item, "shareDate"))){
                    long time = Long.parseLong( Util.getStringFromHash(item, "shareDate") );
                    viewHolder.txtCreatedDate.setText( Util.getFormattedDateString( time, "yyyy-MM-dd"));
                } else {
                    if ( !Util.isEmptyString(Util.getStringFromHash(item, "createdDate"))){
                        long time = Long.parseLong( Util.getStringFromHash(item, "createdDate") );
                        viewHolder.txtCreatedDate.setText( Util.getFormattedDateString( time, "yyyy-MM-dd"));
                    }else
                        viewHolder.txtCreatedDate.setText("");
                }
            } else if ( from == 2 ) {
                if ( !Util.isEmptyString(Util.getStringFromHash(item, "createdDate"))){
                    long time = Long.parseLong( Util.getStringFromHash(item, "createdDate") );
                    viewHolder.txtCreatedDate.setText( Util.getFormattedDateString( time, "yyyy-MM-dd"));
                } else {
                    viewHolder.txtCreatedDate.setText("");
                }
            }

        } catch (Exception ex) {

        }

        return row;
    }
}
