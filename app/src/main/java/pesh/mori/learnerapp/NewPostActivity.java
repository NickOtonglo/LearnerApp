package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class NewPostActivity extends AppCompatActivity {
    private LinearLayout audioLayout,videoLayout, docLayout,layoutEditTexts,layoutPrice,layoutCoursework,layoutNonCoursework;
    private RelativeLayout videoPlayerLayout,audioPlayerLayout;
    private RadioButton radioCoursework,radioNonCoursework,radioYes,radioNo;
    private Button btnImage,btnAudio,btnVideo, btnDoc,btnUpload;
    private ImageView imageImage,videoImage, btnPlayVideo;
    private ImageView btnPlayAudio;
    private TextView docTextView;
    private EditText fileTitle,fileDescription,filePrice;
    private EditText txtCourse, txtDept,txtUnit;
    private VideoView videoView,audioView;

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int AUDIO_REQUEST = 3;
    private static final int VIDEO_REQUEST = 4;
    private static final int DOC_REQUEST = 5;
    private int PIC_COUNT;

    private Uri mFileUri = null;
    private MediaPlayer mediaPlayer;

    private DatabaseReference mPost,mLists,mUsers,mAllPosts,mPublishedItems,mAllPostsSplit;
    private StorageReference sImage,sAudio,sVideo,sStorage;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;
    private CustomDialogActivity mProgressCustom;
    Calendar calendar;
    private SimpleDateFormat sdf;

    private String items9 ="",items8="";
    private List<String> list9,list8;
    private Spinner spinnerList9,spinnerList8;

    private MediaController mediaController;

    private ProgressBar progressBuffer,progressBufferAudio;

    private String uploadType;

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
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        uploadType = getIntent().getExtras().getString("upload_type");

        mProgress = new ProgressDialog(this);
        mProgressCustom = new CustomDialogActivity(NewPostActivity.this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mAuth = FirebaseAuth.getInstance();

        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        list9 = new ArrayList<>();
        list8 = new ArrayList<>();

        spinnerList9 = findViewById(R.id.spinner_file_types);
        spinnerList8 = findViewById(R.id.spinner_category_8_list);

        layoutCoursework = findViewById(R.id.layout_cat_8);
        layoutNonCoursework = findViewById(R.id.layout_cat_9);
        videoPlayerLayout = findViewById(R.id.layout_upload_video_player);
        audioPlayerLayout = findViewById(R.id.layout_upload_audio_player);
        layoutPrice = findViewById(R.id.layout_price);
        audioLayout = (LinearLayout) findViewById(R.id.layout_3);
        videoLayout = (LinearLayout) findViewById(R.id.layout_4);
        docLayout = (LinearLayout)findViewById(R.id.layout_7);
        layoutEditTexts = (LinearLayout) findViewById(R.id.layout_5);
        filePrice = findViewById(R.id.txt_file_price);
        progressBuffer = findViewById(R.id.buffer_progress_upload);
        progressBufferAudio = findViewById(R.id.buffer_progress_upload_audio);

        txtCourse = findViewById(R.id.txt_course);
        txtDept = findViewById(R.id.txt_department);
        txtUnit = findViewById(R.id.txt_unit);

        //radioImage = (RadioButton) findViewById(R.id.radio_diy_image);
        radioCoursework = (RadioButton) findViewById(R.id.radio_coursework);
        radioNonCoursework = (RadioButton) findViewById(R.id.radio_non_coursework);
        radioYes = findViewById(R.id.radio_bid_yes);
        radioNo = findViewById(R.id.radio_bid_no);
        radioYes.setChecked(true);

        fileTitle = (EditText) findViewById(R.id.txt_file_title);
        fileDescription = (EditText) findViewById(R.id.txt_description);

        btnAudio = (Button) findViewById(R.id.btn_select_audio);
        btnVideo = (Button) findViewById(R.id.btn_select_video);
        btnDoc = (Button)findViewById(R.id.btn_select_doc);
        btnUpload = (Button) findViewById(R.id.btn_upload_file);

        btnPlayAudio = (ImageView)findViewById(R.id.btn_play_audio);
        videoImage = (ImageView)findViewById(R.id.img_video_placeholder);
        docTextView = (TextView)findViewById(R.id.txt_select_doc);
        videoView = findViewById(R.id.img_upload_play_video);
        audioView = findViewById(R.id.img_upload_play_audio);
        btnPlayVideo = findViewById(R.id.btn_view_upload_time_play);

        mLists = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists));
        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
        mPublishedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid());

        checkRadioButtons();
        loadSpinners();
        setPreVisibility();
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAudioIntent();
            }
        });
        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkRequirements();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVideoIntent();
            }
        });

        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });

        btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDocIntent();
            }
        });

    }

    private void loadSpinners(){
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.show();
        mLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Non-coursework
                items9 = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_9)).getValue());
                list9 = Arrays.asList(items9.split("\\s*,\\s*"));

                final List<String> getItems9 = new ArrayList<>();
                final int list9Size = list9.size();
                for (int i=0;i<list9Size;i++){
                    Object object = list9.get(i);
                    getItems9.add(object.toString().trim());
                }

                ArrayAdapter<String> list9Adapter = new ArrayAdapter<String>(NewPostActivity.this,android.R.layout.simple_spinner_item,getItems9);
                list9Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerList9.setAdapter(list9Adapter);

                //Coursework
                items8 = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_8)).getValue());
                list8 = Arrays.asList(items8.split("\\s*,\\s*"));

                final List<String> getItems8 = new ArrayList<>();
                final int list8Size = list8.size();
                for (int i=0;i<list8Size;i++){
                    Object object = list8.get(i);
                    getItems8.add(object.toString().trim());
                }

                ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(NewPostActivity.this,android.R.layout.simple_spinner_item,getItems8);
                tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerList8.setAdapter(tagsAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProgress.dismiss();
    }

    private void checkRequirements() {
        String course = txtCourse.getText().toString().trim();
        String department = txtDept.getText().toString().trim();
        String unit = txtUnit.getText().toString().trim();
        String institution = spinnerList8.getSelectedItem().toString();
        if (!radioCoursework.isChecked() && !radioNonCoursework.isChecked()){
            Snackbar.make(findViewById(android.R.id.content), R.string.info_you_must_select_a_post_category, Snackbar.LENGTH_LONG).show();
        } else if(radioCoursework.isChecked() && (institution.equals("--University Institutions--")
                || institution.equals("--Tertiary Institutions--")
                || institution.equals("--Artisan Institutions--"))){
            Snackbar.make(findViewById(android.R.id.content), R.string.you_must_select_a_valid_institution,Snackbar.LENGTH_LONG).show();
        } else if (radioCoursework.isChecked() && (course.isEmpty() || department.isEmpty() || unit.isEmpty())){
            Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
        } else if (radioYes.isChecked() && Double.parseDouble(filePrice.getText().toString().trim())==0){
            Snackbar.make(findViewById(android.R.id.content), R.string.info_you_cannot_allow_bidding_on_a_free_item, Snackbar.LENGTH_LONG).show();
        } else {
            if (uploadType.equals("audio")){
                String title = fileTitle.getText().toString().trim();
                String description = fileDescription.getText().toString().trim();
                if (title.isEmpty() || description.isEmpty()){
                    Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
                }
                if (mFileUri==null){
                    Toast.makeText(this, R.string.info_no_audio_file_selected, Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    filePrice.setError(getString(R.string.info_required));
                }
                if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    startPosting();
                }
            } else if (uploadType.equals("video")){
                String title = fileTitle.getText().toString().trim();
                String description = fileDescription.getText().toString().trim();
                if (title.isEmpty() || description.isEmpty()){
                    Snackbar.make(findViewById(android.R.id.content),getString(R.string.info_one_or_more_required_fields_is_empty), Snackbar.LENGTH_LONG).show();
                }
                if (mFileUri==null){
                    Toast.makeText(this, R.string.info_no_video_file_selected, Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    filePrice.setError(getString(R.string.info_required));
                }
                if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    startPosting();
                }
            } else if (uploadType.equals("PDF")){
                String title = fileTitle.getText().toString().trim();
                String description = fileDescription.getText().toString().trim();
                if (title.isEmpty() || description.isEmpty()){
                    Snackbar.make(findViewById(android.R.id.content),getString(R.string.info_one_or_more_required_fields_is_empty), Snackbar.LENGTH_LONG).show();
                }
                if (mFileUri==null){
                    Toast.makeText(this, R.string.info_no_supported_document_file_selected, Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    filePrice.setError(getString(R.string.info_required));
                }
                if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                    startPosting();
                }
            }
        }
    }

    private void startPosting() {
//        mProgress.setMessage(getString(R.string.info_uploading_file));
//        mProgress.setCanceledOnTouchOutside(false);
        final String title = fileTitle.getText().toString().trim();
        final String description = fileDescription.getText().toString().trim();
        String price = filePrice.getText().toString().trim();
        double unitPrice = Double.parseDouble(price);
        final String time = sdf.format(Calendar.getInstance().getTime());

        final DatabaseReference mPostNode = mAllPosts.push();
        final String keyRef = mPostNode.getKey();
        StorageMetadata metadata = null;

        if (radioCoursework.isChecked()){
            mPost = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
            sStorage = FirebaseStorage.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1))
                    .child(mAuth.getCurrentUser().getUid());
            mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split))
                    .child(getString(R.string.firebase_ref_posts_type_1));
            metadata = new StorageMetadata.Builder()
                    .setCustomMetadata(getString(R.string.firebase_ref_posts_type_1),getFileName(mFileUri).replace(" ","_"))
                    .build();
        } else if (radioNonCoursework.isChecked()){
            mPost = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
            sStorage = FirebaseStorage.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2))
                    .child(mAuth.getCurrentUser().getUid());
            mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all_split))
                    .child(getString(R.string.firebase_ref_posts_type_2));
            metadata = new StorageMetadata.Builder()
                    .setCustomMetadata(getString(R.string.firebase_ref_posts_type_2),getFileName(mFileUri).replace(" ","_"))
                    .build();
        }

        if (uploadType.equals("audio")){
//            mProgress.show();
            mProgressCustom.show(false);
            mPost.child("audio");
            StorageReference filepath = sStorage.child("audio").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mPost.child(keyRef);
//                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    if (radioCoursework.isChecked()){
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_1));
                                    } else {
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_2));
                                    }
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("audio");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                                    Task<Void> thumbnail = mUpload.child("thumbnail").setValue("");
                                    mUpload.child("file_type").setValue("audio");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    if (radioCoursework.isChecked()){
                                        mUpload.child("institution").setValue(spinnerList8.getSelectedItem().toString());
                                        mUpload.child("unit").setValue(txtUnit.getText().toString());
                                        mUpload.child("department").setValue(txtDept.getText().toString());
                                        mUpload.child("course").setValue(txtCourse.getText().toString());
                                    } else if (radioNonCoursework.isChecked()){
                                        mUpload.child("tag").setValue(spinnerList9.getSelectedItem().toString());
                                    }
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
                                    mProgressCustom.dismiss();
