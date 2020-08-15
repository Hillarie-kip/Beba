package com.techkip.bebadriver.account;

/**
 * Created by Hillarie on 3/4/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.Common;
import com.techkip.bebadriver.common.PicassoImage;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListAdapterClass extends BaseAdapter {

    Context context;
    List<account_content> valueList;

    public ListAdapterClass(List<account_content> listValue, Context context) {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_account_content, parent, false);
        }

        TextView tv_driverId = convertView.findViewById(R.id.tv_driverId);
        TextView tv_driverFName = convertView.findViewById(R.id.tv_first_name);
        TextView tv_driverLName = convertView.findViewById(R.id.tv_last_name);
        TextView tv_driverNationalId = convertView.findViewById(R.id.tv_national_id);
        TextView tv_driverPhone = convertView.findViewById(R.id.tv_phone);
        TextView tv_driverTotalDistance = convertView.findViewById(R.id.tv_distance_covered);
        TextView tv_driverTotalEarned = convertView.findViewById(R.id.tv_amount_earned);
        TextView tv_driverTotalDebt = convertView.findViewById(R.id.tv_amount_debt);
        TextView tv_driverTotalPaid = convertView.findViewById(R.id.tv_amount_paid);
        TextView tv_driverTotalBalance = convertView.findViewById(R.id.tv_amount_balance);
        CircleImageView iv_driverImage = convertView.findViewById(R.id.iv_driverImage);



        final account_content content = (account_content) this.getItem(position);

        tv_driverId.setText(content.getDriverId());
        tv_driverFName.setText(content.getDriverFName());
        tv_driverLName.setText(content.getDriverLName());
        tv_driverNationalId.setText(content.getDriverLicenceId());
        tv_driverPhone.setText(content.getDriverPhone());
        tv_driverTotalDistance.setText(content.getDriverTotalDistance());
        tv_driverTotalEarned.setText(content.getDriverTotalAmountEarned());
        tv_driverTotalDebt.setText(content.getDriverTotalAmountDebt());
        tv_driverTotalPaid.setText(content.getDriverTotalAmountPaid());
        tv_driverTotalBalance.setText(content.getDriverTotalBalance());

        PicassoImage.downloadImage(context, Common.currentDriver.getProfilePicUrl(), iv_driverImage);

        return convertView;
    }

}



