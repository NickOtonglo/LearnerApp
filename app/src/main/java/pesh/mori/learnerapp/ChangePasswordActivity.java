package pesh.mori.learnerapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by MORIAMA on 28/12/2017.
 */

public class ChangePasswordActivity extends AppCompatActivity {

    private final AppCompatActivity activity = ChangePasswordActivity.this;

    private InputValidation inputValidation;
    private Button btnSubmit;
    private TextInputEditText txtPasswordNew;
    private TextInputEditText txtPasswordConfirm;
    private TextInputLayout txtParentPasswordNew;
    private TextInputLayout txtParentPasswordConfirm;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ProgressDialog mProgress;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if (new SharedPreferencesHandler(this).getNightMode()){
            setTheme(R.style.DarkTheme_NoActionBar);
        } else if (new SharedPreferencesHandler(this).getSignatureMode()) {
            setTheme(R.style.SignatureTheme_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
        setContentView( R.layout.activity_changepassword );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorToolBarMainText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mProgress = new ProgressDialog(this);
        btnSubmit = (Button) findViewById(R.id.edit);
        initViews();
        checkAuthState();
        initObjects();
        emptyInputEditText();
    }
    private void initViews(){
        txtParentPasswordNew = (TextInputLayout) findViewById(R.id.txt_parent_password_new);
        txtParentPasswordConfirm = (TextInputLayout) findViewById(R.id.txt_parent_password_confirm);
        txtPasswordNew = (TextInputEditText) findViewById(R.id.txt_password_new);
        txtPasswordConfirm = (TextInputEditText) findViewById(R.id.txt_password_confirm);
    }

    private void initObjects(){
        inputValidation = new InputValidation(activity);
    }

    private void emptyInputEditText(){
        txtPasswordNew.setText(null);
        txtPasswordConfirm.setText(null);
    }

    public void checkAuthState(){
        if (mAuth.getCurrentUser()==null){
            mAuth.signOut();
            Intent loginIntent = new Intent(ChangePasswordActivity.this,SelectLoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
        }
    }

    public void doThis(View view) {
        btnSubmit = (Button) findViewById(R.id.edit);
        final String password = txtPasswordNew.getText().toString();
        final String confirm_password = txtPasswordConfirm.getText().toString();
        if (!inputValidation.isInputEditTextFilled(txtPasswordNew,txtParentPasswordNew,getString(R.string.hint_enter_new_password) )) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(txtPasswordConfirm, txtParentPasswordConfirm, getString( R.string.hint_reenter_new_password) )) {
            return;
        }
        if (!confirm_password.equals(password)){
            txtPasswordConfirm.setError(getString(R.string.error_passwords_dont_match));
            return;
        }
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        builder1.setMessage(R.string.confirm_are_you_sure_you_want_to_change_your_password);
        builder1.setCancelable( false );

        builder1.setPositiveButton(
                getString(R.string.option_alert_yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mProgress.setMessage(getString(R.string.info_please_wait));
                        mProgress.setCanceledOnTouchOutside(false);
                        mProgress.show();
                        mUser.updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), getString(R.string.info_password_changed), Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(ChangePasswordActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        dialog.cancel();
                    }
                } );
        builder1.setNegativeButton(getString(R.string.option_alert_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    public boolean onSupportNavigateUp(){

        finish();
        return true;
    }


}
