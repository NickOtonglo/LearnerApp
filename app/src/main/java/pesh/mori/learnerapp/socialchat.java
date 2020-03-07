package pesh.mori.learnerapp;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by MORIAMA on 18/11/2017.
 */

public class socialchat extends Fragment{
    public socialchat(){};



    private InputValidation inputValidation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View socialchat = inflater.inflate(R.layout.fragment_socialchat, container, false);
        Toast.makeText(getActivity(), "INFO | Under Development",  Toast.LENGTH_SHORT).show();
        return socialchat;

    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().findViewById(R.id.find).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                builder1.setMessage( "Your request has been sent." );
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
        });

    }

}