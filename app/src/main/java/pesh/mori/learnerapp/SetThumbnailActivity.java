package pesh.mori.learnerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetThumbnailActivity extends AppCompatActivity {
    private LinearLayout thumbLayout;
    private AppCompatButton btnThumbnail;
    private ImageView imgThumbnail;
    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private int PIC_COUNT;
    private Uri mThumbUri = null;
    private DatabaseReference mPost;
    private StorageReference sStorage;
    private FirebaseAuth mAuth;
    private String postCategory="",postKey="";
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_thumbnail);

        mAuth = FirebaseAuth.getInstance();

        thumbLayout = findViewById(R.id.layout_select_thumbnail);
        btnThumbnail = findViewById(R.id.btn_select_thumbnail);
        imgThumbnail = findViewById(R.id.img_thumbnail);
        mProgressBar = findViewById(R.id.progress_bar);

        postCategory = getIntent().getExtras().getString("postCategory");
        postKey = getIntent().getExtras().getString("postKey");

        mPost = FirebaseDatabase.getInstance().getReference().child(postCategory);
        sStorage = FirebaseStorage.getInstance().getReference().child(postCategory).child(mAuth.getCurrentUser().getUid());

        getImageIntent();

        if(mThumbUri==null){
            btnThumbnail.setText("Select post thumbnail");
        } else {
            btnThumbnail.setText("Set thumbnail");
        }
        btnThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mThumbUri==null){
                    getImageIntent();
                } else {
                    postThumbnail();
                }
            }
        });
    }

    private void getImageIntent() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST){
            try {
                mThumbUri = data.getData();
                CropImage.activity(mThumbUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(4,3)
                        .start(this);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode==CAMERA_REQUEST){
            try {
                mThumbUri = data.getData();
                CropImage.activity(mThumbUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setAspectRatio(4,3)
                        .start(this);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mThumbUri = result.getUri();
                imgThumbnail.setImageURI(mThumbUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(android.R.id.content),String.valueOf(error),Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void postThumbnail(){
        btnThumbnail.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        if (mThumbUri==null){
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        } else {
            final StorageReference thumbpath = sStorage.child("thumb").child(mThumbUri.getLastPathSegment());
            thumbpath.putFile(mThumbUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            FirebaseDatabase.getInstance().getReference().child(postCategory).child(postKey).child("thumbnail").setValue(uri.toString());
                            Toast.makeText(SetThumbnailActivity.this, "Thumbnail added", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                            finish();
                        }
                    });
                }
            });
        }
    }
}
