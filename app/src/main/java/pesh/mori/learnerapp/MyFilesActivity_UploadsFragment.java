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
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
 * Created by MORIAMA on 18/11/2017.
 */

public class MyFilesActivity_UploadsFragment extends Fragment {
    FloatingActionMenu fabMain;
    private FloatingActionButton fabAudio,fabVideo,fabDoc;

    private RecyclerView mRecycler;

    private DatabaseReference mFiles, mPublishedItems, mPosts;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;
    private Query mUserFiles, mUserPosts;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private MyFilesActivity myFilesActivity;

    private String mAuthor="",published="";

    public MyFilesActivity_UploadsFragment(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myPosts = inflater.inflate(R.layout.fragment_myuploads, container, false);

        myFilesActivity = new MyFilesActivity();
        mProgress = new ProgressDialog(getActivity());
        mAlert = new AlertDialog.Builder(getActivity(),R.style.AlertDialogStyle);

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) myPosts.findViewById(R.id.txt_upload_empty);

        fabAudio = myPosts.findViewById(R.id.fab_audio);
        fabVideo = myPosts.findViewById(R.id.fab_video);
        fabDoc = myPosts.findViewById(R.id.fab_doc);

        fabAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFabIntent("audio");
            }
        });
        fabVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFabIntent("video");
            }
        });
        fabDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFabIntent("PDF");
            }
        });

        mPublishedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid());
        mPublishedItems.keepSynced(true);

        mPublishedItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText(getString(R.string.info_no_posts_found));
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)myPosts.findViewById(R.id.layout_recycler_myuploads);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        return myPosts;
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        loadList();
    }

    private void setFabIntent(String uploadType){
        Intent intent = new Intent(getContext(),NewPostActivity.class);
        intent.putExtra("upload_type",uploadType);
        startActivity(intent);
        getActivity().overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
    }

    public void loadList(){
        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                mPublishedItems
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, File model, int position) {
                final String postKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                                mPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
                                mPosts.keepSynced(true);
                                mUserPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
                                mUserPosts.keepSynced(true);
                                buildListItems(mPosts,mUserPosts,viewHolder,postKey,getString(R.string.firebase_ref_posts_type_1));
                            } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))){
                                mPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
                                mPosts.keepSynced(true);
                                mUserPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
                                mUserPosts.keepSynced(true);
                                buildListItems(mPosts,mUserPosts,viewHolder,postKey,getString(R.string.firebase_ref_posts_type_2));
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

    public void buildListItems(DatabaseReference refPosts, Query refUserPosts, PostsViewHolder_BigCard viewHolder, String postKey, String category){
        refPosts.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0){
                    txtEmpty.setVisibility(View.GONE);
                    refUserPosts.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            viewHolder.setTitle(String.valueOf(dataSnapshot.child(postKey).child("title").getValue()));
                            viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(postKey).child("timestamp").getValue()));
                            String file_type = String.valueOf(dataSnapshot.child(postKey).child("file_type").getValue());
                            mAuthor = String.valueOf(dataSnapshot.child(postKey).child("author").getValue());
                            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        loadList();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            String thumbnail = String.valueOf(dataSnapshot.child(postKey).child("thumbnail").getValue());
                            if (!dataSnapshot.child(postKey).child("thumbnail").exists() || thumbnail.equals("")){
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
                                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.child("Published").exists()){
                                                        published = String.valueOf(dataSnapshot.child("Published").getValue());
//                                                                                Log.d("LOG_published",published);
                                                        if (published.equals("false")){
                                                            Intent viewFileIntent = new Intent(getContext(),PreviewPostActivity.class);
                                                            viewFileIntent.putExtra("postKey",postKey)
                                                                    .putExtra("postNode",category);
                                                            startActivity(viewFileIntent);
                                                        } else {
                                                            Intent viewPostIntent = new Intent(getContext(), ViewPostActivity.class);
                                                            viewPostIntent.putExtra("file_key",postKey);
                                                            viewPostIntent.putExtra("postType",category);
                                                            startActivity(viewPostIntent);
                                                        }
                                                    } else {
                                                        Intent viewPostIntent = new Intent(getContext(), ViewPostActivity.class);
                                                        viewPostIntent.putExtra("file_key",postKey);
                                                        viewPostIntent.putExtra("postType",category);
                                                        startActivity(viewPostIntent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

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
                performSearch(s,getString(R.string.firebase_ref_posts_published),"MyFilesActivity_UploadsFragment");

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public String checkOwnership(final String key){
        mUserFiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child(key).child("owners").getValue());
                List<String> resultOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (!resultOwners.contains(String.valueOf(mAuth.getCurrentUser().getUid()))){
                    mAlert.setTitle(R.string.title_purchase_item)
                            .setMessage(R.string.confirm_you_do_not_own_item_purchase_item+" (X tokens)")
                            .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                    .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return key;
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

        mParent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSearch[0] = FirebaseDatabase.getInstance().getReference().child(node).child(mAuth.getCurrentUser().getUid());
                mSearch[0].orderByChild("Title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()){
                            mProgress.dismiss();
                            mAlert.show();
//                            Toast.makeText(getContext(), "No results", Toast.LENGTH_SHORT).show();
                        } else {
                            mProgress.dismiss();
                            Intent resultsActivity = new Intent(getContext(), SearchPostActivity.class);
                            resultsActivity.putExtra("query",searchQuery.toUpperCase());
                            resultsActivity.putExtra("activityFrom",outgoingFragment);

                            startActivity(resultsActivity);
                            getActivity().overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
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

}