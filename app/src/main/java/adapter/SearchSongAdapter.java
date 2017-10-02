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
import com.tessoft.mykaraoke.Constants;
import com.tessoft.mykaraoke.KaraokeApplication;
import com.tessoft.mykaraoke.R;
import com.tessoft.mykaraoke.Util;

import java.util.HashMap;

/**
 * Created by Daeyong on 2017-03-31.
 */
public class SearchSongAdapter extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;
    DisplayImageOptions options = null;
    KaraokeApplication application = null;

    public SearchSongAdapter(Context context, int textViewResourceId, KaraokeApplication application) {
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

        this.application = application;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        SearchResultViewHolder viewHolder = null;

        try {
            HashMap item = getItem(position);

            if (row == null) {
                row = inflater.inflate(R.layout.search_result_item, parent, false);
                viewHolder = new SearchResultViewHolder();
                viewHolder.thumbNailView = (ImageView) row.findViewById(R.id.imgThumbnail);
                viewHolder.txtTitle = (TextView) row.findViewById(R.id.txtSongTitle);
                row.setTag(viewHolder);
            } else {
                viewHolder = (SearchResultViewHolder) row.getTag();
            }

            viewHolder.item = item;
            viewHolder.txtTitle.setText(Util.getStringFromHash(item, "title"));

            String url = "";

            if ( Constants.PLAY_MODE_ALL.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)) ||
                    Constants.PLAY_MODE_MUSIC.equals(application.getMetaInfoString(Constants.PREF_PLAY_MODE)))
                url = Util.getStringFromHash(item,"thumbnailURL2");
            else
                url = Util.getStringFromHash(item,"thumbnailURL1");

            if ( !Util.isEmptyString(url)) {
                ImageLoader.getInstance().displayImage( url, viewHolder.thumbNailView, options);
            } else {
                ImageLoader.getInstance().cancelDisplayTask(viewHolder.thumbNailView);
                viewHolder.thumbNailView.setImageResource(R.drawable.no_image);
            }

        } catch (Exception ex) {

        }

        return row;
    }
}
