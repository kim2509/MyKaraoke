package adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tessoft.mykaraoke.KaraokeApplication;
import fragment.MyPlaylistFragment;
import fragment.PopularMVFragment;
import fragment.PopularPlaylistFragment;
import fragment.PopularSongFragment;
import fragment.SongSearchFragment;

/**
 * Created by Daeyong on 2015-12-14.
 */
public class MainPagerAdapter extends FragmentPagerAdapter {

    Fragment[] fragments = null;
    public KaraokeApplication application = null;

    public MainPagerAdapter(FragmentManager fm, KaraokeApplication application) {
        super(fm);

        this.application = application;

        fragments = new Fragment[5];

        fragments[0] = PopularMVFragment.newInstance();
        fragments[1] = PopularSongFragment.newInstance();
        fragments[2] = SongSearchFragment.newInstance();
        fragments[3] = PopularPlaylistFragment.newInstance();
        fragments[4] = MyPlaylistFragment.newInstance();
    }

    public void setApplication( KaraokeApplication application )
    {
        this.application = application;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        String[] titles = null;

        titles = new String[5];
        titles[0] = "인기뮤비";
        titles[1] = "인기곡";
        titles[2] = "검색";
        titles[3] = "인기 재생 목록";
        titles[4] = "내 목록";

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