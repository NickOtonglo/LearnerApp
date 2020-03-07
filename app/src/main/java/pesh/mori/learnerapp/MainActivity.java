package pesh.mori.learnerapp;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private static int TIME_OUT = 5000;
    protected ImageView tv ;
    private ProgressBar spinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executeCommand();

    }

    private boolean executeCommand() {
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try {
            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue " + mExitValue);
            if (mExitValue == 0) {

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Intent i = new Intent(MainActivity.this,SelectLoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, TIME_OUT);

                return true;

            } else {

                AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
                builder1.setMessage( "No Internet connection. Please check your Internet connection." );
                builder1.setCancelable( false );

                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        } );
                builder1.setNeutralButton( "Reload" , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        executeCommand();
                        dialog.cancel();
                    }
                });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
            System.out.println(" Exception:" + ignore);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(" Exception:" + e);
        }
        return false;
    }
}
