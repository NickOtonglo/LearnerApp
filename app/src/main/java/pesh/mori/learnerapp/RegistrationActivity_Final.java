package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by MORIAMA on 10/12/2017.
 */

public class RegistrationActivity_Final extends AppCompatActivity {

    private final AppCompatActivity activity = RegistrationActivity_Final.this;

    private Button button;
    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirm;

    private TextInputEditText textInputEditTextUsername;
    private TextInputEditText textInputEditTextPassword;
    private TextInputEditText textInputEditTextConfirm;
    private static int TIME_OUT = 10000;

    private InputValidation inputValidation;
    private Uri uri;

    private String activityFrom,fname,lname,dob,email,phone,mLink;

    private FirebaseAuth mAuth;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private Button btn;

    private DatabaseReference mDatabase,mNewUser,mNewMessage,mToken,mLinks;
    Calendar calendar;
    private SimpleDateFormat sdf;

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
        setContentView( R.layout.activity_credentials );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        if (!checkNetworkState()){
            Snackbar.make(findViewById(android.R.id.content),getString(R.string.error_no_internet_connectivity),Snackbar.LENGTH_LONG).show();
        }

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mNewMessage = FirebaseDatabase.getInstance().getReference().child("UserMessages");
        mToken = FirebaseDatabase.getInstance().getReference().child("Tokens");

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);

        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        Bundle newExtrs = getIntent().getExtras();
        activityFrom = newExtrs.getString("activity_from");
        fname = newExtrs.getString("fname");
        lname = newExtrs.getString("lname");
        dob = newExtrs.getString("dob");
        email = newExtrs.getString("email");
        phone = newExtrs.getString("phone");

        initViews();
        initObjects();
        emptyInputEditText();

