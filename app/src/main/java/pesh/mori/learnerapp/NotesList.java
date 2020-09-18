package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotesList extends AppCompatActivity {
    private RecyclerView mRecycler;
    private DatabaseReference mNotes;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;

    private String postKey="",incomingIntent="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView(R.layout.activity_notes_list);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postKey = getIntent().getExtras().getString("post_key");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        mAuth = FirebaseAuth.getInstance();
        txtEmpty = findViewById(R.id.txt_notes_empty);

        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());
        mNotes.keepSynced(true);
//        mNotes.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
//                    Toast.makeText(NotesList.this, dataSnapshot1.child("linkedTo").getValue().toString(), Toast.LENGTH_SHORT).show();
//                    if (!dataSnapshot1.child("linkedTo").equals(postKey)){
//                        txtEmpty.setText("No notes found");
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
        mNotes.orderByChild("linkedTo").equalTo(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText(getString(R.string.info_no_notes_found));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = findViewById(R.id.layout_recycler_notes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (postKey.equals("")){
            fetchAllUserNotes();
        } else {
            FirebaseRecyclerAdapter<Note, MyFilesActivity_NotesFragment.NotesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Note, MyFilesActivity_NotesFragment.NotesViewHolder>(
                    Note.class,
                    R.layout.card_notes,
                    MyFilesActivity_NotesFragment.NotesViewHolder.class,
                    mNotes.orderByChild("linkedTo").equalTo(postKey)
            ) {
                @Override
                protected void populateViewHolder(final MyFilesActivity_NotesFragment.NotesViewHolder viewHolder, Note model, int position) {
                    final String noteKey = getRef(position).getKey();

                    mNotes.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.getChildrenCount()>0){
                                    txtEmpty.setVisibility(View.GONE);
                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(noteKey).child("title").getValue()));
                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(noteKey).child("timestamp").getValue()));
                                    viewHolder.setDescription(String.valueOf(dataSnapshot.child(noteKey).child("description").getValue()));
                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent viewNoteIntent = new Intent(getApplicationContext(),ViewNoteActivity.class);
                                            viewNoteIntent.putExtra("note_key",noteKey);
                                            startActivity(viewNoteIntent);
                                        }
                                    });
                                } else {
                                    viewHolder.setNull();
                                }
                            } else if (!dataSnapshot.exists()){
                                viewHolder.setNull();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
            mRecycler.setAdapter(firebaseRecyclerAdapter);
        }
    }

    private void fetchAllUserNotes() {
        FirebaseRecyclerAdapter<Note, MyFilesActivity_NotesFragment.NotesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Note, MyFilesActivity_NotesFragment.NotesViewHolder>(
                Note.class,
                R.layout.card_notes,
                MyFilesActivity_NotesFragment.NotesViewHolder.class,
                mNotes.orderByChild("timestamp")
        ) {
            @Override
            protected void populateViewHolder(final MyFilesActivity_NotesFragment.NotesViewHolder viewHolder, Note model, int position) {
                final String noteKey = getRef(position).getKey();

                mNotes.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.getChildrenCount()>0){
                                txtEmpty.setVisibility(View.GONE);
                                viewHolder.setTitle(String.valueOf(dataSnapshot.child(noteKey).child("title").getValue()));
                                viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(noteKey).child("timestamp").getValue()));
                                viewHolder.setDescription(String.valueOf(dataSnapshot.child(noteKey).child("description").getValue()));
                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent viewNoteIntent = new Intent(getApplicationContext(),ViewNoteActivity.class);
                                        viewNoteIntent.putExtra("note_key",noteKey);
                                        startActivity(viewNoteIntent);
                                    }
                                });
                            } else {
                                viewHolder.setNull();
                            }
                        } else if (!dataSnapshot.exists()){
                            viewHolder.setNull();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
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
