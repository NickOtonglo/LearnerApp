package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class ViewNoteActivity extends AppCompatActivity {

    private TextView txtTitle,txtDescription,txtTime;

    private String noteKey;
    private DatabaseReference mNotes;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn;
    private Button btnDelete;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        noteKey = getIntent().getExtras().getString("note_key");

        mAuth = FirebaseAuth.getInstance();

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        layoutBtn = findViewById(R.id.layout_view_note_btn);
        layoutBtn = findViewById(R.id.layout_view_note_btn);
        txtTitle = findViewById(R.id.txt_view_note_note_title);
        txtDescription = findViewById(R.id.txt_view_note_note_description);
        txtTime = findViewById(R.id.txt_view_note_note_time);

        FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (String.valueOf(dataSnapshot.child(noteKey).child("author").getValue()).equals(mAuth.getCurrentUser().getEmail())){
                    layoutBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        loadNoteDetails();

    }

    private void loadNoteDetails() {
        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid()).child(noteKey);
        mNotes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removeNote(){
        mAlert.setTitle("Delete note")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mProgress.setMessage("Deleting note...");
                        mProgress.setCancelable(false);
                        mProgress.show();
                        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid()).child(noteKey);
                        mNotes.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
//                if (file_type.equals("image")){
//
//                }
                                mNotes.removeValue();
                                mProgress.dismiss();
                                Toast.makeText(ViewNoteActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                mProgress.dismiss();
                                Toast.makeText(ViewNoteActivity.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    public void goToPost(){
        mNotes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String postKey = String .valueOf(dataSnapshot.child("linkedTo").getValue());
                FirebaseDatabase.getInstance().getReference().child("AllPosts").child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            Toast.makeText(ViewNoteActivity.this, R.string.info_post_not_available, Toast.LENGTH_SHORT).show();
                        } else {
                            final String category = String.valueOf(dataSnapshot.child("Category").getValue());
                            FirebaseDatabase.getInstance().getReference().child(category).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()){
                                        Toast.makeText(ViewNoteActivity.this, R.string.info_post_not_available, Toast.LENGTH_SHORT).show();
                                    } else {
                                        checkOwnershipStatus(postKey,category);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkOwnershipStatus(final String key,final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                Double price = Double.parseDouble(itemPrice);
                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","ViewFileActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    startActivity(transactionsIntent);
                    overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    if (category.equals("Files")){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                    if (category.equals("DIY")){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    if (category.equals("Files")){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                    if (category.equals("DIY")){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_note, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_delete_post:
                removeNote();
                return true;
            case R.id.menu_source:
                goToPost();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
