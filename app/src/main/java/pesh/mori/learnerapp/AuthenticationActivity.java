package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView txtPassword;
    private Button btnSubmit;
    private String incomingIntent;
    private ProgressDialog mProgress;

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
        setContentView(R.layout.activity_authentication);

        mProgress = new ProgressDialog(this);

        incomingIntent = getIntent().getExtras().getString("incomingIntent");

        txtPassword = findViewById(R.id.txt_auth_password);
        btnSubmit = findViewById(R.id.btn_auth_submit);

        mAuth = FirebaseAuth.getInstance();
        checkAuthState();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(txtPassword.getText().toString())){
                    Toast.makeText(AuthenticationActivity.this, getString(R.string.hint_enter_password), Toast.LENGTH_SHORT).show();
                } else {
                    performAuthentication();
                }
            }
        });

    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(AuthenticationActivity.this,SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }

    public void sortIntents(){
        if (incomingIntent.equals("changeEmailIntent")){
            Intent changeEmailIntent = new Intent(AuthenticationActivity.this,ChangeEmailActivity.class);
            mProgress.dismiss();
            startActivity(changeEmailIntent);
            finish();
        }
        if (incomingIntent.equals("changePasswordIntent")){
            Intent changePasswordIntent = new Intent(AuthenticationActivity.this,ChangePasswordActivity.class);
            mProgress.dismiss();
            startActivity(changePasswordIntent);
            finish();
        }
        if (incomingIntent.equals("changePhoneIntent")){
            Intent changePhoneIntent = new Intent(AuthenticationActivity.this,ChangePhoneActivity.class);
            TemporaryPermissions.removeVerificationValue(AuthenticationActivity.this);
            TemporaryPermissions.removeVerificationCode(AuthenticationActivity.this);
            mProgress.dismiss();
            startActivity(changePhoneIntent);
            finish();
        }
        if (incomingIntent.equals("changeBioIntent")){
            Intent changeBioIntent = new Intent(AuthenticationActivity.this,BioActivity.class);
            mProgress.dismiss();
            startActivity(changeBioIntent);
            finish();
        }
    }

    public void performAuthentication(){
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.show();
        String email = mAuth.getCurrentUser().getEmail();
        String password = txtPassword.getText().toString();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sortIntents();
                } else {
                    Toast.makeText(AuthenticationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    mProgress.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
    }

}
