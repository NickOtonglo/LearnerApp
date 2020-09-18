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
import android.widget.RadioButton;
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

import java.util.Arrays;
import java.util.List;

/**
 * Created by MORIAMA on 18/11/2017.
 */

public class MyFilesActivity_DownloadsFragment extends Fragment {

    private RadioButton rAudio,rVideo,rDocument;

    private RecyclerView mRecycler;

    private DatabaseReference mOwnedItems,mCoursework,mNonCoursework;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;
    private RadioButton radioAudio,radioVideo,radioDoc;

    private String fileType;

    private String mAuthor="",author;

    private AlertDialog.Builder mAlert;

    private ProgressDialog mProgress;

    private MyFilesActivity myFilesActivity;

    public MyFilesActivity_DownloadsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myInventory = inflater.inflate( R.layout.fragment_mydownloads, container, false );

        myFilesActivity = new MyFilesActivity();
        mAlert = new AlertDialog.Builder(getActivity(),R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();

        mOwnedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).child(mAuth.getCurrentUser().getUid());
        mOwnedItems.keepSynced(true);
        mCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mCoursework.keepSynced(true);
        mNonCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mNonCoursework.keepSynced(true);

        txtEmpty = myInventory.findViewById(R.id.txt_download_empty);

        mOwnedItems.addValueEventListener(new ValueEventListener() {
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

        //radioImage = MyFilesActivity_DownloadsFragment.findViewById(R.id.radio_image_download);
        radioAudio = myInventory.findViewById(R.id.radio_audio_download);
        radioVideo = myInventory.findViewById(R.id.radio_video_download);
        radioDoc = myInventory.findViewById(R.id.radio_document_download);
        radioAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadList();
            }
        });
        radioVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadList();
            }
        });
        radioDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadList();
            }
        });

        mRecycler = (RecyclerView)myInventory.findViewById(R.id.layout_recycler_mydownloads);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

//        mAlert = new AlertDialog.Builder(getActivity(),R.style.AlertDialogStyle);

        return myInventory;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                mOwnedItems
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, final File model, int position) {
                final String fileKey = getRef(position).getKey();

                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(fileKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                                mCoursework.addValueEventListener(new ValueEventListener() {
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
                                                    reloadList();
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
                                                checkOwnershipStatus(fileKey,getString(R.string.firebase_ref_posts_type_1));
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))){
                                mNonCoursework.addValueEventListener(new ValueEventListener() {
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
                                                    reloadList();
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
                                                checkOwnershipStatus(fileKey,getString(R.string.firebase_ref_posts_type_2));
                                            }
                                        });
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

    private void reloadList(){

        if (radioDoc.isChecked()){
            fileType = "doc";
        }
        if (radioVideo.isChecked()){
            fileType = "video";
        }
        if (radioAudio.isChecked()){
            fileType = "audio";
        }
//        if (radioImage.isChecked()){
//            fileType = "image";
//        }

        FirebaseRecyclerAdapter<File,PostsViewHolder_BigCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                File.class,
                R.layout.card_big,
                PostsViewHolder_BigCard.class,
                mOwnedItems.orderByChild("FileType").equalTo(fileType)
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, final File model, int position) {
                final String key = getRef(position).getKey();
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                        if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                            mCoursework.addValueEventListener(new ValueEventListener() {
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
                                                if (!avatarLink.isEmpty()){
                                                    viewHolder.setAuthorImage(getContext(),avatarLink);
                                                } else {
                                                    viewHolder.setAuthorImage();
                                                }
                                            } catch (NullPointerException e){
                                                e.printStackTrace();
                                                reloadList();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(key,getString(R.string.firebase_ref_posts_type_1));
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_2))){
                            mNonCoursework.addValueEventListener(new ValueEventListener() {
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
                                                if (!avatarLink.isEmpty()){
                                                    viewHolder.setAuthorImage(getContext(),avatarLink);
                                                } else {
                                                    viewHolder.setAuthorImage();
                                                }
                                            } catch (NullPointerException e){
                                                e.printStackTrace();
                                                reloadList();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    String thumbnail = String.valueOf(dataSnapshot.child(key).child("thumbnail").getValue());
                                    if (!dataSnapshot.child(key).child("thumbnail").exists() || thumbnail.equals("")){
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
                                            checkOwnershipStatus(key,getString(R.string.firebase_ref_posts_type_2));
                                        }
                                    });
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
                    transactionsIntent.putExtra("outgoing_intent","FilteredCategoryActivity");
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
                performSearch(s,getString(R.string.firebase_ref_posts_owned),"MyFilesActivity_DownloadsFragment");

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);
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
                mSearch[0].orderByChild("Title").startAt(searchQuery.toUpperCase()).endAt(searchQuery.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
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