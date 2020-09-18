package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class ViewPostActivity extends AppCompatActivity {
    private FrameLayout layoutPrivate;
    private NestedScrollView scrollView;
    private TextView txtTitle,txtDescription,txtTag,txtTime,txtPrice,txtBid,txtInstitution,txtDept,txtCourse;

    private String key,mAuthor,mTitle;
    private DatabaseReference mAllPosts,mSubPosts;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn,layoutImage,layoutDoc,layoutCoursework,layoutNonCoursework,layoutRecycler;
    private Button btnDelete;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private ImageView imageView;
    private FrameLayout filePlaceholder;
    private SlidingUpPanelLayout slidingUpPanel;
    private View bottomHorizontalBar1,bottomHorizontalBar2;
    private Button btnOpenPDF;

    private Uri mFileUri = null;

    private String fileType,filePath;

    /*v1.0.6 new feature 00001*/
    private Boolean hideState;

    private RecyclerView mRecycler,mRecyclerSub;

    FloatingActionMenu fabMain;
    private FloatingActionButton fabAddNote,fabViewNotes,fabViewAuthor,fabReportPost,fabDeletePost;

    private String postType="";

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
        setContentView(R.layout.activity_view_post);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        layoutRecycler = findViewById(R.id.layout_recycler);
        layoutPrivate = findViewById(R.id.layout_private);
        fabMain = findViewById(R.id.fab_actions);
        scrollView = findViewById(R.id.nested_scrollview);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    fabMain.setVisibility(View.GONE);
                } else {
                    fabMain.setVisibility(View.VISIBLE);
                }
            }
        });

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);

        key = getIntent().getExtras().getString("file_key");
        mAuthor = getIntent().getExtras().getString("author");
        postType = getIntent().getExtras().getString("postType");

        mAuth = FirebaseAuth.getInstance();

        //FABs
        fabAddNote = findViewById(R.id.fab_add_note);
        fabViewNotes = findViewById(R.id.fab_view_notes);
        fabViewAuthor = findViewById(R.id.fab_view_author);
        fabReportPost = findViewById(R.id.fab_report);
        fabDeletePost = findViewById(R.id.fab_delete_post);

        imageView = findViewById(R.id.btn_view_select_image);
        layoutImage = findViewById(R.id.view_layout_1);
        filePlaceholder = findViewById(R.id.exoplayer_placeholder);
        bottomHorizontalBar1 = (View)findViewById(R.id.view_bottom_horizontal_bar_1);
        bottomHorizontalBar2 = (View)findViewById(R.id.view_bottom_horizontal_bar_2);
        slidingUpPanel = findViewById(R.id.sliding_up_panel_parent);
        layoutDoc = findViewById(R.id.view_layout_5);
        btnOpenPDF = findViewById(R.id.btn_open_pdf);
        btnOpenPDF.setVisibility(View.GONE);

        layoutBtn = findViewById(R.id.layout_view_btn);
        btnDelete = findViewById(R.id.btn_view_delete);
        txtTitle = findViewById(R.id.txt_view_title);
        txtTag = findViewById(R.id.txt_view_tag);
        txtDescription = findViewById(R.id.txt_view_description);
        txtTime = findViewById(R.id.txt_view_time);
        txtPrice = findViewById(R.id.txt_view_price);
        txtBid = findViewById(R.id.txt_view_bid);
        txtInstitution = findViewById(R.id.txt_view_institution);
        txtDept = findViewById(R.id.txt_view_faculty);
        txtCourse = findViewById(R.id.txt_view_course);

        layoutCoursework = findViewById(R.id.layout_coursework);
        layoutNonCoursework = findViewById(R.id.layout_non_coursework);

        /*v1.0.6 new feature 00001*/
        checkHidden();
        checkPrivacy();

        FirebaseDatabase.getInstance().getReference().child(postType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child(key).child("title").getValue()));
                if (String.valueOf(dataSnapshot.child(key).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                    layoutBtn.setVisibility(View.VISIBLE);
//                    fabDeletePost.setVisibility(View.VISIBLE);
                    fabReportPost.setVisibility(View.GONE);
                } else {
                    layoutBtn.setVisibility(View.GONE);
                    fabDeletePost.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        fabActions(key,postType);
        loadFileDetails();

        mRecycler = (RecyclerView) findViewById(R.id.layout_recycler_rec);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        mRecyclerSub = findViewById(R.id.layout_recycler_sub_posts);
        mRecyclerSub.setHasFixedSize(false);
        mRecyclerSub.setNestedScrollingEnabled(false);
        mRecyclerSub.setLayoutManager(layoutManager2);

        filePlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playVideo();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*v1.0.6 new feature 00001*/
                if (!hideState){
                    mAlert.setTitle(R.string.title_hide_post)
                            .setMessage(R.string.warning_hide_post)
                            .setPositiveButton(R.string.option_hide, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    hidePost();
                                }
                            })
                            .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                } else {
                    mAlert.setTitle(R.string.title_unhide_post)
                            .setMessage(R.string.warning_unhide_post)
                            .setPositiveButton(R.string.option_unhide, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    hidePost();
                                }
                            })
                            .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }
            }
        });


        btnOpenPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAllPosts = FirebaseDatabase.getInstance().getReference().child(postType).child(key);
                mAllPosts.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                        String docName = String.valueOf(dataSnapshot.child("title").getValue());
                        Intent docIntent = new Intent(getApplicationContext(),ReadDocument.class);
                        docIntent.putExtra("filePath",filePath);
                        docIntent.putExtra("docName",docName);
                        docIntent.putExtra("postKey", key);
                        docIntent.putExtra("outgoing_intent","ViewPostActivity");
                        startActivity(docIntent);
                        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        loadAlsoViewed();
    }

    private void loadAlsoViewed(){
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType);

        FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_small,
                PostsViewHolder_SmallCard.class,
                mAllPosts.orderByChild("timestamp").limitToFirst(15)
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String key = getRef(position).getKey();

                mAllPosts.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (dataSnapshot.child("Category").getValue().equals(postType)){
                                FirebaseDatabase.getInstance().getReference().child(postType).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                                checkOwnershipStatus(key,postType);
                                                finish();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    public void loadSubPosts() {
//        layoutRecycler.setVisibility(View.VISIBLE);
        FirebaseRecyclerAdapter<File, PostsViewHolder_SubPostsCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SubPostsCard>(
                File.class,
                R.layout.card_sub_posts_items,
                PostsViewHolder_SubPostsCard.class,
                mSubPosts
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SubPostsCard viewHolder, File model, int position) {
                final String childKey = getRef(position).getKey();

                mSubPosts.child(childKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        viewHolder.setTitle(dataSnapshot.child("title").getValue().toString());
                        viewHolder.setTimestamp(dataSnapshot.child("timestamp").getValue().toString());
                        viewHolder.setType(dataSnapshot.child("file_type").getValue().toString());
                        viewHolder.setDescription(dataSnapshot.child("description").getValue().toString());
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(),ViewSubPostActivity.class);
                                intent.putExtra("file_key",key);
                                intent.putExtra("author",mAuthor);
                                intent.putExtra("postType",postType);
                                intent.putExtra("childKey",childKey);
                                startActivity(intent);
                                overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        mRecyclerSub.setAdapter(firebaseRecyclerAdapter);
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


    public void loadFileDetails() {
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(postType).child(key);
        mAllPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
                    txtInstitution.setText(dataSnapshot.child("institution").getValue().toString());
                    txtDept.setText(dataSnapshot.child("department").getValue().toString());
                    txtCourse.setText(dataSnapshot.child("course").getValue().toString());
                    layoutCoursework.setVisibility(View.VISIBLE);
                } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
                    txtTag.setText(String.valueOf(dataSnapshot.child("tag").getValue()));
                    layoutNonCoursework.setVisibility(View.VISIBLE);
                }
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtPrice.setText(String.valueOf(dataSnapshot.child("price").getValue()));
                txtBid.setText(String.valueOf(dataSnapshot.child("biddable").getValue()));
                fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child("title").getValue()));

                if (fileType.equals("image")){
                    layoutImage.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(filePath).into(imageView);
                    mProgress.dismiss();
                }
                else if (fileType.equals("audio") || fileType.equals("video")){
                    filePlaceholder.setVisibility(View.VISIBLE);
                    Bundle bundle = new Bundle();
                    bundle.putString("file_path", filePath);
                    PanelTopFragment topFragment = new PanelTopFragment();
                    topFragment.setArguments(bundle);
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction().replace(R.id.exoplayer_placeholder,topFragment,topFragment.getTag())
                            .commit();
                    mProgress.dismiss();
                }
                else if (fileType.equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkOwners(){
        final String[] owners = {""};
        FirebaseDatabase.getInstance().getReference().child(postType).child(key).child("owners").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    owners[0] = String.valueOf(dataSnapshot.getValue());
                    if (confirmOwners(owners[0])){
                        Toast.makeText(ViewPostActivity.this, R.string.info_cannot_remove_already_purchased,
                                Toast.LENGTH_LONG).show();
                    } else if (!confirmOwners(owners[0])){
                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).orderByChild("ItemId").equalTo(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()){
                                            removePost();
                                        } else {
                                            Toast.makeText(ViewPostActivity.this, R.string.info_cannot_remove_already_owned,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                if (dataSnapshot.child(ds.getKey()).child(key).exists()){
                                    Toast.makeText(ViewPostActivity.this, R.string.info_cannot_remove_already_owned, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (!dataSnapshot.child(ds.getKey()).child(key).exists()){
                                    //removePost();
                                    //Log.d("LOG_removePost()","removed 2");
                                }
                            }
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

    public boolean confirmOwners(String owner){
        if (owner.equals("")){
            return false;
        } else {
            return true;
        }
    }

    /*v1.0.6 new feature 00001*/
    public void checkHidden(){
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                hideState=false;
                                btnDelete.setText(R.string.option_hide);
//                                Log.d("LOG_checkHidden","condition1");
                            } else {
                                hideState=true;
                                btnDelete.setText(R.string.option_unhide);
//                                Log.d("LOG_checkHidden","condition2");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                hideState=false;
                                btnDelete.setText(R.string.option_hide);
//                                Log.d("LOG_checkHidden","condition3");
                            } else {
                                hideState=true;
                                btnDelete.setText(R.string.option_unhide);
//                                Log.d("LOG_checkHidden","condition4");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*v1.0.6 new feature 00001*/
    public void hidePost(){
        if (hideState){
            mProgress.setMessage(getApplicationContext().getResources().getString(R.string.info_unhiding_post));
            mProgress.setCancelable(false);
            mProgress.show();
            FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(key);
                    mAllPosts.child("Category").setValue(postType);
                    mAllPosts.child("FileType").setValue(dataSnapshot.child("file_type").getValue());
                    mAllPosts.child("ItemId").setValue(key);
                    mAllPosts.child("Title").setValue(dataSnapshot.child("title").getValue());

                    DatabaseReference mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType).child(key);
                    mAllPostsSplit.child("Category").setValue(postType);
                    mAllPostsSplit.child("FileType").setValue(dataSnapshot.child("file_type").getValue());
                    mAllPostsSplit.child("Tag").setValue(dataSnapshot.child("tag").getValue());
                    mAllPostsSplit.child("ItemId").setValue(key);
                    mAllPostsSplit.child("Title").setValue(dataSnapshot.child("title").getValue());

                    checkHidden();
                    Toast.makeText(ViewPostActivity.this, R.string.info_post_unhidden, Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            mProgress.setMessage(getApplicationContext().getResources().getString(R.string.info_hiding_post));
            mProgress.setCancelable(false);
            mProgress.show();
            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType).child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkHidden();
                            Toast.makeText(ViewPostActivity.this, R.string.info_post_hidden, Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    });
                }
            });
        }
    }

    public void removePost(){
        mProgress.setMessage(getString(R.string.info_deleting_post));
        mProgress.setCancelable(false);
        mProgress.show();
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(postType).child(key);
        mAllPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                String file_type = String.valueOf(dataSnapshot.child(diyKey).child("file_type").getValue());
//                if (file_type.equals("image")){
//
//                }
                if (dataSnapshot.child("thumbnail").exists() && !dataSnapshot.child("thumbnail").getValue().equals("")){
                    FirebaseStorage.getInstance().getReference().getStorage()
                            .getReferenceFromUrl(String.valueOf(dataSnapshot.child("thumbnail").getValue())).delete();
                }
                FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(String.valueOf(dataSnapshot.child("file_path").getValue()))
                        .delete();
                mAllPosts.removeValue();
                final DatabaseReference mAllItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(key);
                mAllItems.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            mAllItems.removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                final DatabaseReference mPublishedItem = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(key);
                mPublishedItem.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            mPublishedItem.removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                final DatabaseReference mOwnedItem = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).child(mAuth.getCurrentUser().getUid()).child(key);
                mOwnedItem.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            mOwnedItem.removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                final DatabaseReference mAllItemsSplit = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postType).child(key);
                mAllItemsSplit.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            mAllItemsSplit.removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mProgress.dismiss();
                Toast.makeText(ViewPostActivity.this, R.string.info_post_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                Toast.makeText(ViewPostActivity.this, R.string.error_error_occurred_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        FirebaseDatabase.getInstance().getReference().child(postType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (String.valueOf(dataSnapshot.child(key).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                    getMenuInflater().inflate(R.menu.view_post_context_menu_author, menu);
                } else {
                    getMenuInflater().inflate(R.menu.view_post_context_menu, menu);
                    if (String.valueOf(dataSnapshot.child(key).child("price").getValue()).equals("0")){
                        FirebaseDatabase.getInstance().getReference().child(postType).child(key).child("owners").setValue("all");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.report_post:
                Intent fabIntent = new Intent(getApplicationContext(),ReportPostActivity.class);
                fabIntent.putExtra("post_key", key);
                fabIntent.putExtra("outgoing_intent","ViewPostActivity");
                fabIntent.putExtra("postType",postType);
                startActivity(fabIntent);
                return true;
            case R.id.view_author:
                viewAuthor(key,postType);
                return true;
            case R.id.menu_delete_post:
                /*v1.0.6 new feature 00001*/
                if (!hideState){
                    mAlert.setTitle(R.string.title_hide_post)
                            .setMessage(R.string.warning_hide_post)
                            .setPositiveButton(R.string.option_hide, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    hidePost();
                                }
                            })
                            .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                } else {
                    mAlert.setTitle(R.string.title_unhide_post)
                            .setMessage(R.string.warning_unhide_post)
                            .setPositiveButton(R.string.option_unhide, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    hidePost();
                                }
                            })
                            .setNegativeButton(R.string.option_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .show();
                }
                return true;
            case R.id.menu_update_thumbnail:
                Log.d("LOG_menu","item clicked");
                FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("author").getValue().equals(mAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(getApplicationContext(),SetThumbnailActivity.class);
                                intent.putExtra("postKey",key)
                                        .putExtra("postCategory",postType);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return true;
            case R.id.menu_edit_post:
                FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("author").getValue().equals(mAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(getApplicationContext(),EditPostActivity.class);
                                intent.putExtra("postKey",key)
                                        .putExtra("postNode",postType);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return true;
            case R.id.menu_add_sub_post:
                FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("author").getValue().equals(mAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(getApplicationContext(),AddSubPostActivity.class);
                                intent.putExtra("postKey",key)
                                        .putExtra("postNode",postType)
                                        .putExtra("author",mAuthor);
                                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void getAuthorDetails(){
        FirebaseDatabase.getInstance().getReference().child(postType).child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAuthor = String.valueOf(dataSnapshot.child("author").getValue());
                Intent intent1 = new Intent( ViewPostActivity.this, ViewAuthorActivity.class );
                intent1.putExtra("author",mAuthor);
                ViewPostActivity.this.startActivity( intent1 );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void getAuthorDetails_Report(){
        FirebaseDatabase.getInstance().getReference().child(postType).child(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAuthor = String.valueOf(dataSnapshot.child("author").getValue());
                mTitle = String.valueOf(dataSnapshot.child("title"));
                Intent intent1 = new Intent( ViewPostActivity.this, ReportPostActivity.class );
                intent1.putExtra("author",mAuthor);
                intent1.putExtra("title",mTitle);
                ViewPostActivity.this.startActivity( intent1 );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkOwnership(){
        final DatabaseReference mOwnedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).child(mAuth.getCurrentUser().getUid()).child(key);
        mOwnedItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child(postType).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!String.valueOf(dataSnapshot.child(key).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                                mOwnedItems.child("ItemId").setValue(key);
                                mOwnedItems.child("Category").setValue(postType);
                                mOwnedItems.child("Title").setValue(dataSnapshot.child(key).child("title").getValue());
                                mOwnedItems.child("FileType").setValue(dataSnapshot.child(key).child("file_type").getValue());
                            }
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

    @Override
    protected void onResume() {
        super.onResume();
        checkPrivacy();
        loadAlsoViewed();
        mSubPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_sub)).child(postType).child(key);
        mSubPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    loadSubPosts();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void fabActions(final String key, final String category){
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NewNoteActivity.class);
                fabIntent.putExtra("post_key", ViewPostActivity.this.key);
                fabIntent.putExtra("outgoing_intent","ViewPostActivity");
                startActivity(fabIntent);
                overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
            }
        });

        fabViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NotesList.class);
                fabIntent.putExtra("post_key", ViewPostActivity.this.key);
                fabIntent.putExtra("outgoing_intent","ViewPostActivity");
                startActivity(fabIntent);
                overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
            }
        });

        fabViewAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Intent fabIntent = new Intent(getApplicationContext(),ViewAuthorActivity.class);
                        fabIntent.putExtra("post_key", ViewPostActivity.this.key);
                        fabIntent.putExtra("author_id", String.valueOf(dataSnapshot.child("author").getValue()));
                        FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                fabIntent.putExtra("author_name", String.valueOf(dataSnapshot.child("username").getValue()));
                                fabIntent.putExtra("outgoing_intent","ViewPostActivity");
                                startActivity(fabIntent);
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
        });

        fabReportPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),ReportPostActivity.class);
                fabIntent.putExtra("post_key", key);
                fabIntent.putExtra("outgoing_intent","ViewPostActivity");
                fabIntent.putExtra("postType","postType");
                startActivity(fabIntent);
            }
        });

        fabDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle(getString(R.string.title_delete_post))
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setPositiveButton(getString(R.string.option_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkOwners();
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    public void viewAuthor(final String key, final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Intent intent = new Intent(getApplicationContext(),ViewAuthorActivity.class);
                intent.putExtra("post_key", ViewPostActivity.this.key);
                intent.putExtra("author_id", String.valueOf(dataSnapshot.child("author").getValue()));
                FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        intent.putExtra("author_name", String.valueOf(dataSnapshot.child("username").getValue()));
                        intent.putExtra("outgoing_intent","ViewPostActivity");
                        startActivity(intent);
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    private void checkPrivacy(){
        FirebaseDatabase.getInstance().getReference().child(postType).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                if (!mAuth.getCurrentUser().getUid().equals(author)){
//                    Log.d("LOG_checkPrivacy()","Not author");
//                    Log.d("LOG_checkPrivacy()","--"+author+"--.--"+mAuth.getCurrentUser().getUid().toString()+"--");
//                    Log.d("LOG_dataSnapshot",String.valueOf(dataSnapshot));
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(author).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Published").exists() && dataSnapshot.child("Published").getValue().equals("false")){
                                layoutPrivate.setVisibility(View.VISIBLE);
                            } else {
                                checkOwnership();
//                                btnOpenTicket.setVisibility(View.VISIBLE);
//                                PanelTopFragment topFragment = new PanelTopFragment();
//                                FragmentManager manager = getSupportFragmentManager();
//                                manager.beginTransaction().replace(R.id.exoplayer_placeholder,topFragment,topFragment.getTag())
//                                        .commit();
//                                mProgress.dismiss();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    checkOwnership();
                    btnOpenPDF.setVisibility(View.VISIBLE);
//                    PanelTopFragment topFragment = new PanelTopFragment();
//                    FragmentManager manager = getSupportFragmentManager();
//                    manager.beginTransaction().replace(R.id.exoplayer_placeholder,topFragment,topFragment.getTag())
//                            .commit();
//                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            bottomHorizontalBar1.setVisibility(View.VISIBLE);
            bottomHorizontalBar2.setVisibility(View.VISIBLE);
//            SlidingUpPanelLayout.LayoutParams params = (SlidingUpPanelLayout.LayoutParams) slidingUpPanel.getLayoutParams();
//            params.height = 00;
//            params.width = params.MATCH_PARENT;
//            slidingUpPanel.setLayoutParams(params);
            slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            bottomHorizontalBar1.setVisibility(View.GONE);
            bottomHorizontalBar2.setVisibility(View.GONE);
//            SlidingUpPanelLayout.LayoutParams params = (SlidingUpPanelLayout.LayoutParams) slidingUpPanel.getLayoutParams();
//            params.height = (int)(450*getResources().getDisplayMetrics().density);
//            params.width = params.MATCH_PARENT;
//            slidingUpPanel.setLayoutParams(params);
            slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }
}
