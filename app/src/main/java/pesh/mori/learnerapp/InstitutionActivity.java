package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
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

public class InstitutionActivity extends AppCompatActivity {

    private final AppCompatActivity activity = InstitutionActivity.this;
    private Button log_out,btnSubmit;
    private FloatingActionButton log_in;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;
    private TextInputLayout textInputLayoutinstitution;
    private TextInputEditText textInputEditTextinstitution;
    private InputValidation inputValidation;
    private Menu menu;
    private Spinner spinnerInstitution;
    private DatabaseReference mLists;


    private static CustomProgressBar progressBar = new CustomProgressBar();


    private static int TIME_OUT = 6000;

    private String institution="";
    private List<String> institutionList;

    private Query mSearch;
    private ProgressDialog mProgress;

    private RadioButton radioDoc,radioAudio,radioVideo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_institution );
//        DrawerLayout mDrawerlayout = (DrawerLayout) findViewById( R.id.drawer );
//        mToggle = new ActionBarDrawerToggle( this, mDrawerlayout,R.string.open, R.string.close);
//        mDrawerlayout.addDrawerListener( mToggle );
//        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
//        Toast.makeText(getApplicationContext(), "PREFERENCE | Institutional Learning", Toast.LENGTH_LONG).show();
        mProgress = new ProgressDialog(this);

//        radioAudio = findViewById(R.id.radio_download_audio);
//        radioDoc = findViewById(R.id.radio_download_video);
//        radioVideo = findViewById(R.id.radio_download_video);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        initViews();
        initObjects();
        loadSpinners();
        executeCommand();

        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);

        log_out = (Button) findViewById(R.id.appCompatButtonInstitution);
        log_out.startAnimation(myanim);

        mLists = FirebaseDatabase.getInstance().getReference().child("Lists");

//        log_in = (FloatingActionButton) findViewById(R.id.bupload);
//        log_in.startAnimation(myanim);
//        log_in.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                log_in.startAnimation(myanim);
//                startActivity(new Intent(getApplicationContext(), NewFileUpload.class));
//            }
//
//        });


    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        int id = item.getItemId();

        if (id == R.id.upload){

            startActivity(new Intent(InstitutionActivity.this,NewFileUpload.class));
            overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);

        }

        return super.onOptionsItemSelected( item );
    }


    private void initViews(){
        spinnerInstitution = findViewById(R.id.spinner_select_institution);
    }
    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }

    public void doThis(View view) {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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

                final Query mParent = FirebaseDatabase.getInstance().getReference().child("Files");

                mParent.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSearch = FirebaseDatabase.getInstance().getReference().child("Files");
                        mSearch.orderByChild("title").startAt(txtSearch.toUpperCase()).endAt(txtSearch.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    mProgress.dismiss();
//                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No results found, try another search", Snackbar.LENGTH_LONG);
//                                    snackbar.show();
                                    Toast.makeText(InstitutionActivity.this, "No results", Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgress.dismiss();
                                    Intent resultsActivity = new Intent(getApplicationContext(),DownloadsList.class);
                                    resultsActivity.putExtra("query",txtSearch.toUpperCase());
                                    resultsActivity.putExtra("activityFrom","DownloadActivity");
                                    startActivity(resultsActivity);
                                    overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
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
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        log_out = (Button) findViewById(R.id.appCompatButtonInstitution);
        log_out.startAnimation(myanim);
        String institutionCategory = spinnerInstitution.getSelectedItem().toString();
        Intent downloadIntent = new Intent(getApplicationContext(),DownloadActivity.class);
        if (spinnerInstitution.getSelectedItem().toString().equals("--University Institutions--")
                || spinnerInstitution.getSelectedItem().toString().equals("--Tertiary Institutions--")
                || spinnerInstitution.getSelectedItem().toString().equals("--Artisan Institutions--")){
            Toast.makeText(getApplicationContext(), "Select an institution", Toast.LENGTH_SHORT).show();
        } else {
            downloadIntent.putExtra("category",institutionCategory);
            startActivity(downloadIntent);
        }
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

                institution = String.valueOf(dataSnapshot.child("institutions_list").getValue());
                institutionList = Arrays.asList(institution.split("\\s*,\\s*"));

                //Institution
                final List<String>getInstitution = new ArrayList<>();
                final int institutionSize = institutionList.size();
                for (int i=0;i<institutionSize;i++){
                    Object object = institutionList.get(i);
                    getInstitution.add(object.toString().trim());
                }

                ArrayAdapter<String> institutionAdapter = new ArrayAdapter<String>(InstitutionActivity.this,android.R.layout.simple_spinner_item,getInstitution);
                institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInstitution.setAdapter(institutionAdapter);
                mProgress.dismiss();
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

}
