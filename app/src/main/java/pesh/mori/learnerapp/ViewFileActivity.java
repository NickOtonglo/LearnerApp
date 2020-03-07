package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ViewFileActivity extends AppCompatActivity {
    private FrameLayout layoutPrivate;
    private NestedScrollView scrollView;
    private TextView txtTitle,txtDescription,txtInstitution,txtFaculty,txtCourse,txtTime,txtPrice,txtBid;
    private ImageView imageView,staticIcon;
    private VideoView audioView,videoView;
    private Button btnOpenDoc;
    private ImageView btnPlay;
    private MediaPlayer mediaPlayer;
    private Uri mFileUri = null;

    private String fileKey;
    private DatabaseReference mFiles;
    private StorageReference sStorage;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn,layoutImage,layoutAudio,layoutVideo,layoutDoc;
    private Button btnDelete;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private String fileType,filePath;

    private ProgressBar progressBuffer,progressPosition;
    private ImageView btnPlayVideo;
    private TextView txtVidNow,txtVidEnd;

    private Boolean playState;

    private int current=0,duration=0;

    private ProgressBar progressBufferAudio,progressPositionAudio;
    private ImageView btnPlayAudio;
    private TextView txtAudNow,txtAudEnd;

    private String mAuthor="",author="";

    private MediaController mediaController;

    private RecyclerView mRecycler;

    private FloatingActionMenu fabMain;
    private FloatingActionButton fabAddNote,fabViewNotes,fabViewAuthor,fabReportPost,fabDeletePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

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

        mediaController = new MediaController(this);

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        fileKey = getIntent().getExtras().getString("file_key");

        mAuth = FirebaseAuth.getInstance();

        mediaPlayer = new MediaPlayer();

        //FABs
        fabMain = findViewById(R.id.fab_actions);
        fabAddNote = findViewById(R.id.fab_add_note);
        fabViewNotes = findViewById(R.id.fab_view_notes);
        fabViewAuthor = findViewById(R.id.fab_view_author);
        fabReportPost = findViewById(R.id.fab_report);
        fabDeletePost = findViewById(R.id.fab_delete_post);

        //AudioControls
        progressBufferAudio = findViewById(R.id.buffer_progress_view_file_audio);
//        progressPositionAudio = findViewById(R.id.progress_view_file_time_audio);
//        txtAudNow = findViewById(R.id.txt_view_file_time_start_audio);
//        txtAudEnd = findViewById(R.id.txt_view_file_time_stop_audio);
        btnPlayAudio = findViewById(R.id.btn_view_file_time_play_audio);
        btnPlayAudio.setVisibility(View.GONE);
//        progressBufferAudio.setVisibility(View.GONE);
//        progressPositionAudio.setMax(100);
        staticIcon = findViewById(R.id.btn_view_file_audio_static);

        //VideoControls
        progressBuffer = findViewById(R.id.buffer_progress_view_file);
//        progressPosition = findViewById(R.id.progress_view_file_time);
//        txtVidNow = findViewById(R.id.txt_view_file_time_start);
//        txtVidEnd = findViewById(R.id.txt_view_file_time_stop);
        btnPlayVideo = findViewById(R.id.btn_view_file_time_play);
        btnPlayVideo.setVisibility(View.GONE);
//        progressBuffer.setVisibility(View.GONE);
//        progressPosition.setMax(100);

        playState = false;

        btnPlay = findViewById(R.id.btn_view_file_play);
        imageView = findViewById(R.id.btn_view_file_select_image);
        layoutImage = findViewById(R.id.view_file_layout_1);
        audioView = findViewById(R.id.img_view_file_play_audio);
        layoutAudio = findViewById(R.id.view_file_layout_2);
        videoView = findViewById(R.id.img_view_file_play_video);
        layoutVideo = findViewById(R.id.view_file_layout_3);
        layoutDoc = findViewById(R.id.view_file_layout_5);
        btnOpenDoc = findViewById(R.id.btn_open_pdf);
        btnOpenDoc.setVisibility(View.GONE);

        layoutBtn = findViewById(R.id.layout_view_file_btn);
        btnDelete = findViewById(R.id.btn_view_file_delete);
        txtTitle = findViewById(R.id.txt_view_file_file_title);
        txtDescription = findViewById(R.id.txt_view_file_file_description);
        txtInstitution = findViewById(R.id.txt_view_file_file_institution);
        txtFaculty = findViewById(R.id.txt_view_file_file_faculty);
        txtCourse = findViewById(R.id.txt_view_file_file_course);
        txtTime = findViewById(R.id.txt_view_file_file_time);
        txtPrice = findViewById(R.id.txt_view_file_file_price);
        txtBid = findViewById(R.id.txt_view_file_file_bid);

        mRecycler = (RecyclerView) findViewById(R.id.layout_recycler_rec);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        checkPrivacy();

        FirebaseDatabase.getInstance().getReference().child("Files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
//                Log.d("AuthorCheck",String.valueOf(dataSnapshot.child(fileKey).child("author").getValue())+" "+mAuth.getCurrentUser().getUid());
                if (String.valueOf(dataSnapshot.child(fileKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
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

        fabActions(fileKey,"Files");
        loadFileDetails();
//        checkMediaState();
//        btnPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                checkMediaState();
//            }
//        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Delete post")
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

        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBuffer.setVisibility(View.VISIBLE);
                playAudio();
            }
        });

        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBuffer.setVisibility(View.VISIBLE);
                playVideo();
            }
        });

        btnOpenDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                        String docName = String.valueOf(dataSnapshot.child("title").getValue());
                        Intent docIntent = new Intent(getApplicationContext(),ReadDocument.class);
                        docIntent.putExtra("filePath",filePath);
                        docIntent.putExtra("docName",docName);
                        docIntent.putExtra("postKey",fileKey);
                        docIntent.putExtra("outgoing_intent","ViewFileActivity");
                        startActivity(docIntent);
                        overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAlsoViewed();
    }

    public void getAuthorDetails(){
        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAuthor = String.valueOf(dataSnapshot.child("file_type").getValue());
                author = mAuthor;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(getApplicationContext(), author, Toast.LENGTH_SHORT).show();
    }

    public void loadFileDetails() {

        mProgress.setMessage("Loading...");
        mProgress.setCancelable(false);
        mProgress.show();
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey);
        mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("LOG_dataSnapshot",String.valueOf(dataSnapshot));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                txtInstitution.setText(String.valueOf(dataSnapshot.child("institution").getValue()));
                txtFaculty.setText(String.valueOf(dataSnapshot.child("school").getValue()));
                txtCourse.setText(String.valueOf(dataSnapshot.child("course").getValue()));
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtPrice.setText(String.valueOf(dataSnapshot.child("price").getValue()));
                txtBid.setText(String.valueOf(dataSnapshot.child("biddable").getValue()));
                fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
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
                    btnPlay.setVisibility(View.GONE);
                    mProgress.dismiss();

                }
                else if (fileType.equals("video")){
                    layoutVideo.setVisibility(View.VISIBLE);
                    btnPlay.setVisibility(View.GONE);
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
        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).child("owners").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    owners[0] = String.valueOf(dataSnapshot.getValue());
                    if (confirmOwners(owners[0])){
                        Toast.makeText(ViewFileActivity.this, R.string.info_cannot_remove_already_purchased,
                                Toast.LENGTH_LONG).show();
                    } else if (!confirmOwners(owners[0])){
                        FirebaseDatabase.getInstance().getReference().child("OwnedItems").orderByChild("ItemId").equalTo(fileKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    removePost();
                                } else {
                                    Toast.makeText(ViewFileActivity.this, R.string.info_cannot_remove_already_owned,
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
                                if (dataSnapshot.child(ds.getKey()).child(fileKey).exists()){
                                    Toast.makeText(ViewFileActivity.this, R.string.info_cannot_remove_already_owned,Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (!dataSnapshot.child(ds.getKey()).child(fileKey).exists()){
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
        if (owner.equals("") || owner.equals("null")){
            return false;
        } else {
            return true;
        }
    }

    public void removePost(){
        mProgress.setMessage("Deleting post...");
        mProgress.setCancelable(false);
        mProgress.show();
        sStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey);
        mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
//                if (file_type.equals("image")){
//
//                }
                audioView.pause();
//                videoView.pause();
                if (dataSnapshot.child("thumbnail").exists() && !dataSnapshot.child("thumbnail").getValue().equals("")){
                    FirebaseStorage.getInstance().getReference().getStorage()
                            .getReferenceFromUrl(String.valueOf(dataSnapshot.child("thumbnail").getValue())).delete();
                }
                FirebaseStorage.getInstance().getReference().getStorage().getReferenceFromUrl(String.valueOf(dataSnapshot.child("file_path").getValue()))
                        .delete();
                mFiles.removeValue();
                final DatabaseReference mAllItems = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(fileKey);
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
                final DatabaseReference mOwnedItem = FirebaseDatabase.getInstance().getReference().child("OwnedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey);
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
                final DatabaseReference mPublishedItem = FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey);
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
                final DatabaseReference mAllItemsSplit = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("Files").child(fileKey);
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
                Toast.makeText(ViewFileActivity.this, R.string.info_post_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                Toast.makeText(ViewFileActivity.this, R.string.error_error_occured_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkMediaState(){

        if (mFileUri==null){

        } else {
            if (mediaPlayer.isPlaying()){
                btnPlay.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                mediaPlayer.stop();
            } else {
                btnPlay.setImageResource(R.mipmap.baseline_stop_white_18dp);
                //Mediafileinfo item = new Mediafileinfo();
                try {
                    // mediaPlayer.setDataSource(String.valueOf(myUri));
                    mediaPlayer.setDataSource(ViewFileActivity.this,mFileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        }
    }

    public void playAudio(){
        btnPlayAudio.setVisibility(View.GONE);
        progressBufferAudio.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void playVideo(){
        btnPlayVideo.setVisibility(View.GONE);
        progressBuffer.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
        FirebaseDatabase.getInstance().getReference().child("Files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (String.valueOf(dataSnapshot.child(fileKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                    getMenuInflater().inflate(R.menu.view_post_context_menu_author, menu);
                } else {
                    getMenuInflater().inflate(R.menu.view_post_context_menu, menu);
                    if (String.valueOf(dataSnapshot.child(fileKey).child("price").getValue()).equals("0")){
                        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).child("owners").setValue("all");
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
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("outgoing_intent","ViewFileActivity");
                startActivity(fabIntent);
                return true;
            case R.id.view_author:
                viewAuthor(fileKey,"Files");
                return true;
            case R.id.menu_delete_post:
                mAlert.setTitle("Delete post")
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
        mediaPlayer.stop();
    }

    public class VideoProgress extends AsyncTask<Void,Integer,Void>{

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

    public int getCurrentPositionInt(){
        if (mediaPlayer != null)
            return mediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    public void checkOwnership(){
        final DatabaseReference mOwnedItems = FirebaseDatabase.getInstance().getReference().child("OwnedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey);
        mOwnedItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    FirebaseDatabase.getInstance().getReference().child("Files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!String.valueOf(dataSnapshot.child(fileKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                                mOwnedItems.child("ItemId").setValue(fileKey);
                                mOwnedItems.child("Category").setValue("Files");
                                mOwnedItems.child("Title").setValue(dataSnapshot.child(fileKey).child("title").getValue());
                                mOwnedItems.child("FileType").setValue(dataSnapshot.child(fileKey).child("file_type").getValue());
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


    private void loadAlsoViewed(){

        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");

        FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_SmallCard>(
                File.class,
                R.layout.card_mydownloads_dark,
                PostsViewHolder_SmallCard.class,
                mFiles.orderByChild("timestamp").limitToFirst(10)
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_SmallCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAlsoViewed();
        checkPrivacy();
    }

    public void fabActions(final String key,final String category){
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NewNoteActivity.class);
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("outgoing_intent","ViewFileActivity");
                startActivity(fabIntent);
                overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });

        fabViewNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),NotesList.class);
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("outgoing_intent","ViewFileActivity");
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
                        fabIntent.putExtra("post_key",fileKey);
                        fabIntent.putExtra("author_id",String.valueOf(dataSnapshot.child("author").getValue()));
                        FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addListenerForSingleValueEvent(new ValueEventListener() {
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
        });

        fabReportPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fabIntent = new Intent(getApplicationContext(),ReportPostActivity.class);
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("outgoing_intent","ViewFileActivity");
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
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("author_id",String.valueOf(dataSnapshot.child("author").getValue()));
                FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addListenerForSingleValueEvent(new ValueEventListener() {
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
        FirebaseDatabase.getInstance().getReference().child("Files").child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                if (!mAuth.getCurrentUser().getUid().equals(author)){
                    Log.d("LOG_checkPrivacy()","Not author");
//                    Log.d("LOG_checkPrivacy()","--"+author+"--.--"+mAuth.getCurrentUser().getUid().toString()+"--");
                    FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(author).child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
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
