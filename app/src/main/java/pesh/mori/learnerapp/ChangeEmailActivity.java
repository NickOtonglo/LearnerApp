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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by MORIAMA on 28/12/2017.
 */

public class ChangeEmailActivity extends AppCompatActivity{

    private final AppCompatActivity activity = ChangeEmailActivity.this;

    private InputValidation inputValidation;
    private Button edit;
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText textInputEditTextEmail;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mUsers,mFiles,mNotes;
    private ProgressDialog mProgress;

    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_changemail );
//        Toast.makeText(getApplicationContext(), "INFO | Under Development", Toast.LENGTH_SHORT).show();

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mProgress = new ProgressDialog (this);

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUsers.keepSynced(true);
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mFiles.keepSynced(true);
        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());
        mNotes.keepSynced(true);

        checkAuthState();
        initViews();
        initObjects();
        emptyInputEditText();
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        edit = (Button) findViewById(R.id.btn_update_email);
        edit.startAnimation( myanim );
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(ChangeEmailActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    private void initViews(){


        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayoutchangeemail);

        textInputEditTextEmail = (TextInputEditText) findViewById(R.id.textInputEditTextchangeemail);

    }
    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }
    private void emptyInputEditText(){
        textInputEditTextEmail.setText(null);
    }
    public void doThis(View view) {
        final String email = textInputEditTextEmail.getText().toString().trim();
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        edit = (Button) findViewById(R.id.btn_update_email);
        edit.startAnimation( myanim );
        if (!inputValidation.isInputEditTextFilled( textInputEditTextEmail, textInputLayoutEmail, getString( R.string.error_message_email ) )) {
            return;
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "Are you sure you want to change your email address?" );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.setMessage("Updating email...");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        mUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    updateDatabaseEmails();
                                } else {
                                    Toast.makeText(ChangeEmailActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } );
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                Toast.makeText(getApplicationContext(), "INFO | Update Cancelled", Toast.LENGTH_SHORT).show();

                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void updateDatabaseEmails() {
        final String email = textInputEditTextEmail.getText().toString().trim();;
        mUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mUsers.child("email").setValue(email);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChangeEmailActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String file_key = ds.getKey();
                        mFiles.child(file_key).child("author").setValue(email);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChangeEmailActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        mNotes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                        String note_key = ds.getKey();
                        mNotes.child(note_key).child("author").setValue(email);
                        Toast.makeText(getApplicationContext(), "Update successful", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChangeEmailActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        mProgress.dismiss();
    }

    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
