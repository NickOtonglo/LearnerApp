package pesh.mori.learnerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GoogleAuthHandlerActivity extends AppCompatActivity {
    private TextView txtMessage;
    private DatabaseReference mUser;
    private FirebaseAuth mAuth;
    private HashMap<String, String> hashMap;
    private Calendar calendar;
    private SimpleDateFormat sdf;

    private static final int RC_SIGN_IN = 1;
    private Intent returnIntent;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private static final String TAG = "LOG_GOOGLE_AUTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.Theme_UserDialogDark);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.Theme_UserDialogSignature);
        } else {
            setTheme(R.style.Theme_UserDialog);
        }
        setContentView(R.layout.activity_google_auth_handler);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mAuth = FirebaseAuth.getInstance();
        txtMessage = findViewById(R.id.txt_message);

        returnIntent = getIntent();

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignIn();

    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                txtMessage.setText(getString(R.string.info_authenticating_please_wait));
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e);
                returnIntent.putExtra("LOG_RESULT","FAIL");
                onBackPressed();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
                            updateUI("google");
                        } else {
                            // If sign in fails, display a message to the user.
//                            Log.d(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), R.string.error_authentication_failed_try_again, Toast.LENGTH_SHORT).show();
                            returnIntent.putExtra("LOG_RESULT","FAIL");
                            setResult(Activity.RESULT_CANCELED,returnIntent);
                            onBackPressed();
                        }

                    }
                });
    }

    private void updateUI(String vendor) {
//        Log.d("updateUI","vendor: "+vendor);
        checkAccountExistence(vendor);
    }

    private void checkAccountExistence(final String vendor){
        if (mAuth!=null){
//            Log.d("checkAccountExistence","mAuth not null");
            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
//                        Log.d("checkAccountExistence","account already exists");
                        returnIntent.putExtra("LOG_RESULT","SUCCESS");
                        setResult(Activity.RESULT_OK,returnIntent);
                        onBackPressed();
                    } else {
                        createAccount(vendor);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
//            Log.d("checkAccountExistence","mAuth null");
            Toast.makeText(this, getString(R.string.error_occurred_try_again), Toast.LENGTH_SHORT).show();
            googleSignIn();
        }

    }

    private void createAccount(final String vendor) {
        hashMap = new HashMap<String, String>();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (vendor.equals("google")) {
            mUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            final DatabaseReference mNewAccount = mUser;
            mUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

//                    hashMap.put("status","enabled");
//                    hashMap.put("fname","");
//                    hashMap.put("lname","");
//                    hashMap.put("email",String.valueOf(account.getEmail()));
//                    hashMap.put("dob","");
//                    hashMap.put("phone","");
//                    hashMap.put("username",String.valueOf(account.getDisplayName()));
//                    hashMap.put("profile_picture",String.valueOf(account.getPhotoUrl()));
//                    hashMap.put("timestamp",sdf.format(Calendar.getInstance().getTime()).toString());
//                    hashMap.put("account_manager",vendor);
//                    mNewAccount.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()){
//                                returnIntent.putExtra("LOG_RESULT","SUCCESS");
//                                setResult(Activity.RESULT_OK,returnIntent);
//                                onBackPressed();
//                            }
//                        }
//                    });

                    DatabaseReference mGoogleAccount = mNewAccount;
                    mGoogleAccount.child("status").setValue("enabled");
                    mGoogleAccount.child("fname").setValue("");
                    mGoogleAccount.child("lname").setValue("");
                    mGoogleAccount.child("email").setValue(account.getEmail().toString());
                    mGoogleAccount.child("dob").setValue("");
                    mGoogleAccount.child("phone").setValue("");
                    mGoogleAccount.child("username").setValue(account.getDisplayName().toString());
                    mGoogleAccount.child("profile_picture").setValue(account.getPhotoUrl().toString());
                    mGoogleAccount.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()).toString());
                    mGoogleAccount.child("account_manager").setValue("google").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            final DatabaseReference mNewBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
                            mNewBio.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (task.isSuccessful()){
                                        if (!dataSnapshot.exists()){
                                            mNewBio.child("about").setValue("Hi! My name is "+account.getDisplayName().toString()+".");
                                            mNewBio.child("facebook").setValue("");
                                            mNewBio.child("gender").setValue("not specified");
                                            mNewBio.child("linkedin").setValue("");
                                            mNewBio.child("skill_details").setValue("");
                                            mNewBio.child("skills_sector").setValue("");
                                            mNewBio.child("twitter").setValue("");
                                        }
                                        returnIntent.putExtra("LOG_RESULT","SUCCESS");
                                        setResult(Activity.RESULT_OK,returnIntent);
//                                        Toast.makeText(GoogleAuthHandlerActivity.this, "Signed in as "+account.getDisplayName(), Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
//                                    returnIntent.putExtra("LOG_RESULT","FAIL");
//                                    setResult(Activity.RESULT_CANCELED,returnIntent);
//                                    onBackPressed();
                                }
                            });
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                    returnIntent.putExtra("LOG_RESULT","FAIL");
//                    setResult(Activity.RESULT_CANCELED,returnIntent);
//                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
