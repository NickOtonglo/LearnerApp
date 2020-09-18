package pesh.mori.learnerapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TokensActivity_RedeemTokensFragment extends Fragment {
    public TokensActivity_RedeemTokensFragment(){};
    private Button btnRedeem;
    private TextView txtLink;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,mRedeemLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_redeemtokens, container, false);

        mAuth = FirebaseAuth.getInstance();
        checkAuth();

        txtLink = view.findViewById(R.id.txt_link);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mRedeemLink = FirebaseDatabase.getInstance().getReference().child("Links").child("RedeemTokens");

        mRedeemLink.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SpannableString content = new SpannableString(String.valueOf(dataSnapshot.getValue()));
                content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                txtLink.setText(content);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        txtLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink(mRedeemLink);
            }
        });

//        btnRedeem = view.findViewById(R.id.redeem);
//        btnRedeem.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.child("phone").getValue().equals("")){
//                            Toast.makeText(getContext(), "You have not linked a phone number to your account", Toast.LENGTH_LONG).show();
//                        } else if (!dataSnapshot.child("phone_verified").exists() && !dataSnapshot.child("phone_verified").getValue().equals("true")){
//                            Toast.makeText(getContext(), "Your phone number is not verified", Toast.LENGTH_LONG).show();
//                        } else {
//                            //Transaction is initiated here
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        });

        return view;
    }

    private void openLink(DatabaseReference mRef) {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String url = String.valueOf(dataSnapshot.getValue());
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                /*v1.0.4 bug fix 00001*/
                if (isAdded()){
                    startActivity(i);
                } else {
                    Toast.makeText(getActivity(), R.string.error_occurred_try_again,  Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void checkAuth(){
        if (mAuth.getCurrentUser() == null) {
            mAuth.signOut();
            Intent loginIntent = new Intent(getContext(), SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            getActivity().finish();
        }
    }

}
