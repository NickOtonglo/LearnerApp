package pesh.mori.learnerapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputFilter;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rilixtech.CountryCodePicker;

import java.util.Calendar;

/**
 * Created by MORIAMA on 10/12/2017.
 */

public class RegistrationActivity extends AppCompatActivity {

    private final AppCompatActivity activity = RegistrationActivity.this;

    private ImageView button;
    private TextInputLayout textInputLayoutDob;
    private TextInputLayout textInputLayoutLname;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPhone;
    private TextInputLayout textInputLayoutCode;
    private TextInputLayout textInputLayoutFname;

    private TextInputEditText textInputEditTextFname;
    private TextInputEditText textInputEditTextLname;
    private TextInputEditText textInputEditTextDob;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPhone;
    private TextInputEditText textInputEditTextCode;
    private static TextInputEditText DateEdit;

    private Button verify;

    private InputValidation inputValidation;

    private FirebaseAuth mAuth;
    private FirebaseUser mUSer;
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        setContentView( R.layout.activity_register );

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        if (!checkNetworkState()){
            Snackbar.make(findViewById(android.R.id.content),R.string.error_no_internet_connectivity,Snackbar.LENGTH_LONG).show();
        }

        DateEdit = (TextInputEditText) findViewById( R.id.textInputEditTextdob );
        DateEdit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTruitonDatePickerDialog( v );
            }
        } );
//        Spinner spinner = (Spinner) findViewById( R.id.spinner4 );
        ArrayAdapter adapter = ArrayAdapter.createFromResource( this,
                R.array.countries_array, R.layout.spinner_item );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
//        spinner.setAdapter( adapter );

        initViews();
        initObjects();
        emptyInputEditText();

        ccp = (CountryCodePicker)findViewById(R.id.ccp);

        final Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);

        verify = (Button) findViewById( R.id.verify );

        button = (ImageView) findViewById( R.id.next );
        button.startAnimation(myanim);
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.startAnimation(myanim);
                if (!inputValidation.isInputEditTextFilled( textInputEditTextFname, textInputLayoutFname, getString( R.string.error_message_firstname ) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextLname, textInputLayoutLname, getString( R.string.error_message_lastname ) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextDob, textInputLayoutDob, getString( R.string.error_message_dob ) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextEmail, textInputLayoutEmail, getString( R.string.error_message_email ) )) {
                    return;
                }
//                if (!inputValidation.isInputEditTextFilled( textInputEditTextPhone, textInputLayoutPhone, getString( R.string.error_message_phone ) )) {
//                    return;
//                }
//                if (!inputValidation.isInputEditTextFilled( textInputEditTextCode, textInputLayoutCode, getString( R.string.error_message_code ) )) {
//                    return;
//                }

                checkDetails();

