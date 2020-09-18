package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditSubPostActivity extends AppCompatActivity {

    private TextView txtTime;
    private EditText txtTitle,txtDescription;
    private Button btnSave,btnCancel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String parentKey="",postType="",childKey="";

    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private int PUBLISH_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.Theme_UserDialogDark);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.Theme_UserDialogSignature);
        } else {
            setTheme(R.style.Theme_UserDialog);
        }
        setContentView(R.layout.activity_edit_sub_post);

        mAuth = FirebaseAuth.getInstance();

        parentKey = getIntent().getExtras().getString("parentKey");
        childKey = getIntent().getExtras().getString("childKey");
        postType = getIntent().getExtras().getString("postType");
        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);


        txtTitle = findViewById(R.id.txt_title);
        txtDescription = findViewById(R.id.txt_description);
        txtTime = findViewById(R.id.txt_time);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_sub)).child(postType)
                .child(parentKey).child(childKey);

        fetchValues();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlert.setTitle(R.string.title_save_post)
                        .setMessage(R.string.confirm_are_you_sure)
                        .setPositiveButton(getString(R.string.option_save), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (checkRequirements()){
                                    savePost();
                                }
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
    }

    private void fetchValues() {
        mProgress.setMessage(getString(R.string.info_please_wait));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkRequirements(){
        String title = txtTitle.getText().toString().trim();
        String description = txtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, R.string.info_title_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(description)){
            Toast.makeText(this, R.string.info_description_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void savePost() {
        mProgress.setMessage(getString(R.string.info_saving_post));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }

        mDatabase.child("title").setValue(txtTitle.getText().toString().trim());
        mDatabase.child("description").setValue(txtDescription.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(EditSubPostActivity.this, R.string.info_saved, Toast.LENGTH_SHORT).show();
                    if (mProgress.isShowing()){
                        mProgress.dismiss();
                    }
                } else {
                    Toast.makeText(EditSubPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
                    if (mProgress.isShowing()){
                        mProgress.dismiss();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditSubPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
                if (mProgress.isShowing()){
                    mProgress.dismiss();
                }
            }
        });

    }

    private boolean checkPublishState(){
        if (PUBLISH_STATE==0){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}