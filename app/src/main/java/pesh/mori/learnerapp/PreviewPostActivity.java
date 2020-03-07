package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreviewPostActivity extends AppCompatActivity {
    private LinearLayout layoutAudio,layoutVideo,layoutImage,layoutDoc,layoutFile,layoutDiy;
    private TextView txtTime;
    private EditText txtTitle,txtPrice,txtDescription,txtFaculty,txtDepartment,txtCourse;
    private Spinner spinnerInstitution,spinnerTag;
    private RadioButton radioYes,radioNo;
    private VideoView vidAudio,vidVideo;
    private ImageView btnPlayAudio,btnAudioIcon,btnPlayVideo;
    private ProgressBar mProgressAudio,mProgressVideo;
    private AppCompatButton btnOpenDoc;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mAllPosts,mAllPostsSplit;
    private StorageReference sStorage;
    private String postKey="",postNode="",institution="",tag="";
    private List<String> institutionList,tagList;

    private MediaPlayer mediaPlayer;
    private MediaController mediaController;
    private Uri mUri = null;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private int PUBLISH_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setSubtitle("Not Yet Published");

        mAuth = FirebaseAuth.getInstance();

        postKey = getIntent().getExtras().getString("postKey");
        postNode = getIntent().getExtras().getString("postNode");

        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        layoutAudio = findViewById(R.id.layout_1);
        layoutVideo = findViewById(R.id.layout_2);
        layoutImage = findViewById(R.id.layout_3);
        layoutDoc = findViewById(R.id.layout_4);
        layoutFile = findViewById(R.id.layout_6);
        layoutDiy = findViewById(R.id.layout_7);

        txtTime = findViewById(R.id.txt_time);
        txtTitle = findViewById(R.id.txt_title);
        txtPrice = findViewById(R.id.txt_price);
        txtDescription = findViewById(R.id.txt_description);
        txtFaculty = findViewById(R.id.txt_faculty);
        txtDepartment = findViewById(R.id.txt_department);
        txtCourse = findViewById(R.id.txt_course);

        spinnerInstitution = findViewById(R.id.spinner_institution);
        spinnerTag = findViewById(R.id.spinner_tag);

        radioNo = findViewById(R.id.radio_no);
        radioYes = findViewById(R.id.radio_yes);

        vidAudio = findViewById(R.id.audio_view);
        vidVideo = findViewById(R.id.video_view);

        btnPlayAudio = findViewById(R.id.btn_play_audio);
        btnAudioIcon = findViewById(R.id.btn_audio_icon);
        btnPlayVideo = findViewById(R.id.btn_play_video);

        mProgressAudio = findViewById(R.id.progress_bar_audio);
        mProgressVideo = findViewById(R.id.progress_bar_video);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey);
        mAllPosts = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(postKey);
        mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child(postNode).child(postKey);

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
                        docIntent.putExtra("outgoing_intent","PreviewPostActivity");
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

    private void fetchValues() {
        if (postNode.equals("Files")){
            layoutFile.setVisibility(View.VISIBLE);
            layoutDiy.setVisibility(View.GONE);
        }
        if (postNode.equals("DIY")){
            layoutFile.setVisibility(View.GONE);
            layoutDiy.setVisibility(View.VISIBLE);
        }

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtPrice.setText(String.valueOf(dataSnapshot.child("price").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                if (dataSnapshot.child("file_type").getValue().equals("audio")){
                    layoutAudio.setVisibility(View.VISIBLE);
                } else if (dataSnapshot.child("file_type").getValue().equals("video")){
                    layoutVideo.setVisibility(View.VISIBLE);
                } else if (dataSnapshot.child("file_type").getValue().equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                }
                String bidding = String.valueOf(dataSnapshot.child("biddable").getValue());
                if (bidding.equals("no")){
                    radioNo.setChecked(true);
                } else if (bidding.equals("yes")){
                    radioYes.setChecked(true);
                }
                if (postNode.equals("Files")){
                    txtFaculty.setText(String.valueOf(dataSnapshot.child("school").getValue()));
                    txtDepartment.setText(String.valueOf(dataSnapshot.child("department").getValue()));
                    txtCourse.setText(String.valueOf(dataSnapshot.child("course").getValue()));
                    institution = String.valueOf(dataSnapshot.child("institution").getValue());
                    FirebaseDatabase.getInstance().getReference().child("Lists").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String institutions = String.valueOf(dataSnapshot.child("institutions_list").getValue());
                            institutionList = Arrays.asList(institutions.split("\\s*,\\s*"));
                            final List<String>getInstitution = new ArrayList<>();
                            final int institutionSize = institutionList.size();
                            for (int i=0;i<institutionSize;i++){
                                Object object = institutionList.get(i);
                                getInstitution.add(object.toString().trim());
                            }
                            ArrayAdapter<String> institutionAdapter = new ArrayAdapter<String>(PreviewPostActivity.this,android.R.layout.simple_spinner_item,getInstitution);
                            institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerInstitution.setAdapter(institutionAdapter);
                            spinnerInstitution.setSelection(institutionAdapter.getPosition(institution));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                if (postNode.equals("DIY")){
                    tag = String.valueOf(dataSnapshot.child("tag").getValue());
                    FirebaseDatabase.getInstance().getReference().child("Lists").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String tags = String.valueOf(dataSnapshot.child("tags_list").getValue());
                            tagList = Arrays.asList(tags.split("\\s*,\\s*"));
                            final List<String>getTags = new ArrayList<>();
                            final int facultySize = tagList.size();
                            for (int i=0;i<facultySize;i++){
                                Object object = tagList.get(i);
                                getTags.add(object.toString().trim());
                            }
                            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(PreviewPostActivity.this,android.R.layout.simple_spinner_item,getTags);
                            tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTag.setAdapter(tagsAdapter);
                            spinnerTag.setSelection(tagsAdapter.getPosition(tag));
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

    private boolean checkRequirements(){
        String title = txtTitle.getText().toString().trim();
        String price = txtPrice.getText().toString().trim();
        Double priceVal = 0.0;
        if (!price.isEmpty()){
            priceVal = Double.parseDouble(price);
        }
        String description = txtDescription.getText().toString().trim();
        String faculty = txtFaculty.getText().toString().trim();
        String department = txtDepartment.getText().toString().trim();
        String course = txtCourse.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Title must not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(price)){
            Toast.makeText(this, "Price must not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priceVal==0 && radioYes.isChecked()){
            Toast.makeText(this, "You cannot allow bidding on a free item", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Description must not be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!radioNo.isChecked() && !radioYes.isChecked()){
            Toast.makeText(this, "You must select whether the item is biddable or not", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (postNode.equals("Files")){
            String institution = spinnerInstitution.getSelectedItem().toString();
            if (TextUtils.isEmpty(faculty)){
                Toast.makeText(this, "Faculty must not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(department)){
                Toast.makeText(this, "Department must not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(course)){
                Toast.makeText(this, "Course must not be empty", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.equals(institution,"--University Institutions--") || TextUtils.equals(institution,"--Tertiary Institutions--")
                    || TextUtils.equals(institution,"--Artisan Institutions--")){
                Toast.makeText(this, "No institution selected", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (postNode.equals("DIY")){
            String tag = spinnerTag.getSelectedItem().toString();
            if (TextUtils.equals(tag,"")){
                Toast.makeText(this, "No tag selected", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void savePost(final String category, final Double price) {
        mProgress.setMessage("Saving post...");
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(postKey).child("Title")
                .setValue(txtTitle.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mDatabase.child("title").setValue(txtTitle.getText().toString().trim());
                    mDatabase.child("price").setValue(price);
                    mDatabase.child("description").setValue(txtDescription.getText().toString().trim());
                    if (radioNo.isChecked()){
                        mDatabase.child("biddable").setValue("no");
                    } else if (radioYes.isChecked()){
                        mDatabase.child("biddable").setValue("yes");
                    }
                    switch (category){
                        case "DIY":
                            mDatabase.child("tag").setValue(spinnerTag.getSelectedItem().toString());
                            break;

                        case "Files":
                            mDatabase.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                            mDatabase.child("department").setValue(txtDepartment.getText().toString().trim());
                            mDatabase.child("school").setValue(txtFaculty.getText().toString().trim());
                            mDatabase.child("course").setValue(txtCourse.getText().toString().trim());
                            break;
                    }
                    if (checkPublishState()){
                        publishPost();
                    } else {
                        Toast.makeText(PreviewPostActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                        if (mProgress.isShowing()){
                            mProgress.dismiss();
                        }
                    }
                } else {
                    Toast.makeText(PreviewPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
                    if (mProgress.isShowing()){
                        mProgress.dismiss();
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PreviewPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
                if (mProgress.isShowing()){
                    mProgress.dismiss();
                }
            }
        });
    }

    private boolean checkPublishState(){
        if (PUBLISH_STATE==0){
            return false;
        }
        return true;
    }

    private void publishPost(){
        FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(postKey).child("Published")
                .setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mAllPosts.child("ItemId").setValue(postKey);
                            mAllPosts.child("Category").setValue(postNode);
                            mAllPosts.child("Title").setValue(dataSnapshot.child("title").getValue());
                            mAllPosts.child("FileType").setValue(dataSnapshot.child("file_type").getValue());

                            mAllPostsSplit.child("ItemId").setValue(postKey);
                            mAllPostsSplit.child("Category").setValue(postNode);
                            mAllPostsSplit.child("Title").setValue(dataSnapshot.child("title").getValue());
                            if (postNode.equals("DIY")){
                                mAllPostsSplit.child("Tag").setValue(dataSnapshot.child("tag").getValue());
                            } else if (postNode.equals("Files")){
                                mAllPostsSplit.child("Institution").setValue(dataSnapshot.child("institution").getValue());
                            }
                            mAllPostsSplit.child("FileType").setValue(dataSnapshot.child("file_type").getValue()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    finish();
                                    Toast.makeText(PreviewPostActivity.this, "Your post has been published", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    public void playAudio(){
        btnPlayAudio.setVisibility(View.GONE);
        mProgressAudio.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

        FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void deletePost(){
        mProgress.setMessage("Deleting post...");
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(postKey)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            sStorage = FirebaseStorage.getInstance().getReference().getStorage()
                                    .getReferenceFromUrl(String.valueOf(dataSnapshot.child("file_path").getValue()));
                            if (dataSnapshot.child("thumbnail").exists() && !dataSnapshot.child("thumbnail").getValue().equals("")){
                                FirebaseStorage.getInstance().getReference().getStorage()
                                        .getReferenceFromUrl(String.valueOf(dataSnapshot.child("thumbnail").getValue())).delete();
                            }
                            sStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.removeValue();
                                    Toast.makeText(PreviewPostActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mDatabase.removeValue();
                                    Toast.makeText(PreviewPostActivity.this, "Post deleted", Toast.LENGTH_SHORT).show();
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mProgress.isShowing()){
                    mProgress.dismiss();
                }
                Toast.makeText(PreviewPostActivity.this, R.string.error_unable_to_delete, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview_post,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_new_thumbnail:
                Intent intent = new Intent(getApplicationContext(),SetThumbnailActivity.class);
                intent.putExtra("postCategory",postNode)
                        .putExtra("postKey",postKey);
                startActivity(intent);
                return true;
            case R.id.menu_save:
                mAlert.setTitle("Save post")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (checkRequirements()){
                                    savePost(postNode,Double.parseDouble(txtPrice.getText().toString().trim()));
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            case R.id.menu_delete_post:
                mAlert.setTitle("Delete post")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deletePost();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            case R.id.menu_publish:
                mAlert.setTitle("Publish post")
                        .setMessage("All changes will be saved. You will not have control of this post after publishing.\nAre you sure you want to publish?")
                        .setPositiveButton("Publish", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (checkRequirements()){
                                    PUBLISH_STATE = 1;
                                    savePost(postNode,Double.parseDouble(txtPrice.getText().toString().trim()));
                                }
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
}
