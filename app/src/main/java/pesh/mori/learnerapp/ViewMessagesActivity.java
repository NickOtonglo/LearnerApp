package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewMessagesActivity extends AppCompatActivity {

    private TextView txtTitle,txtMessage,txtTime,txtSender;

    private String messageKey;
    private DatabaseReference mMessages;
    private FirebaseAuth mAuth;

    private LinearLayout layoutBtn;
    private Button btnDelete,btnViewBid;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;
    private String TAG = "ViewMessages";
    private String bidRef;
    private String senderId,bidderId,recipientId;

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
        setContentView(R.layout.activity_view_messages);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        messageKey = getIntent().getExtras().getString("message_key");

        mAuth = FirebaseAuth.getInstance();

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);

        layoutBtn = findViewById(R.id.layout_view_msg_btn);
        btnDelete = findViewById(R.id.btn_view_msg_delete);
        btnViewBid = findViewById(R.id.btn_view_msg_view_bid);
        txtTitle = findViewById(R.id.txt_view_msg_msg_title);
        txtMessage = findViewById(R.id.txt_view_msg_msg_message);
        txtTime = findViewById(R.id.txt_view_msg_msg_time);
        txtSender = findViewById(R.id.txt_view_msg_msg_sender);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAlert.setTitle(R.string.title_delete_message)
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setPositiveButton(getString(R.string.option_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage();
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });

        btnViewBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("LOG_bidRef",bidRef);
                Intent viewBidsIntent = new Intent(getApplicationContext(),ViewBidActivity.class);
                viewBidsIntent.putExtra("bid_reference",bidRef);
                viewBidsIntent.putExtra("bidder_id",senderId);
                viewBidsIntent.putExtra("outgoing_intent","ViewMessagesActivity");
                startActivity(viewBidsIntent);
            }
        });

        setSeen();
        loadPrerequisites();
        loadMessageDetails();
        loadViews();

    }

    @Override
    protected void onResume() {
        super.onResume();

        btnViewBid.setVisibility(View.GONE);
        loadViews();
    }

    public void setSeen(){
        FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid()).child(messageKey).child("seen").setValue("true");
    }

    public void loadPrerequisites(){
        FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid()).child(messageKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bidRef = String.valueOf(dataSnapshot.child("reference").getValue());
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bidderId = String.valueOf(dataSnapshot.child("bidder_id").getValue());
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

    public void loadViews(){
        mProgress.setMessage(getString(R.string.link_loading));
        try {
            if (!mProgress.isShowing() && !((ViewMessagesActivity) this).isFinishing()){
                mProgress.show();
            }
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        try {
            FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid()).child(messageKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    recipientId = String.valueOf(dataSnapshot.child("recipient_id").getValue());
//                Log.d(TAG,"logDetails(): dataSnapshot:"+String.valueOf(dataSnapshot.child("category").getValue()));
                    if(String.valueOf(dataSnapshot.child("category").getValue()).equals("def_bids_0000")){
                        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_bids)).child(mAuth.getCurrentUser().getUid()).child(bidRef).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (senderId.equals(bidderId) && recipientId.equals(mAuth.getCurrentUser().getUid())){
                                        btnViewBid.setVisibility(View.VISIBLE);
                                    }
//                                    Log.d(TAG,"senderId: "+senderId+", bidderId: "+bidderId+", recipientId: "+recipientId+", getUid: "+mAuth.getCurrentUser().getUid());
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
                    loadViews();
                }
            });
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if (mProgress.isShowing())
        mProgress.dismiss();
    }

    public void loadMessageDetails(){
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.setMessage(getString(R.string.link_loading));
        if (!((ViewMessagesActivity) this).isFinishing()){
            mProgress.show();
        }
        mMessages = FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid()).child(messageKey);
        mMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                senderId = String.valueOf(dataSnapshot.child("sender_id").getValue());
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtMessage.setText(String.valueOf(dataSnapshot.child("message").getValue()));
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                getSenderName(String.valueOf(dataSnapshot.child("sender_id").getValue()));

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadMessageDetails();
            }
        });
    }

    public void getSenderName(String senderId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtSender.setText(String.valueOf(dataSnapshot.child("username").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteMessage(){
        mProgress.setMessage(getString(R.string.info_deleting_message));
        mProgress.setCancelable(false);
        if (!((ViewMessagesActivity) this).isFinishing()){
            mProgress.show();
        }
        mMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMessages.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgress.dismiss();
                        Toast.makeText(ViewMessagesActivity.this, R.string.info_message_deleted, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgress.dismiss();
                Toast.makeText(ViewMessagesActivity.this, getString(R.string.error_error_occurred_try_again), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_message, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_delete_message:
                mAlert.setTitle(getString(R.string.title_delete_message))
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setPositiveButton(getString(R.string.option_delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteMessage();
                            }
                        })
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mProgress.isShowing()) {
            mProgress.dismiss();
        }
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
    }
}
