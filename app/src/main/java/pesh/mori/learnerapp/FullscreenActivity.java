package pesh.mori.learnerapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.krishna.fileloader.FileLoader;
import com.krishna.fileloader.listener.FileRequestListener;
import com.krishna.fileloader.pojo.FileResponse;
import com.krishna.fileloader.request.FileLoadRequest;

import java.io.File;

public class FullscreenActivity extends AppCompatActivity {
    private String filePath,docName;
    private PDFView pdfView;
    private ProgressBar mProgressBar;
    private int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        HomeActivity homeActivity = new HomeActivity();
        homeActivity.checkMaintenanceStatus(getApplicationContext());

        filePath = getIntent().getExtras().getString("filePath");
        docName = getIntent().getExtras().getString("docName");

        pdfView = findViewById(R.id.pdf_view_doc);
        mProgressBar = findViewById(R.id.doc_progress_bar);

        mProgressBar.setVisibility(View.VISIBLE);

        permissionsInit();

        Button btn = (Button) findViewById(R.id.dummy_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(FullscreenActivity.this,NewNoteActivity.class);
                startActivity(loginIntent);
            }
        });

        File newDir = new File("Documents");
        if (!newDir.exists()){
            newDir.mkdir();
        }

        openDoc();

    }

    public void openDoc(){
        Dexter.withActivity(this)
                .withPermission(
                        "android.permission.READ_EXTERNAL_STORAGE"
                )
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        FileLoader.with(FullscreenActivity.this)
                                .load(filePath)
                                .fromDirectory("Documents",FileLoader.DIR_EXTERNAL_PUBLIC)
                                .asFile(new FileRequestListener<File>() {
                                    @Override
                                    public void onLoad(FileLoadRequest fileLoadRequest, FileResponse<File> fileResponse) {
                                        mProgressBar.setVisibility(View.GONE);

                                        File docFile = fileResponse.getBody();

                                        pdfView.fromFile(docFile)
                                                .password(null)
                                                .defaultPage(0)
                                                .enableSwipe(true)
                                                .swipeHorizontal(false)
                                                .enableDoubletap(true)
                                                .onDraw(new OnDrawListener() {
                                                    @Override
                                                    public void onLayerDrawn(Canvas canvas, float v, float v1, int i) {

                                                    }
                                                })
                                                .onDrawAll(new OnDrawListener() {
                                                    @Override
                                                    public void onLayerDrawn(Canvas canvas, float v, float v1, int i) {

                                                    }
                                                })
                                                .onPageError(new OnPageErrorListener() {
                                                    @Override
                                                    public void onPageError(int i, Throwable throwable) {
                                                        Toast.makeText(FullscreenActivity.this, "Error opening page "+i, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .onTap(new OnTapListener() {
                                                    @Override
                                                    public boolean onTap(MotionEvent motionEvent) {
                                                        return false;
                                                    }
                                                })
                                                .enableAnnotationRendering(true)
                                                .load();
                                        Toast.makeText(FullscreenActivity.this, "Opening "+docName, Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onError(FileLoadRequest fileLoadRequest, Throwable throwable) {
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(FullscreenActivity.this, "Error: "+throwable.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(FullscreenActivity.this, "Error: "+permissionDeniedResponse, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(FullscreenActivity.this, permissionRequest.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .check();
    }

    public void permissionsInit(){
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(FullscreenActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(FullscreenActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            int grantResultsLength = grantResults.length;
            if (grantResultsLength > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(), "You grant write external storage permission. Please click original button again to continue."
//                        ,Toast.LENGTH_LONG).show();
                openDoc();

            } else {
//                Toast.makeText(getApplicationContext(), "You denied write external storage permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
