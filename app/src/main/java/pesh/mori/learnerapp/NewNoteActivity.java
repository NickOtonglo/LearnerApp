package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NewNoteActivity extends AppCompatActivity {
    private EditText fileTitle,fileDescription;
    private Button btnPost;
    private DatabaseReference mNotes;
    private FirebaseAuth mAuth;

    private ProgressDialog mProgress;
    Calendar calendar;
    private SimpleDateFormat sdf,sdf_full;

    private String postKey="",incomingIntent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//        setSupportActionBar(toolbar);
//
//        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        postKey = getIntent().getExtras().getString("post_key");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        mProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());

        fileTitle = findViewById(R.id.txt_note_title);
        fileDescription = findViewById(R.id.txt_note_description);
        fileDescription.setMovementMethod(new ScrollingMovementMethod());
        btnPost = findViewById(R.id.btn_post_note);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        mProgress.setMessage("Saving note...");
        mProgress.setCanceledOnTouchOutside(false);
        final String title = fileTitle.getText().toString().trim();
        final String description = fileDescription.getText().toString().trim();
        final String time = sdf_full.format(Calendar.getInstance().getTime());
        if (title.isEmpty() || description.isEmpty()){
            Snackbar.make(findViewById(android.R.id.content),"One or more required field(s) is empty",Snackbar.LENGTH_LONG).show();
        } else {
            mProgress.show();
//            final DatabaseReference newNote = mNotes.child(sdf.format(Calendar.getInstance().getTime())).push();
            final DatabaseReference newNote = mNotes.push();
            mNotes.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newNote.child("title").setValue(title);
                    newNote.child("description").setValue(description);
                    newNote.child("timestamp").setValue(time);
                    newNote.child("author").setValue(mAuth.getCurrentUser().getUid());
                    newNote.child("linkedTo").setValue(postKey);
                    mProgress.dismiss();
                    Toast.makeText(NewNoteActivity.this, "Note saved", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(NewNoteActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.static_animation,R.anim.slide_in_from_top);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
}
