package pesh.mori.learnerapp;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PhoneVerificationActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private TextView txtMessage;
    private String phoneNumber = "";
    private int code;
    private String senderNum="";
    private String verificationLink = "https://moripesh.com/learnerappapi/phone_verification.php/";

    private Handler handler;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (TemporaryPermissions.isUserVerified(PhoneVerificationActivity.this)) {
                finish();
                Toast.makeText(PhoneVerificationActivity.this, R.string.info_your_phone_number_is_verified, Toast.LENGTH_SHORT).show();

                // stop handler from updating
                handler.removeCallbacks(runnable);
            }

            handler.postDelayed(runnable, 1000); // keep checking after every one second
        }
    };

    //https://developers.google.com/identity/sms-retriever/user-consent/request
    private static final int SMS_CONSENT_REQUEST = 2;  // Set to an unused request code
    private final BroadcastReceiver smsVerificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                Status smsRetrieverStatus = (Status) extras.get(SmsRetriever.EXTRA_STATUS);

                switch (smsRetrieverStatus.getStatusCode()) {
                    case CommonStatusCodes.SUCCESS:
                        // Get consent intent
                        Intent consentIntent = extras.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            startActivityForResult(consentIntent, SMS_CONSENT_REQUEST);
                        } catch (ActivityNotFoundException e) {
                            // Handle the exception ...
                        }
                        break;
                    case CommonStatusCodes.TIMEOUT:
                        // Time out occurred, handle the error.
                        break;
                }
            }
        }
    };

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
        setContentView(R.layout.activity_phone_verification);

        mProgressBar = findViewById(R.id.progress_bar);
        txtMessage = findViewById(R.id.txt_message);

        phoneNumber = getIntent().getExtras().getString("phone");
        int low = 10000;
        int high = 100000;
        code = new Random().nextInt(high-low) + low;

        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsVerificationReceiver, intentFilter);
        FirebaseDatabase.getInstance().getReference().child("App").child("SMS").child("sender").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                senderNum = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        verifyPhoneNumber(phoneNumber);

        handler = new Handler();
        handler.postDelayed(runnable, 1000);

        runnable.run();

        if (TemporaryPermissions.isUserVerified(PhoneVerificationActivity.this)) {
            finish();
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
//            startActivity(new Intent(this, HomeActivity.class));
        }
    }

    private void verifyPhoneNumber(final String phoneNumber) {
        Task<Void> task = SmsRetriever.getClient(this).startSmsUserConsent(senderNum /* or null */);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("LOG_verifyPhoneNumber","success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("LOG_verifyPhoneNumber","failure");
            }
        });
        TemporaryPermissions.holdVerificationCode(PhoneVerificationActivity.this, String.valueOf(code));
        FirebaseDatabase.getInstance().getReference().child("Links").child("PhoneVerificationScript").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.getValue().equals("")){
                    verificationLink = dataSnapshot.getValue().toString();
                }
                final String url = verificationLink+"?pid="+phoneNumber+"&code="+code;
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                                progressDialog.dismiss();
                        Log.d("Volley","onResponse: "+response);
                        Log.d("HTTP_Request","URL: "+url);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        finish();
                        Toast.makeText(PhoneVerificationActivity.this, getString(R.string.error_network_error), Toast.LENGTH_SHORT).show();
                        Log.d("VolleyError",""+error.getMessage());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        params.put("phone", phoneNumber);

                        return params;
                    }
                };

                Volley.newRequestQueue(PhoneVerificationActivity.this).add(stringRequest);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void verifyPhoneNumber(final String phoneNumber) {
//        Dexter.withActivity(this)
//                .withPermissions(Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS)
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                        if (multiplePermissionsReport.areAllPermissionsGranted()){
//                            TemporaryPermissions.holdVerificationCode(PhoneVerificationActivity.this,String.valueOf(code));
//                            final String url = "https://moripesh.com/learnerappapi/phone_verification.php/?pid="+phoneNumber+"&code="+code;
//                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
////                                progressDialog.dismiss();
//                                    Log.d("Volley","onResponse: "+response);
//                                    Log.d("HTTP_Request","URL: "+url);
//
//                                }
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
////                                progressDialog.dismiss();
//                                    finish();
//                                    Toast.makeText(PhoneVerificationActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
//                                    Log.d("VolleyError",""+error.getMessage());
//                                }
//                            }) {
//                                @Override
//                                protected Map<String, String> getParams() {
//                                    Map<String, String> params = new HashMap<>();
//
//                                    params.put("phone", phoneNumber);
//
//                                    return params;
//                                }
//                            };
//
//                            Volley.newRequestQueue(PhoneVerificationActivity.this).add(stringRequest);
//                        }
//                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()){
//                            showSettingsDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                        permissionToken.continuePermissionRequest();
//                    }
//                }).onSameThread().check();
//    }

    private void showSettingsDialog() {
        AlertDialog.Builder mAlert = new AlertDialog.Builder(PhoneVerificationActivity.this,R.style.AlertDialogStyle);
        mAlert.setTitle(R.string.title_alert_permission_denied);
        mAlert.setMessage(R.string.info_alert_permission_denied);
        mAlert.setPositiveButton(R.string.option_alert_go_back, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                onBackPressed();
            }
        });
        mAlert.show();
    }

//    private void showSettingsDialog() {
//        AlertDialog.Builder mAlert = new AlertDialog.Builder(PhoneVerificationActivity.this,R.style.AlertDialogStyle);
//        mAlert.setTitle(R.string.title_alert_permissions_needed);
//        mAlert.setMessage(R.string.info_alert_permissions_needed);
//        mAlert.setPositiveButton(R.string.option_alert_open_settings, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//                openSettings();
//            }
//        });
//        mAlert.setNegativeButton(R.string.option_alert_close, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        mAlert.show();
//    }

    //Navigating user to app settings
//    private void openSettings() {
//        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", getPackageName(), null);
//        intent.setData(uri);
//        startActivityForResult(intent, 101);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // ...
            case SMS_CONSENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Get SMS message content
                    String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);
                    // Extract one-time code from the message and complete verification
                    // `sms` contains the entire text of the SMS message, so you will need
                    // to parse the string.
                    String vCode = getVerificationCode(message); // define this function
                    handleVerification(vCode);
                    // send one time code to the server
                } else {
                    // Consent canceled, handle the error ...
                    showSettingsDialog();
                }
                break;
        }
    }

    private String getVerificationCode(String message) {
        String vCode = message.replaceAll("[^0-9]", "");
        return vCode;
    }

    public void handleVerification(String msgCode){
        /*v1.0.4 bug fix 00003*/
        try {
            int vCode = Integer.parseInt(msgCode);
            if (vCode==code){
                TemporaryPermissions.saveVerificationValue(getApplicationContext());
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
            Toast.makeText(this, R.string.error_occurred_try_again, Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (TemporaryPermissions.isUserVerified(PhoneVerificationActivity.this)) {
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
            finish();
//             startActivity(new Intent(this, HomeActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
    }

}
