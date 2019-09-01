package com.bug32.librivoxaudiobooks;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private TextView text1, text2;
    String versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setTitle("About");

        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);

        String aboutApp = "Free Audiobooks is an application which uses librivox' API to stream audio-books.\n" +
                "\n" +
                "LibriVox is a group of worldwide volunteers who read and record public domain texts creating free public domain audiobooks for download from their website and other digital library hosting sites on the internet.\n" +
                "\n";

        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String version = "Version "+ versionName;

        text1.setText(aboutApp);
        text2.setText(version);


    }
}
