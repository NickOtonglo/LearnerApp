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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Calendar;

public class AddSubPostActivity extends AppCompatActivity {

    private LinearLayout imageLayout,audioLayout,videoLayout,docLayout,layoutFiles,layoutFileTypes;
    private RelativeLayout videoPlayerLayout,audioPlayerLayout;
    private Button btnImage,btnAudio,btnVideo,btnDoc,btnUpload;
    private ImageView imageImage,videoImage,btnPlayVideo;
    private ImageView btnPlayAudio;
    private TextView docTextView;
    private EditText fileTitle,fileDescription;
    private VideoView videoView,audioView;
    private int PIC_COUNT;

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int AUDIO_REQUEST = 3;
    private static final int VIDEO_REQUEST = 4;
    private static final int DOC_REQUEST = 5;

    private int SPINNER_COUNT = 0;

    private Uri mFileUri = null;
    private MediaPlayer mediaPlayer;

    private DatabaseReference mPost,mAllPosts;
    private StorageReference sStorage;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;
    private CustomDialogActivity mProgressCustom;
    Calendar calendar;
    private SimpleDateFormat sdf;

    private Spinner spinner;

    private MediaController mediaController;

    private ProgressBar progressBuffer,progressBufferAudio;

    private String uploadType="",postType="",parentKey="",mAuthor="";

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
        setContentView(R.layout.activity_add_sub_post);

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

        postType = getIntent().getExtras().getString("postNode");
        parentKey = getIntent().getExtras().getString("postKey");
        mAuthor = getIntent().getExtras().getString("author");

        mProgress = new ProgressDialog(this);
        mProgressCustom = new CustomDialogActivity(AddSubPostActivity.this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mAuth = FirebaseAuth.getInstance();

        mediaController = new MediaController(this);
        mediaPlayer = new MediaPlayer();

        spinner = findViewById(R.id.spinner_file_types);

        layoutFiles = findViewById(R.id.layout_files);
        layoutFileTypes = findViewById(R.id.layout_file_type);
        videoPlayerLayout = findViewById(R.id.layout_upload_video_player);
        audioPlayerLayout = findViewById(R.id.layout_upload_audio_player);
        audioLayout = (LinearLayout) findViewById(R.id.layout_3);
        imageLayout = findViewById(R.id.layout_8);
        videoLayout = (LinearLayout) findViewById(R.id.layout_4);
        docLayout = (LinearLayout)findViewById(R.id.layout_7);
        progressBuffer = findViewById(R.id.buffer_progress_upload);
        progressBufferAudio = findViewById(R.id.buffer_progress_upload_audio);

        fileTitle = (EditText) findViewById(R.id.txt_title);
        fileDescription = (EditText) findViewById(R.id.txt_description);

        btnImage = (Button) findViewById(R.id.btn_select_image);
        btnAudio = (Button) findViewById(R.id.btn_select_audio);
        btnVideo = (Button) findViewById(R.id.btn_select_video);
        btnDoc = (Button)findViewById(R.id.btn_select_doc);
        btnUpload = (Button) findViewById(R.id.btn_upload_file);

        btnPlayAudio = (ImageView)findViewById(R.id.btn_play_audio);
        imageImage = findViewById(R.id.img_view_image);
        videoImage = (ImageView)findViewById(R.id.img_video_placeholder);
        docTextView = (TextView)findViewById(R.id.txt_select_doc);
        videoView = findViewById(R.id.img_upload_play_video);
        audioView = findViewById(R.id.img_upload_play_audio);
        btnPlayVideo = findViewById(R.id.btn_view_upload_time_play);

        mAllPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));