//                startActivity( new Intent( getApplicationContext(), CredentialsActivity.class ) );
//                finish();
            }
        } );
    }

    private void initViews() {

        textInputLayoutFname = (TextInputLayout) findViewById( R.id.textInputLayoutfirstname );
        textInputLayoutLname = (TextInputLayout) findViewById( R.id.textInputLayoutlastname );
        textInputLayoutDob = (TextInputLayout) findViewById( R.id.textInputLayoutdob );
        textInputLayoutEmail = (TextInputLayout) findViewById( R.id.textInputLayoutemail );
        textInputLayoutPhone = (TextInputLayout) findViewById( R.id.textInputLayoutphone );
        textInputLayoutCode = (TextInputLayout) findViewById( R.id.textInputLayoutcode );

        textInputEditTextFname = (TextInputEditText) findViewById( R.id.textInputEditTextfirstname );
        textInputEditTextLname = (TextInputEditText) findViewById( R.id.textInputEditTextlastname );
        textInputEditTextDob = (TextInputEditText) findViewById( R.id.textInputEditTextdob );
        textInputEditTextEmail = (TextInputEditText) findViewById( R.id.textInputEditTextemail );
        textInputEditTextPhone = (TextInputEditText) findViewById( R.id.textInputEditTextphone );
        textInputEditTextPhone.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(10) });
        textInputEditTextCode = (TextInputEditText) findViewById( R.id.textInputEditTextcode );
    }

    private void initObjects() {
        inputValidation = new InputValidation( activity );
    }

    private void emptyInputEditText() {
        textInputEditTextFname.setText( null );
        textInputEditTextLname.setText( null );
        textInputEditTextDob.setText( null );
        textInputEditTextEmail.setText( null );
        textInputEditTextPhone.setText( null );
        textInputEditTextCode.setText( null );
    }

    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show( getSupportFragmentManager(), "datePicker" );
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get( Calendar.YEAR );
            int month = c.get( Calendar.MONTH );
            int day = c.get( Calendar.DAY_OF_MONTH );

            // Create a new instance of DatePickerDialog and return it

            return new DatePickerDialog( getActivity(), this, year, month, day );
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            DateEdit.setText( day + "/" + (month + 1) + "/" + year );
        }
    }
    public void doThis(View view) {
//        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.bounce3);
//        verify.startAnimation(myanim);
//        if (!inputValidation.isInputEditTextFilled( textInputEditTextPhone, textInputLayoutPhone, getString( R.string.error_message_phone ) )) {
//            return;
//        }
//        AlertDialog.Builder builder1 = new AlertDialog.Builder( this );
//        builder1.setMessage( "Verification Code has been sent to the above phone number. This code expires in 5 minutes." );
//        builder1.setCancelable( false );
//
//        builder1.setPositiveButton(
//                "Ok",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                } );
//
//        AlertDialog alert11 = builder1.create();
//        alert11.show();
    }
    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

//    public void checkDetails(){
//        String fname = textInputEditTextFname.getText().toString().trim();
//        String lname = textInputEditTextLname.getText().toString().trim();
//        String dob = textInputEditTextDob.getText().toString().trim();
//        String email = textInputEditTextEmail.getText().toString().trim();
//        String phone = textInputEditTextPhone.getText().toString().trim();
//        String code = textInputEditTextCode.getText().toString().trim();
//
//        if (phone.length()<9 || phone.length()>10){
//            textInputEditTextPhone.setError("Invalid phone number");
//
//        } else {
//            if (!fname.isEmpty() && !lname.isEmpty() && !dob.isEmpty() && !email.isEmpty()){
//                if (isEmailValid(email)){
//
//                    final String phoneNumber = ccp.getFullNumberWithPlus()+phone.substring(phone.length() - 9);
//
//                    Intent i = new Intent(getApplicationContext(),CredentialsActivity.class);
//                    i.putExtra("activity_from","RegistrationActivity");
//                    i.putExtra("fname",fname);
//                    i.putExtra("lname",lname);
//                    i.putExtra("dob",dob);
//                    i.putExtra("email",email);
////                    i.putExtra("phone",phoneNumber);
//                    i.putExtra("phone","");
//                    Bundle extras = new Bundle();
//                    i.putExtras(extras);
//                    startActivity(i);
//                } else {
//                    textInputEditTextEmail.setError("Invalid email format");
//                }
//            }
//        }
//    }

    public void checkDetails(){
        String fname = textInputEditTextFname.getText().toString().trim();
        String lname = textInputEditTextLname.getText().toString().trim();
        String dob = textInputEditTextDob.getText().toString().trim();
        String email = textInputEditTextEmail.getText().toString().trim();

        if (!fname.isEmpty() && !lname.isEmpty() && !dob.isEmpty() && !email.isEmpty()){
            if (isEmailValid(email)){
                Intent i = new Intent(getApplicationContext(),CredentialsActivity.class);
                i.putExtra("activity_from","RegistrationActivity");
                i.putExtra("fname",fname);
                i.putExtra("lname",lname);
                i.putExtra("dob",dob);
                i.putExtra("email",email);
                i.putExtra("phone","");
                Bundle extras = new Bundle();
                i.putExtras(extras);
                startActivity(i);
            } else {
                textInputEditTextEmail.setError("Invalid email format");
            }
        }
    }

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean checkNetworkState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}



