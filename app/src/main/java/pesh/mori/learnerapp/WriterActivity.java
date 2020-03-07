package pesh.mori.learnerapp;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;

/**
 * Created by MORIAMA on 08/03/2018.
 */

public class WriterActivity extends AppCompatActivity{

    private FloatingActionButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_writer );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());
    }
    public void doThis(View view) {
        btn = (FloatingActionButton) findViewById(R.id.button);
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        btn.startAnimation( myanim );

        // get a reference to the already created main layout
        FrameLayout mainLayout = (FrameLayout)
                findViewById( R.id.drawer);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService( LAYOUT_INFLATER_SERVICE );
        View popupView = inflater.inflate( R.layout.beawriterpopup_activity, null );


        // create the popup window
        int width = LayoutParams.MATCH_PARENT;
        int height = LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow( popupView, width, height, focusable );
        popupWindow.setAnimationStyle(R.style.animation);


        // show the popup window
        popupWindow.showAtLocation( mainLayout, Gravity.CENTER, 0, 0 );

        // dismiss the popup window when touched
    }
    public boolean onSupportNavigateUp(){

        finish();
        return true;
    }
}
