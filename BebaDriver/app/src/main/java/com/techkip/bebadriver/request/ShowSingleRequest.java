package com.techkip.bebadriver.request;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.PicassoImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.techkip.bebadriver.common.Common.RiderIdHolder;
import static com.techkip.bebadriver.common.Constants.URLs.singlerequestfilter;



public class ShowSingleRequest extends AppCompatActivity {

    Connector httpParse = new Connector();
    String finalResult;
    HashMap<String, String> hashMap = new HashMap<>();
    String ParseResult;
    HashMap<String, String> ResultHash = new HashMap<>();
    String FinalJSonObject;


    TextView tv_id,tv_driverid, tv_riderid, tv_riderphone, tv_ridername,tv_riderloc,tv_riderdest;
    CircleImageView iv_riderimage;

    String IdHolder, DriverIdHolder, RiderNameHolder, RiderPhoneHolder,RiderImageHolder,RiderLocHolder,RiderDestHolder;
    Button btnCall, btnSms,btnCtn;
    String TempItem;
    ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_single);

        //tv_id = findViewById(R.id.tv_riderId);
        tv_driverid = findViewById(R.id.tv_driverId);
        tv_riderid = findViewById(R.id.tv_riderId);
        tv_ridername = findViewById(R.id.tv_riderName);
        tv_riderphone = findViewById(R.id.tv_riderPhone);
        iv_riderimage = findViewById(R.id.iv_RiderProfile);
        tv_riderloc=findViewById(R.id.tv_riderFrom);
        tv_riderdest=findViewById(R.id.tv_riderTo);


        btnCall = findViewById(R.id.btn_call_phone);
        btnSms = findViewById(R.id.btn_sms_phone);
        btnCtn = findViewById(R.id.btn_continue);

        btnCtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        //Receiving the ListView Clicked item value send by previous activity.
        TempItem = getIntent().getStringExtra("ListViewValue");
        //Calling method to filter Post_content Record and open selected record.
        HttpWebCall(TempItem);


        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //   Toast.makeText(ShowSingleAccount.this, tv_riderphone.getText().toString(), Toast.LENGTH_SHORT).show();

                call(tv_riderphone.getText().toString());
            }
        });
        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
                smsIntent.setType("vnd.android-dir/mms-sms");
                smsIntent.setData(Uri.parse("sms:" + tv_riderphone.getText().toString()));

            }
        });
    }

      private void call(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));


    }




    //Method to show current record Current Selected Record
    public void HttpWebCall(final String PreviousListViewClickedItem) {

        class HttpWebCallFunction extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pd = ProgressDialog.show(ShowSingleRequest.this, null, "Loading! Please wait....", true, true);
            }

            @Override
            protected void onPostExecute(String httpResponseMsg) {

                super.onPostExecute(httpResponseMsg);

                pd.dismiss();

                //Storing Complete JSon Object into String Variable.
                FinalJSonObject = httpResponseMsg;

                //Parsing the Stored JSOn String to GetHttpResponse Method.
                new GetHttpResponse(ShowSingleRequest.this).execute();

            }

            @Override
            protected String doInBackground(String... params) {

                ResultHash.put("ID", params[0]);

                ParseResult = httpParse.connect(ResultHash, singlerequestfilter);

                return ParseResult;
            }
        }

        HttpWebCallFunction httpWebCallFunction = new HttpWebCallFunction();

        httpWebCallFunction.execute(PreviousListViewClickedItem);
    }


    // Parsing Complete JSON Object.
    private class GetHttpResponse extends AsyncTask<Void, Void, Void> {
        public Context context;

        public GetHttpResponse(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                if (FinalJSonObject != null) {
                    JSONArray ja = null;

                    try {
                        ja = new JSONArray(FinalJSonObject);

                        JSONObject jo;

                        for (int i = 0; i < ja.length(); i++) {
                            jo = ja.getJSONObject(i);

                            // Storing Post_content Name, Phone Number, Class into Variables.
                            DriverIdHolder = jo.getString("driver_id").toString();
                            RiderIdHolder = jo.getString("rider_id").toString();
                            RiderNameHolder = jo.getString("rider_name").toString();
                            RiderPhoneHolder = jo.getString("rider_phone").toString();
                            RiderImageHolder = jo.getString("rider_image").toString();
                            RiderLocHolder=jo.getString("loc_name").toString();
                            RiderDestHolder=jo.getString("dest_name").toString();

                            IdHolder = jo.getString("id").toString();


                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            // Setting Post_content Name, Phone Number, Class into TextView after done all process .
            tv_driverid.setText(DriverIdHolder);
            tv_riderid.setText(RiderIdHolder);
            tv_riderphone.setText(RiderPhoneHolder);
            tv_ridername.setText(RiderNameHolder);
            tv_riderloc.setText(RiderLocHolder);
            tv_riderdest.setText(RiderDestHolder);
            PicassoImage.downloadImage(context, RiderImageHolder, iv_riderimage);



        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
