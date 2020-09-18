package pesh.mori.learnerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class TokensActivity_TransferTokensFragment extends Fragment {

    private Button btnSelectUser,btnTransfer;
    private DatabaseReference mUser;
    private FirebaseAuth mAuth;
    private String userId="",email="",username="",about="",avatar="";
    private TextInputEditText txtUserEmail,txtTokenAmount;
    private AlertDialog.Builder mAlert,mAlert2;
    private ProgressDialog mProgress;

    private static final int GET_USER_REQUEST = 1;
    private String itemKey="";
    private Calendar calendar;
    private SimpleDateFormat sdf;

    private TextView txtUsername,txtAbout;
    private CircleImageView imgAvatar;
    private LinearLayout layoutAvatar;

    public TokensActivity_TransferTokensFragment(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View transfertokens = inflater.inflate(R.layout.fragment_transfertokens, container, false);

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        mAlert = new AlertDialog.Builder(requireActivity(),R.style.AlertDialogStyle);
        mAlert2 = new AlertDialog.Builder(requireActivity(),R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(getActivity());

        mAuth = FirebaseAuth.getInstance();

        mUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mUser.keepSynced(true);

        txtTokenAmount = transfertokens.findViewById(R.id.txt_tokens_transfer_amount);

        txtUsername = transfertokens.findViewById(R.id.txt_card_user_list_name);
        txtAbout = transfertokens.findViewById(R.id.txt_card_user_list_about);
        imgAvatar = transfertokens.findViewById(R.id.img_avatar);
        layoutAvatar = transfertokens.findViewById(R.id.layout_avatar);

        btnSelectUser = transfertokens.findViewById(R.id.btn_transfer_search_user);
        btnSelectUser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult((new Intent(getActivity(),SearchUserActivity.class)),GET_USER_REQUEST);
                getActivity().overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
            }
        });

        btnTransfer = transfertokens.findViewById(R.id.btn_transfer_tokens);
        btnTransfer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress.setTitle(getString(R.string.title_sending_tokens));
                mProgress.setMessage(getString(R.string.info_please_wait));
                mProgress.show();
                transferTokens();
                mProgress.dismiss();
            }
        });

        return transfertokens;
    }

    public void retrieveUser(final String userId){
        mUser.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = String.valueOf(dataSnapshot.child("username").getValue());
                avatar = String.valueOf(dataSnapshot.child("profile_picture").getValue());
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_users_bio)).child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        about = String.valueOf(dataSnapshot.child("about").getValue());
                        txtUsername.setText(username);
                        if (!dataSnapshot.exists() || dataSnapshot.child("about").getValue().equals("")){
                            txtAbout.setText(R.string.info_bio_info_not_available);
                        } else {
                            txtAbout.setText(about);
                        }
                        if (!avatar.isEmpty()){
                            Picasso.with(getContext()).load(avatar).into(imgAvatar);
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_baseline_person_24_theme);
                        }
                        layoutAvatar.setVisibility(View.VISIBLE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1){
            if (resultCode == Activity.RESULT_OK){
                userId = data.getStringExtra("userId");
                retrieveUser(userId);
                itemKey = "token-transfer_"+mAuth.getCurrentUser().getUid();
            }
        }
    }

    public void transferTokens(){
        String amount = txtTokenAmount.getText().toString().trim();
//        String email = txtUserEmail.getText().toString().trim();
        if (amount.isEmpty() && !userId.isEmpty()){
            mAlert.setTitle(getString(R.string.title_enter_required_info))
                    .setMessage(getString(R.string.hint_enter_amount))
                    .setPositiveButton(getString(R.string.option_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .show();
        }

        if (userId.isEmpty() && !amount.isEmpty()){
            mAlert2.setTitle(R.string.title_enter_required_info)
                    .setMessage(R.string.info_select_recipient_tap_button_below)
                    .setPositiveButton(R.string.option_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton(R.string.option_select_user_from_list, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult((new Intent(getActivity(),SearchUserActivity.class)),GET_USER_REQUEST);
                        }
                    })
                    .show();
        }
        if (amount.isEmpty() && userId.isEmpty()){
            Toast.makeText(getActivity(), R.string.info_please_enter_required_info, Toast.LENGTH_SHORT).show();
        }
        if (!amount.isEmpty() && !userId.isEmpty()){
            Double transferAmount = Double.parseDouble(amount);
            if (transferAmount<=0){
                Toast.makeText(getActivity(), getString(R.string.info_invalid_amount), Toast.LENGTH_SHORT).show();
            } else {
                createFinancialAccount();
            }
        }

    }

    public void createFinancialAccount(){
        mProgress.setMessage(getString(R.string.info_your_financial_account_is_being_configured));
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mProgress.show();
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid());
                    mMonetaryAccount.child("email").setValue(mAuth.getCurrentUser().getEmail());
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getActivity(), getString(R.string.info_setup_complete)+"!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
                checkBalance(itemKey);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), getString(R.string.error_an_error_occurred_while_configuring_your_account)+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkBalance(final String key){
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.show();
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Double buyerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                FirebaseDatabase.getInstance().getReference().child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Double price = Double.parseDouble(txtTokenAmount.getText().toString().trim());

                        if (buyerBalance<price){
                            Toast.makeText(getActivity(), getString(R.string.info_your_balance_is_insufficient_by)+" "+(price-buyerBalance)+" tokens", Toast.LENGTH_SHORT).show();
                            mProgress.dismiss();
                        } else {
                            getReceiverDetails(itemKey,buyerBalance,price);
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

    public void getReceiverDetails(final String key, final Double bal, final Double amt){
        FirebaseDatabase.getInstance().getReference().child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String receiverId = userId;
                final Double price = Double.parseDouble(txtTokenAmount.getText().toString().trim());
                final String[] receiverEmail = new String[1];
                FirebaseDatabase.getInstance().getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        receiverEmail[0] = String.valueOf(dataSnapshot.child("email").getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            if (!dataSnapshot.exists()){
                                DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(receiverId);
                                mMonetaryAccount.child("email").setValue(receiverEmail[0]);
                                mMonetaryAccount.child("previous_balance").setValue(0.00);
                                mMonetaryAccount.child("current_balance").setValue(0.00);
                                mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                            }

                            Double sellerBalance = Double.parseDouble(String.valueOf(dataSnapshot.child("current_balance").getValue()));
                            debitSender(bal,amt,sellerBalance,price,receiverId);
                        }catch (NumberFormatException e){
                            mProgress.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.error_occurred_try_again), Toast.LENGTH_SHORT).show();
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

    public double debitSender(Double bal, Double amt, Double sbal, Double samt, String receiverId){
        Double previousBalance = bal;
        if (bal>=amt){
            bal = bal-amt;

            DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(mAuth.getCurrentUser().getUid());
            mMonetaryAccount.child("current_balance").setValue(bal);
            final String debitTime = sdf.format(Calendar.getInstance().getTime());
//            getSellerDetails(fileKey);
            creditReceiver(sbal,samt,receiverId,mAuth.getCurrentUser().getUid(),previousBalance,bal,debitTime);
        }
        return bal;
    }

    private double creditReceiver(Double bal, Double amt, String receiverId, String senderId, Double senderPBal, Double senderNBal, String debitTime){
        Double previousBalance = bal;
        bal = bal+amt;

        DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_account_monetary)).child(receiverId);
        mMonetaryAccount.child("current_balance").setValue(bal);

        final String creditTime = sdf.format(Calendar.getInstance().getTime());

        createRecord(senderId,receiverId,senderPBal,previousBalance,senderNBal,bal,amt,debitTime,creditTime);

        return bal;
    }

    public void createRecord(String buyerId, String sellerId, Double buyerPreviousBal, Double sellerPreviousBal, Double buyerNewBal, Double sellerNewBal, Double transAmount, String debitTime, String creditTime){
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
        mTransRecordDebit.child("ItemType").setValue("Token");
        mTransRecordDebit.child("RefNumber").setValue(String.valueOf(referenceNumber));

        DatabaseReference mTransRecordCredit = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Credit").child(key);
        mTransRecordCredit.child("User").setValue(sellerId);
        mTransRecordCredit.child("BalancePrevious").setValue(sellerPreviousBal);
        mTransRecordCredit.child("BalanceNew").setValue(sellerNewBal);
        mTransRecordCredit.child("TransactionCost").setValue(transAmount);
        mTransRecordCredit.child("Time").setValue(creditTime);
        mTransRecordCredit.child("Item").setValue(itemKey);
        mTransRecordCredit.child("ItemType").setValue("Token");
        mTransRecordCredit.child("RefNumber").setValue(String.valueOf(referenceNumber));
        sendMessage(String.valueOf(referenceNumber));

    }

    public void sendMessage(String ref){
        Double amount = Double.parseDouble(txtTokenAmount.getText().toString().trim());
        DatabaseReference mMessage = FirebaseDatabase.getInstance().getReference().child("Messages").child(userId).push();
        mMessage.child("sender_id").setValue(mAuth.getCurrentUser().getUid());
        mMessage.child("recipient_id").setValue(userId);
        mMessage.child("title").setValue("Tokens");
        mMessage.child("category").setValue("def_tokens_0000");
        mMessage.child("message").setValue("I have sent "+amount+" tokens to your account.");
        mMessage.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
        mMessage.child("reference").setValue(ref);

        Toast.makeText(getActivity(), R.string.info_transaction_successful, Toast.LENGTH_SHORT).show();
        mProgress.dismiss();
    }

}