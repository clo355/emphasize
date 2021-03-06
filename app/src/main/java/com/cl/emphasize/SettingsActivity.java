package com.cl.emphasize;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author Chris Lo
 */

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "PreferenceFile";
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final int globalTheme = settings.getInt("globalTheme", R.style.lightTheme);
        setTheme(globalTheme);

        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);

        Button themeButton = (Button)findViewById(R.id.themeButton);
        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle("Theme");
                CharSequence[] themeChoices = {"Light", "Dark"};
                int currentlySelected;
                if(globalTheme == R.style.lightTheme){
                    currentlySelected = 0;
                } else{
                    currentlySelected = 1;
                }
                builder.setSingleChoiceItems(themeChoices, currentlySelected, new DialogInterface.OnClickListener(){
                    Handler myHandler = new Handler();
                    @Override
                    public void onClick(final DialogInterface dialog, int whichRadio){
                        switch(whichRadio){
                            case 0:{ //Light
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("globalTheme", R.style.lightTheme);
                                        editor.commit();
                                        dialog.cancel();
                                        recreate();
                                    }
                                }, 200);
                                break;
                            }
                            case 1:{ //Dark
                                myHandler.postDelayed(new Runnable() {
                                    public void run(){
                                        SharedPreferences.Editor editor = settings.edit();
                                        editor.putInt("globalTheme", R.style.darkTheme);
                                        editor.commit();
                                        dialog.cancel();
                                        recreate();
                                    }
                                }, 200);
                                break;
                            }
                        }
                    }
                });
                builder.create().show();
            }
        });

        Button syncButton = (Button)findViewById(R.id.syncButton);
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //initialized toast in onCreate()
                CharSequence toastText = "This function is being upgraded.\nYou will be notified when it is available.";
                toast.setText(toastText); //setting text ends previous toast
                toast.show();
            }
        });

        Button helpButton = (Button)findViewById(R.id.helpButton);
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent helpIntent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(helpIntent);
                overridePendingTransition(0, 0);
            }
        });

        Button backButton = (Button)findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent(getApplicationContext(), MainActivity.class);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        //main fades in. settings slides out right
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
