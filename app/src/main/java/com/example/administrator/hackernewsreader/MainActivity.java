package com.example.administrator.hackernewsreader;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String URL_IDS = "https://hacker-news.firebaseio.com/v0/topstories.json";
    final String URL_ART_START = "https://hacker-news.firebaseio.com/v0/item/";
    final String URL_ART_END = ".json";

    ListView listview;
    TextView voortgangTextView;
    ArrayAdapter<String> adapter;
    ArrayList<String> titelLijst;
    ArrayList<String> urlLijst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listview = (ListView) findViewById(R.id.listView);
        voortgangTextView = (TextView) findViewById(R.id.voortgangTextview);
        titelLijst = new ArrayList<>();
        urlLijst = new ArrayList<>();

        adapter  = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,titelLijst);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent browserIntent = new Intent (Intent.ACTION_VIEW, Uri.parse(urlLijst.get(position)));
                startActivity(browserIntent);
            }
        });
        new DownloadTaak().execute();
    }

        class DownloadTaak extends AsyncTask<Void,String,Void>{

            @Override
            protected Void doInBackground(Void... params) {
             String resArray = "";
                HttpURLConnection connection = null;
                InputStream in = null;
                InputStreamReader reader = null;

                try {

                    URL urlIds = new URL(URL_IDS);
                    connection = (HttpURLConnection)urlIds.openConnection();
                    in = connection.getInputStream();
                    reader = new InputStreamReader(in);
                    int data = reader.read();
                    while(data != -1) {
                        char c = (char) data;
                        resArray +=c;
                        data = reader .read();
                    }

                    JSONArray jsonArray = new JSONArray(resArray);
                    int index = 0;
                    for(int i=0; i < jsonArray.length(); i++) {

                        String id = jsonArray.getString(i);
                        URL urlArtikel = new URL(URL_ART_START + id + URL_ART_END);
                        connection = (HttpURLConnection) urlArtikel.openConnection();
                        in = connection.getInputStream();
                        reader = new InputStreamReader(in);
                        String jsonObjectString = "";
                        data = reader.read();
                        while (data != -1) {
                            char c = (char) data;
                            jsonObjectString += c;
                            data = reader.read();
                        }
                        JSONObject jsonObject = new JSONObject(jsonObjectString);


                        if (jsonObject.has("title") && jsonObject.has("url")) {
                            titelLijst.add(jsonObject.getString("title"));
                            urlLijst.add(jsonObject.getString("url"));
                            publishProgress("Downloaded:" + index + "articles");
                            index++;
                        }
                    }

                } catch(Exception e) {
                    e.printStackTrace ();
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                    if (in != null){
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

                String update = values[0];
                voortgangTextView.setText(update);
                voortgangTextView.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
                voortgangTextView.setVisibility(View.GONE);
            }
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_Refresh) {
            titelLijst.clear();
            urlLijst.clear();
            adapter.notifyDataSetChanged();
            new DownloadTaak().execute();
        }
        return super.onOptionsItemSelected(item);
    }
}
