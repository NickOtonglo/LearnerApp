//package pesh.mori.learnerapp;
//
//import android.app.ProgressDialog;
//import android.app.SearchManager;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.Signature;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.annotation.NonNull;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.support.design.widget.NavigationView;
//import android.support.v4.view.GravityCompat;
//import android.support.v4.widget.DrawerLayout;
//import android.support.v7.app.ActionBarDrawerToggle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.SearchView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.facebook.AccessToken;
//import com.facebook.Profile;
//import com.facebook.login.LoginManager;
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;
//import com.squareup.picasso.Picasso;
//
//import java.io.IOException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Arrays;
//import java.util.List;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//public class NewHome extends AppCompatActivity
//        implements NavigationView.OnNavigationItemSelectedListener {
//
//    private FirebaseAuth mAuth;
//    private Button diy;
//    private Button school;
//    private TextView preference;
//    private Menu menu;
//    private DrawerLayout mDrawerlayout;
//    private FrameLayout home;
//    private ActionBarDrawerToggle mToggle;
//
//    private RecyclerView mRecycler;
//
//    private RecyclerView mRecycler2;
//
//    private RecyclerView mRecycler3;
//
//    private DatabaseReference mFiles;
//    private StorageReference mStorage;
//
//    private Query mParent;
//
//    private static CustomProgressBar progressBar = new CustomProgressBar();
//
//    private static int TIME_OUT = 2000;
//
//    private static int TIME_OUT2 = 5000;
//
//    private DatabaseReference mUsers;
//
//    private CircleImageView headerImage;
//    private TextView txtName,txtTokens;
//
//    private ProgressDialog mProgress;
//
//    private Query mSearch;
//
//    private StorageReference sStorage;
//
//    private Uri mFileUri = null;
//
//    public String tokenBalance;
//
//    private String TAG = "HomeActivity",node="Texts",socket="BidSocket";
//    private GoogleSignInClient mGoogleSignInClient;
//
//    public NewHome() {
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_new_home);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        initCloudPackets();
//        mAuth = FirebaseAuth.getInstance();
//        if (mAuth.getCurrentUser() == null) {
//            mAuth.signOut();
//            Intent loginIntent = new Intent(NewHome.this, SelectLoginActivity.class);
//            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(loginIntent);
//            finish();
//        }
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        initFCM();
//
////        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
////        setSupportActionBar(toolbar);
//
//
//        diy = (Button) findViewById(R.id.diy);
//        school = (Button) findViewById(R.id.school);
//
//        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
//        mFiles.keepSynced(true);
//        mStorage = FirebaseStorage.getInstance().getReference().child("Files");
//        mParent = FirebaseDatabase.getInstance().getReference().child("Files");
////
////
////
//        mRecycler = (RecyclerView) findViewById(R.id.layout_recycler_mydownloads);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//        mRecycler.setHasFixedSize(true);
//        mRecycler.setLayoutManager(layoutManager);
//
//        mRecycler2 = (RecyclerView) findViewById(R.id.layout_recycler_mydownload);
//        mRecycler2.setHasFixedSize(true);
//        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
//        layoutManager2.setReverseLayout(true);
//        layoutManager2.setStackFromEnd(true);
//        mRecycler2.setLayoutManager(layoutManager2);
//
//        mRecycler3 = (RecyclerView) findViewById(R.id.layout_recycler_mydownloa);
//        mRecycler3.setHasFixedSize(true);
//        LinearLayoutManager layoutManager3 = new LinearLayoutManager(this);
//        layoutManager3.setReverseLayout(true);
//        layoutManager3.setStackFromEnd(true);
//        mRecycler3.setLayoutManager(layoutManager3);
//        //navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
//
//        final TextView txtName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.txt_header_name);
//        final TextView txtTokenBal = navigationView.getHeaderView(0).findViewById(R.id.txt_header_tokens);
//        final CircleImageView circleImageView = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.img_main_header_profile);
//
//        //Get token balance
//        FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                tokenBalance = String.valueOf(dataSnapshot.child("current_balance").getValue());
//                if (tokenBalance.equals("") || (tokenBalance.equals("0")) || !dataSnapshot.exists()){
//                    txtTokenBal.setText("0.00");
//                } else {
//                    Double bal = Double.parseDouble(tokenBalance);
//                    txtTokenBal.setText(String.format("%.2f",bal));
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        sStorage = FirebaseStorage.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
//        mUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
//        mUsers.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
//                if (account == null) {
//                    txtName.setText("Hi there " +String.valueOf(dataSnapshot.child("fname").getValue() +"! Your token balance is:"));
//                    String profilePicture = String.valueOf(dataSnapshot.child("profile_picture").getValue());
//                    if (!profilePicture.equals("")) {
//                        mFileUri = Uri.parse(profilePicture);
//                        Picasso.with(getApplicationContext()).load(mFileUri).into(circleImageView);
//                    }
//                    if (isFacebookAuthenticated()){
//                        Profile profile = Profile.getCurrentProfile();
//                        String facebookProfilePic = profile.getProfilePictureUri(300,300).toString();
//                        Picasso.with(getApplicationContext()).load(facebookProfilePic).into(circleImageView);
//                        txtName.setText("Hi there " +profile.getFirstName()+"! Your token balance is:");
//                    }
//                }
//                else if (account != null) {
//                    Uri accountPictureUri = account.getPhotoUrl();
//                    Picasso.with(getApplicationContext()).load(String.valueOf(accountPictureUri)).into(circleImageView);
//                    txtName.setText("Hi there " +account.getDisplayName()+"! Your token balance is:");
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        executeCommand();
//
//
//        FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder>(
//                File.class,
//                R.layout.card_mydownloadss,
//                HomeActivity.DownloadsViewHolder.class,
//                mFiles.orderByChild("timestamp").limitToLast(5)
//        ) {
//            @Override
//            protected void populateViewHolder(final HomeActivity.DownloadsViewHolder viewHolder, final File model, int position) {
//                final String fileKey = getRef(position).getKey();
//
//                mFiles.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
//                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
//                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
//                        if (file_type.equals("audio")) {
//                            viewHolder.setAudioImage();
//                        } else if (file_type.equals("video")) {
//                            viewHolder.setVideoImage();
//                        } else if (file_type.equals("image")) {
//                            viewHolder.setImageImage(getApplicationContext(),model.getFile());
//                        }
//                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                checkOwnershipStatus(fileKey,"Files");
//                            }
//                        });
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
//
//
//
//        FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder> firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder>(
//                File.class,
//                R.layout.card_mydownloadss,
//                HomeActivity.DownloadsViewHolder.class,
//                mFiles.orderByChild("timestamp").limitToFirst(5)
//        ) {
//            @Override
//            protected void populateViewHolder(final HomeActivity.DownloadsViewHolder viewHolder, final File model, int position) {
//                final String fileKey = getRef(position).getKey();
//
//                mFiles.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
//                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
//                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
//                        if (file_type.equals("audio")) {
//                            viewHolder.setAudioImage();
//                        } else if (file_type.equals("video")) {
//                            viewHolder.setVideoImage();
//                        } else if (file_type.equals("image")) {
//                            viewHolder.setImageImage(getApplicationContext(),model.getFile());
//                        }
//                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                checkOwnershipStatus(fileKey,"Files");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        };
//        mRecycler2.setAdapter(firebaseRecyclerAdapter2);
//
//        FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder> firebaseRecyclerAdapter3 = new FirebaseRecyclerAdapter<File, HomeActivity.DownloadsViewHolder>(
//                File.class,
//                R.layout.card_mydownloadss,
//                HomeActivity.DownloadsViewHolder.class,
//                mFiles.orderByChild("timestamp").limitToLast(5)
//        ) {
//            @Override
//            protected void populateViewHolder(final HomeActivity.DownloadsViewHolder viewHolder, final File model, int position) {
//                final String fileKey = getRef(position).getKey();
//
//                mFiles.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        viewHolder.setTitle(String.valueOf(dataSnapshot.child(fileKey).child("title").getValue()));
//                        viewHolder.setTimestamp(String.valueOf(dataSnapshot.child(fileKey).child("timestamp").getValue()));
//                        String file_type = String.valueOf(dataSnapshot.child(fileKey).child("file_type").getValue());
//                        if (file_type.equals("audio")) {
//                            viewHolder.setAudioImage();
//                        } else if (file_type.equals("video")) {
//                            viewHolder.setVideoImage();
//                        } else if (file_type.equals("image")) {
//                            viewHolder.setImageImage(getApplicationContext(),model.getFile());
//                        }
//                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                checkOwnershipStatus(fileKey,"Files");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        };
//        mRecycler3.setAdapter(firebaseRecyclerAdapter3);
//
//
//    }
//
//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
//        // Handle navigation view item clicks here.
//        int id = item.getItemId();
//
//        if (id == R.id.buy) {
//            Intent intent1 = new Intent( this, TokensActivity.class );
//            this.startActivity( intent1 );
//        } else if (id == R.id.myfiles) {
//            Intent intent1 = new Intent( this, MyFilesActivity.class );
//            this.startActivity( intent1 );
//
//        } else if (id == R.id.social) {
//            Intent intent1 = new Intent( Intent.ACTION_VIEW, Uri.parse("http:moripesh.com"));
//            this.startActivity( intent1 );
//
//        } else if (id == R.id.messages) {
//            Intent intent1 = new Intent( this, MessagesActivity.class );
//            this.startActivity( intent1 );
//
//        } else if (id == R.id.logout) {
//            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//            alertDialogBuilder.setTitle("Sign out?");
//            alertDialogBuilder
//                    .setCancelable(false)
//                    .setPositiveButton("Yes",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
////                                        moveTaskToBack(true);
////                                        android.os.Process.killProcess(android.os.Process.myPid());
////                                        System.exit(1);
//                                    mAuth.signOut();
//                                    LoginManager.getInstance().logOut();
//                                    Intent loginIntent = new Intent(NewHome.this,SelectLoginActivity.class);
//                                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(loginIntent);
//                                    finish();
//                                }
//                            })
//
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//
//                            dialog.cancel();
//                        }
//                    });
//
//            AlertDialog alertDialog = alertDialogBuilder.create();
//            alertDialog.show();
//
//        } else if (id == R.id.settings) {
//            Intent intent1 = new Intent( this, SettingsActivity.class );
//            this.startActivity( intent1 );
//
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }
//    public void checkOwnershipStatus(final String key,final String category){
//        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
//                String itemPrice = String.valueOf(dataSnapshot.child("price").getValue());
//                String author = String.valueOf(dataSnapshot.child("author").getValue());
//                String title = String.valueOf(dataSnapshot.child("title").getValue());
//                String fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
//                Double price = Double.parseDouble(itemPrice);
//                List<String> listOwners = Arrays.asList(owners.split("\\s*,\\s*"));
//                if (category.equals("Files")){
//                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
//                        transactionsIntent.putExtra("file_key",key);
//                        transactionsIntent.putExtra("outgoing_intent","mydownloads");
//                        transactionsIntent.putExtra("item_price",itemPrice);
//                        transactionsIntent.putExtra("title",title);
//                        transactionsIntent.putExtra("file_type",fileType);
//                        startActivity(transactionsIntent);
//                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
//                        viewFileIntent.putExtra("file_key",key);
//                        startActivity(viewFileIntent);
//                    }  else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
//                        viewFileIntent.putExtra("file_key",key);
//                        startActivity(viewFileIntent);
//                    }
//                }else if (category.equals("DIY")){
//                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
//                        transactionsIntent.putExtra("file_key",key);
//                        transactionsIntent.putExtra("outgoing_intent","DownloadDiyActivity");
//                        transactionsIntent.putExtra("item_price",itemPrice);
//                        transactionsIntent.putExtra("title",title);
//                        transactionsIntent.putExtra("file_type",fileType);
//                        startActivity(transactionsIntent);
//                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
//                        viewFileIntent.putExtra("file_key",key);
//                        startActivity(viewFileIntent);
//                    } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
//                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
//                        viewFileIntent.putExtra("file_key",key);
//                        startActivity(viewFileIntent);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search, menu);
//
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//
//        if (null != searchView){
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            searchView.setIconifiedByDefault(false);
//        }
//
//        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(final String txtSearch) {
//
//                mProgress.setCanceledOnTouchOutside(false);
//                mProgress.setMessage("Fetching results...");
//                mProgress.show();
//                final Query mParent = FirebaseDatabase.getInstance().getReference().child("AllPosts"); //upper child needed here
//
//                mParent.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        mSearch = FirebaseDatabase.getInstance().getReference().child("AllPosts");
//                        mSearch.orderByChild("Title").startAt(txtSearch.toUpperCase()).endAt(txtSearch.toUpperCase()+"\uf8ff").addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                if (!dataSnapshot.exists()){
//                                    mProgress.dismiss();
////                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "No results found!", Snackbar.LENGTH_LONG);
////                                    snackbar.show();
//                                    Toast.makeText(NewHome.this, "No results", Toast.LENGTH_SHORT).show();
//                                } else {
//                                    mProgress.dismiss();
//                                    Intent resultsActivity = new Intent(getApplicationContext(),DownloadsList.class);
//                                    resultsActivity.putExtra("query",txtSearch.toUpperCase());
//                                    resultsActivity.putExtra("activityFrom","HomeActivity"); //added this section
//
//                                    startActivity(resultsActivity);
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return true;
//            }
//        };
//
//        searchView.setOnQueryTextListener(queryTextListener);
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public void initCloudPackets(){
//        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        home = findViewById(R.id.home);
//        mDrawerlayout.setEnabled(false);
//        home.setEnabled(false);
//        HandleSockets.ARG_SOCKET_REQUIRED_VALUE=0;
//        FirebaseDatabase.getInstance().getReference().child(node).child(socket).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(!HandleSockets.validateSockets(Integer.parseInt(String.valueOf(dataSnapshot.getValue())))) {
//                    finish();
//                } else {
//                    mDrawerlayout.setEnabled(true);
//                    home.setEnabled(true);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//    public void didTapButton(View view) { //something wrong with DIY, needs debugging...
//        if (view==diy){
//            Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
//            diy.startAnimation(myanim);
//            progressBar.show(this,"Accessing...");
//
//            new Handler().postDelayed(new Runnable() {
//
//
//                @Override
//                public void run() {
//                    //Toast.makeText(getApplicationContext(), "Learn and Earn", Toast.LENGTH_LONG).show();
//                    Intent i = new Intent(NewHome.this,CategoryActivity.class);
//                    startActivity(i);
//                    progressBar.getDialog().dismiss();
//
//                }
//            }, TIME_OUT);
//        }
//
//    }
//
//    public void TapButton(View view) {
//        if (view==school){
//            Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
//            school.startAnimation(myanim);
//            progressBar.show(this,"Accessing...");
//
//            new Handler().postDelayed( new Runnable() {
//
//
//                @Override
//                public void run() {
//                    //Toast.makeText(getApplicationContext(), "Learn and Earn", Toast.LENGTH_LONG).show();
//                    Intent i = new Intent(NewHome.this,InstitutionActivity.class);
//                    startActivity(i);
//                    progressBar.getDialog().dismiss();
//
//                }
//            }, TIME_OUT);
//        }
//
//    }
//    public void doThis(View view) {
//        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
//    }
//
//    @Override
//    public void onPointerCaptureChanged(boolean hasCapture) {
//
//    }
//
//    public static class DownloadsViewHolder extends RecyclerView.ViewHolder{
//        View mView;
//
//        public DownloadsViewHolder(View itemView) {
//            super(itemView);
//            mView = itemView;
//        }
//
//        public void setTitle(String title){
//            TextView txtTitle = (TextView)mView.findViewById(R.id.txt_title_downl) ;
//            txtTitle.setText(title);
//        }
//        public void setTimestamp(String timestamp){
//            TextView txtTime = (TextView)mView.findViewById(R.id.txt_time_downl);
//            txtTime.setText(timestamp);
//        }
//        //        public void setImage(Context ctx,String image){
////            ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_upload);
////            Picasso.with(ctx).load(image).into(imgUpload);
////        }
//        public void setAudioImage(){
//            ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
//            imgUpload.setImageResource(R.mipmap.audio_preview);
//        }
//        public void setVideoImage(){
//            ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
//            imgUpload.setImageResource(R.mipmap.video_preview);
//        }
//        public void setImageImage(Context ctx,String imageThumb){
//            ImageView imgUpload = (ImageView)mView.findViewById(R.id.img_downl);
//            Picasso.with(ctx).load(imageThumb).into(imgUpload);
//        }
//        public void setNull(){
//            TextView txtEmpty = mView.findViewById(R.id.txt_upload_empty);
//            txtEmpty.setText("No Files to Display");
//        }
//    }
//
//    private boolean executeCommand() {
//        System.out.println("executeCommand");
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
//            int mExitValue = mIpAddrProcess.waitFor();
//            System.out.println(" mExitValue " + mExitValue);
//            if (mExitValue == 0) {
//
//                return true;
//
//            } else {
//
//                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Network access unavailable!", Snackbar.LENGTH_LONG);
//                snackbar.show();
//            }
//        } catch (InterruptedException ignore) {
//            ignore.printStackTrace();
//            System.out.println(" Exception:" + ignore);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(" Exception:" + e);
//        }
//        return false;
//    }
//
//    private void sendRegistrationToServer(String token){
//        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
//        FirebaseDatabase.getInstance().getReference()
//                .child("Users")
//                .child(mAuth.getCurrentUser().getUid())
//                .child("msg_token")
//                .setValue(token);
//    }
//
//    private void initFCM(){
//        String token = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "initFCM: token: " + token);
//        sendRegistrationToServer(token);
//    }
//
//    public boolean isFacebookAuthenticated() {
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        return accessToken != null;
//    }
//
//    private boolean isGoogleSignedIn() {
//        return GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null;
//    }
//
//    private void signOut() {
//        if (isGoogleSignedIn()){
//            mGoogleSignInClient.signOut()
//                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            finish();
//                        }
//                    });
//        }
//    }
//
//    private void printKeyHash(){
//        try{
//            PackageInfo info = getPackageManager().getPackageInfo("pesh.mori.learnerapp", PackageManager.GET_SIGNATURES);
//            for (Signature signature:info.signatures){
//                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
//                messageDigest.update(signature.toByteArray());
//                Log.d("KEY_HASH", Base64.encodeToString(messageDigest.digest(),Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
