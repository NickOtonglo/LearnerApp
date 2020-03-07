package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AppCompatActivity {


    TabLayout MyTabs;
    ViewPager MyPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
//        setContentView( R.layout.activity_social );
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        MyTabs = (TabLayout) findViewById( R.id.Mytabs );
        MyPage = (ViewPager) findViewById( R.id.MyPage );

        MyTabs.setupWithViewPager( MyPage );

        setUpViewPager( MyPage );

    }

    public void setUpViewPager(ViewPager viewpage) {
        MyViewPageAdapter Adapter = new MyViewPageAdapter( getSupportFragmentManager() );

        Adapter.AddFragmentPage( new socialchat(), "Find a Writer" );
        Adapter.AddFragmentPage( new socialevent(), "Be a Writer" );


        viewpage.setAdapter( Adapter );
    }

    public class MyViewPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> MyFragment = new ArrayList<>();
        private List<String> MyPageTitle = new ArrayList<>();

        public MyViewPageAdapter(FragmentManager manager) {
            super( manager );
        }

        public void AddFragmentPage(Fragment Frag, String Title) {
            MyFragment.add( Frag );
            MyPageTitle.add( Title );
        }

        @Override
        public Fragment getItem(int position) {
            return MyFragment.get( position );
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MyPageTitle.get( position );
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}