//                                    mProgress.dismiss();
                                    Toast.makeText(NewPostActivity.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.confirm_publish_post)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    if (radioCoursework.isChecked()){
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_1));
                                                    } else {
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_2));
                                                    }
                                                    intent.putExtra("postKey",keyRef);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNeutralButton(R.string.option_alert_later, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            })
                                            .show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(NewPostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    mProgressCustom.setDisplayText(getString(R.string.info_please_wait)+" "+getString(R.string.info_uploaded)+" "+ Math.round(progress)+"%");
                }
            });
        }
        if (uploadType.equals("video")){
//            mProgress.show();
            mProgressCustom.show(false);
            mPost.child("video");
            StorageReference filepath = sStorage.child("video").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mPost.child(keyRef);

                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    if (radioCoursework.isChecked()){
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_1));
                                    } else {
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_2));
                                    }
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("video");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue("video");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    if (radioCoursework.isChecked()){
                                        mUpload.child("institution").setValue(spinnerList8.getSelectedItem().toString());
                                        mUpload.child("unit").setValue(txtUnit.getText().toString());
                                        mUpload.child("department").setValue(txtDept.getText().toString());
                                        mUpload.child("course").setValue(txtCourse.getText().toString());
                                    } else if (radioNonCoursework.isChecked()){
                                        mUpload.child("tag").setValue(spinnerList9.getSelectedItem().toString());
                                    }
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
//                                    mProgress.dismiss();
                                    mProgressCustom.dismiss();
                                    Toast.makeText(NewPostActivity.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.confirm_publish_post)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    if (radioCoursework.isChecked()){
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_1));
                                                    } else {
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_2));
                                                    }
                                                    intent.putExtra("postKey",keyRef);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNeutralButton(R.string.option_alert_later, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            })
                                            .show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(NewPostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    mProgressCustom.setDisplayText(getString(R.string.info_please_wait)+" "+getString(R.string.info_uploaded)+" "+ Math.round(progress)+"%");
                }
            });;
        }
        if (uploadType.equals("PDF")){
//            mProgress.show();
            mProgressCustom.show(false);
            mPost.child("doc");
            StorageReference filepath = sStorage.child("doc").child(keyRef);
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri,metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mPost.child(keyRef);

                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    if (radioCoursework.isChecked()){
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_1));
                                    } else {
                                        mPublishedItems.child(keyRef).child("Category").setValue(getString(R.string.firebase_ref_posts_type_2));
                                    }
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("doc");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                                    Log.d("FilePath", String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue("doc");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    if (radioCoursework.isChecked()){
                                        mUpload.child("institution").setValue(spinnerList8.getSelectedItem().toString());
                                        mUpload.child("unit").setValue(txtUnit.getText().toString());
                                        mUpload.child("department").setValue(txtDept.getText().toString());
                                        mUpload.child("course").setValue(txtCourse.getText().toString());
                                    } else if (radioNonCoursework.isChecked()){
                                        mUpload.child("tag").setValue(spinnerList9.getSelectedItem().toString());
                                    }
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
//                                    mProgress.dismiss();
                                    mProgressCustom.dismiss();
                                    Toast.makeText(NewPostActivity.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.confirm_publish_post)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    if (radioCoursework.isChecked()){
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_1));
                                                    } else {
                                                        intent.putExtra("postNode",getString(R.string.firebase_ref_posts_type_2));
                                                    }
                                                    intent.putExtra("postKey",keyRef);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNeutralButton(R.string.option_alert_later, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    finish();
                                                }
                                            })
                                            .show();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(NewPostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    mProgressCustom.setDisplayText(getString(R.string.info_please_wait)+" "+getString(R.string.info_uploaded)+" "+ Math.round(progress)+"%");
                }
            });;
        }
    }

    private void getVideoIntent() {
        Intent intent_upload = new Intent();
        intent_upload.setType("video/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,VIDEO_REQUEST);
    }

    public void playVideo(){
        btnPlayVideo.setVisibility(View.GONE);
        progressBuffer.setVisibility(View.VISIBLE);

        videoView.setVideoURI(mFileUri);
        videoView.requestFocus();
        videoView.start();
        progressBuffer.setVisibility(View.GONE);

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
        mediaController.setAnchorView(videoView);
    }

    public void playAudio(){
        // btnPlayAudio.setImageResource(R.drawable.baseline_audiotrack_24);
        btnPlayAudio.setVisibility(View.GONE);
        progressBufferAudio.setVisibility(View.VISIBLE);

        audioView.setVideoURI(mFileUri);
        audioView.requestFocus();
        audioView.start();
        progressBufferAudio.setVisibility(View.GONE);

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


    public void checkAudioState(){

        if (mFileUri==null){
            Toast.makeText(NewPostActivity.this, getString(R.string.info_no_audio_file_selected), Toast.LENGTH_SHORT).show();
        } else {
            if (mediaPlayer.isPlaying()){
//                playAudio.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                mediaPlayer.stop();
            } else {
//                playAudio.setImageResource(R.mipmap.baseline_stop_white_18dp);
                //Mediafileinfo item = new Mediafileinfo();
                try {
                    // mediaPlayer.setDataSource(String.valueOf(myUri));
                    mediaPlayer.setDataSource(NewPostActivity.this,mFileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e){
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        }
    }

    public void checkRadioButtons() {
        radioCoursework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioCoursework.isChecked()){
                    radioNonCoursework.setChecked(false);
                    layoutCoursework.setVisibility(View.VISIBLE);
                    layoutNonCoursework.setVisibility(View.GONE);
                }
            }
        });

        radioNonCoursework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioNonCoursework.isChecked()){
                    radioCoursework.setChecked(false);
                    layoutCoursework.setVisibility(View.GONE);
                    layoutNonCoursework.setVisibility(View.VISIBLE);
                }
            }
        });

        radioYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioYes.isChecked()){
                    radioNo.setChecked(false);
                }
            }
        });
        radioNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioNo.isChecked()){
                    radioYes.setChecked(false);
                }
            }
        });
    }

    public void setPreVisibility(){
        if (uploadType.equals("audio")){
            audioLayout.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.GONE);
            docLayout.setVisibility(View.GONE);
            layoutEditTexts.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            layoutPrice.setVisibility(View.VISIBLE);
            btnAudio.setVisibility(View.VISIBLE);
        }
        if (uploadType.equals("video")){
            audioLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            docLayout.setVisibility(View.GONE);
            layoutEditTexts.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            layoutPrice.setVisibility(View.VISIBLE);
            btnVideo.setVisibility(View.VISIBLE);
        }
        if (uploadType.equals("PDF")){
            audioLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.GONE);
            docLayout.setVisibility(View.VISIBLE);
            layoutEditTexts.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            layoutPrice.setVisibility(View.VISIBLE);
