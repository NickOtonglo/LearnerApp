package pesh.mori.learnerapp;

/**
 * Created by MORIAMA on 21/11/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Created by MORIAMA on 04/10/2017.
 */

public class InputValidation {

    private Context context;

    public InputValidation(Context context){
        this.context=context;}
    public boolean isInputEditTextFilled(TextInputEditText textInputEditText, TextInputLayout textInputLayout, String message){
        String value = textInputEditText.getText().toString().trim();
        if (value.isEmpty()){
            textInputEditText.setError(message);
            hideKeyboardFrom(textInputEditText);
            return false;
        }else{
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }
    public boolean isInputEditTextEmail(TextInputEditText textInputEditText,TextInputLayout textInputLayout, String message){
        String value = textInputEditText.getText().toString().trim();
        if (value.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(value).matches()){
            textInputLayout.setError(message);
            hideKeyboardFrom(textInputEditText);
            return false;
        }else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }
    private void hideKeyboardFrom(View view){
        InputMethodManager inn = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inn.hideSoftInputFromWindow(view.getWindowToken(), LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
