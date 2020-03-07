package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 01/01/2018.
 */

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    private Button log_out;
    private FloatingActionButton log_in;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;
    private Menu menu;
    private Spinner spinner;


    private static CustomProgressBar progressBar = new CustomProgressBar();


    private static int TIME_OUT = 6000;

    private Query mSearch;
    private ProgressDialog mProgress;
    private DatabaseReference mFiles;


    private Spinner spinnerTag;
    private DatabaseReference mLists;

    private String tag="";
    private List<String> tagList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_category );
//        DrawerLayout mDrawerlayout = (DrawerLayout) findViewById( R.id.drawer );
//        mToggle = new ActionBarDrawerToggle( this, mDrawerlayout,R.string.open, R.string.close);
//        mDrawerlayout.addDrawerListener( mToggle );
//        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);

        //Toast.makeText(getApplicationContext(), "PREFERENCE | Do-It-Yourself Learning", Toast.LENGTH_LONG).show();
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);

        log_out = (Button) findViewById(R.id.appCompatButtonInstitution);
        log_out.startAnimation(myanim);

        loadSpinners();
        executeCommand();

        spinnerTag = (Spinner) findViewById( R.id.spinner_diy_list );

    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //needs to be debugged...
        getMenuInflater().inflate(R.menu.upload, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (null != searchView){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String txtSearch) {

                mProgress.setCanceledOnTouchOutside(false);
                mProgress.setMessage("Fetching results...");
                try {
                    if (!mProgress.isShowing()){
                        mProgress.show();
                    }
                } catch (WindowManager.BadTokenException e){
                    e.printStackTrace();
                }
                final Query mParent = FirebaseDatabase.getInstance().getReference().child("DIY");

                mParent.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSearch = FirebaseDatabase.getInstance().getReference().child("DIY");
                        mSearch.orderByChild("title").startAt(txtSearch.toUpperCase()).endAt(txtSearch.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    if (!mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
//                                    Snackbar.make(findViewById(android.R.id.content),"No results",Snackbar.LENGTH_LONG).show();
                                    Toast.makeText(CategoryActivity.this, "No results", Toast.LENGTH_SHORT).show();
//                                    Log.d("ExistsNot, dataSnapshot", String.valueOf(dataSnapshot));
//                                    Log.d("txtSearch",txtSearch);

                                } else {
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    Intent resultsActivity = new Intent(getApplicationContext(),DownloadsList.class);
                                    resultsActivity.putExtra("query",txtSearch.toUpperCase());
                                    resultsActivity.putExtra("activityFrom","DownloadDiyActivity");
                                    startActivity(resultsActivity);
                                    overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
//                                    Log.d("Exists, dataSnapshot", String.valueOf(dataSnapshot));
//                                    Log.d("txtSearch",txtSearch);
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

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);

    }

    public void seeThis(View view) {
        String diyCategory = spinnerTag.getSelectedItem().toString();
        Intent downloadIntent = new Intent(getApplicationContext(),DownloadDiyActivity.class);
        downloadIntent.putExtra("category",diyCategory);
        startActivity(downloadIntent);

    }


    private void loadSpinners(){
        mProgress.setCancelable(false);
        mProgress.setMessage("Please wait...");
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        mLists = FirebaseDatabase.getInstance().getReference().child("Lists");
        mLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tag = String.valueOf(dataSnapshot.child("tags_list").getValue());
                tagList = Arrays.asList(tag.split("\\s*,\\s*"));

                //Institution
                final List<String> getTag = new ArrayList<>();
                final int diySize = tagList.size();
                for (int i=0;i<diySize;i++){
                    Object object = tagList.get(i);
                    getTag.add(object.toString().trim());
                }

                ArrayAdapter<String> tagAdapter = new ArrayAdapter<String>(CategoryActivity.this,android.R.layout.simple_spinner_item,getTag);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTag.setAdapter(tagAdapter);
                if (mProgress.isShowing()){
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadSpinners();
            }
        });
    }

    private boolean executeCommand() {
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue " + mExitValue);
            if (mExitValue == 0) {

                return true;

            } else {

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Network access unavailable!", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception:" + ignore);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        int id = item.getItemId();

        if (id == R.id.upload){

            startActivity(new Intent(CategoryActivity.this,NewDiyUpload.class));
            overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);

        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        mProgress.dismiss();
        finish();
    }
}
