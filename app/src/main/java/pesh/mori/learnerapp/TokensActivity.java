package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

/**
 * Created by MORIAMA on 20/11/2017.
 */

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;


import java.util.ArrayList;
import java.util.List;



public class TokensActivity extends AppCompatActivity {
    TabLayout MyTabs;
    ViewPager MyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tokens);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        MyTabs =(TabLayout) findViewById(R.id.Mytabs);
        MyPage = (ViewPager) findViewById(R.id.MyPage);

        MyTabs.setupWithViewPager(MyPage);

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        setUpViewPager(MyPage);
    }

    public void setUpViewPager(ViewPager viewpage){
        MyViewPageAdapter Adapter = new MyViewPageAdapter(getSupportFragmentManager());

        Adapter.AddFragmentPage(new buytokens(),"buy tokens");
        Adapter.AddFragmentPage(new redeemtokens(),"redeem tokens");
        Adapter.AddFragmentPage(new transfertokens(),"share tokens");

        viewpage.setAdapter(Adapter);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    public class MyViewPageAdapter extends FragmentPagerAdapter{
        private List<Fragment> MyFragment = new ArrayList<>();
        private List<String> MyPageTitle = new ArrayList<>();

        public MyViewPageAdapter(FragmentManager manager){
            super(manager);
        }
        public void AddFragmentPage(Fragment Frag, String Title){
            MyFragment.add(Frag);
            MyPageTitle.add(Title);
        }

        @Override
        public Fragment getItem(int position) {
            return MyFragment.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MyPageTitle.get(position);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent clearIntent = new Intent(getApplicationContext(),HomeActivity.class);
        clearIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(clearIntent);
        finish();
    }
}
