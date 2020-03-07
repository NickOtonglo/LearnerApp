package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.IntentCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText email,password;
    private Button sign_in_register;
    private Button signup;
    private Button fp;
    private TextInputLayout email2;
    private TextInputLayout password2;

    private RequestQueue requestQueue;
    private static final String URL = "http://moripesh.com/user_control.php";
    private StringRequest request;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent loginIntent = new Intent(LoginActivity.this,HomeActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }

        email = (TextInputEditText) findViewById(R.id.txtLoginEmail);
        password = (TextInputEditText) findViewById(R.id.txtLoginPassword);

        email2 = (TextInputLayout) findViewById( R.id.txtLoginEmailLabel );
        password2 = (TextInputLayout) findViewById( R.id.txtLoginPasswordLabel);


        final Animation myanim3 = AnimationUtils.loadAnimation(this, R.anim.bounce6);
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        final Animation myanim2 = AnimationUtils.loadAnimation(this, R.anim.bounce7);



        sign_in_register = (Button) findViewById(R.id.btnLogin);

        signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup.startAnimation(myanim);
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
            }
        });
        fp = (Button) findViewById(R.id.fp);

        fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fp.startAnimation(myanim);
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
            }
        });


        requestQueue = Volley.newRequestQueue(this);

        final ProgressDialog[] progress = new ProgressDialog[1];


        sign_in_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sign_in_register.startAnimation( myanim );
                startLogin();
            }
        });
    }

    private void startLogin() {
        String emailLogin = email.getText().toString().trim();
        String passwordLogin = password.getText().toString();

        if (emailLogin.isEmpty()){
            email.setError("Enter your email");
        }
        if (passwordLogin.isEmpty()){
            password.setError("Enter your password");
        }
        if (!emailLogin.isEmpty() && !passwordLogin.isEmpty()){
//            if(!((LoginActivity) getApplicationContext()).isFinishing())
//            {
//                //show dialog
//            }
            mProgress.setMessage("Please wait...");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(emailLogin,passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()){
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            mProgress.dismiss();
                            mAlert.setTitle(R.string.error_general)
                                    .setMessage("Login failed: "+task.getException().getMessage())
                                    .show();
                        }
                    } else {
                        Intent homeIntent = new Intent(LoginActivity.this,HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent selectLoginIntent = new Intent(LoginActivity.this,SelectLoginActivity.class);
        selectLoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selectLoginIntent);
        finish();
    }
}