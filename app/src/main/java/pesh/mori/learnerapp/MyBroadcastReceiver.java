package pesh.mori.learnerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Nick Otto on 15/07/2019.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        // here we will receive the message
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                assert pdusObj != null;

                // check for the new message here
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    final String senderNum = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    FirebaseDatabase.getInstance().getReference().child("App").child("SMS").child("sender").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("LOG_sender",dataSnapshot.getValue().toString());
                            if (senderNum.contentEquals(dataSnapshot.getValue().toString())) {
                                // we are using sms from Africastalking api, so this is the sender
                                // supply the your sender correctly
                                // now we can save verification
                                TemporaryPermissions.saveVerificationValue(context);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } // end for loop
            } // bundle is null
        } catch (Exception e) {
            //Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }

}
