package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

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

public class NewFileUploadFull extends AppCompatActivity {

    private LinearLayout imageLayout,audioLayout,videoLayout,layoutEditTexts,layoutPrice;
    private RadioButton radioImage,radioAudio,radioVideo;
    private Button btnImage,btnAudio,btnVideo,btnUpload;
    private ImageView imageImage,videoImage;
    private ImageButton playAudio;
    private EditText fileTitle,fileDescription,filePrice;

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int AUDIO_REQUEST = 3;
    private static final int VIDEO_REQUEST = 4;
    private int PIC_COUNT;

    private Uri mFileUri = null;
    private MediaPlayer mediaPlayer;

    private DatabaseReference mFiles,mLists,mUsers;
    private StorageReference sImage,sAudio,sVideo,sStorage;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    Calendar calendar;
    private SimpleDateFormat sdf;

    private String faculty="",department="",course="",institution="";
    private List<String> facultyList,departmentList,courseList,institutionList;
    private Spinner spinnerFaculty,spinnerDepartment,spinnerCourse,spinnerInstitution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_file_upload_full);
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        mediaPlayer = new MediaPlayer();

        facultyList = new ArrayList<>();
        departmentList = new ArrayList<>();
        courseList = new ArrayList<>();

        spinnerFaculty = findViewById(R.id.spinner_upload_full_faculty);
        spinnerDepartment = findViewById(R.id.spinner_upload_full_department);
        spinnerCourse = findViewById(R.id.spinner_upload_full_unit);
        spinnerInstitution = findViewById(R.id.spinner_upload_full_institution);

        layoutPrice = findViewById(R.id.layout_upload_full_price);
        imageLayout = (LinearLayout) findViewById(R.id.upload_full_layout_2);
        audioLayout = (LinearLayout) findViewById(R.id.upload_full_layout_3);
        videoLayout = (LinearLayout) findViewById(R.id.upload_full_layout_4);
        layoutEditTexts = (LinearLayout) findViewById(R.id.upload_full_layout_5);
        filePrice = findViewById(R.id.txt_file_price_full);

        radioImage = (RadioButton) findViewById(R.id.radio_upload_full_image);
        radioAudio = (RadioButton) findViewById(R.id.radio_upload_full_audio);
        radioVideo = (RadioButton) findViewById(R.id.radio_upload_full_video);

        fileTitle = (EditText) findViewById(R.id.txt_upload_full_file_title);
        fileDescription = (EditText) findViewById(R.id.txt_upload_full_file_description);

        btnAudio = (Button) findViewById(R.id.btn_full_select_audio);
        btnImage = (Button) findViewById(R.id.btn_full_select_image);
        btnVideo = (Button) findViewById(R.id.btn_full_select_video);
        btnUpload = (Button) findViewById(R.id.btn_upload_full_file);

        imageImage = (ImageView)findViewById(R.id.btn_upload_full_select_image);
        playAudio = (ImageButton)findViewById(R.id.btn_full_play_audio);
        videoImage = (ImageView)findViewById(R.id.btn_upload_full_select_video);

        sStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mLists = FirebaseDatabase.getInstance().getReference().child("Lists");

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

                //Faculty
                final List<String> getFaculties = new ArrayList<>();
                final int facultySize = facultyList.size();
                for (int i=0;i<facultySize;i++){
                    Object object = facultyList.get(i);
                    getFaculties.add(object.toString().trim());
                }

                ArrayAdapter<String> facultyAdapter = new ArrayAdapter<String>(NewFileUploadFull.this,android.R.layout.simple_spinner_item,getFaculties);
                facultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFaculty.setAdapter(facultyAdapter);

                //Department
                final List<String>getDepartments = new ArrayList<>();
                final int departmentSize = departmentList.size();
                for (int i=0;i<departmentSize;i++){
                    Object object = departmentList.get(i);
                    getDepartments.add(object.toString().trim());
                }

                ArrayAdapter<String> departmentAdapter = new ArrayAdapter<String>(NewFileUploadFull.this,android.R.layout.simple_spinner_item,getDepartments);
                departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDepartment.setAdapter(departmentAdapter);

                //Course
                final List<String>getCourses = new ArrayList<>();
                final int courseSize = courseList.size();
                for (int i=0;i<courseSize;i++){
                    Object object = courseList.get(i);
                    getCourses.add(object.toString().trim());
                }

                ArrayAdapter<String> courseAdapter = new ArrayAdapter<String>(NewFileUploadFull.this,android.R.layout.simple_spinner_item,getCourses);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCourse.setAdapter(courseAdapter);

                //Institution
                final List<String>getInstitution = new ArrayList<>();
                final int institutionSize = institutionList.size();
                for (int i=0;i<institutionSize;i++){
                    Object object = institutionList.get(i);
                    getInstitution.add(object.toString().trim());
                }

                ArrayAdapter<String> institutionAdapter = new ArrayAdapter<String>(NewFileUploadFull.this,android.R.layout.simple_spinner_item,getInstitution);
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
        } else if (radioImage.isChecked()){
            String title = fileTitle.getText().toString().trim();
            String description = fileDescription.getText().toString().trim();
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
            }
            if (mFileUri==null){
                Toast.makeText(this, "No image file selected", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(filePrice.getText().toString().trim())){
                filePrice.setError("Required");
            }
            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null && !TextUtils.isEmpty(filePrice.getText().toString().trim())){
                startPosting();
            }
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
    }

    private void startPosting() {
        mProgress.setMessage("Uploading file...");
        mProgress.setCanceledOnTouchOutside(false);
        final String title = fileTitle.getText().toString().trim();
        final String description = fileDescription.getText().toString().trim();
        String price = filePrice.getText().toString().trim();
        double unitPrice = Double.parseDouble(price);
        final String time = sdf.format(Calendar.getInstance().getTime());
        if (radioImage.isChecked()){
            mProgress.show();
            mFiles.child("image");
            StorageReference filepath = sStorage.child("image").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.push();
                    mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUpload.child("title").setValue(title.toUpperCase());
                            mUpload.child("description").setValue(description);
                            mUpload.child("timestamp").setValue(time);
                            mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                            mUpload.child("file_type").setValue("image");
                            mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                            mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                            mUpload.child("school").setValue(spinnerFaculty.getSelectedItem().toString());
                            mUpload.child("department").setValue(spinnerDepartment.getSelectedItem().toString());
                            mUpload.child("course").setValue(spinnerCourse.getSelectedItem().toString());
                            mUpload.child("price").setValue(finalUnitPrice);
                            mProgress.dismiss();
                            Toast.makeText(NewFileUploadFull.this, "File uploaded", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(NewFileUploadFull.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        else if (radioAudio.isChecked()){
            mProgress.show();
            mFiles.child("audio");
            StorageReference filepath = sStorage.child("audio").child(mFileUri.getLastPathSegment());
            final double finalUnitPrice = unitPrice;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.push();
                    mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUpload.child("title").setValue(title.toUpperCase());
                            mUpload.child("description").setValue(description);
                            mUpload.child("timestamp").setValue(time);
                            mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                            mUpload.child("file_type").setValue("audio");
                            mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                            mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                            mUpload.child("school").setValue(spinnerFaculty.getSelectedItem().toString());
                            mUpload.child("department").setValue(spinnerDepartment.getSelectedItem().toString());
                            mUpload.child("course").setValue(spinnerCourse.getSelectedItem().toString());
                            mUpload.child("price").setValue(finalUnitPrice);
                            mProgress.dismiss();
                            Toast.makeText(NewFileUploadFull.this, "File uploaded", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(NewFileUploadFull.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
                    @SuppressWarnings("VisibleForTests") final Task<Uri> downloadUrl =taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    final DatabaseReference mUpload = mFiles.push();
                    mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUpload.child("title").setValue(title.toUpperCase());
                            mUpload.child("description").setValue(description);
                            mUpload.child("timestamp").setValue(time);
                            mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                            mUpload.child("file_type").setValue("video");
                            mUpload.child("author").setValue(mAuth.getCurrentUser().getUid());
                            mUpload.child("institution").setValue(spinnerInstitution.getSelectedItem().toString());
                            mUpload.child("school").setValue(spinnerFaculty.getSelectedItem().toString());
                            mUpload.child("department").setValue(spinnerDepartment.getSelectedItem().toString());
                            mUpload.child("course").setValue(spinnerCourse.getSelectedItem().toString());
                            mUpload.child("price").setValue(finalUnitPrice);
                            mProgress.dismiss();
                            Toast.makeText(NewFileUploadFull.this, "File uploaded", Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(NewFileUploadFull.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
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

    public void checkAudioState(){

        if (mFileUri==null){
            Toast.makeText(NewFileUploadFull.this, "No audio file selected", Toast.LENGTH_SHORT).show();
        } else {
            if (mediaPlayer.isPlaying()){
                playAudio.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                mediaPlayer.stop();
            } else {
                playAudio.setImageResource(R.mipmap.baseline_stop_white_18dp);
                //Mediafileinfo item = new Mediafileinfo();
                try {
                    // mediaPlayer.setDataSource(String.valueOf(myUri));
                    mediaPlayer.setDataSource(NewFileUploadFull.this,mFileUri);
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

    public void checkRadioButtons() {
        radioImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioImage.isChecked()){
                    radioAudio.setChecked(false);
                    radioVideo.setChecked(false);
                    imageLayout.setVisibility(View.VISIBLE);
                    audioLayout.setVisibility(View.GONE);
                    videoLayout.setVisibility(View.GONE);
                    layoutEditTexts.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    layoutPrice.setVisibility(View.VISIBLE);
                }
            }
        });

        radioAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radioAudio.isChecked()){
                    radioImage.setChecked(false);
                    radioVideo.setChecked(false);
                    audioLayout.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.GONE);
                    videoLayout.setVisibility(View.GONE);
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
                    radioImage.setChecked(false);
                    radioAudio.setChecked(false);
                    videoLayout.setVisibility(View.VISIBLE);
                    imageLayout.setVisibility(View.GONE);
                    audioLayout.setVisibility(View.GONE);
                    layoutEditTexts.setVisibility(View.VISIBLE);
                    btnUpload.setVisibility(View.VISIBLE);
                    layoutPrice.setVisibility(View.VISIBLE);
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
                Picasso.with(NewFileUploadFull.this).load(uri).into(videoImage);
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
    }
}
