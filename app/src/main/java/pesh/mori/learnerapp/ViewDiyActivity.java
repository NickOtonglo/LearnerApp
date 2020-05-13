package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class ViewDiyActivity extends AppCompatActivity {
    private FrameLayout layoutPrivate;
    private NestedScrollView scrollView;
    private TextView txtTitle,txtDescription,txtTag,txtTime,txtPrice,txtBid;

    private String diyKey,mAuthor,mTitle;
    private DatabaseReference mDiy;
    private StorageReference sStorage;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn,layoutImage,layoutAudio,layoutVideo,layoutDoc;
    private Button btnDelete;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private ImageView imageView,staticIcon;
    private VideoView audioView,videoView;
    private Button btnOpenDoc;
    private ImageView btnPlay;

    private MediaPlayer mediaPlayer;
    private Uri mFileUri = null;

    private String fileType,filePath,vTag;

    private ProgressBar progressBuffer,progressPosition;
    private ImageView btnPlayVideo;
    private TextView txtVidNow,txtVidEnd;

    /*v1.0.6 new feature 00001*/
    private Boolean playState,hideState;

    private int current=0,duration=0;

    private ProgressBar progressBufferAudio,progressPositionAudio;
    private ImageView btnPlayAudio;
    private TextView txtAudNow,txtAudEnd;
    private RecyclerView mRecycler;

    private MediaController mediaController;

    FloatingActionMenu fabMain;
    private FloatingActionButton fabAddNote,fabViewNotes,fabViewAuthor,fabReportPost,fabDeletePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_diy);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        diyKey = getIntent().getExtras().getString("file_key");
        mAuthor = getIntent().getExtras().getString("author");
        vTag = getIntent().getExtras().getString("tag");

        mAuth = FirebaseAuth.getInstance();

        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        //FABs
        fabAddNote = findViewById(R.id.fab_add_note);
        fabViewNotes = findViewById(R.id.fab_view_notes);
        fabViewAuthor = findViewById(R.id.fab_view_author);
        fabReportPost = findViewById(R.id.fab_report);
        fabDeletePost = findViewById(R.id.fab_delete_post);

        //AudioControls
        progressBufferAudio = findViewById(R.id.buffer_progress_view_diy_audio);
//        progressPositionAudio = findViewById(R.id.progress_view_diy_time_audio);
//        txtAudNow = findViewById(R.id.txt_view_diy_time_start_audio);
//        txtAudEnd = findViewById(R.id.txt_view_diy_time_stop_audio);
        btnPlayAudio = findViewById(R.id.btn_view_diy_time_play_audio);
        btnPlayAudio.setVisibility(View.GONE);
//        progressBufferAudio.setVisibility(View.GONE);
//        progressPositionAudio.setMax(100);
        staticIcon = findViewById(R.id.btn_view_diy_audio_static);

        //VideoControls
        progressBuffer = findViewById(R.id.buffer_progress_view_diy);
//        progressPosition = findViewById(R.id.progress_view_diy_time);
//        txtVidNow = findViewById(R.id.txt_view_diy_time_start);
//        txtVidEnd = findViewById(R.id.txt_view_diy_time_stop);
        btnPlayVideo = findViewById(R.id.btn_view_diy_time_play);
        btnPlayVideo.setVisibility(View.GONE);
