package com.techkip.bebadriver.request;

/**
 * Created by Juned on 3/4/2017.
 */

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.PicassoImage;
import com.techkip.bebadriver.common.request_content;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListAdapterClass extends BaseAdapter {

    Context context;
    List<request_content> valueList;
    Geocoder geocoder;
    List<Address> addresses;


    public ListAdapterClass(List<request_content> listValue, Context context) {
        this.context = context;
        this.valueList = listValue;
    }

    @Override
    public int getCount() {
        return this.valueList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.valueList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_request_content, parent, false);
        }

        TextView tv_driverId = convertView.findViewById(R.id.tv_driverId);
        TextView tv_riderId = convertView.findViewById(R.id.tv_riderId);
        TextView tv_riderName = convertView.findViewById(R.id.tv_riderName);
        TextView tv_riderPhone = convertView.findViewById(R.id.tv_riderPhone);
        CircleImageView iv_riderImage = convertView.findViewById(R.id.iv_RiderProfile);

        TextView tv_rideLong = convertView.findViewById(R.id.tv_riderLong);
        TextView tv_rideLati = convertView.findViewById(R.id.tv_riderLati);
        TextView tv_rideLoc = convertView.findViewById(R.id.tv_riderFrom);
        TextView tv_rideDest = convertView.findViewById(R.id.tv_riderTo);

        TextView tv_rideDestName = convertView.findViewById(R.id.tv_riderToNameTo);



        final request_content content = (request_content) this.getItem(position);

        tv_driverId.setText(content.getDriverId());
        tv_riderId.setText(content.getRiderId());
        tv_riderName.setText(content.getRiderName());
        tv_riderPhone.setText(content.getRiderPhone());

        tv_rideLong.setText(content.getRiderLng());
        tv_rideLati.setText(content.getRiderLat());

        tv_rideLoc.setText(content.getRiderLoc());
        tv_rideDest.setText(content.getRiderDest());

        double latitude= Double.parseDouble(content.getRiderLoc());
        double longitude=Double.parseDouble(String.valueOf(content.getRiderDest()));
        // tv_id.setText(IdHolder);
        try {

            addresses = geocoder.getFromLocation(latitude, longitude,1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String destination = addresses.get(0).getCountryCode(); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        tv_rideDestName.setText(destination);

        PicassoImage.downloadImage(context, content.getRiderImage(), iv_riderImage);

        return convertView;
    }

}



