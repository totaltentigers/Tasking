package me.jakemoritz.tasking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.jakemoritz.tasking.helper.SharedPrefsHelper;

public class HelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check sign-in state
        if (SharedPrefsHelper.getInstance().isLoggedIn()){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
