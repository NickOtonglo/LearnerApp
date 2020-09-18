package pesh.mori.learnerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

class CustomDialogActivity {

    private Activity activity;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private View view;

    public CustomDialogActivity(Activity activity) {
        this.activity = activity;
    }

    void show(Boolean cancellable){
        builder = new AlertDialog.Builder(activity);
        inflater = activity.getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_progress_custom,null);
        builder.setView(view);
        builder.setCancelable(cancellable);
        alertDialog = builder.create();
        alertDialog.show();
    }

    void setDisplayText(String message){
        TextView textView = (TextView)view.findViewById(R.id.custom_dialog_text);
        textView.setText(message);
    }

    void dismiss(){
        alertDialog.dismiss();
    }
}
