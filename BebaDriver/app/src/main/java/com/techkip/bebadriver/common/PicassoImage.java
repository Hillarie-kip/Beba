package com.techkip.bebadriver.common;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.techkip.bebadriver.R;


/**
 * Created by hillarie on 12/7/2017.
 */
public class PicassoImage {

    public static void downloadImage(Context c, String imageUrl, ImageView img) {
        if (imageUrl != null && imageUrl.length() > 0) {
            Picasso.with(c).load(imageUrl).placeholder(R.mipmap.ic_user).into(img);

        } else {
            Picasso.with(c).load(R.mipmap.ic_no_image).into(img);
        }
    }
}
