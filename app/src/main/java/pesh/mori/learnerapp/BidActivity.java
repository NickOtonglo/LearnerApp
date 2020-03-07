package pesh.mori.learnerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private DatabaseReference mSource;
    private EditText bidAmount;
    Calendar calendar;
    private SimpleDateFormat sdf;
    private ProgressDialog mProgress;
    private AlertDialog.Builder mAlert;
    private String TAG = "BidActivity";
    private String incomingIntent;
    private String childRef;
    private String mBidPushKey,mMessagePushKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        itemKey = getIntent().getExtras().getString("itemKey");
        itemPrice = getIntent().getExtras().getString("itemPrice");
        buyerId = getIntent().getExtras().getString("buyerId");
        authorId = getIntent().getExtras().getString("authorId");
        mBidPushKey = getIntent().getExtras().getString("mBidPushKey");
        mMessagePushKey = getIntent().getExtras().getString("mMessagePushKey");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");
        if (incomingIntent.equals("mydownloads") || incomingIntent.equals("DownloadActivity")){
            childRef = "Files";

        } else if (incomingIntent.equals("DownloadDiyActivity")){
            childRef = "DIY";
        }

        bidAmount = findViewById(R.id.txt_edit_bid);

        mAuth = FirebaseAuth.getInstance();

        mSource = FirebaseDatabase.getInstance().getReference().child(childRef);

        btn_bid = findViewById(R.id.btn_bid);
        bidAmount = findViewById(R.id.txt_edit_bid);

        btn_bid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setMessage("Please wait...");
                mProgress.setCancelable(false);
                mProgress.show();
                checkPendingBid();
            }
        });

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public String preCreateBid(){
        mAlert.setMessage("Bid submitted successfully. Please wait for the author to accept.");
        mAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
        mProgress.dismiss();
        mAlert.show();
        return authorId;

    }

    public void checkPendingBid(){
        final String [] pushKey = {""};
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
                                    mAlert.setTitle("Pending bid")
                                            .setMessage("You already submitted a bid for this item. Kindly wait for the author to review it.")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            })
                                            .show();
                                    mProgress.dismiss();
                                } else if (dataSnapshot.child("status").getValue().equals("ready")){
                                    checkBidAmount();
                                }
                            } else {checkBidAmount();}
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {checkBidAmount();}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkBidAmount(){
        if (TextUtils.isEmpty(bidAmount.getText().toString().trim())){

            Toast.makeText(BidActivity.this, "Please enter Bid price", Toast.LENGTH_SHORT).show();
            mProgress.dismiss();

        } else {
            mAlert.setTitle("Submit Bid")
                    .setMessage("Are you sure you want to place your bid of "+bidAmount.getText().toString()+" tokens on this item?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                preCreateBid();
                            }
                        }
                    })
                    .setNeutralButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    }

    public void createBid(String bidsKey, String messageKey){

        final String bidTime = sdf.format(Calendar.getInstance().getTime());

        DatabaseReference mBid = FirebaseDatabase.getInstance().getReference().child("Bids").child(authorId).child(bidsKey);
        String ref = String.valueOf(mBid.getKey());
        Log.d(TAG,"createBid: mBid.getKey() "+ref);
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
        mMessage.child("category").setValue("def_bids_0000");
        mMessage.child("message").setValue("I have placed a bid on an item of yours. Kindly consider my offer.");
        mMessage.child("seen").setValue("false");
        mMessage.child("timestamp").setValue(bidTime);
        mMessage.child("reference").setValue(ref);

        FirebaseDatabase.getInstance().getReference().child("Status").child("Bids").child(mAuth.getCurrentUser().getUid()).child(itemKey).child("status").setValue("pending");

    }

//    public void bidderNotification(String title, String content) {
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("default",
//                    "YOUR_CHANNEL_NAME",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
//            mNotificationManager.createNotificationChannel(channel);
//        }
//
//        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        MediaPlayer mp = MediaPlayer.create(getApplicationContext(),notificationSound);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "default")
//                .setSmallIcon(R.mipmap.ic_launcher_round) // notification icon
//                .setContentTitle(title) // title for notification
//                .setContentText(content)// message for notification
//                .setSound(notificationSound) // set alarm sound for notification
//                .setAutoCancel(true); // clear notification after click
//        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(pi);
//        mNotificationManager.notify(0, mBuilder.build());
//    }


}
