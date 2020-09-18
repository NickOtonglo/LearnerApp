package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditPostActivity extends AppCompatActivity {
    private LinearLayout layoutCat9,layoutCat8;
    private TextView txtTime;
    private EditText txtTitle,txtPrice,txtDescription;
    private EditText txtCourse, txtDept,txtUnit;
    private Spinner spinnerList9,spinnerList8;
    private RadioButton radioYes,radioNo;
    private Button btnSave,btnCancel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String postKey="",postNode="", selectedItem ="";
    private List<String> itemsList;

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
        setContentView(R.layout.activity_edit_post);

        mAuth = FirebaseAuth.getInstance();

        postKey = getIntent().getExtras().getString("postKey");
        postNode = getIntent().getExtras().getString("postNode");
        mProgress = new ProgressDialog(this);
        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);

        layoutCat9 = findViewById(R.id.layout_7);
        layoutCat8 = findViewById(R.id.layout_8);

        txtTitle = findViewById(R.id.txt_title);
        txtPrice = findViewById(R.id.txt_price);
        txtDescription = findViewById(R.id.txt_description);
        txtTime = findViewById(R.id.txt_time);

        txtCourse = findViewById(R.id.txt_course);
        txtDept = findViewById(R.id.txt_department);
        txtUnit = findViewById(R.id.txt_unit);

        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);

        spinnerList9 = findViewById(R.id.spinner_file_types);
        spinnerList8 = findViewById(R.id.spinner_category_8_list);

        radioNo = findViewById(R.id.radio_no);
        radioYes = findViewById(R.id.radio_yes);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(postNode).child(postKey);

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
                                    savePost(postNode, Double.parseDouble(txtPrice.getText().toString().trim()));
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
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
            layoutCat8.setVisibility(View.VISIBLE);
            layoutCat9.setVisibility(View.GONE);
        } else if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
            layoutCat9.setVisibility(View.VISIBLE);
            layoutCat8.setVisibility(View.GONE);
        }

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtPrice.setText(String.valueOf(dataSnapshot.child("price").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                String bidding = String.valueOf(dataSnapshot.child("biddable").getValue());
                if (bidding.equals("no")){
                    radioNo.setChecked(true);
                } else if (bidding.equals("yes")){
                    radioYes.setChecked(true);
                }
                //PostsNonCoursework
                if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
                    selectedItem = String.valueOf(dataSnapshot.child("tag").getValue());
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String tags = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_9)).getValue());
                            itemsList = Arrays.asList(tags.split("\\s*,\\s*"));
                            final List<String> getTags = new ArrayList<>();
                            final int facultySize = itemsList.size();
                            for (int i=0;i<facultySize;i++){
                                Object object = itemsList.get(i);
                                getTags.add(object.toString().trim());
                            }
                            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(EditPostActivity.this,android.R.layout.simple_spinner_item,getTags);
                            tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerList9.setAdapter(tagsAdapter);
                            spinnerList9.setSelection(tagsAdapter.getPosition(selectedItem));
                            if (mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //PostsCoursework
                } else if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
                    selectedItem = String.valueOf(dataSnapshot.child("institution").getValue());
                    txtCourse.setText(String.valueOf(dataSnapshot.child("course").getValue()));
                    txtDept.setText(String.valueOf(dataSnapshot.child("department").getValue()));
                    txtUnit.setText(String.valueOf(dataSnapshot.child("unit").getValue()));
                    FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_lists)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String tags = String.valueOf(dataSnapshot.child(getString(R.string.firebase_ref_lists_cat_8)).getValue());
                            itemsList = Arrays.asList(tags.split("\\s*,\\s*"));
                            final List<String> getTags = new ArrayList<>();
                            final int facultySize = itemsList.size();
                            for (int i=0;i<facultySize;i++){
                                Object object = itemsList.get(i);
                                getTags.add(object.toString().trim());
                            }
                            ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(EditPostActivity.this,android.R.layout.simple_spinner_item,getTags);
                            tagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerList8.setAdapter(tagsAdapter);
                            spinnerList8.setSelection(tagsAdapter.getPosition(selectedItem));
                            if (mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkRequirements(){
        String title = txtTitle.getText().toString().trim();
        String price = txtPrice.getText().toString().trim();
        Double priceVal = 0.0;
        if (!price.isEmpty()){
            priceVal = Double.parseDouble(price);
        }
        String description = txtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, R.string.info_title_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(price)){
            Toast.makeText(this, R.string.info_price_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (priceVal==0 && radioYes.isChecked()){
            Toast.makeText(this, R.string.info_you_cannot_allow_bidding_on_a_free_item, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(description)){
            Toast.makeText(this, R.string.info_description_must_not_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!radioNo.isChecked() && !radioYes.isChecked()){
            Toast.makeText(this, R.string.info_you_must_select_if_item_is_biddable_or_not, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_2))){
            String tag = spinnerList9.getSelectedItem().toString();
            if (TextUtils.equals(tag,"")){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_no_category_selected,Snackbar.LENGTH_SHORT).show();
                return false;
            }
        }
        if (postNode.equals(getString(R.string.firebase_ref_posts_type_1))){
            String course = txtCourse.getText().toString().trim();
            String department = txtDept.getText().toString().trim();
            String unit = txtUnit.getText().toString().trim();
            String institution = spinnerList8.getSelectedItem().toString();
            if(institution.equals("--University Institutions--")
                    || institution.equals("--Tertiary Institutions--")
                    || institution.equals("--Artisan Institutions--")){
                Snackbar.make(findViewById(android.R.id.content), R.string.you_must_select_a_valid_institution,Snackbar.LENGTH_LONG).show();
                return false;
            }
            if (course.isEmpty() || department.isEmpty() || unit.isEmpty()){
                Snackbar.make(findViewById(android.R.id.content), R.string.info_one_or_more_required_fields_is_empty, Snackbar.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private void savePost(final String category, final Double price) {
        mProgress.setMessage(getString(R.string.info_saving_post));
        mProgress.setCancelable(false);
        try {
            if (!mProgress.isShowing()){
                mProgress.show();
            }
        } catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
        FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_published)).child(mAuth.getCurrentUser().getUid()).child(postKey).child("Title")
                .setValue(txtTitle.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    mDatabase.child("title").setValue(txtTitle.getText().toString().trim());
                    mDatabase.child("price").setValue(price);
                    mDatabase.child("description").setValue(txtDescription.getText().toString().trim());
                    if (radioNo.isChecked()){
                        mDatabase.child("biddable").setValue("no");
                    } else if (radioYes.isChecked()){
                        mDatabase.child("biddable").setValue("yes");
                    }
                    if (category.equals(getString(R.string.firebase_ref_posts_type_1))){
                        mDatabase.child("institution").setValue(spinnerList8.getSelectedItem().toString());
                        mDatabase.child("unit").setValue(txtUnit.getText().toString());
                        mDatabase.child("department").setValue(txtDept.getText().toString());
                        mDatabase.child("course").setValue(txtCourse.getText().toString());
                    } else if (category.equals(getString(R.string.firebase_ref_posts_type_2))){
                        mDatabase.child("tag").setValue(spinnerList9.getSelectedItem().toString());
                    }

                    if (checkPublishState()){
//                        publishPost();
                    } else {
                        Toast.makeText(EditPostActivity.this, R.string.info_saved, Toast.LENGTH_SHORT).show();
                        if (mProgress.isShowing()){
                            mProgress.dismiss();
                        }
                    }
                } else {
                    Toast.makeText(EditPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
                    if (mProgress.isShowing()){
                        mProgress.dismiss();
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditPostActivity.this, R.string.error_unable_to_save, Toast.LENGTH_SHORT).show();
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