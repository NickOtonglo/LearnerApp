package pesh.mori.learnerapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
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

public class ReadDocument extends AppCompatActivity {
    private String filePath,docName,postKey="",outgoingIntent="";
    private PDFView pdfView;
    private WebView webView;
    private ProgressBar mProgressBar;
    private static int TIME_OUT = 40000;
    private int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_document);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postKey = getIntent().getExtras().getString("postKey");
        filePath = getIntent().getExtras().getString("filePath");
        docName = getIntent().getExtras().getString("docName");
        outgoingIntent = getIntent().getExtras().getString("outgoing_intent");

        pdfView = findViewById(R.id.pdf_view_doc);
        webView = findViewById(R.id.webview);
        mProgressBar = findViewById(R.id.doc_progress_bar);

        mProgressBar.setVisibility(View.VISIBLE);

        permissionsInit();

        File newDir = new File("Documents");
        if (!newDir.exists()){
            newDir.mkdir();
        }

        openDoc();

//        new Handler().postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                Toast.makeText(ReadDocument.this, "Preview Timeout", Toast.LENGTH_LONG).show();
//                finish();
//            }
//        }, TIME_OUT);

    }

    public void openDoc(){
        Dexter.withActivity(this)
                .withPermission(
                        "android.permission.READ_EXTERNAL_STORAGE"
                )
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        FileLoader.with(ReadDocument.this)
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
                                                        Toast.makeText(ReadDocument.this, "Error opening page "+i, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(ReadDocument.this, "Opening "+docName, Toast.LENGTH_SHORT).show();

                                    }

                                    @Override
                                    public void onError(FileLoadRequest fileLoadRequest, Throwable throwable) {
                                        mProgressBar.setVisibility(View.GONE);
                                        Toast.makeText(ReadDocument.this, "Error: Unable to load file. Please report a problem with this file or contact support.", Toast.LENGTH_LONG).show();
                                        Log.d("LOG_PdfViewError",throwable.getMessage());
                                        openDocFromUrl(filePath);
                                    }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ReadDocument.this, "Error: "+permissionDeniedResponse, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ReadDocument.this, permissionRequest.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .check();
    }

    public void openDocFromUrl(String url){
        pdfView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+Uri.parse(url));
//        webView.loadUrl("https://docs.google.com/gview?embedded=true&url="+url);
    }

    public void permissionsInit(){
        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(ReadDocument.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(writeExternalStoragePermission!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(ReadDocument.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (!outgoingIntent.equals("PreviewPostActivity"))
        getMenuInflater().inflate(R.menu.menu_notes, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.new_note:
                Intent intent = new Intent(getApplicationContext(),NewNoteActivity.class);
                intent.putExtra("post_key",postKey);
                intent.putExtra("outgoing_intent",outgoingIntent);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                return true;
            case R.id.all_notes:
                Intent intent1 = new Intent(getApplicationContext(),NotesList.class);
                intent1.putExtra("post_key",postKey);
                intent1.putExtra("outgoing_intent",outgoingIntent);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_from_bottom,R.anim.static_animation);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.static_animation,R.anim.slide_in_from_top);
    }
}