//        progressBuffer.setVisibility(View.GONE);
//        progressPosition.setMax(100);

        playState = false;

        imageView = findViewById(R.id.btn_view_diy_select_image);
        layoutImage = findViewById(R.id.view_diy_layout_1);
        audioView = findViewById(R.id.img_view_diy_play_audio);
        layoutAudio = findViewById(R.id.view_diy_layout_2);
        videoView = findViewById(R.id.img_view_diy_play_video);
        layoutVideo = findViewById(R.id.view_diy_layout_3);
        layoutDoc = findViewById(R.id.view_diy_layout_5);
        btnOpenDoc = findViewById(R.id.btn_open_pdf_diy);
        btnOpenDoc.setVisibility(View.GONE);

        layoutBtn = findViewById(R.id.layout_view_diy_btn);
        btnDelete = findViewById(R.id.btn_view_diy_delete);
        txtTitle = findViewById(R.id.txt_view_diy_diy_title);
        txtTag = findViewById(R.id.txt_view_diy_diy_tag);
        txtDescription = findViewById(R.id.txt_view_diy_diy_description);
        txtTime = findViewById(R.id.txt_view_diy_diy_time);
        txtPrice = findViewById(R.id.txt_view_diy_diy_price);
        txtBid = findViewById(R.id.txt_view_diy_diy_bid);

        /*v1.0.6 new feature 00001*/
        checkHidden();
        checkPrivacy();

        FirebaseDatabase.getInstance().getReference().child("DIY").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child(diyKey).child("title").getValue()));
                if (String.valueOf(dataSnapshot.child(diyKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
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

        fabActions(diyKey,"DIY");
        loadFileDetails();

        mRecycler = (RecyclerView) findViewById(R.id.layout_recycler_rec);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playVideo();
            }
        });

        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });

        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
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


        btnOpenDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiy = FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey);
                mDiy.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                        String docName = String.valueOf(dataSnapshot.child("title").getValue());
                        Intent docIntent = new Intent(getApplicationContext(),ReadDocument.class);
                        docIntent.putExtra("filePath",filePath);
                        docIntent.putExtra("docName",docName);
                        docIntent.putExtra("postKey",diyKey);
                        docIntent.putExtra("outgoing_intent","ViewDiyActivity");
                        startActivity(docIntent);
                        overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
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

        mDiy = FirebaseDatabase.getInstance().getReference().child("DIY");

        FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads_dark,
                PostsViewHolder_SmallCard.class,
                mDiy.orderByChild("timestamp").limitToFirst(10)
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                mDiy.addValueEventListener(new ValueEventListener() {
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
                                checkOwnershipStatus(fileKey,"DIY");
                                finish();
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
                        transactionsIntent.putExtra("outgoing_intent","mydownloads");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }  else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
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
                        startActivity(viewFileIntent);
                    } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void loadFileDetails() {
        mDiy = FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey);
        mDiy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                txtTag.setText(String.valueOf(dataSnapshot.child("tag").getValue()));
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
                else if (fileType.equals("audio")){
//                    layoutAudio.setVisibility(View.VISIBLE);
//                    btnPlay.setVisibility(View.VISIBLE);
//                    try {
//                        if (!audioView.isPlaying()){
//                            mFileUri = Uri.parse(filePath);
//                            audioView.setVideoURI(mFileUri);
//                            audioView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                @Override
//                                public void onCompletion(MediaPlayer mediaPlayer) {
//                                    btnPlay.setImageResource(R.mipmap.baseline_stop_white_18dp);
//                                }
//                            });
//                        } else {
//                            audioView.pause();
//                            btnPlay.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
//                        }
//
//                    } catch (Exception e){
//
//                    }
//                    audioView.requestFocus();
//                    audioView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mediaPlayer) {
//                            mProgress.dismiss();
//                        }
//                    });
                    layoutAudio.setVisibility(View.VISIBLE);
                    mProgress.dismiss();

                }
                else if (fileType.equals("video")){
                    layoutVideo.setVisibility(View.VISIBLE);
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
        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).child("owners").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    owners[0] = String.valueOf(dataSnapshot.getValue());
                    if (confirmOwners(owners[0])){
                        Toast.makeText(ViewDiyActivity.this, R.string.info_cannot_remove_already_purchased,
                                Toast.LENGTH_LONG).show();
                    } else if (!confirmOwners(owners[0])){
                        FirebaseDatabase.getInstance().getReference().child("OwnedItems").orderByChild("ItemId").equalTo(diyKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.exists()){
                                            removePost();
                                        } else {
                                            Toast.makeText(ViewDiyActivity.this, R.string.info_cannot_remove_already_owned,
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                } else {
                    FirebaseDatabase.getInstance().getReference().child("OwnedItems").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                if (dataSnapshot.child(ds.getKey()).child(diyKey).exists()){
                                    Toast.makeText(ViewDiyActivity.this, R.string.info_cannot_remove_already_owned,Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (!dataSnapshot.child(ds.getKey()).child(diyKey).exists()){
                                    //removePost();
                                    Log.d("LOG_removePost()","removed 2");
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
        FirebaseDatabase.getInstance().getReference().child("AllPosts").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                hideState=false;
                                btnDelete.setText(R.string.option_hide);
                                Log.d("LOG_checkHidden","condition1");
                            } else {
                                hideState=true;
                                btnDelete.setText(R.string.option_unhide);
                                Log.d("LOG_checkHidden","condition2");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                hideState=false;
                                btnDelete.setText(R.string.option_hide);
                                Log.d("LOG_checkHidden","condition3");
                            } else {
                                hideState=true;
                                btnDelete.setText(R.string.option_unhide);
                                Log.d("LOG_checkHidden","condition4");
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
            FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference mAllPosts = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(diyKey);
                    mAllPosts.child("Category").setValue("DIY");
                    mAllPosts.child("FileType").setValue(dataSnapshot.child("file_type").getValue());
                    mAllPosts.child("ItemId").setValue(diyKey);
                    mAllPosts.child("Title").setValue(dataSnapshot.child("title").getValue());

                    DatabaseReference mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY").child(diyKey);
                    mAllPostsSplit.child("Category").setValue("DIY");
                    mAllPostsSplit.child("FileType").setValue(dataSnapshot.child("file_type").getValue());
                    mAllPostsSplit.child("Tag").setValue(dataSnapshot.child("tag").getValue());
                    mAllPostsSplit.child("ItemId").setValue(diyKey);
                    mAllPostsSplit.child("Title").setValue(dataSnapshot.child("title").getValue());

                    checkHidden();
                    Toast.makeText(ViewDiyActivity.this, R.string.info_post_unhidden, Toast.LENGTH_SHORT).show();
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
            FirebaseDatabase.getInstance().getReference().child("AllPosts").child(diyKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY").child(diyKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkHidden();
                            Toast.makeText(ViewDiyActivity.this, R.string.info_post_hidden, Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    });
                }
            });
        }
    }

    public void removePost(){
        mProgress.setMessage("Deleting post...");
        mProgress.setCancelable(false);
        mProgress.show();
        sStorage = FirebaseStorage.getInstance().getReference().child("DIY").child(mAuth.getCurrentUser().getUid());
        mDiy = FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey);
        mDiy.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                String file_type = String.valueOf(dataSnapshot.child(diyKey).child("file_type").getValue());
//                if (file_type.equals("image")){
//
//                }
                audioView.pause();
                if (dataSnapshot.child("thumbnail").exists() && !dataSnapshot.child("thumbnail").getValue().equals("")){
                    FirebaseStorage.getInstance().getReference().getStorage()
                            .getReferenceFromUrl(String.valueOf(dataSnapshot.child("thumbnail").getValue())).delete();
                }
                FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(String.valueOf(dataSnapshot.child("file_path").getValue()))
                        .delete();
                mDiy.removeValue();
                final DatabaseReference mAllItems = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(diyKey);
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
                final DatabaseReference mPublishedItem = FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(diyKey);
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
                final DatabaseReference mOwnedItem = FirebaseDatabase.getInstance().getReference().child("OwnedItems").child(mAuth.getCurrentUser().getUid()).child(diyKey);
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
                final DatabaseReference mAllItemsSplit = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("DIY").child(diyKey);
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
                Toast.makeText(ViewDiyActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                Toast.makeText(ViewDiyActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void playAudio(){
        btnPlayAudio.setVisibility(View.GONE);
        progressBufferAudio.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFileUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                audioView.setVideoURI(mFileUri);
                audioView.requestFocus();
                audioView.start();

                progressBufferAudio.setVisibility(View.GONE);
                staticIcon.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    audioView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                progressBufferAudio.setVisibility(View.VISIBLE);
                            } else {
                                progressBufferAudio.setVisibility(View.GONE);
                            }

                            return false;
                        }
                    });
                }
                audioView.setMediaController(mediaController);
                mediaController.setAnchorView(audioView);

