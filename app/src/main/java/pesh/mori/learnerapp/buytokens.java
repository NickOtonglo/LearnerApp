package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;

import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by MORIAMA on 18/11/2017.
 */

public class buytokens extends Fragment{


    public buytokens(){};
    private Button btnBuy;

    private RequestQueue requestQueue;
    private static final String URL = "https://moripesh.com/brian/brian/stkPush.php";
    private StringRequest request;
    private TextInputEditText txtAmount;
    private AppCompatRadioButton radioMpesa;
    private Double amount = 0.0;
    private String phone;
    private TextView txtHelp;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog mProgress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buytokens, container, false);

        mAuth = FirebaseAuth.getInstance();
        checkAuth();

        mProgress = new ProgressDialog(getActivity());
        txtHelp = view.findViewById(R.id.txt_help);
        txtAmount = view.findViewById(R.id.textInputEditTextbuy);
        radioMpesa = view.findViewById(R.id.mpesa);

        txtHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),BuyTokenGuide.class));
                getActivity().overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //The button that will initiate the M-Pesa Transaction (C2B and STK Push)
        btnBuy = view.findViewById(R.id.buy);
        btnBuy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("phone").getValue().equals("")){
                            Toast.makeText(getContext(), "You have not linked a phone number to your account", Toast.LENGTH_LONG).show();
                        } else if (!dataSnapshot.child("phone_verified").exists() && !dataSnapshot.child("phone_verified").getValue().equals("true")){
                            Toast.makeText(getContext(), "Your phone number is not verified", Toast.LENGTH_LONG).show();
                        } else {
                            createFinancialAccount();
                            phone = dataSnapshot.child("phone").getValue().toString();
                            if (radioMpesa.isChecked()){
                                if (txtAmount.getText().toString().equals("")){
                                    Toast.makeText(getActivity(), "Enter amount", Toast.LENGTH_SHORT).show();
                                } else
                                amount = Double.parseDouble(txtAmount.getText().toString());
                                if (amount<=0){
                                    Toast.makeText(getActivity(), "Invalid amount", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Initiate the M-Pesa Transaction here
                                    Toast.makeText(getActivity(), "Please wait for up to 20 seconds", Toast.LENGTH_LONG).show();
                                    int transAmount = amount.intValue();
                                    final String urlParams = URL+"?amount="+transAmount+"&account=0"+phone.substring(4)+"&phone="+phone.substring(1);
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, urlParams, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.d("Volley","onResponse: "+response);
                                            Log.d("HTTP_Request","URL: "+urlParams);
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getActivity(), "Network error!", Toast.LENGTH_SHORT).show();
//                                            Log.d("Volley","error: "+error);
                                        }
                                    });

                                    Volley.newRequestQueue(getActivity()).add(stringRequest);
                                }
                            } else {
                                Toast.makeText(getActivity(), "Select a payment method first", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }

    public void createFinancialAccount(){
        mProgress.setMessage("Your financial account is being configured. Please wait...");
        FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    mProgress.show();
                    DatabaseReference mMonetaryAccount = FirebaseDatabase.getInstance().getReference().child("MonetaryAccount").child(mAuth.getCurrentUser().getUid());
                    mMonetaryAccount.child("email").setValue(mAuth.getCurrentUser().getEmail());
                    mMonetaryAccount.child("previous_balance").setValue(0.00);
                    mMonetaryAccount.child("current_balance").setValue(0.00);
                    mMonetaryAccount.child("status").setValue("enabled"); //enabled or disabled
                    Toast.makeText(getActivity(), "Setup complete!", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "An error occurred while configuring your account: "+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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



