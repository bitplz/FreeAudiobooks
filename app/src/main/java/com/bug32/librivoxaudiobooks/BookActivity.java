package com.bug32.librivoxaudiobooks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class BookActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private String mp3url ="", playlistUrl, booktitle, genres;
    private ArrayList<String> sections = new ArrayList<>(), tempUrlList = new ArrayList<>();
    private TextView title, author, summaryTextView, genre, notAvail;
    private Button button;
    private ImageView imageView;
    private boolean isAvail = true;
    private WebView webView;
    private DrawerLayout mdrawer;
    private String authorName, summary, imgUrl, url_text_source;
    BufferedReader r;
    ProgressDialog pd = null;
    private CoordinatorLayout clayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        init();

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                notAvail.setVisibility(View.VISIBLE);
//                button.setText("Listen Audio");
                button.setEnabled(false);
                isAvail = false;
                Snackbar.make(mdrawer,"Listen to Audio only.",Snackbar.LENGTH_INDEFINITE).setAction("PLAY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent1 = new Intent(BookActivity.this, ReadBook.class);
                        intent1.putExtra("url_text_source",url_text_source);
                        intent1.putExtra("title", booktitle);
                        intent1.putStringArrayListExtra("sections", (ArrayList<String>) sections);
                        intent1.putExtra("isAvail", isAvail);
                        intent1.putExtra("imgUrl", imgUrl);
                        intent1.putStringArrayListExtra("urlList",tempUrlList);
                        startActivity(intent1);
                    }
                }).setActionTextColor(Color.RED).show();

            }
        });
        webView.loadUrl(url_text_source);

        if (!new File(Environment.getExternalStorageDirectory() + "/Android/data/com.bug32.librivoxaudiobooks/"+booktitle+"/"+booktitle+".m3u").exists()){
            new fetchData().execute();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(BookActivity.this, ReadBook.class);
                intent1.putExtra("url_text_source",url_text_source);
                intent1.putExtra("title", booktitle);
                intent1.putStringArrayListExtra("sections", (ArrayList<String>) sections);
                intent1.putExtra("isAvail", isAvail);
                intent1.putExtra("imgUrl", imgUrl);
                intent1.putStringArrayListExtra("urlList",tempUrlList);
                startActivity(intent1);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){

            case R.id.goHome:

                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                mdrawer.closeDrawer(GravityCompat.START);
                startActivity(homeIntent);

                break;

            case R.id.share :

                mdrawer.closeDrawer(GravityCompat.START);

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Librivox Audiobooks");
                intent.putExtra(Intent.EXTRA_TEXT,"Listen to Free English Audiobooks by Librivox, Read by the volunteers from around the world.\n" +
                        "Download the app now :\n https://drive.google.com/uc?id=10ET9Tw5vK1YIs_WJm4VkzhkmVJB1L_DZ&export=download");
                startActivity(Intent.createChooser(intent, "Share Using "));
                break;

            case R.id.about :

                mdrawer.closeDrawer(GravityCompat.START);
                Toast.makeText(BookActivity.this, "Version 1.0 (Early Access)", Toast.LENGTH_LONG).show();

                break;

        }

        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private class fetchData extends AsyncTask<Void, Integer, ArrayList<String>> {

        ArrayList<String> arrayList = new ArrayList<>();
        int progress = 0;

        @Override
        public ArrayList<String> doInBackground(Void... voids) {

            publishProgress(progress);

            try {
                URL url = new URL(playlistUrl);
                URLConnection ucon = url.openConnection();

                InputStream in = ucon.getInputStream();

                File path = Environment.getExternalStorageDirectory();
                File f = new File(path, "/Android/data/com.bug32.librivoxaudiobooks/"+booktitle);

                if (!f.exists()) {

                    if (f.mkdirs()) {

                        OutputStreamWriter outputWriter;
                        FileOutputStream fo = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/Android/data/com.bug32.librivoxaudiobooks/"+booktitle+"/"+booktitle+".m3u"));
                        outputWriter = new OutputStreamWriter(fo);

                        r = new BufferedReader(new InputStreamReader(in));

                        mp3url = r.readLine();

                        while (mp3url != null) {

                            arrayList.add(mp3url);
                            outputWriter.write(mp3url);
                            outputWriter.write('\n');
                            progress++;
                            publishProgress(progress);

                            Log.d(mp3url, "doInBackgroundAAAAAAAAAAA: \n" + "AL LLLLLL"+arrayList);
                            mp3url = r.readLine();
                        }

                        outputWriter.close();
                        tempUrlList.addAll(arrayList);
                        Log.d(mp3url, "doInBackground: XXXXX"+tempUrlList);
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> arrayList) {
//            super.onPostExecute(arrayList);

            Log.d(String.valueOf(tempUrlList), "onPostExecute: TTTTTTTTTTLL"+tempUrlList);
            pd.dismiss();

        }

        @Override
        protected void onPreExecute() {
//            super.onPreExecute();

            pd = new ProgressDialog(BookActivity.this);
            pd.setTitle("Wait");
            pd.setMessage("Getting Audio files...");
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setIndeterminate(false);
            pd.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            super.onProgressUpdate(values);
            pd.setProgress(values[0]);
        }
    }

    public void init(){

        imageView = (ImageView) findViewById(R.id.imageView);
        title = (TextView) findViewById(R.id.bookTitle);
        author = (TextView) findViewById(R.id.authorName);
        summaryTextView = (TextView) findViewById(R.id.summaryText);
        button = (Button) findViewById(R.id.read_book);
        genre = (TextView) findViewById(R.id.genres);
        notAvail = (TextView) findViewById(R.id.notAvail);
        mdrawer = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView nav = (NavigationView) findViewById(R.id.navigationView) ;
        nav.setNavigationItemSelectedListener(this);
//        clayout = (CoordinatorLayout) findViewById(R.id.cLayout);

        final Intent intent = getIntent();
        imgUrl = intent.getStringExtra("imgUrl");
        booktitle = intent.getStringExtra("title");
        authorName = "By: "+intent.getStringExtra("author");
        summary = intent.getStringExtra("summary");
        url_text_source = intent.getStringExtra("url_text_source");
        playlistUrl = intent.getStringExtra("playlist");
        sections.addAll(intent.getStringArrayListExtra("sections"));
        genres = intent.getStringExtra("genre");
        webView = (WebView) findViewById(R.id.webView);

        Log.d(String.valueOf(sections), "onCreate: SSSS"+sections);

        getSupportActionBar().setTitle(booktitle);

        Picasso.get().load(imgUrl).fit().into(imageView);
        title.setText(booktitle);
        author.setText(authorName);
        genre.setText(genres);
        summaryTextView.setText(Html.fromHtml(summary));
    }

}