//        loadSpinners();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                uploadType = selectedItemText;
                if (SPINNER_COUNT >= 1 && (selectedItemText.equals("Image") || selectedItemText.equals("Audio") || selectedItemText.equals("Video")
                || selectedItemText.equals("PDF") || selectedItemText.equals("Text"))){
                    Log.d("LOG_selectedItemText",selectedItemText);
                    Log.d("LOG_SPINNER_COUNT", String.valueOf(SPINNER_COUNT));
                    setPreVisibility();
                }
                SPINNER_COUNT++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageIntent();
            }
        });
    }

    private void loadSpinners(){
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.show();

        String[] listItems = getResources().getStringArray(R.array.spinner_sub_post_file_types);

        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(AddSubPostActivity.this,android.R.layout.simple_spinner_item,listItems);
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(listAdapter);

        mProgress.dismiss();
    }

    private void checkRequirements() {
        Log.d("LOG_checkRequirements","uploadType: "+uploadType);
        String title = fileTitle.getText().toString().trim();
        String description = fileDescription.getText().toString().trim();
        if (uploadType.equals("Image")){
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
                Log.d("LOG_checkRequirements_1","empty fields");
            }
            if (mFileUri==null){
                Toast.makeText(this, R.string.info_no_image_file_selected, Toast.LENGTH_SHORT).show();
            }
            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null){
                startPosting();
            }
        }
        if (uploadType.equals("Audio")){
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
            }
            if (mFileUri==null){
                Toast.makeText(this, R.string.info_no_audio_file_selected, Toast.LENGTH_SHORT).show();
            }
            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null){
                startPosting();
            }
        } else if (uploadType.equals("Video")){
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content),getString(R.string.info_one_or_more_required_fields_is_empty), Snackbar.LENGTH_LONG).show();
            }
            if (mFileUri==null){
                Toast.makeText(this, R.string.info_no_video_file_selected, Toast.LENGTH_SHORT).show();
            }
            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null){
                startPosting();
            }
        } else if (uploadType.equals("PDF")){
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content),getString(R.string.info_one_or_more_required_fields_is_empty), Snackbar.LENGTH_LONG).show();
            }
            if (mFileUri==null){
                Toast.makeText(this, R.string.info_no_supported_document_file_selected, Toast.LENGTH_SHORT).show();
            }
            if (!title.isEmpty() && !description.isEmpty() && mFileUri!=null){
                startPosting();
            }
        } else if (uploadType.equals("Text")){
            if (title.isEmpty() || description.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content),getString(R.string.info_one_or_more_required_fields_is_empty), Snackbar.LENGTH_LONG).show();
            }
            if (!title.isEmpty() && !description.isEmpty()){
                startPosting();
            }
        }
    }

    private void startPosting() {
        final String title = fileTitle.getText().toString().trim();
        final String description = fileDescription.getText().toString().trim();
        final String time = sdf.format(Calendar.getInstance().getTime());
        String fileTypeChild = "";

        StorageMetadata metadata = null;

        mPost = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_sub)).child(postType).child(parentKey);
        sStorage = FirebaseStorage.getInstance().getReference().child(postType)
                .child(mAuth.getCurrentUser().getUid());
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1)) && !spinner.getSelectedItem().toString().equals("Text")){
            metadata = new StorageMetadata.Builder()
                    .setCustomMetadata(getString(R.string.firebase_ref_posts_type_1),getFileName(mFileUri).replace(" ","_"))
                    .build();
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2)) && !spinner.getSelectedItem().toString().equals("Text")){
            metadata = new StorageMetadata.Builder()
                    .setCustomMetadata(getString(R.string.firebase_ref_posts_type_2),getFileName(mFileUri).replace(" ","_"))
                    .build();
        }

        uploadType = spinner.getSelectedItem().toString();
        if (uploadType.equals("Image")){
            fileTypeChild = "image";
        } else if (uploadType.equals("Audio")){
            fileTypeChild = "audio";
        } else if (uploadType.equals("Video")){
            fileTypeChild = "video";
        } else if (uploadType.equals("PDF")){
            fileTypeChild = "doc";
        } else if (uploadType.equals("Text")){
            fileTypeChild = "text";
        }

        mProgressCustom.show(false);
