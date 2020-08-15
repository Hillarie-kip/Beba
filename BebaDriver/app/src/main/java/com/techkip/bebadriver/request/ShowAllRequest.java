package com.techkip.bebadriver.request;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.HttpServicesClass;
import com.techkip.bebadriver.common.request_content;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.techkip.bebadriver.common.Constants.URLs.filterrequestlat;



public class ShowAllRequest extends AppCompatActivity {

    ListView HouseListView;
    ProgressBar progressBar;
    String tvlat;
    List<String> IdList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_request_list);
        HouseListView = (ListView) findViewById(R.id.lv_houses);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        tvlat=(getIntent().getStringExtra("lat"));

        //tvlat = ("224682844752251");



        new GetHttpResponse(ShowAllRequest.this).execute();


        //Adding ListView Item click Listener.
        HouseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // TODO Auto-generated method stub

                Intent intent = new Intent(ShowAllRequest.this, ShowSingleRequest.class);
                // Sending ListView clicked value using intent.
                intent.putExtra("ListViewValue", IdList.get(position).toString());
                startActivity(intent);//Finishing current activity after open next activity.
                finish();

            }
        });
    }


    // JSON parse class started from here.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        String JSonResult;

        List<request_content> RequestList;

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
            HttpServicesClass httpServicesClass = new HttpServicesClass(filterrequestlat + tvlat);
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

                            RequestList = new ArrayList<request_content>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                postcontent = new request_content();

                                jo = jsonArray.getJSONObject(i);

                                // Adding  TO IdList Array.
                                IdList.add(jo.getString("id").toString());
                                postcontent.driverId = jo.getString("driver_id").toString();
                                postcontent.riderId = jo.getString("rider_id").toString();
                                postcontent.riderName = jo.getString("rider_name").toString();
                                postcontent.riderPhone = jo.getString("rider_phone").toString();
                                postcontent.riderImage = jo.getString("rider_image").toString();
                                postcontent.riderLng = jo.getString("rider_lng").toString();
                                postcontent.riderLat = jo.getString("rider_lat").toString();
                                postcontent.riderLoc = jo.getString("loc_name").toString();
                                postcontent.riderDest = jo.getString("dest_name").toString();
                                RequestList.add(postcontent);


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
            if (RequestList == null) {
                Toast.makeText(context, "Sorry! refresh again", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } else {
             /* progressBar.setVisibility(View.GONE);
                HouseListView.setVisibility(View.VISIBLE);
                ListAdapterClass adapter = new ListAdapterClass(RequestList, context);
                HouseListView.setAdapter(adapter);
*/
                Intent intent = new Intent(ShowAllRequest.this, ShowSingleRequest.class);
                // Sending ListView clicked value using intent.
                int position = 0;
                intent.putExtra("ListViewValue", IdList.get(position).toString());
                startActivity(intent);//Finishing current activity after open next activity.
                finish();



            }


        }


    }
    @Override
    public void onBackPressed() {
      //  Toast.makeText(this, "Contact your customer", Toast.LENGTH_SHORT).show();
        super.onBackPressed();

    }
}
