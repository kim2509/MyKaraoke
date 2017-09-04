package adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tessoft.mykaraoke.KaraokeApplication;
import com.tessoft.mykaraoke.MyPlaylistFragment;
import com.tessoft.mykaraoke.PopularMVFragment;
import com.tessoft.mykaraoke.PopularPlaylistFragment;
import com.tessoft.mykaraoke.PopularSongFragment;

/**
 * Created by Daeyong on 2015-12-14.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    Fragment[] fragments = null;
    public KaraokeApplication application = null;

    public MainPagerAdapter(FragmentManager fm, KaraokeApplication application) {
        super(fm);

        this.application = application;

        fragments = new Fragment[4];

        fragments[0] = PopularMVFragment.newInstance();
        fragments[1] = PopularSongFragment.newInstance();
        fragments[2] = PopularPlaylistFragment.newInstance();
        fragments[3] = MyPlaylistFragment.newInstance();
    }

    public void setApplication( KaraokeApplication application )
    {
        this.application = application;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titles = null;

        titles = new String[4];
        titles[0] = "인기뮤비";
        titles[1] = "인기곡";
        titles[2] = "인기 재생 목록";
        titles[3] = "내 목록";

        return titles[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }
}