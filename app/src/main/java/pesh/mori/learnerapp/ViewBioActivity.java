package pesh.mori.learnerapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by MORIAMA on 07/03/2018.
 */

public class ViewBioActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.activity_viewbio );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());
    }
    public boolean onSupportNavigateUp(){

        finish();
        return true;
    }
}
