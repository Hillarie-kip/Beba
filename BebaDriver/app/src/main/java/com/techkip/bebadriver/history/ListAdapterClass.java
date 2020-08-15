package com.techkip.bebadriver.history;

/**
 * Created by Juned on 3/4/2017.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.techkip.bebadriver.R;
import com.techkip.bebadriver.common.request_content;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListAdapterClass extends BaseAdapter {

    Context context;
    List<request_content> valueList;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.history_content, parent, false);
        }

        TextView tv_Date = convertView.findViewById(R.id.tv_date);
        TextView tv_Distance = convertView.findViewById(R.id.tv_distance);
        TextView tv_Amount = convertView.findViewById(R.id.tv_amount);
        TextView tv_From = convertView.findViewById(R.id.tv_from);
        TextView tv_To = convertView.findViewById(R.id.tv_to);



        final request_content content = (request_content) this.getItem(position);

        tv_Date.setText(content.getTravelDate());
        tv_Distance.setText(content.getTravelDistance());
        tv_Amount.setText(content.getAmountPaid());
        tv_From.setText(content.getTravelFrom());
        tv_To.setText(content.getTravelTo());


        return convertView;
    }

}



