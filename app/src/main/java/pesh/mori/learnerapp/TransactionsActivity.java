package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class TransactionsActivity extends AppCompatActivity {

    private TextView txtTitle,txtDescription,txtTime,txtPrice,txtBidding,txtTag,txtInstitution,txtDept,txtCourse;
    private TextView txtAuthor;
    private CircleImageView imgAuthor;
    private ProgressBar mProgressBar;
    private ImageView imageView,staticIcon;
    private VideoView audioView,videoView;
    private MediaPlayer mediaPlayer;
    private Uri mUri = null;
    private FirebaseAuth mAuth;
    private String fileKey;
    private DatabaseReference mPosts;
    private LinearLayout layoutImage,layoutAudio,layoutVideo,layoutDoc;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private String fileType,filePath;
    private ProgressBar progressBuffer;
    private ImageView btnPlayVideo;
    private ProgressBar progressBufferAudio;
    private ImageView btnPlayAudio;
    private String itemKey,incomingIntent;
    private String itemPrice;
    private String fType,fTitle,postType;
    private LinearLayout layoutNonCoursework,layoutCoursework;
    private Button btnTransactCancel,btnTransactPurchase,btnTransactBid;
    Calendar calendar;
    private SimpleDateFormat sdf;
    private String childRef;
    private String mBidPushKey;
    private String mMessagePushKey;
    private MediaController mediaController;
    static boolean active = false;
    private Double transactionChargeRate=0.0;
    private Double amount,d;

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
        setContentView(R.layout.activity_transactions);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtTitle = findViewById(R.id.txt_view_title);
        txtDescription = findViewById(R.id.txt_view_description);
        txtTime = findViewById(R.id.txt_view_time);
        txtPrice = findViewById(R.id.txt_view_price);
        txtBidding = findViewById(R.id.txt_view_bid);
        txtTag = findViewById(R.id.txt_view_tag);
        txtInstitution = findViewById(R.id.txt_view_institution);
        txtDept = findViewById(R.id.txt_view_faculty);
        txtCourse = findViewById(R.id.txt_view_course);

        txtAuthor = findViewById(R.id.txt_author);
        imgAuthor = findViewById(R.id.img_author);
        mProgressBar = findViewById(R.id.progress_bar);

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);
        mProgress.setCancelable(false);

        fileKey = getIntent().getExtras().getString("file_key");
        postType = getIntent().getExtras().getString("postType");

        Log.d("LOG_postType",postType);

        layoutNonCoursework = findViewById(R.id.layout_non_coursework);
        layoutCoursework = findViewById(R.id.layout_coursework);

        mAuth = FirebaseAuth.getInstance();

        mediaPlayer = new MediaPlayer();
        mediaController = new MediaController(this);

        //AudioControls
        progressBufferAudio = findViewById(R.id.buffer_progress_view_file_audio);
        btnPlayAudio = findViewById(R.id.btn_view_file_time_play_audio);
        staticIcon = findViewById(R.id.btn_view_file_audio_static);

        //VideoControls
        progressBuffer = findViewById(R.id.buffer_progress_view_file);
        btnPlayVideo = findViewById(R.id.btn_view_file_time_play);

        imageView = findViewById(R.id.btn_view_file_select_image);
        layoutImage = findViewById(R.id.view_file_layout_1);
        audioView = findViewById(R.id.img_view_file_play_audio);
        layoutAudio = findViewById(R.id.view_file_layout_2);
        videoView = findViewById(R.id.img_view_file_play_video);
        layoutVideo = findViewById(R.id.view_file_layout_3);
        layoutDoc = findViewById(R.id.view_file_layout_4);

        btnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAudio();

            }
        });

        btnPlayVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();

            }
        });

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        FirebaseDatabase.getInstance().getReference().child("Values").child(getString(R.string.firebase_ref_values_transactions))
                .child(getString(R.string.firebase_ref_values_transactions_fees_sell_content)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactionChargeRate = Double.parseDouble(dataSnapshot.getValue().toString());
                checkDefaultAccount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        itemKey = getIntent().getExtras().getString("file_key");
        itemPrice = getIntent().getExtras().getString("item_price");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");
        fTitle = getIntent().getExtras().getString("title");
        fType = getIntent().getExtras().getString("file_type");

        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            childRef = getString(R.string.firebase_ref_posts_type_1);
            layoutCoursework.setVisibility(View.VISIBLE);
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            childRef = getString(R.string.firebase_ref_posts_type_2);
            layoutNonCoursework.setVisibility(View.VISIBLE);
        }

        FirebaseDatabase.getInstance().getReference().child(childRef).child(itemKey).child("biddable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue().toString().equals("yes")){
                    btnTransactBid.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAuth = FirebaseAuth.getInstance();

        //txtTransactPrompt = findViewById(R.id.txtTransactPrompt);
        btnTransactCancel = findViewById(R.id.btnTransactCancel);
        btnTransactPurchase = findViewById(R.id.btnTransactPurchase);
        btnTransactBid = findViewById(R.id.btnTransactBid);
        //txtTransactPrompt.setText("You do not own this item. Would you like to purchase it for "+itemPrice+" token(s)?");

        btnTransactCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnTransactPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle(getString(R.string.title_purchase_item))
                        .setMessage(R.string.confirm_are_you_sure_you_want_to_purchase_this_item)
                        .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkPendingBid();
                            }
                        })
                        .setNeutralButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                if (!(TransactionsActivity.this).isFinishing()){
                    mAlert.show();
                }
            }
        });
        btnTransactBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String bidState = String.valueOf (dataSnapshot.child(itemKey).child("biddable").getValue());
                        if (bidState.equals("yes")){
                            final Intent bidIntent = new Intent(TransactionsActivity.this, BidActivity.class);
                            bidIntent.putExtra("buyerId", mAuth.getCurrentUser().getUid() );
                            bidIntent.putExtra("itemKey", itemKey);
                            bidIntent.putExtra("itemPrice", itemPrice);
                            bidIntent.putExtra("outgoing_intent",incomingIntent);
                            bidIntent.putExtra("postType",postType);
                            DatabaseReference mSource = FirebaseDatabase.getInstance().getReference().child(childRef);
                            mSource.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String authorId = String.valueOf(dataSnapshot.child("author").getValue());
                                    mBidPushKey = String.valueOf(FirebaseDatabase.getInstance().getReference().child("Bids").child(authorId).push().getKey());
                                    mMessagePushKey = String.valueOf(FirebaseDatabase.getInstance().getReference().child("Messages").child(authorId).push().getKey());
                                    bidIntent.putExtra("authorId",authorId);
                                    bidIntent.putExtra("mBidPushKey",mBidPushKey);
                                    bidIntent.putExtra("mMessagePushKey",mMessagePushKey);
                                    startActivity(bidIntent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        } else if (bidState.equals("no")){

                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.info_this_item_is_not_biddable, Snackbar.LENGTH_LONG);
                            snackbar.show();


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        loadPostDetails();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        FirebaseDatabase.getInstance().getReference().child(childRef).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (String.valueOf(dataSnapshot.child(fileKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                    getMenuInflater().inflate(R.menu.view_post_context_menu_author, menu);
                } else {
                    getMenuInflater().inflate(R.menu.view_post_context_menu, menu);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.report_post:
                Intent fabIntent = new Intent(getApplicationContext(),ReportPostActivity.class);
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("outgoing_intent","ViewFileActivity");
                startActivity(fabIntent);
                return true;
            case R.id.view_author:
                viewAuthor(fileKey,childRef);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void viewAuthor(final String key, final String category){
        FirebaseDatabase.getInstance().getReference().child(category).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Intent fabIntent = new Intent(getApplicationContext(),ViewAuthorActivity.class);
                fabIntent.putExtra("post_key",fileKey);
                fabIntent.putExtra("author_id", String.valueOf(dataSnapshot.child("author").getValue()));
                FirebaseDatabase.getInstance().getReference().child("Users").child(String.valueOf(dataSnapshot.child("author").getValue())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fabIntent.putExtra("author_name", String.valueOf(dataSnapshot.child("username").getValue()));
                        fabIntent.putExtra("outgoing_intent","TransactionsActivity");
                        startActivity(fabIntent);
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

    /*v1.0.4 bug fix 00006*/
    public void checkPendingBid(){
        mProgress.setMessage(getString(R.string.info_please_wait));
        if (!mProgress.isShowing() && !(TransactionsActivity.this).isFinishing()){
            mProgress.show();
        }
        final String[] pushKey = {""};
        final DatabaseReference mBids = FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(mAuth.getCurrentUser().getUid());
        mBids.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mBids.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child("status").getValue().equals("pending")){
                                    mAlert.setTitle(R.string.title_pending_bid)
                                            .setMessage(R.string.info_bid_pending_wait_for_author_to_review)
                                            .setPositiveButton(getString(R.string.option_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .show();
                                    if (mProgress.isShowing() && !(TransactionsActivity.this).isFinishing()){
                                        mProgress.dismiss();
                                    }
                                } else if (dataSnapshot.child("status").getValue().equals("ready")){
                                    createFinancialAccount();
                                }
                            } else {createFinancialAccount();}
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {createFinancialAccount();}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage(getString(R.string.info_your_financial_account_is_being_configured));
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    if (!(TransactionsActivity.this).isFinishing()){
                        mProgress.show();
                    }
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid());
                    mMonetaryAccount.child("email").setValue(mAuth.getCurrentUser().getEmail());
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(TransactionsActivity.this, getString(R.string.info_setup_complete), Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransactionsActivity.this, getString(R.string.error_an_error_occurred_while_configuring_your_account)+": "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage(getString(R.string.info_your_financial_account_is_being_configured));
        if (!mProgress.isShowing() && !(TransactionsActivity.this).isFinishing()){
            mProgress.show();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid());
                    mMonetaryAccount.child("email").setValue(mAuth.getCurrentUser().getEmail());
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(TransactionsActivity.this, getString(R.string.info_setup_complete), Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
                checkBalance(itemKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TransactionsActivity.this, getString(R.string.error_an_error_occurred_while_configuring_your_account)+": "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkBalance(final String key){
        mProgress.setMessage(getString(R.string.info_please_wait));
        if (!mProgress.isShowing() && !(TransactionsActivity.this).isFinishing()){
            mProgress.show();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Double buyerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                FirebaseDatabase.getInstance().getReference().child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Double price = Double.parseDouble(String.valueOf(dataSnapshot.child(key).child("price").getValue()));

                        if (buyerBalance<price){
                            Toast.makeText(TransactionsActivity.this, getString(R.string.info_your_balance_is_insufficient_by)+" "+(price-buyerBalance)+" tokens", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        } else {
                            getSellerDetails(itemKey,buyerBalance,price);
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
    public void getSellerDetails(final String key, final Double bal, final Double amt){
        FirebaseDatabase.getInstance().getReference().child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String sellerId = String.valueOf(dataSnapshot.child(key).child("author").getValue());
                final Double price = Double.parseDouble(String.valueOf(dataSnapshot.child(key).child("price").getValue()));
                final String[] sellerEmail = new String[1];
                FirebaseDatabase.getInstance().getReference().child("Users").child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sellerEmail[0] = String.valueOf(dataSnapshot.child("email").getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            if (!dataSnapshot.exists()){
                                DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(sellerId);
                                mMonetaryAccount.child("email").setValue(sellerEmail[0]);
                                mMonetaryAccount.child("previous_balance").setValue(0.00);
                                mMonetaryAccount.child("current_balance").setValue(0.00);
                                mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                            }

                            Double sellerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                            debitBuyer(bal,amt,sellerBalance,price,sellerId);
                        }catch (NumberFormatException e){
                            mProgress.dismiss();
                            Toast.makeText(TransactionsActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
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

    public double debitBuyer(Double bal, Double amt, Double sbal, Double samt, String sellerId){
        Double previousBalance = bal;
        if (bal>=amt){
            bal = bal-amt;

            DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid());
            mMonetaryAccount.child("current_balance").setValue(bal);
            mMonetaryAccount.child("previous_balance").setValue(bal+amt);
            final String debitTime = sdf.format(Calendar.getInstance().getTime());
//            getSellerDetails(fileKey);
            creditSeller(sbal,samt,sellerId,mAuth.getCurrentUser().getUid(),previousBalance,bal,debitTime);
            addBuyerToList();
        }
        return bal;
    }

    public double creditSeller(Double bal, Double amt, String sellerId, String buyerId, Double buyerPBal, Double buyerNBal, String debitTime){
        amount = (transactionChargeRate/100.0)*amt;
        amt = amt-(amount);
        DatabaseReference mDefault = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(getString(R.string.firebase_ref_monetary_app_default_name));
        mDefault.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mDefault.child("email").setValue("");
                    mDefault.child("previous_balance").setValue(d);
                    mDefault.child("current_balance").setValue(amount);
                    mDefault.child("status").setValue("enabled");
                } else {
                    d = Double.parseDouble(dataSnapshot.child("current_balance").getValue().toString());
                    mDefault.child("current_balance").setValue(d +(amount));
                    mDefault.child("previous_balance").setValue(d);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Double previousBalance = bal;
        bal = bal+amt;

        DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(sellerId);
        mMonetaryAccount.child("current_balance").setValue(bal);
        mMonetaryAccount.child("previous_balance").setValue(bal-amt);

        final String creditTime = sdf.format(Calendar.getInstance().getTime());

        Double finalBal = bal;
        Double finalAmt = amt;
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary))
                .child(getString(R.string.firebase_ref_monetary_app_default_name)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double prev = Double.parseDouble(dataSnapshot.child("previous_balance").getValue().toString());

                createRecord(buyerId,sellerId,buyerPBal,previousBalance,prev,buyerNBal,
                        finalBal,amount+prev, finalAmt +amount,debitTime,creditTime);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return bal;
    }

    public void addBuyerToList(){
        final DatabaseReference mOwnersList = FirebaseDatabase.getInstance().getReference().child(childRef).child(itemKey);
        mOwnersList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String owners = String.valueOf(dataSnapshot.child("owners").getValue());
                List<String> ownersList = new LinkedList<String>(Arrays.asList(owners.split("\\s*,\\s*")));
                ownersList.add(String.valueOf(mAuth.getCurrentUser().getUid()));
                final String postOwners = TextUtils.join(",",ownersList);
                mOwnersList.child("owners").setValue(postOwners);

                DatabaseReference mOwnedItems = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_owned)).child(mAuth.getCurrentUser().getUid()).child(itemKey);
                mOwnedItems.child("ItemId").setValue(itemKey);
                mOwnedItems.child("Category").setValue(childRef);
                mOwnedItems.child("Title").setValue(fTitle);
                mOwnedItems.child("FileType").setValue(fType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void createRecord(String buyerId, String sellerId, Double buyerPreviousBal, Double sellerPreviousBal, Double defPreviousBal, Double buyerNewBal, Double sellerNewBal, Double defNewBal, Double transAmount, String debitTime, String creditTime){
        Random rand = new Random();
        int referenceNumber = 100000000 + rand.nextInt(999999999);
        DatabaseReference mTransRecordDebit = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Debit").push();
        String key = mTransRecordDebit.getKey();
        mTransRecordDebit.child("User").setValue(buyerId);
        mTransRecordDebit.child("BalancePrevious").setValue(buyerPreviousBal);
        mTransRecordDebit.child("BalanceNew").setValue(buyerNewBal);
        mTransRecordDebit.child("TransactionCost").setValue(transAmount);
        mTransRecordDebit.child("Time").setValue(debitTime);
        mTransRecordDebit.child("Item").setValue(itemKey);
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            mTransRecordDebit.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_1));
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            mTransRecordDebit.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_2));
        }
        mTransRecordDebit.child("RefNumber").setValue(String.valueOf(referenceNumber));

        DatabaseReference mTransRecordCredit = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Credit").child(key);
        mTransRecordCredit.child("User").setValue(sellerId);
        mTransRecordCredit.child("BalancePrevious").setValue(sellerPreviousBal);
        mTransRecordCredit.child("BalanceNew").setValue(sellerNewBal);
        mTransRecordCredit.child("TransactionCost").setValue(transAmount);
        mTransRecordCredit.child("Time").setValue(creditTime);
        mTransRecordCredit.child("Item").setValue(itemKey);
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            mTransRecordCredit.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_1));
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            mTransRecordCredit.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_2));
        }
        mTransRecordCredit.child("RefNumber").setValue(String.valueOf(referenceNumber));

        DatabaseReference mTransFee = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Fee").child(key);
        mTransFee.child("User").setValue(getString(R.string.firebase_ref_monetary_app_default_name));
        mTransFee.child("BalancePrevious").setValue(defPreviousBal);
        mTransFee.child("BalanceNew").setValue(defNewBal);
        mTransFee.child("TransactionCost").setValue(transAmount);
        mTransFee.child("Time").setValue(creditTime);
        mTransFee.child("Item").setValue(itemKey);
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            mTransFee.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_1));
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            mTransFee.child("ItemType").setValue(getString(R.string.firebase_ref_posts_type_2));
        }
        mTransFee.child("RefNumber").setValue(String.valueOf(referenceNumber));

        Toast.makeText(TransactionsActivity.this, R.string.info_transaction_successful, Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
        finish();
    }


    public void playAudio(){
        btnPlayAudio.setVisibility(View.GONE);
        progressBufferAudio.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child(childRef).child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                audioView.setVideoURI(mUri);
                audioView.requestFocus();
                audioView.start();
                progressBufferAudio.setVisibility(View.GONE);
                staticIcon.setVisibility(View.VISIBLE);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (active){
//                            Toast.makeText(TransactionsActivity.this, "End of preview", Toast.LENGTH_SHORT).show();
                        }
                        mediaPlayer.stop();
                        audioView.stopPlayback();
                        btnPlayAudio.setVisibility(View.VISIBLE);
                        staticIcon.setVisibility(View.GONE);
                    }
                }, 15000);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    audioView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                progressBufferAudio.setVisibility(View.VISIBLE);
                            } else {
                                progressBufferAudio.setVisibility(View.GONE);
                            }

                            return false;
                        }
                    });
                }
                audioView.setMediaController(mediaController);
                mediaController.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void playVideo(){
        btnPlayVideo.setVisibility(View.GONE);
        progressBuffer.setVisibility(View.VISIBLE);

        FirebaseDatabase.getInstance().getReference().child(childRef).child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUri = Uri.parse(String.valueOf(dataSnapshot.child("file_path").getValue()));
                videoView.setVideoURI(mUri);
                videoView.requestFocus();
                videoView.start();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (active){
//                            Toast.makeText(TransactionsActivity.this, "End of preview", Toast.LENGTH_SHORT).show();
                        }
                        mediaPlayer.stop();
                        videoView.stopPlayback();
                        btnPlayVideo.setVisibility(View.VISIBLE);
                    }
                }, 15000);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        @Override
                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                            if (i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                                progressBuffer.setVisibility(View.VISIBLE);
                            } else {
                                progressBuffer.setVisibility(View.GONE);
                            }

                            return false;
                        }
                    });
                }

                videoView.setMediaController(mediaController);
                mediaController.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.stop();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
    }

    public void loadPostDetails() {
        mPosts = FirebaseDatabase.getInstance().getReference().child(childRef).child(itemKey);
        mPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtPrice.setText(String.valueOf(dataSnapshot.child("price").getValue()));
                txtBidding.setText(String.valueOf(dataSnapshot.child("biddable").getValue()));
                if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
                    txtInstitution.setText(dataSnapshot.child("institution").getValue().toString());
                    txtDept.setText(dataSnapshot.child("department").getValue().toString());
                    txtCourse.setText(dataSnapshot.child("course").getValue().toString());
                } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
                    txtTag.setText(String.valueOf(dataSnapshot.child("tag").getValue()));
                }
                fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                if (fileType.equals("image")){
                    layoutImage.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(filePath).into(imageView);
                    mProgress.dismiss();
                }
                else if (fileType.equals("audio")){
                    layoutAudio.setVisibility(View.VISIBLE);
                    mProgress.dismiss();

                }
                else if (fileType.equals("video")){
                    layoutVideo.setVisibility(View.VISIBLE);
                    mProgress.dismiss();
                }
                else if (fileType.equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                    mProgress.dismiss();
                }
                loadAuthorProfile(dataSnapshot.child("author").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadAuthorProfile(String author) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(author).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child("profile_picture").getValue().toString().isEmpty()){
                    Picasso.with(getApplicationContext()).load(dataSnapshot.child("profile_picture").getValue().toString()).into(imgAuthor);
                }
                txtAuthor.setText(dataSnapshot.child("username").getValue().toString());
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                    Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                    transactionsIntent.putExtra("file_key",key);
                    transactionsIntent.putExtra("outgoing_intent","FilteredCategoryActivity");
                    transactionsIntent.putExtra("item_price",itemPrice);
                    transactionsIntent.putExtra("title",title);
                    transactionsIntent.putExtra("file_type",fileType);
                    transactionsIntent.putExtra("postType",category);
                    startActivity(transactionsIntent);
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

    public void checkDefaultAccount(){
        mProgress.setMessage(getString(R.string.info_please_wait));
        DatabaseReference mDefault = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary))
                .child(getString(R.string.firebase_ref_monetary_app_default_name));
        mDefault.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mDefault.child("email").setValue("");
                    mDefault.child("previous_balance").setValue(0.0);
                    mDefault.child("current_balance").setValue(0.0);
                    mDefault.child("status").setValue("enabled").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                if (mProgress.isShowing())
                                mProgress.dismiss();
                            }
                        }
                    });
                } else
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