//            mPost.child(fileTypeChild);
        if (!fileTypeChild.equals("text")){
            StorageReference filepath = sStorage.child(fileTypeChild).child(mFileUri.getLastPathSegment());
            String finalFileTypeChild = fileTypeChild;
            filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final DatabaseReference mUpload = mPost.push();
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final Uri downloadUrl = uri;
                            mPost.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    mUpload.child("title").setValue(title.toUpperCase());
                                    mUpload.child("description").setValue(description);
                                    mUpload.child("timestamp").setValue(time);
                                    mUpload.child("file_path").setValue(String.valueOf(downloadUrl));
                                    mUpload.child("file_type").setValue(finalFileTypeChild);
                                    mProgressCustom.dismiss();
                                    Toast.makeText(AddSubPostActivity.this, R.string.info_sub_post_created, Toast.LENGTH_LONG).show();
                                    mAlert.setCancelable(false)
                                            .setMessage(R.string.info_you_can_view_and_manage_sub_posts)
                                            .setPositiveButton(R.string.option_view_sub_post, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                Intent intent = new Intent(getApplicationContext(),PreviewPostActivity.class);
//                                                intent.putExtra("postKey",keyRef);
//                                                startActivity(intent);
//                                                finish();
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
                                    Toast.makeText(AddSubPostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
        } else {
            final DatabaseReference mUpload = mPost.push();
            final String[] childKey = {mUpload.getKey()};
            String finalFileTypeChild1 = fileTypeChild;
            mPost.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mUpload.child("title").setValue(title.toUpperCase());
                    mUpload.child("description").setValue(description);
                    mUpload.child("timestamp").setValue(time);
                    mUpload.child("file_path").setValue("");
                    mUpload.child("file_type").setValue(finalFileTypeChild1).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mProgressCustom.dismiss();
                                Toast.makeText(AddSubPostActivity.this, R.string.info_sub_post_created, Toast.LENGTH_LONG).show();
                                btnUpload.setVisibility(View.GONE);
                                mAlert.setCancelable(false)
                                        .setMessage(R.string.info_you_can_view_and_manage_sub_posts)
                                        .setPositiveButton(R.string.option_view_sub_post, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(getApplicationContext(),ViewSubPostActivity.class);
                                                intent.putExtra("file_key",parentKey)
                                                        .putExtra("author",mAuthor)
                                                        .putExtra("postType",postType)
                                                        .putExtra("childKey",childKey[0]);
                                                startActivity(intent);
                                            }
                                        })
                                        .setNeutralButton(R.string.option_alert_later, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                mProgressCustom.dismiss();
                                Toast.makeText(AddSubPostActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(AddSubPostActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
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
            Toast.makeText(AddSubPostActivity.this, getString(R.string.info_no_audio_file_selected), Toast.LENGTH_SHORT).show();
        } else {
            if (mediaPlayer.isPlaying()){
//                playAudio.setImageResource(R.mipmap.baseline_play_circle_outline_white_18dp);
                mediaPlayer.stop();
            } else {
//                playAudio.setImageResource(R.mipmap.baseline_stop_white_18dp);
                //Mediafileinfo item = new Mediafileinfo();
                try {
                    // mediaPlayer.setDataSource(String.valueOf(myUri));
                    mediaPlayer.setDataSource(AddSubPostActivity.this,mFileUri);
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

    public void setPreVisibility(){
        if (uploadType.equals("Image")){
            layoutFiles.setVisibility(View.VISIBLE);
            imageLayout.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            btnImage.setVisibility(View.VISIBLE);
            layoutFileTypes.setVisibility(View.GONE);
        }
        if (uploadType.equals("Audio")){
            layoutFiles.setVisibility(View.VISIBLE);
            audioLayout.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            btnAudio.setVisibility(View.VISIBLE);
            layoutFileTypes.setVisibility(View.GONE);
        }
        if (uploadType.equals("Video")){
            layoutFiles.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            btnVideo.setVisibility(View.VISIBLE);
            layoutFileTypes.setVisibility(View.GONE);
        }
        if (uploadType.equals("PDF")){
            layoutFiles.setVisibility(View.VISIBLE);
            docLayout.setVisibility(View.VISIBLE);
            btnUpload.setVisibility(View.VISIBLE);
            layoutFileTypes.setVisibility(View.GONE);
        }
        if (uploadType.equals("Text")){
            layoutFiles.setVisibility(View.GONE);
            btnUpload.setVisibility(View.VISIBLE);
            layoutFileTypes.setVisibility(View.GONE);
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

    private void getVideoIntent() {
        Intent intent_upload = new Intent();
        intent_upload.setType("video/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload,VIDEO_REQUEST);
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
                Log.d("LOG_mFileUri",mFileUri.toString());
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
                Log.d("LOG_mFileUri",mFileUri.toString());
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
                Log.d("LOG_mFileUri",mFileUri.toString());
                audioPlayerLayout.setVisibility(View.VISIBLE);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==VIDEO_REQUEST){
            try {
                mFileUri = data.getData();
                Log.d("LOG_mFileUri",mFileUri.toString());
                Uri uri = mFileUri.buildUpon().appendQueryParameter("t", "0").build();
                Picasso.with(AddSubPostActivity.this).load(uri).into(videoImage);
                videoPlayerLayout.setVisibility(View.VISIBLE);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode== DOC_REQUEST){
            try {
                mFileUri = data.getData();
                Log.d("LOG_mFileUri",mFileUri.toString());
                docTextView.setText(getFileName(mFileUri));
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mFileUri = result.getUri();
                Log.d("LOG_mFileUri",mFileUri.toString());
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