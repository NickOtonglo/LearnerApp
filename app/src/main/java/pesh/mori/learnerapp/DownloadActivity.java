package pesh.mori.learnerapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MORIAMA on 21/11/2017.
 */

public class DownloadActivity extends AppCompatActivity {

    private final AppCompatActivity activity = DownloadActivity.this;

    private ImageView button;
    private ListView listView;
    private ArrayAdapter arrayAdapter;
    private ArrayList<String> files_on_server = new ArrayList<>();
    private Handler handler;
    private String selected_file;
    private ProgressDialog progressDialog;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;
    private FloatingActionButton log_out;
    private TextInputLayout textInputLayoutinstitution;
    private TextInputEditText txtSearchInput;
    private InputValidation inputValidation;
    private TextInputEditText editText;
    private ImageView btnSearch;
    private Menu menu;
    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;

    private RecyclerView mRecycler;

    private RecyclerView mRecycler2;

    private RecyclerView mRecycler3;

    private RecyclerView mRecycler4;

    private RecyclerView mRecycler5;

    private String fileType;

    private FirebaseAuth mAuth;
    private DatabaseReference mLists;
    private Query mSearch;
    private StorageReference mStorage;
    private TextView txtEmpty;
    private Query mParent;
    private String institutionCategory;

    private DatabaseReference mFiles,mFilesItems;

    private String faculty="",department="",course="",institution="";
    private List<String> facultyList,departmentList,courseList,institutionList;
    private Spinner spinnerFaculty,spinnerDepartment,spinnerCourse,spinnerInstitution;
    private RadioButton radioDoc,radioAudio,radioVideo;

    private String mAuthor="";

    public DownloadActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Bundle downloadExtras = getIntent().getExtras();
        institutionCategory = downloadExtras.getString("category");
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setSubtitle(institutionCategory);

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), institutionCategory, Snackbar.LENGTH_LONG);
        snackbar.show();

        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mFilesItems = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("Files");
        mFilesItems.keepSynced(true);
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child("Files");
        mParent = FirebaseDatabase.getInstance().getReference().child("Files");

        txtEmpty = findViewById(R.id.txt_empty);

        mFilesItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText("No posts found");
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        radioAudio = findViewById(R.id.radio_audio_act_down);
//        radioVideo = findViewById(R.id.radio_video_act_down);
//        radioDoc = findViewById(R.id.radio_document_act_down);
//        radioAudio.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                reloadList();
//            }
//        });
//        radioVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                reloadList();
//            }
//        });
//        radioDoc.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                reloadList();
//            }
//        });

        mRecycler = (RecyclerView)findViewById(R.id.layout_recycler_act_down);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        mAlert = new AlertDialog.Builder(this);

        executeCommand();

        loadList(mFilesItems.orderByChild("Institution").equalTo(institutionCategory));
    }

    public void loadList(Query databaseReference){
        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, File model, int position) {
                final String fileKey = getRef(position).getKey();

                mFiles.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                        mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    String name = dataSnapshot.child("username").getValue().toString();
                                    String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                    viewHolder.setAuthorName(name);
                                    if (!avatarLink.isEmpty()){
                                        viewHolder.setAuthorImage(getApplicationContext(),avatarLink);
                                    } else {
                                        viewHolder.setAuthorImage();
                                    }
                                } catch (NullPointerException e){
                                    e.printStackTrace();
                                    loadList(mFilesItems.orderByChild("Institution").equalTo(institutionCategory));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        String thumbnail = String.valueOf(dataSnapshot.child(fileKey).child("thumbnail").getValue());
                        if (!dataSnapshot.child(fileKey).child("thumbnail").exists() || thumbnail.equals("")){
                            if (file_type.equals("audio")){
                                viewHolder.setAudioImage();
                            } else if (file_type.equals("video")){
                                viewHolder.setVideoImage();
                            } else if (file_type.equals("image")){
                                viewHolder.setImageImage();
                            } else if (file_type.equals("doc")){
                                viewHolder.setDocImage();
                            }
                        } else {
                            viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                        }
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                checkOwnershipStatus(fileKey);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    private void reloadList(){

        if (radioDoc.isChecked()){
            fileType = "image";
        }
        if (radioVideo.isChecked()){
            fileType = "video";
        }
        if (radioAudio.isChecked()){
            fileType = "audio";
        }

        loadList(mFiles.orderByChild("file_type").equalTo(fileType));
        getSupportActionBar().setSubtitle("All Posts");

    }

    public void setNull(){
        txtEmpty.setText("No Files to Display");
        mRecycler.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        int id = item.getItemId();

        if (id == R.id.upload){

            startActivity(new Intent(DownloadActivity.this,NewFileUpload.class));
            overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);

        }

        return super.onOptionsItemSelected( item );
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
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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
                performSearch(txtSearch);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    public void performSearch(String searchQuery){
        getSupportActionBar().setSubtitle("All Posts");
        mSearch = FirebaseDatabase.getInstance().getReference().child("Files");
        Query databaseReference = mSearch.orderByChild("title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff");
        loadList(databaseReference);
    }

    public void checkOwnershipStatus(final String key){
        FirebaseDatabase.getInstance().getReference().child("Files").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                Double price = Double.parseDouble(itemPrice);
                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","DownloadActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    startActivity(transactionsIntent);
                    overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                    viewFileIntent.putExtra("file_key",key);
                    startActivity(viewFileIntent);
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                    viewFileIntent.putExtra("file_key",key);
                    startActivity(viewFileIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}