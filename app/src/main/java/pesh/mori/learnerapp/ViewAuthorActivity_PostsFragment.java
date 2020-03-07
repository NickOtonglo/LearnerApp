package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick Otto on 14/06/2019.
 */

public class ViewAuthorActivity_PostsFragment extends Fragment {
    private RecyclerView mRecycler;
    private DatabaseReference mFiles,mFilesItems,mDIY;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private TextView txtEmpty;
    private Query mUserFiles,mUserDIY;
    private String mAuthor;
    private AlertDialog.Builder mAlert;

    private ProgressDialog mProgress;

    public ViewAuthorActivity_PostsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View postsFragment = inflater.inflate(R.layout.fragment_author_posts,container,false);

        mAuthor = ((ViewAuthorActivity)getActivity()).getAuthorId();

        mProgress = new ProgressDialog(getContext());
        mAlert = new AlertDialog.Builder(getContext());

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) postsFragment.findViewById(R.id.txt_upload_empty);

        mFilesItems = FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuthor);
        mFilesItems.keepSynced(true);
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mDIY.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuthor);
        mUserFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mUserFiles.keepSynced(true);
        mUserDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mUserDIY.keepSynced(true);

        mFilesItems.orderByChild("Published").equalTo("true").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    txtEmpty.setText("No posts found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)postsFragment.findViewById(R.id.layout_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        return postsFragment;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        listPosts(mFilesItems.orderByChild("Published").equalTo("true"));
    }

    public void listPosts(Query databaseReference){
        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, File model, int position) {
                final String fileKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuthor).child(fileKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (dataSnapshot.child("Category").getValue().equals("Files")){
                                mFiles.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount()>0){
                                            txtEmpty.setVisibility(View.GONE);
                                            mUserFiles.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try {
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                listPosts(mFilesItems.orderByChild("Published").equalTo("true"));
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    String thumbnail = String.valueOf(dataSnapshot.child(fileKey).child("thumbnail").getValue());
                                                    if (!dataSnapshot.child(fileKey).child("thumbnail").exists() || thumbnail.equals("")){
                                                        if (file_type.equals("audio")){
                                                            viewHolder.setAudioImage();
                                                        } else if (file_type.equals("video")){
                                                            viewHolder.setVideoImage();
                                                        } else if (file_type.equals("image")){
                                                            viewHolder.setImageImage();
                                                        } else if (file_type.equals("doc")){
                                                            viewHolder.setDocImage();
                                                        }
                                                    } else {
                                                        viewHolder.setThumbnail(getContext(),thumbnail);
                                                    }
                                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            checkOwnershipStatus(fileKey,"Files");
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        } else {
                                            viewHolder.setNull();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            else if (dataSnapshot.child("Category").getValue().equals("DIY")){
                                mDIY.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount()>0){
                                            txtEmpty.setVisibility(View.GONE);
                                            mUserDIY.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try {
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                listPosts(mFilesItems);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    String thumbnail = String.valueOf(dataSnapshot.child(fileKey).child("thumbnail").getValue());
                                                    if (!dataSnapshot.child(fileKey).child("thumbnail").exists() || thumbnail.equals("")){
                                                        if (file_type.equals("audio")){
                                                            viewHolder.setAudioImage();
                                                        } else if (file_type.equals("video")){
                                                            viewHolder.setVideoImage();
                                                        } else if (file_type.equals("image")){
                                                            viewHolder.setImageImage();
                                                        } else if (file_type.equals("doc")){
                                                            viewHolder.setDocImage();
                                                        }
                                                    } else {
                                                        viewHolder.setThumbnail(getContext(),thumbnail);
                                                    }
                                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            checkOwnershipStatus(fileKey,"DIY");
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        } else {
                                            viewHolder.setNull();
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
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public void checkOwnershipStatus(final String key,final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
                String author = String.valueOf(dataSnapshot.child("author").getValue());
                String title = String.valueOf(dataSnapshot.child("title").getValue());
                String fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                Double price = Double.parseDouble(itemPrice);
                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (category.equals("Files")){
                    String tag = String.valueOf(dataSnapshot.child("institution").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","ViewAuthorActivity_PostsFragment_F");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }  else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    }
                }else if (category.equals("DIY")){
                    String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","ViewAuthorActivity_PostsFragment_D");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        startActivity(viewFileIntent);
                    } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getContext(),ViewDiyActivity.class);
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
    }

    public void performSearch(final String searchQuery){
        Query reference = mFilesItems.orderByChild("Title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff");
        listPosts(reference);
    }

}
