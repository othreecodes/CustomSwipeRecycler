package com.davidmadethis.testrestapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.JsonReader;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.davidmadethis.testrestapp.adapters.DataAdapter;
import com.davidmadethis.testrestapp.dto.Data;
import com.davidmadethis.testrestapp.utils.SwipeController;
import com.davidmadethis.testrestapp.utils.SwipeControllerActions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private DataAdapter mAdapter ;
    private  static final String API_URL = "https://restcountries.eu/rest/v2/all";
    ProgressBar bar;
    SwipeController swipeController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bar = findViewById(R.id.progressBar);

        HttpRequest httpRequest = new HttpRequest();
        String rs = null;



        httpRequest.execute(API_URL);

    }




    private void parseJson(String rs) {
        setContentView(R.layout.list_layout);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);


        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        try {
            JSONArray jsonArray = new JSONArray(rs);
            List<Data> data = new ArrayList<>();
            for (int i = 0 ; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                Data d = new Data();
                d.setName(obj.getString("name"));
                d.setLanguage(obj.getJSONArray("languages").getJSONObject(0).getString("name"));
                d.setCurrencyName(obj.getJSONArray("currencies").getJSONObject(0).getString("name"));
                data.add(d);
            }

            mAdapter = new DataAdapter(data);
            recyclerView.setAdapter(mAdapter);

            swipeController = new SwipeController(new SwipeControllerActions() {
                @Override
                public void onRightClicked(int position) {
                    mAdapter.getData().remove(position);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
                }
            });

            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);

            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    swipeController.onDraw(c);
                }
            });


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private class HttpRequest extends AsyncTask<String, Void, String> {
        static final String REQUEST_METHOD = "GET";
        static final int READ_TIMEOUT = 15000;
        static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = strings[0];
            String result = null;
            String inputLine;

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();

                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(String s) {
             parseJson(s);
            super.onPostExecute(s);
        }
    }

}
