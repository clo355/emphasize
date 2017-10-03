package com.cl.emphasize;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CharSequence testPrintText = "ListView Settings: UI light medium dark\n" +
                "notes display size\n" +
                "editor text size\n" +
                "Help\n";

        TextView testPrint = (TextView)findViewById(R.id.testPrint);
        testPrint.setText(testPrintText);

        Button settingsBackButton = (Button)findViewById(R.id.settingsBackButton);
        settingsBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }
}
