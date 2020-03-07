package pesh.mori.learnerapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class notification extends Fragment {
    public notification(){};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View notification = inflater.inflate(R.layout.fragment_notification, container, false);
        Toast.makeText(getActivity(), "INFO | Under Development",  Toast.LENGTH_SHORT).show();
        return notification;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );
    }
}
