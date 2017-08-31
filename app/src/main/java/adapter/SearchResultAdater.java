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
public class SearchResultAdater extends ArrayAdapter<HashMap> {

    LayoutInflater inflater = null;
    DisplayImageOptions options = null;

    public SearchResultAdater(Context context, int textViewResourceId) {
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
        SearchResultViewHolder viewHolder = null;

        try {
            HashMap item = getItem(position);
            HashMap snippet = (HashMap) item.get("snippet");
            HashMap thumbnails = (HashMap) snippet.get("thumbnails");
            HashMap medium = (HashMap) thumbnails.get("default");
            String url = Util.getStringFromHash(medium, "url");

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
            viewHolder.txtTitle.setText(Util.getStringFromHash(snippet, "title"));

            if ( !Util.isEmptyForKey(medium,"url")) {
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
