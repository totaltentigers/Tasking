package me.jakemoritz.tasking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class HelperActivity extends AppCompatActivity {

    static final int IS_LOGGED_IN_REQUEST = 1;

    private boolean isLoggedIn = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IS_LOGGED_IN_REQUEST){
            if (resultCode == RESULT_OK){
                isLoggedIn = data.getBooleanExtra("isLoggedIn", false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent checkIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(checkIntent, IS_LOGGED_IN_REQUEST);

        if (isLoggedIn){
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

}
