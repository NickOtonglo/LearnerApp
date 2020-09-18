package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NestedScrollView scrollView;
    private FirebaseAuth mAuth;
//    private FloatingActionMenu fabMenu;
    private com.github.clans.fab.FloatingActionButton floatingActionButton;
    private DrawerLayout mDrawerLayout;
    private FrameLayout home;
    private String mAuthor="";
    private RecyclerView recyclerAnnouncements,recyclerPromotions,recyclerPosts;
    private DatabaseReference mLinks,mPostsNonCoursework,mPostsCoursework,mPostsAll,mPromos,mAnnouncements,mAnnouncementsRef;
    private DatabaseReference mUsers;
    private TextView txtAnnouncements,txtPromoted,txtPosts;
    private ProgressDialog mProgress;
    private Query mSearch;
    private Uri mFileUri = null;
    public String tokenBalance,mLink;
    private String TAG = "HomeActivity",node="Texts",socket="BidSocket";
    private GoogleSignInClient mGoogleSignInClient;
    private int vCode = BuildConfig.VERSION_CODE;
    private BadgeDrawerToggle badgeToggle;
    /*v1.0.5 enhancement 00002*/
    int announcementLimit=10,promosLimit=10,postsLimit=25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        if (!checkNetworkState()){
            Snackbar.make(findViewById(android.R.id.content),R.string.error_no_internet_connectivity,Snackbar.LENGTH_LONG).show();
        }

        setAppVersion();
        checkAppUpdate();
        checkUserEnabledStatus();
        /*v1.0.5 enhancement 00002*/
        initCardLimits();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        floatingActionButton = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab);
//        fabMenu = findViewById(R.id.fab_upload);

        scrollView = findViewById(R.id.nested_scrollview);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    floatingActionButton.setVisibility(View.GONE);
                } else {
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }
        });

        mProgress = new ProgressDialog(this);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SelectGenreActivity.class));
            }
        });

        NewItemsCounter.countUnreadMessages();
        initCloudPackets();
        checkMaintenanceStatus(getApplicationContext());
        if (mAuth.getCurrentUser() == null) {
            mAuth.signOut();
            Intent loginIntent = new Intent(HomeActivity.this, SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        } else {
            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    /*v1.0.4 bug fix 00002*/
                    if (dataSnapshot.child("account_manager").exists() && dataSnapshot.child("phone").exists()){
                        if(dataSnapshot.child("account_manager").getValue().equals("learner_app") && dataSnapshot.child("phone").getValue().equals("")){
                            Intent loginIntent = new Intent(HomeActivity.this, ChangePhoneActivity.class);
                            loginIntent.putExtra("incomingIntent","HomeActivity");
                            startActivity(loginIntent);
                            finish();
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, R.string.error_account_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        initFCM();

//        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        txtAnnouncements = findViewById(R.id.txt_label_announcements);
        txtPromoted = findViewById(R.id.txt_label_promoted);
        txtPosts = findViewById(R.id.txt_label_posts);

        mPostsAll = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
        mPostsAll.keepSynced(true);
        mPostsNonCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_2));
        mPostsNonCoursework.keepSynced(true);
        mPostsCoursework = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_type_1));
        mPostsCoursework.keepSynced(true);

        mAnnouncements = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_announcements));
        mAnnouncements.keepSynced(true);
        mAnnouncementsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_announcements_ref));
        mAnnouncementsRef.keepSynced(true);
        mPromos = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_promoted));
        mPromos.keepSynced(true);

        setNulls();

        recyclerAnnouncements = (RecyclerView) findViewById(R.id.layout_recycler_announcements);
        recyclerAnnouncements.setHasFixedSize(true);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true);
        layoutManager1.setReverseLayout(true);
        layoutManager1.setStackFromEnd(true);
        recyclerAnnouncements.setNestedScrollingEnabled(false);
        recyclerAnnouncements.setLayoutManager(layoutManager1);
//
        recyclerPromotions = (RecyclerView) findViewById(R.id.layout_recycler_promoted);
        recyclerPromotions.setHasFixedSize(true);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true);
        layoutManager2.setReverseLayout(true);
        layoutManager2.setStackFromEnd(true);
        recyclerPromotions.setNestedScrollingEnabled(false);
        recyclerPromotions.setLayoutManager(layoutManager2);
