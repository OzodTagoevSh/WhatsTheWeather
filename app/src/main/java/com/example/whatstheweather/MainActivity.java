package com.example.whatstheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    EditText searchEditText;
    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEditText = findViewById(R.id.searchEditText);
        resultTextView = findViewById(R.id.resultTextView);
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return "Failed";
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s.equals("Failed")) {
                Toast.makeText(getApplicationContext(), "Could not find weather!", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    String weatherInfo = jsonObject.getString("weather");

                    JSONArray array = new JSONArray(weatherInfo);
                    String mainResult = "";
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject jsonPart = array.getJSONObject(i);

                        String main = jsonPart.getString("main");
                        String description = jsonPart.getString("description");
                        if (!main.equals("") && !description.equals("")) {
                            mainResult = main + ": " + description + "\r\n";
                        }
                    }

                    if (!mainResult.equals("")) {
                        resultTextView.setText(mainResult);
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not find weather!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            super.onPostExecute(s);
        }
    }

    public void findWeather(View view) {
        if (searchEditText.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter the name of the city", Toast.LENGTH_SHORT).show();
        } else {
            try {
                String encodedCityName = URLEncoder.encode(searchEditText.getText().toString(), "UTF-8");
                DownloadTask task = new DownloadTask();
                task.execute("https://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=de462ad4d56e819c538c983a305a9dfd");

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}