package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
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

public class SelectGenreActivity extends AppCompatActivity {
    private Query mSearch;
    private ProgressDialog mProgress;
    private Spinner spinner;
    private DatabaseReference mLists;
    private String tag="",institution="",postType="";
    private List<String> tagList,institutionList;
    private RadioButton radioCoursework,radioNonCoursework;
    private TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView( R.layout.activity_category );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);

        txtTitle = findViewById(R.id.txt_title);
        spinner = (Spinner) findViewById(R.id.spinner_genre_list);
        radioCoursework = findViewById(R.id.radio_coursework);
        radioNonCoursework = findViewById(R.id.radio_non_coursework);

        executeCommand();

        radioCoursework.setChecked(true);
        loadSpinners(getString(R.string.firebase_ref_posts_type_1),getString(R.string.firebase_ref_lists_cat_8));

        radioCoursework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSpinners(getString(R.string.firebase_ref_posts_type_1),getString(R.string.firebase_ref_lists_cat_8));
                txtTitle.setText(getString(R.string.title_select_institution));
            }
        });
        radioNonCoursework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSpinners(getString(R.string.firebase_ref_posts_type_2),getString(R.string.firebase_ref_lists_cat_9));
                txtTitle.setText(getString(R.string.title_select_tag_category));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_upload, menu);

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
                mProgress.setMessage(getString(R.string.info_fetching_results));
                try {
                    if (!mProgress.isShowing()){
                        mProgress.show();
                    }
                } catch (WindowManager.BadTokenException e){
                    e.printStackTrace();
                }
                final Query mParent = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));

                mParent.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSearch = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
                        mSearch.orderByChild("Title").startAt(txtSearch.toUpperCase()).endAt(txtSearch.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    mProgress.dismiss();
//                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No results found!", Snackbar.LENGTH_LONG);
//                                    snackbar.show();
                                    Toast.makeText(SelectGenreActivity.this, R.string.info_no_results, Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgress.dismiss();
                                    Intent resultsActivity = new Intent(getApplicationContext(), SearchPostActivity.class);
                                    resultsActivity.putExtra("query",txtSearch.toUpperCase());
                                    resultsActivity.putExtra("activityFrom","HomeActivity"); //added this section
                                    startActivity(resultsActivity);
                                    overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
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

    public void performFilter(View view) {
        String category = spinner.getSelectedItem().toString();
        if (radioCoursework.isChecked()){
            if (category.equals("--University Institutions--")
                    || category.equals("--Tertiary Institutions--")
                    || category.equals("--Artisan Institutions--")){
                Snackbar.make(findViewById(android.R.id.content), R.string.you_must_select_a_valid_institution,Snackbar.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(getApplicationContext(), FilteredCategoryActivity.class);
                intent.putExtra("category",category);
                intent.putExtra("postType",postType);
                startActivity(intent);
            }
        } else if (radioNonCoursework.isChecked()){
            Intent intent = new Intent(getApplicationContext(), FilteredCategoryActivity.class);
            intent.putExtra("category",category);
            intent.putExtra("postType",postType);
            startActivity(intent);
        }
    }

    private void loadSpinners(String type, String list){
        postType = type;
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.info_please_wait));
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        mLists = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists));
        mLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tag = String.valueOf(dataSnapshot.child(list).getValue());
                tagList = Arrays.asList(tag.split("\\s*,\\s*"));

                final List<String> getTag = new ArrayList<>();
                final int diySize = tagList.size();
                for (int i=0;i<diySize;i++){
                    Object object = tagList.get(i);
                    getTag.add(object.toString().trim());
                }

                ArrayAdapter<String> tagAdapter = new ArrayAdapter<String>(SelectGenreActivity.this,android.R.layout.simple_spinner_item,getTag);
                tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(tagAdapter);
                if (mProgress.isShowing()){
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (radioCoursework.isChecked()){
                    loadSpinners(getString(R.string.firebase_ref_posts_type_1),getString(R.string.firebase_ref_lists_cat_8));
                    txtTitle.setText(getString(R.string.title_select_institution));
                } else if (radioNonCoursework.isChecked()){
                    loadSpinners(getString(R.string.firebase_ref_posts_type_2),getString(R.string.firebase_ref_lists_cat_9));
                    txtTitle.setText(getString(R.string.title_select_tag_category));
                }
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

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.error_network_access_unavailable, Snackbar.LENGTH_LONG);
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

        if (id == R.id.sub_menu_audio){
            Intent i = new Intent(getApplicationContext(),NewPostActivity.class);
            i.putExtra("upload_type","audio");
            startActivity(i);
            overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
        }

        if (id == R.id.sub_menu_video){
            Intent i = new Intent(getApplicationContext(),NewPostActivity.class);
            i.putExtra("upload_type","video");
            startActivity(i);
            overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
        }

        if (id == R.id.sub_menu_doc){
            Intent i = new Intent(getApplicationContext(),NewPostActivity.class);
            i.putExtra("upload_type","PDF");
            startActivity(i);
            overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
        }

        return super.onOptionsItemSelected( item );
    }

    @Override
    public void onBackPressed() {
        mProgress.dismiss();
        finish();
    }
}
