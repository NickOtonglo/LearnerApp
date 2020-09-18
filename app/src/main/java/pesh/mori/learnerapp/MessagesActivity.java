package pesh.mori.learnerapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView mRecycler;

    private DatabaseReference mMessages;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;

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
        setContentView(R.layout.activity_messages);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView)findViewById(R.id.txt_msg_empty);

        mMessages = FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid());
        mMessages.keepSynced(true);
        mMessages.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText(R.string.info_no_messages_to_display);
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)findViewById(R.id.layout_recycler_msg);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(layoutManager);
    }

    public void loadMessages(Query databaseReference){
        FirebaseRecyclerAdapter<Message,MessagesViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message,MessagesViewHolder>(
                Message.class,
                R.layout.card_messages,
                MessagesViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final MessagesViewHolder viewHolder, Message model, int position) {
                final String messageKey = getRef(position).getKey();

                mMessages.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.getChildrenCount()>0){
                                if (dataSnapshot.child(messageKey).child("seen").exists() && dataSnapshot.child(messageKey).child("seen").getValue().equals("false")){
//                                    Log.d("setSeenIndicator","seen: false");
                                    viewHolder.setSeenIndicator();
                                }
                                viewHolder.setTitle(String.valueOf(dataSnapshot.child(messageKey).child("title").getValue()));
                                viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(messageKey).child("timestamp").getValue()));
                                viewHolder.setMessage(String.valueOf(dataSnapshot.child(messageKey).child("message").getValue()));
                                viewHolder.setSender(String.valueOf(dataSnapshot.child(messageKey).child("sender_id").getValue()));
                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent viewMessageIntent = new Intent(getApplicationContext(),ViewMessagesActivity.class);
                                        viewMessageIntent.putExtra("message_key",messageKey);
                                        startActivity(viewMessageIntent);
                                        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                                    }
                                });
                            } else {

                            }
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

    public void performSearch(String searchQuery){
        Query reference = mMessages.orderByChild("title").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        loadMessages(reference);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadMessages(mMessages.orderByChild("timestamp"));
    }

    public static class MessagesViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public MessagesViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title){
            TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title_msg) ;
            txtTitle.setText(title);
        }
        public void setTimestamp(String timestamp){
            TextView txtTime = (TextView)mView.findViewById(R.id.txt_time_msg);
            txtTime.setText(timestamp);
        }
        public void setMessage(String message){
            TextView txtMessage = (TextView)mView.findViewById(R.id.txt_text_msg);
            txtMessage.setText(message);
        }
        public void setSender(String sender_id){
            final TextView txtSender = (TextView)mView.findViewById(R.id.txt_sender_msg);
            FirebaseDatabase.getInstance().getReference().child("Users").child(sender_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    txtSender.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        public void setNull(){
            TextView txtEmpty = mView.findViewById(R.id.txt_notes_empty);
            txtEmpty.setText(R.string.info_no_messages_to_display);
        }
        public void setSeenIndicator(){
            TextView txtMessage = (TextView)mView.findViewById(R.id.txt_read_indicator);
            txtMessage.setText(R.string.info_new_indicator);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        if (null != searchView){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                performSearch(s);
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
        return super.onCreateOptionsMenu(menu);
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
    }
}