//                audioView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mediaPlayer) {
//                        duration = mediaPlayer.getDuration()/1000;
//                        String durationString = String.format("%02d:%02d",duration/60,duration%60);
//                        txtAudEnd.setText(durationString);
//
//                    }
//                });
//
//                audioView.start();
//                playState = true;
//                btnPlayAudio.setImageResource(R.drawable.baseline_pause_white_24);
//                new AudioProgress().execute();
//
//                btnPlayAudio.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (playState){
//                            audioView.pause();
//                            playState = false;
//                            btnPlayAudio.setImageResource(R.drawable.baseline_play_arrow_white_24);
//                        } else {
//                            audioView.start();
//                            playState = true;
//                            btnPlayAudio.setImageResource(R.drawable.baseline_pause_white_24);
//                        }
//                    }
//                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void playVideo(){
        btnPlayVideo.setVisibility(View.GONE);
        progressBuffer.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFileUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                videoView.setVideoURI(mFileUri);
                videoView.requestFocus();
                videoView.start();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                progressBuffer.setVisibility(View.VISIBLE);
                            } else {
                                progressBuffer.setVisibility(View.GONE);
                            }

                            return false;
                        }
                    });
                }

                videoView.setMediaController(mediaController);
                mediaController.setAnchorView(layoutVideo);

