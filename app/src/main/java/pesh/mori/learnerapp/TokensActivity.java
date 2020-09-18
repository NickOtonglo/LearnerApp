package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class TokensActivity extends AppCompatActivity {
    TabLayout MyTabs;
    ViewPager MyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
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

        Adapter.AddFragmentPage(new TokensActivity_BuyTokensFragment(),getString(R.string.title_buy_tokens));
        Adapter.AddFragmentPage(new TokensActivity_RedeemTokensFragment(),getString(R.string.title_redeem_tokens));
        Adapter.AddFragmentPage(new TokensActivity_TransferTokensFragment(),getString(R.string.title_share_tokens));

        viewpage.setAdapter(Adapter);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    public class MyViewPageAdapter extends FragmentPagerAdapter {
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