//            btnDoc.setVisibility(View.VISIBLE);
        }
    }

    public void getImageIntent(){
        final CharSequence[] items = {getString(R.string.option_take_photo), getString(R.string.option_choose_from_gallery)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle(R.string.title_image_options);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getString(R.string.option_take_photo))) {
                    PIC_COUNT = 1;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else if (items[item].equals(getString(R.string.option_choose_from_gallery))) {
                    PIC_COUNT = 1;
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,GALLERY_REQUEST);
                }
            }
        });
        builder.show();
    }

    public void getAudioIntent(){
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,AUDIO_REQUEST);
    }

    public void getDocIntent(){
        Intent intent_upload = new Intent();
        intent_upload.setType("application/pdf");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, DOC_REQUEST);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST){
            try {
                mFileUri = data.getData();
                CropImage.activity(mFileUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(4,3)
                        .start(this);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==CAMERA_REQUEST){
            try {
                mFileUri = data.getData();
                CropImage.activity(mFileUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(4,3)
                        .start(this);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==AUDIO_REQUEST){
            try {
                mFileUri = data.getData();
                audioPlayerLayout.setVisibility(View.VISIBLE);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==VIDEO_REQUEST){
            try {
                mFileUri = data.getData();
                Uri uri = mFileUri.buildUpon().appendQueryParameter("t", "0").build();
                Picasso.with(NewPostActivity.this).load(uri).into(videoImage);
                videoPlayerLayout.setVisibility(View.VISIBLE);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode== DOC_REQUEST){
            try {
                mFileUri = data.getData();
                docTextView.setText(getFileName(mFileUri));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mFileUri = result.getUri();
                imageImage.setImageURI(mFileUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(android.R.id.content), String.valueOf(error), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

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

    private void setSpinnerDefaults(String type){
        if (type.equals(getString(R.string.firebase_ref_posts_type_1))){
            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).child(getString(R.string.firebase_ref_lists_cat_8))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        } else if (type.equals(getString(R.string.firebase_ref_posts_type_2))){
            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).child(getString(R.string.firebase_ref_lists_cat_9))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}
