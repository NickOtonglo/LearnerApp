package pesh.mori.learnerapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ViewAuthorActivity extends AppCompatActivity {
    private ViewPager mViewPager;

    private DatabaseReference mAuthorDetails;
    private String authorId;

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
        setContentView(R.layout.activity_view_author);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_author));
        getSupportActionBar().setSubtitle(getIntent().getExtras().getString("author_name"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        authorId = getIntent().getExtras().getString("author_id");

        mAuthorDetails = FirebaseDatabase.getInstance().getReference().child("Users").child(authorId);

        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        setUpViewPager(mViewPager);

    }

    public void setUpViewPager(ViewPager viewPager){
        mViewPageAdapter mAdapter = new mViewPageAdapter(getSupportFragmentManager());

        mAdapter.AddFragmentPage(new ViewAuthorActivity_DetailsFragment(),getString(R.string.title_details));
        mAdapter.AddFragmentPage(new ViewAuthorActivity_PostsFragment(),getString(R.string.title_posts));

        viewPager.setAdapter(mAdapter);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_view_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class mViewPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragment = new ArrayList<>();
        private List<String> mViewPageTitle = new ArrayList<>();

        public mViewPageAdapter(FragmentManager manager){
            super(manager);
        }
        public void AddFragmentPage(Fragment Frag, String Title){
            mFragment.add(Frag);
            mViewPageTitle.add(Title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragment.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mViewPageTitle.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public String getAuthorId(){
        return authorId;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
