package pesh.mori.learnerapp;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler {
    SharedPreferences mSharedPreferences;

    public SharedPreferencesHandler (Context context){
        mSharedPreferences = context.getSharedPreferences("AppTheme", Context.MODE_PRIVATE);
    }

    public void setNightMode(Boolean state){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("NightMode",state);
        editor.apply();
    }

    public Boolean getNightMode(){
        return mSharedPreferences.getBoolean("NightMode",false);
    }

    public void setSignatureMode(Boolean state){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("SignatureMode",state);
        editor.apply();
    }

    public Boolean getSignatureMode(){
        return mSharedPreferences.getBoolean("SignatureMode",false);
    }
}
