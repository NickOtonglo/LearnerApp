package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick Otto on 14/06/2019.
 */

public class ViewAuthorActivity_PostsFragment extends Fragment {
    private RecyclerView mRecycler;
    private DatabaseReference mPublishedItems,mCoursework,mNonCoursework;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;
    private Query mCourseworkQuery,mNonCourseworkQuery;
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

        mProgress = new ProgressDialog(getActivity());
        mAlert = new AlertDialog.Builder(requireActivity(),R.style.AlertDialogStyle);

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) postsFragment.findViewById(R.id.txt_upload_empty);

        mPublishedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuthor);
        mPublishedItems.keepSynced(true);
        mCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mCoursework.keepSynced(true);
        mNonCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mNonCoursework.keepSynced(true);
        mCourseworkQuery = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mCourseworkQuery.keepSynced(true);
        mNonCourseworkQuery = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mNonCourseworkQuery.keepSynced(true);

        mPublishedItems.orderByChild("Published").equalTo("true").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    txtEmpty.setText(getString(R.string.info_no_posts_found));
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

        listPosts(mPublishedItems.orderByChild("Published").equalTo("true"));
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
                final String key = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuthor).child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (dataSnapshot.child("Published").getValue().equals("true")) {
                                if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))) {
                                    mCoursework.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                txtEmpty.setVisibility(View.GONE);
                                                mCourseworkQuery.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(key).child("title").getValue()));
                                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(key).child("timestamp").getValue()));
                                                        String file_type = String.valueOf(dataSnapshot.child(key).child("file_type").getValue());
                                                        mAuthor = String.valueOf(dataSnapshot.child(key).child("author").getValue());
                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                try {
                                                                    String name = dataSnapshot.child("username").getValue().toString();
                                                                    String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                    viewHolder.setAuthorName(name);
                                                                    if (!avatarLink.isEmpty()) {
                                                                        viewHolder.setAuthorImage(getContext(), avatarLink);
                                                                    } else {
                                                                        viewHolder.setAuthorImage();
                                                                    }
                                                                } catch (NullPointerException e) {
                                                                    e.printStackTrace();
                                                                    listPosts(mPublishedItems);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                                        if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")) {
                                                            if (file_type.equals("audio")) {
                                                                viewHolder.setAudioImage();
                                                            } else if (file_type.equals("video")) {
                                                                viewHolder.setVideoImage();
                                                            } else if (file_type.equals("image")) {
                                                                viewHolder.setImageImage();
                                                            } else if (file_type.equals("ticket")) {
                                                                viewHolder.setDocImage();
                                                            }
                                                        } else {
                                                            viewHolder.setThumbnail(getContext(), thumbnail);
                                                        }
                                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                checkOwnershipStatus(key, getString(R.string.firebase_ref_posts_type_1));
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
                                } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))) {
                                    mNonCoursework.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getChildrenCount() > 0) {
                                                txtEmpty.setVisibility(View.GONE);
                                                mNonCourseworkQuery.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(key).child("title").getValue()));
                                                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(key).child("timestamp").getValue()));
                                                        String file_type = String.valueOf(dataSnapshot.child(key).child("file_type").getValue());
                                                        mAuthor = String.valueOf(dataSnapshot.child(key).child("author").getValue());
                                                        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                try {
                                                                    String name = dataSnapshot.child("username").getValue().toString();
                                                                    String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                    viewHolder.setAuthorName(name);
                                                                    if (!avatarLink.isEmpty()) {
                                                                        viewHolder.setAuthorImage(getContext(), avatarLink);
                                                                    } else {
                                                                        viewHolder.setAuthorImage();
                                                                    }
                                                                } catch (NullPointerException e) {
                                                                    e.printStackTrace();
                                                                    listPosts(mPublishedItems);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                                        if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")) {
                                                            if (file_type.equals("audio")) {
                                                                viewHolder.setAudioImage();
                                                            } else if (file_type.equals("video")) {
                                                                viewHolder.setVideoImage();
                                                            } else if (file_type.equals("image")) {
                                                                viewHolder.setImageImage();
                                                            } else if (file_type.equals("ticket")) {
                                                                viewHolder.setDocImage();
                                                            }
                                                        } else {
                                                            viewHolder.setThumbnail(getContext(), thumbnail);
                                                        }
                                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                checkOwnershipStatus(key, getString(R.string.firebase_ref_posts_type_2));
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
                            } else {
                                viewHolder.setTitle(getString(R.string.info_post_is_private));
                                viewHolder.setAuthorImage();
                                viewHolder.setTimestamp(" ");
                                viewHolder.setAuthorName(" ");
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

    public void checkOwnershipStatus(final String key, final String category){
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

                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","ViewAuthorActivity_PostsFragment_D");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    transactionsIntent.putExtra("title",title);
                    transactionsIntent.putExtra("file_type",fileType);
                    transactionsIntent.putExtra("postType",category);
                    startActivity(transactionsIntent);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",category);
                    startActivity(viewPostIntent);
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",category);
                    startActivity(viewPostIntent);
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
        Query reference = mPublishedItems.orderByChild("Title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff");
        listPosts(reference);
    }

}
