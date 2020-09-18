package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by MORIAMA on 18/11/2017.
 */

public class MyFilesActivity_NotesFragment extends Fragment {
//    private FloatingActionButton fabNew;
    private com.github.clans.fab.FloatingActionButton fabNew;

    private RecyclerView mRecycler;

    private DatabaseReference mNotes;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;

    private ProgressDialog mProgress;

    private MyFilesActivity myFilesActivity;
    private AlertDialog.Builder mAlert;

    public MyFilesActivity_NotesFragment(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mynotes = inflater.inflate(R.layout.fragment_mynotes, container, false);

        myFilesActivity = new MyFilesActivity();
        mAlert = new AlertDialog.Builder(getActivity(),R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) mynotes.findViewById(R.id.txt_notes_empty);
//
//        fabNew = MyFilesActivity_NotesFragment.findViewById(R.id.fab_new_note);
//        fabNew.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getActivity(),NewNoteActivity.class));
//            }
//        });

        mNotes = FirebaseDatabase.getInstance().getReference().child("Notes").child(mAuth.getCurrentUser().getUid());
        mNotes.keepSynced(true);
        mNotes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText(R.string.info_no_notes_found);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)mynotes.findViewById(R.id.layout_recycler_mynotes);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        return mynotes;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        FirebaseRecyclerAdapter<Note, NotesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Note, NotesViewHolder>(
                Note.class,
                R.layout.card_notes,
                NotesViewHolder.class,
                mNotes.orderByChild("timestamp")
        ) {
            @Override
            protected void populateViewHolder(final NotesViewHolder viewHolder, Note model, int position) {
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
                                viewHolder.mView.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent viewNoteIntent = new Intent(getContext(),ViewNoteActivity.class);
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

    public void performSearch(final String searchQuery, final String node, final String outgoingFragment){
        mAlert.setMessage(getString(R.string.info_no_results))
                .setPositiveButton(getString(R.string.option_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage(getString(R.string.info_fetching_results));
        mProgress.show();
        final Query mParent = FirebaseDatabase.getInstance().getReference().child(node);
        final DatabaseReference[] mSearch = new DatabaseReference[1];

        mParent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSearch[0] = FirebaseDatabase.getInstance().getReference().child(node).child(mAuth.getCurrentUser().getUid());
                mSearch[0].orderByChild("title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            mProgress.dismiss();
                            mAlert.show();
//                            Toast.makeText(getContext(), "No results", Toast.LENGTH_SHORT).show();
                        } else {
                            mProgress.dismiss();
                            Intent resultsActivity = new Intent(getContext(),NotesList.class);
                            resultsActivity.putExtra("post_key","");
                            resultsActivity.putExtra("outgoing_intent","MyFilesActivity_NotesFragment");

                            startActivity(resultsActivity);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        super.onCreateOptionsMenu(menu, inflater);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (null != searchView){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearch(s,"Notes","MyFilesActivity_NotesFragment");

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
    }

    public static class NotesViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public NotesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title_notes) ;
            txtTitle.setText(title);
        }
        public void setTimestamp(String timestamp){
            TextView txtTime = (TextView)mView.findViewById(R.id.txt_time_notes);
            txtTime.setText(timestamp);
        }
        public void setDescription(String description){
            TextView txtDescription = (TextView)mView.findViewById(R.id.txt_text_notes);
            txtDescription.setText(description);
        }
        public void setNull(){
//            TextView txtEmpty = mView.findViewById(R.id.txt_notes_empty);
//            txtEmpty.setText(R.string.info_no_notes_to_display);
        }
    }

}