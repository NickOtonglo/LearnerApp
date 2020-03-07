package pesh.mori.learnerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;

/**
 * Created by MORIAMA on 13/12/2017.
 */

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnSubmit;
    private TextInputLayout txtEmailLabel;
    private TextInputEditText txtEmail;
    private FirebaseAuth mAuth;
    private AlertDialog.Builder mAlert;
    private ProgressBar mProgressBar;
    private LinearLayoutCompat layoutMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_forgotpassword );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        if (!checkNetworkState()){
            Snackbar.make(findViewById(android.R.id.content),R.string.error_no_internet_connectivity,Snackbar.LENGTH_LONG).show();
        }

        mAlert = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = findViewById(R.id.progress_bar);

        if (mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
            finish();
        }

        layoutMain = findViewById(R.id.layout_main);
        txtEmailLabel = (TextInputLayout) findViewById(R.id.txt_email_label);
        txtEmail = (TextInputEditText) findViewById(R.id.txt_email);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitEmail();
            }
        });

    }

    private void submitEmail() {
        layoutMain.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
        final String email = txtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)){
            txtEmail.setError("Required");
            layoutMain.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("EMAIL STATUS", "Email sent.");
                                mAlert.setTitle("Reset link generated")
                                        .setMessage("We have sent you reset link to the email address you submitted. Check your inbox and follow the link to reset your password." +
                                                "\nThe link expires in 10 minutes. In case it expires, repeat the process to reset your password.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                startActivity(new Intent(getApplicationContext(),SelectLoginActivity.class));
                                            }
                                        })
                                        .show();
                                layoutMain.setEnabled(true);
                                mProgressBar.setVisibility(View.GONE);
                            }  else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthEmailException e) {
                                    e.printStackTrace();
                                    mAlert.setTitle(R.string.error_general)
                                            .setMessage(task.getException().getMessage())
                                            .show();
                                    layoutMain.setEnabled(true);
                                    mProgressBar.setVisibility(View.GONE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mAlert.setTitle(R.string.error_general)
                                            .setMessage(task.getException().getMessage())
                                            .show();
                                    layoutMain.setEnabled(true);
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
        }
    }

    public void enableDisableViews(){
        if (layoutMain.isEnabled()){
            layoutMain.setEnabled(false);
            mProgressBar.setVisibility(View.VISIBLE);
        }
        if (!layoutMain.isEnabled()){
            layoutMain.setEnabled(true);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public boolean checkNetworkState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}
