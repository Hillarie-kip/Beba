package com.techkip.bebadriver.stupdstuff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.techkip.bebadriver.R;

public class PrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        setTitle("Privacy");
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
          if (id == R.id.menu_help) {
                Intent intent = new Intent(PrivacyActivity.this, HelpActivity.class);
                startActivity(intent);

        }
        if (id == R.id.menu_terms) {
            Intent intent = new Intent(PrivacyActivity.this, HelpActivity.class);
            startActivity(intent);

        }
        if (id == R.id.menu_about) {
            Intent intent = new Intent(PrivacyActivity.this, AboutActivity.class);
            startActivity(intent);

        }
        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }
}
