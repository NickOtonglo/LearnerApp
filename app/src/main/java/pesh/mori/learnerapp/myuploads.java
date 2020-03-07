package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.github.clans.fab.FloatingActionMenu;
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
 * Created by MORIAMA on 18/11/2017.
 */

public class myuploads extends Fragment{
//    private FloatingActionButton fabUpload;
    private FloatingActionMenu fabMenu;
    private com.github.clans.fab.FloatingActionButton fabDiy,fabFile;

    private RecyclerView mRecycler;

    private DatabaseReference mFiles,mFilesItems,mDIY;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private TextView txtEmpty;
    private Query mUserFiles,mUserDIY;

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private MyFilesActivity myFilesActivity;

    private String mAuthor="",published="";

    public myuploads(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myuploads = inflater.inflate(R.layout.fragment_myuploads, container, false);
//        Toast.makeText(getActivity(), "INFO | Under Development",  Toast.LENGTH_SHORT).show();

        myFilesActivity = new MyFilesActivity();
        mProgress = new ProgressDialog(getContext());
        mAlert = new AlertDialog.Builder(getContext());

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) myuploads.findViewById(R.id.txt_upload_empty);

        fabDiy = myuploads.findViewById(R.id.fab_diy);
        fabDiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),NewDiyUpload.class));
                getActivity().overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });
        fabFile = myuploads.findViewById(R.id.fab_file);
        fabFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),NewFileUpload.class));
                getActivity().overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });

//        fabUpload = (FloatingActionButton)myuploads.findViewById(R.id.fab_upload);
//        fabUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getActivity(),NewFileUpload.class));
//            }
//        });

        mFilesItems = FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid());
        mFilesItems.keepSynced(true);
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mDIY.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference().child("Files").child(mAuth.getCurrentUser().getUid());
        mUserFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mUserFiles.keepSynced(true);
        mUserDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mUserDIY.keepSynced(true);

        mFilesItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtEmpty.setText("No posts found");
                } else {
                    txtEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)myuploads.findViewById(R.id.layout_recycler_myuploads);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        return myuploads;
    }
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        loadList();
    }

    public void loadList(){
        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                mFilesItems
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, File model, int position) {
                final String fileKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
//                            Log.d("LOG_dataSnapshot",String.valueOf(dataSnapshot));
                            if (dataSnapshot.child("Category").getValue().equals("Files")){
                                mFiles.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount()>0){
                                            txtEmpty.setVisibility(View.GONE);
                                            mUserFiles.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
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
                                                            FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey)
                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            if (dataSnapshot.child("Published").exists()){
                                                                                published = String.valueOf(dataSnapshot.child("Published").getValue());
//                                                                                Log.d("LOG_published",published);
                                                                                if (published.equals("false")){
                                                                                    Intent viewFileIntent = new Intent(getContext(),PreviewPostActivity.class);
                                                                                    viewFileIntent.putExtra("postKey",fileKey)
                                                                                            .putExtra("postNode","Files");
                                                                                    startActivity(viewFileIntent);
                                                                                } else {
                                                                                    Intent viewFileIntent = new Intent(getContext(),ViewFileActivity.class);
                                                                                    viewFileIntent.putExtra("file_key",fileKey);
                                                                                    startActivity(viewFileIntent);
                                                                                }
                                                                            } else {
                                                                                Intent viewFileIntent = new Intent(getContext(),ViewFileActivity.class);
                                                                                viewFileIntent.putExtra("file_key",fileKey);
                                                                                startActivity(viewFileIntent);
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
                            else if (dataSnapshot.child("Category").getValue().equals("DIY")){
                                mDIY.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount()>0){
                                            txtEmpty.setVisibility(View.GONE);
                                            mUserDIY.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
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
                                                            FirebaseDatabase.getInstance().getReference().child("PublishedItems").child(mAuth.getCurrentUser().getUid()).child(fileKey)
                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            if (dataSnapshot.child("Published").exists()){
                                                                                published = String.valueOf(dataSnapshot.child("Published").getValue());
//                                                                                Log.d("LOG_published",published);
                                                                                if (published.equals("false")){
                                                                                    Intent viewFileIntent = new Intent(getContext(),PreviewPostActivity.class);
                                                                                    viewFileIntent.putExtra("postKey",fileKey)
                                                                                            .putExtra("postNode","DIY");
                                                                                    startActivity(viewFileIntent);
                                                                                } else {
                                                                                    Intent viewFileIntent = new Intent(getContext(),ViewDiyActivity.class);
                                                                                    viewFileIntent.putExtra("file_key",fileKey);
                                                                                    startActivity(viewFileIntent);
                                                                                }
                                                                            } else {
                                                                                Intent viewFileIntent = new Intent(getContext(),ViewDiyActivity.class);
                                                                                viewFileIntent.putExtra("file_key",fileKey);
                                                                                startActivity(viewFileIntent);
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
                performSearch(s,"PublishedItems","myuploads");

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

//        FirebaseRecyclerAdapter<File,UploadsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, UploadsViewHolder>(
//                File.class,
//                R.layout.card_big,
//                UploadsViewHolder.class,
//                mFiles.orderByChild("file_type")
//        ) {
//            @Override
//            protected void populateViewHolder(final UploadsViewHolder viewHolder, File model, int position) {
//                final String fileKey = getRef(position).getKey();
//
//                viewHolder.setTitle(model.getTitle());
//                viewHolder.setTimestamp(model.getTimestamp());
//                mFiles.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.getChildrenCount()>0){
//                            txtEmpty.setVisibility(View.GONE);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        };
//        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public String checkOwnership(final String key){
        mUserFiles.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child(key).child("owners").getValue());
                List<String> resultOwners = Arrays.asList(owners.split("\\s*,\\s*"));
                if (!resultOwners.contains(String.valueOf(mAuth.getCurrentUser().getUid()))){
                    mAlert.setTitle("Purchase item")
                            .setMessage("You don't own this item yet. Would you like to purchase it for...")
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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

    public void performSearch(final String searchQuery,final String node,final String outgoingFragment){
        mAlert.setMessage("No results")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage("Fetching results...");
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
                            Intent resultsActivity = new Intent(getContext(),DownloadsList.class);
                            resultsActivity.putExtra("query",searchQuery.toUpperCase());
                            resultsActivity.putExtra("activityFrom",outgoingFragment);

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


}