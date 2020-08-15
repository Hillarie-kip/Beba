package com.techkip.bebadriver.account;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import com.techkip.bebadriver.R;

import com.techkip.bebadriver.common.HttpServicesClass;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.techkip.bebadriver.common.Constants.URLs.driverAccount;


public class ShowAllAccount extends AppCompatActivity  implements SwipeRefreshLayout.OnRefreshListener {
    SwipeRefreshLayout swipeRefreshLayout;

    ListView HouseListView;
    ProgressBar progressBar;
    String driverId;
    List<String> IdList = new ArrayList<>();
    FloatingActionButton Acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);
        HouseListView = (ListView) findViewById(R.id.lv_houses);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        Acc=findViewById(R.id.fab_account);

        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        driverId= (getIntent().getStringExtra("driverId"));


        new GetHttpResponse(ShowAllAccount.this).execute();


    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refreshing......", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ShowAllAccount.this, ShowAllAccount.class);
                intent.putExtra("driverId", driverId);
                startActivity(intent);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    // JSON parse class started from here.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        String JSonResult;

        List<account_content> HouseList;

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
            HttpServicesClass httpServicesClass = new HttpServicesClass(driverAccount +driverId);
            try {
                httpServicesClass.ExecutePostRequest();

                if (httpServicesClass.getResponseCode() == 200) {
                    JSonResult = httpServicesClass.getResponse();

                    if (JSonResult != null) {
                        JSONArray jsonArray = null;

                        try {
                            jsonArray = new JSONArray(JSonResult);

                            JSONObject jo;

                            account_content postcontent;

                            HouseList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                postcontent = new account_content();

                                jo = jsonArray.getJSONObject(i);

                                   // Adding  TO IdList Array.
                               // IdList.add(jo.getString("id").toString());
                                postcontent.driverId = jo.getString("driver_id").toString();
                                postcontent.driverFName = jo.getString("first_name").toString();
                                postcontent.driverLName = jo.getString("last_name").toString();
                                postcontent.driverLicenceId = jo.getString("driver_licence").toString();
                                postcontent.driverPhone = jo.getString("phone_number").toString();
                                postcontent.driverTotalDistance = jo.getString("total_distance").toString();
                                postcontent.driverTotalAmountEarned = jo.getString("total_earned").toString();
                                postcontent.driverTotalAmountDebt = jo.getString("amount_debt").toString();
                                postcontent.driverTotalAmountPaid = jo.getString("mpesa_paid").toString();
                                postcontent.driverTotalBalance = jo.getString("balance").toString();
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
            }
            else {
              progressBar.setVisibility(View.GONE);
                HouseListView.setVisibility(View.VISIBLE);
                ListAdapterClass adapter = new ListAdapterClass(HouseList, context);
                HouseListView.setAdapter(adapter);


            }


        }


    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