//                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mediaPlayer) {
//                        duration = mediaPlayer.getDuration()/1000;
//                        String durationString = String.format("%02d:%02d",duration/60,duration%60);
//                        txtVidEnd.setText(durationString);
//
//                    }
//                });
//
//                videoView.start();
//                playState = true;
//                btnPlayVideo.setImageResource(R.drawable.baseline_pause_white_24);
//                new VideoProgress().execute();
//
//                btnPlayVideo.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (playState){
//                            videoView.pause();
//                            playState = false;
//                            btnPlayVideo.setImageResource(R.drawable.baseline_play_arrow_white_24);
//                        } else {
//                            videoView.start();
//                            playState = true;
//                            btnPlayVideo.setImageResource(R.drawable.baseline_pause_white_24);
//                        }
//                    }
//                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        FirebaseDatabase.getInstance().getReference().child("DIY").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (String.valueOf(dataSnapshot.child(diyKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                    getMenuInflater().inflate(R.menu.view_post_context_menu_author, menu);
                } else {
                    getMenuInflater().inflate(R.menu.view_post_context_menu, menu);
                    if (String.valueOf(dataSnapshot.child(diyKey).child("price").getValue()).equals("0")){
                        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).child("owners").setValue("all");
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
                fabIntent.putExtra("post_key",diyKey);
                fabIntent.putExtra("outgoing_intent","ViewDiyActivity");
                startActivity(fabIntent);
                return true;
            case R.id.view_author:
                viewAuthor(diyKey,"DIY");
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        playState = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class VideoProgress extends AsyncTask<Void,Integer,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            do {
                if (playState){
                    current = videoView.getCurrentPosition()/1000;
                    publishProgress(current);
                }
            } while (progressPosition.getProgress()<=100);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {
                int currentPercent = current * 100 / duration;
                progressPosition.setProgress(currentPercent);
                String currentString = String.format("%02d:%02d", values[0] / 60, values[0] % 60);
                txtVidNow.setText(currentString);

            } catch (Exception e) {

            }

        }
    }

    public class AudioProgress extends AsyncTask<Void,Integer,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            do {
                if (playState){
                    current = audioView.getCurrentPosition()/1000;
                    publishProgress(current);
                }
            } while (progressPositionAudio.getProgress()<=100);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {
                int currentPercent = current * 100 / duration;
                progressPositionAudio.setProgress(currentPercent);
                String currentString = String.format("%02d:%02d", values[0] / 60, values[0] % 60);
                txtAudNow.setText(currentString);

            } catch (Exception e) {

            }

        }
    }
    public void getAuthorDetails(){
        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAuthor = String.valueOf(dataSnapshot.child("author").getValue());
                Intent intent1 = new Intent( ViewDiyActivity.this, ViewAuthorActivity.class );
                intent1.putExtra("author",mAuthor);
                ViewDiyActivity.this.startActivity( intent1 );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void getAuthorDetails_Report(){
        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAuthor = String.valueOf(dataSnapshot.child("author").getValue());
                mTitle = String.valueOf(dataSnapshot.child("title"));
                Intent intent1 = new Intent( ViewDiyActivity.this, ReportPostActivity.class );
                intent1.putExtra("author",mAuthor);
                intent1.putExtra("title",mTitle);
                ViewDiyActivity.this.startActivity( intent1 );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkOwnership(){
        final DatabaseReference mOwnedItems = FirebaseDatabase.getInstance().getReference().child("OwnedItems").child(mAuth.getCurrentUser().getUid()).child(diyKey);
        mOwnedItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child("DIY").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!String.valueOf(dataSnapshot.child(diyKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                                mOwnedItems.child("ItemId").setValue(diyKey);
                                mOwnedItems.child("Category").setValue("DIY");
                                mOwnedItems.child("Title").setValue(dataSnapshot.child(diyKey).child("title").getValue());
                                mOwnedItems.child("FileType").setValue(dataSnapshot.child(diyKey).child("file_type").getValue());
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
    }

    public void fabActions(final String key, final String category){
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NewNoteActivity.class);
                fabIntent.putExtra("post_key",diyKey);
                fabIntent.putExtra("outgoing_intent","ViewDiyActivity");
                startActivity(fabIntent);
                overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });

        fabViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NotesList.class);
                fabIntent.putExtra("post_key",diyKey);
                fabIntent.putExtra("outgoing_intent","ViewDiyActivity");
                startActivity(fabIntent);
                overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });

        fabViewAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Intent fabIntent = new Intent(getApplicationContext(),ViewAuthorActivity.class);
                        fabIntent.putExtra("post_key",diyKey);
                        fabIntent.putExtra("author_id",String.valueOf(dataSnapshot.child("author").getValue()));
                        FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                fabIntent.putExtra("author_name",String.valueOf(dataSnapshot.child("username").getValue()));
                                fabIntent.putExtra("outgoing_intent","ViewDiyActivity");
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
                fabIntent.putExtra("post_key",diyKey);
                fabIntent.putExtra("outgoing_intent","ViewDiyActivity");
                startActivity(fabIntent);
            }
        });

        fabDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Delete file")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkOwners();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    public void viewAuthor(final String key,final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Intent fabIntent = new Intent(getApplicationContext(),ViewAuthorActivity.class);
                fabIntent.putExtra("post_key",diyKey);
                fabIntent.putExtra("author_id",String.valueOf(dataSnapshot.child("author").getValue()));
                FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fabIntent.putExtra("author_name",String.valueOf(dataSnapshot.child("username").getValue()));
                        fabIntent.putExtra("outgoing_intent","ViewFileActivity");
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

    private void checkPrivacy(){
        FirebaseDatabase.getInstance().getReference().child("DIY").child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                if (!mAuth.getCurrentUser().getUid().equals(author)){
//                    Log.d("LOG_checkPrivacy()","Not author");
//                    Log.d("LOG_checkPrivacy()","--"+author+"--.--"+mAuth.getCurrentUser().getUid().toString()+"--");
//                    Log.d("LOG_dataSnapshot",String.valueOf(dataSnapshot));
                    FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(author).child(diyKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Published").exists() && dataSnapshot.child("Published").getValue().equals("false")){
                                layoutPrivate.setVisibility(View.VISIBLE);
                            } else {
                                checkOwnership();
                                btnOpenDoc.setVisibility(View.VISIBLE);
                                btnPlayAudio.setVisibility(View.VISIBLE);
                                btnPlayVideo.setVisibility(View.VISIBLE);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    checkOwnership();
                    btnOpenDoc.setVisibility(View.VISIBLE);
                    btnPlayAudio.setVisibility(View.VISIBLE);
                    btnPlayVideo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
