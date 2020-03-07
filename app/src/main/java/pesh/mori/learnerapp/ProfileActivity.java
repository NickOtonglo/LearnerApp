package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by MORIAMA on 12/12/2017.
 */

public class ProfileActivity extends AppCompatActivity {

    private TextView changemail;
    private TextView changepassword;
    private TextView changecell;
    private TextView bio;
    private TextView txtName,txtEmail,txtPhone;
    private CircleImageView profile;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private StorageReference sStorage;

    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;

    private Uri mFileUri = null;

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;
    private static final int AUDIO_REQUEST = 3;
    private static final int VIDEO_REQUEST = 4;
    private int PIC_COUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile);
//        Toast.makeText(getApplicationContext(), "INFO | Under Development", Toast.LENGTH_SHORT).show();

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this);

        txtName = findViewById(R.id.account_name);
        txtEmail = findViewById(R.id.account_email);
        txtPhone = findViewById(R.id.account_phone);

        changepassword = (TextView) findViewById(R.id.changepassword);
        changemail = (TextView) findViewById(R.id.changemail);
        profile = (CircleImageView) findViewById(R.id.profile_image);
        changecell = (TextView) findViewById(R.id.changecell);
        bio = (TextView) findViewById(R.id.bio);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            if (isFacebookAuthenticated()){
                txtName.setText(String.valueOf(Profile.getCurrentProfile().getFirstName())+" "+(Profile.getCurrentProfile().getLastName()));
                txtEmail.setText("(Logged in with Facebook)");
                txtPhone.setVisibility(View.GONE);
                txtPhone.setTextColor(Color.parseColor("#3b5999"));
                mAlert.setTitle("Profile Management")
                        .setMessage("Your profile is currently managed by an external service (Facebook).")
                        .setPositiveButton("Close Dialog", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                changepassword.setEnabled(false);
                changepassword.setTextColor(Color.parseColor("#cccccc"));
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    changepassword.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
                } else {
//                    changepassword.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
                }
                changemail.setEnabled(false);
                changemail.setTextColor(Color.parseColor("#cccccc"));
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    changemail.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
                } else {
//                    changemail.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
                }
                profile.setEnabled(false);
                changecell.setEnabled(false);
                changecell.setTextColor(Color.parseColor("#cccccc"));
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    changecell.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
                } else {
//                    changecell.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
                }
//                bio.setEnabled(false);
                String facebookProfilePic = Profile.getCurrentProfile().getProfilePictureUri(300,300).toString();
                Picasso.with(getApplicationContext()).load(facebookProfilePic).into(profile);
            } else {
                sStorage = FirebaseStorage.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        txtName.setText(String.valueOf(dataSnapshot.child("fname").getValue()) + " " + (dataSnapshot.child("lname").getValue())
                                +" ("+dataSnapshot.child("username").getValue()+")");
                        txtEmail.setText(String.valueOf(dataSnapshot.child("email").getValue()));
                        txtPhone.setText(String.valueOf(dataSnapshot.child("phone").getValue()));
                        String profilePicture = String.valueOf(dataSnapshot.child("profile_picture").getValue());
                        if (!profilePicture.equals("")) {
                            mFileUri = Uri.parse(profilePicture);
                            Picasso.with(getApplicationContext()).load(mFileUri).into(profile);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
        else if (account != null){
            txtName.setText(account.getDisplayName());
            txtEmail.setText(account.getEmail());
            txtPhone.setText("(Logged in with Google)");
            mAlert.setTitle("Profile Management")
                    .setMessage("Your profile is currently managed by an external service (Google).")
                    .setPositiveButton("Close Dialog", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
            changepassword.setEnabled(false);
            changepassword.setTextColor(Color.parseColor("#cccccc"));
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                changepassword.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
            } else {
//                changepassword.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
            }
            changemail.setEnabled(false);
            changemail.setTextColor(Color.parseColor("#cccccc"));
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                changemail.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
            } else {
//                changemail.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
            }
            profile.setEnabled(false);
            changecell.setEnabled(false);
            changecell.setTextColor(Color.parseColor("#cccccc"));
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                changecell.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
            } else {
//                changecell.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
            }
//            bio.setEnabled(false);
            Uri accountPictureUri = account.getPhotoUrl();
            Picasso.with(getApplicationContext()).load(String.valueOf(accountPictureUri)).into(profile);
        }


        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageIntent();
            }
        });

        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        changecell.startAnimation( myanim );
        changemail.startAnimation( myanim );
        changepassword.startAnimation( myanim );
        bio.startAnimation( myanim );

    }
    public void onButtonShowPopupWindowClick(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        profile = (CircleImageView) findViewById(R.id.profile);
        profile.startAnimation( myanim );

        // get a reference to the already created main layout
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.activity_profile);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.profilepopup_activity, null);

        // create the popup window
        int width = LayoutParams.MATCH_PARENT;
        int height = LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.animation);

        // show the popup window
        popupWindow.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
    public void openThis(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        changemail.startAnimation( myanim );
        Intent changeEmailIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changeEmailIntent.putExtra("incomingIntent","changeEmailIntent");
        startActivity(changeEmailIntent);

    }

    public void seeThis(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        changepassword.startAnimation( myanim );
        Intent changePasswordIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changePasswordIntent.putExtra("incomingIntent","changePasswordIntent");
        startActivity(changePasswordIntent);
    }

    public void showThis(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        changecell.startAnimation( myanim );
        Intent changePhoneIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changePhoneIntent.putExtra("incomingIntent","changePhoneIntent");
        startActivity(changePhoneIntent);
    }
    public void viewThis(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        bio.startAnimation( myanim );
        if(isFacebookAuthenticated() || isGoogleAuthenticated()){
            startActivity(new Intent(getApplicationContext(),BioActivity.class));
        }
        if (!isFacebookAuthenticated() && !isGoogleAuthenticated()){
            Intent changeBioIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
            changeBioIntent.putExtra("incomingIntent","changeBioIntent");
            startActivity(changeBioIntent);
        }
    }

    public void getImageIntent(){
        final CharSequence[] items = {"Take Photo", "Choose from Gallery"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Picture");
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            mProgress.setMessage("Saving profile picture...");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mFileUri = result.getUri();
                profile.setImageURI(mFileUri);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(mFileUri.toString()))
                        .build();
                mUser.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    StorageReference filepath = sStorage.child("image").child(mFileUri.getLastPathSegment());
                                    filepath.putFile(mFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    Uri downloadUrl = uri;
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("profile_picture")
                                                            .setValue(String.valueOf(downloadUrl));
                                                    mProgress.dismiss();
                                                    Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(android.R.id.content),String.valueOf(error),Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
//        finish();
        onBackPressed();
        return true;
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

    public void onBackPressed() {
        super.onBackPressed();
//        Intent clearIntent = new Intent(getApplicationContext(),HomeActivity.class);
//        clearIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(clearIntent);
//        finish();
    }

    public boolean isFacebookAuthenticated() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public boolean isGoogleAuthenticated(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null){
            return false;
        } else {
            return true;
        }
    }
}
