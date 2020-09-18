package pesh.mori.learnerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BidActivity extends AppCompatActivity {

    private Button btn_bid;
    private String itemPrice, itemKey, buyerId;
    private String authorId;
    private FirebaseAuth mAuth;
    private EditText bidAmount;
    Calendar calendar;
    private SimpleDateFormat sdf;
    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert,mAlert2;
    private String incomingIntent;
    private String childRef,postType="";
    private String mBidPushKey,mMessagePushKey;

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
        setContentView(R.layout.activity_bid);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mAlert2 = new AlertDialog.Builder(this,R.style.AlertDialogStyle);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        itemKey = getIntent().getExtras().getString("itemKey");
        itemPrice = getIntent().getExtras().getString("itemPrice");
        buyerId = getIntent().getExtras().getString("buyerId");
        authorId = getIntent().getExtras().getString("authorId");
        mBidPushKey = getIntent().getExtras().getString("mBidPushKey");
        mMessagePushKey = getIntent().getExtras().getString("mMessagePushKey");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");
        postType = getIntent().getExtras().getString("postType");
        if (postType.equals(getString(R.string.firebase_ref_posts_type_1))){
            childRef = getString(R.string.firebase_ref_posts_type_1);
        } else if (postType.equals(getString(R.string.firebase_ref_posts_type_2))){
            childRef = getString(R.string.firebase_ref_posts_type_2);
        }

        bidAmount = findViewById(R.id.txt_edit_bid);

        mAuth = FirebaseAuth.getInstance();

        btn_bid = findViewById(R.id.btn_bid);
        bidAmount = findViewById(R.id.txt_edit_bid);

        /*v1.0.5 new feature 00001*/
        btn_bid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setMessage(getString(R.string.info_please_wait));
                mProgress.setCancelable(false);
                mProgress.show();
                checkBidAmount();
            }
        });

        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.setCancelable(false);
        mProgress.show();
        checkPendingBid();
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public String preCreateBid(){
        mAlert.setMessage(R.string.info_bid_submitted_successfuly_please_wait_for_author_to_accept);
        mAlert.setPositiveButton(getString(R.string.option_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        mAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });

        createBid(mBidPushKey,mMessagePushKey);
        /*v1.0.4 bug fix 00005*/
        if (mProgress!=null && mProgress.isShowing()){
            mProgress.dismiss();
        }
        mAlert.show();
        return authorId;

    }

    /*v1.0.5 new feature 00001*/
    public void checkPendingBid(){
        final String[] pushKey = {""};
        final DatabaseReference mBids = FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(mAuth.getCurrentUser().getUid());
        mBids.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    mBids.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child("status").getValue().equals("pending")){
                                    mAlert.setTitle(R.string.title_pending_bid)
                                            .setMessage(R.string.info_you_already_submitted_a_bid_for_this_item)
                                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                @Override
                                                public void onCancel(DialogInterface dialogInterface) {
                                                    finish();
                                                }
                                            })
                                            .setPositiveButton(getString(R.string.option_ok), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                    if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                                                        mProgress.dismiss();
                                                    }
                                                    finish();
                                                }
                                            })
                                            .setNeutralButton(R.string.option_cancel_bid, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    /*v1.0.5 new feature 00001*/
                                                    dialogInterface.dismiss();

                                                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids))
                                                            .child(authorId)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    for (final DataSnapshot dataSnapshot2:dataSnapshot.getChildren()) {
//                                                                        Log.d("LOG_X_itemKey",itemKey+" : "+dataSnapshot2.child("item_key").getValue());
//                                                                        Log.d("LOG_X_bidder",mAuth.getCurrentUser().getUid()+" : "+dataSnapshot2.child("bidder_id").getValue());
//                                                                        Log.d("LOG_X_status","pending"+" : "+dataSnapshot2.child("status").getValue());
                                                                        if (dataSnapshot2.child("item_key").getValue().equals(itemKey)
                                                                                && dataSnapshot2.child("bidder_id").getValue().equals(mAuth.getCurrentUser().getUid())
                                                                                && dataSnapshot2.child("status").getValue().equals("pending")){
                                                                            mAlert2.setTitle(getString(R.string.alert_cancel_bid)+" "+dataSnapshot2.child("price_bid").getValue()+" tokens.")
                                                                                    .setPositiveButton(R.string.option_continue, new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                                            FirebaseDatabase.getInstance().getReference().child("Messages").child(authorId)
                                                                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                            for (DataSnapshot dataSnapshot3:dataSnapshot.getChildren()){
//                                                                                                                Log.d("LOG_X_category",getString(R.string.bids_category_message_id)+" : "+dataSnapshot3.child("category").getValue());
//                                                                                                                Log.d("LOG_X_sender",mAuth.getCurrentUser().getUid()+" : "+dataSnapshot3.child("sender_id").getValue());
//                                                                                                                Log.d("LOG_X_ref",dataSnapshot2.getKey()+" : "+dataSnapshot3.child("reference").getValue());
                                                                                                                if (dataSnapshot3.child("category").getValue().equals(getString(R.string.bids_category_message_id))
                                                                                                                        && dataSnapshot3.child("sender_id").getValue().equals(mAuth.getCurrentUser().getUid())
                                                                                                                        && dataSnapshot3.child("reference").getValue().equals(dataSnapshot2.getKey())){
                                                                                                                    cancelBid(dataSnapshot2.getKey(),dataSnapshot3.getKey());
                                                                                                                }
                                                                                                            }
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    })
                                                                                    .setNeutralButton(R.string.option_do_not_continue, new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                                            dialogInterface.dismiss();
                                                                                            finish();
                                                                                        }
                                                                                    })
                                                                                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                                                                        @Override
                                                                                        public void onCancel(DialogInterface dialogInterface) {
                                                                                            finish();
                                                                                        }
                                                                                    });
                                                                            if (!BidActivity.this.isFinishing()){
                                                                                mAlert2.show();
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });
                                                }
                                            })
                                            .show();
                                    if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                                        mProgress.dismiss();
                                    }
                                } else if (dataSnapshot.child("status").getValue().equals("ready")){
//                                    checkBidAmount();
                                    if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                                        mProgress.dismiss();
                                    }
                                }
                            } else {
//                                checkBidAmount();
                                if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                                    mProgress.dismiss();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
//                    checkBidAmount();
                    if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                        mProgress.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkBidAmount(){
        if (TextUtils.isEmpty(bidAmount.getText().toString().trim())){

            Toast.makeText(BidActivity.this, R.string.info_please_enter_bid_price, Toast.LENGTH_SHORT).show();
            mProgress.dismiss();

        } else {
            mAlert.setTitle(R.string.title_submit_bid)
                    .setMessage(getString(R.string.required_are_you_sure_you_want_to_submit_your_bid_of_1)+" "+bidAmount.getText().toString()+" "+getString(R.string.required_are_you_sure_you_want_to_submit_your_bid_of_2))
                    .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                preCreateBid();
                            }
                        }
                    })
                    .setNeutralButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            /*v1.0.4 bug fix 00005*/
                            if (mProgress!=null && mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                        }
                    })
                    /*v1.0.4 bug fix 00005*/
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            dialogInterface.dismiss();
                            if (mProgress!=null && mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                        }
                    })
                    .show();
        }
    }

    public void createBid(String bidsKey, String messageKey){

        final String bidTime = sdf.format(Calendar.getInstance().getTime());

        DatabaseReference mBid = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids))
                .child(authorId).child(bidsKey);
        String ref = String.valueOf(mBid.getKey());
