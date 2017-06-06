package me.jakemoritz.tasking.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import me.jakemoritz.tasking.R;

public class HelperActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Check sign-in state
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_prefs_account), 0);
        boolean isLoggedIn = sharedPreferences.getBoolean(getString(R.string.shared_prefs_logged_in), false);

        if (isLoggedIn){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

}
