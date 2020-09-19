package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class ViewSubPostActivity extends AppCompatActivity {

    private TextView txtTitle,txtDescription,txtTime;

    private String parentKey, childKey,mAuthor;
    private DatabaseReference mPosts;
    private FirebaseAuth mAuth;

    private LinearLayout layoutImage,layoutDoc;
    private AlertDialog.Builder mAlert;
    private ProgressDialog mProgress;

    private ImageView imageView;
    private FrameLayout filePlaceholder;
    private View bottomHorizontalBar2;
    private Button btnOpenPDF;

    private String fileType,filePath;

    private String postType="";

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
        setContentView(R.layout.activity_view_sub_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAlert = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        mProgress = new ProgressDialog(this);

        parentKey = getIntent().getExtras().getString("file_key");
        mAuthor = getIntent().getExtras().getString("author");
        postType = getIntent().getExtras().getString("postType");
        childKey = getIntent().getExtras().getString("childKey");

        mAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.btn_view_select_image);
        layoutImage = findViewById(R.id.view_layout_1);
        filePlaceholder = findViewById(R.id.exoplayer_placeholder);
        bottomHorizontalBar2 = (View)findViewById(R.id.view_bottom_horizontal_bar_2);
        layoutDoc = findViewById(R.id.view_layout_5);
        btnOpenPDF = findViewById(R.id.btn_open_pdf);
        btnOpenPDF.setVisibility(View.GONE);

        txtTitle = findViewById(R.id.txt_view_title);
        txtDescription = findViewById(R.id.txt_view_description);
        txtTime = findViewById(R.id.txt_view_time);

        loadFileDetails();
    }

    public void loadFileDetails() {
        mPosts = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_sub)).child(postType)
                .child(parentKey).child(childKey);
        mPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                txtDescription.setText(String.valueOf(dataSnapshot.child("description").getValue()));
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                fileType = String.valueOf(dataSnapshot.child("file_type").getValue());
                filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
//                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child("title").getValue()));

                if (fileType.equals("image")){
                    layoutImage.setVisibility(View.VISIBLE);
                    Picasso.with(getApplicationContext()).load(filePath).into(imageView);
                    mProgress.dismiss();
                }
                else if (fileType.equals("audio") || fileType.equals("video")){
                    filePlaceholder.setVisibility(View.VISIBLE);
                    Bundle bundle = new Bundle();
                    bundle.putString("file_path", filePath);
                    PanelTopFragment topFragment = new PanelTopFragment();
                    topFragment.setArguments(bundle);
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction().replace(R.id.exoplayer_placeholder,topFragment,topFragment.getTag())
                            .commit();
                    mProgress.dismiss();
                }
                else if (fileType.equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                    mProgress.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void removePost(){
        mProgress.setMessage(getString(R.string.info_deleting_post));
        mProgress.setCancelable(false);
        mProgress.show();

        mPosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (!dataSnapshot.child("file_path").getValue().equals("")){
                        FirebaseStorage.getInstance().getReference().getStorage()
                                .getReferenceFromUrl(String.valueOf(dataSnapshot.child("file_path").getValue()))
                                .delete();
                    }
                    mPosts.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ViewSubPostActivity.this, getString(R.string.info_sub_post_removed), Toast.LENGTH_LONG).show();
                            if (mProgress.isShowing()){
                                mProgress.dismiss();
                            }
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        FirebaseDatabase.getInstance().getReference().child(postType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    if (String.valueOf(dataSnapshot.child(parentKey).child("author").getValue()).equals(mAuth.getCurrentUser().getUid())){
                        getMenuInflater().inflate(R.menu.menu_view_sub_post_author, menu);
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_edit_post:
                FirebaseDatabase.getInstance().getReference().child(postType).child(parentKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("author").getValue().equals(mAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(getApplicationContext(),EditSubPostActivity.class);
                                intent.putExtra("parentKey",parentKey)
                                        .putExtra("postType",postType)
                                        .putExtra("childKey",childKey);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                return true;
            case R.id.menu_delete_post:
                mAlert.setTitle(getString(R.string.title_delete_post))
                        .setMessage(getString(R.string.confirm_are_you_sure))
                        .setNegativeButton(getString(R.string.option_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(getString(R.string.option_alert_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference().child(postType).child(parentKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            if (dataSnapshot.child("author").getValue().equals(mAuth.getCurrentUser().getUid())){
                                                removePost();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.static_animation,R.transition.slide_in_from_top);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) filePlaceholder.getLayoutParams();
            params.height = params.MATCH_PARENT;
            params.width = params.MATCH_PARENT;
            filePlaceholder.setLayoutParams(params);
            bottomHorizontalBar2.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) filePlaceholder.getLayoutParams();
            params.height = (int)(200*getResources().getDisplayMetrics().density);
            params.width = params.MATCH_PARENT;
            filePlaceholder.setLayoutParams(params);
            bottomHorizontalBar2.setVisibility(View.GONE);
        }
    }
}