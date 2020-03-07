package pesh.mori.learnerapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by MORIAMA on 07/03/2018.
 */

public class RecentActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_recent );
    }
    public boolean onSupportNavigateUp(){

        finish();
        return true;
    }
}
