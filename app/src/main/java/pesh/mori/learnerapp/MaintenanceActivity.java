package pesh.mori.learnerapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import android.view.View;
import android.widget.LinearLayout;

public class MaintenanceActivity extends AppCompatActivity {
    private LinearLayout layoutMaintenance;
    private AppCompatButton btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance);

        layoutMaintenance = findViewById(R.id.layout_maintenance);
        layoutMaintenance.setVisibility(View.VISIBLE);

        btnExit = findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }
}
