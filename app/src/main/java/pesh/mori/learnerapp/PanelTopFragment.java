package pesh.mori.learnerapp;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;

public class PanelTopFragment extends Fragment {

    SimpleExoPlayer player;
    private PlayerView playerView;
    private Uri mp4VideoUri;
    private boolean playWhenReady = true;
    private boolean fullscreen = false;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    private ProgressBar mProgressBar;
    private ImageView btnFullScreen;
    private String filePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_panel_top, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel

        filePath = getArguments().getString("file_path");

//        mp4VideoUri = Uri.parse("http://mirrors.standaloneinstaller.com/video-sample/star_trails.mp4");
        mp4VideoUri = Uri.parse(filePath);
    }

    private void initialisePlayer(){
        playerView = getView().findViewById(R.id.video_view);
        mProgressBar = getView().findViewById(R.id.exo_buffering);
        mProgressBar.setVisibility(View.VISIBLE);

        btnFullScreen = playerView.findViewById(R.id.exo_fullscreen_icon);
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullScreen();
            }
        });

        player = new SimpleExoPlayer.Builder(getActivity()).build();
        playerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), this.getString(R.string.app_name)));
        // This is the MediaSource representing the media to be played.
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri);
        // Prepare the player with the source.
        player.prepare(mediaSource, false, false);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);

//        player.addListener(new Player.EventListener() {
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//
//            }
//
//            @Override
//            public void onPlayerError(ExoPlaybackException error) {
//                Log.d("LOG_initialisePlayer()",error.getMessage());
//            }
//        });

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady){
                    Log.d("LOG_initialisePlayer()","playWhenReady: true, state: "+playbackState);
                } else {
                    Log.d("LOG_initialisePlayer()","playWhenReady: false, state: "+playbackState);
                }

                switch(playbackState){
                    /*
                     * Player.STATE_IDLE = 1
                     * Player.STATE_BUFFERING = 2
                     * Player.STATE_READY = 3
                     * Player.STATE_ENDED = 4
                     * */
                    case Player.STATE_IDLE:
                    case Player.STATE_ENDED:
                    case Player.STATE_BUFFERING:
                        playerView.setKeepScreenOn(false);
                        break;
                    case Player.STATE_READY:
                        playerView.setKeepScreenOn(true);
                        break;
                }

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if (error.type == ExoPlaybackException.TYPE_SOURCE) {
                    IOException cause = error.getSourceException();
                    if (cause instanceof HttpDataSource.HttpDataSourceException) {
                        /*Log.d("LOG_initialisePlayer()","onPlayerError(): cause - "+cause);*/
                        Toast.makeText(getActivity(), R.string.error_network_access_unavailable, Toast.LENGTH_SHORT).show();
                        HttpDataSource.HttpDataSourceException httpError = (HttpDataSource.HttpDataSourceException) cause;
                        // This is the request for which the error occurred.
                        DataSpec requestDataSpec = httpError.dataSpec;
                        // It's possible to find out more about the error both by casting and by
                        // querying the cause.
                        /*Log.d("LOG_initialisePlayer()","onPlayerError(): requestDataSpec - "+requestDataSpec);*/
                        if (httpError instanceof HttpDataSource.InvalidResponseCodeException) {
                            // Cast to InvalidResponseCodeException and retrieve the response code,
                            // message and headers.
                            /*Log.d("LOG_initialisePlayer()","onPlayerError(): httpError1 - "+((HttpDataSource.InvalidResponseCodeException) cause).responseMessage);*/
                        } else {
                            // Try calling httpError.getCause() to retrieve the underlying cause,
                            // although note that it may be null.
                            /*Log.d("LOG_initialisePlayer()","onPlayerError(): httpError2 - "+httpError.getCause());*/
                        }
                    }
                }
            }
        });
    }

    private void toggleFullScreen() {
        if (fullscreen){
            btnFullScreen.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_baseline_fullscreen_24));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (((AppCompatActivity)getActivity()).getSupportActionBar() != null){
                ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            }
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)playerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int)(300*getActivity().getResources().getDisplayMetrics().density);
            playerView.setLayoutParams(params);
            fullscreen = false;
        } else {
            btnFullScreen.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_baseline_fullscreen_exit_24));
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (((AppCompatActivity)getActivity()).getSupportActionBar() != null){
                ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            }
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)playerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = params.MATCH_PARENT;
            playerView.setLayoutParams(params);
            fullscreen = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initialisePlayer();
            Log.d("LOG_onStart()","initialisePlayer() started");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initialisePlayer();
            Log.d("LOG_onResume()","initialisePlayer() started");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(getView().SYSTEM_UI_FLAG_LOW_PROFILE
                | getView().SYSTEM_UI_FLAG_FULLSCREEN
                | getView().SYSTEM_UI_FLAG_LAYOUT_STABLE
                | getView().SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | getView().SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | getView().SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }
}
