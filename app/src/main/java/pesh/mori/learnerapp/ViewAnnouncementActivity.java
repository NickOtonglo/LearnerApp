package pesh.mori.learnerapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewAnnouncementActivity extends AppCompatActivity {

    private LinearLayout layoutDoc;
    private FrameLayout filePlaceholder;
    private View bottomHorizontalBar2;
    private TextView txtTime,txtTitle,txtMessage,txtMoreInfo;
    private AppCompatButton btnOpenDoc;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Uri mUri = null;

    private String postKey="",filePath="";

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
        setContentView(R.layout.activity_view_announcement);

        new HomeActivity().checkMaintenanceStatus(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        postKey = getIntent().getExtras().getString("file_key");

        layoutDoc = findViewById(R.id.layout_4);

        txtTime = findViewById(R.id.txt_time);
        txtTitle = findViewById(R.id.txt_title);
        txtMessage = findViewById(R.id.txt_body);
        txtMoreInfo = findViewById(R.id.txt_more);

        filePlaceholder = findViewById(R.id.exoplayer_placeholder);
        bottomHorizontalBar2 = (View)findViewById(R.id.view_bottom_horizontal_bar_2);

        mDatabase = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_ref_posts_announcements)).child(postKey);

        txtMoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLink();
            }
        });

        btnOpenDoc = findViewById(R.id.btn_open_pdf);
        btnOpenDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String filePath = String.valueOf(dataSnapshot.child("file_path").getValue());
                        String docName = String.valueOf(dataSnapshot.child("title").getValue());
                        Intent docIntent = new Intent(getApplicationContext(),ReadDocument.class);
                        docIntent.putExtra("filePath",filePath);
                        docIntent.putExtra("docName",docName);
                        docIntent.putExtra("postKey",postKey);
                        docIntent.putExtra("outgoing_intent","ViewAnnouncementActivity");
                        startActivity(docIntent);
                        overridePendingTransition(R.transition.slide_in_from_bottom,R.transition.static_animation);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        fetchValues();
    }

    private void openLink() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("link").exists() && !dataSnapshot.child("link").getValue().equals("")){
                    String url = String.valueOf(dataSnapshot.child("link").getValue());
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchValues() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtTime.setText(String.valueOf(dataSnapshot.child("timestamp").getValue()));
                txtTitle.setText(String.valueOf(dataSnapshot.child("title").getValue()));
                getSupportActionBar().setSubtitle(String.valueOf(dataSnapshot.child("title").getValue()));
                txtMessage.setText(String.valueOf(dataSnapshot.child("body").getValue()));
                filePath = (String) dataSnapshot.child("file_path").getValue();
                if (!dataSnapshot.child("link").exists() || dataSnapshot.child("link").getValue().toString().equals("")){
                    txtMoreInfo.setText(R.string.info_none_at_the_moment);
                    txtMoreInfo.setTextColor(getResources().getColor(R.color.colorTextDisabled));
                } else {
                    SpannableString content = new SpannableString(String.valueOf(dataSnapshot.child("link").getValue()));
                    content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                    txtMoreInfo.setText(content);
                }
                if (dataSnapshot.child("file_type").getValue().equals("audio") || dataSnapshot.child("file_type").getValue().equals("video")){
                    filePlaceholder.setVisibility(View.VISIBLE);
                    Bundle bundle = new Bundle();
                    bundle.putString("file_path", filePath);
                    PanelTopFragment topFragment = new PanelTopFragment();
                    topFragment.setArguments(bundle);
                    FragmentManager manager = getSupportFragmentManager();
                    manager.beginTransaction().replace(R.id.exoplayer_placeholder,topFragment,topFragment.getTag())
                            .commit();
                } else if (dataSnapshot.child("file_type").getValue().equals("doc")){
                    layoutDoc.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
