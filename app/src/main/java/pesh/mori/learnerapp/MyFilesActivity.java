package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyFilesActivity extends AppCompatActivity {
    private Query mSearch;
    public static ProgressDialog mProgress;
    public static AlertDialog.Builder mAlert;

    public static FirebaseAuth mAuth;

    TabLayout MyTabs;
    ViewPager MyPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfiles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        MyTabs =(TabLayout) findViewById(R.id.Mytabs);
        MyPage = (ViewPager) findViewById(R.id.MyPage);

        MyTabs.setupWithViewPager(MyPage);

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        setUpViewPager(MyPage);

    }

    public void setUpViewPager(ViewPager viewpage){
        MyViewPageAdapter Adapter = new MyViewPageAdapter(getSupportFragmentManager());

        Adapter.AddFragmentPage(new mydownloads(),"Downloads");
        Adapter.AddFragmentPage(new mynotes(),"Notes");
        Adapter.AddFragmentPage( new myuploads(),"Uploads" );

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search, menu);
//
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//
//        if (null != searchView){
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            searchView.setIconifiedByDefault(false);
//        }
//
//        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String txtSearch) {
//
//
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return true;
//            }
//        };
//
//        searchView.setOnQueryTextListener(queryTextListener);
//
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent clearIntent = new Intent(getApplicationContext(),HomeActivity.class);
        clearIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(clearIntent);
        finish();
    }

    public void performSearch(final String searchQuery,final String node,final String outgoingFragment){
        mAlert.setMessage("No results")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage("Fetching results...");
        mProgress.show();
        final Query mParent = FirebaseDatabase.getInstance().getReference().child(node);
        final DatabaseReference[] mSearch = new DatabaseReference[1];

        mParent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSearch[0] = FirebaseDatabase.getInstance().getReference().child(node).child(mAuth.getCurrentUser().getUid());
                mSearch[0].orderByChild("Title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            mProgress.dismiss();
                            mAlert.show();
//                            Toast.makeText(getApplicationContext(), "No results", Toast.LENGTH_SHORT).show();
                        } else {
                            mProgress.dismiss();
                            Intent resultsActivity = new Intent(getApplicationContext(),DownloadsList.class);
                            resultsActivity.putExtra("query",searchQuery.toUpperCase());
                            resultsActivity.putExtra("activityFrom",outgoingFragment);

                            startActivity(resultsActivity);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
