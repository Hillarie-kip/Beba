package com.techkip.bebarider;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.techkip.bebarider.common.Common;
import com.techkip.bebarider.model.Rate;


import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button BtnSubmit;
    MaterialRatingBar Ratingbar;
    MaterialEditText EtComment;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    double ratingStars=0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database=FirebaseDatabase.getInstance();

        rateDetailRef=database.getReference(Common.rate_detail_table);
        driverInformationRef=database.getReference(Common.user_driver_table);

        BtnSubmit = findViewById(R.id.btn_SubmitRate);
        Ratingbar=findViewById(R.id.ratingBar);
        EtComment =findViewById(R.id.et_CommentTxt);
     //   Toast.makeText(this, ""+Common.driverId, Toast.LENGTH_SHORT).show();
        Ratingbar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars=rating;

            }
        });

        BtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRateDetail(Common.driverId);
            }
        });
    }

    private void submitRateDetail(final String driverId) {
        final SpotsDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();

        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(EtComment.getText().toString());
        //update new data

        rateDetailRef.child(driverId)
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        rateDetailRef.child(driverId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                double averageStars = 0.0;
                                int count =0;
                                for (DataSnapshot postSnapshot:dataSnapshot.getChildren())
                                {
                                    Rate rate =postSnapshot.getValue(Rate.class);
                                    averageStars+=Double.parseDouble(rate.getRates());
                                    count++;

                                }
                                double finalaverage=averageStars/count;
                                DecimalFormat df = new DecimalFormat("#.#");
                                String valueUpdate = df.format(finalaverage);

                                //create object
                                Map<String,Object> driverUpdateRate = new HashMap<>();
                                driverUpdateRate.put("rates",valueUpdate);
                                driverInformationRef.child(Common.driverId).updateChildren(driverUpdateRate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        alertDialog.dismiss();
                                        Toast.makeText(RateActivity.this, "Thank you for rating and have a nice time", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(RateActivity.this, Home.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();


                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        alertDialog.dismiss();
                                        Toast.makeText(RateActivity.this, "updated but cant write to db failed", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.dismiss();
                Toast.makeText(RateActivity.this, "Failed Try Again", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RateActivity.this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}