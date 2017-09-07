package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import java.util.HashMap;

/**
 * Created by Daeyong on 2017-03-31.
 */
public class PopularItemAdapter extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;
    DisplayImageOptions options = null;

    public PopularItemAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        options = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.no_image)
                .showImageForEmptyUri(R.drawable.no_image)
                .showImageOnFail(R.drawable.no_image)
                .displayer(new RoundedBitmapDisplayer(20))
                .delayBeforeLoading(100)
                .build();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        PopularItemViewHolder viewHolder = null;

        try {
            HashMap item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.popular_item, parent, false);
                viewHolder = new PopularItemViewHolder();
                viewHolder.thumbNailView = (ImageView) row.findViewById(R.id.imgThumbnail);
                viewHolder.txtTitle = (TextView) row.findViewById(R.id.txtSongTitle);
                viewHolder.txtPlayCount = (TextView) row.findViewById(R.id.txtPlayCount);
                row.setTag(viewHolder);
            } else {
                viewHolder = (PopularItemViewHolder) row.getTag();
            }

            viewHolder.item = item;
            viewHolder.txtTitle.setText(Util.getStringFromHash(item, "title"));
            viewHolder.txtPlayCount.setText("재생횟수:" + Util.getStringFromHash(item, "playCount"));

            if ( !Util.isEmptyForKey(item,"thumbnailURL")) {
                ImageLoader.getInstance().displayImage( Util.getStringFromHash(item, "thumbnailURL"), viewHolder.thumbNailView, options);
            } else {
                ImageLoader.getInstance().cancelDisplayTask(viewHolder.thumbNailView);
                viewHolder.thumbNailView.setImageResource(R.drawable.no_image);
            }

        } catch (Exception ex) {

        }

        return row;
    }
}
