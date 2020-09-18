package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

    private TextView changEmailAddress;
    private TextView changePassword;
    private TextView changePhoneNumber;
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
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
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
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);

        txtName = findViewById(R.id.account_name);
        txtEmail = findViewById(R.id.account_email);
        txtPhone = findViewById(R.id.account_phone);

        changePassword = (TextView) findViewById(R.id.changepassword);
        changEmailAddress = (TextView) findViewById(R.id.changemail);
        profile = (CircleImageView) findViewById(R.id.profile_image);
        changePhoneNumber = (TextView) findViewById(R.id.changecell);
        bio = (TextView) findViewById(R.id.bio);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            if (isFacebookAuthenticated()){
                txtName.setText(String.valueOf(Profile.getCurrentProfile().getFirstName())+" "+(Profile.getCurrentProfile().getLastName()));
                txtEmail.setText("("+getString(R.string.info_logged_in_with_facebook)+")");
                txtPhone.setVisibility(View.GONE);
                mAlert.setTitle(R.string.title_profile_management)
                        .setMessage(getString(R.string.info_your_profile_is_currently_managed_by_external_service)+" (Facebook).")
                        .setPositiveButton(R.string.option_close_dialog, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                changePassword.setEnabled(false);
                changePassword.setTextColor(getResources().getColor(R.color.colorTextDisabled));
                final int sdk = android.os.Build.VERSION.SDK_INT;
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    changepassword.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
                } else {
//                    changepassword.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
                }
                changEmailAddress.setEnabled(false);
                changEmailAddress.setTextColor(getResources().getColor(R.color.colorTextDisabled));
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                    changemail.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
                } else {
//                    changemail.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
                }
                profile.setEnabled(false);
                changePhoneNumber.setEnabled(false);
                changePhoneNumber.setTextColor(getResources().getColor(R.color.colorTextDisabled));
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
            txtPhone.setText("("+getString(R.string.info_logged_in_with_google)+")");
            mAlert.setTitle(getString(R.string.title_profile_management))
                    .setMessage(getString(R.string.info_your_profile_is_currently_managed_by_external_service)+" (Google).")
                    .setPositiveButton(getString(R.string.option_close_dialog), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
            changePassword.setEnabled(false);
            changePassword.setTextColor(getResources().getColor(R.color.colorTextDisabled));
            final int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                changepassword.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
            } else {
//                changepassword.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
            }
            changEmailAddress.setEnabled(false);
            changEmailAddress.setTextColor(getResources().getColor(R.color.colorTextDisabled));
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
//                changemail.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey) );
            } else {
//                changemail.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.input_outline_grey));
            }
            profile.setEnabled(false);
            changePhoneNumber.setEnabled(false);
            changePhoneNumber.setTextColor(getResources().getColor(R.color.colorTextDisabled));
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

    }

    public void openThis(View view) {
        Intent changeEmailIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changeEmailIntent.putExtra("incomingIntent","changeEmailIntent");
        startActivity(changeEmailIntent);
        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
    }

    public void seeThis(View view) {
        Intent changePasswordIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changePasswordIntent.putExtra("incomingIntent","changePasswordIntent");
        startActivity(changePasswordIntent);
        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
    }

    public void showThis(View view) {
        Intent changePhoneIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
        changePhoneIntent.putExtra("incomingIntent","changePhoneIntent");
        startActivity(changePhoneIntent);
        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
    }
    public void viewThis(View view) {
        if(isFacebookAuthenticated() || isGoogleAuthenticated()){
            startActivity(new Intent(getApplicationContext(),BioActivity.class));
        }
        if (!isFacebookAuthenticated() && !isGoogleAuthenticated()){
            Intent changeBioIntent = new Intent(getApplicationContext(),AuthenticationActivity.class);
            changeBioIntent.putExtra("incomingIntent","changeBioIntent");
            startActivity(changeBioIntent);
            overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
        }
    }

    public void getImageIntent(){
        final CharSequence[] items = {getString(R.string.option_take_photo), getString(R.string.option_choose_from_gallery)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder.setTitle(R.string.title_update_picture);
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
            mProgress.setMessage(getString(R.string.info_saving_profile_picture));
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
                                                    Toast.makeText(ProfileActivity.this, R.string.info_profile_picture_updated, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(android.R.id.content), String.valueOf(error),Snackbar.LENGTH_LONG).show();
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
