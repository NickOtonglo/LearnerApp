package pesh.mori.learnerapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class alerts extends Fragment {


    public alerts(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View alerts = inflater.inflate(R.layout.fragment_alerts, container, false);
        Toast.makeText(getActivity(), "INFO | Under Development",  Toast.LENGTH_SHORT).show();
        return alerts;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );
    }

}
