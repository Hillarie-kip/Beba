package com.techkip.bebarider.history;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.techkip.bebarider.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.techkip.bebarider.common.Constants.URLs.riderHistory;



public class History extends AppCompatActivity {

    ListView HouseListView;
    ProgressBar progressBar;
    String riderId;
    List<String> IdList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        HouseListView = (ListView) findViewById(R.id.lv_houses);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        riderId= (getIntent().getStringExtra("riderId"));


        new GetHttpResponse(History.this).execute();


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
            HttpServicesClass httpServicesClass = new HttpServicesClass(    riderHistory + riderId);
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
    public void onBackPressed() {
        super.onBackPressed();

    }
}
