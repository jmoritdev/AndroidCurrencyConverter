package apps.joey.androidcurrencyconverterv2;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    ListView listView;
    TextView dateText;

    static final String[] KEYS = {"EUR", "USD", "GBP", "JPY", "AUD"};
    static final String BASEURL = "http://api.fixer.io/latest?base=";

    ArrayList<String> currencyNames;
    ArrayList<Double> rates;
    int selectedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = (Spinner) findViewById(R.id.spinner);
        listView = (ListView) findViewById(R.id.listView);
        dateText = (TextView) findViewById(R.id.dateText);

        currencyNames = new ArrayList<>();
        rates = new ArrayList<>();

        for(String key : KEYS){
            int resId = getResources().getIdentifier(key, "string", getPackageName());
            currencyNames.add(getString(resId));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, currencyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = position;
                rates.clear();

                generateListView();
                String url = BASEURL + KEYS[selectedCurrency];
                new DownloadTask().execute(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        generateListView();
        String url = BASEURL + KEYS[selectedCurrency];
        new DownloadTask().execute(url);
    }

    public void generateListView(){
        ArrayList<String> list = new ArrayList<>();
        for(int x = 0; x < currencyNames.size(); x++){
            if(x != selectedCurrency){
                String item = "";
                if(rates.isEmpty()){
                    item = currencyNames.get(x) + ": " + getString(R.string.loadingmsg);
                } else {
                    item = currencyNames.get(x) + ": " + rates.get(x);
                }
                list.add(item);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }

    class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0];
            String jsonString = "";
            HttpURLConnection connection = null;
            InputStream in = null;
            InputStreamReader reader = null;

            try{
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                in = connection.getInputStream();
                reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1){
                    char letter = (char) data;
                    jsonString += letter;
                    data = reader.read();
                }

            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
                if(in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String jsonString){
            super.onPostExecute(jsonString);

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                String datum = jsonObject.getString("date");
                dateText.setText(datum);
                JSONObject rtes = jsonObject.getJSONObject("rates");
                rates.clear();
                for(String key : KEYS){
                    Double d;
                    try {
                        d = rtes.getDouble(key);
                    } catch(JSONException je){
                        d = 0.0;
                    }
                    rates.add(d);
                }
                generateListView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