//        Toast.makeText(getApplicationContext(), "You're almost done...", Toast.LENGTH_LONG).show();
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.info_you_are_almost_done)+"...",Snackbar.LENGTH_LONG).show();
        button = (Button) findViewById(R.id.signup);
    }
    private void initViews(){

        textInputLayoutUsername = (TextInputLayout) findViewById(R.id.textInputLayoutusername);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutpassword);
        textInputLayoutConfirm = (TextInputLayout) findViewById(R.id.textInputLayoutconfirm);

        textInputEditTextUsername = (TextInputEditText) findViewById(R.id.textInputEditTextusername);
        textInputEditTextPassword = (TextInputEditText) findViewById(R.id.textInputEditTextpassword);
        textInputEditTextConfirm = (TextInputEditText) findViewById(R.id.textInputEditTextconfirm);
    }
    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }

    private void emptyInputEditText(){
        textInputEditTextUsername.setText(null);
        textInputEditTextPassword.setText(null);
        textInputEditTextConfirm.setText(null);
    }

    public void doThis(View view) {
        if (!inputValidation.isInputEditTextFilled( textInputEditTextUsername, textInputLayoutUsername, getString( R.string.hint_enter_new_username) )) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled( textInputEditTextPassword, textInputLayoutPassword, getString( R.string.hint_enter_new_password ) )) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled( textInputEditTextConfirm, textInputLayoutConfirm, getString( R.string.hint_reenter_new_password ) )) {
            return;
        }
        mProgress = new ProgressDialog(RegistrationActivity_Final.this);
        mProgress.setTitle(getString(R.string.title_creating_your_account));
        mProgress.setMessage(getString(R.string.info_please_wait));
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this ,R.style.AlertDialogStyle);
        builder1.setMessage(getString(R.string.info_sign_up_notice));
        builder1.setCancelable( false );
        builder1.setPositiveButton( getString(R.string.option_proceed), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.show();
                        signUp();
                    }
                });
        builder1.setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
            }
        });
        builder1.setNeutralButton( R.string.link_review , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                launchTermsAndConditions();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void launchTermsAndConditions() {
        mLinks = FirebaseDatabase.getInstance().getReference().child("Links");
        mLinks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLink = String.valueOf(dataSnapshot.child("TandCs").getValue());
                Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse(mLink));
                RegistrationActivity_Final.this.startActivity( intent1 );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void signUp(){
        final String username = textInputEditTextUsername.getText().toString().trim();
        String password = textInputEditTextPassword.getText().toString().trim();
        String confirm = textInputEditTextConfirm.getText().toString().trim();

        if (!username.isEmpty() && !password.isEmpty() && !confirm.isEmpty()){
            if (!confirm.equals(password)){
                mProgress.dismiss();
                textInputEditTextConfirm.setError(getString(R.string.info_the_two_passwords_you_have_entered_must_be_exactly_the_same));
            } else {
                mAuth.createUserWithEmailAndPassword(email,password)
                        .addOnCompleteListener(RegistrationActivity_Final.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()){
                                    mProgress.dismiss();
                                    try {
                                        throw task.getException();
                                    } catch(FirebaseAuthWeakPasswordException e) {
                                        mAlert.setTitle(R.string.error_general)
                                                .setMessage(getString(R.string.error_registration_failed)+": "+task.getException().getMessage())
                                                .show();
                                    } catch(FirebaseAuthInvalidCredentialsException e) {
                                        mAlert.setTitle(R.string.error_general)
                                                .setMessage(getString(R.string.error_registration_failed)+": "+task.getException().getMessage())
                                                .show();
                                    } catch(FirebaseAuthUserCollisionException e) {
                                        mAlert.setTitle(R.string.error_general)
                                                .setMessage(getString(R.string.error_registration_failed)+": "+task.getException().getMessage())
                                                .show();
                                    } catch(Exception e) {
                                        mAlert.setTitle(R.string.error_general)
                                                .setMessage(getString(R.string.error_registration_failed)+": "+task.getException().getMessage())
                                                .show();
                                    }
                                } else {
                                    mNewUser = mDatabase.child(mAuth.getCurrentUser().getUid());
                                    mNewUser.child("status").setValue("enabled");
                                    mNewUser.child("fname").setValue(fname);
                                    mNewUser.child("lname").setValue(lname);
                                    mNewUser.child("email").setValue(email);
                                    mNewUser.child("dob").setValue(dob);
                                    mNewUser.child("phone").setValue(phone);
                                    mNewUser.child("username").setValue(username);
                                    mNewUser.child("profile_picture").setValue("");
                                    mNewUser.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                                    mNewUser.child("account_manager").setValue(getString(R.string.firebase_ref_users_account_manager));
                                    mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());

//                                    DatabaseReference newMessage = mNewMessage.child(mAuth.getCurrentUser().getUid());
//                                    newMessage.child("state").setValue("0");
//                                    newMessage.child("title").setValue("");
//                                    newMessage.child("item").setValue("");
//
//                                    DatabaseReference newToken = mToken.child(mAuth.getCurrentUser().getUid());
//                                    newToken.child("email").setValue(mAuth.getCurrentUser().getEmail());
//                                    newToken.child("balance").setValue("0");
//                                    newToken.child("suspended").setValue("0");

                                    mProgress.dismiss();
                                    mAlert.setTitle(R.string.info_sign_up_complete)
                                            .setMessage(R.string.link_proceed_to_login)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.option_login, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    mAuth.signOut();
                                                    Intent loginIntent = new Intent(RegistrationActivity_Final.this,SelectLoginActivity.class);
                                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(loginIntent);
                                                    finish();
                                                }
                                            })
                                    .show();
                                }
                            }
                        });
            }
        }
    }

    public boolean checkNetworkState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

//    private boolean executeCommand() {
//        System.out.println("executeCommand");
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
//            int mExitValue = mIpAddrProcess.waitFor();
//            System.out.println(" mExitValue " + mExitValue);
//            if (mExitValue == 0) {
//
//                return true;
//
//            } else {
//
//                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Network access unavailable!", Snackbar.LENGTH_LONG);
//                snackbar.show();
//            }
//        } catch (InterruptedException ignore) {
//            ignore.printStackTrace();
//            System.out.println(" Exception:" + ignore);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(" Exception:" + e);
//        }
//        return false;
//    }

}

