package pesh.mori.learnerapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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

import java.util.Arrays;
import java.util.List;

public class DownloadsList extends AppCompatActivity {

    private RecyclerView mRecycler;
    private DatabaseReference mFiles,mDIY,mFilesItems;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private TextView txtEmpty;
    private Query mParent;
    private String txtSearch,uid,activityFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads_list);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
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

        if (activityFrom.equals("DownloadActivity")){
            populateFiles();
        } else if (activityFrom.equals("DownloadDiyActivity")){
            populateDiy();
        } else if (activityFrom.equals("HomeActivity") || activityFrom.equals("mydownloads") || activityFrom.equals("myuploads")) {
            populate();
        } else if (activityFrom.equals("ViewAuthorActivity_PostsFragment")){
            populate(uid);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.static_animation,R.anim.slide_in_from_top);
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

    public void populateFiles(){

        mFiles = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("Files");
        mFiles.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mParent = FirebaseDatabase.getInstance().getReference().child("Files");

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads,
                PostsViewHolder_SmallCard.class,
                mFiles.orderByChild("Title").startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                mParent.orderByKey().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
                        mFiles.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
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
                                            checkOwnershipStatus(fileKey,"Files");
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

    public void populateDiy(){

        mFiles = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY");
        mFiles.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child("DIY").child(mAuth.getCurrentUser().getUid());
        mParent = FirebaseDatabase.getInstance().getReference().child("DIY");

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads,
                PostsViewHolder_SmallCard.class,
                mFiles.orderByChild("Title").startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String diyKey = getRef(position).getKey();

                mParent.orderByKey().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFiles = FirebaseDatabase.getInstance().getReference().child("DIY");
                        mFiles.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(diyKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(diyKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(diyKey).child("file_type").getValue());
                                    String thumbnail = String.valueOf(dataSnapshot.child(diyKey).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(diyKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(diyKey,"DIY");
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
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mDIY.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child(mAuth.getCurrentUser().getUid());
        mParent = FirebaseDatabase.getInstance().getReference();
        mFilesItems = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        mFilesItems.keepSynced(true);

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads,
                PostsViewHolder_SmallCard.class,
                mFilesItems.orderByChild("Title").startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child("AllPosts").child(fileKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            Log.d("dataSnapshot",String.valueOf(dataSnapshot));
                            if (dataSnapshot.child("Category").getValue().equals("Files")){
                                mFiles.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
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
                                                checkOwnershipStatus(fileKey,"Files");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (dataSnapshot.child("Category").getValue().equals("DIY")){
                                mDIY.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
                                        Log.d("DIY fileKey",fileKey);
                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
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
                                                checkOwnershipStatus(fileKey,"DIY");
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
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mDIY.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child(userId);
        mParent = FirebaseDatabase.getInstance().getReference();
        mFilesItems = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        mFilesItems.keepSynced(true);

        FirebaseRecyclerAdapter<File,PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads,
                PostsViewHolder_SmallCard.class,
                mFilesItems.orderByChild("Title").startAt(txtSearch).endAt(txtSearch+"\uf8ff")
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child("AllPosts").child(fileKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            Log.d("dataSnapshot",String.valueOf(dataSnapshot));
                        if (dataSnapshot.child("Category").getValue().equals("Files")){
                            mFiles.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
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
                                            checkOwnershipStatus(fileKey,"Files");
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else if (dataSnapshot.child("Category").getValue().equals("DIY")){
                            mDIY.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("DIY dataSnapshot", String.valueOf(dataSnapshot.child(fileKey)));
                                    Log.d("DIY fileKey",fileKey);
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
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
                                            checkOwnershipStatus(fileKey,"DIY");
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

    public void checkOwnershipStatus(final String key,final String category){
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
                if (category.equals("Files")){
                    String tag = String.valueOf(dataSnapshot.child("institution").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","DownloadActivity");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }  else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }
                }else if (category.equals("DIY")){
                    String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","DownloadDiyActivity");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }
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

        Intent intent = new Intent(getApplicationContext(),DownloadsList.class);
        intent.putExtra("query",txtSearch);
        intent.putExtra("activityFrom",activityFrom);

//        startActivity(intent);
//        finish();

    }
}
