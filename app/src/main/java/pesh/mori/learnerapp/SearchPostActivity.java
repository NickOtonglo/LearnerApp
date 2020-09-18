package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

public class SearchPostActivity extends AppCompatActivity {

    private RecyclerView mRecycler;
    private DatabaseReference mPosts,mCourseworkPosts,mNonCourseworkPosts,mAllPosts;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private TextView txtEmpty;
    private Query mParent;
    private String txtSearch,uid,activityFrom;

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
        setContentView(R.layout.activity_downloads_list);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        Bundle newExtrs = getIntent().getExtras();
        txtSearch = newExtrs.getString("query").toUpperCase();
        uid = newExtrs.getString("uid");
        activityFrom = newExtrs.getString("activityFrom");

        mRecycler = (RecyclerView)findViewById(R.id.layout_recycler_downloads_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (activityFrom.equals("FilteredCategoryActivity") || activityFrom.equals("SelectGenreActivity")){
            populateCourseworkPost();
        } else if (activityFrom.equals("HomeActivity") || activityFrom.equals("MyFilesActivity_DownloadsFragment") || activityFrom.equals("MyFilesActivity_UploadsFragment")) {
            populate();
        } else if (activityFrom.equals("ViewAuthorActivity_PostsFragment")){
            populate(uid);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
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

    public void populateCourseworkPost(){
        mPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(getString(R.string.firebase_ref_posts_type_1));
        mPosts.keepSynced(true);
        mParent = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_small,
                PostsViewHolder_SmallCard.class,
                mPosts.orderByChild(getString(R.string.firebase_ref_posts_search_keyword_ref)).startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String key = getRef(position).getKey();

                mParent.orderByKey().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
                        mPosts.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(key).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(key).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(key).child("file_type").getValue());
                                    String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(key,getString(R.string.firebase_ref_posts_type_1));
                                        }
                                    });
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
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public void populate(){ //upper child needed here
        mCourseworkPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mCourseworkPosts.keepSynced(true);
        mNonCourseworkPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mNonCourseworkPosts.keepSynced(true);
        mParent = FirebaseDatabase.getInstance().getReference();
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
        mAllPosts.keepSynced(true);

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_small,
                PostsViewHolder_SmallCard.class,
                mAllPosts.orderByChild(getString(R.string.firebase_ref_posts_search_keyword_ref)).startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String postKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
//                            Log.d("dataSnapshot", String.valueOf(dataSnapshot));
                            if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                                mCourseworkPosts.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
//                                        Log.d("DIY fileKey",fileKey);
                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(postKey).child("title").getValue()));
                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(postKey).child("timestamp").getValue()));
                                        String file_type = String.valueOf(dataSnapshot.child(postKey).child("file_type").getValue());
                                        String thumbnail = String.valueOf(dataSnapshot.child(postKey).child("thumbnail").getValue());
                                        if (!dataSnapshot.child(postKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                                checkOwnershipStatus(postKey,getString(R.string.firebase_ref_posts_type_1));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))){
                                mNonCourseworkPosts.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
//                                        Log.d("DIY fileKey",fileKey);
                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(postKey).child("title").getValue()));
                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(postKey).child("timestamp").getValue()));
                                        String file_type = String.valueOf(dataSnapshot.child(postKey).child("file_type").getValue());
                                        String thumbnail = String.valueOf(dataSnapshot.child(postKey).child("thumbnail").getValue());
                                        if (!dataSnapshot.child(postKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                                checkOwnershipStatus(postKey,getString(R.string.firebase_ref_posts_type_2));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public void populate(final String userId){
        mCourseworkPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mCourseworkPosts.keepSynced(true);
        mNonCourseworkPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mNonCourseworkPosts.keepSynced(true);
        mParent = FirebaseDatabase.getInstance().getReference();
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
        mAllPosts.keepSynced(true);

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_small,
                PostsViewHolder_SmallCard.class,
                mAllPosts.orderByChild(getString(R.string.firebase_ref_posts_search_keyword_ref)).startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String postKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(postKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
//                            Log.d("dataSnapshot", String.valueOf(dataSnapshot));
                        if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                            mCourseworkPosts.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                    Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
//                                    Log.d("DIY fileKey",fileKey);
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(postKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(postKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(postKey).child("file_type").getValue());
                                    String thumbnail = String.valueOf(dataSnapshot.child(postKey).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(postKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(postKey,getString(R.string.firebase_ref_posts_type_1));
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))){
                            mNonCourseworkPosts.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//                                        Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
//                                        Log.d("DIY fileKey",fileKey);
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(postKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(postKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(postKey).child("file_type").getValue());
                                    String thumbnail = String.valueOf(dataSnapshot.child(postKey).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(postKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(postKey,getString(R.string.firebase_ref_posts_type_2));
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public void checkOwnershipStatus(final String key, final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                String title = String.valueOf(dataSnapshot.child("title").getValue());
                String fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                Double price = Double.parseDouble(itemPrice);
                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));

                String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","FilteredCategoryActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    transactionsIntent.putExtra("title",title);
                    transactionsIntent.putExtra("file_type",fileType);
                    transactionsIntent.putExtra("postType",category);
                    startActivity(transactionsIntent);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",category);
                    startActivity(viewPostIntent);
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",category);
                    startActivity(viewPostIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = new Intent(getApplicationContext(), SearchPostActivity.class);
        intent.putExtra("query",txtSearch);
        intent.putExtra("activityFrom",activityFrom);

//        startActivity(intent);
//        finish();

    }
}
