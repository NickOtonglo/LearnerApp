package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by MORIAMA on 28/12/2017.
 */

public class ChangeEmailActivity extends AppCompatActivity {
    private Button btnSubmit;
    private TextInputEditText txtEmail,txtConfirmEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mUsers;
    private ProgressDialog mProgress;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView(R.layout.activity_changemail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mProgress = new ProgressDialog(this);

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUsers.keepSynced(true);

        checkAuthState();
        txtEmail = (TextInputEditText) findViewById(R.id.txt_email);
        txtConfirmEmail = findViewById(R.id.txt_confirm_email);
        btnSubmit = (Button) findViewById(R.id.btn_update_email);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
            }
        });
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(ChangeEmailActivity.this,SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }

    public void updateEmail() {
        final String email = txtEmail.getText().toString().trim();

        btnSubmit = (Button) findViewById(R.id.btn_update_email);

        if (txtEmail.getText().toString().trim().equals("")) {
            txtEmail.setError(getString(R.string.hint_enter_your_email_address));
            return;
        }

        if (txtConfirmEmail.getText().toString().trim().equals("")) {
            txtConfirmEmail.setError(getString(R.string.hint_reenter_your_new_email_address));
            return;
        }

        if (!txtConfirmEmail.getText().toString().trim().equals(txtEmail.getText().toString().trim())) {
            txtConfirmEmail.setError(getString(R.string.info_kindly_ensure_email_addresses_match));
            return;
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder1.setMessage(getString(R.string.confirm_are_you_sure_you_want_to_change_your_email_address));
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                getString(R.string.option_alert_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.setMessage(getString(R.string.info_please_wait));
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        mUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    updateDatabaseEmails();
                                } else {
                                    Toast.makeText(ChangeEmailActivity.this, getString(R.string.error_general)+" "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    mProgress.dismiss();
                                }
                            }
                        });
                    }
                } );
        builder1.setNegativeButton(R.string.option_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                Toast.makeText(getApplicationContext(), "INFO | Update Cancelled", Toast.LENGTH_SHORT).show();

                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void updateDatabaseEmails() {
        final String email = txtEmail.getText().toString().trim();;
        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mUsers.child("email").setValue(email);
                    Toast.makeText(getApplicationContext(), R.string.info_email_address_changed, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChangeEmailActivity.this, getString(R.string.error_occurred_try_again), Toast.LENGTH_SHORT).show();
            }
        });
        mProgress.dismiss();
    }

    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
