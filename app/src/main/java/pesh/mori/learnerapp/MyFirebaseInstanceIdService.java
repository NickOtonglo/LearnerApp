package pesh.mori.learnerapp;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Nick Otto on 10/12/2018.
 */

public class MyFirebaseInstanceIdService /**extends FirebaseInstanceIdService**/ {
    private static final String TAG = "MyFirebaseInstanceIdSer";

//    @Override
//    public void onTokenRefresh() {
//        String refreshToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG,"RefreshedToken: "+refreshToken);
//    }

    private void sendRegistrationToServer(String token){
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("msg_token")
                .setValue(token);
    }
}
