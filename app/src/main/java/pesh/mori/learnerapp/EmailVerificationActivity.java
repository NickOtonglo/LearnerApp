package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class EmailVerificationActivity extends AppCompatActivity {

    private TextView txtMessage,txtResendLink;
    private Button btnResendLink;
    private FirebaseAuth mAuth;
    private String incomingIntent="";
    private ProgressBar progressBar;

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
        setContentView(R.layout.activity_email_verification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

//        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        mAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.progress_bar);
        txtMessage = findViewById(R.id.txt_message);
        txtResendLink = findViewById(R.id.txt_resend_link);
        btnResendLink = findViewById(R.id.btn_resend_link);

        checkVerificationStatus();

        btnResendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                txtMessage.setText(R.string.info_resending_verification_link_please_wait);
                txtResendLink.setVisibility(View.GONE);
                sendVerificationEmail();
            }
        });
    }

    private void checkVerificationStatus() {
        if (mAuth.getCurrentUser().isEmailVerified()){
            progressBar.setVisibility(View.GONE);
            txtMessage.setText(R.string.info_your_email_address_is_already_verified_no_further_action_is_required);
        } else {
            sendVerificationEmail();
        }
    }

    public void sendVerificationEmail(){
        mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                txtMessage.setText(R.string.info_sending_verification_email_please_wait);
                txtResendLink.setVisibility(View.VISIBLE);
                btnResendLink.setVisibility(View.VISIBLE);
                mAuth.getCurrentUser().reload();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                txtMessage.setText(R.string.error_error_sending_verification_email_retry);
                btnResendLink.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
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
//        super.onBackPressed();
        mAuth.signOut();
        Intent loginIntent = new Intent(EmailVerificationActivity.this, SelectLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
