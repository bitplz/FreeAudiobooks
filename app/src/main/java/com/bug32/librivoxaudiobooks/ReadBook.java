package com.bug32.librivoxaudiobooks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ReadBook extends AppCompatActivity {

    private static final String TAG = "";
    WebView webView;
    private FloatingActionButton floatingActionButton, chapters, saveBook, play, next, prev;
    private LinearLayout mBookLayout, mChapters, mpLayout, cLayout;
    private Animation showFloatButtons, hideFloatButtons, show_mp, hide_mp, rotateButton, rotateButton2;
    private ArrayList<String> sections = new ArrayList<>(), list = new ArrayList<>();
    private String title, textSource, imgUrl;
    private String mp3url = "", audioUrl;
    private TextView maxDurationView, onGoingDurationView, nowPlaying;
    private BufferedReader r;
    private int trackno = 0;
    private Runnable runnable;
    Handler handler = new Handler();
    private ImageView imageView;
    private AppCompatSeekBar seekBar;
    private boolean isAvail , isPlaying = false, isPause = false;
    private ArrayList<String> arrayList = new ArrayList<>();
    private static MediaPlayer mediaPlayer = new MediaPlayer();

    @SuppressLint({"SetJavaScriptEnabled", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_book);

        initView();



        final Intent intent = getIntent();
        textSource = intent.getStringExtra("url_text_source");
        title = intent.getStringExtra("title");
        sections.addAll(intent.getStringArrayListExtra("sections"));
        isAvail = intent.getBooleanExtra("isAvail",true);
        imgUrl = intent.getStringExtra("imgUrl");
        list.addAll(intent.getStringArrayListExtra("urlList"));


        if (!new File(Environment.getExternalStorageDirectory() + "/Android/data/com.bug32.librivoxaudiobooks/"+title+"/"+title+".m3u").exists()){
            floatingActionButton.setVisibility(View.GONE);
            mpLayout.setVisibility(View.GONE);
            Toast.makeText(ReadBook.this, "Audio File not Available, Try again Sometime.", Toast.LENGTH_LONG).show();
        }else {
            getMp3();
        }

        if (!isAvail){
            webView.setVisibility(View.GONE);
            cLayout.setVisibility(View.VISIBLE);
            Picasso.get().load(imgUrl).fit().into(imageView);
            nowPlaying.setText(sections.get(trackno));

        }else {

            final ProgressDialog progressDialog = new ProgressDialog(ReadBook.this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Loading book...");
            progressDialog.show();


            webView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(textSource);
                    return true;
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    Toast.makeText(ReadBook.this, "Text Book not Available!", Toast.LENGTH_LONG).show();
                    finish();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    progressDialog.dismiss();

                }
            });
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(textSource);
        }


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mediaPlayer.getDuration());
                updateSeekbar();
                mediaPlayer.start();
                play.setImageResource(R.drawable.ic_action_pause);
            }
        });



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                if (fromUser){
                    mediaPlayer.seekTo(progress);
                    onGoingDurationView.setText(calCurrentTime(progress));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBookLayout.getVisibility() == View.GONE && mChapters.getVisibility() == View.GONE) {
                    floatingActionButton.setAnimation(rotateButton);
                    mBookLayout.setVisibility(View.VISIBLE);
                    mChapters.setVisibility(View.VISIBLE);
                    mBookLayout.startAnimation(showFloatButtons);
                    mChapters.startAnimation(showFloatButtons);

                } else {
                    floatingActionButton.setAnimation(rotateButton2);
                    mBookLayout.setVisibility(View.GONE);
                    mChapters.setVisibility(View.GONE);
                    mBookLayout.startAnimation(hideFloatButtons);
                    mChapters.startAnimation(hideFloatButtons);
                }
            }
        });

        chapters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayAdapter<String> a = new ArrayAdapter<String>(ReadBook.this, android.R.layout.simple_list_item_1,sections);
                Log.d(String.valueOf(sections), "onClick: FFFFFF"+sections);
                AlertDialog.Builder builder = new AlertDialog.Builder(ReadBook.this);
                builder.setTitle("Chapters / Sections");
                builder.setIcon(R.drawable.ic_action_list);
                builder.setAdapter(a, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mediaPlayer.isPlaying()){
                            mediaPlayer.reset();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            trackno = which;
                            audioUrl = list.get(trackno);
                            nowPlaying.setText(sections.get(trackno));
                            try {
                                mediaPlayer.setDataSource(audioUrl);
                                mediaPlayer.prepareAsync();
                                Toast.makeText(ReadBook.this, "Loading...", Toast.LENGTH_LONG).show();
                                mediaPlayer.start();
                                Toast.makeText(ReadBook.this, "Playing: " + sections.get(trackno), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
                builder.show();
                floatingActionButton.performClick();
            }
        });

        saveBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                new saveThisBook().execute();
                Toast.makeText(ReadBook.this, "This feature is not Available.", Toast.LENGTH_LONG).show();
                floatingActionButton.performClick();

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        trackno++;
                        if (trackno > list.size()-1) {
                            Toast.makeText(ReadBook.this, "THE END", Toast.LENGTH_LONG).show();
                            trackno--;
                        } else {
                            audioUrl = list.get(trackno);
                            mediaPlayer.setDataSource(audioUrl);
                            mediaPlayer.prepareAsync();
                            Toast.makeText(ReadBook.this, "Loading...", Toast.LENGTH_LONG).show();
                            mediaPlayer.start();
                            seekBar.setMax(mediaPlayer.getDuration());
                            Toast.makeText(ReadBook.this, "Playing: " + sections.get(trackno), Toast.LENGTH_LONG).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        trackno--;
                        if (trackno <0) {
                            trackno++;
                            audioUrl = list.get(trackno);
                        } else {
                            audioUrl = list.get(trackno);
                        }


                        mediaPlayer.setDataSource(audioUrl);
                        mediaPlayer.prepareAsync();
                        Toast.makeText(ReadBook.this, "Loading...", Toast.LENGTH_LONG).show();
                        mediaPlayer.start();
                        seekBar.setMax(mediaPlayer.getDuration());

                        Toast.makeText(ReadBook.this, "Playing: " + sections.get(trackno), Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isPlaying) {
                    mediaPlayer.pause();
                    isPause = true;
                    isPlaying = false;
                    play.setImageResource(R.drawable.ic_action_play);
                } else {

                    play.setImageResource(R.drawable.ic_action_pause);
                    mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    try {
                        if (isPause){
                            mediaPlayer.start();
                        }else {
                            audioUrl = list.get(trackno);
                            mediaPlayer.setDataSource(audioUrl);
                            mediaPlayer.prepareAsync();
                            Toast.makeText(ReadBook.this, "Loading...", Toast.LENGTH_LONG).show();
                            mediaPlayer.start();
                            seekBar.setMax(mediaPlayer.getDuration());
//                        maxDurationView.setText(calCurrentTime(mediaPlayer.getDuration()));
                            isPlaying = true;
                            Toast.makeText(ReadBook.this, "Playing: " + sections.get(trackno), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    public void initView(){
        webView = (WebView) findViewById(R.id.webView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        play = (FloatingActionButton) findViewById(R.id.play);
        prev = (FloatingActionButton) findViewById(R.id.prev);
        next = (FloatingActionButton) findViewById(R.id.next);
        mBookLayout = (LinearLayout) findViewById(R.id.bookLayout);
        mChapters = (LinearLayout) findViewById(R.id.chapters);
        mpLayout = (LinearLayout) findViewById(R.id.media_player_layout);
        cLayout = (LinearLayout) findViewById(R.id.cover_layout);
        chapters = (FloatingActionButton) findViewById(R.id.secButton);
        saveBook = (FloatingActionButton) findViewById(R.id.saveBook);
        seekBar = (AppCompatSeekBar) findViewById(R.id.mSeekBar);
        maxDurationView = (TextView) findViewById(R.id.maxDuration);
        onGoingDurationView = (TextView) findViewById(R.id.onGoingDuration);
        imageView = (ImageView) findViewById(R.id.cover);
        nowPlaying = (TextView) findViewById(R.id.nowPlaying);


        rotateButton = AnimationUtils.loadAnimation(ReadBook.this, R.anim.rotate_button);
        rotateButton2 = AnimationUtils.loadAnimation(ReadBook.this, R.anim.rotate_button2);
        showFloatButtons = AnimationUtils.loadAnimation(ReadBook.this, R.anim.show_layout);
        hideFloatButtons = AnimationUtils.loadAnimation(ReadBook.this, R.anim.hide_layout);
        show_mp = AnimationUtils.loadAnimation(ReadBook.this, R.anim.show_media_player);
        hide_mp = AnimationUtils.loadAnimation(ReadBook.this, R.anim.hide_media_player);

    }

    public void getMp3(){
        try {
            FileInputStream fo = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/Android/data/com.bug32.librivoxaudiobooks/"+title+"/"+title+".m3u"));
            DataInputStream in = new DataInputStream(fo);

            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            mp3url = br.readLine();

            while (mp3url != null) {
                list.add(mp3url);
                mp3url = br.readLine();
            }

            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateSeekbar(){
        if (mediaPlayer.isPlaying()){
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            maxDurationView.setText(calCurrentTime(mediaPlayer.getDuration()));
            onGoingDurationView.setText(calCurrentTime(mediaPlayer.getCurrentPosition()));
        }

            runnable = new Runnable() {
                @Override
                public void run() {
                updateSeekbar();

                }

            };
            handler.postDelayed(runnable, 1000);

    }

    public String calCurrentTime(int duration){

        int milSec = duration/1000;
        int min = milSec/60;
        int sec = milSec - (min*60);
        return min+":"+sec;
    }



}


