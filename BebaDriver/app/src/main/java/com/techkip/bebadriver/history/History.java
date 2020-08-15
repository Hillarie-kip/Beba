package com.techkip.bebadriver.history;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.github.clans.fab.FloatingActionButton;
import com.techkip.bebadriver.R;
import com.techkip.bebadriver.account.ShowAllAccount;
import com.techkip.bebadriver.common.HttpServicesClass;
import com.techkip.bebadriver.common.request_content;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.techkip.bebadriver.common.Constants.URLs.driverHistory;



public class History extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeRefreshLayout;
    ListView HouseListView;
    ProgressBar progressBar;
    String driverId;
    List<String> IdList = new ArrayList<>();
    FloatingActionButton Acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        HouseListView = (ListView) findViewById(R.id.lv_houses);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Acc=findViewById(R.id.fab_account);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        driverId= (getIntent().getStringExtra("driverId"));



        new GetHttpResponse(History.this).execute();
        Acc.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(Account account) {
                String driverId =account.getId();
                Intent intent = new Intent(History.this, ShowAllAccount.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);


            }

            @Override
            public void onError(AccountKitError accountKitError) {

            }
        });
    }
});

    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refreshing......", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }


    // JSON parse class started from here.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        String JSonResult;

        List<request_content> HouseList;

        public GetHttpResponse(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Passing HTTP URL to HttpServicesClass Class.
            HttpServicesClass httpServicesClass = new HttpServicesClass(driverHistory + driverId);
            try {
                httpServicesClass.ExecutePostRequest();

                if (httpServicesClass.getResponseCode() == 200) {
                    JSonResult = httpServicesClass.getResponse();

                    if (JSonResult != null) {
                        JSONArray jsonArray = null;

                        try {
                            jsonArray = new JSONArray(JSonResult);

                            JSONObject jo;

                            request_content postcontent;

                            HouseList = new ArrayList<request_content>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                postcontent = new request_content();

                                jo = jsonArray.getJSONObject(i);


                                // Adding  TO IdList Array.
                                IdList.add(jo.getString("id").toString());
                                postcontent.driverId = jo.getString("driver_id").toString();
                                postcontent.travelDate = jo.getString("travel_date").toString();
                                postcontent.travelDistance = jo.getString("travel_distance").toString();
                                postcontent.amountPaid = jo.getString("amount_paid").toString();
                                postcontent.travelFrom = jo.getString("travel_from").toString();
                                postcontent.travelTo = jo.getString("travel_to").toString();
                                HouseList.add(postcontent);

                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(context, httpServicesClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result)

        {
            if (HouseList == null) {
                Toast.makeText(context, "Sorry! Unable to load,retry", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                HouseListView.setVisibility(View.VISIBLE);
                ListAdapterClass adapter = new ListAdapterClass(HouseList, context);
                HouseListView.setAdapter(adapter);

            }


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Intent intent = new Intent(History.this, History.class);
            intent.putExtra("driverId", driverId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
