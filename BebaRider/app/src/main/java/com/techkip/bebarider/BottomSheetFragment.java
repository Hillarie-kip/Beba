package com.techkip.bebarider;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ilitengenezwa na hillarie on 3/16/18.
 */

public class BottomSheetFragment extends BottomSheetDialogFragment {
    String mLocation, mDestination;
    boolean isTapOnMap;
    IGoogleAPI mService;
    TextView txtLocation, txtDestination, TxtBronzeprice, TxtSilverprice, TxtGoldprice,TxtBodaprice,TxtDistance;

    public static BottomSheetFragment newInstance(String location, String destination, boolean isTapOnMap) {

        BottomSheetFragment f = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("location", location);
        args.putString("destination", destination);
        args.putBoolean("isTapOnMap",isTapOnMap);
        f.setArguments(args);
        return f;

    }


    //press crtl o

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = getArguments().getString("location");
        mDestination = getArguments().getString("destination");
        isTapOnMap = getArguments().getBoolean("isTapOnMap");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheetrider, container, false);

        txtLocation = view.findViewById(R.id.txtLocation);
        txtDestination = view.findViewById(R.id.txtDestination);


        TxtBronzeprice = view.findViewById(R.id.txt_x_price);
        TxtSilverprice = view.findViewById(R.id.txt_vip_price);
        TxtGoldprice = view.findViewById(R.id.txt_tuktuk_price);
        TxtBodaprice = view.findViewById(R.id.txt_boda_price);
        TxtDistance = view.findViewById(R.id. txtDistance);

        mService = Common.getGoogleService();
        getPrice(mLocation, mDestination);


        if (!isTapOnMap) {
            txtLocation.setText(mLocation);
            txtDestination.setText(mDestination);
        }

        return view;


    }

    private void getPrice(String mLocation, String mDestination) {
        String requestUrl = null;
        try {
            requestUrl = "https://maps.googleapis.com/maps/api/directions" +
                    "/json?mode=driving&transit_routing_preference=less_driving&origin=" + mLocation + "&destination=" + mDestination + "&key=" + getResources().getString(R.string.google_direction_api);//browser api key

            Log.d("Hillare", requestUrl);//PRINT Url for debug

            mService.getPath(requestUrl)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                //get distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");

                                //use regex to extract double from string remove all text;

                                Double double_distance = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+", ""));

                                JSONObject time = legsObject.getJSONObject("duration");
                                String time_text = time.getString("text");

                                Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+", ""));

                                String total_distance = String.format("%s + %s", time_text,distance_text);
                                String total_calculateBronze = String.format("Ksh %.2f", Common.formulaCostBronze(double_distance, time_value));
                                String total_calculateSilver = String.format("Ksh %.2f", Common.formulaCostSilver(double_distance, time_value));
                                String total_calculateGold = String.format("Ksh %.2f",  Common.formulaCostGold(double_distance, time_value));
                                String total_calculateBoda = String.format("Ksh %.2f",  Common.formulaCostBoda(double_distance, time_value));


                                TxtBronzeprice.setText(total_calculateBronze);
                                TxtSilverprice.setText(total_calculateSilver);
                                TxtGoldprice.setText(total_calculateGold);
                                TxtBodaprice.setText(total_calculateBoda);
                                TxtDistance.setText(total_distance);

                                if (isTapOnMap){

                                    String start_address = legsObject.getString("start_address");
                                    String end_address = legsObject.getString("end_address");

                                    txtLocation.setText(start_address);
                                    txtDestination.setText(end_address);

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d("Google Maps", t.getMessage());

                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
