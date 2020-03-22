package pesh.mori.learnerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewAnnouncementActivity extends AppCompatActivity {

    private LinearLayout layoutAudio,layoutVideo,layoutImage,layoutDoc,layoutFile,layoutDiy;
    private TextView txtTime,txtTitle,txtMessage,txtMoreInfo;
    private VideoView vidAudio,vidVideo;
    private ImageView btnPlayAudio,btnAudioIcon,btnPlayVideo;
    private ProgressBar mProgressAudio,mProgressVideo;
    private AppCompatButton btnOpenDoc;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mDatabaseRef;
    private StorageReference sStorage;
    private MediaController mediaController;
    private MediaPlayer mediaPlayer;
    private Uri mUri = null;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private String postKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcement);

        new HomeActivity().checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        postKey = getIntent().getExtras().getString("file_key");

        layoutAudio = findViewById(R.id.layout_1);
        layoutVideo = findViewById(R.id.layout_2);
        layoutImage = findViewById(R.id.layout_3);
        layoutDoc = findViewById(R.id.layout_4);

        txtTime = findViewById(R.id.txt_time);
        txtTitle = findViewById(R.id.txt_title);
        txtMessage = findViewById(R.id.txt_body);
        txtMoreInfo = findViewById(R.id.txt_more);

        vidAudio = findViewById(R.id.audio_view);
        vidVideo = findViewById(R.id.video_view);

        btnPlayAudio = findViewById(R.id.btn_play_audio);
        btnAudioIcon = findViewById(R.id.btn_audio_icon);
        btnPlayVideo = findViewById(R.id.btn_play_video);
        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        mProgressAudio = findViewById(R.id.progress_bar_audio);
        mProgressVideo = findViewById(R.id.progress_bar_video);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Announcements").child(postKey);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("AnnouncementsRef").child(postKey);

        txtMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink();
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

        btnOpenDoc = findViewById(R.id.btn_open_pdf);
        btnOpenDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                        String docName = String.valueOf(dataSnapshot.child("title").getValue());
                        Intent docIntent = new Intent(getApplicationContext(),ReadDocument.class);
                        docIntent.putExtra("filePath",filePath);
                        docIntent.putExtra("docName",docName);
                        docIntent.putExtra("postKey",postKey);
                        docIntent.putExtra("outgoing_intent","ViewAnnouncementActivity");
                        startActivity(docIntent);
                        overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        fetchValues();
    }

    private void openLink() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("link").exists() && !dataSnapshot.child("link").getValue().equals("")){
                    String url = String.valueOf(dataSnapshot.child("link").getValue());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchValues() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child("title").getValue()));
                txtMessage.setText(String.valueOf(dataSnapshot.child("body").getValue()));
                if (!dataSnapshot.child("link").exists() || dataSnapshot.child("link").getValue().toString().equals("")){
                    txtMoreInfo.setText("None at the moment");
                    txtMoreInfo.setTextColor(Color.parseColor("#cccccc"));
                } else {
                    SpannableString content = new SpannableString(String.valueOf(dataSnapshot.child("link").getValue()));
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                    txtMoreInfo.setText(content);
                }
                if (dataSnapshot.child("file_type").getValue().equals("audio")){
                    layoutAudio.setVisibility(View.VISIBLE);
                } else if (dataSnapshot.child("file_type").getValue().equals("video")){
                    layoutVideo.setVisibility(View.VISIBLE);
                } else if (dataSnapshot.child("file_type").getValue().equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void playAudio(){
        btnPlayAudio.setVisibility(View.GONE);
        mProgressAudio.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child("Announcements").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                vidAudio.setVideoURI(mUri);
                vidAudio.requestFocus();
                vidAudio.start();

                mProgressAudio.setVisibility(View.GONE);
                btnAudioIcon.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    vidAudio.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                mProgressAudio.setVisibility(View.VISIBLE);
                            } else {
                                mProgressAudio.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    });
                }
                vidAudio.setMediaController(mediaController);
                mediaController.setAnchorView(vidAudio);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void playVideo(){
        btnPlayVideo.setVisibility(View.GONE);
        mProgressVideo.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child("Announcements").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                vidVideo.setVideoURI(mUri);
                vidVideo.requestFocus();
                vidVideo.start();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    vidVideo.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                mProgressVideo.setVisibility(View.VISIBLE);
                            } else {
                                mProgressVideo.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    });
                }
                vidVideo.setMediaController(mediaController);
                mediaController.setAnchorView(layoutVideo);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
}
