package pesh.mori.learnerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by MORIAMA on 01/02/2018.
 */

public class MypcActivity extends AppCompatActivity {
    private static int TIME_OUT = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_mypc );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        new Handler().postDelayed( new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "INFO | PC connection failed", Toast.LENGTH_LONG).show();
                finish();
            }
        }, TIME_OUT);
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent clearIntent = new Intent(getApplicationContext(),HomeActivity.class);
        clearIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(clearIntent);
        finish();
    }
}
