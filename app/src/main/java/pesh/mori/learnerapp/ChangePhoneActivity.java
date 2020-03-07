package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rilixtech.CountryCodePicker;

/**
 * Created by MORIAMA on 28/12/2017.
 */

public class ChangePhoneActivity extends AppCompatActivity {

    private final AppCompatActivity activity = ChangePhoneActivity.this;

    private InputValidation inputValidation;
    private Button btnUpdate;
    private TextInputLayout textInputLayoutPhone;
    private TextInputEditText txtPhoneNumber;
    private TextInputLayout textInputLayoutCode;
    private TextInputEditText txtVerificationCode;

    private Button btnVerify;

    private FirebaseAuth mAuth;
    private CountryCodePicker ccp;;
    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;

    private String incomingIntent="";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_changephone );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog (this);
        mAlert = new AlertDialog.Builder(this);
        inputValidation = new InputValidation(activity);

        if (getIntent().hasExtra("incomingIntent")){
            incomingIntent = getIntent().getExtras().getString("incomingIntent");
        }

        checkAuthState();

        ccp = findViewById(R.id.ccp);
        txtPhoneNumber = findViewById(R.id.txt_phone_number);
        txtPhoneNumber.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(10) });
        txtVerificationCode = findViewById(R.id.txt_verification_code);

        textInputLayoutPhone = (TextInputLayout) findViewById(R.id.textInputLayoutUpdatePhone);
        textInputLayoutCode = (TextInputLayout) findViewById(R.id.textInputLayoutcode);

        btnUpdate = findViewById(R.id.btn_update);
        btnVerify = findViewById(R.id.btn_verify);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtPhoneNumber.getText().toString().trim().length()<9 || txtPhoneNumber.getText().toString().trim().length()>10){
                    txtPhoneNumber.setError("Invalid phone number");
                } else
                verifyPhoneNumber();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhoneNumber();
            }
        });
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(ChangePhoneActivity.this,SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    public void verifyPhoneNumber(){
        if (!inputValidation.isInputEditTextFilled(txtPhoneNumber, textInputLayoutPhone, getString( R.string.error_message_phone ) )) {
            return;
        }

        mAlert.setMessage(getText(R.string.info_new_phone_alert_message_1)+" ("+txtPhoneNumber.getText().toString()+")." +
                "\n"+getText(R.string.info_new_phone_alert_message_2)+"." +
                "\n"+getText(R.string.info_new_phone_alert_message_3)+".")
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        String phone = ccp.getFullNumberWithPlus()+txtPhoneNumber.getText().toString().trim();
        final String phoneNumber = ccp.getFullNumberWithPlus()+phone.substring(phone.length() - 9);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        final DatabaseReference mQuery = mDatabase;
        mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("LOG_phone",".."+phoneNumber+".. -- .."+dataSnapshot.child("phone").getValue()+"..");
                if (dataSnapshot.child("phone").getValue().equals(phoneNumber)){
                    if (dataSnapshot.child("phone_verified").exists() && dataSnapshot.child("phone_verified").getValue().equals("true")){
//                        Log.d("LOG_phone_verified","true");
                        Toast.makeText(ChangePhoneActivity.this, "This number is already verified", Toast.LENGTH_LONG).show();
                    } else {
//                        Log.d("LOG_phone_verified","false");
                        mAlert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(getApplicationContext(),PhoneVerificationActivity.class);
                                intent.putExtra("verified","false");
                                intent.putExtra("phone",phoneNumber.substring(phoneNumber.length() - 9));
                                startActivity(intent);
                            }
                        })
                                .show();

                    }
                } else {
                    mQuery.orderByChild("phone").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
//                                Toast.makeText(ChangePhoneActivity.this, "This phone number is already in use.\nContact support to resolve any conflicts.", Toast.LENGTH_SHORT).show();
                            } else {
                                mAlert.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                TemporaryPermissions.removeVerificationValue(ChangePhoneActivity.this);
                                                Intent intent = new Intent(getApplicationContext(),PhoneVerificationActivity.class);
                                                intent.putExtra("verified","false");
                                                intent.putExtra("phone",phoneNumber);
                                                startActivity(intent);
                                            }
                                        })
                                        .show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
//        builder1.setMessage( "Verification Code has been sent to the above phone number. This code expires in 5 minutes." );
//        builder1.setCancelable( false );
//
//        builder1.setPositiveButton(
//                "Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                } );
//
//        AlertDialog alert1 = builder1.create();
//        alert1.show();
    }

    public void savePhoneNumber(){
//        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
//        btnUpdate.startAnimation( myanim );
        final String phone = txtPhoneNumber.getText().toString().trim();
        if (!inputValidation.isInputEditTextFilled(txtPhoneNumber, textInputLayoutPhone, getString( R.string.error_message_phone ) )) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(txtVerificationCode, textInputLayoutCode, getString( R.string.error_message_code ))
                && !TemporaryPermissions.isUserVerified(ChangePhoneActivity.this)) {
            return;
        }
        if (!phone.isEmpty() && (phone.length()<9 || phone.length()>10)){
            txtPhoneNumber.setError("Invalid phone number");
            return;
        }
        final String phoneNumber = ccp.getFullNumberWithPlus()+phone.substring(phone.length() - 9);

        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "Are you sure you want to update your phone number?" );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.setMessage("Updating phone number...");
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        if (TemporaryPermissions.isUserVerified(ChangePhoneActivity.this)){
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                            mDatabase.child("phone_verified").setValue("true");
                            mDatabase.child("phone").setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        TemporaryPermissions.removeVerificationValue(ChangePhoneActivity.this);
                                        Toast.makeText(getApplicationContext(), "Phone number updated", Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                        if (incomingIntent.equals("HomeActivity")){
                                            startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(ChangePhoneActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        mProgress.dismiss();
                                    }
                                }
                            });
                        } else {
//                            Toast.makeText(ChangePhoneActivity.this, TemporaryPermissions.showVerificationCode(ChangePhoneActivity.this), Toast.LENGTH_LONG).show();
                            if (!txtVerificationCode.getText().toString().trim().equals(TemporaryPermissions.showVerificationCode(ChangePhoneActivity.this))){
                                Toast.makeText(ChangePhoneActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            } else if (txtVerificationCode.getText().toString().trim().equals(TemporaryPermissions.showVerificationCode(ChangePhoneActivity.this))){
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                                mDatabase.child("phone_verified").setValue("true");
                                mDatabase.child("phone").setValue(phoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            TemporaryPermissions.removeVerificationCode(ChangePhoneActivity.this);
                                            Toast.makeText(getApplicationContext(), "Phone number updated", Toast.LENGTH_SHORT).show();
                                            mProgress.dismiss();
                                            if (incomingIntent.equals("HomeActivity")){
                                                startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(ChangePhoneActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            mProgress.dismiss();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ChangePhoneActivity.this, "The phone number is not verified", Toast.LENGTH_LONG).show();
                                mProgress.dismiss();
                            }
                        }
                        dialog.cancel();
                    }
                });
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert1 = builder1.create();
        alert1.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TemporaryPermissions.isUserVerified(ChangePhoneActivity.this)){
            textInputLayoutCode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
