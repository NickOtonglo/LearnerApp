package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by MORIAMA on 12/12/2017.
 */

public class ReportPostActivity extends AppCompatActivity {

    private final AppCompatActivity activity = ReportPostActivity.this;


    private TextInputLayout textInputLayoutSchool;
    private TextInputEditText textInputEditTextSchool;
    private Button submit;
    private DatabaseReference mReports;
    private TextInputLayout textInputLayoutName;
    private EditText editText;
    private InputValidation inputValidation;
    private ProgressDialog mProgress;

    private RadioButton radioAdult,radioIllegal,radioAbusive,radioMalicious,radioCopyright,radioOther;

    private FirebaseAuth mAuth;
    Calendar calendar;
    private SimpleDateFormat sdf;

    private String criteriaRep="",postAuthor="",postTitle="",postKey="",category="",incomingIntent="",postType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView( R.layout.activity_report_post);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        editText = findViewById(R.id.edtInput);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        postType = getIntent().getExtras().getString("postType");
        postKey = getIntent().getExtras().getString("post_key");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            category = getString(R.string.firebase_ref_posts_type_1);
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            category = getString(R.string.firebase_ref_posts_type_2);
        }

        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(this);

        radioAbusive = findViewById(R.id.abusive_content);
        radioIllegal = findViewById(R.id.illegal_content);
        radioAdult = findViewById(R.id.adult_content);
        radioCopyright = findViewById(R.id.copyright_infringement);
        radioMalicious = findViewById(R.id.malicious_content);
        radioOther = findViewById(R.id.other_content);
        submit = (Button) findViewById(R.id.report);

        mReports = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_reported)).child(category).child(postKey).child(mAuth.getCurrentUser().getUid());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (!radioAbusive.isChecked() && !radioIllegal.isChecked() && !radioAdult.isChecked() && !radioCopyright.isChecked()
                    && !radioMalicious.isChecked() & !radioOther.isChecked()){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_kindly_select_a_criterion,Snackbar.LENGTH_LONG).show();
            } else {
                reportContent();
            }

            }
        });
    }

    private void  reportContent(){
        if (radioOther.isChecked()){
            criteriaRep = getString(R.string.radio_other);
        }
        if (radioIllegal.isChecked()){
            criteriaRep = getString(R.string.radio_illegal_content);
        }
        if (radioAbusive.isChecked()){
            criteriaRep = getString(R.string.radio_abusive_content);
        }
        if (radioMalicious.isChecked()){
            criteriaRep = getString(R.string.radio_malicious_content);
        }
        if (radioCopyright.isChecked()){
            criteriaRep = getString(R.string.radio_copyright_infringement);
        }
        if (radioAdult.isChecked()){
            criteriaRep = getString(R.string.radio_adult_content);
        }

        mProgress.setMessage(getString(R.string.info_submitting_report));
        mProgress.setCanceledOnTouchOutside(false);
        String description = editText.getText().toString().trim();
        if (description.isEmpty() || description.equals("") || TextUtils.isEmpty(description)){
            description = "";
        }
        final String timestamp = sdf.format(calendar.getTime());

        if (description.isEmpty() && radioOther.isChecked()){
            Snackbar.make(findViewById(android.R.id.content), R.string.info_kindly_give_us_more_information,Snackbar.LENGTH_LONG).show();
        } else {
            mProgress.show();
//            final DatabaseReference newNote = mNotes.child(sdf.format(Calendar.getInstance().getTime())).push();
            final DatabaseReference newReport = mReports.push();
            final String finalDescription = description;
            FirebaseDatabase.getInstance().getReference().child(category).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newReport.child("criterion").setValue(criteriaRep);
                    newReport.child("post_title").setValue(dataSnapshot.child("title").getValue());
                    newReport.child("author").setValue(dataSnapshot.child("author").getValue());
                    newReport.child("item_category").setValue(category);
                    newReport.child("timestamp").setValue(timestamp);
                    newReport.child("submitter").setValue(mAuth.getCurrentUser().getUid());
                    newReport.child("descrtiption").setValue(finalDescription);
                    newReport.child("item_id").setValue(postKey);
                    mProgress.dismiss();
                    Toast.makeText(ReportPostActivity.this, R.string.info_post_reported, Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ReportPostActivity.this, getString(R.string.error_general)+" "+databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
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
        super.onBackPressed();
        finish();
    }
}
