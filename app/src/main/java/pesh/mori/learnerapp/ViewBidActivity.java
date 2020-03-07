package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.content.IntentCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private String itemName,itemKey,bidderId,recipientId,price;
    private CircleImageView imgUser;

    Calendar calendar;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bid);

        bidRef = getIntent().getExtras().getString("bid_reference");
        recipientId = getIntent().getExtras().getString("bidder_id");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mAlert = new AlertDialog.Builder(this);
        mProgress = new ProgressDialog(this);

        imgUser = findViewById(R.id.img_user);
        txtPriceOld = findViewById(R.id.txt_view_bid_bid_price_old);
        txtPriceNew = findViewById(R.id.txt_view_bid_bid_price_new);
        txtItem = findViewById(R.id.txt_view_bid_bid_item);
        txtTime = findViewById(R.id.txt_view_bid_bid_time);
        txtBidder = findViewById(R.id.txt_view_bid_bid_sender);
        layoutBtn = findViewById(R.id.layout_view_bid_btn);
        btnApprove = findViewById(R.id.btn_view_bid_view_approve);
        btnReject = findViewById(R.id.btn_view_bid_view_reject);
        txtBlank = findViewById(R.id.txt_view_bid_bid_blank);
        txtBlankPrice = findViewById(R.id.txt_view_bid_bid_blank_price);

        mBids = FirebaseDatabase.getInstance().getReference().child("Bids");

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Reject bid")
                        .setMessage("Are you sure?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                rejectBid();
                            }
                        })
                        .show();
            }
        });

        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle("Accept bid")
                        .setMessage("You are about to accept a bid offer. Proceed?")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                approveBid();
                            }
                        })
                        .show();
            }
        });

        loadViews();

    }

    public void loadViews(){
        mProgress.setMessage("Loading...");
        if (!mProgress.isShowing()){
            mProgress.show();
        }
        try {
            mBids.child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        childRef = String.valueOf(dataSnapshot.child("source").getValue());
                        itemKey = String.valueOf(dataSnapshot.child("item_key").getValue());
                        bidderId = String.valueOf(dataSnapshot.child("bidder_id").getValue());
                        txtBlank.setText(bidderId);
                        Log.d(TAG,"MessageKey: "+bidRef);
                        Log.d(TAG,"logDetails(): dataSnapshot:"+String.valueOf(dataSnapshot));
                        txtTime.setText(String.valueOf(dataSnapshot.child("bid_time").getValue()));
                        getItemName(itemKey);
                        getSenderName(bidderId);
                        txtPriceNew.setText(String.valueOf(dataSnapshot.child("price_bid").getValue()));
                        txtBlankPrice.setText(String.valueOf(dataSnapshot.child("price_bid").getValue()));
                        txtPriceOld.setText(String.valueOf(dataSnapshot.child("price_original").getValue()));
                        if (dataSnapshot.child("status").getValue().equals("pending")){
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
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.show();
        FirebaseDatabase.getInstance().getReference().child("Bids").child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference().child("Bids").child(mAuth.getCurrentUser().getUid()).child(bidRef).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        changeBidStatus("rejected");
                        DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(bidder_id).push();
                        mMessage.child("sender_id").setValue(mAuth.getCurrentUser().getUid());
                        mMessage.child("recipient_id").setValue(bidder_id);
                        Log.d(TAG,"rejectBid: bidderId: "+bidderId+", recipientId: "+recipientId);
                        mMessage.child("title").setValue("Bid Rejected");
                        mMessage.child("category").setValue("def_bids_0000");
                        mMessage.child("message").setValue("Unfortunately, I cannot accept your offer of "+price+" tokens on my item "+item+".");
                        mMessage.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                        mMessage.child("reference").setValue(bidRef);
                        mMessage.child("seen").setValue("false");
                        Toast.makeText(ViewBidActivity.this, "Bid rejected. The concerned bidder has been notified.", Toast.LENGTH_LONG).show();
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
        changeBidStatus("approved");
        sendApprovalMessage();
    }

    public void changeBidStatus(String command){
        if (command.equals("approved")){
            FirebaseDatabase.getInstance().getReference().child("Bids").child(mAuth.getCurrentUser().getUid()).child(bidRef).child("status").setValue("approved");
        }
        FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(bidderId).child(itemKey).child("status").setValue("ready");
    }

    public void sendApprovalMessage(){
        price = txtPriceNew.getText().toString();
        final String item = txtItem.getText().toString();
        final String bidder_id = txtBlank.getText().toString();
        mProgress.setMessage("Finishing...");
        mProgress.setCancelable(false);
        mProgress.show();
        FirebaseDatabase.getInstance().getReference().child("Bids").child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(bidder_id).push();
                mMessage.child("sender_id").setValue(mAuth.getCurrentUser().getUid());
                mMessage.child("recipient_id").setValue(bidder_id);
                Log.d(TAG,"acceptBid: bidderId: "+bidderId+", recipientId: "+recipientId);
                mMessage.child("title").setValue("Bid Accepted");
                mMessage.child("category").setValue("def_bids_0000");
                mMessage.child("message").setValue("I have accepted your bid of "+price+" token(s) on my item "+item+". Enjoy!");
                mMessage.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                mMessage.child("reference").setValue(bidRef);
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage("Your financial account is being configured. Please wait...");
        FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mProgress.show();
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId);
                    mMonetaryAccount.child("email").setValue(getBidderEmail(bidderId));
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getApplicationContext(), "Setup complete!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "An error occured: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createFinancialAccount(){
//        mProgress.setTitle("Account Configuration");
        mProgress.setMessage("Bidder's financial account is being configured. Please wait...");
        FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mProgress.show();
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId);
                    mMonetaryAccount.child("email").setValue(getBidderEmail(bidderId));
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getApplicationContext(), "Setup complete!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
                checkBalance(itemKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "An error occured: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkBalance(final String key){
        mProgress.setMessage("Please wait...");
        mProgress.show();
        FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Double buyerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                FirebaseDatabase.getInstance().getReference().child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Double price = Double.parseDouble(txtBlankPrice.getText().toString().trim());

                        if (buyerBalance<price){
                            Toast.makeText(getApplicationContext(), "Bidder has insufficient tokens to purchase your item", Toast.LENGTH_SHORT).show();
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
                FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(sellerId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            if (!dataSnapshot.exists()){
                                DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(sellerId);
                                mMonetaryAccount.child("email").setValue(sellerEmail[0]);
                                mMonetaryAccount.child("previous_balance").setValue(0.00);
                                mMonetaryAccount.child("current_balance").setValue(0.00);
                                mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                            }

                            Double sellerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                            debitBuyer(bal,amt,sellerBalance,price,sellerId);
                        }catch (NumberFormatException e){
                            mProgress.dismiss();
                            Toast.makeText(getApplicationContext(), "An error occured. Please try again.", Toast.LENGTH_SHORT).show();
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

    public double debitBuyer(Double bal,Double amt,Double sbal,Double samt,String sellerId){
        Double previousBalance = bal;
        if (bal>=amt){
            bal = bal-amt;

            DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(bidderId);
            mMonetaryAccount.child("current_balance").setValue(bal);
            final String debitTime = sdf.format(Calendar.getInstance().getTime());
//            getSellerDetails(fileKey);
            creditSeller(sbal,samt,sellerId,bidderId,previousBalance,bal,debitTime);
            addBuyerToList();
        }
        return bal;
    }

    public double creditSeller(Double bal,Double amt, String sellerId, String buyerId, Double buyerPBal, Double buyerNBal, String debitTime){
        Double previousBalance = bal;
        bal = bal+amt;

        DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(sellerId);
        mMonetaryAccount.child("current_balance").setValue(bal);

        final String creditTime = sdf.format(Calendar.getInstance().getTime());

        createRecord(buyerId,sellerId,buyerPBal,previousBalance,buyerNBal,bal,amt,debitTime,creditTime);

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

    public void createRecord(String buyerId,String sellerId,Double buyerPreviousBal,Double sellerPreviousBal,Double buyerNewBal,Double sellerNewBal,Double transAmount,String debitTime,String creditTime){
        Random rand = new Random();
        int referenceNumber = 100000000 + rand.nextInt(999999999);
        DatabaseReference mTransRecord = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").push();
        mTransRecord.child("Debit").child("User").setValue(buyerId);
        mTransRecord.child("Debit").child("BalancePrevious").setValue(buyerPreviousBal);
        mTransRecord.child("Debit").child("BalanceNew").setValue(buyerNewBal);
        mTransRecord.child("Debit").child("TransactionCost").setValue(transAmount);
        mTransRecord.child("Debit").child("Time").setValue(debitTime);
        mTransRecord.child("Debit").child("Item").setValue(itemKey);
        if (incomingIntent.equals("mydownloads") || incomingIntent.equals("DownloadActivity")){
            mTransRecord.child("Debit").child("ItemType").setValue("File");
        } else if (incomingIntent.equals("DownloadDiyActivity")){
            mTransRecord.child("Debit").child("ItemType").setValue("DIY");
        }
        mTransRecord.child("Debit").child("RefNumber").setValue(String.valueOf(referenceNumber));
        mTransRecord.child("Credit").child("User").setValue(sellerId);
        mTransRecord.child("Credit").child("BalancePrevious").setValue(sellerPreviousBal);
        mTransRecord.child("Credit").child("BalanceNew").setValue(sellerNewBal);
        mTransRecord.child("Credit").child("TransactionCost").setValue(transAmount);
        mTransRecord.child("Credit").child("Time").setValue(creditTime);
        mTransRecord.child("Credit").child("Item").setValue(itemKey);
        if (incomingIntent.equals("mydownloads") || incomingIntent.equals("DownloadActivity")){
            mTransRecord.child("Credit").child("ItemType").setValue("File");
        } else if (incomingIntent.equals("DownloadDiyActivity")){
            mTransRecord.child("Credit").child("ItemType").setValue("DIY");
        }
        mTransRecord.child("Credit").child("RefNumber").setValue(String.valueOf(referenceNumber));

        layoutBtn.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), "Transaction successful", Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (mProgress.isShowing()){
            mProgress.dismiss();
        }
        finish();
    }
}
