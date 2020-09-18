package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private DatabaseReference mBio,mLists;
    private ProgressDialog mProgress;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private AlertDialog.Builder mAlert;
    private String sector ="";
    private List<String> sectorList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView( R.layout.activity_bio );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);

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

        btnSubmit = (Button) findViewById( R.id.btn_bio_submit );
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBio();
            }
        });

        spinner = (Spinner) findViewById( R.id.spinner_bio_skills);
        loadSpinners();
        preloadBio();
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(BioActivity.this,SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }

    public void preloadBio(){
        mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
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

    private void loadSpinners(){
        mProgress.setCancelable(false);
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.show();
        mLists = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists));
        mLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sector = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_2)).getValue());

                sectorList = Arrays.asList(sector.split("\\s*,\\s*"));

                //Tag
                final List<String> getTags = new ArrayList<>();
                final int facultySize = sectorList.size();
                for (int i=0;i<facultySize;i++){
                    Object object = sectorList.get(i);
                    getTags.add(object.toString().trim());
                }

                adapter = new ArrayAdapter<String>(BioActivity.this,android.R.layout.simple_spinner_item,getTags);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mProgress.dismiss();
    }

    public void saveBio(){
        btnSubmit = (Button) findViewById( R.id.btn_bio_submit );
        mProgress.setMessage(getString(R.string.info_please_wait));
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

        AlertDialog.Builder builder1 = new AlertDialog.Builder( this,R.style.AlertDialogStyle);
        builder1.setMessage(R.string.confirm_bio_update);
        builder1.setCancelable( false );

        final String finalGender = gender;
        builder1.setPositiveButton(
                getString(R.string.option_alert_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.show();
                        mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
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
                                Toast.makeText(getApplicationContext(), R.string.info_bio_updated, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(BioActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
                            }
                        });

                        dialog.cancel();
                    }
                } );

        builder1.setNegativeButton(getString(R.string.option_alert_no), new DialogInterface.OnClickListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bio, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.hide_bio:
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child("hidden").exists() && dataSnapshot.child("hidden").getValue().equals("true")){
                                    unhideBio();
                                } else if (dataSnapshot.exists()){
                                    hideBio();
                                } else if (!dataSnapshot.exists()){
                                    Toast.makeText(BioActivity.this, R.string.info_you_have_not_created_your_bio_yet, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                return true;
            case R.id.remove_bio:
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                removeBio();
                                else
                                    Toast.makeText(BioActivity.this, R.string.info_you_have_not_created_your_bio_yet, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeBio() {
        mAlert.setMessage(R.string.confirm_remove_bio)
                .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgress.setMessage(getString(R.string.info_please_wait));
                        mProgress.show();
                        DatabaseReference mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
                        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mBio.removeValue();
                                Toast.makeText(BioActivity.this, R.string.into_bio_removed, Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BioActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void hideBio() {
        mAlert.setMessage(R.string.confirm_hide_bio)
                .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgress.setMessage(getString(R.string.info_please_wait));
                        mProgress.show();
                        DatabaseReference mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
                        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mBio.child("hidden").setValue("true");
                                Toast.makeText(BioActivity.this, R.string.into_bio_hidden, Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BioActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    private void unhideBio(){
        mAlert.setMessage(R.string.confirm_bio_hidden_make_public)
                .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgress.setMessage(getString(R.string.info_please_wait));
                        mProgress.show();
                        DatabaseReference mBio = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(mAuth.getCurrentUser().getUid());
                        mBio.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mBio.child("hidden").removeValue();
                                Toast.makeText(BioActivity.this, R.string.into_bio_made_public, Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(BioActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
                                mProgress.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
