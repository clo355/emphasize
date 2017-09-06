package com.cl.emphasize;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SaveAsActivity extends AppCompatActivity {

    boolean isNewFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_as);

        isNewFile = getIntent().getExtras().getBoolean("isNewFile");

        Intent returnIntent = new Intent();
        returnIntent.putExtra("isNewFile", isNewFile);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
