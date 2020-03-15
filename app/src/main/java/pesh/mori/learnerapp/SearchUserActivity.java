package pesh.mori.learnerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SearchUserActivity extends AppCompatActivity {
    private RecyclerView mRecycler;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAlert = new AlertDialog.Builder(this);

        mAuth = FirebaseAuth.getInstance();

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mRecycler = (RecyclerView)findViewById(R.id.layout_recycler_search_user);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        listUsers(mUsers.orderByChild("email"));
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

    private void performSearch(String searchQuery) {
        Query databaseReference = mUsers.orderByChild("username").startAt(searchQuery).endAt(searchQuery+"\uf8ff");
        listUsers(databaseReference);
    }

    public void listUsers(Query databaseReference){
        try {
            FirebaseRecyclerAdapter<SearchUser,SearchUserActivity.UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<SearchUser,SearchUserActivity.UsersViewHolder>(
                    SearchUser.class,
                    R.layout.card_user_list,
                    SearchUserActivity.UsersViewHolder.class,
                    databaseReference
            ) {
                @Override
                protected void populateViewHolder(final SearchUserActivity.UsersViewHolder viewHolder, SearchUser model, int position) {
                    final String userId = getRef(position).getKey();

                    mUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.getChildrenCount()>0){
                                    viewHolder.setName(String.valueOf(dataSnapshot.child(userId).child("username").getValue()));
                                    if (dataSnapshot.child(userId).child("profile_picture").exists() && !dataSnapshot.child(userId).child("profile_picture").getValue().toString().equals("")){
                                        viewHolder.setAvatar(getApplicationContext(),dataSnapshot.child(userId).child("profile_picture").getValue().toString());
                                    } else {
                                        viewHolder.setAvatar();
                                    }
                                    FirebaseDatabase.getInstance().getReference().child("Bio").child(userId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                viewHolder.setBio(String.valueOf(dataSnapshot.child("about").getValue()));
                                            } else {
                                                viewHolder.setBio("Bio information not available.");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (dataSnapshot.child(userId).child("email").exists()){
                                                if (dataSnapshot.child(userId).child("email").getValue().equals(mAuth.getCurrentUser().getEmail())){
                                                    Toast.makeText(SearchUserActivity.this, "You cannot transfer tokens to yourself!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Intent returnIntent = getIntent();
                                                    returnIntent.putExtra("userId",userId);
                                                    setResult(Activity.RESULT_OK,returnIntent);
                                                    onBackPressed();
                                                }
                                            } else {
                                                mAlert.setTitle(R.string.error_general)
                                                        .setMessage(R.string.error_user_account_unable_to_transfer_tokens)
                                                        .setPositiveButton(R.string.option_contact_support, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                contactSupport();
                                                            }
                                                        })
                                                        .setNegativeButton(R.string.option_alert_close, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        })
                                                        .show();
                                            }

                                        }
                                    });
                                } else {

                                }
                            } else if (!dataSnapshot.exists()){
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
            mRecycler.setAdapter(firebaseRecyclerAdapter);
        } catch (NullPointerException e){
            Log.d("LOG_EmptyUserNode",e.getMessage());
        }
    }

    public void contactSupport(){
        DatabaseReference mLinks = FirebaseDatabase.getInstance().getReference().child("Links");
        mLinks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse(dataSnapshot.child("support").getValue().toString()));
                SearchUserActivity.this.startActivity( intent1 );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView txtName = (TextView)mView.findViewById(R.id.txt_card_user_list_name) ;
            txtName.setText(name);
        }
        public void setBio(String about){
            TextView txtAbout = (TextView)mView.findViewById(R.id.txt_card_user_list_about);
            txtAbout.setText(about);
        }
        public void setAvatar(Context ctx, String image){
            ImageView imgAvatar = (ImageView)mView.findViewById(R.id.img_avatar);
            Picasso.with(ctx).load(image).into(imgAvatar);
        }
        public void setAvatar(){
            ImageView imgAvatar = (ImageView)mView.findViewById(R.id.img_avatar);
            imgAvatar.setImageResource(R.mipmap.ic_user);
        }
    }
}
