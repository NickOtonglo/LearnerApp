package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
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

public class NewFileUpload extends AppCompatActivity {

    private LinearLayout imageLayout,audioLayout,videoLayout,docLayout,layoutEditTexts,layoutPrice;
    private RelativeLayout videoPlayerLayout;
    private RadioButton radioImage,radioAudio,radioVideo,radioYes,radioNo,radioDoc;
    private Button btnImage,btnAudio,btnVideo,btnDoc,btnUpload;
    private ImageView imageImage,videoImage,playVideo;
    private ImageButton playAudio;
    private TextView docTextView;
    private EditText fileTitle,fileDescription,filePrice,edtxtCourse,edtxtDepartment,edtxtUnit;
    private VideoView videoView;

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int AUDIO_REQUEST = 3;
    private static final int VIDEO_REQUEST = 4;
    private static final int DOC_REQUEST = 5;
    private int PIC_COUNT;

    private Uri mFileUri = null;
    private MediaPlayer mediaPlayer;

    private DatabaseReference mFiles,mLists,mUsers,mAllPosts,mPublishedItems,mAllPostsSplit;
    private StorageReference sImage,sAudio,sVideo,sStorage;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;
    Calendar calendar;
    private SimpleDateFormat sdf;

    private String faculty="",department="",course="",institution="";
    private List<String> facultyList,departmentList,courseList,institutionList;
    private Spinner spinnerFaculty,spinnerDepartment,spinnerCourse,spinnerInstitution;

    private MediaController mediaController;

    private ProgressBar progressBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_file_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        facultyList = new ArrayList<>();
        departmentList = new ArrayList<>();
        courseList = new ArrayList<>();

        spinnerFaculty = findViewById(R.id.spinner_upload_faculty);
        spinnerDepartment = findViewById(R.id.spinner_upload_department);
        spinnerCourse = findViewById(R.id.spinner_upload_unit);
        spinnerInstitution = findViewById(R.id.spinner_upload_institution);
        edtxtCourse = findViewById(R.id.txt_course);
        edtxtDepartment = findViewById(R.id.txt_department);
        edtxtUnit = findViewById(R.id.txt_unit);

        videoPlayerLayout = findViewById(R.id.layout_upload_file_video_player);
        layoutPrice = findViewById(R.id.layout_upload_price);
        imageLayout = (LinearLayout) findViewById(R.id.upload_layout_2);
        audioLayout = (LinearLayout) findViewById(R.id.upload_layout_3);
        videoLayout = (LinearLayout) findViewById(R.id.upload_layout_4);
        docLayout = (LinearLayout)findViewById(R.id.upload_layout_7);
        layoutEditTexts = (LinearLayout) findViewById(R.id.upload_layout_5);
        filePrice = findViewById(R.id.txt_file_price);
        progressBuffer = findViewById(R.id.buffer_progress_upload_file);

        //radioImage = (RadioButton) findViewById(R.id.radio_upload_image);
        radioAudio = (RadioButton) findViewById(R.id.radio_upload_audio);
        radioVideo = (RadioButton) findViewById(R.id.radio_upload_video);
        radioDoc = (RadioButton)findViewById(R.id.radio_upload_doc);
        radioYes = findViewById(R.id.radio_upload_bid_yes);
        radioNo = findViewById(R.id.radio_upload_bid_no);
        radioYes.setChecked(true);

        fileTitle = (EditText) findViewById(R.id.txt_upload_file_title);
        fileDescription = (EditText) findViewById(R.id.txt_upload_file_description);

        btnAudio = (Button) findViewById(R.id.btn_select_audio);
        btnImage = (Button) findViewById(R.id.btn_select_image);
        btnVideo = (Button) findViewById(R.id.btn_select_video);
        btnDoc = (Button)findViewById(R.id.btn_select_doc);
        btnUpload = (Button) findViewById(R.id.btn_upload_file);

        imageImage = (ImageView)findViewById(R.id.btn_upload_select_image);
        playAudio = (ImageButton)findViewById(R.id.btn_play_audio);
        videoImage = (ImageView)findViewById(R.id.btn_upload_select_video);
        docTextView = (TextView)findViewById(R.id.txt_upload_select_doc);
        videoView = findViewById(R.id.img_upload_file_play_video);
        playVideo = findViewById(R.id.btn_view_upload_time_play);

        sStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mLists = FirebaseDatabase.getInstance().getReference().child("Lists");
        mAllPosts = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        mPublishedItems = FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid());
        mAllPostsSplit = FirebaseDatabase.getInstance().getReference().child("AllPostsSplit").child("Files");

        checkRadioButtons();
        loadSpinners();

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageIntent();
            }
        });
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAudioIntent();
            }
        });
        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAudioState();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    playAudio.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                }
                checkRequirements();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getVideoIntent();
            }
        });

        playVideo.setOnClickListener(new View.OnClickListener() {
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
        mProgress.setMessage("Please wait...");
        mProgress.show();
        mLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                faculty = String.valueOf(dataSnapshot.child("faculty_list").getValue());
                department = String.valueOf(dataSnapshot.child("department_list").getValue());
                course = String.valueOf(dataSnapshot.child("course_list").getValue());
                institution = String.valueOf(dataSnapshot.child("institutions_list").getValue());

                facultyList = Arrays.asList(faculty.split("\\s*,\\s*"));
                departmentList = Arrays.asList(department.split("\\s*,\\s*"));
                courseList = Arrays.asList(course.split("\\s*,\\s*"));
                institutionList = Arrays.asList(institution.split("\\s*,\\s*"));

                HomeActivity homeActivity = new HomeActivity();
                homeActivity.checkMaintenanceStatus(getApplicationContext());

                //Faculty
                final List<String>getFaculties = new ArrayList<>();
                final int facultySize = facultyList.size();
                for (int i=0;i<facultySize;i++){
                    Object object = facultyList.get(i);
                    getFaculties.add(object.toString().trim());
                }

                ArrayAdapter<String> facultyAdapter = new ArrayAdapter<String>(NewFileUpload.this,android.R.layout.simple_spinner_item,getFaculties);
                facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFaculty.setAdapter(facultyAdapter);

                //Department
                final List<String>getDepartments = new ArrayList<>();
                final int departmentSize = departmentList.size();
                for (int i=0;i<departmentSize;i++){
                    Object object = departmentList.get(i);
                    getDepartments.add(object.toString().trim());
                }

                ArrayAdapter<String> departmentAdapter = new ArrayAdapter<String>(NewFileUpload.this,android.R.layout.simple_spinner_item,getDepartments);
                departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDepartment.setAdapter(departmentAdapter);

                //Course
                final List<String>getCourses = new ArrayList<>();
                final int courseSize = courseList.size();
                for (int i=0;i<courseSize;i++){
                    Object object = courseList.get(i);
                    getCourses.add(object.toString().trim());
                }

                ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(NewFileUpload.this,android.R.layout.simple_spinner_item,getCourses);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCourse.setAdapter(courseAdapter);

                //Institution
                final List<String>getInstitution = new ArrayList<>();
                final int institutionSize = institutionList.size();
                for (int i=0;i<institutionSize;i++){
                    Object object = institutionList.get(i);
                    getInstitution.add(object.toString().trim());
                }

                ArrayAdapter<String> institutionAdapter = new ArrayAdapter<String>(NewFileUpload.this,android.R.layout.simple_spinner_item,getInstitution);
                institutionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerInstitution.setAdapter(institutionAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProgress.dismiss();
    }

    private void checkRequirements() {
        if (radioYes.isChecked() && Double.parseDouble(filePrice.getText().toString().trim())==0){
            Snackbar.make(findViewById(android.R.id.content),"You cannot allow bidding on a free item",Snackbar.LENGTH_LONG).show();
        } else {
            if (spinnerInstitution.getSelectedItem().toString().equals("--University Institutions--")
                    || spinnerInstitution.getSelectedItem().toString().equals("--Tertiary Institutions--")
                    || spinnerInstitution.getSelectedItem().toString().equals("--Artisan Institutions--")){
                Snackbar.make(findViewById(android.R.id.content),"Select an institution",Snackbar.LENGTH_LONG).show();
            } else {
                if (radioAudio.isChecked()){
                    String title = fileTitle.getText().toString().trim();
                    String description = fileDescription.getText().toString().trim();
                    if (title.isEmpty() || description.isEmpty()){
                        Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
                    }
                    if (mFileUri==null){
                        Toast.makeText(this, "No audio file selected", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        filePrice.setError("Required");
                    }
                    if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        startPosting();
                    }
//        } else if (radioImage.isChecked()){
//            String title = fileTitle.getText().toString().trim();
//            String description = fileDescription.getText().toString().trim();
//            if (title.isEmpty() || description.isEmpty()){
//                Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
//            }
//            if (mFileUri==null){
//                Toast.makeText(this, "No image file selected", Toast.LENGTH_SHORT).show();
//            }
//            if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
//                filePrice.setError("Required");
//            }
//            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
//                startPosting();
//            }
                } else if (radioVideo.isChecked()){
                    String title = fileTitle.getText().toString().trim();
                    String description = fileDescription.getText().toString().trim();
                    if (title.isEmpty() || description.isEmpty()){
                        Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
                    }
                    if (mFileUri==null){
                        Toast.makeText(this, "No video file selected", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        filePrice.setError("Required");
                    }
                    if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        startPosting();
                    }
                }
                else if (radioDoc.isChecked()){
                    String title = fileTitle.getText().toString().trim();
                    String description = fileDescription.getText().toString().trim();
                    if (title.isEmpty() || description.isEmpty()){
                        Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
                    }
                    if (mFileUri==null){
                        Toast.makeText(this, "No document file selected", Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        filePrice.setError("Required");
                    }
                    if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                        startPosting();
                    }
                }
            }
        }
    }

    private void startPosting() {
        mProgress.setMessage("Creating post...");
        mProgress.setCanceledOnTouchOutside(false);
        final String title = fileTitle.getText().toString().trim();
        final String description = fileDescription.getText().toString().trim();
        String price = filePrice.getText().toString().trim();
        double unitPrice = Double.parseDouble(price);
        final String time = sdf.format(Calendar.getInstance().getTime());

        final DatabaseReference mPostNode = mAllPosts.push();
        final String keyRef = mPostNode.getKey();
        final StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("files",getFileName(mFileUri).replace(" ","_"))
                .build();

//        if (radioImage.isChecked()){
//            mProgress.show();
//            mFiles.child("image");
//            StorageReference filepath = sStorage.child("image").child(mFileUri.getLastPathSegment());
//            final double finalUnitPrice = unitPrice;
//            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
////                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl(); //changed this
//                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
//                    final DatabaseReference mUpload = mFiles.child(keyRef);
//                    mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            mPostNode.child("ItemId").setValue(keyRef);
//                            mPostNode.child("Category").setValue("Files");
//                            mPostNode.child("Title").setValue(title.toUpperCase());
//                            mPostNode.child("FileType").setValue("image");
//
//                            mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
//                            mPublishedItems.child(keyRef).child("Category").setValue("Files");
//                            mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
//                            mPublishedItems.child(keyRef).child("FileType").setValue("image");
//                            mPublishedItems.child(keyRef).child("Published").setValue("false");

//                            mAllPostsSplit.child(keyRef).child("ItemId").setValue(keyRef);
//                            mAllPostsSplit.child(keyRef).child("Category").setValue("Files");
//                            mAllPostsSplit.child(keyRef).child("Title").setValue(title.toUpperCase());
//                            mAllPostsSplit.child(keyRef).child("FileType").setValue("audio");
//                            mAllPostsSplit.child(keyRef).child("Institution").setValue(spinnerTag.getSelectedItem().toString());
//
//                            mUpload.child("title").setValue(title.toUpperCase());
//                            mUpload.child("description").setValue(description);
//                            mUpload.child("timestamp").setValue(time);
//                            mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
////                            Log.d("FilePath",String.valueOf(downloadUrl));
//                            mUpload.child("file_type").setValue("image");
//                            mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
//                            mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
//                            mUpload.child("school").setValue(spinnerFaculty.getSelectedItem().toString());
//                            mUpload.child("department").setValue(spinnerDepartment.getSelectedItem().toString());
//                            mUpload.child("course").setValue(spinnerCourse.getSelectedItem().toString());
//                            mUpload.child("price").setValue(finalUnitPrice);
//                            mUpload.child("owners").setValue("");
//                            if (radioYes.isChecked()){
//                                mUpload.child("biddable").setValue("yes");
//                            } else if (radioNo.isChecked()){
//                                mUpload.child("biddable").setValue("no");
//                            }
//                            mProgress.dismiss();
//                            Toast.makeText(NewFileUpload.this, "File uploaded", Toast.LENGTH_LONG).show();
//                            finish();
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            Toast.makeText(NewFileUpload.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            });
//        }
        if (radioAudio.isChecked()){
            mProgress.show();
            mFiles.child("audio");
            StorageReference filepath = sStorage.child("audio").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.child(keyRef);
//                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//                            mPostNode.child("ItemId").setValue(keyRef);
//                            mPostNode.child("Category").setValue("Files");
//                            mPostNode.child("Title").setValue(title.toUpperCase());
//                            mPostNode.child("FileType").setValue("audio");

                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    mPublishedItems.child(keyRef).child("Category").setValue("Files");
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("audio");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

//                            mAllPostsSplit.child(keyRef).child("ItemId").setValue(keyRef);
//                            mAllPostsSplit.child(keyRef).child("Category").setValue("Files");
//                            mAllPostsSplit.child(keyRef).child("Title").setValue(title.toUpperCase());
//                            mAllPostsSplit.child(keyRef).child("FileType").setValue("audio");
//                            mAllPostsSplit.child(keyRef).child("Institution").setValue(spinnerInstitution.getSelectedItem().toString());

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("thumbnail").setValue("");
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
//                            Log.d("FilePath",String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue("audio");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                                    mUpload.child("school").setValue(edtxtCourse.getText().toString());
                                    mUpload.child("department").setValue(edtxtDepartment.getText().toString());
                                    mUpload.child("course").setValue(edtxtUnit.getText().toString());
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
                                    mProgress.dismiss();
                                    Toast.makeText(NewFileUpload.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.alert_publish)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    intent.putExtra("postNode","Files")
                                                            .putExtra("postKey",keyRef);
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
                                    Toast.makeText(NewFileUpload.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                }
            });
        }
        if (radioVideo.isChecked()){
            mProgress.show();
            mFiles.child("video");
            StorageReference filepath = sStorage.child("video").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.child(keyRef);
//                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//                            mPostNode.child("ItemId").setValue(keyRef);
//                            mPostNode.child("Category").setValue("Files");
//                            mPostNode.child("Title").setValue(title.toUpperCase());
//                            mPostNode.child("FileType").setValue("video");

                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    mPublishedItems.child(keyRef).child("Category").setValue("Files");
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("video");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

//                            mAllPostsSplit.child(keyRef).child("ItemId").setValue(keyRef);
//                            mAllPostsSplit.child(keyRef).child("Category").setValue("Files");
//                            mAllPostsSplit.child(keyRef).child("Title").setValue(title.toUpperCase());
//                            mAllPostsSplit.child(keyRef).child("FileType").setValue("audio");
//                            mAllPostsSplit.child(keyRef).child("Institution").setValue(spinnerInstitution.getSelectedItem().toString());

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("thumbnail").setValue("");
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
//                            Log.d("FilePath",String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue("video");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                                    mUpload.child("school").setValue(edtxtCourse.getText().toString());
                                    mUpload.child("department").setValue(edtxtDepartment.getText().toString());
                                    mUpload.child("course").setValue(edtxtUnit.getText().toString());
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
                                    mProgress.dismiss();
                                    Toast.makeText(NewFileUpload.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.alert_publish)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    intent.putExtra("postNode","Files")
                                                            .putExtra("postKey",keyRef);
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
                                    Toast.makeText(NewFileUpload.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            });
        }
        if (radioDoc.isChecked()){
            mProgress.show();
            mFiles.child("doc");
            StorageReference filepath = sStorage.child("doc").child(keyRef);
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri,metadata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.child(keyRef);
//                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
//                            mPostNode.child("ItemId").setValue(keyRef);
//                            mPostNode.child("Category").setValue("Files");
//                            mPostNode.child("Title").setValue(title.toUpperCase());
//                            mPostNode.child("FileType").setValue("doc");

                                    mPublishedItems.child(keyRef).child("ItemId").setValue(keyRef);
                                    mPublishedItems.child(keyRef).child("Category").setValue("Files");
                                    mPublishedItems.child(keyRef).child("Title").setValue(title.toUpperCase());
                                    mPublishedItems.child(keyRef).child("FileType").setValue("image");
                                    mPublishedItems.child(keyRef).child("Published").setValue("false");

//                            mAllPostsSplit.child(keyRef).child("ItemId").setValue(keyRef);
//                            mAllPostsSplit.child(keyRef).child("Category").setValue("Files");
//                            mAllPostsSplit.child(keyRef).child("Title").setValue(title.toUpperCase());
//                            mAllPostsSplit.child(keyRef).child("FileType").setValue("audio");
//                            mAllPostsSplit.child(keyRef).child("Institution").setValue(spinnerInstitution.getSelectedItem().toString());

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("thumbnail").setValue("");
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
//                            Log.d("FilePath",String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue("doc");
                                    mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                                    mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                                    mUpload.child("school").setValue(edtxtCourse.getText().toString());
                                    mUpload.child("department").setValue(edtxtDepartment.getText().toString());
                                    mUpload.child("course").setValue(edtxtUnit.getText().toString());
                                    mUpload.child("price").setValue(finalUnitPrice);
                                    if (radioYes.isChecked()){
                                        mUpload.child("biddable").setValue("yes");
                                    } else if (radioNo.isChecked()){
                                        mUpload.child("biddable").setValue("no");
                                    }
                                    mProgress.dismiss();
                                    Toast.makeText(NewFileUpload.this, R.string.info_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.alert_publish)
                                            .setPositiveButton(R.string.option_alert_review_and_publish, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
                                                    intent.putExtra("postNode","Files")
                                                            .putExtra("postKey",keyRef);
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
                                    Toast.makeText(NewFileUpload.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                }
            });
        }
    }

    private void getVideoIntent() {
        Intent intent_upload = new Intent();
        intent_upload.setType("video/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,VIDEO_REQUEST);
    }

    private void checkVideoState() {

    }

    public void playVideo(){
        playVideo.setVisibility(View.GONE);
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

    public void checkAudioState(){

        if (mFileUri==null){
            Toast.makeText(NewFileUpload.this, "No audio file selected", Toast.LENGTH_SHORT).show();
        } else {
            if (mediaPlayer.isPlaying()){
                playAudio.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                mediaPlayer.stop();
            } else {
                playAudio.setImageResource(R.mipmap.baseline_stop_white_18dp);
                //Mediafileinfo item = new Mediafileinfo();
                try {
                    // mediaPlayer.setDataSource(String.valueOf(myUri));
                    mediaPlayer.setDataSource(NewFileUpload.this,mFileUri);
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
//        radioImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (radioImage.isChecked()){
//                    radioAudio.setChecked(false);
//                    radioVideo.setChecked(false);
//                    radioDoc.setChecked(false);
//                    imageLayout.setVisibility(View.VISIBLE);
//                    audioLayout.setVisibility(View.GONE);
//                    videoLayout.setVisibility(View.GONE);
//                    docLayout.setVisibility(View.GONE);
//                    layoutEditTexts.setVisibility(View.VISIBLE);
//                    btnUpload.setVisibility(View.VISIBLE);
//                    layoutPrice.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        radioAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioAudio.isChecked()){
                    //radioImage.setChecked(false);
                    radioVideo.setChecked(false);
                    radioDoc.setChecked(false);
                    audioLayout.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.GONE);
                    videoLayout.setVisibility(View.GONE);
                    docLayout.setVisibility(View.GONE);
                    layoutEditTexts.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    layoutPrice.setVisibility(View.VISIBLE);
                }
            }
        });

        radioVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioVideo.isChecked()){
                    //radioImage.setChecked(false);
                    radioAudio.setChecked(false);
                    radioDoc.setChecked(false);
                    videoLayout.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.GONE);
                    audioLayout.setVisibility(View.GONE);
                    docLayout.setVisibility(View.GONE);
                    layoutEditTexts.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    layoutPrice.setVisibility(View.VISIBLE);
                }
            }
        });

        radioDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioDoc.isChecked()){
                    //radioImage.setChecked(false);
                    radioAudio.setChecked(false);
                    radioVideo.setChecked(false);
                    docLayout.setVisibility(View.VISIBLE);
                    videoLayout.setVisibility(View.GONE);
                    imageLayout.setVisibility(View.GONE);
                    audioLayout.setVisibility(View.GONE);
                    layoutEditTexts.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    layoutPrice.setVisibility(View.VISIBLE);
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

    public void getImageIntent(){
        final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Image options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Take Photo")) {
                    PIC_COUNT = 1;
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
                } else if (items[item].equals("Choose from Gallery")) {
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
        startActivityForResult(intent_upload,DOC_REQUEST);
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
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==VIDEO_REQUEST){
            try {
                mFileUri = data.getData();

                Uri uri = mFileUri.buildUpon().appendQueryParameter("t", "0").build();
                Picasso.with(NewFileUpload.this).load(uri).into(videoImage);
                videoPlayerLayout.setVisibility(View.VISIBLE);
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
                Snackbar.make(findViewById(android.R.id.content),String.valueOf(error),Snackbar.LENGTH_LONG).show();
            }
        }
        if (requestCode==DOC_REQUEST){
            try {
                mFileUri = data.getData();
                docTextView.setText(getFileName(mFileUri));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
        overridePendingTransition(R.anim.static_animation,R.anim.slide_in_from_top);
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
}