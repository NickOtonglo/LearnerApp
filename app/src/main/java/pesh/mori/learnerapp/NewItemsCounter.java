package pesh.mori.learnerapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NewItemsCounter {
    protected static DatabaseReference mMessages;
    private static FirebaseAuth mAuth;

    public static void initValues(){
        mAuth = FirebaseAuth.getInstance();
        mMessages = FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid());
    }

    public static void countUnreadMessages(){
        initValues();
        mMessages.orderByChild("seen").equalTo("false").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("countUnreadMessages", String.valueOf(dataSnapshot.getChildrenCount()));
                getMessageCount(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static int getMessageCount(long count){
        Log.d("getMessageCount", String.valueOf(count));
        return (int)(long)count;
    }
}
