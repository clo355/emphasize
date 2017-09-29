package com.cl.emphasize;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "PreferenceFile";
    boolean someSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //This is how to get existing values or defaults
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean someSetting = settings.getBoolean("someSetting", true);

        TextView testPrint = (TextView)findViewById(R.id.testPrint);
        CharSequence testPrintText = "someSetting was: " + String.valueOf(someSetting);
        testPrint.setText(testPrintText);

        Button settingsBackButton = (Button)findViewById(R.id.settingsBackButton);
        settingsBackButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putBoolean("someSetting", true);
                settingsEditor.commit();
                finish();
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        //This is how to edit/update settings
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("someSetting", false);
        settingsEditor.commit();
    }
}
