package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.content.DialogInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadActivity extends AppCompatActivity {

    private final AppCompatActivity activity = UploadActivity.this;

    private Button button;
    private TextInputLayout textInputLayoutUniversity;
    private TextInputLayout textInputLayoutSchool;
    private TextInputLayout textInputLayoutDepartment;
    private TextInputLayout textInputLayoutCourse;
    private TextInputLayout textInputLayoutLecturer;
    private TextInputLayout textInputLayoutName;

    private TextInputEditText textInputEditTextUniversity;
    private TextInputEditText textInputEditTextSchool;
    private TextInputEditText textInputEditTextDepartment;
    private TextInputEditText textInputEditTextCourse;
    private TextInputEditText textInputEditTextLecturer;
    private TextInputEditText textInputEditTextName;

    private InputValidation inputValidation;
    private FloatingActionButton log_in;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_upload );
        mDrawerlayout = (DrawerLayout) findViewById( R.id.drawer );
        mToggle = new ActionBarDrawerToggle( this, mDrawerlayout, R.string.open, R.string.close );
        mDrawerlayout.addDrawerListener( mToggle );
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        initViews();
        initObjects();
        emptyInputEditText();

        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);


        button = (Button) findViewById( R.id.b_upload );
        button.startAnimation(myanim);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100 );
                return;
            }
        }

        enable_button();
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

    }
    private void initViews(){


        textInputLayoutName = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        textInputLayoutSchool = (TextInputLayout) findViewById(R.id.textInputLayoutSchool);
        textInputLayoutUniversity = (TextInputLayout) findViewById(R.id.textInputLayoutUniversity);
        textInputLayoutDepartment = (TextInputLayout) findViewById(R.id.textInputLayoutDepartment);
        textInputLayoutCourse = (TextInputLayout) findViewById(R.id.textInputLayoutCourse);
        textInputLayoutLecturer = (TextInputLayout) findViewById(R.id.textInputLayoutLecturer);

        textInputEditTextName = (TextInputEditText) findViewById(R.id.textInputEditTextName);
        textInputEditTextSchool = (TextInputEditText) findViewById(R.id.textInputEditTextSchool);
        textInputEditTextUniversity = (TextInputEditText) findViewById(R.id.textInputEditTextUniversity);
        textInputEditTextDepartment = (TextInputEditText) findViewById(R.id.textInputEditTextDepartment);
        textInputEditTextCourse = (TextInputEditText) findViewById(R.id.textInputEditTextCourse);
        textInputEditTextLecturer = (TextInputEditText) findViewById(R.id.textInputEditTextLecturer);}

    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }
    private void emptyInputEditText(){
        textInputEditTextName.setText(null);
        textInputEditTextSchool.setText(null);
        textInputEditTextUniversity.setText(null);
        textInputEditTextDepartment.setText(null);
        textInputEditTextCourse.setText(null);
        textInputEditTextLecturer.setText(null);}


    private void enable_button() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            enable_button();
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }

    ProgressDialog progress;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode == 10 && resultCode == RESULT_OK){

            progress = new ProgressDialog(UploadActivity.this);
            progress.setTitle("Uploading your file");
            progress.setMessage("Please wait...");
            progress.show();


            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {

                    File f  = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    String content_type  = getMimeType(f.getPath());

                    String file_path = f.getAbsolutePath();
                    OkHttpClient client = new OkHttpClient();
                    RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);

                    RequestBody request_body = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("type",content_type)
                            .addFormDataPart("uploaded_file",file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                            .build();

                    Request request = new Request.Builder()
                            .url("http://moripesh.com/upload2.php")
                            .post(request_body)
                            .build();

                    try {
                        Response response = client.newCall(request).execute();

                        if(!response.isSuccessful()){
                            throw new IOException("Error : "+response);

                        }
                        progress.dismiss();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

            });
            t.start();

        }else
            {Toast.makeText(this, "INFO | File Upload Failed", Toast.LENGTH_LONG).show();}



    }

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
    public void doThis(View view) {
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
    }
    public void viewThis (View view) {
        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
        button.startAnimation(myanim);
        if (!inputValidation.isInputEditTextFilled(textInputEditTextUniversity,textInputLayoutUniversity, getString(R.string.error_message_university))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextSchool,textInputLayoutSchool, getString(R.string.error_message_school))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextDepartment,textInputLayoutDepartment, getString(R.string.error_message_department))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextCourse,textInputLayoutCourse, getString(R.string.error_message_course))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextLecturer,textInputLayoutLecturer, getString(R.string.error_message_lecturer))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextName,textInputLayoutName, getString(R.string.error_message_name))) {
            return;
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "Your content shall be uploaded using the information provided." );
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new MaterialFilePicker()
                                .withActivity( UploadActivity.this )
                                .withRequestCode( 10 )
                                .start();}
                } );

        builder1.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(getApplicationContext(), InstitutionActivity.class));
                Toast.makeText(getApplicationContext(), "INFO | Upload Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();


    }
    public void tryThis (View view) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
        builder1.setMessage( "By declining Bids you reduce the chances of selling your content." );
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
