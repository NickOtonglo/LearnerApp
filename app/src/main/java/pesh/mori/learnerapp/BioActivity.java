package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.IntentCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by MORIAMA on 08/02/2018.
 */

public class BioActivity extends AppCompatActivity {

    private TextView txtAbout,txtSkills,txtFacebook,txtTwitter,txtLinkedIn;
//    private Spinner spinnerSkills;
    private RadioButton radioFemale,radioMale;

    private Button btnSubmit;
    private String spinnerItem;
    private FirebaseAuth mAuth;
    private DatabaseReference mBio;
    private ProgressDialog mProgress;
    private Spinner spinner;
    private ArrayAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_bio );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        checkAuthState();
        mProgress = new ProgressDialog(this);

        txtAbout = findViewById(R.id.txt_bio_about);
        txtAbout.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(600) });
        txtSkills = findViewById(R.id.txt_bio_skills);
        txtSkills.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(600) });
        txtFacebook = findViewById(R.id.txt_bio_facebook);
        txtTwitter = findViewById(R.id.txt_bio_twitter);
        txtLinkedIn = findViewById(R.id.txt_bio_linkedin);

        radioFemale = findViewById(R.id.radio_bio_female);
        radioMale = findViewById(R.id.radio_bio_male);

        preloadBio();

        final Animation myanim = AnimationUtils.loadAnimation( this, R.anim.bounce3 );
        btnSubmit = (Button) findViewById( R.id.btn_bio_submit );
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnSubmit.startAnimation( myanim );
                saveBio();
            }
        });

        spinner = (Spinner) findViewById( R.id.spinner_bio_skills);
        adapter = ArrayAdapter.createFromResource( this,
                R.array.list_occupation, R.layout.spinner_item2 );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinner.setAdapter( adapter );
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(BioActivity.this,LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    public void preloadBio(){
        mBio = FirebaseDatabase.getInstance().getReference().child("Bio").child(mAuth.getCurrentUser().getUid());
        mBio.keepSynced(true);
        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    txtAbout.setText(String.valueOf(dataSnapshot.child("about").getValue()));
                    txtFacebook.setText(String.valueOf(dataSnapshot.child("facebook").getValue()));
                    if (String.valueOf(dataSnapshot.child("gender").getValue()).equals("male")){
                        radioMale.setChecked(true);
                    } else if (String.valueOf(dataSnapshot.child("gender").getValue()).equals("female")){
                        radioFemale.setChecked(true);
                    }
                    txtLinkedIn.setText(String.valueOf(dataSnapshot.child("linkedin").getValue()));
                    txtSkills.setText(String.valueOf(dataSnapshot.child("skill_details").getValue()));
                    txtTwitter.setText(String.valueOf(dataSnapshot.child("twitter").getValue()));
                    spinner.setSelection(adapter.getPosition(String.valueOf(dataSnapshot.child("skills_sector").getValue())));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void saveBio(){
        final Animation myanim = AnimationUtils.loadAnimation( this, R.anim.bounce3 );
        btnSubmit = (Button) findViewById( R.id.btn_bio_submit );
        btnSubmit.startAnimation( myanim );
        mProgress.setMessage("Updating bio...");
        mProgress.setCanceledOnTouchOutside(false);

        spinnerItem = spinner.getSelectedItem().toString();
        final String about = txtAbout.getText().toString().trim();
        final String skills = txtSkills.getText().toString().trim();
        final String facebook = txtFacebook.getText().toString().trim();
        final String twitter = txtTwitter.getText().toString().trim();
        final String linkedin = txtLinkedIn.getText().toString().trim();
        final String spinner_item = spinnerItem.trim();
        String gender = "not specified";
        if (radioMale.isChecked()){
            gender = "male";
        } else if (radioFemale.isChecked()){
            gender = "female";
        }

        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "Are sure you want to update your L'earnerApp Bio with new information?" );
        builder1.setCancelable( false );

        final String finalGender = gender;
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.show();
                        mBio = FirebaseDatabase.getInstance().getReference().child("Bio").child(mAuth.getCurrentUser().getUid());
                        mBio.keepSynced(true);
                        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mBio.child("about").setValue(about);
                                mBio.child("skill_details").setValue(skills);
                                mBio.child("skills_sector").setValue(spinner_item);
                                mBio.child("facebook").setValue(facebook);
                                mBio.child("twitter").setValue(twitter);
                                mBio.child("linkedin").setValue(linkedin);
                                mBio.child("gender").setValue(finalGender);
                                mProgress.dismiss();
                                Toast.makeText(getApplicationContext(), "Bio updated", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(BioActivity.this, "An error occured. Please try again.", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