//
        recyclerPosts = (RecyclerView) findViewById(R.id.layout_recycler_posts);
        recyclerPosts.setHasFixedSize(false);
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        layoutManager3.setReverseLayout(true);
        layoutManager3.setStackFromEnd(true);
        recyclerPosts.setNestedScrollingEnabled(false);
        recyclerPosts.setLayoutManager(layoutManager3);

        //navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

        getMessageCount();
        final TextView txtName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_header_name);
        final TextView txtTokenBal = navigationView.getHeaderView(0).findViewById(R.id.txt_header_tokens);
//        final CircleImageView circleImageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.img_main_header_profile);
        final CircleImageView headerImageDefault = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.img_main_header_profile_default);

        //Get token balance
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tokenBalance = String.valueOf(dataSnapshot.child("current_balance").getValue());
                if (tokenBalance.equals("") || (tokenBalance.equals("0")) || !dataSnapshot.exists()){
                    txtTokenBal.setText(getString(R.string.hint_token_default_value));
                } else {
                    Double bal = Double.parseDouble(tokenBalance);
                    txtTokenBal.setText(String.format("%.2f",bal));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                if (account == null) {
                    txtName.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                    getSupportActionBar().setSubtitle(getString(R.string.title_hi)+" "+ String.valueOf(dataSnapshot.child("username").getValue()+"!"));
                    String profilePicture = String.valueOf(dataSnapshot.child("profile_picture").getValue());
                    if (!profilePicture.equals("")) {
                        mFileUri = Uri.parse(profilePicture);
                        Picasso.with(getApplicationContext()).load(mFileUri).into(headerImageDefault);
                        getSupportActionBar().setSubtitle(getString(R.string.title_hi)+" "+ String.valueOf(dataSnapshot.child("username").getValue())+"!");
//                        Log.d("LOG_HomeActivity","profilePicture: not null");
                    }
                    if (profilePicture.equals("")){
//                        circleImageView.setVisibility(View.GONE);
                        headerImageDefault.setVisibility(View.VISIBLE);
                        mFileUri = Uri.parse(profilePicture);
//                        Picasso.with(getApplicationContext()).load(R.drawable.ic_baseline_person_24).into(headerImageDefault);
                        getSupportActionBar().setSubtitle(getString(R.string.title_hi)+" "+ String.valueOf(dataSnapshot.child("username").getValue())+"!");
//                        Log.d("LOG_HomeActivity","profilePicture: null");
                    }
                    if (isFacebookAuthenticated()){
                        Profile profile = Profile.getCurrentProfile();
                        String facebookProfilePic = profile.getProfilePictureUri(300,300).toString();
                        Picasso.with(getApplicationContext()).load(facebookProfilePic).into(headerImageDefault);
                        txtName.setText(profile.getName());
                        getSupportActionBar().setSubtitle(getString(R.string.title_hi)+" "+profile.getFirstName()+"!");
//                        updateProfilePicture(profile.getProfilePictureUri(300,300));
                    }
                }
                else if (account != null) {
                    Uri accountPictureUri = account.getPhotoUrl();
                    Picasso.with(getApplicationContext()).load(String.valueOf(accountPictureUri)).into(headerImageDefault);
                    txtName.setText(account.getDisplayName());
                    getSupportActionBar().setSubtitle(getString(R.string.title_hi)+" "+account.getDisplayName()+"!");
                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("profile_picture")
                            .setValue(String.valueOf(accountPictureUri));
//                    updateProfilePicture(accountPictureUri);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        loadCards();

    }

    public void loadCards(){
        /*v1.0.5 enhancement 0001*/
        /*Because it loads asynchronously, dynamic limitToLast() will not work without initCardLimits(). The actual values can remain as they are when initCardLimits() is run.*/
        FirebaseDatabase.getInstance().getReference().child("Values").child("Defaults").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard> firebaseRecyclerAdapter1 = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                            File.class,
                            R.layout.card_home_horizontal_view,
                            PostsViewHolder_BigCard.class,
                            mAnnouncementsRef.limitToLast(announcementLimit)
                    ) {
                        @Override
                        protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, final File model, int position) {
                            final String fileKey = getRef(position).getKey();

                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_announcements_ref)).child(fileKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                        mAnnouncements.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                viewHolder.setAuthorName(getString(R.string.title_administrator));
                                                try{
                                                    viewHolder.setAdminImage();
                                                } catch (NullPointerException e){
                                                    loadCards();
                                                    e.printStackTrace();
                                                }
                                                String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                try {
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
                                                        viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                                                    }
                                                } catch (NullPointerException e){
                                                    loadCards();
                                                    e.printStackTrace();
                                                }
                                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        openAnnouncement(fileKey);
                                                    }
                                                });
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
                    };
                    recyclerAnnouncements.setAdapter(firebaseRecyclerAdapter1);

                    FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard> firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                            File.class,
                            R.layout.card_home_horizontal_view,
                            PostsViewHolder_BigCard.class,
                            mPromos.limitToLast(promosLimit)
                    ) {
                        @Override
                        protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, final File model, int position) {
                            final String fileKey = getRef(position).getKey();

                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_promoted)).child(fileKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                        if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                                            mPostsCoursework.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try{
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getApplicationContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                loadCards();
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    try {
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
                                                            viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                                                        }
                                                    } catch (NullPointerException e){
                                                        e.printStackTrace();
                                                        loadCards();
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
                                            mPostsNonCoursework.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try{
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getApplicationContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                loadCards();
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    try {
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
                                                            viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                                                        }
                                                    } catch (NullPointerException e){
                                                        e.printStackTrace();
                                                        loadCards();
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
                    recyclerPromotions.setAdapter(firebaseRecyclerAdapter2);

                    FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard> firebaseRecyclerAdapter3 = new FirebaseRecyclerAdapter<File, PostsViewHolder_BigCard>(
                            File.class,
                            R.layout.card_big,
                            PostsViewHolder_BigCard.class,
                            mPostsAll.limitToLast(postsLimit)
                    ) {
                        @Override
                        protected void populateViewHolder(final PostsViewHolder_BigCard viewHolder, final File model, int position) {
                            final String fileKey = getRef(position).getKey();

                            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)).child(fileKey).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                        if (dataSnapshot.child("Category").getValue().equals(getString(R.string.firebase_ref_posts_type_1))){
                                            mPostsCoursework.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try{
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getApplicationContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                loadCards();
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    try {
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
                                                            viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                                                        }
                                                    } catch (NullPointerException e){
                                                        e.printStackTrace();
                                                        loadCards();
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
                                            mPostsNonCoursework.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
                                                    viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
                                                    String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
                                                    mAuthor = String.valueOf(dataSnapshot.child(fileKey).child("author").getValue());
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(mAuthor).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            try{
                                                                String name = dataSnapshot.child("username").getValue().toString();
                                                                String avatarLink = dataSnapshot.child("profile_picture").getValue().toString();
                                                                viewHolder.setAuthorName(name);
                                                                if (!avatarLink.isEmpty()){
                                                                    viewHolder.setAuthorImage(getApplicationContext(),avatarLink);
                                                                } else {
                                                                    viewHolder.setAuthorImage();
                                                                }
                                                            } catch (NullPointerException e){
                                                                e.printStackTrace();
                                                                loadCards();
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                    try {
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
                                                            viewHolder.setThumbnail(getApplicationContext(),thumbnail);
                                                        }
                                                    } catch (NullPointerException e){
                                                        e.printStackTrace();
                                                        loadCards();
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
                    recyclerPosts.setAdapter(firebaseRecyclerAdapter3);
                } catch (NullPointerException e){
                    loadCards();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        initCloudPackets();

    }

    @Override
    protected void onPause() {
        super.onPause();
        initCloudPackets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCloudPackets();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            finish();
            System.exit(0);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.buy) {
            Intent intent1 = new Intent( this, TokensActivity.class );
            this.startActivity( intent1 );
        } else if (id == R.id.myfiles) {
            Intent intent1 = new Intent( this, MyFilesActivity.class );
            this.startActivity( intent1 );

//        } else if (id == R.id.social) {
//            us();

        } else if (id == R.id.terms) {
            tandc();


        } else if (id == R.id.support) {
            support();

        } else if (id == R.id.privacy_policy){
            startActivity(new Intent(getApplicationContext(), ViewPrivacyPolicy.class));
            overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
        } else if (id == R.id.messages) {
            Intent intent1 = new Intent( this, MessagesActivity.class );
            this.startActivity( intent1 );

        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this,R.style.AlertDialogStyle);
            alertDialogBuilder.setTitle(R.string.required_log_out);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.option_alert_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                                        moveTaskToBack(true);
//                                        android.os.Process.killProcess(android.os.Process.myPid());
//                                        System.exit(1);
                                    logout();
                                }
                            })

                    .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        } else if (id == R.id.settings) {
            Intent intent1 = new Intent( this, SettingsActivity.class );
            this.startActivity( intent1 );
            finish();
        } else if (id == R.id.nav_transaction_records){
            startActivity(new Intent(getApplicationContext(),TransactionHistoryActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

                String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","FilteredCategoryActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    transactionsIntent.putExtra("title",title);
                    transactionsIntent.putExtra("file_type",fileType);
                    transactionsIntent.putExtra("tag",tag);
                    transactionsIntent.putExtra("postType",category);
                    startActivity(transactionsIntent);
                    overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
                    viewPostIntent.putExtra("file_key",key);
                    viewPostIntent.putExtra("postType",category);
                    startActivity(viewPostIntent);
                } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                    Intent viewPostIntent = new Intent(getApplicationContext(), ViewPostActivity.class);
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

    public void openAnnouncement(final String key){
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_announcements)).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent viewAnnouncementIntent = new Intent(getApplicationContext(),ViewAnnouncementActivity.class);
                viewAnnouncementIntent.putExtra("file_key",key);
                startActivity(viewAnnouncementIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
            public boolean onQueryTextSubmit(final String txtSearch) {

                mProgress.setCanceledOnTouchOutside(false);
                mProgress.setMessage(getString(R.string.info_fetching_results));
                mProgress.show();
                final Query mParent = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all)); //upper child needed here

                mParent.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSearch = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_all));
                        mSearch.orderByChild("Title").startAt(txtSearch.toUpperCase()).endAt(txtSearch.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()){
                                    mProgress.dismiss();
//                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No results found!", Snackbar.LENGTH_LONG);
//                                    snackbar.show();
                                    Toast.makeText(HomeActivity.this, R.string.info_no_results, Toast.LENGTH_SHORT).show();
                                } else {
                                    mProgress.dismiss();
                                    Intent resultsActivity = new Intent(getApplicationContext(), SearchPostActivity.class);
                                    resultsActivity.putExtra("query",txtSearch.toUpperCase());
                                    resultsActivity.putExtra("activityFrom","HomeActivity"); //added this section
                                    startActivity(resultsActivity);
                                    overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
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

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        };

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    public void initCloudPackets(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        home = findViewById(R.id.home);
        mDrawerLayout.setEnabled(false);
        home.setEnabled(false);
        SocketsHandler.ARG_SOCKET_REQUIRED_VALUE=0;
        FirebaseDatabase.getInstance().getReference().child(node).child(socket).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if(!SocketsHandler.validateSockets(Integer.parseInt(String.valueOf(dataSnapshot.getValue())))) {
                        finish();
                    } else {
                        mDrawerLayout.setEnabled(true);
                        home.setEnabled(true);
                    }
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void doThis(View view) {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean executeCommand() {
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue " + mExitValue);
            if (mExitValue == 0) {

                return true;

            } else {

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.error_network_access_unavailable), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception:" + ignore);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        }
        return false;
    }

    private void sendRegistrationToServer(final String token){
//        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference mToken = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("msg_token");
        mToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getValue().equals("")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(mAuth.getCurrentUser().getUid())
                            .child("msg_token")
                            .setValue(token);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( HomeActivity.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                Log.d(TAG, "initFCM: token: " + token);
                sendRegistrationToServer(token);
            }
        });
    }

    public boolean isFacebookAuthenticated() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private boolean isGoogleSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null;
    }

    private void signOut() {
        if (isGoogleSignedIn()){
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
        }
    }

    public void setNulls(){
        mAnnouncementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtAnnouncements.setText(R.string.info_no_announcements_yet);
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPromos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtPromoted.setText(R.string.info_no_promotions_yet);
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPostsAll.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount()==0){
                    txtPosts.setText(R.string.info_no_posts_yet);
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void printKeyHash(){
        try{
            PackageInfo info = getPackageManager().getPackageInfo(getString(R.string.app_package_name), PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures){
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                Log.d("KEY_HASH", Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private void support() {
        mLinks = FirebaseDatabase.getInstance().getReference().child("Links");
        mLinks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLink = String.valueOf(dataSnapshot.child("support").getValue());
                Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse(mLink));
                HomeActivity.this.startActivity( intent1 );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void tandc() {
        mLinks = FirebaseDatabase.getInstance().getReference().child("Links");
        mLinks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLink = String.valueOf(dataSnapshot.child("TandCs").getValue());
                Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse(mLink));
                HomeActivity.this.startActivity( intent1 );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void us() {
        mLinks = FirebaseDatabase.getInstance().getReference().child("Links");
        mLinks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mLink = String.valueOf(dataSnapshot.child("Us").getValue());
                Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse(mLink));
                HomeActivity.this.startActivity( intent1 );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkMaintenanceStatus(final Context ctx){
        FirebaseDatabase.getInstance().getReference().child("App").child("System").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("MaintenanceState").getValue().equals("true")){
                        Intent intent = new Intent(ctx,MaintenanceActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setAppVersion() {
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
            vCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("app_ver").setValue(String.valueOf(vCode));
    }

    private void checkAppUpdate(){
        FirebaseDatabase.getInstance().getReference().child("App").child("Version").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(getPackageName(), 0);
                    vCode = pInfo.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (dataSnapshot.exists()){
                    int code = Integer.parseInt(String.valueOf(dataSnapshot.child("VersionCode").getValue()));
                    String force = String.valueOf(dataSnapshot.child("ForceUpdate").getValue());
                    if (code>vCode && force.equals("true")){
                        finish();
                        System.exit(0);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public boolean checkNetworkState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public void checkUserEnabledStatus(){
        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || dataSnapshot.getValue().equals("disabled")){
                    Toast.makeText(HomeActivity.this, R.string.info_user_disabled, Toast.LENGTH_LONG).show();
                    logout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getMessageCount(){
        NewItemsCounter.initValues();
        NewItemsCounter.mMessages.orderByChild("seen").equalTo("false").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()>0){
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.setNavigationItemSelectedListener(HomeActivity.this);
                    Menu menuNav = navigationView.getMenu();
                    MenuItem messageItem = menuNav.findItem(R.id.messages);
                    messageItem.setTitle("("+dataSnapshot.getChildrenCount()+") unread messages");
                    badgeToggle = new BadgeDrawerToggle(HomeActivity.this,(DrawerLayout) findViewById(R.id.drawer_layout),
                            (Toolbar)findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                    badgeToggle.setBadgeText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*v1.0.5 enhancement 0002*/
    public void initCardLimits(){
        FirebaseDatabase.getInstance().getReference().child("Values").child("Defaults").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("HomeAnnouncementsCardsLimit").exists() && !dataSnapshot.child("HomeAnnouncementsCardsLimit").getValue().equals("")){
                        announcementLimit = Integer.parseInt(dataSnapshot.child("HomeAnnouncementsCardsLimit").getValue().toString());
                    }
                    if (dataSnapshot.child("HomePromosCardsLimit").exists() && !dataSnapshot.child("HomePromosCardsLimit").getValue().equals("")){
                        promosLimit = Integer.parseInt(dataSnapshot.child("HomePromosCardsLimit").getValue().toString());
                    }
                    if (dataSnapshot.child("HomePostsCardsLimit").exists() && !dataSnapshot.child("HomePostsCardsLimit").getValue().equals("")){
                        postsLimit = Integer.parseInt(dataSnapshot.child("HomePostsCardsLimit").getValue().toString());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void logout(){
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        signOut();
        Intent loginIntent = new Intent(HomeActivity.this,SelectLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loginIntent.putExtra("homeIntent","HomeActivity");
        startActivity(loginIntent);
        finish();
    }

}
