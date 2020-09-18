package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class ViewBidActivity extends AppCompatActivity {
    private TextView txtPriceOld,txtPriceNew,txtItem,txtTime,txtBidder,txtBlank,txtBlankPrice;

    private String bidRef;
    private DatabaseReference mBids;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn;
    private Button btnReject,btnApprove;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private String childRef,incomingIntent;
    private String TAG = "ViewBidActivity";
    private String itemName,itemKey,bidderId,recipientId,price,postType;
    private CircleImageView imgUser;
    private int BID_STATE = 0;

    private Double transactionChargeRate=0.0;
    private Double amount,d;

    Calendar calendar;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.Theme_UserDialogDark);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.Theme_UserDialogSignature);
        } else {
            setTheme(R.style.Theme_UserDialog);
        }
        setContentView(R.layout.activity_view_bid);

        bidRef = getIntent().getExtras().getString("bid_reference");
        recipientId = getIntent().getExtras().getString("bidder_id");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);

        imgUser = findViewById(R.id.img_user);
        txtPriceOld = findViewById(R.id.txt_view_bid_bid_price_old);
        txtPriceNew = findViewById(R.id.txt_view_bid_bid_price_new);
        txtItem = findViewById(R.id.txt_view_bid_bid_item);
        txtTime = findViewById(R.id.txt_time);
        txtBidder = findViewById(R.id.txt_view_bid_bid_sender);
        layoutBtn = findViewById(R.id.layout_view_bid_btn);
        btnApprove = findViewById(R.id.btn_view_bid_view_approve);
        btnReject = findViewById(R.id.btn_view_bid_view_reject);
        txtBlank = findViewById(R.id.txt_view_bid_bid_blank);
        txtBlankPrice = findViewById(R.id.txt_view_bid_bid_blank_price);

        mBids = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids));

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

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle(R.string.title_reject_bid)
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (BID_STATE == 1){
                                    rejectBid();
                                }
                            }
                        })
                        .show();
            }
        });

        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle(R.string.title_accept_bid)
                        .setMessage(R.string.confirm_you_are_about_to_accept_bid_offer_proceed)
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (BID_STATE == 1){
                                    approveBid();
                                }
                            }
                        })
                        .show();
            }
        });

        loadViews();

    }

    public void loadViews(){
        mProgress.setMessage(getString(R.string.link_loading));
        if (!mProgress.isShowing() && !((ViewBidActivity) this).isFinishing()){
            mProgress.show();
        }
        try {
            mBids.child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        childRef = String.valueOf(dataSnapshot.child("source").getValue());
                        postType = String.valueOf(dataSnapshot.child("source").getValue());
                        itemKey = String.valueOf(dataSnapshot.child("item_key").getValue());
                        bidderId = String.valueOf(dataSnapshot.child("bidder_id").getValue());
                        txtBlank.setText(bidderId);
//                        Log.d(TAG,"MessageKey: "+bidRef);
//                        Log.d(TAG,"logDetails(): dataSnapshot:"+ String.valueOf(dataSnapshot));
                        txtTime.setText(String.valueOf(dataSnapshot.child("bid_time").getValue()));
                        getItemName(itemKey);
                        getSenderName(bidderId);
                        txtPriceNew.setText(String.valueOf(dataSnapshot.child("price_bid").getValue()));
                        txtBlankPrice.setText(String.valueOf(dataSnapshot.child("price_bid").getValue()));
                        txtPriceOld.setText(String.valueOf(dataSnapshot.child("price_original").getValue()));
                        if (dataSnapshot.child("status").getValue().equals("pending")){
                            BID_STATE = 1;
                            layoutBtn.setVisibility(View.VISIBLE);
                        }
                    }

                    if (mProgress.isShowing())
                        mProgress.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loadViews();
                }
            });
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public void getSenderName(String senderId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                txtBidder.setText(String.valueOf(dataSnapshot.child("username").getValue())+" ("+String.valueOf(dataSnapshot.child("email").getValue())+")");
                txtBidder.setText(String.valueOf(dataSnapshot.child("username").getValue()));
                if (dataSnapshot.child("profile_picture").exists() && !dataSnapshot.child("profile_picture").getValue().equals("")){
                    Uri mFileUri = Uri.parse(dataSnapshot.child("profile_picture").getValue().toString());
                    Picasso.with(getApplicationContext()).load(mFileUri).into(imgUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getItemName (final String itemId){
        FirebaseDatabase.getInstance().getReference().child(childRef).child(itemId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itemName = String.valueOf(dataSnapshot.child("title").getValue());
                txtItem.setText(itemName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadViews();
            }
        });
    }

    public String getBidderEmail(String id){
        final String[] email = new String[1];
        FirebaseDatabase.getInstance().getReference().child("Users").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                email[0] = String.valueOf(dataSnapshot.child("email").getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return email[0];
    }

    public void rejectBid(){
        price = txtPriceNew.getText().toString();
        final String item = txtItem.getText().toString();
        final String bidder_id = txtBlank.getText().toString();
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.setCancelable(false);
        if (!((ViewBidActivity) this).isFinishing()){
            mProgress.show();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        changeBidStatus("rejected");
                        DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(bidder_id).push();
                        mMessage.child("sender_id").setValue(mAuth.getCurrentUser().getUid());
                        mMessage.child("recipient_id").setValue(bidder_id);
//                        Log.d(TAG,"rejectBid: bidderId: "+bidderId+", recipientId: "+recipientId);
                        mMessage.child("title").setValue("Bid Rejected");
                        mMessage.child("category").setValue("def_bids_0000");
                        mMessage.child("message").setValue("Unfortunately, I cannot accept your offer of "+price+" token(s) on my item "+item+".");
                        mMessage.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                        mMessage.child("reference").setValue(bidRef);
                        mMessage.child("seen").setValue("false");
                        Toast.makeText(ViewBidActivity.this, R.string.info_bid_rejected_bidder_notified, Toast.LENGTH_LONG).show();
                        mProgress.dismiss();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void approveBid(){
        final String fileKey = itemKey;
        final String itemPrice = price;
        createFinancialAccount();
    }

    public void changeBidStatus(String command){
        if (command.equals("approved")){
            FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).child("status").setValue("approved");
        }
        FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(bidderId).child(itemKey).child("status").setValue("ready");
    }

    public void sendApprovalMessage(){
        price = txtPriceNew.getText().toString();
        final String item = txtItem.getText().toString();
        final String bidder_id = txtBlank.getText().toString();
        mProgress.setMessage(getString(R.string.info_finishing));
        mProgress.setCancelable(false);
        if (!((ViewBidActivity) this).isFinishing()){
            mProgress.show();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(bidder_id).push();
                mMessage.child("sender_id").setValue(mAuth.getCurrentUser().getUid());
                mMessage.child("recipient_id").setValue(bidder_id);
//                Log.d(TAG,"acceptBid: bidderId: "+bidderId+", recipientId: "+recipientId);
                mMessage.child("title").setValue("Bid Accepted");
                mMessage.child("category").setValue("def_bids_0000");
                mMessage.child("message").setValue("I have accepted your bid of "+price+" token(s) on my item "+item+". Enjoy!");
                mMessage.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                mMessage.child("reference").setValue(bidRef);
                if (mProgress.isShowing() && !(ViewBidActivity.this).isFinishing()){
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage(getString(R.string.info_your_financial_account_is_being_configured));
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    if (!mProgress.isShowing() && !(ViewBidActivity.this).isFinishing()){
                        mProgress.show();
                    }
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId);
                    mMonetaryAccount.child("email").setValue(getBidderEmail(bidderId));
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getApplicationContext(), getString(R.string.info_setup_complete), Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_an_error_occurred_while_configuring_your_account)+": "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage(getString(R.string.info_bidders_account_is_being_prepared_please_wait));
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    if (!mProgress.isShowing() && !(ViewBidActivity.this).isFinishing()){
                        mProgress.show();
                    }
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId);
                    mMonetaryAccount.child("email").setValue(getBidderEmail(bidderId));
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getApplicationContext(), getString(R.string.info_setup_complete), Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
                checkBalance(itemKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkBalance(final String key){
        mProgress.setMessage(getString(R.string.info_please_wait));
        if (!((ViewBidActivity) this).isFinishing()){
            mProgress.show();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Double buyerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                FirebaseDatabase.getInstance().getReference().child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Double price = Double.parseDouble(txtBlankPrice.getText().toString().trim());

                        if (buyerBalance<price){
                            Toast.makeText(getApplicationContext(), R.string.info_bidder_unable_to_complete_transaction, Toast.LENGTH_SHORT).show();
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
                final Double price = Double.parseDouble(txtBlankPrice.getText().toString().trim());
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
                            Toast.makeText(getApplicationContext(), R.string.error_error_occurred_try_again, Toast.LENGTH_SHORT).show();
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

            DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(bidderId);
            mMonetaryAccount.child("current_balance").setValue(bal);
            mMonetaryAccount.child("previous_balance").setValue(bal+amt);
            final String debitTime = sdf.format(Calendar.getInstance().getTime());
//            getSellerDetails(fileKey);
            creditSeller(sbal,samt,sellerId,bidderId,previousBalance,bal,debitTime);
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
                ownersList.add(String.valueOf(bidderId));
                final String postOwners = TextUtils.join(",",ownersList);
                mOwnersList.child("owners").setValue(postOwners);
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

        changeBidStatus("approved");
        sendApprovalMessage();
        Toast.makeText(getApplicationContext(), R.string.info_transaction_successful, Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
        finish();
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

    @Override
    public void onBackPressed() {
        if (!mProgress.isShowing() && !((ViewBidActivity) this).isFinishing()){
            mProgress.show();
        }
        finish();
    }
}
