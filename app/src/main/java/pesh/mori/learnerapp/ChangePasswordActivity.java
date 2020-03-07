package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.IntentCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by MORIAMA on 28/12/2017.
 */

public class ChangePasswordActivity extends AppCompatActivity {

    private final AppCompatActivity activity = ChangePasswordActivity.this;

    private InputValidation inputValidation;
    private Button edit;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextPassword;
    private TextInputLayout textInputLayoutConfirm;
    private TextInputEditText textInputEditTextConfirm;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ProgressDialog mProgress;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.activity_changepassword );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);

        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        edit = (Button) findViewById(R.id.edit);
        edit.startAnimation( myanim );
        initViews();
        checkAuthState();
        initObjects();
        emptyInputEditText();
    }
    private void initViews(){


        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayoutpasswordnew);
        textInputLayoutConfirm = (TextInputLayout) findViewById(R.id.textInputLayoutconfirm);

        textInputEditTextPassword = (TextInputEditText) findViewById(R.id.textInputEditTextpasswordnew);
        textInputEditTextConfirm = (TextInputEditText) findViewById(R.id.textInputEditTextconfirm);
    }
    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }
    private void emptyInputEditText(){
        textInputEditTextPassword.setText(null);
        textInputEditTextConfirm.setText(null);
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(ChangePasswordActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    public void doThis(View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        edit = (Button) findViewById(R.id.edit);
        edit.startAnimation( myanim );
        final String password = textInputEditTextPassword.getText().toString();
        final String confirm_password = textInputEditTextConfirm.getText().toString();
        if (!inputValidation.isInputEditTextFilled( textInputEditTextPassword, textInputLayoutPassword, getString( R.string.error_message_password ) )) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled( textInputEditTextConfirm, textInputLayoutConfirm, getString( R.string.error_message_passwordconfirm ) )) {
            return;
        }
        if (!confirm_password.equals(password)){
            textInputEditTextConfirm.setError("Your two passwords do not match!");
            return;
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "Are you sure you want to change your password?" );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.setMessage("Changing password...");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        mUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Password changed", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        dialog.cancel();
                    }
                } );
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public boolean onSupportNavigateUp(){

        finish();
        return true;
    }


}
