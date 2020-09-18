package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
//    private SlideAdapter myadapter;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        myadapter = new SlideAdapter(this);
//        viewPager.setAdapter(myadapter);
//        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);


        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent loginIntent = new Intent(WelcomeActivity.this,HomeActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }

        final TextView textView = (TextView) findViewById(R.id.skip);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                textView.startAnimation(myanim);
                startActivity(new Intent(getApplicationContext(), SelectLoginActivity.class));
                finish();

            }
        });
    }
}
