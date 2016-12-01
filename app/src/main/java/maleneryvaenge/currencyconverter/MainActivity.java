package maleneryvaenge.currencyconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.System.exit;


public class MainActivity extends ActionBarActivity {


    private EditText editText_currency;
    private Spinner spinner_from;
    private Spinner spinner_to;
    private Button btn_convert;
    private TextView textView_from;
    private TextView textView_to;

    ProgressDialog pd;
    private AQuery aq;
    /** API */
    private String base_url = "http://rate-exchange-1.appspot.com/currency?"; //from=NOK&to=USD //"http://openexchangerates.org/api/latest.json?app_id=7ffb4d91bb1642e187eaf5038d7d6e9d";

    
    public String getCurrencyAcronym(String curr) {
        switch(curr) {
            case "(NOK) Norwegian Krone":
                curr = "NOK";
                break;
            case "(USD) US Dollar":
                curr = "USD";
                break;
            case "(AUD) Australian Dollar":
                curr = "AUD";
                break;
            case "(GBP) British Pound":
                curr = "GBP";
                break;
            case "(EUR) Euro":
                curr = "EUR";
                break;
            case "(BRL) Brazilian Real":
                curr = "BRL";
                break;
            case "(DKK) Danish Krone":
                curr = "DKK";
                break;
            case "(SEK) Swedish Krone":
                curr = "SEK";
                break;
            case "(JPY) Japanese Yen":
                curr = "JPY";
                break;
        }
        return curr;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_money);

        editText_currency = (EditText)findViewById(R.id.editText_currency);
        spinner_from = (Spinner)findViewById(R.id.spinner_from);
        spinner_to = (Spinner)findViewById(R.id.spinner_to);
        btn_convert = (Button)findViewById(R.id.btn_convert);
        textView_from = (TextView)findViewById(R.id.textView_from);
        textView_to = (TextView)findViewById(R.id.textView_to);

        aq = new AQuery(this);


        /** SPINNER */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_from.setAdapter(adapter);
        spinner_to.setAdapter(adapter);

        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editText_currency.getText().toString().length() < 1) {
                    Toast.makeText(MainActivity.this, "You have to write a value", Toast.LENGTH_LONG).show();

                } else {

                    //Double currency_from_value = Double.valueOf(editText_currency.getText().toString());
                    String from_currency = String.valueOf(spinner_from.getSelectedItem());
                    String to_currency = String.valueOf(spinner_to.getSelectedItem());

                    from_currency = getCurrencyAcronym(from_currency);
                    to_currency = getCurrencyAcronym(to_currency);

                    String url = base_url + "from=" + from_currency + "&to=" + to_currency;

                    new JsonTask().execute(url);
                }
            }
        });
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL( params[0] );
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //Her printes hele resultate ut
                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            JSONObject obj = null;
            try {
                obj = new JSONObject(result);
                double rate = Double.parseDouble(obj.getString("rate"));

                Double currency_from_value = Double.valueOf(editText_currency.getText().toString());
                double the_result = currency_from_value * rate;

                String from_currency = spinner_from.getSelectedItem().toString();
                String to_currency = spinner_to.getSelectedItem().toString();

                from_currency = getCurrencyAcronym(from_currency);
                to_currency = getCurrencyAcronym(to_currency);

                textView_from.setText(editText_currency.getText().toString() + " " + from_currency + " = ");
                textView_to.setText(String.valueOf(the_result) + " " + to_currency);

               // Context context = getApplicationContext();
               // CharSequence text = Double.toString(the_result);
               // int duration = Toast.LENGTH_SHORT;

               // Toast toast = Toast.makeText(context, text, duration);
                // toast.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    /** MENY */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.currency_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                about();
                return true;
            case R.id.exit:
                exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void about() { // Metoden åpner et nytt aktivitetsvindu

        Intent intent = new Intent(this, About.class);
        startActivity(intent);
        finish();

    }

}
