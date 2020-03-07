package pesh.mori.learnerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class FacebookAuthHandlerActivity extends AppCompatActivity {
    private TextView txtMessage;
    private DatabaseReference mUser;
    private FirebaseAuth mAuth;
    private HashMap<String, String> hashMap;
    private Calendar calendar;
    private SimpleDateFormat sdf;

    //Facebook
    private Intent returnIntent;
    private CallbackManager mCallbackManager;
    private static final String TAG = "LOG_FACEBOOK_AUTH";
    private String userId="",firstName="",lastName="",email="",birthday="",gender="";
    private URL facebookProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_auth_handler);

        FacebookSdk.sdkInitialize(getApplicationContext());

        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mAuth = FirebaseAuth.getInstance();
        txtMessage = findViewById(R.id.txt_message);

        returnIntent = getIntent();

        facebookLogin();

    }

    public void facebookLogin(){
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(FacebookAuthHandlerActivity.this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e(TAG,object.toString());
                        Log.e(TAG,response.toString());
                        try {
                            userId = object.getString("id");
                            facebookProfile = new URL("https://graph.facebook.com/" + userId);
                            if(object.has("first_name"))
                                firstName = object.getString("first_name");
                            if(object.has("last_name"))
                                lastName = object.getString("last_name");
                            if (object.has("email"))
                                email = object.getString("email");
                            if (object.has("birthday"))
                                birthday = object.getString("birthday");
                            if (object.has("gender"))
                                gender = object.getString("gender");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                });
                //Here we put the requested fields to be returned from the JSONObject
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, birthday, gender");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                returnIntent.putExtra("LOG_RESULT","FAIL");
                setResult(Activity.RESULT_CANCELED,returnIntent);
                onBackPressed();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                returnIntent.putExtra("LOG_RESULT","FAIL");
                Toast.makeText(getApplicationContext(), "Authentication failed! "+error.getMessage(), Toast.LENGTH_LONG).show();
                setResult(Activity.RESULT_CANCELED,returnIntent);
                onBackPressed();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            updateUI("facebook");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed! "+task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            returnIntent.putExtra("LOG_RESULT","FAIL");
                            setResult(Activity.RESULT_CANCELED,returnIntent);
                            onBackPressed();
                        }
                    }
                });
    }

    private void updateUI(String vendor) {
        Log.d("updateUI","vendor: "+vendor);
        checkAccountExistence(vendor);
    }

    private void checkAccountExistence(final String vendor){
        if (mAuth!=null){
            Log.d("checkAccountExistence","mAuth not null");
            FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        Log.d("checkAccountExistence","account already exists");
                        returnIntent.putExtra("LOG_RESULT","SUCCESS");
                        setResult(Activity.RESULT_OK,returnIntent);
                        onBackPressed();
                    } else {
                        createAccount(vendor);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Log.d("checkAccountExistence","mAuth null");
            Toast.makeText(this, getString(R.string.error_occurred_try_again), Toast.LENGTH_SHORT).show();
            facebookLogin();
        }

    }

    private void createAccount(String vendor) {
        if (vendor.equals("facebook")) {
            final DatabaseReference mNewAccount = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mNewAccount.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mNewAccount.child("status").setValue("enabled");
                    mNewAccount.child("fname").setValue("");
                    mNewAccount.child("lname").setValue("");
                    mNewAccount.child("email").setValue(mAuth.getCurrentUser().getEmail());
                    mNewAccount.child("dob").setValue("");
                    mNewAccount.child("phone").setValue("");
                    mNewAccount.child("username").setValue(String.valueOf(Profile.getCurrentProfile().getFirstName())+" "+(Profile.getCurrentProfile().getLastName()));
                    mNewAccount.child("profile_picture").setValue("https://graph.facebook.com/" + userId + "/picture?type=large");
                    mNewAccount.child("timestamp").setValue(sdf.format(Calendar.getInstance().getTime()));
                    mNewAccount.child("account_manager").setValue("facebook").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            final DatabaseReference mNewBio = FirebaseDatabase.getInstance().getReference().child("Bio").child(mAuth.getCurrentUser().getUid());
                            mNewBio.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (task.isSuccessful()){
                                        if (!dataSnapshot.exists()){
                                            mNewBio.child("about").setValue("Hi! My name is "+firstName+" "+lastName+".");
//                                                mNewBio.child("facebook").setValue(String.valueOf(facebookProfile));
                                                mNewBio.child("facebook").setValue("");
                                            if (gender.equals("male") || gender.equals("Male") || gender.equals("MALE") ||
                                                    gender.equals("female") || gender.equals("Female") || gender.equals("FEMALE")){
                                                mNewBio.child("gender").setValue(gender.toLowerCase());
                                            } else {
                                                mNewBio.child("gender").setValue("not specified");
                                            }
                                            mNewBio.child("linkedin").setValue("");
                                            mNewBio.child("skill_details").setValue("");
                                            mNewBio.child("skills_sector").setValue("");
                                            mNewBio.child("twitter").setValue("");
                                        }
                                        returnIntent.putExtra("LOG_RESULT","SUCCESS");
                                        setResult(Activity.RESULT_OK,returnIntent);
//                                        Toast.makeText(FacebookAuthHandlerActivity.this, "Signed in as "+firstName+" "+lastName, Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
//                                    returnIntent.putExtra("LOG_RESULT","FAIL");
//                                    setResult(Activity.RESULT_CANCELED,returnIntent);
//                                    onBackPressed();
                                }
                            });
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                    returnIntent.putExtra("LOG_RESULT","FAIL");
//                    setResult(Activity.RESULT_CANCELED,returnIntent);
//                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