//        Log.d(TAG,"createBid: mBid.getKey() "+ref);
        mBid.child("bidder_id").setValue(buyerId);
        mBid.child("item_key").setValue(itemKey);

        mBid.child("source").setValue(childRef);
        mBid.child("price_original").setValue(itemPrice);
        mBid.child("price_bid").setValue(bidAmount.getText().toString().trim());
        mBid.child("bid_time").setValue(bidTime);
        mBid.child("status").setValue("pending");

        DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(authorId).child(messageKey);
        mMessage.child("sender_id").setValue(buyerId);
        mMessage.child("recipient_id").setValue(authorId);
        mMessage.child("title").setValue("Bid");
        mMessage.child("category").setValue(getString(R.string.bids_category_message_id));
        mMessage.child("message").setValue("I have placed a bid on an item of yours. Kindly consider my offer.");
        mMessage.child("seen").setValue("false");
        mMessage.child("timestamp").setValue(bidTime);
        mMessage.child("reference").setValue(ref);

        FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(mAuth.getCurrentUser().getUid()).child(itemKey).child("status").setValue("pending");

    }

    /*v1.0.5 new feature 00001*/
    public void cancelBid(String bidsKey, final String messageKey){
        mProgress.setMessage(getString(R.string.info_please_wait));
        if (mProgress != null && !mProgress.isShowing() && !BidActivity.this.isFinishing()){
            mProgress.show();
        }
        DatabaseReference mBid = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(authorId).child(bidsKey);
        mBid.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(authorId).child(messageKey);
                mMessage.removeValue();
                DatabaseReference mBidStatus = FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(mAuth.getCurrentUser().getUid()).child(itemKey);
                mBidStatus.removeValue();
                Toast.makeText(BidActivity.this, R.string.info_bid_cancelled, Toast.LENGTH_LONG).show();
                if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                    mProgress.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (mProgress != null && mProgress.isShowing() && !BidActivity.this.isFinishing()){
                    mProgress.dismiss();
                }
                Toast.makeText(BidActivity.this, R.string.error_occurred_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
