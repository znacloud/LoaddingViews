package com.github.znacloud.loaddingview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.znacloud.loadingview.ColorfuleProgressBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ColorfuleProgressBar cpbSample = (ColorfuleProgressBar) findViewById(R.id.cpb_sample);

        new AsyncTask<Void, Integer, Void>() {
            int progress = 0;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                cpbSample.setProgress(0);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                cpbSample.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                cpbSample.setProgress(100);
            }

            @Override
            protected Void doInBackground(Void... params) {
                while (true) {
                    progress += 2;
                    if (progress > 100) progress = 0;
                    publishProgress(progress);
                    try {
                        Thread.sleep(60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();

    }
}
