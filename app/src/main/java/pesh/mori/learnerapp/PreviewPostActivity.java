package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
    private LinearLayout layoutAudio,layoutVideo, layoutDoc,layoutCat9,layoutCat8;
    private TextView txtTime;
    private EditText txtTitle,txtPrice,txtDescription;
    private EditText txtCourse, txtDept,txtUnit;
    private Spinner spinnerList9,spinnerList8;
    private RadioButton radioYes,radioNo;
    private VideoView vidAudio,vidVideo;
    private ImageView btnPlayAudio,btnAudioIcon,btnPlayVideo;
    private ProgressBar mProgressAudio,mProgressVideo;
    private AppCompatButton btnOpenDoc;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mAllPosts,mAllPostsSplit;
    private StorageReference sStorage;
    private String postKey="",postNode="", selectedItem ="";
    private List<String> itemsList;

    private MediaController mediaController;
    private Uri mUri = null;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private int PUBLISH_STATE = 0;

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
        setContentView(R.layout.activity_preview_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setSubtitle(getString(R.string.title_not_yet_published));

        mAuth = FirebaseAuth.getInstance();

        postKey = getIntent().getExtras().getString("postKey");
        postNode = getIntent().getExtras().getString("postNode");

        mediaController = new MediaController(this);

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);

        layoutAudio = findViewById(R.id.layout_1);
        layoutVideo = findViewById(R.id.layout_2);
        layoutDoc = findViewById(R.id.layout_4);
        layoutCat9 = findViewById(R.id.layout_7);
        layoutCat8 = findViewById(R.id.layout_8);

        txtTitle = findViewById(R.id.txt_title);
        txtPrice = findViewById(R.id.txt_price);
        txtDescription = findViewById(R.id.txt_description);
        txtTime = findViewById(R.id.txt_time);

        txtCourse = findViewById(R.id.txt_course);
        txtDept = findViewById(R.id.txt_department);
        txtUnit = findViewById(R.id.txt_unit);

        spinnerList9 = findViewById(R.id.spinner_file_types);
        spinnerList8 = findViewById(R.id.spinner_category_8_list);

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
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(postKey);
        mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split)).child(postNode).child(postKey);

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
                        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
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
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
            layoutCat8.setVisibility(View.VISIBLE);
            layoutCat9.setVisibility(View.GONE);
        } else if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
            layoutCat9.setVisibility(View.VISIBLE);
            layoutCat8.setVisibility(View.GONE);
        }

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(" "+dataSnapshot.child("timestamp").getValue());
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
                //PostsNonCoursework
                if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
                    selectedItem = String.valueOf(dataSnapshot.child("tag").getValue());
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String tags = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_9)).getValue());
                            itemsList = Arrays.asList(tags.split("\\s*,\\s*"));
                            final List<String> getTags = new ArrayList<>();
                            final int facultySize = itemsList.size();
                            for (int i=0;i<facultySize;i++){
                                Object object = itemsList.get(i);
                                getTags.add(object.toString().trim());
                            }
                            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(PreviewPostActivity.this,android.R.layout.simple_spinner_item,getTags);
                            tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerList9.setAdapter(tagsAdapter);
                            spinnerList9.setSelection(tagsAdapter.getPosition(selectedItem));
                            if (mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //PostsCoursework
                } else if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
                    selectedItem = String.valueOf(dataSnapshot.child("institution").getValue());
                    txtCourse.setText(String.valueOf(dataSnapshot.child("course").getValue()));
                    txtDept.setText(String.valueOf(dataSnapshot.child("department").getValue()));
                    txtUnit.setText(String.valueOf(dataSnapshot.child("unit").getValue()));
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String tags = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_8)).getValue());
                            itemsList = Arrays.asList(tags.split("\\s*,\\s*"));
                            final List<String> getTags = new ArrayList<>();
                            final int facultySize = itemsList.size();
                            for (int i=0;i<facultySize;i++){
                                Object object = itemsList.get(i);
                                getTags.add(object.toString().trim());
                            }
                            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(PreviewPostActivity.this,android.R.layout.simple_spinner_item,getTags);
                            tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerList8.setAdapter(tagsAdapter);
                            spinnerList8.setSelection(tagsAdapter.getPosition(selectedItem));
                            if (mProgress.isShowing()){
                                mProgress.dismiss();
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

    private boolean checkRequirements(){
        String title = txtTitle.getText().toString().trim();
        String price = txtPrice.getText().toString().trim();
        Double priceVal = 0.0;
        if (!price.isEmpty()){
            priceVal = Double.parseDouble(price);
        }
        String description = txtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, R.string.info_title_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(price)){
            Toast.makeText(this, R.string.info_price_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priceVal==0 && radioYes.isChecked()){
            Toast.makeText(this, R.string.info_you_cannot_allow_bidding_on_a_free_item, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(description)){
            Toast.makeText(this, R.string.info_description_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!radioNo.isChecked() && !radioYes.isChecked()){
            Toast.makeText(this, R.string.info_you_must_select_if_item_is_biddable_or_not, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
            String tag = spinnerList9.getSelectedItem().toString();
            if (TextUtils.equals(tag,"")){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_no_category_selected,Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
            String course = txtCourse.getText().toString().trim();
            String department = txtDept.getText().toString().trim();
            String unit = txtUnit.getText().toString().trim();
            String institution = spinnerList8.getSelectedItem().toString();
            if(institution.equals("--University Institutions--")
                    || institution.equals("--Tertiary Institutions--")
                    || institution.equals("--Artisan Institutions--")){
                Snackbar.make(findViewById(android.R.id.content), R.string.you_must_select_a_valid_institution,Snackbar.LENGTH_LONG).show();
                return false;
            }
            if (course.isEmpty() || department.isEmpty() || unit.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private void savePost(final String category, final Double price) {
        mProgress.setMessage(getString(R.string.info_saving_post));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey).child("Title")
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
                    if (category.equals(getString(R.string.firebase_ref_posts_type_1))){
                        mDatabase.child("institution").setValue(spinnerList8.getSelectedItem().toString());
                        mDatabase.child("unit").setValue(txtUnit.getText().toString());
                        mDatabase.child("department").setValue(txtDept.getText().toString());
                        mDatabase.child("course").setValue(txtCourse.getText().toString());
                    } else if (category.equals(getString(R.string.firebase_ref_posts_type_2))){
                        mDatabase.child("tag").setValue(spinnerList9.getSelectedItem().toString());
                    }
//                    switch (category){
//                        case "PostsMedia":
//
//                            break;
//                    }
                    if (checkPublishState()){
                        publishPost();
                    } else {
                        Toast.makeText(PreviewPostActivity.this, R.string.info_saved, Toast.LENGTH_SHORT).show();
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
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey).child("Published")
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
                            if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
                                mAllPostsSplit.child("Institution").setValue(dataSnapshot.child("institution").getValue());
                            } else if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
                                mAllPostsSplit.child("Tag").setValue(dataSnapshot.child("tag").getValue());
                            }
                            mAllPostsSplit.child("FileType").setValue(dataSnapshot.child("file_type").getValue()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    finish();
                                    Toast.makeText(PreviewPostActivity.this, R.string.info_your_post_has_been_published, Toast.LENGTH_SHORT).show();
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
                vidVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
//                        Log.d("LOG_vidVideo",mUri.toString());
                        return false;
                    }
                });

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
        mProgress.setMessage(getString(R.string.info_deleting_post));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey)
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
                                    Toast.makeText(PreviewPostActivity.this, R.string.info_post_deleted, Toast.LENGTH_SHORT).show();
                                    if (mProgress.isShowing()){
                                        mProgress.dismiss();
                                    }
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    mDatabase.removeValue();
                                    Toast.makeText(PreviewPostActivity.this, R.string.info_post_deleted, Toast.LENGTH_SHORT).show();
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
                mAlert.setTitle(R.string.title_save_post)
                        .setMessage(R.string.confirm_are_you_sure)
                        .setPositiveButton(getString(R.string.option_save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (checkRequirements()){
                                    savePost(postNode, Double.parseDouble(txtPrice.getText().toString().trim()));
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            case R.id.menu_delete_post:
                mAlert.setTitle(R.string.title_delete_post)
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setPositiveButton(getString(R.string.option_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deletePost();
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
            case R.id.menu_publish:
                mAlert.setTitle(R.string.title_publish_post)
                        .setMessage(R.string.info_pre_publishing_notice)
                        .setPositiveButton(R.string.option_publish, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (checkRequirements()){
                                    PUBLISH_STATE = 1;
                                    savePost(postNode, Double.parseDouble(txtPrice.getText().toString().trim()));
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
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
