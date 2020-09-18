package pesh.mori.learnerapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MORIAMA on 21/11/2017.
 */

public class FilteredCategoryActivity extends AppCompatActivity {
    private Query mSearch;
    private RecyclerView mRecycler;
    private DatabaseReference mPosts, mAllPosts;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;
    private String mAuthor="",author;
    String category="",postType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_downloaddiy);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        category = getIntent().getExtras().getString("category");
        postType = getIntent().getExtras().getString("postType");
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        getSupportActionBar().setTitle(postType);
        getSupportActionBar().setSubtitle(category);

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), category, Snackbar.LENGTH_LONG);
        snackbar.show();

        mAuth = FirebaseAuth.getInstance();

        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType);
        mAllPosts.keepSynced(true);
        mPosts = FirebaseDatabase.getInstance().getReference().child(postType);
        mPosts.keepSynced(true);

        txtEmpty = findViewById(R.id.txt_empty);
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            mAllPosts.orderByChild("Institution").equalTo(category).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                        txtEmpty.setText(R.string.info_no_posts_found);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            mAllPosts.orderByChild("Tag").equalTo(category).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                        txtEmpty.setText(R.string.info_no_posts_found);
                    } else {
                        txtEmpty.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mRecycler = (RecyclerView)findViewById(R.id.layout_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        executeCommand();

        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            loadList(mAllPosts.orderByChild("Institution").equalTo(category));
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            loadList(mAllPosts.orderByChild("Tag").equalTo(category));
        }
    }

    public void loadList(Query databaseReference){
        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, File model, int position) {
                final String key = getRef(position).getKey();

                mPosts.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mAuthor = String.valueOf(dataSnapshot.child(key).child("author").getValue());
                        DatabaseReference mPublishedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuthor);
                        mPublishedItems.child(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Log.d("LOG_mPublishedItems", String.valueOf(mPublishedItems.child(key)));
                                        if (dataSnapshot.exists())
                                            if (dataSnapshot.child("Published").getValue().equals("true")) {
                                                if (dataSnapshot.child("Category").getValue().equals(postType)) {
                                                    mPosts.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                                txtEmpty.setVisibility(View.GONE);
                                                                mPosts.addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(key).child("title").getValue()));
                                                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(key).child("timestamp").getValue()));
                                                                        String file_type = String.valueOf(dataSnapshot.child(key).child("file_type").getValue());
                                                                        mAuthor = String.valueOf(dataSnapshot.child(key).child("author").getValue());
                                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                try {
                                                                                    String name = dataSnapshot.child("username").getValue().toString();
                                                                                    String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                                    viewHolder.setAuthorName(name);
                                                                                    if (!avatarLink.isEmpty()) {
                                                                                        viewHolder.setAuthorImage(getApplicationContext(), avatarLink);
                                                                                    } else {
                                                                                        viewHolder.setAuthorImage();
                                                                                    }
                                                                                } catch (NullPointerException e) {
                                                                                    e.printStackTrace();
                                                                                    loadList(mPublishedItems);
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                        String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                                                        if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")) {
                                                                            if (file_type.equals("audio")) {
                                                                                viewHolder.setAudioImage();
                                                                            } else if (file_type.equals("video")) {
                                                                                viewHolder.setVideoImage();
                                                                            } else if (file_type.equals("image")) {
                                                                                viewHolder.setImageImage();
                                                                            } else if (file_type.equals("ticket")) {
                                                                                viewHolder.setDocImage();
                                                                            }
                                                                        } else {
                                                                            viewHolder.setThumbnail(getApplicationContext(), thumbnail);
                                                                        }
                                                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View view) {
                                                                                checkOwnershipStatus(key);
                                                                            }
                                                                        });
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            } else {
                                                                viewHolder.setNull();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            } else {
                                                viewHolder.setTitle(getString(R.string.info_post_is_private));
                                                viewHolder.setAuthorImage();
                                                viewHolder.setTimestamp(" ");
                                                viewHolder.setAuthorName(" ");
                                            }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public void setNull(){
        TextView txtEmpty = findViewById(R.id.txt_empty);
        txtEmpty.setText(R.string.info_no_posts_to_display);
        mRecycler.setVisibility(View.GONE);
        txtEmpty.setVisibility(View.VISIBLE);
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

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_network_access_unavailable), Snackbar.LENGTH_LONG);
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
        getSupportActionBar().setSubtitle(getString(R.string.title_all_posts));
        mSearch = FirebaseDatabase.getInstance().getReference().child(postType);
        Query databaseReference = mSearch.orderByChild("title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff");
        loadList(databaseReference);
    }

    public void checkOwnershipStatus(final String key){
        FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                String title = String.valueOf(dataSnapshot.child("title").getValue());
                String fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                Double price = Double.parseDouble(itemPrice);
                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","FilteredCategoryActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    transactionsIntent.putExtra("title",title);
                    transactionsIntent.putExtra("file_type",fileType);
                    transactionsIntent.putExtra("postType",postType);
                    startActivity(transactionsIntent);
                    overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",postType);
                    startActivity(viewPostIntent);
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",postType);
                    startActivity(viewPostIntent);
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
