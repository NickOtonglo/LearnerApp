package pesh.mori.learnerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class ViewTransactionActivity extends AppCompatActivity {
    private String incomingIntent="",postKey="", transType="";
    private TextView txtTime,txtRefNumber,txtItem,txtItemLabel,txtCost,txtOldBal,txtNewBal,txtAccountNumber,txtRate;
    private LinearLayout layoutReg,layoutToken;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        txtTime = findViewById(R.id.txt_time);
        layoutReg = findViewById(R.id.layout_reg);
        layoutToken = findViewById(R.id.layout_tk);

        transType = getIntent().getExtras().getString("trans_type");
        postKey = getIntent().getExtras().getString("post_key");
        incomingIntent = getIntent().getExtras().getString("outgoing_intent");

        if (incomingIntent.equals("TransactionHistoryActivity_TokensFragment")){
            layoutToken.setVisibility(View.VISIBLE);
            txtRefNumber = findViewById(R.id.txt_ref_tk);
            txtItem = findViewById(R.id.txt_item_tk);
            txtCost = findViewById(R.id.txt_amount_tk);
            txtOldBal = findViewById(R.id.txt_bal_prev_tk);
            txtNewBal = findViewById(R.id.txt_bal_new_tk);
            txtAccountNumber = findViewById(R.id.txt_account_tk);
            txtRate = findViewById(R.id.txt_rate_tk);
        } else if (incomingIntent.equals("TransactionHistoryActivity_PurchaseFragment")
                || incomingIntent.equals("TransactionHistoryActivity_SalesFragment")){
            layoutReg.setVisibility(View.VISIBLE);
            txtRefNumber = findViewById(R.id.txt_ref);
            txtItem = findViewById(R.id.txt_item);
            txtItemLabel = findViewById(R.id.txt_item_label);
            txtCost = findViewById(R.id.txt_amount);
            txtOldBal = findViewById(R.id.txt_bal_prev);
            txtNewBal = findViewById(R.id.txt_bal_new);
        }

        setSeen();
        setValues();
    }

    public void setValues(){
        if (incomingIntent.equals("TransactionHistoryActivity_TokensFragment")){
            FirebaseDatabase.getInstance().getReference().child("TokenPurchase").child(mAuth.getCurrentUser().getUid()).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    txtTime.setText(dataSnapshot.child("Time").getValue().toString());
                    txtRefNumber.setText(dataSnapshot.child("RefNumber").getValue().toString());
                    txtItem.setText("M-Pesa Token top-up");
                    txtCost.setText("KES "+dataSnapshot.child("TokensValue").getValue()+" ("+dataSnapshot.child("TokensPurchased").getValue()+" tokens)");
                    txtOldBal.setText(dataSnapshot.child("BalancePrevious").getValue()+" tokens");
                    txtNewBal.setText(dataSnapshot.child("BalanceNew").getValue()+" tokens");
                    txtAccountNumber.setText(dataSnapshot.child("AccountNumber").getValue().toString());
                    txtRate.setText("KES "+dataSnapshot.child("ExchangeRate").getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else if (incomingIntent.equals("TransactionHistoryActivity_PurchaseFragment")
                || incomingIntent.equals("TransactionHistoryActivity_SalesFragment")){
            FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child(transType).child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                    txtTime.setText(dataSnapshot.child("Time").getValue().toString());
                    txtRefNumber.setText(dataSnapshot.child("RefNumber").getValue().toString());
                    txtCost.setText(dataSnapshot.child("TransactionCost").getValue().toString());
                    txtOldBal.setText(dataSnapshot.child("BalancePrevious").getValue()+" tokens");
                    txtNewBal.setText(dataSnapshot.child("BalanceNew").getValue()+" tokens");
                    if (dataSnapshot.child("ItemType").getValue().equals("Token")){
                        if (incomingIntent.equals("TransactionHistoryActivity_PurchaseFragment")){
                            txtItem.setText("TOKENS SENT");
                        }else if (incomingIntent.equals("TransactionHistoryActivity_SalesFragment")) {
                            txtItem.setText("TOKENS RECEIVED");
                        }
                    } else if (dataSnapshot.child("ItemType").getValue().equals("File") || dataSnapshot.child("ItemType").getValue().equals("DIY")){
                        FirebaseDatabase.getInstance().getReference().child("AllPosts").child(String.valueOf(dataSnapshot.child("Item").getValue())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                txtItemLabel.setText("Item (tap to open)");
                                txtItem.setTextColor(getResources().getColor(R.color.com_facebook_blue));
                                SpannableString content = new SpannableString(String.valueOf(dataSnapshot.child("Title").getValue()));
                                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                                txtItem.setText(content);
                                txtItem.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        checkOwnershipStatus(String.valueOf(dataSnapshot.child("ItemId").getValue()),String.valueOf(dataSnapshot.child("Category").getValue()));
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void setSeen(){
        if (incomingIntent.equals("TransactionHistoryActivity_TokensFragment")){
            FirebaseDatabase.getInstance().getReference().child("TokenPurchase").child(mAuth.getCurrentUser().getUid()).child(postKey)
                    .child("Seen").setValue("true");
        } else if (incomingIntent.equals("TransactionHistoryActivity_PurchaseFragment")){
            FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Debit").child(postKey)
                    .child("Seen").setValue("true");
        } else if (incomingIntent.equals("TransactionHistoryActivity_SalesFragment")){
            FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Credit").child(postKey)
                    .child("Seen").setValue("true");
        }
    }

    public void checkOwnershipStatus(final String key,final String category){
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
                if (category.equals("Files")){
                    String tag = String.valueOf(dataSnapshot.child("institution").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","DownloadActivity");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }  else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewFileActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }
                }else if (category.equals("DIY")){
                    String tag = String.valueOf(dataSnapshot.child("tag").getValue());
                    if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price>0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent transactionsIntent = new Intent(getApplicationContext(),TransactionsActivity.class);
                        transactionsIntent.putExtra("file_key",key);
                        transactionsIntent.putExtra("outgoing_intent","DownloadDiyActivity");
                        transactionsIntent.putExtra("item_price",itemPrice);
                        transactionsIntent.putExtra("title",title);
                        transactionsIntent.putExtra("file_type",fileType);
                        transactionsIntent.putExtra("tag",tag);
                        startActivity(transactionsIntent);
                    } else if (listOwners.contains(mAuth.getCurrentUser().getUid()) || author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    } else if (!listOwners.contains(mAuth.getCurrentUser().getUid()) && price==0 && !author.equals(mAuth.getCurrentUser().getUid())){
                        Intent viewFileIntent = new Intent(getApplicationContext(),ViewDiyActivity.class);
                        viewFileIntent.putExtra("file_key",key);
                        viewFileIntent.putExtra("tag",tag);
                        startActivity(viewFileIntent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
