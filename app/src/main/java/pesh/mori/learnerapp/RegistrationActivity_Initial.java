package pesh.mori.learnerapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rilixtech.CountryCodePicker;

import java.util.Calendar;

/**
 * Created by MORIAMA on 10/12/2017.
 */

public class RegistrationActivity_Initial extends AppCompatActivity {

    private final AppCompatActivity activity = RegistrationActivity_Initial.this;

    private AppCompatButton button;
    private TextInputLayout textInputLayoutDob;
    private TextInputLayout textInputLayoutLname;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutFname;

    private TextInputEditText textInputEditTextFname;
    private TextInputEditText textInputEditTextLname;
    private TextInputEditText textInputEditTextDob;
    private TextInputEditText textInputEditTextEmail;
    private static TextInputEditText DateEdit;

    private InputValidation inputValidation;

    private FirebaseAuth mAuth;
    private FirebaseUser mUSer;
    private CountryCodePicker ccp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView( R.layout.activity_register );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        if (!checkNetworkState()){
            Snackbar.make(findViewById(android.R.id.content),R.string.error_no_internet_connectivity,Snackbar.LENGTH_LONG).show();
        }

        DateEdit = (TextInputEditText) findViewById( R.id.txt_dob);
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

        button = (AppCompatButton) findViewById( R.id.next );
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inputValidation.isInputEditTextFilled( textInputEditTextFname, textInputLayoutFname, getString( R.string.hint_enter_your_first_name) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextLname, textInputLayoutLname, getString( R.string.hint_enter_your_last_name) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextDob, textInputLayoutDob, getString( R.string.hint_enter_your_dob) )) {
                    return;
                }
                if (!inputValidation.isInputEditTextFilled( textInputEditTextEmail, textInputLayoutEmail, getString( R.string.hint_enter_your_email_address) )) {
                    return;
                }
                checkDetails();
            }
        } );
    }

    private void initViews() {

        textInputLayoutFname = (TextInputLayout) findViewById( R.id.txt_parent_first_name);
        textInputLayoutLname = (TextInputLayout) findViewById( R.id.txt_parent_last_name);
        textInputLayoutDob = (TextInputLayout) findViewById( R.id.txt_parent_dob);
        textInputLayoutEmail = (TextInputLayout) findViewById( R.id.txt_parent_email);

        textInputEditTextFname = (TextInputEditText) findViewById( R.id.txt_first_name);
        textInputEditTextLname = (TextInputEditText) findViewById( R.id.txt_last_name);
        textInputEditTextDob = (TextInputEditText) findViewById( R.id.txt_dob);
        textInputEditTextEmail = (TextInputEditText) findViewById( R.id.txt_email);
    }

    private void initObjects() {
        inputValidation = new InputValidation( activity );
    }

    private void emptyInputEditText() {
        textInputEditTextFname.setText( null );
        textInputEditTextLname.setText( null );
        textInputEditTextDob.setText( null );
        textInputEditTextEmail.setText( null );
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

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void checkDetails(){
        String fname = textInputEditTextFname.getText().toString().trim();
        String lname = textInputEditTextLname.getText().toString().trim();
        String dob = textInputEditTextDob.getText().toString().trim();
        String email = textInputEditTextEmail.getText().toString().trim();

        if (!fname.isEmpty() && !lname.isEmpty() && !dob.isEmpty() && !email.isEmpty()){
            if (isEmailValid(email)){
                Intent i = new Intent(getApplicationContext(), RegistrationActivity_Final.class);
                i.putExtra("activity_from","RegistrationActivity_Initial");
                i.putExtra("fname",fname);
                i.putExtra("lname",lname);
                i.putExtra("dob",dob);
                i.putExtra("email",email);
                i.putExtra("phone","");
                Bundle extras = new Bundle();
                i.putExtras(extras);
                startActivity(i);
            } else {
                textInputEditTextEmail.setError(getString(R.string.info_invalid_email_format));
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



