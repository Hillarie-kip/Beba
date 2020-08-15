package com.techkip.bebarider.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.techkip.bebarider.R;


/**
 * ilitengenezwa na hillarie on 3/16/18.
 */

public class CustomerWindowInfo implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomerWindowInfo(Context context) {
        myView = LayoutInflater.from(context)
                .inflate(R.layout.customer_info_window_rider,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView txtPickUPTitle = ((TextView)myView.findViewById(R.id.tv_PickupInfo));
        txtPickUPTitle.setText(marker.getTitle());

        TextView txtPickUPSnippet = ((TextView)myView.findViewById(R.id.tv_PickupSnippet));
        txtPickUPSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
