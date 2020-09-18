package pesh.mori.learnerapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nick Otto on 18/07/2019.
 */

public class TemporaryPermissions {
    private static final String FILE_NAME = "file_name";
    private static final String KEY_THEM_ALL = "my_key";
    private static final String CODE = "";

    public static void saveVerificationValue(Context context) {
        // value equals 1 if user is already verified. Anything else will mean that the user is not verified
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_THEM_ALL, "1");
        editor.apply();
    }

    public static boolean isUserVerified(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        String code = sharedPreferences.getString(KEY_THEM_ALL, "");

        if (code.contentEquals("1")) {
            return true;
        }

        return false;
    }

    public static void removeVerificationValue(Context context) {
        // value equals 1 if user is already verified. Anything else will mean that the user is not verified
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_THEM_ALL, "0");
        editor.apply();
    }

    public static void holdVerificationCode(Context context, String code){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE, code);
        editor.apply();
//        Log.d("LOG_VerificationCode",code);
    }

    public static String showVerificationCode(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
        String code = sharedPreferences.getString(CODE, "");
        return code;
    }

    public static void removeVerificationCode(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CODE, "0");
        editor.apply();
    }

}
