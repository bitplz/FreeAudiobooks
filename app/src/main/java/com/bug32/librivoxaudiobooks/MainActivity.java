package com.bug32.librivoxaudiobooks;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, productAdapter.OnItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private productAdapter mProductAdapter;
    private ArrayList<Product> productItemList;
    private RequestQueue mRequestQueue;
    private String searchText = "", sNo, secTitle, genName;
    private String url = "https://librivox.org/api/feed/audiobooks/?title=^" + searchText + "&format=json&extended=1";
    boolean doubleBackToExitPressedOnce = false;
    private DrawerLayout mDrawerLayout;
    private LinearLayout retryLayout;
    private Button retry;
    private List<String> genreList = new ArrayList<>();
    private ActionBarDrawerToggle drawerToggle;
    private boolean isPermissionGiven = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        productItemList = new ArrayList<>();
        mProductAdapter = new productAdapter(this, productItemList);
        recyclerView.setAdapter(mProductAdapter);
        retry = (Button) findViewById(R.id.retry);
        retryLayout = (LinearLayout) findViewById(R.id.retryLayout);
        mRequestQueue = Volley.newRequestQueue(this);
        NavigationView nav = (NavigationView) findViewById(R.id.navigationView);
        nav.setNavigationItemSelectedListener(this);

        chkInternet();
        setMenu();


        if(Build.VERSION.SDK_INT >= 23)
        {
            if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
            else
            {
                parseJson();
            }
        }
    }


    public void parseJson() {
        productItemList.clear();

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Loading Content...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray jsonArray = null;
                        Writer output = null;
                        try {

                            JSONObject root = new JSONObject();
                            JSONObject book = new JSONObject();
                            root = response.getJSONObject("books");

                            Iterator<?> keys = root.keys();

                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                if (root.get(key) instanceof JSONObject) {
                                    book = root.getJSONObject(key);

                                    String authorName = null, fName, lName;

                                    ////////////Authors/////////////
                                    JSONArray authors = book.getJSONArray("authors");
                                    for (int i = 0; i < authors.length(); i++) {
                                        JSONObject j = authors.getJSONObject(i);
                                        fName = j.getString("first_name");
                                        lName = j.getString("last_name");
                                        authorName = fName + " " + lName;

                                        try {

                                            File file = new File(Environment.getExternalStorageDirectory() + "/books.json");
                                            output = new BufferedWriter(new FileWriter(file));
                                            output.write(root.toString());
                                            //output.write(book.toString());

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    ///////////////////////////////

                                    ////////////Sections/////////////
                                    String sec = null;
                                    List<String> secList = new ArrayList<>();
                                    JSONArray sections = book.getJSONArray("sections");
                                    for (int i = 0; i < sections.length(); i++){
                                        JSONObject sectionsJSONObject = sections.getJSONObject(i);
                                        sNo = sectionsJSONObject.getString("section_number");
                                        secTitle = sectionsJSONObject.getString("title");
                                        sec = sNo + ".    " + secTitle;
                                        Log.d(sec, "onResponse: ZZZZZZZ"+ sec);
                                        secList.add(sec);

                                    }
                                    ///////////////////////////////

                                    ////////////Genres/////////////
                                    JSONArray genres = book.getJSONArray("genres");
                                    for (int i = 0; i < genres.length(); i++){
                                        JSONObject genreobj = genres.getJSONObject(i);
                                        genName = genreobj.getString("name");
                                        Log.d(sec, "onResponse: GGGGGG"+ genName);
                                        genreList.add(genName);

                                    }
                                    ///////////////////////////////

                                    String title = book.getString("title");
                                    String summary = book.getString("description");
                                    String imgUrl = book.getString("url_iarchive");
                                    String url_text_source = book.getString("url_text_source");
                                    String url_text_source_final = "http://www.gutenberg.org/files/" +
                                            url_text_source.substring(url_text_source.lastIndexOf('/') + 1) + "/" + url_text_source.substring(url_text_source.lastIndexOf('/') + 1) + "-h/" +
                                            url_text_source.substring(url_text_source.lastIndexOf('/') + 1) + "-h.htm";
                                    imgUrl = "https://archive.org/services/img/" + imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
                                    String playListUrl = "https://archive.org/download/" + imgUrl.substring(imgUrl.lastIndexOf('/') + 1) + "/" + imgUrl.substring(imgUrl.lastIndexOf('/') + 1) + "_64kb.m3u";

                                    productItemList.add(new Product(title, summary, imgUrl, authorName, url_text_source_final, playListUrl, secList, genreList));

                                }
                            }

                            output.close();
                            setProductAdapter();
                            progressDialog.dismiss();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);

    }

    private void setProductAdapter() {
        mProductAdapter = new productAdapter(MainActivity.this, productItemList);
        recyclerView.setAdapter(mProductAdapter);
        mProductAdapter.setOnItemClickListener(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public void onItemClick(int position) {

        Product clickedItem = productItemList.get(position);
        Intent intent = new Intent(MainActivity.this, BookActivity.class);
        intent.putExtra("title", clickedItem.getMtitle());
        intent.putExtra("author", clickedItem.getmAuthor());
        intent.putExtra("summary", clickedItem.getmSummary());
        intent.putExtra("imgUrl", clickedItem.getmImgUrl());
        intent.putExtra("url_text_source", clickedItem.getmUrl_text());
        intent.putExtra("playlist", clickedItem.getM3uUrl());
        intent.putExtra("genre", clickedItem.getmGenres().get(position));
        intent.putStringArrayListExtra("sections", (ArrayList<String>) clickedItem.getmSections());
        startActivity(intent);

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        url = "https://librivox.org/api/feed/audiobooks/?title=^" + query + "&format=json&extended=1";
        parseJson();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        ArrayList<Product> newlist = new ArrayList<>();
        for (Product product : productItemList) {
            String name = product.getMtitle().toLowerCase();
            if (name.contains(newText)) {
                newlist.add(product);
            }
        }

        mProductAdapter.setFilter(newlist);

        return false;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private void setMenu() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        switch (id){

            case R.id.goHome:

                if (this == MainActivity.this){
                    parseJson();
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }else {

                    Intent homeIntent = new Intent(this, MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }
                break;

            case R.id.share :

                mDrawerLayout.closeDrawer(GravityCompat.START);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"Librivox Audiobooks");
                intent.putExtra(Intent.EXTRA_TEXT,"Listen to Free English Audiobooks by Librivox, Read by the volunteers from around the world.\n" +
                        "Download the app now :\n https://drive.google.com/uc?id=10ET9Tw5vK1YIs_WJm4VkzhkmVJB1L_DZ&export=download");
                startActivity(Intent.createChooser(intent, "Share Using "));
                break;

            case R.id.about :

                mDrawerLayout.closeDrawer(GravityCompat.START);

                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);

                break;

        }

        return true;
    }

    public void chkInternet() {
        if (!isNetworkAvailable()) {

            recyclerView.setVisibility(View.GONE);
            retryLayout.setVisibility(View.VISIBLE);

            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNetworkAvailable()){
                        retryLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        parseJson();
                    }else {
                        chkInternet();
                    }
                }
            });

        }
    }

    // Private class isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            finishAffinity();
        }

        else
        {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode)
        {
            case 1:
                if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    parseJson();

                }
                else
                {
                    closeNow();
                    Toast.makeText(MainActivity.this, "Permission is must!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}


