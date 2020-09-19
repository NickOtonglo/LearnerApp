package pesh.mori.learnerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class SelectLoginActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    private FirebaseAuth mAuth;
    private Button btnFacebookLogin;
    private Button btnGoogleSignIn;
    private String node="Texts",socket="BidSocket";

    private static final int APP_UPDATE=1,GET_GOOGLE_AUTH_REQUEST=2,GET_FACEBOOK_AUTH_RESULT=3;
    Calendar calendar;
    private SimpleDateFormat sdf;
    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;
    private ProgressBar mProgressBar,mProgressBar1,mProgressBarMini;
    private ImageView imgLogo;
    private LinearLayout layoutLogin,layoutUpdate,layoutMaintenance;
    private LinearLayout txtRegister;
    private TextView txtForgotPassword;
    private TextView txtBottomLabel;

    private int vCode = BuildConfig.VERSION_CODE;
    private String vName = BuildConfig.VERSION_NAME;
    private int code;
    private String force,message;

   //Maintenance
    private TextView txtMaintenance;
    private AppCompatButton btnExit;

    //Update
    private TextView txtUpdateMessage,txtUpdateHeader;
    private AppCompatButton btnCancel,btnUpdate;
    DatabaseReference mVersion;

    //Login
    private TextInputEditText txtEmail;
    private TextInputEditText txtPassword;
    private Button btnLogin;

    //Facebook
    private CallbackManager mCallbackManager;

    //Google
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    //Privacy Policy
    private TextView txtPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_login);
        initCloudPackets();
        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgressBar = findViewById(R.id.progress_bar_login);
        mProgressBar1 = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mProgressBarMini = findViewById(R.id.progress_bar_mini);
        txtBottomLabel = findViewById(R.id.txt_bottom_label);

        layoutLogin = findViewById(R.id.layout_login);
        layoutUpdate = findViewById(R.id.layout_update);
        layoutMaintenance = findViewById(R.id.layout_maintenance);
        imgLogo = findViewById(R.id.img_logo);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (mAuth.getCurrentUser() == null){
            final int secs = 1;
            new CountDownTimer((secs +1) * 1000, 1000) {
                @Override
                public final void onTick(final long millisUntilFinished) {

                }
                @Override
                public final void onFinish() {
                    initialiseActivity();
                }
            }.start();
        } else {
            initialiseActivity();
        }

    }

    public void initialiseActivity(){
        if (!checkNetworkState()){
            mAlert.setMessage(getString(R.string.info_no_internet_connection_ensure_then_reload))
                    .setCancelable(true)
                    .setPositiveButton(R.string.option_reload, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            initialiseActivity();
                        }
                    })
                    .setNeutralButton(getString(R.string.option_exit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            txtBottomLabel.setText(R.string.info_no_internet_connection_sad_face);
                        }
                    });
            /*v1.0.4 bug fix 00004*/
            if(!SelectLoginActivity.this.isFinishing()){
                mAlert.show();
            }
        } else {
            checkMaintenanceStatus();
        }
    }

    public void loadData(){
        layoutLogin.setEnabled(true);
        layoutUpdate.setVisibility(View.GONE);
        mProgressBarMini.setVisibility(View.GONE);
        mProgressBar1.setVisibility(View.VISIBLE);
        txtRegister = findViewById(R.id.txt_register);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegistrationActivity_Initial.class));
            }
        });
        txtForgotPassword = findViewById(R.id.txt_forgot_password);
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),ForgotPasswordActivity.class));
            }
        });

        txtPrivacyPolicy = findViewById(R.id.txt_privacy_policy);
        txtPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPrivacyPolicy();
            }
        });

        txtEmail = findViewById(R.id.txtLoginEmail);
        txtPassword = findViewById(R.id.txtLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLogin();
            }
        });

        btnFacebookLogin = findViewById(R.id.facebook_login);
        btnGoogleSignIn = findViewById(R.id.google_login);

        checkAuthState();

        mCallbackManager = CallbackManager.Factory.create();
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),FacebookAuthHandlerActivity.class),GET_FACEBOOK_AUTH_RESULT);
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),GoogleAuthHandlerActivity.class),GET_GOOGLE_AUTH_REQUEST);
            }
        });
    }

    private void checkAuthState() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            mProgressBar1.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            if (mAuth.getCurrentUser().getDisplayName() == null || mAuth.getCurrentUser().getDisplayName().toString().isEmpty()){

            } else {
                Intent loginIntent = new Intent(SelectLoginActivity.this,HomeActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar1.setVisibility(View.GONE);
            imgLogo.requestLayout();
            imgLogo.getLayoutParams().height = 200;
            layoutLogin.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
//            updateUI();
        }
    }

    public void initCloudPackets(){
        frameLayout = findViewById(R.id.frame_select_login_main);
        frameLayout.setEnabled(false);
        FirebaseDatabase.getInstance().getReference().child(node).child(socket).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()||!SocketsHandler.validateSockets(Integer.parseInt(String.valueOf(dataSnapshot.getValue())))) {
                    finish();
                } else {
                    frameLayout.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_GOOGLE_AUTH_REQUEST){
            if (resultCode == Activity.RESULT_CANCELED){
                LoginManager.getInstance().logOut();
                signOut();
                mAuth.signOut();
            }
            if (resultCode == Activity.RESULT_OK){
//                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                checkAuthState();
            }
        }
        if (requestCode == GET_FACEBOOK_AUTH_RESULT){
            if (resultCode == Activity.RESULT_CANCELED){
                LoginManager.getInstance().logOut();
                signOut();
                mAuth.signOut();
            }
            if (resultCode == Activity.RESULT_OK){
//                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                checkAuthState();
            }
        }
        if (requestCode == APP_UPDATE){
            startActivity(new Intent(getApplicationContext(),SelectLoginActivity.class));
            finish();
        }
    }

    public boolean isFacebookAuthenticated() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void startLogin() {
        String emailLogin = txtEmail.getText().toString().trim();
        String passwordLogin = txtPassword.getText().toString();

        if (emailLogin.isEmpty()){
            txtEmail.setError(getString(R.string.hint_enter_your_email_address));
        }
        if (passwordLogin.isEmpty()){
            txtPassword.setError(getString(R.string.hint_enter_password));
        }
        if (!emailLogin.isEmpty() && !passwordLogin.isEmpty()){
            layoutLogin.setVisibility(View.GONE);
            mProgress.setMessage(getString(R.string.info_please_wait));
            if (!mProgress.isShowing()){
                mProgress.show();
            }
            mAuth.signInWithEmailAndPassword(emailLogin,passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            layoutLogin.setVisibility(View.VISIBLE);
                            mProgress.dismiss();
                            mAlert.setTitle(R.string.error_general)
                                    .setMessage(getString(R.string.error_login_failed)+": "+task.getException().getMessage())
                                    .show();
                        }
                    } else {
                        /*for production only*/
//                        if (!Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()){
//                            Intent homeIntent = new Intent(getApplicationContext(), EmailVerificationActivity.class);
//                            homeIntent.putExtra("outgoingIntent","SelectLoginActivity");
//                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(homeIntent);
//                        } else {
//                            Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
//                            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                            startActivity(homeIntent);
//                            finish();
//                        }
                        Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        finish();
                    }
                }
            });
        }
    }

    public void handleUpdate(){
        layoutLogin.setEnabled(false);
        layoutLogin.setVisibility(View.GONE);
        mProgressBarMini.setVisibility(View.VISIBLE);
        btnCancel = findViewById(R.id.btn_cancel);
        btnUpdate = findViewById(R.id.btn_update);
        txtUpdateMessage = findViewById(R.id.txt_message);
        txtUpdateHeader = findViewById(R.id.txt_header);
        FirebaseDatabase.getInstance().getReference().child("App").child("Version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("LOG_DataSnapshot",dataSnapshot.toString());
                try {
                    PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
                    vCode = pInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (dataSnapshot.exists()){
                    code = Integer.parseInt(String.valueOf(dataSnapshot.child("VersionCode").getValue()));
                    force = String.valueOf(dataSnapshot.child("ForceUpdate").getValue());
                    message = String.valueOf(dataSnapshot.child("UpdateMessage").getValue());
                    if (code>vCode){
                        imgLogo.requestLayout();
                        imgLogo.getLayoutParams().height = 200;
                        layoutLogin.setVisibility(View.GONE);
                        layoutMaintenance.setVisibility(View.GONE);
                        layoutUpdate.setVisibility(View.VISIBLE);
                        txtUpdateMessage.setText(message);
                        mProgressBarMini.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        if (force.equals("true")){
                            btnCancel.setText(getString(R.string.option_exit));
                            txtUpdateHeader.setText(R.string.info_an_update_is_required);
                            txtUpdateMessage.setText(String.valueOf(dataSnapshot.child("UpdateMessageForce").getValue()));
                            btnUpdate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException exception) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            });
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                    System.exit(0);
                                }
                            });
                        } else if (force.equals("false")){
                            btnCancel.setText(R.string.option_later);
                            btnUpdate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            });
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    loadData();
                                }
                            });
                        }

                    } else {
                        loadData();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.d("LOG_DatabaseError",databaseError.getMessage());
            }
        });
    }

    public void checkMaintenanceStatus(){
        mProgressBarMini.setVisibility(View.VISIBLE);
        txtMaintenance = findViewById(R.id.txt_maintenance);
        btnExit = findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                System.exit(0);
            }
        });
        FirebaseDatabase.getInstance().getReference().child("App").child("System").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("MaintenanceState").getValue().equals("true")){
                        layoutLogin.setEnabled(false);
                        layoutLogin.setVisibility(View.GONE);
                        layoutMaintenance.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        mProgressBarMini.setVisibility(View.GONE);
                    } else {
                        if (dataSnapshot.child("MaintenanceState").getValue().equals("false")) {
                            layoutMaintenance.setVisibility(View.GONE);
                            handleUpdate();
                        } else {
                            layoutLogin.setVisibility(View.GONE);
                            layoutLogin.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            mProgressBarMini.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isGoogleSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null;
    }

    private void signOut() {
        if (isGoogleSignedIn()){
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
        }
    }

    public boolean checkNetworkState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public void openPrivacyPolicy(){
        startActivity(new Intent(getApplicationContext(), ViewPrivacyPolicy.class));
        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
//        FirebaseDatabase.getInstance().getReference().child("Links").child("PrivacyPolicy").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String url = String.valueOf(dataSnapshot.getValue());
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(url));
//                startActivity(i);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

//    private void updateProfilePicture(final Uri uri){
//        StorageReference filepath = FirebaseStorage.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("image")
//                .child(uri.getLastPathSegment());
//        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("profile_picture")
//                            .setValue(String.valueOf(uri));
//                }
//            }
//        });
//    }

}