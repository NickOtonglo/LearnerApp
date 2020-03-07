package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by MORIAMA on 07/02/2018.
 */

public class ContentDiyActivity extends AppCompatActivity{

    private FloatingActionButton btn;
    private Handler handler;
    private ProgressDialog progressDialog;
    private Context context;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;


    private CircleImageView profile;


    private static CustomProgressBar progressBar = new CustomProgressBar();


    private static int TIME_OUT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_contentdiy );
        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,mDrawerlayout,R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(getApplicationContext(), "INFO | Click content to Preview", Toast.LENGTH_SHORT).show();

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        return mToggle.onOptionsItemSelected( item ) || super.onOptionsItemSelected( item );
    }
    public void doThis(MenuItem item){
        switch (item.getItemId()) {
            case R.id.buy:
                Intent intent1 = new Intent( this, TokensActivity.class );
                this.startActivity( intent1 );
        }
        switch (item.getItemId()) {
            case R.id.myfiles:
                Intent intent1 = new Intent( this, MyFilesActivity.class );
                this.startActivity( intent1 );

        }
        switch (item.getItemId()) {
            case R.id.social:
                Intent intent1 = new Intent( this, SocialActivity.class );
                this.startActivity( intent1 );

        }
        switch (item.getItemId()) {
            case R.id.messages:
                Intent intent1 = new Intent( this, MymessagesActivity.class );
                this.startActivity( intent1 );

        }
        switch (item.getItemId()) {
            case R.id.logout:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Exit L'earnerApp?");
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        moveTaskToBack(true);
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                        System.exit(1);
                                    }
                                })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
        }
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent1 = new Intent( this, ProfileActivity.class );
                this.startActivity( intent1 );
        }
        switch (item.getItemId()) {
            case R.id.report:
                Intent intent1 = new Intent( this, ReportPostActivity.class );
                this.startActivity( intent1 );
        }
        switch (item.getItemId()) {
            case R.id.pc:
                Intent intent1 = new Intent( this, MypcActivity.class );
                this.startActivity( intent1 );
        }
        switch (item.getItemId()) {
            case R.id.share:
                startActivity(new Intent(getApplicationContext(), ShareActivity.class));


        }
        switch (item.getItemId()) {
            case R.id.recent:
                startActivity(new Intent(getApplicationContext(), RecentActivity.class));
        }

    }



    public void viewThis(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://docs.google.com/gview?embedded=true&url=http://moripesh.com/DIY learning uploads/AutopsyPresentation.pptx"));
        startActivity(browserIntent);
    }
    public void doThis(View view) {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dropmenu, menu);
        return true;
    }
    public void openThis(View view) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading selected file...");
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        handler = new Handler();
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "The token amount displayed shall be deducted from your account." );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(), "ALERT | You have insufficient Tokens.",Toast.LENGTH_LONG ).show();
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpClient client = new OkHttpClient();
                                Request request = new Request.Builder().url("http://moripesh.com/DIY learning uploads/AutopsyPresentation.pptx").build();




                                Response response = null;
                                try {
                                    response = client.newCall(request).execute();
                                    float file_size = response.body().contentLength();

                                    BufferedInputStream inputStream = new BufferedInputStream(response.body().byteStream());
                                    OutputStream stream = new FileOutputStream( Environment.getExternalStorageDirectory()+"/L'earnerApp/AutopsyPresentation.pptx");

                                    byte[] data = new byte[8192];
                                    float total = 0;
                                    int read_bytes=0;

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            progressDialog.show();
                                        }

                                    });



                                    while ( (read_bytes = inputStream.read(data)) != -1 ){

                                        total = total + read_bytes;
                                        stream.write( data, 0, read_bytes);
                                        progressDialog.setProgress((int) ((total / file_size)*100));


                                    }


                                    progressDialog.dismiss();
                                    stream.flush();
                                    stream.close();
                                    response.body().close();

                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }

                        });
                        t.start();
                    }
                });

        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(getApplicationContext(), "INFO | Download Cancelled", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }
    public void onButtonShowPopupWindowClick(View view){
        btn = (FloatingActionButton) findViewById(R.id.btn);
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        btn.startAnimation( myanim );

        DrawerLayout mainLayout = (DrawerLayout)
                findViewById( R.id.drawer);

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService( LAYOUT_INFLATER_SERVICE );
        View popupView = inflater.inflate( R.layout.downloadpopup_activity, null );

        // create the popup window
        int width = LayoutParams.MATCH_PARENT;
        int height = LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow( popupView, width, height, focusable );
        popupWindow.setAnimationStyle(R.style.animation);

        // show the popup window
        popupWindow.showAtLocation( mainLayout, Gravity.BOTTOM, 0, 0 );

    }
    public void seeThis (View view){
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "This content is not biddable." );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                } );

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
